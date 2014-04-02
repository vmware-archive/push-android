package org.omnia.pushsdk.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import org.omnia.pushsdk.model.MessageReceiptData;
import org.omnia.pushsdk.util.Const;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MessageReceiptsProviderImpl implements MessageReceiptsProvider {

    private static final String MESSAGE_RECEIPTS_TAG = Const.TAG_NAME + "MessageReceipts";
    private static final String PROPERTY_MESSAGE_RECEIPTS = "message_receipts";

    private final Context context;
    private List<MessageReceiptData> listMessageReceipts;

    public MessageReceiptsProviderImpl(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
        this.context = context;
    }

    @Override
    public synchronized List<MessageReceiptData> loadMessageReceipts() {
        if (listMessageReceipts == null) {
            loadMessageReceiptsFromSharedPreferences();
            if (listMessageReceipts == null) {
                return null;
            }
        }
        return Collections.unmodifiableList(listMessageReceipts);
    }

    private void loadMessageReceiptsFromSharedPreferences() {
        final String str = getSharedPreferences().getString(PROPERTY_MESSAGE_RECEIPTS, null);
        if (str != null) {
            listMessageReceipts = MessageReceiptData.jsonStringToList(str);
        } else {
            listMessageReceipts = null;
        }
    }

    @Override
    public synchronized void saveMessageReceipts(List<MessageReceiptData> messageReceipts) {
        if (messageReceipts != null) {
            listMessageReceipts = new LinkedList<MessageReceiptData>(messageReceipts);
        } else {
            listMessageReceipts = null;
        }
        saveMessageReceiptsToSharedPreferences();
    }

    @Override
    public synchronized void addMessageReceipt(MessageReceiptData messageReceipt) {
        if (listMessageReceipts == null) {
            loadMessageReceiptsFromSharedPreferences();
        }
        if (listMessageReceipts == null) {
            listMessageReceipts = new LinkedList<MessageReceiptData>();
        }
        listMessageReceipts.add(messageReceipt);
        saveMessageReceiptsToSharedPreferences();
    }

    private void saveMessageReceiptsToSharedPreferences() {
        final SharedPreferences prefs = getSharedPreferences();
        final SharedPreferences.Editor editor = prefs.edit();

        if (listMessageReceipts == null) {
            editor.clear();
        } else {
            final String str = MessageReceiptData.listToJsonString(listMessageReceipts);
            editor.putString(PROPERTY_MESSAGE_RECEIPTS, str);
        }
        editor.commit();
    }

    @Override
    public synchronized int numberOfMessageReceipts() {
        if (listMessageReceipts == null) {
            loadMessageReceiptsFromSharedPreferences();
        }
        if (listMessageReceipts == null) {
            return 0;
        } else {
            return listMessageReceipts.size();
        }
    }

    private SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences(MESSAGE_RECEIPTS_TAG, Context.MODE_PRIVATE);
    }

    @Override
    public synchronized void clear() {
        listMessageReceipts = null;
        saveMessageReceiptsToSharedPreferences();
    }

    @Override
    public synchronized int removeMessageReceipts(List<MessageReceiptData> messageReceipts) {
        int numberOfItemsRemoved = 0;
        if (messageReceipts == null || messageReceipts.size() <= 0 || listMessageReceipts == null || listMessageReceipts.size() <= 0) {
            return 0;
        }
        final List<MessageReceiptData> newList = new LinkedList<MessageReceiptData>(listMessageReceipts);
        for (MessageReceiptData messageReceipt : messageReceipts) {
            if (newList.contains(messageReceipt)) {
                newList.remove(messageReceipt);
                numberOfItemsRemoved += 1;
            }
        }
        if (numberOfItemsRemoved > 0) {
            listMessageReceipts = newList;
            saveMessageReceiptsToSharedPreferences();
        }
        return numberOfItemsRemoved;
    }
}
