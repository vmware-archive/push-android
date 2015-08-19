package io.pivotal.android.push.analytics.jobs;

import org.mockito.Mockito;

import java.util.Date;

import io.pivotal.android.push.backend.analytics.FakePCFPushCheckBackEndVersionApiRequest;
import io.pivotal.android.push.version.Version;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CheckBackEndVersionJobTest extends JobTest {

    public static final Version EXPECTED_VERSION = new Version("1.3.3.7");

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        preferencesProvider.setBackEndVersionTimePolled(new Date(0));
        preferencesProvider.setBackEndVersion(null);
        when(timeProvider.currentTimeMillis()).thenReturn(1337L);
        Mockito.doNothing().when(timeProvider).sleep(anyLong());
    }

    public void testIsPollingTime0() {
        preferencesProvider.setBackEndVersionTimePolled(null);
        when(timeProvider.currentTimeMillis()).thenReturn(0L);
        assertTrue(CheckBackEndVersionJob.isPollingTime(true, timeProvider, preferencesProvider));
    }

    public void testIsPollingTime1() {
        when(timeProvider.currentTimeMillis()).thenReturn(0L);
        assertFalse(CheckBackEndVersionJob.isPollingTime(true, timeProvider, preferencesProvider));
    }

    public void testIsPollingTime2() {
        when(timeProvider.currentTimeMillis()).thenReturn(5 * 60 * 1000L - 1);
        assertFalse(CheckBackEndVersionJob.isPollingTime(true, timeProvider, preferencesProvider));
    }

    public void testIsPollingTime3() {
        when(timeProvider.currentTimeMillis()).thenReturn(5 * 60 * 1000L);
        assertTrue(CheckBackEndVersionJob.isPollingTime(true, timeProvider, preferencesProvider));
    }

    public void testIsPollingTime4() {
        when(timeProvider.currentTimeMillis()).thenReturn(5 * 60 * 1000L + 1);
        assertTrue(CheckBackEndVersionJob.isPollingTime(true, timeProvider, preferencesProvider));
    }

    public void testSuccessfulRequest() throws InterruptedException {

        checkBackEndVersionApiRequest.setRequests(
                new FakePCFPushCheckBackEndVersionApiRequest.Request(FakePCFPushCheckBackEndVersionApiRequest.RequestResult.SUCCESS, EXPECTED_VERSION));

        final CheckBackEndVersionJob job = new CheckBackEndVersionJob();
        job.run(getJobParams(new JobResultListener() {

            @Override
            public void onJobComplete(int resultCode) {
                assertEquals(CheckBackEndVersionJob.RESULT_SERVER_VERSION_RETRIEVED_SUCCESSFULLY, resultCode);
                semaphore.release();
            }
        }));

        semaphore.acquire();
        assertEquals(1, checkBackEndVersionApiRequest.getNumberOfRequestsMade());
        assertEquals(EXPECTED_VERSION, preferencesProvider.getBackEndVersion());
        assertEquals(1337L, preferencesProvider.getBackEndVersionTimePolled().getTime());
        verify(timeProvider, never()).sleep(anyLong());
    }

    public void testOldServer() throws InterruptedException {

        checkBackEndVersionApiRequest.setRequests(
                new FakePCFPushCheckBackEndVersionApiRequest.Request(FakePCFPushCheckBackEndVersionApiRequest.RequestResult.OLD));

        final CheckBackEndVersionJob job = new CheckBackEndVersionJob();
        job.run(getJobParams(new JobResultListener() {

            @Override
            public void onJobComplete(int resultCode) {
                assertEquals(CheckBackEndVersionJob.RESULT_SERVER_VERSION_OLD, resultCode);
                semaphore.release();
            }
        }));

        semaphore.acquire();
        assertEquals(1, checkBackEndVersionApiRequest.getNumberOfRequestsMade());
        assertNull(preferencesProvider.getBackEndVersion());
        assertEquals(1337L, preferencesProvider.getBackEndVersionTimePolled().getTime());
        verify(timeProvider, never()).sleep(anyLong());
    }

    public void testFailedRequest() throws InterruptedException {

        checkBackEndVersionApiRequest.setRequests(
                new FakePCFPushCheckBackEndVersionApiRequest.Request(FakePCFPushCheckBackEndVersionApiRequest.RequestResult.RETRYABLE_FAILURE),
                new FakePCFPushCheckBackEndVersionApiRequest.Request(FakePCFPushCheckBackEndVersionApiRequest.RequestResult.RETRYABLE_FAILURE),
                new FakePCFPushCheckBackEndVersionApiRequest.Request(FakePCFPushCheckBackEndVersionApiRequest.RequestResult.RETRYABLE_FAILURE));

        final CheckBackEndVersionJob job = new CheckBackEndVersionJob();
        job.run(getJobParams(new JobResultListener() {

            @Override
            public void onJobComplete(int resultCode) {
                assertEquals(CheckBackEndVersionJob.RESULT_SERVER_VERSION_FAILED, resultCode);
                semaphore.release();
            }
        }));

        semaphore.acquire();
        assertEquals(3, checkBackEndVersionApiRequest.getNumberOfRequestsMade());
        assertNull(preferencesProvider.getBackEndVersion());
        assertEquals(0L, preferencesProvider.getBackEndVersionTimePolled().getTime());
        verify(timeProvider, times(2)).sleep(anyLong());
    }

    public void testRetryableFailureThenFatalFailure() throws InterruptedException {

        checkBackEndVersionApiRequest.setRequests(
                new FakePCFPushCheckBackEndVersionApiRequest.Request(FakePCFPushCheckBackEndVersionApiRequest.RequestResult.RETRYABLE_FAILURE),
                new FakePCFPushCheckBackEndVersionApiRequest.Request(FakePCFPushCheckBackEndVersionApiRequest.RequestResult.FATAL_FAILURE));

        final CheckBackEndVersionJob job = new CheckBackEndVersionJob();
        job.run(getJobParams(new JobResultListener() {

            @Override
            public void onJobComplete(int resultCode) {
                assertEquals(CheckBackEndVersionJob.RESULT_SERVER_VERSION_FAILED, resultCode);
                semaphore.release();
            }
        }));

        semaphore.acquire();
        assertEquals(2, checkBackEndVersionApiRequest.getNumberOfRequestsMade());
        assertNull(preferencesProvider.getBackEndVersion());
        assertEquals(0L, preferencesProvider.getBackEndVersionTimePolled().getTime());
        verify(timeProvider, times(1)).sleep(anyLong());
    }

    public void testFailedTwoRequestsThenSucceeded() throws InterruptedException {

        checkBackEndVersionApiRequest.setRequests(
                new FakePCFPushCheckBackEndVersionApiRequest.Request(FakePCFPushCheckBackEndVersionApiRequest.RequestResult.RETRYABLE_FAILURE),
                new FakePCFPushCheckBackEndVersionApiRequest.Request(FakePCFPushCheckBackEndVersionApiRequest.RequestResult.RETRYABLE_FAILURE),
                new FakePCFPushCheckBackEndVersionApiRequest.Request(FakePCFPushCheckBackEndVersionApiRequest.RequestResult.SUCCESS, EXPECTED_VERSION));

        final CheckBackEndVersionJob job = new CheckBackEndVersionJob();
        job.run(getJobParams(new JobResultListener() {

            @Override
            public void onJobComplete(int resultCode) {
                assertEquals(CheckBackEndVersionJob.RESULT_SERVER_VERSION_RETRIEVED_SUCCESSFULLY, resultCode);
                semaphore.release();
            }
        }));

        semaphore.acquire();
        assertEquals(3, checkBackEndVersionApiRequest.getNumberOfRequestsMade());
        assertEquals(EXPECTED_VERSION, preferencesProvider.getBackEndVersion());
        assertEquals(1337L, preferencesProvider.getBackEndVersionTimePolled().getTime());
        verify(timeProvider, times(2)).sleep(anyLong());
    }

    public void testFailedTwoRequestsThenOld() throws InterruptedException {

        checkBackEndVersionApiRequest.setRequests(
                new FakePCFPushCheckBackEndVersionApiRequest.Request(FakePCFPushCheckBackEndVersionApiRequest.RequestResult.RETRYABLE_FAILURE),
                new FakePCFPushCheckBackEndVersionApiRequest.Request(FakePCFPushCheckBackEndVersionApiRequest.RequestResult.RETRYABLE_FAILURE),
                new FakePCFPushCheckBackEndVersionApiRequest.Request(FakePCFPushCheckBackEndVersionApiRequest.RequestResult.OLD));

        final CheckBackEndVersionJob job = new CheckBackEndVersionJob();
        job.run(getJobParams(new JobResultListener() {

            @Override
            public void onJobComplete(int resultCode) {
                assertEquals(CheckBackEndVersionJob.RESULT_SERVER_VERSION_OLD, resultCode);
                semaphore.release();
            }
        }));

        semaphore.acquire();
        assertEquals(3, checkBackEndVersionApiRequest.getNumberOfRequestsMade());
        assertEquals(1337L, preferencesProvider.getBackEndVersionTimePolled().getTime());
        verify(timeProvider, times(2)).sleep(anyLong());
    }

    public void testEquals() {
        final CheckBackEndVersionJob job1 = new CheckBackEndVersionJob();
        final CheckBackEndVersionJob job2 = new CheckBackEndVersionJob();
        assertEquals(job1, job2);
    }

    public void testParcelsData() {
        final CheckBackEndVersionJob inputJob = new CheckBackEndVersionJob();
        final CheckBackEndVersionJob outputJob = getJobViaParcel(inputJob);
        assertNotNull(outputJob);
        assertEquals(inputJob, outputJob);
    }
}
