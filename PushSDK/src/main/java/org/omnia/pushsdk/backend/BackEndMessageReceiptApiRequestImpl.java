package org.omnia.pushsdk.backend;

import android.content.Context;
import android.net.Uri;

import com.google.gson.Gson;

import org.omnia.pushsdk.database.EventsStorage;
import org.omnia.pushsdk.model.MessageReceiptEvent;
import org.omnia.pushsdk.network.NetworkWrapper;
import org.omnia.pushsdk.util.PushLibLogger;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

public class BackEndMessageReceiptApiRequestImpl extends ApiRequestImpl implements BackEndMessageReceiptApiRequest {

    private Context context;
    private EventsStorage eventsStorage;

    public BackEndMessageReceiptApiRequestImpl(Context context, EventsStorage eventsStorage, NetworkWrapper networkWrapper) {
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

    public void startSendMessageReceipts(List<Uri> uris, BackEndMessageReceiptListener listener) {
        verifyRequestArguments(uris, listener);
        processRequest(uris, listener);
    }

    private void verifyRequestArguments(List<Uri> uris, BackEndMessageReceiptListener listener) {
        if (uris == null || uris.isEmpty()) {
            throw new IllegalArgumentException("uris may not be null or empty");
        }
        if (listener == null) {
            throw new IllegalArgumentException("listener may not be null");
        }
    }

    private void processRequest(List<Uri> uris, BackEndMessageReceiptListener listener) {

        OutputStream outputStream = null;

        try {
//            final URL url = new URL(Const.BACKEND_MESSAGE_RECEIPT_URL);
//            final HttpURLConnection urlConnection = getHttpURLConnection(url);
//            urlConnection.addRequestProperty("Content-Type", "application/json");
//            urlConnection.setRequestMethod("POST");
//            urlConnection.setDoInput(true);
//            urlConnection.connect();
//
//            outputStream = new BufferedOutputStream(urlConnection.getOutputStream());

            // TODO - serialize events directly as JSON into the url connection?

            final String requestBodyData = getRequestBodyData(uris);
            PushLibLogger.v("Making network request to post message receipts to the back-end server: " + requestBodyData);
//            writeOutput(requestBodyData, outputStream);
//
//            final int statusCode = urlConnection.getResponseCode();
//            urlConnection.disconnect();

            // TODO - restore the commented code above and stop hardcoding the statusCode once
            // the server accepts message receipts

            final int statusCode = 200; // <---- TODO! REMOVE!

            onSuccessfulNetworkRequest(statusCode, listener);

        } catch (Exception e) {
            PushLibLogger.ex("Back-end message receipt attempt failed", e);
            listener.onBackEndMessageReceiptFailed(e.getLocalizedMessage());

        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {}
            }
        }
    }

    private String getRequestBodyData(List<Uri> uris) {
        final List<MessageReceiptEvent> events = getEvents(uris);
        final Gson gson = new Gson();
        final String requestBodyData = gson.toJson(events);
        return requestBodyData;
    }

    private List<MessageReceiptEvent> getEvents(List<Uri> uris) {
        final List<MessageReceiptEvent> events = new LinkedList<MessageReceiptEvent>();
        for (final Uri uri : uris) {
            final MessageReceiptEvent event = (MessageReceiptEvent) eventsStorage.readEvent(context, uri);
            events.add(event);
        }
        return events;
    }

    private void onSuccessfulNetworkRequest(int statusCode, BackEndMessageReceiptListener listener) {

        if (isFailureStatusCode(statusCode)) {
            PushLibLogger.e("Back-end server message receipt failed: server returned HTTP status " + statusCode);
            listener.onBackEndMessageReceiptFailed("Back-end server message receipt returned HTTP status " + statusCode);
            return;
        }

        PushLibLogger.i("Back-end Server message receipt succeeded.");
        listener.onBackEndMessageReceiptSuccess();
    }

    @Override
    public BackEndMessageReceiptApiRequest copy() {
        return new BackEndMessageReceiptApiRequestImpl(context, eventsStorage, networkWrapper);
    }
}
