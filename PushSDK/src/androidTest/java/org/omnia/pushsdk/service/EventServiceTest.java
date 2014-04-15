package org.omnia.pushsdk.service;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.test.ServiceTestCase;

import org.omnia.pushsdk.backend.BackEndMessageReceiptApiRequestProvider;
import org.omnia.pushsdk.backend.FakeBackEndMessageReceiptApiRequest;
import org.omnia.pushsdk.broadcastreceiver.FakeMessageReceiptAlarmProvider;
import org.omnia.pushsdk.database.EventsStorage;
import org.omnia.pushsdk.database.FakeEventsStorage;
import org.omnia.pushsdk.jobs.BaseJob;
import org.omnia.pushsdk.jobs.DummyJob;
import org.omnia.pushsdk.jobs.Job;
import org.omnia.pushsdk.model.EventBase;
import org.omnia.pushsdk.model.MessageReceiptEvent;
import org.omnia.pushsdk.model.MessageReceiptEventTest;
import org.omnia.pushsdk.network.FakeNetworkWrapper;
import org.omnia.pushsdk.prefs.FakePreferencesProvider;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class EventServiceTest extends ServiceTestCase<EventService> {

    private static final int DUMMY_RESULT_CODE = 1337;

    private FakeNetworkWrapper networkWrapper;
    private FakeEventsStorage eventsStorage;
    private FakePreferencesProvider preferencesProvider;
    private FakeMessageReceiptAlarmProvider messageReceiptAlarmProvider;
    private FakeBackEndMessageReceiptApiRequest backEndMessageReceiptApiRequest;
    private int testResultCode = EventService.NO_RESULT;
    private TestResultReceiver testResultReceiver;

    // Captures result codes from the service itself
    public class TestResultReceiver extends ResultReceiver {

        public List<MessageReceiptEvent> extraEventsToAdd = null;

        public TestResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            testResultCode = resultCode;
        }
    }

    public EventServiceTest() {
        super(EventService.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        networkWrapper = new FakeNetworkWrapper();
        eventsStorage = new FakeEventsStorage();
        preferencesProvider = new FakePreferencesProvider(null, null, 0, null, null, null, null, null);
        backEndMessageReceiptApiRequest = new FakeBackEndMessageReceiptApiRequest();
        testResultReceiver = new TestResultReceiver(null);

        messageReceiptAlarmProvider = new FakeMessageReceiptAlarmProvider();
        messageReceiptAlarmProvider.enableAlarm();

        EventService.semaphore = new Semaphore(0);
        EventService.networkWrapper = networkWrapper;
        EventService.eventsStorage = eventsStorage;
        EventService.preferencesProvider = preferencesProvider;
        EventService.backEndMessageReceiptApiRequestProvider = new BackEndMessageReceiptApiRequestProvider(backEndMessageReceiptApiRequest);
        EventService.alarmProvider = messageReceiptAlarmProvider;
    }

    @Override
    protected void tearDown() throws Exception {
        EventService.semaphore = null;
        EventService.networkWrapper = null;
        EventService.eventsStorage = null;
        EventService.preferencesProvider = null;
        EventService.alarmProvider = null;
        EventService.backEndMessageReceiptApiRequestProvider = null;
        super.tearDown();
    }

    public void testReceiveNullIntent() throws InterruptedException {
        startService(null);
        EventService.semaphore.acquire();
        assertEquals(EventService.NO_RESULT, testResultCode);
    }

    public void testGetIntentToRunJob() {
        final DummyJob inputJob = new DummyJob();
        final Intent intent = EventService.getIntentToRunJob(getContext(), inputJob);
        assertNotNull(intent);
        assertTrue(intent.hasExtra(EventService.KEY_JOB));
        final DummyJob outputJob = intent.getParcelableExtra(EventService.KEY_JOB);
        assertEquals(inputJob, outputJob);
    }

    public void testRunNoJob() throws InterruptedException {
        final Intent intent = EventService.getIntentToRunJob(getContext(), null);
        addResultReceiverToIntent(intent);
        startService(intent);
        EventService.semaphore.acquire();
        assertEquals(EventService.NO_RESULT, testResultCode);
    }

    public void testRunNotParcelableJob() throws InterruptedException {
        final Intent intent = EventService.getIntentToRunJob(getContext(), null);
        intent.putExtra(EventService.KEY_JOB, "NOT A JOB");
        addResultReceiverToIntent(intent);
        startService(intent);
        EventService.semaphore.acquire();
        assertEquals(EventService.NO_RESULT, testResultCode);
    }

    public void testRunNotAJob() throws InterruptedException {
        final Intent intent = EventService.getIntentToRunJob(getContext(), null);
        intent.putExtra(EventService.KEY_JOB, MessageReceiptEventTest.getMessageReceiptEvent1());
        addResultReceiverToIntent(intent);
        startService(intent);
        EventService.semaphore.acquire();
        assertEquals(EventService.NO_RESULT, testResultCode);
    }

    public void testRunDummyJob() throws InterruptedException {
        final DummyJob inputJob = new DummyJob();
        inputJob.setResultCode(DUMMY_RESULT_CODE);
        final Intent intent = EventService.getIntentToRunJob(getContext(), inputJob);
        addResultReceiverToIntent(intent);
        startService(intent);
        EventService.semaphore.acquire();
        assertEquals(DUMMY_RESULT_CODE, testResultCode);
    }

    public void testJobInterrupts() throws InterruptedException {
        final DummyJob inputJob = new DummyJob();
        inputJob.setWillInterrupt(true);
        final Intent intent = EventService.getIntentToRunJob(getContext(), inputJob);
        addResultReceiverToIntent(intent);
        startService(intent);
        EventService.semaphore.acquire();
        assertEquals(EventService.JOB_INTERRUPTED, testResultCode);
    }

    private void addResultReceiverToIntent(Intent intent) {
        intent.putExtra(EventService.KEY_RESULT_RECEIVER, testResultReceiver);
    }
}
