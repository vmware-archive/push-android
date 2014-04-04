package org.omnia.pushsdk.prefs;

import org.omnia.pushsdk.model.MessageReceiptEvent;

import java.util.LinkedList;
import java.util.List;

public class FakeMessageReceiptsProvider implements MessageReceiptsProvider {

    private List<MessageReceiptEvent> listMessageReceipts;
    private List<MessageReceiptEvent> extraListOfMessageReceipts;

    public FakeMessageReceiptsProvider(List<MessageReceiptEvent> listToLoad) {
        saveMessageReceipts(listToLoad);
    }

    public void loadExtraListOfMessageReceipts(List<MessageReceiptEvent> list) {
        // Set up an "extra" list of message receipts to load after the existing list is loaded in order
        // to test that that MessageReceiptService only clears the items it knows about instead of
        // items that get added afterwards (say, if a message arrives while some receipts are being posted
        // to the server).
        extraListOfMessageReceipts = new LinkedList<MessageReceiptEvent>(list);
    }

    @Override
    public synchronized List<MessageReceiptEvent> loadMessageReceipts() {
        final List<MessageReceiptEvent> listToReturn = new LinkedList<MessageReceiptEvent>(listMessageReceipts);
        if (extraListOfMessageReceipts != null) {
            listMessageReceipts.addAll(extraListOfMessageReceipts);
            extraListOfMessageReceipts = null;
        }
        return listToReturn;
    }

    @Override
    public synchronized void saveMessageReceipts(List<MessageReceiptEvent> messageReceipts) {
        if (messageReceipts != null) {
            listMessageReceipts = new LinkedList<MessageReceiptEvent>(messageReceipts);
        } else {
            listMessageReceipts = null;
        }
    }

    @Override
    public synchronized void addMessageReceipt(MessageReceiptEvent messageReceipt) {
        if (listMessageReceipts == null) {
            listMessageReceipts = new LinkedList<MessageReceiptEvent>();
        }
        listMessageReceipts.add(messageReceipt);
    }

    @Override
    public synchronized int removeMessageReceipts(List<MessageReceiptEvent> messageReceipts) {
        int numberOfItemsRemoved = 0;
        if (messageReceipts == null || messageReceipts.size() <= 0 || listMessageReceipts == null || listMessageReceipts.size() <= 0) {
            return 0;
        }
        for (MessageReceiptEvent messageReceipt : messageReceipts) {
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
