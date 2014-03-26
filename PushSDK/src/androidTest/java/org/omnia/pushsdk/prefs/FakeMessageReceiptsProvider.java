package org.omnia.pushsdk.prefs;

import org.omnia.pushsdk.sample.model.MessageReceiptData;

import java.util.LinkedList;
import java.util.List;

public class FakeMessageReceiptsProvider implements MessageReceiptsProvider {

    private List<MessageReceiptData> listMessageReceipts;

    public FakeMessageReceiptsProvider(List<MessageReceiptData> listToLoad) {
        saveMessageReceipts(listToLoad);
    }

    @Override
    public List<MessageReceiptData> loadMessageReceipts() {
        // TODO - should we return an unmodifiable list?
        return listMessageReceipts;
    }

    @Override
    public void saveMessageReceipts(List<MessageReceiptData> messageReceipts) {
        if (messageReceipts != null) {
            listMessageReceipts = new LinkedList<MessageReceiptData>(messageReceipts);
        } else {
            listMessageReceipts = null;
        }
    }

    @Override
    public void addMessageReceipt(MessageReceiptData messageReceipt) {
        if (listMessageReceipts == null) {
            listMessageReceipts = new LinkedList<MessageReceiptData>();
        }
        listMessageReceipts.add(messageReceipt);
    }

    @Override
    public int numberOfMessageReceipts() {
        if (listMessageReceipts == null) {
            return 0;
        } else {
            return listMessageReceipts.size();
        }
    }

    @Override
    public void clear() {
        listMessageReceipts = null;
    }
}
