package org.omnia.pushsdk.backend;

public interface BackEndMessageReceiptApiRequest {

    void startMessageReceipt(String messageUuid, BackEndMessageReceiptListener listener);
    BackEndMessageReceiptApiRequest copy();
}
