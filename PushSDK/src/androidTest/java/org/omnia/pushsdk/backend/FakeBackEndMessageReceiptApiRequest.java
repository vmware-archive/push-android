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
    public void startSendMessageReceipts(List<MessageReceiptData> messageReceipts, BackEndMessageReceiptListener listener) {

        wasRequestAttempted = true;
        if (originatingRequest != null) {
            originatingRequest.wasRequestAttempted = true;
        }

        if (willBeSuccessfulRequest) {
            receivedMessageReceipts = messageReceipts;
            if (originatingRequest != null) {
                originatingRequest.receivedMessageReceipts = messageReceipts;
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
        if (receivedMessageReceipts == null) {
            return 0;
        } else {
            return receivedMessageReceipts.size();
        }
    };
}
