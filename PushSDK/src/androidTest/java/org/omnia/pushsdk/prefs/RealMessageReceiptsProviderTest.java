package org.omnia.pushsdk.prefs;

import android.test.AndroidTestCase;

import org.omnia.pushsdk.sample.model.MessageReceiptData;
import org.omnia.pushsdk.sample.model.MessageReceiptDataTest;

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

    public void testAddAndAdd() {
        final MessageReceiptData messageReceipt1 = MessageReceiptDataTest.getMessageReceiptData1();
        final MessageReceiptData messageReceipt2 = MessageReceiptDataTest.getMessageReceiptData1();
        assertEquals(0, messageReceiptsProvider.numberOfMessageReceipts());

        messageReceiptsProvider.addMessageReceipt(messageReceipt1);
        assertEquals(1, messageReceiptsProvider.numberOfMessageReceipts());
        assertEquals(messageReceipt1, messageReceiptsProvider.loadMessageReceipts().get(0));

        messageReceiptsProvider.addMessageReceipt(messageReceipt2);
        assertEquals(2, messageReceiptsProvider.numberOfMessageReceipts());
        assertEquals(messageReceipt1, messageReceiptsProvider.loadMessageReceipts().get(0));
        assertEquals(messageReceipt1, messageReceiptsProvider.loadMessageReceipts().get(1));
    }
}
