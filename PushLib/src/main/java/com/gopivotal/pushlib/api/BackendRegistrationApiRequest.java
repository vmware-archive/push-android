package com.gopivotal.pushlib.api;

import android.os.Build;

import com.google.gson.Gson;
import com.gopivotal.pushlib.model.ApiRegistrationRequestData;
import com.gopivotal.pushlib.network.NetworkWrapper;
import com.xtreme.commons.Logger;
import com.xtreme.network.NetworkError;
import com.xtreme.network.NetworkRequest;
import com.xtreme.network.NetworkRequestListener;
import com.xtreme.network.NetworkResponse;

public class BackEndRegistrationApiRequest {

    private static final String REQUEST_URL = "http://ec2-54-234-124-123.compute-1.amazonaws.com:8080/v1/registration";

    private NetworkWrapper networkWrapper;

    public BackEndRegistrationApiRequest(NetworkWrapper networkWrapper) {
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
                    listener.onBackEndRegistrationFailed("no networkResponse");
                    return;
                }

                if (networkResponse.getStatus() == null) {
                    Logger.e("Back-end server registration failed: no statusLine in networkResponse");
                    listener.onBackEndRegistrationFailed("no statusLine in networkResponse");
                    return;
                }

                final int statusCode = networkResponse.getStatus().getStatusCode();
                if (isFailureStatusCode(statusCode)) {
                    Logger.e("Back-end server registration failed: server returned HTTP status " + statusCode);
                    listener.onBackEndRegistrationFailed("server returned HTTP status " + statusCode);
                    return;
                }

                Logger.i("Back-end Server registration succeeded.");

                // TODO - pass back device_uuid from network response
                listener.onBackEndRegistrationSuccess("CATS");
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
        final ApiRegistrationRequestData data = new ApiRegistrationRequestData();
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
