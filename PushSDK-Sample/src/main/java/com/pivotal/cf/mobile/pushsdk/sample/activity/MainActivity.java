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

package com.pivotal.cf.mobile.pushsdk.sample.activity;

import android.app.NotificationManager;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.pivotal.cf.mobile.pushsdk.PushLib;
import com.pivotal.cf.mobile.pushsdk.RegistrationParameters;
import com.pivotal.cf.mobile.pushsdk.database.DatabaseEventsStorage;
import com.pivotal.cf.mobile.pushsdk.database.EventsStorage;
import com.pivotal.cf.mobile.pushsdk.registration.RegistrationListener;
import com.pivotal.cf.mobile.pushsdk.registration.UnregistrationListener;
import com.pivotal.cf.mobile.pushsdk.sample.adapter.LogAdapter;
import com.pivotal.cf.mobile.pushsdk.sample.broadcastreceiver.MyPivotalCFMSRemotePushLibBroadcastReceiver;
import com.pivotal.cf.mobile.pushsdk.sample.dialogfragment.ClearRegistrationDialogFragment;
import com.pivotal.cf.mobile.pushsdk.sample.dialogfragment.LogItemLongClickDialogFragment;
import com.pivotal.cf.mobile.pushsdk.sample.model.BackEndMessageRequest;
import com.pivotal.cf.mobile.pushsdk.sample.model.GcmMessageRequest;
import com.pivotal.cf.mobile.pushsdk.sample.model.LogItem;
import com.pivotal.cf.mobile.pushsdk.sample.util.Settings;
import com.pivotal.cf.mobile.pushsdk.util.Const;
import com.pivotal.cf.mobile.pushsdk.util.DebugUtil;
import com.pivotal.cf.mobile.pushsdk.util.PushLibLogger;
import com.pivotal.cf.mobile.pushsdk.util.StringUtil;
import com.pivotal.cf.mobile.pushsdk.util.ThreadUtil;

