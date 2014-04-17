package org.omnia.pushsdk.jobs;

import android.net.Uri;

import org.omnia.pushsdk.database.EventsStorage;
import org.omnia.pushsdk.model.BaseEvent;

public class PrepareDatabaseJobTest extends JobTest {

    public void testHandlesDatabaseWithManyEvents() throws InterruptedException {

        final Uri uri1 = saveEventWithStatus(BaseEvent.Status.NOT_POSTED);
        final Uri uri2 = saveEventWithStatus(BaseEvent.Status.POSTING);
        final Uri uri3 = saveEventWithStatus(BaseEvent.Status.POSTING_ERROR);
        final Uri uri4 = saveEventWithStatus(BaseEvent.Status.POSTED);

        assertEquals(4, eventsStorage.getNumberOfEvents(EventsStorage.EventType.MESSAGE_RECEIPT));

        final PrepareDatabaseJob job = new PrepareDatabaseJob();
        job.run(getJobParams(new JobResultListener() {

            @Override
            public void onJobComplete(int resultCode) {
                assertEquals(JobResultListener.RESULT_SUCCESS, resultCode);
                semaphore.release();
            }
        }));

        semaphore.acquire();

        assertEquals(3, eventsStorage.getNumberOfEvents(EventsStorage.EventType.MESSAGE_RECEIPT));

        assertEventHasStatus(uri1, BaseEvent.Status.NOT_POSTED);
        assertEventHasStatus(uri2, BaseEvent.Status.NOT_POSTED);
        assertEventHasStatus(uri3, BaseEvent.Status.POSTING_ERROR);
        assertEventNotInStorage(uri4);
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
}
