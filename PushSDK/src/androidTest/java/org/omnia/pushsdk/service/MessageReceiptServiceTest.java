package org.omnia.pushsdk.service;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.test.ServiceTestCase;

import org.omnia.pushsdk.backend.BackEndMessageReceiptApiRequestProvider;
import org.omnia.pushsdk.backend.FakeBackEndMessageReceiptApiRequest;
import org.omnia.pushsdk.broadcastreceiver.FakeMessageReceiptAlarmProvider;
import org.omnia.pushsdk.model.MessageReceiptEvent;
import org.omnia.pushsdk.model.MessageReceiptEventTest;
import org.omnia.pushsdk.prefs.FakeMessageReceiptsProvider;
import org.omnia.pushsdk.model.MessageReceiptData;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class MessageReceiptServiceTest extends ServiceTestCase<MessageReceiptService> {

    private FakeMessageReceiptsProvider messageReceiptsProvider;
    private FakeMessageReceiptAlarmProvider messageReceiptAlarmProvider;
    private int testResultCode = MessageReceiptService.NO_RESULT;
    private TestResultReceiver testResultReceiver;
    private Intent intent;
    private FakeBackEndMessageReceiptApiRequest backEndMessageReceiptApiRequest;
    private List<MessageReceiptEvent> listWithOneItem;
    private List<MessageReceiptEvent> listWithOneOtherItem;

    // Captures result codes from the service itself
    public class TestResultReceiver extends ResultReceiver {

        public TestResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            testResultCode = resultCode;
        }
    }

    public MessageReceiptServiceTest() {
        super(MessageReceiptService.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        intent = getServiceIntent();
        testResultReceiver = new TestResultReceiver(null);
        messageReceiptsProvider = new FakeMessageReceiptsProvider(null);
        messageReceiptAlarmProvider = new FakeMessageReceiptAlarmProvider();
        messageReceiptAlarmProvider.enableAlarm();
        backEndMessageReceiptApiRequest = new FakeBackEndMessageReceiptApiRequest();
        listWithOneItem = new LinkedList<MessageReceiptEvent>();
        listWithOneItem.add(MessageReceiptEventTest.getMessageReceiptEvent1());
        listWithOneOtherItem = new LinkedList<MessageReceiptEvent>();
        listWithOneOtherItem.add(MessageReceiptEventTest.getMessageReceiptEvent2());
        MessageReceiptService.semaphore = new Semaphore(0);
        MessageReceiptService.messageReceiptsProvider = messageReceiptsProvider;
        MessageReceiptService.backEndMessageReceiptApiRequestProvider = new BackEndMessageReceiptApiRequestProvider(backEndMessageReceiptApiRequest);
        MessageReceiptService.messageReceiptAlarmProvider = messageReceiptAlarmProvider;
    }

    @Override
    protected void tearDown() throws Exception {
        MessageReceiptService.semaphore = null;
        MessageReceiptService.messageReceiptsProvider = null;
        MessageReceiptService.backEndMessageReceiptApiRequestProvider = null;
        MessageReceiptService.messageReceiptAlarmProvider = null;
        super.tearDown();
    }

    public void testReceiveNullIntent() throws InterruptedException {
        startService(null);
        MessageReceiptService.semaphore.acquire();
        assertEquals(MessageReceiptService.NO_RESULT, testResultCode);
        assertEquals(0, messageReceiptsProvider.numberOfMessageReceipts());
        assertFalse(backEndMessageReceiptApiRequest.wasRequestAttempted());
        assertFalse(messageReceiptAlarmProvider.isAlarmEnabled());
    }

    public void testEmptyJob() throws InterruptedException {
        startService(getServiceIntent());
        MessageReceiptService.semaphore.acquire();
        assertEquals(MessageReceiptService.RESULT_NO_WORK_TO_DO, testResultCode);
        assertEquals(0, messageReceiptsProvider.numberOfMessageReceipts());
        assertFalse(backEndMessageReceiptApiRequest.wasRequestAttempted());
        assertFalse(messageReceiptAlarmProvider.isAlarmEnabled());
    }

    public void testSuccessfulSendWithOneItem() throws InterruptedException {
        messageReceiptsProvider.saveMessageReceipts(listWithOneItem);
        backEndMessageReceiptApiRequest.setWillBeSuccessfulRequest(true);
        startService(getServiceIntent());
        MessageReceiptService.semaphore.acquire();
        assertEquals(MessageReceiptService.RESULT_SENT_RECEIPTS_SUCCESSFULLY, testResultCode);
        assertEquals(0, messageReceiptsProvider.numberOfMessageReceipts());
        assertTrue(backEndMessageReceiptApiRequest.wasRequestAttempted());
        assertEquals(1, backEndMessageReceiptApiRequest.numberOfMessageReceiptsSent());
        assertFalse(messageReceiptAlarmProvider.isAlarmEnabled());
    }

    public void testFailedSendWithOneItem() throws InterruptedException {
        messageReceiptsProvider.saveMessageReceipts(listWithOneItem);
        backEndMessageReceiptApiRequest.setWillBeSuccessfulRequest(false);
        startService(getServiceIntent());
        MessageReceiptService.semaphore.acquire();
        assertEquals(MessageReceiptService.RESULT_FAILED_TO_SEND_RECEIPTS, testResultCode);
        assertEquals(1, messageReceiptsProvider.numberOfMessageReceipts());
        assertTrue(backEndMessageReceiptApiRequest.wasRequestAttempted());
        assertEquals(0, backEndMessageReceiptApiRequest.numberOfMessageReceiptsSent());
        assertTrue(messageReceiptAlarmProvider.isAlarmEnabled());
    }

    public void testSuccessfulSendWithOneItemWhileAnExtraItemIsAddedToTheList() throws InterruptedException {
        messageReceiptsProvider.saveMessageReceipts(listWithOneItem);
        messageReceiptsProvider.loadExtraListOfMessageReceipts(listWithOneOtherItem);
        backEndMessageReceiptApiRequest.setWillBeSuccessfulRequest(true);
        startService(getServiceIntent());
        MessageReceiptService.semaphore.acquire();
        assertEquals(MessageReceiptService.RESULT_SENT_RECEIPTS_SUCCESSFULLY, testResultCode);
        assertEquals(1, messageReceiptsProvider.numberOfMessageReceipts());
        assertTrue(backEndMessageReceiptApiRequest.wasRequestAttempted());
        assertEquals(1, backEndMessageReceiptApiRequest.numberOfMessageReceiptsSent());
        assertTrue(messageReceiptAlarmProvider.isAlarmEnabled());
    }

    private Intent getServiceIntent() {
        final Intent intent = new Intent(getContext(), GcmService.class);
        intent.putExtra(MessageReceiptService.KEY_RESULT_RECEIVER, testResultReceiver);
        return intent;
    }
}
