package org.omnia.pushsdk.backend;

import com.google.gson.Gson;

import org.omnia.pushsdk.network.NetworkWrapper;
import org.omnia.pushsdk.model.MessageReceiptData;
import org.omnia.pushsdk.util.Const;
import org.omnia.pushsdk.util.PushLibLogger;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class BackEndMessageReceiptApiRequestImpl extends ApiRequestImpl implements BackEndMessageReceiptApiRequest {

    public BackEndMessageReceiptApiRequestImpl(NetworkWrapper networkWrapper) {
        super(networkWrapper);
    }

    public void startSendMessageReceipts(List<MessageReceiptData> messageReceipts, BackEndMessageReceiptListener listener) {
        verifyRequestArguments(messageReceipts, listener);
        processRequest(messageReceipts, listener);
    }

    private void verifyRequestArguments(List<MessageReceiptData> messageReceipts, BackEndMessageReceiptListener listener) {
        if (messageReceipts == null || messageReceipts.isEmpty()) {
            throw new IllegalArgumentException("messageReceipts may not be null or empty");
        }
        if (listener == null) {
            throw new IllegalArgumentException("listener may not be null");
        }
    }

    private void processRequest(List<MessageReceiptData> messageReceipts, BackEndMessageReceiptListener listener) {

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
            final String requestBodyData = getRequestBodyData(messageReceipts);
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

    private String getRequestBodyData(List<MessageReceiptData> messageReceipts) {
        final Gson gson = new Gson();
        final String requestBodyData = gson.toJson(messageReceipts);
        return requestBodyData;
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
        return new BackEndMessageReceiptApiRequestImpl(networkWrapper);
    }
}
