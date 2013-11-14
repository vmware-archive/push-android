package com.gopivotal.pushlib.api;

import android.os.Build;

import com.google.gson.Gson;
import com.gopivotal.pushlib.model.BackEndApiRegistrationRequestData;
import com.gopivotal.pushlib.model.BackEndApiRegistrationResponseData;
import com.gopivotal.pushlib.network.NetworkWrapper;
import com.xtreme.commons.Logger;
import com.xtreme.network.NetworkError;
import com.xtreme.network.NetworkRequest;
import com.xtreme.network.NetworkRequestListener;
import com.xtreme.network.NetworkResponse;

import java.io.IOException;

public class BackEndRegistrationApiRequestImpl implements BackEndRegistrationApiRequest {

    private static final String REQUEST_URL = "http://ec2-54-234-124-123.compute-1.amazonaws.com:8080/v1/registration";

    private NetworkWrapper networkWrapper;

    public BackEndRegistrationApiRequestImpl(NetworkWrapper networkWrapper) {
        verifyArguments(networkWrapper);
        saveArguments(networkWrapper);
    }

    private void verifyArguments(NetworkWrapper networkWrapper) {
        if (networkWrapper == null) {
            throw new IllegalArgumentException("apiProvider may not be null");
        }
    }

    private void saveArguments(NetworkWrapper networkWrapper) {
        this.networkWrapper = networkWrapper;
    }

    public void startDeviceRegistration(String gcmDeviceRegistrationId, BackEndRegistrationListener listener) {
        final NetworkRequest request = getNetworkRequest(gcmDeviceRegistrationId, listener);
        networkWrapper.getNetworkRequestLauncher().executeRequest(request);
    }

    private NetworkRequest getNetworkRequest(String gcmDeviceRegistrationId, final BackEndRegistrationListener listener) {
        if (gcmDeviceRegistrationId == null) {
            throw new IllegalArgumentException("gcmDeviceRegistrationId may not be null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("listener may not be null");
        }

        Logger.i("Making network request to register this device with the back-end server");
        final NetworkRequest networkRequest = new NetworkRequest(REQUEST_URL, getNetworkRequestListener(listener));
        networkRequest.setRequestType(NetworkRequest.RequestType.POST);
        networkRequest.setBodyData(getRequestBodyData(gcmDeviceRegistrationId));
        return networkRequest;
    }

    private NetworkRequestListener getNetworkRequestListener(final BackEndRegistrationListener listener) {
        return new NetworkRequestListener() {

            @Override
            public void onSuccess(NetworkResponse networkResponse) {
                Logger.i("Back-end server registration successful");

                if (networkResponse == null) {
                    Logger.e("Back-end server registration failed: no networkResponse");
                    listener.onBackEndRegistrationFailed("No networkResponse from back-end server.");
                    return;
                }

                if (networkResponse.getStatus() == null) {
                    Logger.e("Back-end server registration failed: no statusLine in networkResponse");
                    listener.onBackEndRegistrationFailed("Back-end no statusLine in networkResponse");
                    return;
                }

                final int statusCode = networkResponse.getStatus().getStatusCode();
                if (isFailureStatusCode(statusCode)) {
                    Logger.e("Back-end server registration failed: server returned HTTP status " + statusCode);
                    listener.onBackEndRegistrationFailed("Back-end server returned HTTP status " + statusCode);
                    return;
                }

                final Gson gson = new Gson();
                final String responseString;
                try {
                    responseString = networkResponse.getResponseString();
                } catch (IOException e) {
                    Logger.e("Back-end server registration failed: server response empty");
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
                    Logger.e("Back-end server registration failed: " + e.getLocalizedMessage());
                    listener.onBackEndRegistrationFailed(e.getLocalizedMessage());
                    return;
                }

                final String deviceUuid = responseData.getDeviceUuid();
                if (deviceUuid == null || deviceUuid.isEmpty()) {
                    Logger.e("Back-end server registration failed: did not return device_uuid");
                    listener.onBackEndRegistrationFailed("Back-end server did not return device_uuid");
                    return;
                }

                // TODO - pass back device_uuid from network response
                Logger.i("Back-end Server registration succeeded.");
                listener.onBackEndRegistrationSuccess(deviceUuid);
            }

            @Override
            public void onFailure(NetworkError networkError) {
                String reason = "Unknown failure reason";
                if (networkError != null) {
                    if (networkError.getException() != null) {
                        reason = networkError.getException().getMessage();
                    } else if (networkError.getHttpStatus() != null) {
                        reason = "Back-end server returned HTTP status upon registration attempt " + networkError.getHttpStatus().getStatusCode();
                    }
                }
                listener.onBackEndRegistrationFailed(reason);
            }
        };
    }

    private boolean isFailureStatusCode(int statusCode) {
        return (statusCode < 200 || statusCode >= 300);
    }

    private String getRequestBodyData(String deviceRegistrationId) {
        // TODO - most of this data is bogus. I need to figure out what it's really supposed to look like.
        final BackEndApiRegistrationRequestData data = new BackEndApiRegistrationRequestData();
        data.setReplicantUuid("9e60c311-f5c7-4416-aea2-d07bbc94f208");
        data.setSecret("3c676b20-3c49-4215-be1a-3932e3458514");
        data.setDeviceAlias("androidtest");
        data.setDeviceModel("someModel");
        data.setDeviceType("phone");
        data.setOs("android");
        data.setOsVersion(Build.VERSION.RELEASE);
        data.setRegistrationToken(deviceRegistrationId);
        final Gson gson = new Gson();
        return gson.toJson(data);
    }
}