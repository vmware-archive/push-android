package io.pivotal.android.push.backend.analytics;

import android.content.Context;
import android.net.Uri;

import com.google.gson.Gson;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import io.pivotal.android.push.PushParameters;
import io.pivotal.android.push.database.AnalyticsEventsStorage;
import io.pivotal.android.push.model.analytics.AnalyticsEvent;
import io.pivotal.android.push.model.analytics.AnalyticsEventList;
import io.pivotal.android.push.prefs.PushRequestHeaders;
import io.pivotal.android.push.util.ApiRequestImpl;
import io.pivotal.android.push.util.Const;
import io.pivotal.android.push.util.Logger;
import io.pivotal.android.push.util.NetworkWrapper;

public class PCFPushSendAnalyticsApiRequestImpl extends ApiRequestImpl implements PCFPushSendAnalyticsApiRequest {

    private Context context;
    private AnalyticsEventsStorage eventsStorage;
    private PushRequestHeaders pushRequestHeaders;
    private PushParameters parameters;

    public PCFPushSendAnalyticsApiRequestImpl(
            Context context,
            PushParameters parameters,
            AnalyticsEventsStorage eventsStorage,
            PushRequestHeaders pushRequestHeaders,
            NetworkWrapper networkWrapper
    ) {
        super(context, networkWrapper);
        verifyArguments(context, eventsStorage, parameters, pushRequestHeaders);
        saveArguments(context, eventsStorage, parameters, pushRequestHeaders);
    }

    private void verifyArguments(Context context, AnalyticsEventsStorage eventsStorage, PushParameters parameters, PushRequestHeaders pushRequestHeaders) {
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
        if (eventsStorage == null) {
            throw new IllegalArgumentException("eventsStorage may not be null");
        }
        if (parameters == null) {
            throw new IllegalArgumentException("parameters may not be null");
        }
        if (pushRequestHeaders == null) {
            throw new IllegalArgumentException("pushRequestHeaders may not be null");
        }

    }

    private void saveArguments(Context context, AnalyticsEventsStorage eventsStorage, PushParameters parameters, PushRequestHeaders pushRequestHeaders) {
        this.context = context;
        this.eventsStorage = eventsStorage;
        this.parameters = parameters;
        this.pushRequestHeaders = pushRequestHeaders;
    }

    @Override
    public void startSendEvents(List<Uri> eventUris, PCFPushSendAnalyticsListener listener) {
        verifyRequestArguments(eventUris, listener);
        processRequest(eventUris, listener);
    }

    private void verifyRequestArguments(List<Uri> uris, PCFPushSendAnalyticsListener listener) {
        if (uris == null || uris.isEmpty()) {
            throw new IllegalArgumentException("uris may not be null or empty");
        }
        if (listener == null) {
            throw new IllegalArgumentException("listener may not be null");
        }
    }

    private void processRequest(List<Uri> uris, PCFPushSendAnalyticsListener listener) {
        OutputStream outputStream = null;

        try {

            final URL url = getUrl(parameters);
            final HttpURLConnection urlConnection = getHttpURLConnection(url, parameters);

            urlConnection.addRequestProperty("Content-Type", "application/json");
            urlConnection.addRequestProperty("Authorization", getBasicAuthorizationValue(parameters));
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.connect();

            outputStream = new BufferedOutputStream(urlConnection.getOutputStream());

            final String requestBodyData = getRequestBodyData(uris);
            Logger.v("Making network request to post event data to the back-end server: " + requestBodyData);
            writeOutput(requestBodyData, outputStream);

            final int statusCode = urlConnection.getResponseCode();
            urlConnection.disconnect();

            onSuccessfulNetworkRequest(statusCode, listener);

        } catch (Exception e) {
            Logger.ex("Sending event data to back-end server failed", e);
            listener.onBackEndSendEventsFailed(e.getLocalizedMessage());

        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {}
            }
        }
    }

    private URL getUrl(PushParameters parameters) throws MalformedURLException {

        try {
            return new URL(parameters.getServiceUrl() + "/" + Const.PCF_PUSH_ANALYTICS_REQUEST_ENDPOINT);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private String getRequestBodyData(List<Uri> uris) {
        final List<AnalyticsEvent> events = getEvents(uris);
        final AnalyticsEventList eventList = new AnalyticsEventList();
        eventList.setEvents(events);
        final Gson gson = new Gson();
        final String requestBodyData = gson.toJson(eventList);
        return requestBodyData;
    }

    private List<AnalyticsEvent> getEvents(List<Uri> uris) {
        final List<AnalyticsEvent> events = new LinkedList<>();
        for (final Uri uri : uris) {
            final AnalyticsEvent event = eventsStorage.readEvent(uri);
            events.add(event);
        }
        return events;
    }

    private void onSuccessfulNetworkRequest(int statusCode, PCFPushSendAnalyticsListener listener) {

        if (isFailureStatusCode(statusCode)) {
            Logger.e("Sending event data to back-end server failed: server returned HTTP status " + statusCode);
            listener.onBackEndSendEventsFailed("Sending event data to back-end server returned HTTP status " + statusCode);
            return;
        }

        Logger.i("Sending event data to back-end server succeeded.");
        listener.onBackEndSendEventsSuccess();
    }

    @Override
    public PCFPushSendAnalyticsApiRequest copy() {
        return new PCFPushSendAnalyticsApiRequestImpl(context, parameters, eventsStorage, pushRequestHeaders, networkWrapper);
    }
}
