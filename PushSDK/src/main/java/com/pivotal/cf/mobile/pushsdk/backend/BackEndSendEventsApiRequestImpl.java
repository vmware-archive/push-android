package com.pivotal.cf.mobile.pushsdk.backend;

import android.content.Context;
import android.net.Uri;

import com.google.gson.Gson;
import com.pivotal.cf.mobile.pushsdk.database.EventsStorage;
import com.pivotal.cf.mobile.pushsdk.model.events.Event;
import com.pivotal.cf.mobile.pushsdk.network.NetworkWrapper;
import com.pivotal.cf.mobile.pushsdk.util.Const;
import com.pivotal.cf.mobile.pushsdk.util.PushLibLogger;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

// TODO: generalize to other event types
public class BackEndSendEventsApiRequestImpl extends ApiRequestImpl implements BackEndSendEventsApiRequest {

    private static final boolean POST_TO_BACK_END = false;
    private Context context;
    private EventsStorage eventsStorage;

    public BackEndSendEventsApiRequestImpl(Context context, EventsStorage eventsStorage, NetworkWrapper networkWrapper) {
        super(networkWrapper);
        verifyArguments(context, eventsStorage);
        saveArguments(context, eventsStorage);
    }

    private void verifyArguments(Context context, EventsStorage eventsStorage) {
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
        if (eventsStorage == null) {
            throw new IllegalArgumentException("eventsStorage may not be null");
        }
    }

    private void saveArguments(Context context, EventsStorage eventsStorage) {
        this.context = context;
        this.eventsStorage = eventsStorage;
    }

    public void startSendEvents(List<Uri> eventUris, BackEndSendEventsListener listener) {
        verifyRequestArguments(eventUris, listener);
        processRequest(eventUris, listener);
    }

    private void verifyRequestArguments(List<Uri> uris, BackEndSendEventsListener listener) {
        if (uris == null || uris.isEmpty()) {
            throw new IllegalArgumentException("uris may not be null or empty");
        }
        if (listener == null) {
            throw new IllegalArgumentException("listener may not be null");
        }
    }

    private void processRequest(List<Uri> uris, BackEndSendEventsListener listener) {

        OutputStream outputStream = null;

        try {

            // TODO - once the server supports receiving analytics events then remove
            // this silly 'if' block and let the library post the events for real.
            // At this time, if you attempt to post events to the server
            // then you will get a 405 error.

            if (POST_TO_BACK_END) {

                final URL url = new URL(Const.BACKEND_SEND_EVENTS_URL);
                final HttpURLConnection urlConnection = getHttpURLConnection(url);
                urlConnection.addRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.connect();

                outputStream = new BufferedOutputStream(urlConnection.getOutputStream());

                // TODO - serialize events directly as JSON into the url connection?

                final String requestBodyData = getRequestBodyData(uris);
                PushLibLogger.v("Making network request to post event data to the back-end server: " + requestBodyData);
                writeOutput(requestBodyData, outputStream);

                final int statusCode = urlConnection.getResponseCode();
                urlConnection.disconnect();

                onSuccessfulNetworkRequest(statusCode, listener);

            } else { // FAKE IT!

                // NOTE: the server does not support receiving events at this time.  In the meanwhile,
                // this block of code is hard-coded to pretend that the library posts to the server and succeeds.

                final String requestBodyData = getRequestBodyData(uris);
                PushLibLogger.v("Making network request to post event data to the back-end server: " + requestBodyData);

                final int statusCode = 200;
                onSuccessfulNetworkRequest(statusCode, listener);
            }

        } catch (Exception e) {
            PushLibLogger.ex("Sending event data to back-end server failed", e);
            listener.onBackEndSendEventsFailed(e.getLocalizedMessage());

        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {}
            }
        }
    }

    private String getRequestBodyData(List<Uri> uris) {
        final List<Event> events = getEvents(uris);
        final Gson gson = new Gson();
        final String requestBodyData = gson.toJson(events);
        return requestBodyData;
    }

    private List<Event> getEvents(List<Uri> uris) {
        final List<Event> events = new LinkedList<Event>();
        for (final Uri uri : uris) {
            final Event event = (Event) eventsStorage.readEvent(uri);
            events.add(event);
        }
        return events;
    }

    private void onSuccessfulNetworkRequest(int statusCode, BackEndSendEventsListener listener) {

        if (isFailureStatusCode(statusCode)) {
            PushLibLogger.e("Sending event data to back-end server failed: server returned HTTP status " + statusCode);
            listener.onBackEndSendEventsFailed("Sending event data to back-end server returned HTTP status " + statusCode);
            return;
        }

        PushLibLogger.i("Sending event data to back-end server succeeded.");
        listener.onBackEndSendEventsSuccess();
    }

    @Override
    public BackEndSendEventsApiRequest copy() {
        return new BackEndSendEventsApiRequestImpl(context, eventsStorage, networkWrapper);
    }
}
