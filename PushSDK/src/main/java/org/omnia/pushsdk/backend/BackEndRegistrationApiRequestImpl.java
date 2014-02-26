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
import com.xtreme.network.NetworkRequest;
import com.xtreme.network.NetworkResponse;

import java.io.IOException;

/**
 * API request for registering a device with the Omnia Mobile Services back-end server.
 */
public class BackEndRegistrationApiRequestImpl implements BackEndRegistrationApiRequest {

    private NetworkWrapper networkWrapper;
    private Context context;

    public BackEndRegistrationApiRequestImpl(Context context, NetworkWrapper networkWrapper) {
        verifyArguments(context, networkWrapper);
        saveArguments(context, networkWrapper);
    }

    private void verifyArguments(Context context, NetworkWrapper networkWrapper) {
        if (networkWrapper == null) {
            throw new IllegalArgumentException("networkWrapper may not be null");
        }
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
    }

    private void saveArguments(Context context, NetworkWrapper networkWrapper) {
        this.networkWrapper = networkWrapper;
        this.context = context;
    }

    public void startDeviceRegistration(String gcmDeviceRegistrationId, RegistrationParameters parameters, BackEndRegistrationListener listener) {

        verifyRegistrationArguments(gcmDeviceRegistrationId, listener);

        final NetworkRequest request = getNetworkRequest(gcmDeviceRegistrationId, parameters);

        try {
            final NetworkResponse response = networkWrapper.getNetworkRequestLauncher().executeRequestSynchronously(request);
            onSuccessfulNetworkRequest(response, listener);
        } catch(Exception e) {
            PushLibLogger.ex("Back-end device registration attempt failed", e);
            listener.onBackEndRegistrationFailed(e.getLocalizedMessage());
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

    private NetworkRequest getNetworkRequest(String gcmDeviceRegistrationId, RegistrationParameters parameters) {
        final NetworkRequest networkRequest = new NetworkRequest(Const.BACKEND_REGISTRATION_REQUEST_URL);
        final String bodyData = getRequestBodyData(gcmDeviceRegistrationId, parameters);
        networkRequest.setRequestType(NetworkRequest.RequestType.POST);
        networkRequest.setBodyData(bodyData);
        networkRequest.addHeaderParam("Content-Type", "application/json");
        PushLibLogger.v("Making network request to register this device with the back-end server: " + bodyData);
        return networkRequest;
    }

    public void onSuccessfulNetworkRequest(NetworkResponse networkResponse, final BackEndRegistrationListener listener) {

        if (networkResponse == null) {
            PushLibLogger.e("Back-end server registration failed: no networkResponse");
            listener.onBackEndRegistrationFailed("No networkResponse from back-end server.");
            return;
        }

        if (networkResponse.getStatus() == null) {
            PushLibLogger.e("Back-end server registration failed: no statusLine in networkResponse");
            listener.onBackEndRegistrationFailed("Back-end no statusLine in networkResponse");
            return;
        }

        final int statusCode = networkResponse.getStatus().getStatusCode();
        if (isFailureStatusCode(statusCode)) {
            PushLibLogger.e("Back-end server registration failed: server returned HTTP status " + statusCode);
            listener.onBackEndRegistrationFailed("Back-end server returned HTTP status " + statusCode);
            return;
        }

        final Gson gson = new Gson();
        final String responseString;
        try {
            responseString = networkResponse.getResponseString();
        } catch (IOException e) {
            PushLibLogger.e("Back-end server registration failed: server response empty");
            listener.onBackEndRegistrationFailed("Back-end server response empty");
            return;
        }

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

    private boolean isFailureStatusCode(int statusCode) {
        return (statusCode < 200 || statusCode >= 300);
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