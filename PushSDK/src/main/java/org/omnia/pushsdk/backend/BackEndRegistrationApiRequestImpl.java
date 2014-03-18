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

package org.omnia.pushsdk.backend;

import android.content.Context;
import android.os.Build;

import com.google.gson.Gson;
import org.omnia.pushsdk.RegistrationParameters;
import org.omnia.pushsdk.model.BackEndApiRegistrationRequestData;
import org.omnia.pushsdk.model.BackEndApiRegistrationResponseData;
import org.omnia.pushsdk.network.NetworkWrapper;
import org.omnia.pushsdk.util.Const;
import org.omnia.pushsdk.util.PushLibLogger;
import org.omnia.pushsdk.util.Util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * API request for registering a device with the Omnia Mobile Services back-end server.
 */
public class BackEndRegistrationApiRequestImpl extends ApiRequestImpl implements BackEndRegistrationApiRequest {

    private Context context;

    public BackEndRegistrationApiRequestImpl(Context context, NetworkWrapper networkWrapper) {
        super(networkWrapper);
        verifyArguments(context);
        saveArguments(context);
    }

    private void verifyArguments(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
    }

    private void saveArguments(Context context) {
        this.context = context;
    }

    public void startDeviceRegistration(String gcmDeviceRegistrationId, RegistrationParameters parameters, BackEndRegistrationListener listener) {

        verifyRegistrationArguments(gcmDeviceRegistrationId, listener);

        OutputStream outputStream = null;
        try {
            final URL url = new URL(Const.BACKEND_REGISTRATION_REQUEST_URL);
            final HttpURLConnection urlConnection = getHttpURLConnection(url);
            urlConnection.setDoOutput(true); // indicate "POST" request
            urlConnection.setDoInput(true);
            urlConnection.addRequestProperty("Content-Type", "application/json");
            urlConnection.connect();

            outputStream = new BufferedOutputStream(urlConnection.getOutputStream());
            final String requestBodyData = getRequestBodyData(gcmDeviceRegistrationId, parameters);
            PushLibLogger.v("Making network request to register this device with the back-end server: " + requestBodyData);
            writeOutput(requestBodyData, outputStream);

            final int statusCode = urlConnection.getResponseCode();

            final InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
            final String responseString = readInput(inputStream);

            urlConnection.disconnect();

            onSuccessfulNetworkRequest(statusCode, responseString, listener);

        } catch (Exception e) {
            PushLibLogger.ex("Back-end device registration attempt failed", e);
            listener.onBackEndRegistrationFailed(e.getLocalizedMessage());
        }

        finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {}
            }
        }
    }

    private void verifyRegistrationArguments(String gcmDeviceRegistrationId, BackEndRegistrationListener listener) {
        if (gcmDeviceRegistrationId == null) {
            throw new IllegalArgumentException("gcmDeviceRegistrationId may not be null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("listener may not be null");
        }
    }

    public void onSuccessfulNetworkRequest(int statusCode, String responseString, final BackEndRegistrationListener listener) {

        if (isFailureStatusCode(statusCode)) {
            PushLibLogger.e("Back-end server registration failed: server returned HTTP status " + statusCode);
            listener.onBackEndRegistrationFailed("Back-end server returned HTTP status " + statusCode);
            return;
        }

        if (responseString == null) {
            PushLibLogger.e("Back-end server registration failed: server response empty");
            listener.onBackEndRegistrationFailed("Back-end server response empty");
            return;
        }

        final Gson gson = new Gson();
        final BackEndApiRegistrationResponseData responseData;
        try {
            responseData = gson.fromJson(responseString, BackEndApiRegistrationResponseData.class);
            if (responseData == null) {
                throw new Exception("unable to parse server response");
            }
        } catch (Exception e) {
            PushLibLogger.e("Back-end server registration failed: " + e.getLocalizedMessage());
            listener.onBackEndRegistrationFailed(e.getLocalizedMessage());
            return;
        }

        final String deviceUuid = responseData.getDeviceUuid();
        if (deviceUuid == null || deviceUuid.isEmpty()) {
            PushLibLogger.e("Back-end server registration failed: did not return device_uuid");
            listener.onBackEndRegistrationFailed("Back-end server did not return device_uuid");
            return;
        }

        Util.saveIdToFilesystem(context, deviceUuid, "device_uuid");

        PushLibLogger.i("Back-end Server registration succeeded.");
        listener.onBackEndRegistrationSuccess(deviceUuid);
    }

    private String getRequestBodyData(String deviceRegistrationId, RegistrationParameters parameters) {
        final BackEndApiRegistrationRequestData data = getBackEndApiRegistrationRequestData(deviceRegistrationId, parameters);
        final Gson gson = new Gson();
        return gson.toJson(data);
    }

    private BackEndApiRegistrationRequestData getBackEndApiRegistrationRequestData(String deviceRegistrationId, RegistrationParameters parameters) {
        final BackEndApiRegistrationRequestData data = new BackEndApiRegistrationRequestData();
        data.setReleaseUuid(parameters.getReleaseUuid());
        data.setSecret(parameters.getReleaseSecret());
        if (parameters.getDeviceAlias() == null) {
            data.setDeviceAlias("");
        } else {
            data.setDeviceAlias(parameters.getDeviceAlias());
        }
        data.setDeviceModel(Build.MODEL);
        data.setDeviceManufacturer(Build.MANUFACTURER);
        data.setOs("android");
        data.setOsVersion(Build.VERSION.RELEASE);
        data.setRegistrationToken(deviceRegistrationId);
        return data;
    }

    @Override
    public BackEndRegistrationApiRequest copy() {
        return new BackEndRegistrationApiRequestImpl(context, networkWrapper);
    }
}
