package org.omnia.pushsdk.jobs;

import android.net.Uri;
import android.test.MoreAsserts;

import org.omnia.pushsdk.backend.FakeBackEndMessageReceiptApiRequest;
import org.omnia.pushsdk.database.EventsStorage;
import org.omnia.pushsdk.model.BaseEvent;

import java.util.ArrayList;
import java.util.List;

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

    public void testSuccessfulSend() throws InterruptedException {

        final Uri uri1 = saveEventWithStatus(BaseEvent.Status.NOT_POSTED);
        final Uri uri2 = saveEventWithStatus(BaseEvent.Status.POSTING);
        final Uri uri3 = saveEventWithStatus(BaseEvent.Status.POSTING_ERROR);
        final Uri uri4 = saveEventWithStatus(BaseEvent.Status.POSTED);

        backEndMessageReceiptApiRequest.setWillBeSuccessfulRequest(true);
        backEndMessageReceiptApiRequest.setRequestHook(new FakeBackEndMessageReceiptApiRequest.RequestHook() {

            @Override
            public void onRequestMade(FakeBackEndMessageReceiptApiRequest request, List<Uri> uris) {
                assertEquals(2, uris.size());
                final List<Uri> expectedUrisInRequest = new ArrayList<Uri>(2);
                expectedUrisInRequest.add(uri1);
                expectedUrisInRequest.add(uri3);
                MoreAsserts.assertContentsInAnyOrder(expectedUrisInRequest, uris);

                // Checks that events have the POSTING status while they are being posted
                assertEventHasStatus(uri1, BaseEvent.Status.POSTING);
                assertEventHasStatus(uri1, BaseEvent.Status.POSTING);
                assertEventHasStatus(uri1, BaseEvent.Status.POSTING);
                assertEventHasStatus(uri1, BaseEvent.Status.POSTED);
            }
        });

        assertEquals(4, eventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));

        final SendEventsJob job = new SendEventsJob();
        job.run(getJobParams(new JobResultListener() {

            @Override
            public void onJobComplete(int resultCode) {
                assertEquals(JobResultListener.RESULT_SUCCESS, resultCode);
                semaphore.release();
            }
        }));

        semaphore.acquire();
        assertEquals(2, eventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
        assertTrue(backEndMessageReceiptApiRequest.wasRequestAttempted());
        assertEquals(2, backEndMessageReceiptApiRequest.numberOfMessageReceiptsSent());

        assertEventHasStatus(uri2, BaseEvent.Status.POSTING);
        assertEventHasStatus(uri4, BaseEvent.Status.POSTED);
        assertEventNotInStorage(uri1); // posted events should be removed
        assertEventNotInStorage(uri3);
    }

    public void testFailedSendWithOneNotPostedEventInStorage() throws InterruptedException {

        final Uri uri = saveEventWithStatus(BaseEvent.Status.NOT_POSTED);

        backEndMessageReceiptApiRequest.setWillBeSuccessfulRequest(false);
        backEndMessageReceiptApiRequest.setRequestHook(new FakeBackEndMessageReceiptApiRequest.RequestHook() {

            @Override
            public void onRequestMade(FakeBackEndMessageReceiptApiRequest request, List<Uri> uris) {
                assertEquals(1, uris.size());
                assertEquals(uri, uris.get(0));
                assertEventHasStatus(uri, BaseEvent.Status.POSTING);
            }
        });

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
        assertEventHasStatus(uri, BaseEvent.Status.POSTING_ERROR);
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
