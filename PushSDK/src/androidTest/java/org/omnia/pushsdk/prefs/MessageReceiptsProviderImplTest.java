package org.omnia.pushsdk.prefs;

import android.test.AndroidTestCase;

import org.omnia.pushsdk.sample.model.MessageReceiptData;
import org.omnia.pushsdk.sample.model.MessageReceiptDataTest;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MessageReceiptsProviderImplTest extends AndroidTestCase {

    private MessageReceiptsProviderImpl messageReceiptsProvider;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        messageReceiptsProvider = new MessageReceiptsProviderImpl(getContext());
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

    public void testAddAndAddToSameInstance() {
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

    public void testAddAndAddToNewInstance() {
        final MessageReceiptData messageReceipt1 = MessageReceiptDataTest.getMessageReceiptData1();
        final MessageReceiptData messageReceipt2 = MessageReceiptDataTest.getMessageReceiptData1();
        assertEquals(0, messageReceiptsProvider.numberOfMessageReceipts());

        messageReceiptsProvider.addMessageReceipt(messageReceipt1);
        assertEquals(1, messageReceiptsProvider.numberOfMessageReceipts());
        assertEquals(messageReceipt1, messageReceiptsProvider.loadMessageReceipts().get(0));

        messageReceiptsProvider = new MessageReceiptsProviderImpl(getContext());

        messageReceiptsProvider.addMessageReceipt(messageReceipt2);
        assertEquals(2, messageReceiptsProvider.numberOfMessageReceipts());
        assertEquals(messageReceipt1, messageReceiptsProvider.loadMessageReceipts().get(0));
        assertEquals(messageReceipt1, messageReceiptsProvider.loadMessageReceipts().get(1));
    }

    public void testRemoveNullFromNullList() {
        messageReceiptsProvider.saveMessageReceipts(null);
        messageReceiptsProvider.removeMessageReceipts(null);
        assertEquals(0, messageReceiptsProvider.numberOfMessageReceipts());
    }

    public void testRemoveNullFromEmptyList() {
        messageReceiptsProvider.saveMessageReceipts(new LinkedList<MessageReceiptData>());
        messageReceiptsProvider.removeMessageReceipts(null);
        assertEquals(0, messageReceiptsProvider.numberOfMessageReceipts());
    }

    public void testRemoveEmptyListFromNullList() {
        messageReceiptsProvider.saveMessageReceipts(null);
        messageReceiptsProvider.removeMessageReceipts(new LinkedList<MessageReceiptData>());
        assertEquals(0, messageReceiptsProvider.numberOfMessageReceipts());
    }

    public void testRemoveEmptyListFromEmptyList() {
        messageReceiptsProvider.saveMessageReceipts(new LinkedList<MessageReceiptData>());
        messageReceiptsProvider.removeMessageReceipts(new LinkedList<MessageReceiptData>());
        assertEquals(0, messageReceiptsProvider.numberOfMessageReceipts());
    }

    public void testRemoveNullFromListWithOneItem() {
        setupListWithOneItem();
        messageReceiptsProvider.removeMessageReceipts(null);
        assertEquals(1, messageReceiptsProvider.numberOfMessageReceipts());
    }

    public void testRemoveOneItemFromListWithOneItem() {
        final MessageReceiptData messageReceipt = setupListWithOneItem();
        final LinkedList<MessageReceiptData> listToRemove = new LinkedList<MessageReceiptData>();
        listToRemove.add(messageReceipt);
        final int numberOfItemsRemoved = messageReceiptsProvider.removeMessageReceipts(listToRemove);
        assertEquals(0, messageReceiptsProvider.numberOfMessageReceipts());
        assertEquals(1, numberOfItemsRemoved);
    }

    public void testRemoveOneItemFromListWithNoItems() {
        final List<MessageReceiptData> listToRemove = new LinkedList<MessageReceiptData>();
        listToRemove.add(MessageReceiptDataTest.getMessageReceiptData1());
        final int numberOfItemsRemoved = messageReceiptsProvider.removeMessageReceipts(listToRemove);
        assertEquals(0, messageReceiptsProvider.numberOfMessageReceipts());
        assertEquals(0, numberOfItemsRemoved);
    }

    public void testRemoveOneItemFromListWithTwoItems() {
        final List<MessageReceiptData> list = setupListWithTwoItems();
        final List<MessageReceiptData> listToRemove = new LinkedList<MessageReceiptData>();
        listToRemove.add(list.get(0));
        final int numberOfItemsRemoved = messageReceiptsProvider.removeMessageReceipts(listToRemove);
        assertEquals(1, messageReceiptsProvider.numberOfMessageReceipts());
        assertEquals(1, numberOfItemsRemoved);
    }

    public void testSavesAfterRemove() {
        final List<MessageReceiptData> list = setupListWithTwoItems();
        final List<MessageReceiptData> listToRemove = new LinkedList<MessageReceiptData>();
        listToRemove.add(list.get(0));
        final int numberOfItemsRemoved = messageReceiptsProvider.removeMessageReceipts(listToRemove);
        assertEquals(1, messageReceiptsProvider.numberOfMessageReceipts());
        assertEquals(1, numberOfItemsRemoved);

        final MessageReceiptsProvider provider2 = new MessageReceiptsProviderImpl(getContext());
        assertEquals(1, provider2.numberOfMessageReceipts());
    }

    private MessageReceiptData setupListWithOneItem() {
        final MessageReceiptData messageReceipt = MessageReceiptDataTest.getMessageReceiptData1();
        messageReceiptsProvider.addMessageReceipt(messageReceipt);
        assertEquals(1, messageReceiptsProvider.numberOfMessageReceipts());
        return messageReceipt;
    }

    private List<MessageReceiptData> setupListWithTwoItems() {
        final MessageReceiptData messageReceipt1 = MessageReceiptDataTest.getMessageReceiptData1();
        final MessageReceiptData messageReceipt2 = MessageReceiptDataTest.getMessageReceiptData1();
        messageReceiptsProvider.addMessageReceipt(messageReceipt1);
        messageReceiptsProvider.addMessageReceipt(messageReceipt2);
        assertEquals(2, messageReceiptsProvider.numberOfMessageReceipts());
        final List<MessageReceiptData> list = new LinkedList<MessageReceiptData>();
        list.add(messageReceipt1);
        list.add(messageReceipt2);
        return list;
    }
}
