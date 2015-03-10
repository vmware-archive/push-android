/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.backend.geofence;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import io.pivotal.android.push.PushParameters;
import io.pivotal.android.push.model.geofence.PCFPushGeofenceResponseData;
import io.pivotal.android.push.util.ApiRequestImpl;
import io.pivotal.android.push.util.Const;
import io.pivotal.android.push.util.GsonUtil;
import io.pivotal.android.push.util.Logger;
import io.pivotal.android.push.util.NetworkWrapper;

public class PCFPushGetGeofenceUpdatesApiRequest extends ApiRequestImpl {

    public PCFPushGetGeofenceUpdatesApiRequest(NetworkWrapper networkWrapper) {
        super(networkWrapper);
    }

    public void getGeofenceUpdates(long timestamp,
                                   PushParameters parameters,
                                   PCFPushGetGeofenceUpdatesListener listener) {

        verifyArguments(timestamp, parameters, listener);
        handleRequest(timestamp, parameters, listener);
    }

    private void verifyArguments(long timestamp, PushParameters parameters, PCFPushGetGeofenceUpdatesListener listener) {
        if (timestamp < 0) {
            throw new IllegalArgumentException("timestamp must be non-negative");
        }
        if (parameters == null) {
            throw new IllegalArgumentException("parameters may not be null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("listener may not be null");
        }
    }

    private void handleRequest(long timestamp, PushParameters parameters, PCFPushGetGeofenceUpdatesListener listener) {
        OutputStream outputStream = null;
        try {
            final URL url = getURL(timestamp, parameters);
            final HttpURLConnection urlConnection = getHttpURLConnection(url);
            urlConnection.setDoInput(true);
            urlConnection.setRequestMethod("GET");
            urlConnection.addRequestProperty("Authorization", getBasicAuthorizationValue(parameters));
            urlConnection.connect();

            Logger.v("Making network request to get updated geofences with timestamps > " + timestamp);

            final int statusCode = urlConnection.getResponseCode();

            final InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
            final String responseString = readInput(inputStream);

            urlConnection.disconnect();

            onSuccessfulNetworkRequest(statusCode, responseString, listener);

        } catch (Exception e) {
            Logger.ex("PCF Push get geofence updates request failed", e);
            listener.onPCFPushGetGeofenceUpdatesFailed(e.getLocalizedMessage());
        }

        finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {}
            }
        }
    }

    public void onSuccessfulNetworkRequest(int statusCode,
                                           String responseString,
                                           final PCFPushGetGeofenceUpdatesListener listener) {

        if (isFailureStatusCode(statusCode)) {
            Logger.e("PCF Push get geofence updates failed: server returned HTTP status " + statusCode);
            listener.onPCFPushGetGeofenceUpdatesFailed("PCF Push server returned HTTP status " + statusCode);
            return;
        }

        if (responseString == null) {
            Logger.e("PCF Push get geofence updates failed: server response empty");
            listener.onPCFPushGetGeofenceUpdatesFailed("PCF Push server response empty");
            return;
        }

        final Gson gson = GsonUtil.getGson();
        final PCFPushGeofenceResponseData responseData;
        try {
            responseData = gson.fromJson(responseString, PCFPushGeofenceResponseData.class);
            if (responseData == null) {
                throw new Exception("unable to parse server response");
            }
        } catch (Exception e) {
            Logger.e("PCF Push get geofence updates failed: " + e.getLocalizedMessage());
            listener.onPCFPushGetGeofenceUpdatesFailed(e.getLocalizedMessage());
            return;
        }

        Logger.i("PCF Push get geofence updates succeeded.");
        listener.onPCFPushGetGeofenceUpdatesSuccess(responseData);
    }

    private URL getURL(long timestamp,
                       PushParameters parameters) throws MalformedURLException {

        return new URL(parameters.getServiceUrl() + "/" + Const.PCF_PUSH_GEOFENCE_UPDATE_REQUEST_ENDPOINT  + "?" + Const.PCF_PUSH_GEOFENCE_UPDATE_REQUEST_TIMESTAMP + "=" + timestamp);
    }
}
