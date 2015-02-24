/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.backend.api;

import java.net.HttpURLConnection;
import java.net.URL;

import io.pivotal.android.push.PushParameters;
import io.pivotal.android.push.util.ApiRequestImpl;
import io.pivotal.android.push.util.Const;
import io.pivotal.android.push.util.Logger;
import io.pivotal.android.push.util.NetworkWrapper;

/**
 * API request for unregistering a device from the Pivotal CF Mobile Services Push server.
 */
public class PCFPushUnregisterDeviceApiRequestImpl extends ApiRequestImpl implements PCFPushUnregisterDeviceApiRequest {

    public PCFPushUnregisterDeviceApiRequestImpl(NetworkWrapper networkWrapper) {
        super(networkWrapper);
    }

    @Override
    public void startUnregisterDevice(String pcfPushDeviceRegistrationId, PushParameters parameters, PCFPushUnregisterDeviceListener listener) {

        verifyUnregistrationArguments(pcfPushDeviceRegistrationId, parameters, listener);

        try {
            Logger.v("Making network request to the PCF Push server to unregister the device ID:" + pcfPushDeviceRegistrationId);
            final URL url = new URL(parameters.getServiceUrl() + "/" + Const.PCF_PUSH_REGISTRATION_REQUEST_ENDPOINT + "/" + pcfPushDeviceRegistrationId);
            final HttpURLConnection urlConnection = getHttpURLConnection(url);
            urlConnection.setRequestMethod("DELETE");
            urlConnection.addRequestProperty("Authorization", ApiRequestImpl.getBasicAuthorizationValue(parameters));
            urlConnection.connect();

            final int statusCode = urlConnection.getResponseCode();

            urlConnection.disconnect();

            onSuccessfulRequest(statusCode, listener);

        } catch (Exception e) {
            Logger.ex("PCF Push device unregistration attempt failed", e);
            listener.onPCFPushUnregisterDeviceFailed(e.getLocalizedMessage());
        }
    }

    private void verifyUnregistrationArguments(String pcfPushDeviceRegistrationId, PushParameters parameters, PCFPushUnregisterDeviceListener listener) {
        if (pcfPushDeviceRegistrationId == null) {
            throw new IllegalArgumentException("pcfPushDeviceRegistrationId may not be null");
        }
        if (parameters == null) {
            throw new IllegalArgumentException("parameters may not be null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("listener may not be null");
        }
    }

    public void onSuccessfulRequest(int statusCode, PCFPushUnregisterDeviceListener listener) {

        // HTTP status 404 is not considered a failure.  If the server doesn't know about
        // this device registration ID then the device can already been considered unregistered.
        if (statusCode != 404 && isFailureStatusCode(statusCode)) {
            Logger.e("PCF Push server unregistration failed: server returned HTTP status " + statusCode);
            listener.onPCFPushUnregisterDeviceFailed("PCF Push server returned HTTP status " + statusCode);
            return;
        }

        Logger.i("PCF Push Server device unregistration succeeded.");
        listener.onPCFPushUnregisterDeviceSuccess();
    }

    @Override
    public PCFPushUnregisterDeviceApiRequest copy() {
        return new PCFPushUnregisterDeviceApiRequestImpl(networkWrapper);
    }
}
