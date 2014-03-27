package org.omnia.pushsdk.prefs;

import org.omnia.pushsdk.sample.model.MessageReceiptData;

import java.util.List;

public interface MessageReceiptsProvider {

    List<MessageReceiptData> loadMessageReceipts();

    void saveMessageReceipts(List<MessageReceiptData> messageReceipts);

    void addMessageReceipt(MessageReceiptData messageReceipt);

    int removeMessageReceipts(List<MessageReceiptData> messageReceipts);

    int numberOfMessageReceipts();

    void clear();
}
