package org.omnia.pushsdk.backend;

import org.omnia.pushsdk.sample.model.MessageReceiptData;

import java.util.List;

public interface BackEndMessageReceiptApiRequest {

    void startMessageReceipt(List<MessageReceiptData> messageReceipts, BackEndMessageReceiptListener listener);
    BackEndMessageReceiptApiRequest copy();
}
