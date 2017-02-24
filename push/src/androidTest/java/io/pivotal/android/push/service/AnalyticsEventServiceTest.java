package io.pivotal.android.push.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.test.ServiceTestCase;

import io.pivotal.android.push.prefs.PushPreferences;
import junit.framework.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import io.pivotal.android.push.analytics.jobs.DummyJob;
import io.pivotal.android.push.analytics.jobs.PrepareDatabaseJob;
import io.pivotal.android.push.backend.analytics.FakePCFPushSendAnalyticsApiRequest;
import io.pivotal.android.push.backend.analytics.PCFPushSendAnalyticsApiRequestProvider;
import io.pivotal.android.push.database.DatabaseWrapper;
import io.pivotal.android.push.database.FakeAnalyticsEventsStorage;
import io.pivotal.android.push.model.analytics.AnalyticsEventTest;
import io.pivotal.android.push.receiver.FakeAnalyticsEventsSenderAlarmProvider;
import io.pivotal.android.push.util.FakeNetworkWrapper;
import io.pivotal.android.push.util.FakeServiceStarter;

public class AnalyticsEventServiceTest extends ServiceTestCase<AnalyticsEventService> {

    private static final int DUMMY_RESULT_CODE = 1337;

    private List<String> listOfCompletedJobs;
    private int testResultCode = AnalyticsEventService.NO_RESULT;
    private TestResultReceiver testResultReceiver;
    private PushPreferences pushPreferences;

    // Captures result codes from the service itself
    public class TestResultReceiver extends ResultReceiver {
        public final Creator<ResultReceiver> CREATOR = null;

