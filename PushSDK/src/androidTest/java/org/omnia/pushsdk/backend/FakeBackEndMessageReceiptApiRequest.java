package org.omnia.pushsdk.backend;

import android.net.Uri;

import java.util.Collections;
import java.util.List;

public class FakeBackEndMessageReceiptApiRequest implements BackEndMessageReceiptApiRequest {

    private final FakeBackEndMessageReceiptApiRequest originatingRequest;
    private boolean willBeSuccessfulRequest = false;
    private boolean wasRequestAttempted = false;
    private List<Uri> receivedUris = null;

    public FakeBackEndMessageReceiptApiRequest(FakeBackEndMessageReceiptApiRequest originatingRequest) {
        this.originatingRequest = originatingRequest;
    }

    public FakeBackEndMessageReceiptApiRequest() {
        this.originatingRequest = null;
        this.willBeSuccessfulRequest = false;
    }

    @Override
    public void startSendMessageReceipts(List<Uri> uris, BackEndMessageReceiptListener listener) {

        wasRequestAttempted = true;
        if (originatingRequest != null) {
            originatingRequest.wasRequestAttempted = true;
        }

        if (willBeSuccessfulRequest) {
            receivedUris = uris;
            if (originatingRequest != null) {
                originatingRequest.receivedUris = uris;
            }
            listener.onBackEndMessageReceiptSuccess();
        } else {
            listener.onBackEndMessageReceiptFailed("The fake request failed fakely.");
        }
    }

    @Override
    public BackEndMessageReceiptApiRequest copy() {
        final FakeBackEndMessageReceiptApiRequest newRequest = new FakeBackEndMessageReceiptApiRequest(this);
        newRequest.willBeSuccessfulRequest = willBeSuccessfulRequest;
        return newRequest;
    }

    public boolean wasRequestAttempted() {
        return wasRequestAttempted;
    }

    public void setWillBeSuccessfulRequest(boolean b) {
        this.willBeSuccessfulRequest = b;
    }

    public int numberOfMessageReceiptsSent() {
        if (receivedUris == null) {
            return 0;
        } else {
            return receivedUris.size();
        }
    };

    public List<Uri> getListOfReceivedUris() {
        return Collections.unmodifiableList(receivedUris);
    }
}
