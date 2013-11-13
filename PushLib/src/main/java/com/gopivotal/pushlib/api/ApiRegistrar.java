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

public class ApiRegistrar {

    private static final String REQUEST_URL = "http://ec2-54-234-124-123.compute-1.amazonaws.com:8080/v1/registration";

    private NetworkWrapper networkWrapper;

    public ApiRegistrar(NetworkWrapper networkWrapper) {
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

    public void startDeviceRegistration(String deviceRegistrationId, ApiRegistrarListener listener) {
        final NetworkRequest request = getNetworkRequest(deviceRegistrationId, listener);
        networkWrapper.getNetworkRequestLauncher().executeRequest(request);
    }

    private NetworkRequest getNetworkRequest(String deviceRegistrationId, final ApiRegistrarListener listener) {
        if (deviceRegistrationId == null) {
            throw new IllegalArgumentException("deviceRegistrationId may not be null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("listener may not be null");
        }

        Logger.i("Making network request to register this device with the Pivotal Push server");
        final NetworkRequest networkRequest = new NetworkRequest(REQUEST_URL, getNetworkRequestListener(listener));
        networkRequest.setRequestType(NetworkRequest.RequestType.POST);
        networkRequest.setBodyData(getRequestBodyData(deviceRegistrationId));
        return networkRequest;
    }

    private NetworkRequestListener getNetworkRequestListener(final ApiRegistrarListener listener) {
        return new NetworkRequestListener() {

            @Override
            public void onSuccess(NetworkResponse networkResponse) {
                Logger.i("Pivotal Push server registration successful");

                if (networkResponse == null) {
                    Logger.e("Pivotal Push server registration failed: no networkResponse");
                    listener.onRegistrationFailed("no networkResponse");
                    return;
                }

                if (networkResponse.getStatus() == null) {
                    Logger.e("Pivotal Push server registration failed: no statusLine in networkResponse");
                    listener.onRegistrationFailed("no statusLine in networkResponse");
                    return;
                }

                final int statusCode = networkResponse.getStatus().getStatusCode();
                if (isFailureStatusCode(statusCode)) {
                    Logger.e("Pivotal Push server registration failed: server returned HTTP status " + statusCode);
                    listener.onRegistrationFailed("server returned HTTP status " + statusCode);
                    return;
                }

                Logger.i("Pivtoal Push Server registration succeeded.");

                listener.onRegistrationSuccess();
            }

            @Override
            public void onFailure(NetworkError networkError) {
                String reason = "Unknown failure reason";
                if (networkError != null) {
                    if (networkError.getException() != null) {
                        reason = networkError.getException().getMessage();
                    } else if (networkError.getHttpStatus() != null) {
                        reason = "Server returned HTTP status " + networkError.getHttpStatus().getStatusCode();
                    }
                }
                listener.onRegistrationFailed(reason);
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
