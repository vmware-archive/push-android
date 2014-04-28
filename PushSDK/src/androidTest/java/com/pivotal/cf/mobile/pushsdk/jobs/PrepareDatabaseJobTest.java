package com.pivotal.cf.mobile.pushsdk.jobs;

import android.net.Uri;

import com.pivotal.cf.mobile.pushsdk.model.events.Event;

public class PrepareDatabaseJobTest extends JobTest {

    public void testHandlesEmptyDatabase() throws InterruptedException {

        assertDatabaseEventCount(0);
        assertFalse(alarmProvider.isAlarmEnabled());

        runPrepareDatabaseJob();

        assertDatabaseEventCount(0);
        assertFalse(alarmProvider.isAlarmEnabled());
    }

    public void testStartsAlarmForNotPostedEvent() throws InterruptedException {

        final Uri uri = saveEventWithStatus(Event.Status.NOT_POSTED);
        assertDatabaseEventCount(1);
        assertFalse(alarmProvider.isAlarmEnabled());

        runPrepareDatabaseJob();

        assertDatabaseEventCount(1);
        assertTrue(alarmProvider.isAlarmEnabled());
        assertEventHasStatus(uri, Event.Status.NOT_POSTED);
    }

    public void testStartsAlarmForPostingErrorEvent() throws InterruptedException {

        final Uri uri = saveEventWithStatus(Event.Status.POSTING_ERROR);
        assertDatabaseEventCount(1);
        assertFalse(alarmProvider.isAlarmEnabled());

        runPrepareDatabaseJob();

        assertDatabaseEventCount(1);
        assertTrue(alarmProvider.isAlarmEnabled());
        assertEventHasStatus(uri, Event.Status.POSTING_ERROR);
    }

    public void testResetsStatusAndStartsAlarmForPostingEvent() throws InterruptedException {

        final Uri uri = saveEventWithStatus(Event.Status.POSTING);
        assertDatabaseEventCount(1);
        assertFalse(alarmProvider.isAlarmEnabled());

        runPrepareDatabaseJob();

        assertDatabaseEventCount(1);
        assertTrue(alarmProvider.isAlarmEnabled());
        assertEventHasStatus(uri, Event.Status.NOT_POSTED);
    }

    public void testDeletesPostedEvent() throws InterruptedException {

        final Uri uri = saveEventWithStatus(Event.Status.POSTED);
        assertDatabaseEventCount(1);
        assertFalse(alarmProvider.isAlarmEnabled());

        runPrepareDatabaseJob();

        assertDatabaseEventCount(0);
        assertFalse(alarmProvider.isAlarmEnabled());
        assertEventNotInStorage(uri);
    }

    public void testHandlesDatabaseWithManyEvents() throws InterruptedException {

        final Uri uri1 = saveEventWithStatus(Event.Status.NOT_POSTED);
        final Uri uri2 = saveEventWithStatus(Event.Status.POSTING);
        final Uri uri3 = saveEventWithStatus(Event.Status.POSTING_ERROR);
        final Uri uri4 = saveEventWithStatus(Event.Status.POSTED);

        assertDatabaseEventCount(4);
        assertFalse(alarmProvider.isAlarmEnabled());

        runPrepareDatabaseJob();

        assertDatabaseEventCount(3);

        assertEventHasStatus(uri1, Event.Status.NOT_POSTED);
        assertEventHasStatus(uri2, Event.Status.NOT_POSTED);
        assertEventHasStatus(uri3, Event.Status.POSTING_ERROR);
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
                assertEquals(JobResultListener.RESULT_SUCCESS, resultCode);
                semaphore.release();
            }
        }));

        semaphore.acquire();
    }
}
