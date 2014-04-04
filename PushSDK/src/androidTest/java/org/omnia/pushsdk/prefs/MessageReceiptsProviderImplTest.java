package org.omnia.pushsdk.prefs;

import android.test.AndroidTestCase;

import org.omnia.pushsdk.model.MessageReceiptData;
import org.omnia.pushsdk.model.MessageReceiptEvent;
import org.omnia.pushsdk.model.MessageReceiptEventTest;

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
        final List<MessageReceiptEvent> list1 = MessageReceiptEvent.jsonStringToList(MessageReceiptEventTest.getTestListOfMessageReceipts());
        messageReceiptsProvider.saveMessageReceipts(list1);
        assertEquals(2, messageReceiptsProvider.numberOfMessageReceipts());
        final List<MessageReceiptEvent> list2 = messageReceiptsProvider.loadMessageReceipts();
        assertEquals(2, list2.size());
        assertEquals(list1.get(0), list2.get(0));
        assertEquals(list1.get(1), list2.get(1));
    }

    public void testSaveNullAndLoad() {
        final List<MessageReceiptEvent> list1 = MessageReceiptEvent.jsonStringToList(MessageReceiptEventTest.getTestListOfMessageReceipts());
        messageReceiptsProvider.saveMessageReceipts(list1);
        messageReceiptsProvider.saveMessageReceipts(null);
        assertEquals(0, messageReceiptsProvider.numberOfMessageReceipts());
        assertNull(messageReceiptsProvider.loadMessageReceipts());
    }

    public void testAddAndAddToSameInstance() {
        final MessageReceiptEvent messageReceipt1 = MessageReceiptEventTest.getMessageReceiptEvent1();
        final MessageReceiptEvent messageReceipt2 = MessageReceiptEventTest.getMessageReceiptEvent2();
        assertEquals(0, messageReceiptsProvider.numberOfMessageReceipts());

        messageReceiptsProvider.addMessageReceipt(messageReceipt1);
        assertEquals(1, messageReceiptsProvider.numberOfMessageReceipts());
        assertEquals(messageReceipt1, messageReceiptsProvider.loadMessageReceipts().get(0));

        messageReceiptsProvider.addMessageReceipt(messageReceipt2);
        assertEquals(2, messageReceiptsProvider.numberOfMessageReceipts());
        assertEquals(messageReceipt1, messageReceiptsProvider.loadMessageReceipts().get(0));
        assertEquals(messageReceipt2, messageReceiptsProvider.loadMessageReceipts().get(1));
    }

    public void testAddAndAddToNewInstance() {
        final MessageReceiptEvent messageReceipt1 = MessageReceiptEventTest.getMessageReceiptEvent1();
        final MessageReceiptEvent messageReceipt2 = MessageReceiptEventTest.getMessageReceiptEvent2();
        assertEquals(0, messageReceiptsProvider.numberOfMessageReceipts());

        messageReceiptsProvider.addMessageReceipt(messageReceipt1);
        assertEquals(1, messageReceiptsProvider.numberOfMessageReceipts());
        assertEquals(messageReceipt1, messageReceiptsProvider.loadMessageReceipts().get(0));

        messageReceiptsProvider = new MessageReceiptsProviderImpl(getContext());

        messageReceiptsProvider.addMessageReceipt(messageReceipt2);
        assertEquals(2, messageReceiptsProvider.numberOfMessageReceipts());
        assertEquals(messageReceipt1, messageReceiptsProvider.loadMessageReceipts().get(0));
        assertEquals(messageReceipt2, messageReceiptsProvider.loadMessageReceipts().get(1));
    }

    public void testRemoveNullFromNullList() {
        messageReceiptsProvider.saveMessageReceipts(null);
        messageReceiptsProvider.removeMessageReceipts(null);
        assertEquals(0, messageReceiptsProvider.numberOfMessageReceipts());
    }

    public void testRemoveNullFromEmptyList() {
        messageReceiptsProvider.saveMessageReceipts(new LinkedList<MessageReceiptEvent>());
        messageReceiptsProvider.removeMessageReceipts(null);
        assertEquals(0, messageReceiptsProvider.numberOfMessageReceipts());
    }

    public void testRemoveEmptyListFromNullList() {
        messageReceiptsProvider.saveMessageReceipts(null);
        messageReceiptsProvider.removeMessageReceipts(new LinkedList<MessageReceiptEvent>());
        assertEquals(0, messageReceiptsProvider.numberOfMessageReceipts());
    }

    public void testRemoveEmptyListFromEmptyList() {
        messageReceiptsProvider.saveMessageReceipts(new LinkedList<MessageReceiptEvent>());
        messageReceiptsProvider.removeMessageReceipts(new LinkedList<MessageReceiptEvent>());
        assertEquals(0, messageReceiptsProvider.numberOfMessageReceipts());
    }

    public void testRemoveNullFromListWithOneItem() {
        setupListWithOneItem();
        messageReceiptsProvider.removeMessageReceipts(null);
        assertEquals(1, messageReceiptsProvider.numberOfMessageReceipts());
    }

    public void testRemoveOneItemFromListWithOneItem() {
        final MessageReceiptEvent messageReceipt = setupListWithOneItem();
        final List<MessageReceiptEvent> listToRemove = new LinkedList<MessageReceiptEvent>();
        listToRemove.add(messageReceipt);
        final int numberOfItemsRemoved = messageReceiptsProvider.removeMessageReceipts(listToRemove);
        assertEquals(0, messageReceiptsProvider.numberOfMessageReceipts());
        assertEquals(1, numberOfItemsRemoved);
    }

    public void testRemoveOneItemFromListWithNoItems() {
        final List<MessageReceiptEvent> listToRemove = new LinkedList<MessageReceiptEvent>();
        listToRemove.add(MessageReceiptEventTest.getMessageReceiptEvent1());
        final int numberOfItemsRemoved = messageReceiptsProvider.removeMessageReceipts(listToRemove);
        assertEquals(0, messageReceiptsProvider.numberOfMessageReceipts());
        assertEquals(0, numberOfItemsRemoved);
    }

    public void testRemoveOneItemFromListWithTwoItems() {
        final List<MessageReceiptEvent> list = setupListWithTwoItems();
        final List<MessageReceiptEvent> listToRemove = new LinkedList<MessageReceiptEvent>();
        listToRemove.add(list.get(0));
        final int numberOfItemsRemoved = messageReceiptsProvider.removeMessageReceipts(listToRemove);
        assertEquals(1, messageReceiptsProvider.numberOfMessageReceipts());
        assertEquals(1, numberOfItemsRemoved);
    }

    public void testSavesAfterRemove() {
        final List<MessageReceiptEvent> list = setupListWithTwoItems();
        final List<MessageReceiptEvent> listToRemove = new LinkedList<MessageReceiptEvent>();
        listToRemove.add(list.get(0));
        final int numberOfItemsRemoved = messageReceiptsProvider.removeMessageReceipts(listToRemove);
        assertEquals(1, messageReceiptsProvider.numberOfMessageReceipts());
        assertEquals(1, numberOfItemsRemoved);

        final MessageReceiptsProvider provider2 = new MessageReceiptsProviderImpl(getContext());
        assertEquals(1, provider2.numberOfMessageReceipts());
    }

    private MessageReceiptEvent setupListWithOneItem() {
        final MessageReceiptEvent messageReceipt = MessageReceiptEventTest.getMessageReceiptEvent1();
        messageReceiptsProvider.addMessageReceipt(messageReceipt);
        assertEquals(1, messageReceiptsProvider.numberOfMessageReceipts());
        return messageReceipt;
    }

    private List<MessageReceiptEvent> setupListWithTwoItems() {
        final MessageReceiptEvent messageReceipt1 = MessageReceiptEventTest.getMessageReceiptEvent1();
        final MessageReceiptEvent messageReceipt2 = MessageReceiptEventTest.getMessageReceiptEvent2();
        messageReceiptsProvider.addMessageReceipt(messageReceipt1);
        messageReceiptsProvider.addMessageReceipt(messageReceipt2);
        assertEquals(2, messageReceiptsProvider.numberOfMessageReceipts());
        final List<MessageReceiptEvent> list = new LinkedList<MessageReceiptEvent>();
        list.add(messageReceipt1);
        list.add(messageReceipt2);
        return list;
    }
}
