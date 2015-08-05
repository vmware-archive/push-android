package io.pivotal.android.push.analytics.jobs;

import android.net.Uri;

import io.pivotal.android.push.model.analytics.AnalyticsEvent;

public class PrepareDatabaseJobTest extends JobTest {

    public void testHandlesEmptyDatabase() throws InterruptedException {

        assertDatabaseEventCount(0);
        assertFalse(alarmProvider.isAlarmEnabled());

        runPrepareDatabaseJob();

        assertDatabaseEventCount(0);
        assertFalse(alarmProvider.isAlarmEnabled());
    }

    public void testStartsAlarmForNotPostedEvent() throws InterruptedException {

        final Uri uri = saveEventWithStatus(AnalyticsEvent.Status.NOT_POSTED);
        assertDatabaseEventCount(1);
        assertFalse(alarmProvider.isAlarmEnabled());

        runPrepareDatabaseJob();

        assertDatabaseEventCount(1);
        assertTrue(alarmProvider.isAlarmEnabled());
        assertEventHasStatus(uri, AnalyticsEvent.Status.NOT_POSTED);
    }

    public void testStartsAlarmForPostingErrorEvent() throws InterruptedException {

        final Uri uri = saveEventWithStatus(AnalyticsEvent.Status.POSTING_ERROR);
        assertDatabaseEventCount(1);
        assertFalse(alarmProvider.isAlarmEnabled());

        runPrepareDatabaseJob();

        assertDatabaseEventCount(1);
        assertTrue(alarmProvider.isAlarmEnabled());
        assertEventHasStatus(uri, AnalyticsEvent.Status.POSTING_ERROR);
    }

    public void testResetsStatusAndStartsAlarmForPostingEvent() throws InterruptedException {

        final Uri uri = saveEventWithStatus(AnalyticsEvent.Status.POSTING);
        assertDatabaseEventCount(1);
        assertFalse(alarmProvider.isAlarmEnabled());

        runPrepareDatabaseJob();

        assertDatabaseEventCount(1);
        assertTrue(alarmProvider.isAlarmEnabled());
        assertEventHasStatus(uri, AnalyticsEvent.Status.NOT_POSTED);
    }

    public void testDeletesPostedEvent() throws InterruptedException {

        final Uri uri = saveEventWithStatus(AnalyticsEvent.Status.POSTED);
        assertDatabaseEventCount(1);
        assertFalse(alarmProvider.isAlarmEnabled());

        runPrepareDatabaseJob();

        assertDatabaseEventCount(0);
        assertFalse(alarmProvider.isAlarmEnabled());
        assertEventNotInStorage(uri);
    }

    public void testHandlesDatabaseWithManyEvents() throws InterruptedException {

        final Uri uri1 = saveEventWithStatus(AnalyticsEvent.Status.NOT_POSTED);
        final Uri uri2 = saveEventWithStatus(AnalyticsEvent.Status.POSTING);
        final Uri uri3 = saveEventWithStatus(AnalyticsEvent.Status.POSTING_ERROR);
        final Uri uri4 = saveEventWithStatus(AnalyticsEvent.Status.POSTED);

        assertDatabaseEventCount(4);
        assertFalse(alarmProvider.isAlarmEnabled());

        runPrepareDatabaseJob();

        assertDatabaseEventCount(3);

        assertEventHasStatus(uri1, AnalyticsEvent.Status.NOT_POSTED);
        assertEventHasStatus(uri2, AnalyticsEvent.Status.NOT_POSTED);
        assertEventHasStatus(uri3, AnalyticsEvent.Status.POSTING_ERROR);
        assertEventNotInStorage(uri4);

        assertTrue(alarmProvider.isAlarmEnabled());
    }

    public void testEquals() {
        final PrepareDatabaseJob job1 = new PrepareDatabaseJob();
        final PrepareDatabaseJob job2 = new PrepareDatabaseJob();
        assertEquals(event1, event1);
        assertEquals(job1, job2);
    }

    public void testParcelsData() {
        final PrepareDatabaseJob inputJob = new PrepareDatabaseJob();
        final PrepareDatabaseJob outputJob = getJobViaParcel(inputJob);
        assertNotNull(outputJob);
        assertEquals(inputJob, outputJob);
    }

    private void runPrepareDatabaseJob() throws InterruptedException {
        final PrepareDatabaseJob job = new PrepareDatabaseJob();
        job.run(getJobParams(new JobResultListener() {

            @Override
            public void onJobComplete(int resultCode) {
                assertEquals(RESULT_SUCCESS, resultCode);
                semaphore.release();
            }
        }));

        semaphore.acquire();
    }
}
