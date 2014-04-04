package org.omnia.pushsdk.prefs;

import org.omnia.pushsdk.model.MessageReceiptEvent;

import java.util.List;

public interface MessageReceiptsProvider {

    List<MessageReceiptEvent> loadMessageReceipts();

    void saveMessageReceipts(List<MessageReceiptEvent> messageReceipts);

    void addMessageReceipt(MessageReceiptEvent messageReceipt);

    int removeMessageReceipts(List<MessageReceiptEvent> messageReceipts);

    int numberOfMessageReceipts();

    void clear();
}
