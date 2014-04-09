package org.omnia.pushsdk.broadcastreceiver;

import android.content.Intent;
import android.test.AndroidTestCase;

import org.omnia.pushsdk.database.EventsStorage;
import org.omnia.pushsdk.database.FakeEventsStorage;
import org.omnia.pushsdk.model.MessageReceiptEventTest;

public class BootCompletedBroadcastReceiverTestCase extends AndroidTestCase {

    private BootCompletedBroadcastReceiver broadcastReceiver;
    private FakeMessageReceiptAlarmProvider messageReceiptAlarmProvider;
    private FakeEventsStorage eventsStorage;
    private Intent testIntent;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        testIntent = new Intent();
        messageReceiptAlarmProvider = new FakeMessageReceiptAlarmProvider();
        messageReceiptAlarmProvider.disableAlarm();
        eventsStorage = new FakeEventsStorage();
        broadcastReceiver = new BootCompletedBroadcastReceiver(getContext(), eventsStorage, messageReceiptAlarmProvider);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        messageReceiptAlarmProvider = null;
        eventsStorage = null;
    }

    public void testEmptyMessageReceiptQueue() {
        broadcastReceiver.onReceive(getContext(), testIntent);
        assertFalse(messageReceiptAlarmProvider.isAlarmEnabled());
        assertNumberOfMessageReceiptsInStorage(0);
    }

    public void testMessageReceiptQueueWithOneItem() {
        eventsStorage.saveEvent(getContext(), MessageReceiptEventTest.getMessageReceiptEvent1(), EventsStorage.EventType.MESSAGE_RECEIPT);
        broadcastReceiver.onReceive(getContext(), testIntent);
        assertTrue(messageReceiptAlarmProvider.isAlarmEnabled());
        assertNumberOfMessageReceiptsInStorage(1);
    }

    private void assertNumberOfMessageReceiptsInStorage(int expected) {
        assertEquals(expected, eventsStorage.getNumberOfEvents(getContext(), EventsStorage.EventType.MESSAGE_RECEIPT));
    }
}
