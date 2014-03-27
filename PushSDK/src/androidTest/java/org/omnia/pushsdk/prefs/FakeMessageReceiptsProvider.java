package org.omnia.pushsdk.prefs;

import org.omnia.pushsdk.sample.model.MessageReceiptData;

import java.util.LinkedList;
import java.util.List;

public class FakeMessageReceiptsProvider implements MessageReceiptsProvider {

    private List<MessageReceiptData> listMessageReceipts;
    private List<MessageReceiptData> extraListOfMessageReceipts;

    public FakeMessageReceiptsProvider(List<MessageReceiptData> listToLoad) {
        saveMessageReceipts(listToLoad);
    }

    public void loadExtraListOfMessageReceipts(List<MessageReceiptData> list) {
        // Set up an "extra" list of message receipts to load after the existing list is loaded in order
        // to test that that MessageReceiptService only clears the items it knows about instead of
        // items that get added afterwards (say, if a message arrives while some receipts are being posted
        // to the server).
        extraListOfMessageReceipts = new LinkedList<MessageReceiptData>(list);
    }

    @Override
    public synchronized List<MessageReceiptData> loadMessageReceipts() {
        final List<MessageReceiptData> listToReturn = new LinkedList<MessageReceiptData>(listMessageReceipts);
        if (extraListOfMessageReceipts != null) {
            listMessageReceipts.addAll(extraListOfMessageReceipts);
            extraListOfMessageReceipts = null;
        }
        return listToReturn;
    }

    @Override
    public synchronized void saveMessageReceipts(List<MessageReceiptData> messageReceipts) {
        if (messageReceipts != null) {
            listMessageReceipts = new LinkedList<MessageReceiptData>(messageReceipts);
        } else {
            listMessageReceipts = null;
        }
    }

    @Override
    public synchronized void addMessageReceipt(MessageReceiptData messageReceipt) {
        if (listMessageReceipts == null) {
            listMessageReceipts = new LinkedList<MessageReceiptData>();
        }
        listMessageReceipts.add(messageReceipt);
    }

    @Override
    public synchronized int removeMessageReceipts(List<MessageReceiptData> messageReceipts) {
        int numberOfItemsRemoved = 0;
        if (messageReceipts == null || messageReceipts.size() <= 0 || listMessageReceipts == null || listMessageReceipts.size() <= 0) {
            return 0;
        }
        for (MessageReceiptData messageReceipt : messageReceipts) {
            if (listMessageReceipts.contains(messageReceipt)) {
                listMessageReceipts.remove(messageReceipt);
                numberOfItemsRemoved += 1;
            }
        }
        return numberOfItemsRemoved;
    }

    @Override
    public synchronized int numberOfMessageReceipts() {
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
