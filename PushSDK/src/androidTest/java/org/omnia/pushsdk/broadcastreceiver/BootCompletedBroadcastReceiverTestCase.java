package org.omnia.pushsdk.broadcastreceiver;

import android.content.Intent;
import android.test.AndroidTestCase;

import org.omnia.pushsdk.model.MessageReceiptEventTest;
import org.omnia.pushsdk.prefs.FakeMessageReceiptsProvider;

public class BootCompletedBroadcastReceiverTestCase extends AndroidTestCase {

    private BootCompletedBroadcastReceiver broadcastReceiver;
    private FakeMessageReceiptAlarmProvider messageReceiptAlarmProvider;
    private FakeMessageReceiptsProvider messageReceiptsProvider;
    private Intent testIntent;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        testIntent = new Intent();
        messageReceiptAlarmProvider = new FakeMessageReceiptAlarmProvider();
        messageReceiptAlarmProvider.disableAlarm();
        messageReceiptsProvider = new FakeMessageReceiptsProvider(null);
        broadcastReceiver = new BootCompletedBroadcastReceiver(messageReceiptAlarmProvider, messageReceiptsProvider);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        messageReceiptAlarmProvider = null;
        messageReceiptsProvider = null;
    }

    public void testEmptyMessageReceiptQueue() {
        broadcastReceiver.onReceive(getContext(), testIntent);
        assertFalse(messageReceiptAlarmProvider.isAlarmEnabled());
        assertEquals(0, messageReceiptsProvider.numberOfMessageReceipts());
    }

    public void testMessageReceiptQueueWithOneItem() {
        messageReceiptsProvider.addMessageReceipt(MessageReceiptEventTest.getMessageReceiptEvent1());
        broadcastReceiver.onReceive(getContext(), testIntent);
        assertTrue(messageReceiptAlarmProvider.isAlarmEnabled());
        assertEquals(1, messageReceiptsProvider.numberOfMessageReceipts());
    }
}
