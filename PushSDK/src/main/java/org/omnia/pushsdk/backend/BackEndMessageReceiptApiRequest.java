package org.omnia.pushsdk.backend;

import org.omnia.pushsdk.model.MessageReceiptData;

import java.util.List;

public interface BackEndMessageReceiptApiRequest {

    void startSendMessageReceipts(List<MessageReceiptData> messageReceipts, BackEndMessageReceiptListener listener);
    BackEndMessageReceiptApiRequest copy();
}
