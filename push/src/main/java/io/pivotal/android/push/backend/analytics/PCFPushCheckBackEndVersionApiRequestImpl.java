package io.pivotal.android.push.backend.analytics;

import android.content.Context;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import io.pivotal.android.push.PushParameters;
import io.pivotal.android.push.model.version.VersionResult;
import io.pivotal.android.push.prefs.PushPreferencesProvider;
import io.pivotal.android.push.prefs.PushRequestHeaders;
import io.pivotal.android.push.util.ApiRequestImpl;
import io.pivotal.android.push.util.Const;
import io.pivotal.android.push.util.Logger;
import io.pivotal.android.push.util.NetworkWrapper;
import io.pivotal.android.push.version.Version;

public class PCFPushCheckBackEndVersionApiRequestImpl extends ApiRequestImpl implements PCFPushCheckBackEndVersionApiRequest {

    private final PushPreferencesProvider preferencesProvider;
    private final PushRequestHeaders pushRequestHeaders;

    public PCFPushCheckBackEndVersionApiRequestImpl(Context context,
                                                    PushPreferencesProvider preferencesProvider,
                                                    PushRequestHeaders pushRequestHeaders,
                                                    NetworkWrapper networkWrapper) {
        super(context, networkWrapper);
        if (preferencesProvider == null) {
            throw new IllegalArgumentException("preferencesProvider may not be null");
        }
        this.preferencesProvider = preferencesProvider;
        this.pushRequestHeaders = pushRequestHeaders;
    }

    @Override
    public void startCheckBackEndVersion(PCFPushCheckBackEndVersionListener listener) {
        verifyRequestArguments(listener);
        processRequest(listener);
    }

    private void verifyRequestArguments(PCFPushCheckBackEndVersionListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener may not be null");
        }
    }

    private void processRequest(PCFPushCheckBackEndVersionListener listener) {

        int statusCode = 0;

        try {

            final PushParameters parameters = new PushParameters(context, preferencesProvider, pushRequestHeaders, null, null);
            final URL url = getUrl(parameters);
            final HttpURLConnection urlConnection = getHttpURLConnection(url, parameters);

            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);
            urlConnection.connect();

            Logger.v("Making network request to get the back-end server version...");

            statusCode = urlConnection.getResponseCode();

            final InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
            final String responseString = readInput(inputStream);

            urlConnection.disconnect();

            onSuccessfulNetworkRequest(statusCode, listener, responseString);

        } catch (Exception e) {

            handleError(statusCode, e, listener);
        }
    }

    private void handleError(int statusCode, Exception e, PCFPushCheckBackEndVersionListener listener) {
        if (statusCode == 404) {
            Logger.e("Back-end server does not support version API (1). It must be <= version 1.3.1.");
            listener.onCheckBackEndVersionIsOldVersion();

        } else if (isFatalStatusCode(statusCode)) {
            Logger.e("Checking back-end server version failed: server returned fatal HTTP status " + statusCode);
            listener.onCheckBackEndVersionFatalFailure("Getting back-end server version returned HTTP status " + statusCode);

        } else if (isFailureStatusCode(statusCode)) {
            Logger.e("Checking back-end server version failed: server returned retryable HTTP status " + statusCode);
            listener.onCheckBackEndVersionRetryableFailure("Getting back-end server version returned HTTP status " + statusCode);

        } else if (e != null) {
            Logger.ex("Checking back-end server version failed.", e);
            listener.onCheckBackEndVersionRetryableFailure("Getting back-end server version failed" + e);

        } else {
            Logger.e("An unknown error occured while checking the back-end server version.");
            listener.onCheckBackEndVersionRetryableFailure("An unknown error occured while checking the back-end server version.");
        }
    }

    private URL getUrl(PushParameters parameters) throws MalformedURLException {

        try {
            return new URL(parameters.getServiceUrl() + "/" + Const.PCF_PUSH_VERSION_REQUEST_ENDPOINT);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private void onSuccessfulNetworkRequest(int statusCode, PCFPushCheckBackEndVersionListener listener, String responseString) {

        if (isFailureStatusCode(statusCode)) {
            handleError(statusCode, null, listener);
            return;
        }

        if (responseString == null) {
            Logger.e("Checking back-end server version failed: server response empty");
            listener.onCheckBackEndVersionRetryableFailure("Checking back-end server version failed: server response empty");
            return;
        }

        final Gson gson = new Gson();
        final VersionResult responseData;
        try {
            responseData = gson.fromJson(responseString, VersionResult.class);
            if (responseData == null) {
                throw new Exception("unable to parse server response");
            }
        } catch (Exception e) {
            Logger.e("Checking back-end server version failed: " + e.getLocalizedMessage());
            listener.onCheckBackEndVersionRetryableFailure(e.getLocalizedMessage());
            return;
        }

        final Version version = responseData.getVersion();
        Logger.i("Getting back-end server version succeeded. PCF Push server version is " + version);
        listener.onCheckBackEndVersionSuccess(version);
    }

    @Override
    public PCFPushCheckBackEndVersionApiRequest copy() {
        return new PCFPushCheckBackEndVersionApiRequestImpl(context, preferencesProvider, pushRequestHeaders, networkWrapper);
    }
}
