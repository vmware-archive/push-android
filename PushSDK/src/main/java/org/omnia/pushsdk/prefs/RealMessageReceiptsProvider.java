package org.omnia.pushsdk.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import org.omnia.pushsdk.model.MessageReceiptData;
import org.omnia.pushsdk.util.Const;

import java.util.List;

public class RealMessageReceiptsProvider implements MessageReceiptsProvider {

    private static final String MESSAGE_RECEIPTS_TAG = Const.TAG_NAME + "MessageReceipts";
    private static final String PROPERTY_MESSAGE_RECEIPTS = "message_receipts";

    private final Context context;

    public RealMessageReceiptsProvider(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
        this.context = context;
    }

    @Override
    public List<MessageReceiptData> loadMessageReceipts() {
        final String str = getSharedPreferences().getString(PROPERTY_MESSAGE_RECEIPTS, null);
        if (str == null) {
            return null;
        }
        final List<MessageReceiptData> list = MessageReceiptData.jsonStringToList(str);
        return list;
    }

    @Override
    public void saveMessageReceipts(List<MessageReceiptData> messageReceipts) {
        final SharedPreferences prefs = getSharedPreferences();
        final SharedPreferences.Editor editor = prefs.edit();

        if (messageReceipts == null) {
            editor.clear();
        } else {
            final String str = MessageReceiptData.listToJsonString(messageReceipts);
            editor.putString(PROPERTY_MESSAGE_RECEIPTS, str);
        }
        editor.commit();
    }

    private SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences(MESSAGE_RECEIPTS_TAG, Context.MODE_PRIVATE);
    }
}
