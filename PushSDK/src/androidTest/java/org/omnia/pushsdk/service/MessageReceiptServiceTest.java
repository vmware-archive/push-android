package org.omnia.pushsdk.service;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.test.ServiceTestCase;

import org.omnia.pushsdk.backend.BackEndMessageReceiptApiRequestProvider;
import org.omnia.pushsdk.backend.FakeBackEndMessageReceiptApiRequest;
import org.omnia.pushsdk.broadcastreceiver.FakeMessageReceiptAlarmProvider;
import org.omnia.pushsdk.database.EventsStorage;
import org.omnia.pushsdk.database.FakeEventsStorage;
import org.omnia.pushsdk.model.EventBase;
import org.omnia.pushsdk.model.MessageReceiptEvent;
import org.omnia.pushsdk.model.MessageReceiptEventTest;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class MessageReceiptServiceTest extends ServiceTestCase<MessageReceiptService> {

    private FakeEventsStorage eventsStorage;
    private FakeMessageReceiptAlarmProvider messageReceiptAlarmProvider;
    private int testResultCode = MessageReceiptService.NO_RESULT;
    private TestResultReceiver testResultReceiver;
    private FakeBackEndMessageReceiptApiRequest backEndMessageReceiptApiRequest;
    private List<MessageReceiptEvent> unpostedItemList;
    private List<MessageReceiptEvent> anotherUnpostedItemList;
    private List<MessageReceiptEvent> postingItemList;

    // Captures result codes from the service itself
    public class TestResultReceiver extends ResultReceiver {

        public List<MessageReceiptEvent> extraEventsToAdd = null;

        public TestResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            testResultCode = resultCode;
            if (extraEventsToAdd != null) {
                saveMessageReceipts(extraEventsToAdd);
            }
        }
    }

    public MessageReceiptServiceTest() {
        super(MessageReceiptService.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        eventsStorage = new FakeEventsStorage();
        testResultReceiver = new TestResultReceiver(null);
        backEndMessageReceiptApiRequest = new FakeBackEndMessageReceiptApiRequest();

        messageReceiptAlarmProvider = new FakeMessageReceiptAlarmProvider();
        messageReceiptAlarmProvider.enableAlarm();

        unpostedItemList = new LinkedList<MessageReceiptEvent>();
        unpostedItemList.add(MessageReceiptEventTest.getMessageReceiptEvent1());

        anotherUnpostedItemList = new LinkedList<MessageReceiptEvent>();
        anotherUnpostedItemList.add(MessageReceiptEventTest.getMessageReceiptEvent1());
        anotherUnpostedItemList.get(0).setEventId("ANOTHER EVENT ID");

        postingItemList = new LinkedList<MessageReceiptEvent>();
        postingItemList.add(MessageReceiptEventTest.getMessageReceiptEvent2());
        postingItemList.get(0).setStatus(EventBase.Status.POSTING);

        MessageReceiptService.semaphore = new Semaphore(0);
        MessageReceiptService.eventsStorage = eventsStorage;
        MessageReceiptService.backEndMessageReceiptApiRequestProvider = new BackEndMessageReceiptApiRequestProvider(backEndMessageReceiptApiRequest);
        MessageReceiptService.messageReceiptAlarmProvider = messageReceiptAlarmProvider;
    }

    @Override
    protected void tearDown() throws Exception {
        MessageReceiptService.semaphore = null;
        MessageReceiptService.eventsStorage = null;
        MessageReceiptService.backEndMessageReceiptApiRequestProvider = null;
        MessageReceiptService.messageReceiptAlarmProvider = null;
        super.tearDown();
    }

    public void testReceiveNullIntent() throws InterruptedException {
        startService(null);
        MessageReceiptService.semaphore.acquire();
        assertEquals(MessageReceiptService.NO_RESULT, testResultCode);
        assertNumberOfMessageReceiptsInStorage(0);
        assertFalse(backEndMessageReceiptApiRequest.wasRequestAttempted());
        assertFalse(messageReceiptAlarmProvider.isAlarmEnabled());
    }

    public void testEmptyJob() throws InterruptedException {
        startService(getServiceIntent());
        MessageReceiptService.semaphore.acquire();
        assertEquals(MessageReceiptService.RESULT_NO_WORK_TO_DO, testResultCode);
        assertNumberOfMessageReceiptsInStorage(0);
        assertFalse(backEndMessageReceiptApiRequest.wasRequestAttempted());
        assertFalse(messageReceiptAlarmProvider.isAlarmEnabled());
    }

    public void testSuccessfulSendWithOneItem() throws InterruptedException {
        saveMessageReceipts(unpostedItemList);
        backEndMessageReceiptApiRequest.setWillBeSuccessfulRequest(true);
        startService(getServiceIntent());
        MessageReceiptService.semaphore.acquire();
        assertEquals(MessageReceiptService.RESULT_SENT_RECEIPTS_SUCCESSFULLY, testResultCode);
        assertNumberOfMessageReceiptsInStorage(0);
        assertTrue(backEndMessageReceiptApiRequest.wasRequestAttempted());
        assertEquals(1, backEndMessageReceiptApiRequest.numberOfMessageReceiptsSent());
        assertFalse(messageReceiptAlarmProvider.isAlarmEnabled());
    }

    public void testFailedSendWithOneItem() throws InterruptedException {
        saveMessageReceipts(unpostedItemList);
        backEndMessageReceiptApiRequest.setWillBeSuccessfulRequest(false);
        startService(getServiceIntent());
        MessageReceiptService.semaphore.acquire();
        assertEquals(MessageReceiptService.RESULT_FAILED_TO_SEND_RECEIPTS, testResultCode);
        assertNumberOfMessageReceiptsInStorage(1);
        assertTrue(backEndMessageReceiptApiRequest.wasRequestAttempted());
        assertEquals(0, backEndMessageReceiptApiRequest.numberOfMessageReceiptsSent());
        assertTrue(messageReceiptAlarmProvider.isAlarmEnabled());
    }

    public void testSuccessfulSendWithOneItemWithAnotherPostingItemAlreadyInTheDatabase() throws InterruptedException {
        saveMessageReceipts(unpostedItemList);
        saveMessageReceipts(postingItemList);
        assertNumberOfMessageReceiptsInStorage(2);
        backEndMessageReceiptApiRequest.setWillBeSuccessfulRequest(true);
        startService(getServiceIntent());
        MessageReceiptService.semaphore.acquire();
        assertEquals(MessageReceiptService.RESULT_SENT_RECEIPTS_SUCCESSFULLY, testResultCode);
        assertNumberOfMessageReceiptsInStorage(1);
        assertTrue(backEndMessageReceiptApiRequest.wasRequestAttempted());
        assertEquals(1, backEndMessageReceiptApiRequest.numberOfMessageReceiptsSent());
        assertFalse(messageReceiptAlarmProvider.isAlarmEnabled());
    }

    public void testSuccessfulSendWithOneItemWithAnotherNotPostedItemWasInTheDatabase() throws InterruptedException {
        saveMessageReceipts(unpostedItemList);
        saveMessageReceipts(postingItemList);
        testResultReceiver.extraEventsToAdd = anotherUnpostedItemList;
        assertNumberOfMessageReceiptsInStorage(2);
        backEndMessageReceiptApiRequest.setWillBeSuccessfulRequest(true);
        startService(getServiceIntent());
        MessageReceiptService.semaphore.acquire();
        assertEquals(MessageReceiptService.RESULT_SENT_RECEIPTS_SUCCESSFULLY, testResultCode);
        assertNumberOfMessageReceiptsInStorage(2);
        assertTrue(backEndMessageReceiptApiRequest.wasRequestAttempted());
        assertEquals(1, backEndMessageReceiptApiRequest.numberOfMessageReceiptsSent());
        assertTrue(messageReceiptAlarmProvider.isAlarmEnabled());
    }

    private Intent getServiceIntent() {
        final Intent intent = new Intent(getContext(), GcmService.class);
        intent.putExtra(MessageReceiptService.KEY_RESULT_RECEIVER, testResultReceiver);
        return intent;
    }

    private void saveMessageReceipts(List<MessageReceiptEvent> events) {
        for (final MessageReceiptEvent event : events) {
            eventsStorage.saveEvent(getContext(), event, EventsStorage.EventType.MESSAGE_RECEIPTS);
        }
    }

    private void assertNumberOfMessageReceiptsInStorage(int expected) {
        assertEquals(expected, eventsStorage.getNumberOfEvents(getContext(), EventsStorage.EventType.MESSAGE_RECEIPTS));
    }
}
