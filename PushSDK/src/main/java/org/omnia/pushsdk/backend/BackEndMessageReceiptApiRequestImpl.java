package org.omnia.pushsdk.backend;

import org.omnia.pushsdk.network.NetworkWrapper;
import org.omnia.pushsdk.util.Const;
import org.omnia.pushsdk.util.PushLibLogger;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class BackEndMessageReceiptApiRequestImpl extends ApiRequestImpl  {

    public BackEndMessageReceiptApiRequestImpl(NetworkWrapper networkWrapper) {
        super(networkWrapper);
    }

    public void startMessageReceipt(String messageUuid, BackEndMessageReceiptListener listener) {
        verifyRequestArguments(messageUuid, listener);
        processRequest(messageUuid, listener);
    }

    private void verifyRequestArguments(String messageUuid, BackEndMessageReceiptListener listener) {
        if (messageUuid == null || messageUuid.isEmpty()) {
            throw new IllegalArgumentException("messageUuid may not be null or empty");
        }
        if (listener == null) {
            throw new IllegalArgumentException("listener may not be null");
        }
    }

    private void processRequest(String messageUuid, BackEndMessageReceiptListener listener) {

        try {
            final String encodedMessageUuid = URLEncoder.encode(messageUuid, "UTF-8");
            final URL url = new URL(Const.BACKEND_MESSAGE_RECEIPT_URL + "/" + encodedMessageUuid);
            final HttpURLConnection urlConnection = getHttpURLConnection(url);
            urlConnection.setRequestMethod("POST");
            urlConnection.connect();

            final int statusCode = urlConnection.getResponseCode();
            urlConnection.disconnect();
            onSuccessfulNetworkRequest(statusCode, listener);

        } catch (Exception e) {
            PushLibLogger.ex("Back-end message receipt attempt failed", e);
            listener.onBackEndMessageReceiptFailed(e.getLocalizedMessage());
        }
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
}
