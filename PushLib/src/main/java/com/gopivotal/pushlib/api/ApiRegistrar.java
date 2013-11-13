package com.gopivotal.pushlib.api;

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
        final NetworkRequest networkRequest = new NetworkRequest(REQUEST_URL, new NetworkRequestListener() {

            @Override
            public void onSuccess(NetworkResponse networkResponse) {
                Logger.i("Pivotal Push server registration successful");

                if (networkResponse == null) {
                    listener.onRegistrationFailed("no networkResponse");
                    return;
                }

                if (networkResponse.getStatus() == null) {
                    listener.onRegistrationFailed("no statusLine in networkResponse");
                    return;
                }

                final int statusCode = networkResponse.getStatus().getStatusCode();
                if (isFailureStatusCode(statusCode)) {
                    listener.onRegistrationFailed("server returned HTTP status "+statusCode);
                    return;
                }

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
        });
        return networkRequest;
    }

    private boolean isFailureStatusCode(int statusCode) {
        return (statusCode < 200 || statusCode >= 300);
    }
}
