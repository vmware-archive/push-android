package org.omnia.pushsdk.jobs;

import android.net.Uri;

import org.omnia.pushsdk.database.EventsStorage;
import org.omnia.pushsdk.model.BaseEvent;

public class SendEventsJobTest extends JobTest {

    public void testWithEmptyDatabase() throws InterruptedException {

        final SendEventsJob job = new SendEventsJob();
        job.run(getJobParams(new JobResultListener() {

            @Override
            public void onJobComplete(int resultCode) {
                assertEquals(SendEventsJob.RESULT_NO_WORK_TO_DO, resultCode);
                semaphore.release();
            }
        }));

        semaphore.acquire();
        assertEquals(0, eventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
        assertFalse(backEndMessageReceiptApiRequest.wasRequestAttempted());
    }

    public void testWithOneNotPostedEventInStorage() throws InterruptedException {

        event1.setStatus(BaseEvent.Status.NOT_POSTED);
        final Uri uri = eventsStorage.saveEvent(event1, EventsStorage.EventType.MESSAGE_RECEIPT);
        backEndMessageReceiptApiRequest.setWillBeSuccessfulRequest(true);

        final SendEventsJob job = new SendEventsJob();
        job.run(getJobParams(new JobResultListener() {

            @Override
            public void onJobComplete(int resultCode) {
                assertEquals(JobResultListener.RESULT_SUCCESS, resultCode);
                semaphore.release();
            }
        }));

        semaphore.acquire();
        assertEquals(0, eventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
        assertTrue(backEndMessageReceiptApiRequest.wasRequestAttempted());
        assertEquals(1, backEndMessageReceiptApiRequest.numberOfMessageReceiptsSent());
        assertEquals(uri, backEndMessageReceiptApiRequest.getListOfReceivedUris().get(0));
    }

    public void testWithOnePostedEventInStorage() throws InterruptedException {

        event1.setStatus(BaseEvent.Status.POSTED);
        eventsStorage.saveEvent(event1, EventsStorage.EventType.MESSAGE_RECEIPT);

        final SendEventsJob job = new SendEventsJob();
        job.run(getJobParams(new JobResultListener() {

            @Override
            public void onJobComplete(int resultCode) {
                assertEquals(SendEventsJob.RESULT_NO_WORK_TO_DO, resultCode);
                semaphore.release();
            }
        }));

        semaphore.acquire();
        assertEquals(1, eventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
        assertFalse(backEndMessageReceiptApiRequest.wasRequestAttempted());
        assertEquals(0, backEndMessageReceiptApiRequest.numberOfMessageReceiptsSent());
    }

    public void testFailedSendWithOneNotPostedEventInStorage() throws InterruptedException {

        event1.setStatus(BaseEvent.Status.NOT_POSTED);
        final Uri uri = eventsStorage.saveEvent(event1, EventsStorage.EventType.MESSAGE_RECEIPT);
        backEndMessageReceiptApiRequest.setWillBeSuccessfulRequest(false);

        final SendEventsJob job = new SendEventsJob();
        job.run(getJobParams(new JobResultListener() {

            @Override
            public void onJobComplete(int resultCode) {
                assertEquals(SendEventsJob.RESULT_FAILED_TO_SEND_RECEIPTS, resultCode);
                semaphore.release();
            }
        }));

        semaphore.acquire();
        assertEquals(1, eventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
        assertTrue(backEndMessageReceiptApiRequest.wasRequestAttempted());
        assertEquals(0, backEndMessageReceiptApiRequest.numberOfMessageReceiptsSent());
    }

    public void testEquals() {
        final SendEventsJob job1 = new SendEventsJob();
        final SendEventsJob job2 = new SendEventsJob();
        assertEquals(event1, event1);
        assertEquals(job1, job2);
    }

    public void testParcelsData() {
        final SendEventsJob inputJob = new SendEventsJob();
        final SendEventsJob outputJob = getJobViaParcel(inputJob);
        assertNotNull(outputJob);
        assertEquals(inputJob, outputJob);
    }
}