import com.pivotal.cf.mobile.pushsdk.sample.dialogfragment.SendMessageDialogFragment;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    private static final String GCM_SEND_MESSAGE_URL = "https://android.googleapis.com/gcm/send";
    private static final String BACK_END_SEND_MESSAGE_URL = Const.BASE_SERVER_URL + "v1/push";

    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.SSS");
    private static final int[] baseRowColours = new int[]{0xddeeff, 0xddffee, 0xffeedd};

    private static int currentBaseRowColour = 0;
    private static List<LogItem> logItems = new ArrayList<LogItem>();

    private ListView listView;
    private LogAdapter adapter;
    private PushLib pushLib;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.pivotal.cf.mobile.pushsdk.sample.R.layout.activity_main);
        adapter = new LogAdapter(getApplicationContext(), logItems);
        listView = (ListView) findViewById(com.pivotal.cf.mobile.pushsdk.sample.R.id.listView);
        listView.setAdapter(adapter);
        listView.setDividerHeight(0);
        listView.setLongClickable(true);
        listView.setOnItemLongClickListener(getLogItemLongClickListener());
        PushLibLogger.setup(this, Const.TAG_NAME);
        PushLibLogger.setListener(getLogListener());
        if (logItems.isEmpty()) {
            addLogMessage("Press the \"Register\" button to attempt registration.");
        }
        PreferenceManager.setDefaultValues(this, com.pivotal.cf.mobile.pushsdk.sample.R.xml.preferences, false);
        pushLib = PushLib.init(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        scrollToBottom();
        clearNotifications();
    }

    private void clearNotifications() {
        final NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(MyPivotalCFMSRemotePushLibBroadcastReceiver.NOTIFICATION_ID);
    }

    private void startRegistration() {
        updateCurrentBaseRowColour();
        addLogMessage("Starting registration...");

        final String gcmSenderId = Settings.getGcmSenderId(this);
        final String variantUuid = Settings.getVariantUuid(this);
        final String variantSecret = Settings.getVariantSecret(this);
        final String deviceAlias = Settings.getDeviceAlias(this);
        addLogMessage("GCM Sender ID: '" + gcmSenderId + "'\nVariant UUID: '" + variantUuid + "\nVariant Secret: '" + variantSecret + "'\nDevice Alias: '" + deviceAlias + "'");

        final RegistrationParameters parameters = new RegistrationParameters(gcmSenderId, variantUuid, variantSecret, deviceAlias);
        try {
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
        menuInflater.inflate(com.pivotal.cf.mobile.pushsdk.sample.R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case com.pivotal.cf.mobile.pushsdk.sample.R.id.action_register:
                startRegistration();
                break;

            case com.pivotal.cf.mobile.pushsdk.sample.R.id.action_clear_message_receipts:
                clearMessageReceipts();
                break;

            case com.pivotal.cf.mobile.pushsdk.sample.R.id.action_clear_registration:
                clearRegistration();
                break;

            case com.pivotal.cf.mobile.pushsdk.sample.R.id.action_edit_registration_parameters:
                editRegistrationParameters();
                break;

            case com.pivotal.cf.mobile.pushsdk.sample.R.id.action_send_message:
                sendMessage();
                break;

            case com.pivotal.cf.mobile.pushsdk.sample.R.id.action_unregister:
                unregister();
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
        final File externalFilesDir = getExternalFilesDir(null);
        if (externalFilesDir == null) {
            Toast.makeText(this, "This feature requires the SD-card to be mounted.", Toast.LENGTH_SHORT).show();
            return;
        }
        final SendMessageDialogFragment.Listener listener = new SendMessageDialogFragment.Listener() {
            @Override
            public void onClickResult(int result) {
                if (result == SendMessageDialogFragment.VIA_GCM) {
                    sendMessageViaGcm();
                } else if (result == SendMessageDialogFragment.VIA_BACK_END) {
                    sendMessageViaBackEnd();
                }
            }
        };
        final SendMessageDialogFragment dialog = new SendMessageDialogFragment();
        dialog.setListener(listener);
        dialog.show(getSupportFragmentManager(), "SendMessageDialogFragment");
    }

    private void sendMessageViaBackEnd() {
        updateCurrentBaseRowColour();
        final String data = getBackEndMessageRequestString();
        if (data == null) {
            addLogMessage("Can not send message. Please register first.");
            return;
        }
        addLogMessage("Sending message via back-end server...");
        addLogMessage("Message body data: \"" + data + "\"");

        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                OutputStream outputStream = null;

                try {
                    final URL url = new URL(BACK_END_SEND_MESSAGE_URL);
                    final HttpURLConnection urlConnection = getUrlConnection(url);
                    urlConnection.setDoOutput(true);
                    urlConnection.connect();

                    outputStream = new BufferedOutputStream(urlConnection.getOutputStream());
                    writeConnectionOutput(data, outputStream);

                    final int statusCode = urlConnection.getResponseCode();
                    if (statusCode >= 200 && statusCode < 300) {
                        queueLogMessage("Back-end server accepted network request to send message. HTTP response status code is " + statusCode + ".");
                    } else {
                        queueLogMessage("Back-end server rejected network request to send message. HTTP response status code is " + statusCode + ".");
                    }

                    urlConnection.disconnect();

                } catch (IOException e) {
                    queueLogMessage("ERROR: got exception parsing network response from Back-end server: " + e.getLocalizedMessage());

                } finally {
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {}
                    }
                }
                return null;
            }
        };

        asyncTask.execute((Void)null);
    }

    private void writeConnectionOutput(String requestBodyData, OutputStream outputStream) throws IOException {
        final byte[] bytes = requestBodyData.getBytes();
        for (byte b : bytes) {
            outputStream.write(b);
        }
        outputStream.close();
    }

    private String getBackEndMessageRequestString() {
        final String device_uuid = readIdFromFile("device_uuid");
        if (device_uuid == null) {
            return null;
        }
        final String[] devices = new String[]{device_uuid};
        final String platforms = "android";
        final String messageTitle = "Sample Message Title";
        final String messageBody = "This message was sent to the back-end at " + getTimestamp() + "." ;
        final String environmentUuid = Settings.getBackEndEnvironmentUuid(this);
        final String environmentKey = Settings.getBackEndEnvironmentKey(this);
        final BackEndMessageRequest messageRequest = new BackEndMessageRequest(environmentUuid, environmentKey, messageTitle, messageBody, platforms, devices);
        final Gson gson = new Gson();
        return gson.toJson(messageRequest);
    }

    private void sendMessageViaGcm() {
        updateCurrentBaseRowColour();
        final String data = getGcmMessageRequestString();
        if (data == null) {
            addLogMessage("Can not send message. Please register first.");
            return;
        }
        addLogMessage("Sending message via GCM...");
        addLogMessage("Message body data: \"" + data + "\"");

        final AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                OutputStream outputStream = null;
                try {
                    final URL url = new URL(GCM_SEND_MESSAGE_URL);
                    final HttpURLConnection urlConnection = getUrlConnection(url);
                    urlConnection.addRequestProperty("Authorization", "key=" + Settings.getGcmBrowserApiKey(MainActivity.this));
                    urlConnection.setDoOutput(true);
                    urlConnection.connect();

                    outputStream = new BufferedOutputStream(urlConnection.getOutputStream());
                    writeConnectionOutput(data, outputStream);

                    final int statusCode = urlConnection.getResponseCode();
                    if (statusCode >= 200 && statusCode < 300) {
                        queueLogMessage("GCM server accepted network request to send message. HTTP response status code is " + statusCode + ".");
                    } else {
                        queueLogMessage("GCM server rejected network request to send message. HTTP response status code is " + statusCode + ".");
                    }

                    urlConnection.disconnect();

                } catch (Exception e) {
                    queueLogMessage("ERROR: got exception posting message to GCM server: " + e.getLocalizedMessage());
                }

                finally {
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {}
                    }
                }
                return null;
            }
        };
        asyncTask.execute((Void) null);
    }

    private HttpURLConnection getUrlConnection(URL url) throws IOException {
        final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setDoInput(true);
        urlConnection.setRequestMethod("POST");
        urlConnection.setConnectTimeout(60000);
        urlConnection.setReadTimeout(60000);
        urlConnection.addRequestProperty("Content-Type", "application/json");
        return urlConnection;
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

    private void clearMessageReceipts() {
        addLogMessage("Clearing all message receipts.");
        final DatabaseEventsStorage eventsStorage = new DatabaseEventsStorage();
        eventsStorage.reset(EventsStorage.EventType.MESSAGE_RECEIPT);
    }

    private void clearRegistration() {
        final ClearRegistrationDialogFragment.Listener listener = new ClearRegistrationDialogFragment.Listener() {

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
                        editor.remove("variant_uuid");
                        editor.remove("variant_secret");
                        editor.remove("device_alias");
                        editor.remove("backend_device_registration_id");
                    }
                    editor.commit();
                }
            }
        };
        final ClearRegistrationDialogFragment dialog = new ClearRegistrationDialogFragment();
        dialog.setListener(listener);
        dialog.show(getSupportFragmentManager(), "ClearRegistrationDialogFragment");
    }

    public AdapterView.OnItemLongClickListener getLogItemLongClickListener() {
        return new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, long id) {
                final int originalViewBackgroundColour = adapter.getBackgroundColour(position);
                final LogItem logItem = (LogItem) adapter.getItem(position);
                final LogItemLongClickDialogFragment.Listener listener = new LogItemLongClickDialogFragment.Listener() {

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
                };
                final LogItemLongClickDialogFragment dialog = new LogItemLongClickDialogFragment();
                dialog.setListener(listener);
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

    private void unregister() {
        updateCurrentBaseRowColour();
        addLogMessage("Starting unregistration...");

        try {
            pushLib.startUnregistration(new UnregistrationListener() {
                @Override
                public void onUnregistrationComplete() {
                    queueLogMessage("Unregistration successful.");
                }

                @Override
                public void onUnregistrationFailed(String reason) {
                    queueLogMessage("Unregistration failed. Reason is '" + reason + "'.");
                }
            });
        } catch (Exception e) {
            queueLogMessage("Unregistration failed: " + e.getLocalizedMessage());
        }
    }
}
