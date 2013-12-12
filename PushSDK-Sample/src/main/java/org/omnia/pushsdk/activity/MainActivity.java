/* Copyright (c) 2013 Pivotal Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.omnia.pushsdk.activity;

import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import org.omnia.pushsdk.dialogfragment.SendMessageDialogFragment;
import org.omnia.pushsdk.service.GcmIntentService;
import org.omnia.pushsdk.adapter.LogAdapter;
import org.omnia.pushsdk.model.LogItem;
import org.omnia.pushsdk.PushLib;
import org.omnia.pushsdk.R;
import org.omnia.pushsdk.RegistrationParameters;
import org.omnia.pushsdk.util.Settings;
import org.omnia.pushsdk.dialogfragment.ClearRegistrationDialogFragment;
import org.omnia.pushsdk.dialogfragment.LogItemLongClickDialogFragment;
import org.omnia.pushsdk.model.BackEndMessageRequest;
import org.omnia.pushsdk.model.GcmMessageRequest;
import org.omnia.pushsdk.registration.RegistrationListener;
import org.omnia.pushsdk.util.Const;
import org.omnia.pushsdk.util.PushLibLogger;

import com.xtreme.commons.DebugUtil;
import com.xtreme.commons.StringUtil;
import com.xtreme.commons.ThreadUtil;
import com.xtreme.network.NetworkError;
import com.xtreme.network.NetworkRequest;
import com.xtreme.network.NetworkRequestLauncher;
import com.xtreme.network.NetworkRequestListener;
import com.xtreme.network.NetworkResponse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    private static final String GCM_SEND_MESSAGE_URL = "https://android.googleapis.com/gcm/send";
    private static final String BACK_END_SEND_MESSAGE_URL = "http://ec2-54-234-124-123.compute-1.amazonaws.com:8090/v1/push";

    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.SSS");
    private static final int[] baseRowColours = new int[]{0xddeeff, 0xddffee, 0xffeedd};

    private static int currentBaseRowColour = 0;
    private static List<LogItem> logItems = new ArrayList<LogItem>();

    private ListView listView;
    private LogAdapter adapter;
    private PushLib pushLib;
    private String logAsString;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        adapter = new LogAdapter(getApplicationContext(), logItems);
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setDividerHeight(0);
        listView.setLongClickable(true);
        listView.setOnItemLongClickListener(getLogItemLongClickListener());
        PushLibLogger.setup(this, Const.TAG_NAME);
        PushLibLogger.setListener(getLogListener());
        if (logItems.isEmpty()) {
            addLogMessage("Press the \"Register\" button to attempt registration.");
        }
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        scrollToBottom();
        clearNotifications();
    }

    private void clearNotifications() {
        final NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(GcmIntentService.NOTIFICATION_ID);
    }

    private void startRegistration() {
        updateCurrentBaseRowColour();
        addLogMessage("Starting registration...");

        final String gcmSenderId = Settings.getGcmSenderId(this);
        final String releaseUuid = Settings.getReleaseUuid(this);
        final String releaseSecret = Settings.getReleaseSecret(this);
        final String deviceAlias = Settings.getDeviceAlias(this);
        addLogMessage("GCM Sender ID: '" + gcmSenderId + "'\nRelease UUID: '" + releaseUuid + "'\nRelease Secret: '" + releaseSecret + "'\nDevice Alias: '" + deviceAlias + "'");

        final RegistrationParameters parameters = new RegistrationParameters(gcmSenderId, releaseUuid, releaseSecret, deviceAlias);
        try {
            pushLib = PushLib.init(this);
            pushLib.startRegistration(parameters, new RegistrationListener() {

                @Override
                public void onRegistrationComplete() {
                    queueLogMessage("Registration successful.");
                }

                @Override
                public void onRegistrationFailed(String reason) {
                    queueLogMessage("Registration failed. Reason is '" + reason + "'.");
                }
            });
        } catch (Exception e) {
            queueLogMessage("Registration failed: " + e.getLocalizedMessage());
        }
    }

    public PushLibLogger.Listener getLogListener() {
        return new PushLibLogger.Listener() {
            @Override
            public void onLogMessage(String message) {
                addLogMessage(message);
            }
        };
    }

    private void queueLogMessage(final String message) {
        if (ThreadUtil.isUIThread()) {
            addLogMessage(message);
        } else {
            ThreadUtil.getUIThreadHandler().post(new Runnable() {
                @Override
                public void run() {
                    addLogMessage(message);
                }
            });
        }
    }

    private void addLogMessage(String message) {
        final String timestamp = getTimestamp();
        final LogItem logItem = new LogItem(timestamp, message, baseRowColours[currentBaseRowColour]);
        logItems.add(logItem);
        adapter.notifyDataSetChanged();
        scrollToBottom();
    }

    private String getTimestamp() {
        return dateFormatter.format(new Date());
    }

    private void scrollToBottom() {
        listView.setSelection(logItems.size() - 1);
    }

    private void updateCurrentBaseRowColour() {
        currentBaseRowColour = (currentBaseRowColour + 1) % baseRowColours.length;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_register:
                startRegistration();
                break;
            case R.id.action_clear_registration:
                clearRegistration();
                break;
            case R.id.action_edit_registration_parameters:
                editRegistrationParameters();
                break;
            case R.id.action_send_message:
                sendMessage();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void sendMessage() {
        if (!DebugUtil.getInstance(this).isDebuggable()) {
            Toast.makeText(this, "This feature does not work in release builds.", Toast.LENGTH_SHORT).show();
            return;
        }
        final DialogFragment dialog = new SendMessageDialogFragment(new SendMessageDialogFragment.Listener() {
            @Override
            public void onClickResult(int result) {
                if (result == SendMessageDialogFragment.VIA_GCM) {
                    sendMessageViaGcm();
                } else if (result == SendMessageDialogFragment.VIA_BACK_END) {
                    sendMessageViaBackEnd();
                }
            }
        });
        dialog.show(getSupportFragmentManager(), "SendMessageDialogFragment");
    }

    private void sendMessageViaBackEnd() {
        updateCurrentBaseRowColour();
        addLogMessage("Sending message via back-end server...");
        final String data = getBackEndMessageRequestString();
        queueLogMessage("Message body data: \"" + data + "\"");
        final NetworkRequest networkRequest = new NetworkRequest(BACK_END_SEND_MESSAGE_URL, new NetworkRequestListener() {

            @Override
            public void onSuccess(NetworkResponse networkResponse) {
                try {
                    int statusCode = networkResponse.getStatus().getStatusCode();
                    if (statusCode >= 200 && statusCode < 300) {
                        queueLogMessage("Back-end server accepted network request to send message. HTTP response status code is " + statusCode + ".");
                    } else {
                        queueLogMessage("Back-end server rejected network request to send message. HTTP response status code is " + statusCode + ".");
                    }
                } catch (Exception e) {
                    queueLogMessage("ERROR: got exception parsing network response from Back-end server: " + e.getLocalizedMessage());
                }
            }

            @Override
            public void onFailure(NetworkError networkError) {
                try {
                    queueLogMessage("ERROR sending request to Back-end server: " + networkError.getException().getLocalizedMessage());
                } catch (Exception e) {
                    queueLogMessage("ERROR got exception parsing network error: " + e.getLocalizedMessage());
                }
            }
        });
        networkRequest.setRequestType(NetworkRequest.RequestType.POST);
        networkRequest.addHeaderParam("Content-Type", "application/json");
        networkRequest.setBodyData(data);
        NetworkRequestLauncher.getInstance().executeRequest(networkRequest);
    }

    private String getBackEndMessageRequestString() {
        final String device_uuid = readIdFromFile("device_uuid");
        final String[] devices = new String[]{device_uuid};
        final String platforms = "android";
        final String messageTitle = "Sample Message Title";
        final String messageBody = "This message was sent to the back-end at " + getTimestamp() + "." ;
        final String appUuid = Settings.getBackEndAppUuid(this);
        final String appSecretKey = Settings.getBackEndAppSecretKey(this);
        final BackEndMessageRequest messageRequest = new BackEndMessageRequest(appUuid, appSecretKey, messageTitle, messageBody, platforms, devices);
        final Gson gson = new Gson();
        return gson.toJson(messageRequest);
    }

    private void sendMessageViaGcm() {
        updateCurrentBaseRowColour();
        final String data = getGcmMessageRequestString();
        queueLogMessage("Message body data: \"" + data + "\"");
        final NetworkRequest networkRequest = new NetworkRequest(GCM_SEND_MESSAGE_URL, new NetworkRequestListener() {

            @Override
            public void onSuccess(NetworkResponse networkResponse) {
                try {
                    int statusCode = networkResponse.getStatus().getStatusCode();
                    if (statusCode >= 200 && statusCode < 300) {
                        queueLogMessage("GCM server accepted network request to send message. HTTP response status code is " + statusCode + ".");
                    } else {
                        queueLogMessage("GCM server rejected network request to send message. HTTP response status code is " + statusCode + ".");
                    }
                } catch (Exception e) {
                    queueLogMessage("ERROR: got exception parsing network response from GCM server: " + e.getLocalizedMessage());
                }
            }

            @Override
            public void onFailure(NetworkError networkError) {
                try {
                    queueLogMessage("ERROR sending request to GCM server: " + networkError.getException().getLocalizedMessage());
                } catch (Exception e) {
                    queueLogMessage("ERROR got exception parsing network error: " + e.getLocalizedMessage());
                }
            }
        });
        networkRequest.setRequestType(NetworkRequest.RequestType.POST);
        networkRequest.addHeaderParam("Content-Type", "application/json");
        networkRequest.addHeaderParam("Authorization", "key=" + Settings.getGcmBrowserApiKey(this));
        networkRequest.setBodyData(data);
        NetworkRequestLauncher.getInstance().executeRequest(networkRequest);
    }

    private String getGcmMessageRequestString() {
        final String regId = readIdFromFile("gcm_registration_id");
        if (regId == null) {
            return null;
        }
        final String[] devices = new String[]{regId};
        final String message = "This message was sent to GCM at " + getTimestamp() + ".";
        final GcmMessageRequest messageRequest = new GcmMessageRequest(devices, message);
        final Gson gson = new Gson();
        return gson.toJson(messageRequest);
    }

    private String readIdFromFile(String idType) {
        final File externalFilesDir = getExternalFilesDir(null);
        if (externalFilesDir == null) {
            addLogMessage("ERROR: Was not able to get the externalFilesDir");
            return null;
        }
        final File dir = new File(externalFilesDir.getAbsolutePath() + File.separator + "pushlib");
        final File regIdFile = new File(dir, idType + ".txt");
        if (!regIdFile.exists() || !regIdFile.canRead()) {
            addLogMessage("ERROR: " + idType + " file not found (" + regIdFile.getAbsoluteFile() + "). Have you registered with GCM and the back-end successfully? Are you running a debug build? Is the external cache directory accessible?");
            return null;
        }
        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(regIdFile);
            br = new BufferedReader(fr);
            return br.readLine();

        } catch (Exception e) {
            addLogMessage("ERROR reading " + idType + " file:" + e.getLocalizedMessage());
            return null;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    // Swallow exception
                }
            }
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException e) {
                    // Swallow exception
                }
            }
        }
    }

    private void clearRegistration() {
        final DialogFragment dialog = new ClearRegistrationDialogFragment(new ClearRegistrationDialogFragment.Listener() {

            @Override
            public void onClickResult(int result) {
                if (result != ClearRegistrationDialogFragment.CLEAR_REGISTRATIONS_CANCELLED) {
                    final SharedPreferences.Editor editor = getSharedPreferences(Const.TAG_NAME, Context.MODE_PRIVATE).edit();
                    if (result == ClearRegistrationDialogFragment.CLEAR_REGISTRATIONS_FROM_GCM || result == ClearRegistrationDialogFragment.CLEAR_REGISTRATIONS_FROM_BOTH) {
                        addLogMessage("Clearing device registration from GCM");
                        editor.remove("gcm_sender_id");
                        editor.remove("gcm_device_registration_id");
                        editor.remove("app_version");
                    }
                    if (result == ClearRegistrationDialogFragment.CLEAR_REGISTRATIONS_FROM_BACK_END || result == ClearRegistrationDialogFragment.CLEAR_REGISTRATIONS_FROM_BOTH) {
                        addLogMessage("Clearing device registration from the back-end");
                        editor.remove("release_uuid");
                        editor.remove("release_secret");
                        editor.remove("device_alias");
                        editor.remove("backend_device_registration_id");
                    }
                    editor.commit();
                }
            }
        });
        dialog.show(getSupportFragmentManager(), "ClearRegistrationDialogFragment");
    }

    public AdapterView.OnItemLongClickListener getLogItemLongClickListener() {
        return new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, long id) {
                final int originalViewBackgroundColour = adapter.getBackgroundColour(position);
                final LogItem logItem = (LogItem) adapter.getItem(position);
                final DialogFragment dialog = new LogItemLongClickDialogFragment(new LogItemLongClickDialogFragment.Listener() {

                    @Override
                    public void onClickResult(int result) {
                        if (result == LogItemLongClickDialogFragment.COPY_ITEM) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                final ClipData clipData = ClipData.newPlainText("log item text", logItem.message);
                                final android.content.ClipboardManager clipboardManager = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                clipboardManager.setPrimaryClip(clipData);
                            } else {
                                final android.text.ClipboardManager clipboardManager = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                clipboardManager.setText(logItem.message);
                            }
                            Toast.makeText(MainActivity.this, "Log item copied to clipboard", Toast.LENGTH_SHORT).show();
                        } else if (result == LogItemLongClickDialogFragment.COPY_ALL_ITEMS) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                final ClipData clipData = ClipData.newPlainText("log text", getLogAsString());
                                final android.content.ClipboardManager clipboardManager = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                clipboardManager.setPrimaryClip(clipData);
                            } else {
                                final android.text.ClipboardManager clipboardManager = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                clipboardManager.setText(getLogAsString());
                            }
                            Toast.makeText(MainActivity.this, "Log copied to clipboard", Toast.LENGTH_SHORT).show();
                        } else if (result == LogItemLongClickDialogFragment.CLEAR_LOG) {
                            logItems.clear();
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
                dialog.show(getSupportFragmentManager(), "LogItemLongClickDialogFragment");
                return true;
            }
        };
    }

    private void editRegistrationParameters() {
        final Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public String getLogAsString() {
        final List<String> lines = new LinkedList<String>();
        for (final LogItem logItem : logItems) {
            lines.add(logItem.timestamp + "\t" + logItem.message);
        }
        return StringUtil.join(lines, "\n");
    }
}
