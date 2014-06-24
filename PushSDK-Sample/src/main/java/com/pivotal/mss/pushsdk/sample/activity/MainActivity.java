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

package com.pivotal.mss.pushsdk.sample.activity;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.gson.Gson;
import com.pivotal.mss.analyticssdk.AnalyticsParameters;
import com.pivotal.mss.analyticssdk.database.DatabaseEventsStorage;
import com.pivotal.mss.common.sample.activity.BaseMainActivity;
import com.pivotal.mss.common.sample.activity.BasePreferencesActivity;
import com.pivotal.mss.common.util.DebugUtil;
import com.pivotal.mss.pushsdk.PushSDK;
import com.pivotal.mss.pushsdk.RegistrationParameters;
import com.pivotal.mss.pushsdk.prefs.PushPreferencesProviderImpl;
import com.pivotal.mss.pushsdk.registration.RegistrationListener;
import com.pivotal.mss.pushsdk.registration.UnregistrationListener;
import com.pivotal.mss.pushsdk.sample.R;
import com.pivotal.mss.pushsdk.sample.broadcastreceiver.MyPivotalMSSRemotePushLibBroadcastReceiver;
import com.pivotal.mss.pushsdk.sample.dialogfragment.ClearRegistrationDialogFragment;
import com.pivotal.mss.pushsdk.sample.dialogfragment.SendMessageDialogFragment;
import com.pivotal.mss.pushsdk.sample.model.BackEndMessageRequest;
import com.pivotal.mss.pushsdk.sample.model.GcmMessageRequest;
import com.pivotal.mss.pushsdk.sample.util.Preferences;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends BaseMainActivity {

    private static final String GCM_SEND_MESSAGE_URL = "https://android.googleapis.com/gcm/send";
    private static final String BACK_END_SEND_MESSAGE_URL = "v1/push";

    private PushSDK pushSDK;

    protected Class<? extends BasePreferencesActivity> getPreferencesActivity() {
        return PreferencesActivity.class;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (logItems.isEmpty()) {
            addLogMessage("Press the \"Register\" button to attempt registration.");
        }
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCurrentBaseRowColour();
        setupPushSDK();
        setupAnalyticsSDK();
        clearNotifications();
    }

    private void setupPushSDK() {
        try {
            pushSDK = PushSDK.getInstance(this);
        } catch (IllegalArgumentException e) {
            addLogMessage("Not able to initialize Push SDK: " + e.getMessage());
        }
    }

    private void setupAnalyticsSDK() {
        try {
            final AnalyticsParameters analyticsParameters = getAnalyticsParameters();
            pushSDK.setupAnalytics(analyticsParameters);
        } catch (IllegalArgumentException e) {
            addLogMessage("Not able to initialize Analytics SDK: " + e.getMessage());
        }
    }

    private AnalyticsParameters getAnalyticsParameters() {
        final URL baseServerUrl = getAnalyticsBaseServerUrl();
        final boolean isAnalyticsEnabled = Preferences.isAnalyticsEnabled(this);
        final AnalyticsParameters parameters = new AnalyticsParameters(isAnalyticsEnabled, baseServerUrl);
        return parameters;
    }

    private void clearNotifications() {
        final NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(MyPivotalMSSRemotePushLibBroadcastReceiver.NOTIFICATION_ID);
    }

    private void startRegistration() {
        updateCurrentBaseRowColour();
        addLogMessage("Starting registration...");

        final RegistrationParameters parameters = getRegistrationParameters();
        if (parameters == null) {
            return;
        }

        try {
            pushSDK.startRegistration(parameters, new RegistrationListener() {

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

    private RegistrationParameters getRegistrationParameters() {
        final String gcmSenderId = Preferences.getGcmSenderId(this);
        final String variantUuid = Preferences.getVariantUuid(this);
        final String variantSecret = Preferences.getVariantSecret(this);
        final String deviceAlias = Preferences.getDeviceAlias(this);
        final URL baseServerUrl = getPushBaseServerUrl();
        addLogMessage("GCM Sender ID: '" + gcmSenderId + "'\nVariant UUID: '" + variantUuid + "\nVariant Secret: '" + variantSecret + "'\nDevice Alias: '" + deviceAlias + "'\nBase Server URL: '" + baseServerUrl + "'.");
        final RegistrationParameters parameters = new RegistrationParameters(gcmSenderId, variantUuid, variantSecret, deviceAlias, baseServerUrl);
        return parameters;
    }

    private URL getPushBaseServerUrl() {
        final String baseServerUrl = Preferences.getPushBaseServerUrl(this);
        try {
            return new URL(baseServerUrl);
        } catch (MalformedURLException e) {
            addLogMessage("Invalid push base server URL: '" + baseServerUrl + "'.");
            return null;
        }
    }

    private URL getAnalyticsBaseServerUrl() {
        final String baseServerUrl = Preferences.getAnalyticsBaseServerUrl(this);
        try {
            return new URL(baseServerUrl);
        } catch (MalformedURLException e) {
            addLogMessage("Invalid analytics base server URL: '" + baseServerUrl + "'.");
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
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

            case R.id.action_clear_events:
                clearEvents();
                break;

            case R.id.action_clear_registration:
                clearRegistration();
                break;

            case R.id.action_send_message:
                sendMessage();
                break;

            case R.id.action_unregister:
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
                    final URL url = new URL(Preferences.getPushBaseServerUrl(MainActivity.this) + "/" + BACK_END_SEND_MESSAGE_URL);
                    final HttpURLConnection urlConnection = getUrlConnection(url);
                    urlConnection.setDoOutput(true);
                    urlConnection.addRequestProperty("Authorization", getBasicAuthorizationValue());
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

    private String getBasicAuthorizationValue() {
        final String environmentUuid = Preferences.getBackEndEnvironmentUuid(this);
        final String environmentKey = Preferences.getBackEndEnvironmentKey(this);
        final String stringToEncode = environmentUuid + ":" + environmentKey;
        return "Basic  " + Base64.encodeToString(stringToEncode.getBytes(), Base64.DEFAULT | Base64.NO_WRAP);
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
        final BackEndMessageRequest messageRequest = new BackEndMessageRequest(messageTitle, messageBody, platforms, devices);
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
                    urlConnection.addRequestProperty("Authorization", "key=" + Preferences.getGcmBrowserApiKey(MainActivity.this));
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

    private void clearEvents() {
        if (Preferences.isAnalyticsEnabled(this)) {
            addLogMessage("Clearing all events.");
            final DatabaseEventsStorage eventsStorage = new DatabaseEventsStorage();
            eventsStorage.reset();
        } else {
            addLogMessage("Cannot clear events if analytics are disabled.");
        }
    }

    private void clearRegistration() {
        final ClearRegistrationDialogFragment.Listener listener = new ClearRegistrationDialogFragment.Listener() {

            @Override
            public void onClickResult(int result) {
                if (result != ClearRegistrationDialogFragment.CLEAR_REGISTRATIONS_CANCELLED) {
                    final SharedPreferences.Editor editor = getSharedPreferences(PushPreferencesProviderImpl.TAG_NAME, Context.MODE_PRIVATE).edit();
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
                        editor.remove("base_server_url");
                    }
                    editor.commit();
                }
            }
        };
        final ClearRegistrationDialogFragment dialog = new ClearRegistrationDialogFragment();
        dialog.setListener(listener);
        dialog.show(getSupportFragmentManager(), "ClearRegistrationDialogFragment");
    }

    private void unregister() {
        updateCurrentBaseRowColour();
        addLogMessage("Starting unregistration...");

        final RegistrationParameters parameters = getRegistrationParameters();
        if (parameters == null) {
            return;
        }

        try {
            pushSDK.startUnregistration(parameters, new UnregistrationListener() {
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
