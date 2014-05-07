package com.pivotal.cf.mobile.analyticssdk.service;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.test.ServiceTestCase;

import com.pivotal.cf.mobile.analyticssdk.backend.BackEndSendEventsApiRequestProvider;
import com.pivotal.cf.mobile.analyticssdk.backend.FakeBackEndSendEventsApiRequest;
import com.pivotal.cf.mobile.analyticssdk.broadcastreceiver.FakeEventsSenderAlarmProvider;
import com.pivotal.cf.mobile.analyticssdk.database.DatabaseWrapper;
import com.pivotal.cf.mobile.analyticssdk.database.FakeEventsStorage;
import com.pivotal.cf.mobile.analyticssdk.jobs.DummyJob;
import com.pivotal.cf.mobile.analyticssdk.jobs.PrepareDatabaseJob;
import com.pivotal.cf.mobile.analyticssdk.model.events.EventTest;
import com.pivotal.cf.mobile.common.test.prefs.FakeAnalyticsPreferencesProvider;
import com.pivotal.cf.mobile.common.test.network.FakeNetworkWrapper;

import junit.framework.Assert;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class EventServiceTest extends ServiceTestCase<EventService> {

    private static final int DUMMY_RESULT_CODE = 1337;

    private FakeNetworkWrapper networkWrapper;
    private FakeEventsStorage eventsStorage;
    private FakeAnalyticsPreferencesProvider analyticsPreferencesProvider;
    private FakeEventsSenderAlarmProvider alarmProvider;
    private FakeBackEndSendEventsApiRequest backEndMessageReceiptApiRequest;
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
        analyticsPreferencesProvider = new FakeAnalyticsPreferencesProvider(true, null);
        backEndMessageReceiptApiRequest = new FakeBackEndSendEventsApiRequest();
        testResultReceiver = new TestResultReceiver(null);
        listOfCompletedJobs = new LinkedList<String>();

        alarmProvider = new FakeEventsSenderAlarmProvider();
        alarmProvider.enableAlarm();

        EventService.semaphore = new Semaphore(0);
        EventService.networkWrapper = networkWrapper;
        EventService.eventsStorage = eventsStorage;
        EventService.analyticsPreferencesProvider = analyticsPreferencesProvider;
        EventService.backEndSendEventsApiRequestProvider = new BackEndSendEventsApiRequestProvider(backEndMessageReceiptApiRequest);
        EventService.alarmProvider = alarmProvider;
        EventService.listOfCompletedJobs = listOfCompletedJobs;
    }

    @Override
    protected void tearDown() throws Exception {
        EventService.semaphore = null;
        EventService.networkWrapper = null;
        EventService.eventsStorage = null;
        EventService.analyticsPreferencesProvider = null;
        EventService.alarmProvider = null;
        EventService.backEndSendEventsApiRequestProvider = null;
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

    public void testAnalyticsDisabled() throws InterruptedException {
        analyticsPreferencesProvider.setIsAnalyticsEnabled(false);
        final DummyJob inputJob = new DummyJob();
        inputJob.setResultCode(DUMMY_RESULT_CODE);
        final Intent intent = EventService.getIntentToRunJob(getContext(), inputJob);
        addResultReceiverToIntent(intent);
        startService(intent);
        EventService.semaphore.acquire();
        assertEquals(EventService.ANALYTICS_DISABLED, testResultCode);
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
        Assert.assertEquals(inputJob.toString(), listOfCompletedJobs.get(0));
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
        Assert.assertEquals(inputJob.toString(), listOfCompletedJobs.get(1));
    }

    public void testDoesNotRunPrepareDatabaseJobIfReceivingAFreshDatabaseInstanceAndAnalyticsAreDisabled() throws InterruptedException {
        analyticsPreferencesProvider.setIsAnalyticsEnabled(false);
        EventService.eventsStorage = null;
        DatabaseWrapper.removeDatabaseInstance();
        final DummyJob inputJob = new DummyJob();
        final Intent intent = EventService.getIntentToRunJob(getContext(), inputJob);
        addResultReceiverToIntent(intent);
        startService(intent);
        EventService.semaphore.acquire();
        assertEquals(0, listOfCompletedJobs.size());
    }

    private void addResultReceiverToIntent(Intent intent) {
        intent.putExtra(EventService.KEY_RESULT_RECEIVER, testResultReceiver);
    }
}
