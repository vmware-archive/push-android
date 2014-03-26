package org.omnia.pushsdk.backend;

import org.omnia.pushsdk.sample.model.MessageReceiptData;

import java.util.List;

public class FakeBackEndMessageReceiptApiRequest implements BackEndMessageReceiptApiRequest {

    private final FakeBackEndMessageReceiptApiRequest originatingRequest;
    private boolean willBeSuccessfulRequest = false;
    private List<MessageReceiptData> receivedMessageReceipts = null;
    private boolean wasRequestAttempted = false;

    public FakeBackEndMessageReceiptApiRequest(FakeBackEndMessageReceiptApiRequest originatingRequest) {
        this.originatingRequest = originatingRequest;
    }

    public FakeBackEndMessageReceiptApiRequest() {
        this.originatingRequest = null;
        this.willBeSuccessfulRequest = false;
    }

    @Override
    public void startMessageReceipt(List<MessageReceiptData> messageReceipts, BackEndMessageReceiptListener listener) {

        receivedMessageReceipts = messageReceipts;
        wasRequestAttempted = true;

        if (originatingRequest != null) {
            originatingRequest.receivedMessageReceipts = messageReceipts;
            originatingRequest.wasRequestAttempted = true;
        }

        if (willBeSuccessfulRequest) {
            listener.onBackEndMessageReceiptSuccess();
        } else {
            listener.onBackEndMessageReceiptFailed("The fake request failed fakely.");
        }
    }

    @Override
    public BackEndMessageReceiptApiRequest copy() {
        return new FakeBackEndMessageReceiptApiRequest(this);
    }

    public boolean wasRequestAttempted() {
        return wasRequestAttempted;
    }

    public void setWillBeSuccessfulRequest(boolean b) {
        this.willBeSuccessfulRequest = b;
    }
}
