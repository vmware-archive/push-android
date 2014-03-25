package org.omnia.pushsdk.prefs;

import android.test.AndroidTestCase;

import org.omnia.pushsdk.model.MessageReceiptData;
import org.omnia.pushsdk.model.MessageReceiptDataTest;

import java.util.List;

public class RealMessageReceiptsProviderTest extends AndroidTestCase {

    private RealMessageReceiptsProvider messageReceiptsProvider;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        messageReceiptsProvider = new RealMessageReceiptsProvider(getContext());
        messageReceiptsProvider.clear();
    }

    public void testClear() {
        messageReceiptsProvider.clear();
        assertEquals(0, messageReceiptsProvider.numberOfMessageReceipts());
        assertNull(messageReceiptsProvider.loadMessageReceipts());
    }

    public void testSaveAndLoad() {
        final List<MessageReceiptData> list1 = MessageReceiptData.jsonStringToList(MessageReceiptDataTest.getTestListOfMessageReceipts());
        messageReceiptsProvider.saveMessageReceipts(list1);
        assertEquals(2, messageReceiptsProvider.numberOfMessageReceipts());
        final List<MessageReceiptData> list2 = messageReceiptsProvider.loadMessageReceipts();
        assertEquals(2, list2.size());
        assertEquals(list1.get(0), list2.get(0));
        assertEquals(list1.get(1), list2.get(1));
    }

    public void testSaveNullAndLoad() {
        final List<MessageReceiptData> list1 = MessageReceiptData.jsonStringToList(MessageReceiptDataTest.getTestListOfMessageReceipts());
        messageReceiptsProvider.saveMessageReceipts(list1);
        messageReceiptsProvider.saveMessageReceipts(null);
        assertEquals(0, messageReceiptsProvider.numberOfMessageReceipts());
        assertNull(messageReceiptsProvider.loadMessageReceipts());
    }
}