        public TestResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            testResultCode = resultCode;
        }
    }

    public AnalyticsEventServiceTest() {
        super(AnalyticsEventService.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        final FakeNetworkWrapper networkWrapper = new FakeNetworkWrapper();
        final FakeAnalyticsEventsStorage eventsStorage = new FakeAnalyticsEventsStorage();
        final FakePCFPushSendAnalyticsApiRequest apiRequest = new FakePCFPushSendAnalyticsApiRequest();
        final FakeServiceStarter serviceStarter = new FakeServiceStarter();

        pushPreferences = mock(PushPreferences.class);
        when(pushPreferences.areAnalyticsEnabled()).thenReturn(true);

        testResultReceiver = new TestResultReceiver(null);
        listOfCompletedJobs = new ArrayList<>();

        FakeAnalyticsEventsSenderAlarmProvider alarmProvider = new FakeAnalyticsEventsSenderAlarmProvider();
        alarmProvider.enableAlarm();

        AnalyticsEventService.semaphore = new Semaphore(0);
        AnalyticsEventService.networkWrapper = networkWrapper;
        AnalyticsEventService.serviceStarter = serviceStarter;
        AnalyticsEventService.eventsStorage = eventsStorage;
        AnalyticsEventService.pushPreferences = pushPreferences;
        AnalyticsEventService.sendAnalyticsRequestProvider = new PCFPushSendAnalyticsApiRequestProvider(apiRequest);
        AnalyticsEventService.alarmProvider = alarmProvider;
        AnalyticsEventService.listOfCompletedJobs = listOfCompletedJobs;
    }

    @Override
    protected void tearDown() throws Exception {
        AnalyticsEventService.semaphore = null;
        AnalyticsEventService.networkWrapper = null;
        AnalyticsEventService.serviceStarter = null;
        AnalyticsEventService.eventsStorage = null;
        AnalyticsEventService.pushPreferences = null;
        AnalyticsEventService.alarmProvider = null;
        AnalyticsEventService.sendAnalyticsRequestProvider = null;

        reset(pushPreferences);
        super.tearDown();
    }

    public void testGetIntentToRunJob() {
        final DummyJob inputJob = new DummyJob();
        final Intent intent = AnalyticsEventService.getIntentToRunJob(getContext(), inputJob);
        assertNotNull(intent);
        assertTrue(intent.hasExtra(AnalyticsEventService.KEY_JOB));
        final DummyJob outputJob = intent.getParcelableExtra(AnalyticsEventService.KEY_JOB);
        Assert.assertEquals(inputJob, outputJob);
    }

    public void testReceiveNullIntent() throws InterruptedException {
        startService(null);
        AnalyticsEventService.semaphore.acquire();
        assertEquals(AnalyticsEventService.NO_RESULT, testResultCode);
        assertEquals(0, listOfCompletedJobs.size());
    }

    public void testRunNoJob() throws InterruptedException {
        final Intent intent = AnalyticsEventService.getIntentToRunJob(getContext(), null);
        addResultReceiverToIntent(intent);
        startService(intent);
        AnalyticsEventService.semaphore.acquire();
        assertEquals(AnalyticsEventService.NO_RESULT, testResultCode);
        assertEquals(0, listOfCompletedJobs.size());
    }

    public void testRunNotParcelableJob() throws InterruptedException {
        final Intent intent = AnalyticsEventService.getIntentToRunJob(getContext(), null);
        intent.putExtra(AnalyticsEventService.KEY_JOB, "NOT A JOB");
        addResultReceiverToIntent(intent);
        startService(intent);
        AnalyticsEventService.semaphore.acquire();
        assertEquals(AnalyticsEventService.NO_RESULT, testResultCode);
        assertEquals(0, listOfCompletedJobs.size());
    }

    public void testRunNotAJob() throws InterruptedException {
        final Intent intent = AnalyticsEventService.getIntentToRunJob(getContext(), null);
        intent.putExtra(AnalyticsEventService.KEY_JOB, AnalyticsEventTest.getEvent1());
        addResultReceiverToIntent(intent);
        startService(intent);
        AnalyticsEventService.semaphore.acquire();
        assertEquals(AnalyticsEventService.NO_RESULT, testResultCode);
        assertEquals(0, listOfCompletedJobs.size());
    }

    public void testAnalyticsDisabled() throws InterruptedException {
        when(pushPreferences.areAnalyticsEnabled()).thenReturn(false);

        final DummyJob inputJob = new DummyJob();
        inputJob.setResultCode(DUMMY_RESULT_CODE);
        final Intent intent = AnalyticsEventService.getIntentToRunJob(getContext(), inputJob);
        addResultReceiverToIntent(intent);
        startService(intent);
        AnalyticsEventService.semaphore.acquire();
        assertEquals(AnalyticsEventService.ANALYTICS_DISABLED, testResultCode);
        assertEquals(0, listOfCompletedJobs.size());
    }

    public void testRunDummyJob() throws InterruptedException {
        final DummyJob inputJob = new DummyJob();
        inputJob.setResultCode(DUMMY_RESULT_CODE);
        final Intent intent = AnalyticsEventService.getIntentToRunJob(getContext(), inputJob);
        addResultReceiverToIntent(intent);
        startService(intent);
        AnalyticsEventService.semaphore.acquire();
        assertEquals(DUMMY_RESULT_CODE, testResultCode);
        assertEquals(1, listOfCompletedJobs.size());
        assertEquals(inputJob.toString(), listOfCompletedJobs.get(0));
    }

    public void testJobInterrupts() throws InterruptedException {
        final DummyJob inputJob = new DummyJob();
        inputJob.setWillInterrupt(true);
        final Intent intent = AnalyticsEventService.getIntentToRunJob(getContext(), inputJob);
        addResultReceiverToIntent(intent);
        startService(intent);
        AnalyticsEventService.semaphore.acquire();
        assertEquals(AnalyticsEventService.JOB_INTERRUPTED, testResultCode);
        assertEquals(0, listOfCompletedJobs.size());
    }

    public void testRunsPrepareDatabaseJobIfReceivingAFreshDatabaseInstanceAndAnalyticsAreEnabled() throws InterruptedException {
        AnalyticsEventService.eventsStorage = null;
        DatabaseWrapper.removeDatabaseInstance();
        final DummyJob inputJob = new DummyJob();
        final Intent intent = AnalyticsEventService.getIntentToRunJob(getContext(), inputJob);
        addResultReceiverToIntent(intent);
        startService(intent);
        AnalyticsEventService.semaphore.acquire();
        assertEquals(2, listOfCompletedJobs.size());
        Assert.assertEquals(new PrepareDatabaseJob(true).toString(), listOfCompletedJobs.get(0));
        assertEquals(inputJob.toString(), listOfCompletedJobs.get(1));
    }

    public void testDoesNotRunPrepareDatabaseJobIfReceivingAFreshDatabaseInstanceAndAnalyticsAreDisabled() throws InterruptedException {
        when(pushPreferences.areAnalyticsEnabled()).thenReturn(false);

        AnalyticsEventService.eventsStorage = null;
        DatabaseWrapper.removeDatabaseInstance();
        final DummyJob inputJob = new DummyJob();
        final Intent intent = AnalyticsEventService.getIntentToRunJob(getContext(), inputJob);
        addResultReceiverToIntent(intent);
        startService(intent);
        AnalyticsEventService.semaphore.acquire();
        assertEquals(0, listOfCompletedJobs.size());
    }

    private void addResultReceiverToIntent(Intent intent) {
        intent.putExtra(AnalyticsEventService.KEY_RESULT_RECEIVER, testResultReceiver);
    }
}
