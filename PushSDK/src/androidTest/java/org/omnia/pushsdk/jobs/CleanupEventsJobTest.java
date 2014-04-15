package org.omnia.pushsdk.jobs;

import android.net.Uri;

import org.omnia.pushsdk.database.EventsStorage;
import org.omnia.pushsdk.model.BaseEvent;
import org.omnia.pushsdk.model.MessageReceiptEvent;

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

        assertEquals(4, eventsStorage.getNumberOfEvents(EventsStorage.EventType.MESSAGE_RECEIPT));

        assertStatusForEvent(uri1, BaseEvent.Status.NOT_POSTED);
        assertStatusForEvent(uri2, BaseEvent.Status.NOT_POSTED);
        assertStatusForEvent(uri3, BaseEvent.Status.NOT_POSTED);
        assertStatusForEvent(uri4, BaseEvent.Status.POSTED);
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

    private Uri saveEventWithStatus(int status) {
        event1.setStatus(status);
        return eventsStorage.saveEvent(event1, EventsStorage.EventType.MESSAGE_RECEIPT);
    }

    private void assertStatusForEvent(Uri uri, int expectedStatus) {
        final MessageReceiptEvent event = (MessageReceiptEvent) eventsStorage.readEvent(uri);
        assertEquals(expectedStatus, event.getStatus());
    }
}
