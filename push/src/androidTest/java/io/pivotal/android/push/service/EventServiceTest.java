package io.pivotal.android.push.service;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.test.ServiceTestCase;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import io.pivotal.android.push.analytics.jobs.DummyJob;
import io.pivotal.android.push.analytics.jobs.PrepareDatabaseJob;
import io.pivotal.android.push.backend.analytics.FakePCFPushSendAnalyticsApiRequest;
import io.pivotal.android.push.backend.analytics.PCFPushSendAnalyticsApiRequestProvider;
import io.pivotal.android.push.database.DatabaseWrapper;
import io.pivotal.android.push.database.FakeEventsStorage;
import io.pivotal.android.push.model.analytics.EventTest;
import io.pivotal.android.push.prefs.FakePushPreferencesProvider;
import io.pivotal.android.push.receiver.FakeEventsSenderAlarmProvider;
import io.pivotal.android.push.util.FakeNetworkWrapper;

public class EventServiceTest extends ServiceTestCase<EventService> {

    private static final int DUMMY_RESULT_CODE = 1337;

    private FakeNetworkWrapper networkWrapper;
    private FakeEventsStorage eventsStorage;
    private FakePushPreferencesProvider pushPreferencesProvider;
    private FakeEventsSenderAlarmProvider alarmProvider;
    private FakePCFPushSendAnalyticsApiRequest apiRequest;
    private List<String> listOfCompletedJobs;
    private int testResultCode = EventService.NO_RESULT;
    private TestResultReceiver testResultReceiver;

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

    public EventServiceTest() {
        super(EventService.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        networkWrapper = new FakeNetworkWrapper();
        eventsStorage = new FakeEventsStorage();
        pushPreferencesProvider = new FakePushPreferencesProvider(null, null, 0, null, null, null, null, null, null, null, 0, false);
        apiRequest = new FakePCFPushSendAnalyticsApiRequest();
        testResultReceiver = new TestResultReceiver(null);
        listOfCompletedJobs = new ArrayList<>();

        alarmProvider = new FakeEventsSenderAlarmProvider();
        alarmProvider.enableAlarm();

        EventService.semaphore = new Semaphore(0);
        EventService.networkWrapper = networkWrapper;
        EventService.eventsStorage = eventsStorage;
        EventService.pushPreferencesProvider = pushPreferencesProvider;
        EventService.requestProvider = new PCFPushSendAnalyticsApiRequestProvider(apiRequest);
        EventService.alarmProvider = alarmProvider;
        EventService.listOfCompletedJobs = listOfCompletedJobs;
    }

    @Override
    protected void tearDown() throws Exception {
        EventService.semaphore = null;
        EventService.networkWrapper = null;
        EventService.eventsStorage = null;
        EventService.pushPreferencesProvider = null;
        EventService.alarmProvider = null;
        EventService.requestProvider = null;
        super.tearDown();
    }

    public void testGetIntentToRunJob() {
        final DummyJob inputJob = new DummyJob();
        final Intent intent = EventService.getIntentToRunJob(getContext(), inputJob);
        assertNotNull(intent);
        assertTrue(intent.hasExtra(EventService.KEY_JOB));
        final DummyJob outputJob = intent.getParcelableExtra(EventService.KEY_JOB);
        Assert.assertEquals(inputJob, outputJob);
    }

    public void testReceiveNullIntent() throws InterruptedException {
        startService(null);
        EventService.semaphore.acquire();
        assertEquals(EventService.NO_RESULT, testResultCode);
        assertEquals(0, listOfCompletedJobs.size());
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
        intent.putExtra(EventService.KEY_JOB, EventTest.getEvent1());
        addResultReceiverToIntent(intent);
        startService(intent);
        EventService.semaphore.acquire();
        assertEquals(EventService.NO_RESULT, testResultCode);
        assertEquals(0, listOfCompletedJobs.size());
    }

//    public void testAnalyticsDisabled() throws InterruptedException {
//        pushPreferencesProvider.setIsAnalyticsEnabled(false);
//        final DummyJob inputJob = new DummyJob();
//        inputJob.setResultCode(DUMMY_RESULT_CODE);
//        final Intent intent = EventService.getIntentToRunJob(getContext(), inputJob);
//        addResultReceiverToIntent(intent);
//        startService(intent);
//        EventService.semaphore.acquire();
//        assertEquals(EventService.ANALYTICS_DISABLED, testResultCode);
//        assertEquals(0, listOfCompletedJobs.size());
//    }

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

    public void testRunsPrepareDatabaseJobIfReceivingAFreshDatabaseInstanceAndAnalyticsAreEnabled() throws InterruptedException {
        EventService.eventsStorage = null;
        DatabaseWrapper.removeDatabaseInstance();
        final DummyJob inputJob = new DummyJob();
        final Intent intent = EventService.getIntentToRunJob(getContext(), inputJob);
        addResultReceiverToIntent(intent);
        startService(intent);
        EventService.semaphore.acquire();
        assertEquals(2, listOfCompletedJobs.size());
        Assert.assertEquals(new PrepareDatabaseJob().toString(), listOfCompletedJobs.get(0));
        assertEquals(inputJob.toString(), listOfCompletedJobs.get(1));
    }

//    public void testDoesNotRunPrepareDatabaseJobIfReceivingAFreshDatabaseInstanceAndAnalyticsAreDisabled() throws InterruptedException {
//        pushPreferencesProvider.setIsAnalyticsEnabled(false);
//        EventService.eventsStorage = null;
//        DatabaseWrapper.removeDatabaseInstance();
//        final DummyJob inputJob = new DummyJob();
//        final Intent intent = EventService.getIntentToRunJob(getContext(), inputJob);
//        addResultReceiverToIntent(intent);
//        startService(intent);
//        EventService.semaphore.acquire();
//        assertEquals(0, listOfCompletedJobs.size());
//    }

    private void addResultReceiverToIntent(Intent intent) {
        intent.putExtra(EventService.KEY_RESULT_RECEIVER, testResultReceiver);
    }
}
