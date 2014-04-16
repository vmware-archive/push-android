package org.omnia.pushsdk.jobs;

import android.net.Uri;

import org.omnia.pushsdk.database.EventsStorage;
import org.omnia.pushsdk.model.BaseEvent;

public class CleanupEventsJobTest extends JobTest {

    public void testHandlesDatabaseWithManyEvents() throws InterruptedException {

        final Uri uri1 = saveEventWithStatus(BaseEvent.Status.NOT_POSTED);
        final Uri uri2 = saveEventWithStatus(BaseEvent.Status.POSTING);
        final Uri uri3 = saveEventWithStatus(BaseEvent.Status.POSTING_ERROR);
        final Uri uri4 = saveEventWithStatus(BaseEvent.Status.POSTED);

        assertEquals(4, eventsStorage.getNumberOfEvents(EventsStorage.EventType.MESSAGE_RECEIPT));

        final CleanupEventsJob job = new CleanupEventsJob();
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
        final CleanupEventsJob job1 = new CleanupEventsJob();
        final CleanupEventsJob job2 = new CleanupEventsJob();
        assertEquals(event1, event1);
        assertEquals(job1, job2);
    }

    public void testParcelsData() {
        final CleanupEventsJob inputJob = new CleanupEventsJob();
        final CleanupEventsJob outputJob = getJobViaParcel(inputJob);
        assertNotNull(outputJob);
        assertEquals(inputJob, outputJob);
    }
}
