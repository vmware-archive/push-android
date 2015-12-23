package io.pivotal.android.push.analytics.jobs;

import android.content.Intent;
import android.net.Uri;

import io.pivotal.android.push.model.analytics.AnalyticsEvent;
import io.pivotal.android.push.service.AnalyticsEventService;

public class PrepareDatabaseJobTest extends JobTest {

    public void testHandlesEmptyDatabase() throws InterruptedException {

        assertDatabaseEventCount(0);

        runPrepareDatabaseJob(true);

        assertDatabaseEventCount(0);
    }

    public void testSendEventsForNotPostedEvent() throws InterruptedException {

        final Uri uri = saveEventWithStatus(AnalyticsEvent.Status.NOT_POSTED);
        assertDatabaseEventCount(1);

        runPrepareDatabaseJob(true);

        assertDatabaseEventCount(1);
        assertEventHasStatus(uri, AnalyticsEvent.Status.NOT_POSTED);
        assertNumberOfStartedJobs(1);
        assertTrue(getJob(0) instanceof SendAnalyticsEventsJob);
    }

    public void testDoesNotSendEventsForNotPostedEvent() throws InterruptedException {

        final Uri uri = saveEventWithStatus(AnalyticsEvent.Status.NOT_POSTED);
        assertDatabaseEventCount(1);

        runPrepareDatabaseJob(false);

        assertDatabaseEventCount(1);
        assertEventHasStatus(uri, AnalyticsEvent.Status.NOT_POSTED);
        assertNumberOfStartedJobs(0);
    }

    public void testSendEventsForPostingErrorEvent() throws InterruptedException {

        final Uri uri = saveEventWithStatus(AnalyticsEvent.Status.POSTING_ERROR);
        assertDatabaseEventCount(1);

        runPrepareDatabaseJob(true);

        assertDatabaseEventCount(1);
        assertEventHasStatus(uri, AnalyticsEvent.Status.POSTING_ERROR);
        assertNumberOfStartedJobs(1);
        assertTrue(getJob(0) instanceof SendAnalyticsEventsJob);
    }

    public void testDoesNotSendEventsForPostingErrorEvent() throws InterruptedException {

        final Uri uri = saveEventWithStatus(AnalyticsEvent.Status.POSTING_ERROR);
        assertDatabaseEventCount(1);

        runPrepareDatabaseJob(false);

        assertDatabaseEventCount(1);
        assertEventHasStatus(uri, AnalyticsEvent.Status.POSTING_ERROR);
        assertNumberOfStartedJobs(0);
    }

    public void testResetsStatusAndSendsEventsForPostingEvent() throws InterruptedException {

        final Uri uri = saveEventWithStatus(AnalyticsEvent.Status.POSTING);
        assertDatabaseEventCount(1);

        runPrepareDatabaseJob(true);

        assertDatabaseEventCount(1);
        assertEventHasStatus(uri, AnalyticsEvent.Status.NOT_POSTED);
        assertNumberOfStartedJobs(1);
        assertTrue(getJob(0) instanceof SendAnalyticsEventsJob);
    }

    public void testResetsStatusAndDoesNotSendsEventsForPostingEvent() throws InterruptedException {

        final Uri uri = saveEventWithStatus(AnalyticsEvent.Status.POSTING);
        assertDatabaseEventCount(1);

        runPrepareDatabaseJob(false);

        assertDatabaseEventCount(1);
        assertEventHasStatus(uri, AnalyticsEvent.Status.NOT_POSTED);
        assertNumberOfStartedJobs(0);
    }

    public void testDeletesPostedEvent() throws InterruptedException {

        final Uri uri = saveEventWithStatus(AnalyticsEvent.Status.POSTED);
        assertDatabaseEventCount(1);

        runPrepareDatabaseJob(true);

        assertDatabaseEventCount(0);
        assertEventNotInStorage(uri);
        assertNumberOfStartedJobs(0);
    }

    public void testHandlesDatabaseWithManyEvents() throws InterruptedException {

        final Uri uri1 = saveEventWithStatus(AnalyticsEvent.Status.NOT_POSTED);
        final Uri uri2 = saveEventWithStatus(AnalyticsEvent.Status.POSTING);
        final Uri uri3 = saveEventWithStatus(AnalyticsEvent.Status.POSTING_ERROR);
        final Uri uri4 = saveEventWithStatus(AnalyticsEvent.Status.POSTED);

        assertDatabaseEventCount(4);

        runPrepareDatabaseJob(true);

        assertDatabaseEventCount(3);

        assertEventHasStatus(uri1, AnalyticsEvent.Status.NOT_POSTED);
        assertEventHasStatus(uri2, AnalyticsEvent.Status.NOT_POSTED);
        assertEventHasStatus(uri3, AnalyticsEvent.Status.POSTING_ERROR);
        assertEventNotInStorage(uri4);
        assertNumberOfStartedJobs(1);
        assertTrue(getJob(0) instanceof SendAnalyticsEventsJob);
    }

    public void testEquals() {
        final PrepareDatabaseJob job1 = new PrepareDatabaseJob(true);
        final PrepareDatabaseJob job2 = new PrepareDatabaseJob(true);
        assertEquals(event1, event1);
        assertEquals(job1, job2);
    }

    public void testDoesNotEqual() {
        final PrepareDatabaseJob job1 = new PrepareDatabaseJob(true);
        final PrepareDatabaseJob job2 = new PrepareDatabaseJob(false);
        assertEquals(event1, event1);
        assertFalse(job1.equals(job2));
    }

    public void testParcelsData() {
        final PrepareDatabaseJob inputJob = new PrepareDatabaseJob(true);
        final PrepareDatabaseJob outputJob = getJobViaParcel(inputJob);
        assertNotNull(outputJob);
        assertEquals(inputJob, outputJob);
    }

    private void runPrepareDatabaseJob(boolean canSendEvents) throws InterruptedException {
        final PrepareDatabaseJob job = new PrepareDatabaseJob(canSendEvents);
        job.run(getJobParams(new JobResultListener() {

            @Override
            public void onJobComplete(int resultCode) {
                assertEquals(RESULT_SUCCESS, resultCode);
                semaphore.release();
            }
        }));

        semaphore.acquire();
    }

    private <T extends BaseJob> T getJob(int intentNumber) {
        final Intent intent = serviceStarter.getStartedIntents().get(intentNumber);
        return intent.getParcelableExtra(AnalyticsEventService.KEY_JOB);
    }

    private void assertNumberOfStartedJobs(int expected) {
        assertEquals(expected, serviceStarter.getStartedIntents().size());
    }
}
