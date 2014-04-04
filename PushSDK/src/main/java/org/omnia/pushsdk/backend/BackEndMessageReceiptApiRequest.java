package org.omnia.pushsdk.backend;

import org.omnia.pushsdk.model.MessageReceiptEvent;

import java.util.List;

public interface BackEndMessageReceiptApiRequest {

    void startSendMessageReceipts(List<MessageReceiptEvent> messageReceipts, BackEndMessageReceiptListener listener);
    BackEndMessageReceiptApiRequest copy();
}
