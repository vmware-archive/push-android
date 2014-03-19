package org.omnia.pushsdk.backend;

public class FakeBackEndMessageReceiptApiRequest implements BackEndMessageReceiptApiRequest {

    private final FakeBackEndMessageReceiptApiRequest originatingRequest;
    private boolean willBeSuccessfulRequest = false;
    private String receivedMessageUuid = null;
    private boolean wasRequestAttempted = false;

    public FakeBackEndMessageReceiptApiRequest(FakeBackEndMessageReceiptApiRequest originatingRequest) {
        this.originatingRequest = originatingRequest;
    }

    public FakeBackEndMessageReceiptApiRequest() {
        this.originatingRequest = null;
        this.willBeSuccessfulRequest = false;
    }

    @Override
    public void startMessageReceipt(String messageUuid, BackEndMessageReceiptListener listener) {

        receivedMessageUuid = messageUuid;
        wasRequestAttempted = true;

        if (originatingRequest != null) {
            originatingRequest.receivedMessageUuid = messageUuid;
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
