package com.pivotal.cf.mobile.pushsdk.service;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.test.ServiceTestCase;

import com.pivotal.cf.mobile.pushsdk.backend.BackEndMessageReceiptApiRequestProvider;
import com.pivotal.cf.mobile.pushsdk.backend.FakeBackEndMessageReceiptApiRequest;
import com.pivotal.cf.mobile.pushsdk.broadcastreceiver.FakeEventsSenderAlarmProvider;
import com.pivotal.cf.mobile.pushsdk.database.EventsDatabaseWrapper;
import com.pivotal.cf.mobile.pushsdk.database.FakeEventsStorage;
import com.pivotal.cf.mobile.pushsdk.jobs.DummyJob;
import com.pivotal.cf.mobile.pushsdk.jobs.PrepareDatabaseJob;
import com.pivotal.cf.mobile.pushsdk.model.BaseEventTest;
import com.pivotal.cf.mobile.pushsdk.model.MessageReceiptEvent;
import com.pivotal.cf.mobile.pushsdk.network.FakeNetworkWrapper;
import com.pivotal.cf.mobile.pushsdk.prefs.FakePreferencesProvider;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class EventServiceTest extends ServiceTestCase<EventService> {

    private static final int DUMMY_RESULT_CODE = 1337;

    private FakeNetworkWrapper networkWrapper;
    private FakeEventsStorage eventsStorage;
    private FakePreferencesProvider preferencesProvider;
    private FakeEventsSenderAlarmProvider messageReceiptAlarmProvider;
    private FakeBackEndMessageReceiptApiRequest backEndMessageReceiptApiRequest;
    private List<String> listOfCompletedJobs;
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
        listOfCompletedJobs = new LinkedList<String>();

        messageReceiptAlarmProvider = new FakeEventsSenderAlarmProvider();
        messageReceiptAlarmProvider.enableAlarm();

        EventService.semaphore = new Semaphore(0);
        EventService.networkWrapper = networkWrapper;
        EventService.eventsStorage = eventsStorage;
        EventService.preferencesProvider = preferencesProvider;
        EventService.backEndMessageReceiptApiRequestProvider = new BackEndMessageReceiptApiRequestProvider(backEndMessageReceiptApiRequest);
        EventService.alarmProvider = messageReceiptAlarmProvider;
        EventService.listOfCompletedJobs = listOfCompletedJobs;
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
        assertEquals(0, listOfCompletedJobs.size());
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
        assertEquals(0, listOfCompletedJobs.size());
    }

    public void testRunNotParcelableJob() throws InterruptedException {
        final Intent intent = EventService.getIntentToRunJob(getContext(), null);
        intent.putExtra(EventService.KEY_JOB, "NOT A JOB");
        addResultReceiverToIntent(intent);
        startService(intent);
        EventService.semaphore.acquire();
        assertEquals(EventService.NO_RESULT, testResultCode);
        assertEquals(0, listOfCompletedJobs.size());
    }

    public void testRunNotAJob() throws InterruptedException {
        final Intent intent = EventService.getIntentToRunJob(getContext(), null);
        intent.putExtra(EventService.KEY_JOB, BaseEventTest.getBaseEvent1());
        addResultReceiverToIntent(intent);
        startService(intent);
        EventService.semaphore.acquire();
        assertEquals(EventService.NO_RESULT, testResultCode);
        assertEquals(0, listOfCompletedJobs.size());
    }

    public void testRunDummyJob() throws InterruptedException {
        final DummyJob inputJob = new DummyJob();
        inputJob.setResultCode(DUMMY_RESULT_CODE);
        final Intent intent = EventService.getIntentToRunJob(getContext(), inputJob);
        addResultReceiverToIntent(intent);
        startService(intent);
        EventService.semaphore.acquire();
        assertEquals(DUMMY_RESULT_CODE, testResultCode);
        assertEquals(1, listOfCompletedJobs.size());
        assertEquals(inputJob.toString(), listOfCompletedJobs.get(0));
    }

    public void testJobInterrupts() throws InterruptedException {
        final DummyJob inputJob = new DummyJob();
        inputJob.setWillInterrupt(true);
        final Intent intent = EventService.getIntentToRunJob(getContext(), inputJob);
        addResultReceiverToIntent(intent);
        startService(intent);
        EventService.semaphore.acquire();
        assertEquals(EventService.JOB_INTERRUPTED, testResultCode);
        assertEquals(0, listOfCompletedJobs.size());
    }

    public void testRunsPrepareDatabaseJobIfReceivingAFreshDatabaseInstance() throws InterruptedException {
        EventService.eventsStorage = null;
        EventsDatabaseWrapper.removeDatabaseInstance();
        final DummyJob inputJob = new DummyJob();
        final Intent intent = EventService.getIntentToRunJob(getContext(), inputJob);
        addResultReceiverToIntent(intent);
        startService(intent);
        EventService.semaphore.acquire();
        assertEquals(2, listOfCompletedJobs.size());
        assertEquals(new PrepareDatabaseJob().toString(), listOfCompletedJobs.get(0));
        assertEquals(inputJob.toString(), listOfCompletedJobs.get(1));
    }

    private void addResultReceiverToIntent(Intent intent) {
        intent.putExtra(EventService.KEY_RESULT_RECEIVER, testResultReceiver);
    }
}
