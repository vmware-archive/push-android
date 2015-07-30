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
import io.pivotal.android.push.database.EventsStorage;
import io.pivotal.android.push.model.analytics.Event;
import io.pivotal.android.push.model.analytics.EventList;
import io.pivotal.android.push.util.ApiRequestImpl;
import io.pivotal.android.push.util.Const;
import io.pivotal.android.push.util.Logger;
import io.pivotal.android.push.util.NetworkWrapper;

public class PCFPushSendAnalyticsApiRequestImpl extends ApiRequestImpl implements PCFPushSendAnalyticsApiRequest {

    // Set to 'true' to test really send events to the server. The server does not accept these events right now.
    private static final boolean POST_TO_BACK_END = false;
    private Context context;
    private EventsStorage eventsStorage;
    private PushParameters parameters;

    public PCFPushSendAnalyticsApiRequestImpl(Context context, EventsStorage eventsStorage, PushParameters parameters, NetworkWrapper networkWrapper) {
        super(context, networkWrapper);
        verifyArguments(context, eventsStorage, parameters);
        saveArguments(context, eventsStorage, parameters);
    }

    private void verifyArguments(Context context, EventsStorage eventsStorage, PushParameters parameters) {
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
        if (eventsStorage == null) {
            throw new IllegalArgumentException("eventsStorage may not be null");
        }
        if (parameters == null) {
            throw new IllegalArgumentException("parameters may not be null");
        }
    }

    private void saveArguments(Context context, EventsStorage eventsStorage, PushParameters parameters) {
        this.context = context;
        this.eventsStorage = eventsStorage;
        this.parameters = parameters;
    }

    @Override
    public void startSendEvents(List<Uri> eventUris, PushParameters parameters, PCFPushSendAnalyticsListener listener) {
        verifyRequestArguments(eventUris, listener);
        processRequest(eventUris, parameters, listener);
    }

    private void verifyRequestArguments(List<Uri> uris, PCFPushSendAnalyticsListener listener) {
        if (uris == null || uris.isEmpty()) {
            throw new IllegalArgumentException("uris may not be null or empty");
        }
        if (listener == null) {
            throw new IllegalArgumentException("listener may not be null");
        }
    }

    private void processRequest(List<Uri> uris, PushParameters parameters, PCFPushSendAnalyticsListener listener) {

        OutputStream outputStream = null;

        try {

            final URL url = getUrl(parameters);

            // TODO - once the server supports receiving analytics events then remove
            // this silly 'if' block and let the library post the events for real.
            // At this time, if you attempt to post events to the server
            // then you will get a 405 error.

            if (POST_TO_BACK_END) {

                final HttpURLConnection urlConnection = getHttpURLConnection(url);
                //final URL sendEventsUrl = new URL(baseServerUrl, Const.BACKEND_SEND_EVENTS_ENDPOINT);
                //final HttpURLConnection urlConnection = getHttpURLConnection(sendEventsUrl);
                urlConnection.addRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.connect();

                outputStream = new BufferedOutputStream(urlConnection.getOutputStream());

                // TODO - serialize events directly as JSON into the url connection?

                final String requestBodyData = getRequestBodyData(uris);
                Logger.v("Making network request to post event data to the back-end server: " + requestBodyData);
                writeOutput(requestBodyData, outputStream);

                final int statusCode = urlConnection.getResponseCode();
                urlConnection.disconnect();

                onSuccessfulNetworkRequest(statusCode, listener);

            } else { // FAKE IT!

                // NOTE: the server does not support receiving events at this time.  In the meanwhile,
                // this block of code is hard-coded to pretend that the library posts to the server and succeeds.

                final String requestBodyData = getRequestBodyData(uris);
                Logger.v("Making network request to post event data to the back-end server: " + requestBodyData);

                final int statusCode = 200;
                onSuccessfulNetworkRequest(statusCode, listener);
            }

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
        final List<Event> events = getEvents(uris);
        final EventList eventList = new EventList();
        eventList.setEvents(events);
        final Gson gson = new Gson();
        final String requestBodyData = gson.toJson(eventList);
        return requestBodyData;
    }

    private List<Event> getEvents(List<Uri> uris) {
        final List<Event> events = new LinkedList<Event>();
        for (final Uri uri : uris) {
            final Event event = eventsStorage.readEvent(uri);
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
        return new PCFPushSendAnalyticsApiRequestImpl(context, eventsStorage, parameters, networkWrapper);
    }
}
