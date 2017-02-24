package io.pivotal.android.push.analytics.jobs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.net.Uri;
import android.support.test.runner.AndroidJUnit4;
import android.test.MoreAsserts;
import io.pivotal.android.push.backend.analytics.FakePCFPushSendAnalyticsApiRequest;
import io.pivotal.android.push.model.analytics.AnalyticsEvent;
import java.util.ArrayList;
import java.util.List;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SendAnalyticsEventsJobTest extends JobTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        alarmProvider.enableAlarm();
    }

    @Test
    public void testWithEmptyDatabase() throws InterruptedException {

        final SendAnalyticsEventsJob job = new SendAnalyticsEventsJob();
        job.run(getJobParams(new JobResultListener() {

            @Override
            public void onJobComplete(int resultCode) {
                assertEquals(SendAnalyticsEventsJob.RESULT_NO_WORK_TO_DO, resultCode);
                semaphore.release();
            }
        }));

        semaphore.acquire();
        Assert.assertEquals(0, eventsStorage.getNumberOfEvents());
        assertFalse(sendAnalyticsApiRequest.wasRequestAttempted());
        assertFalse(alarmProvider.isAlarmEnabled());
    }

    @Test
    public void testSuccessfulSend() throws InterruptedException {

        final Uri uri1 = saveEventWithStatus(AnalyticsEvent.Status.NOT_POSTED);
        final Uri uri2 = saveEventWithStatus(AnalyticsEvent.Status.POSTING);
        final Uri uri3 = saveEventWithStatus(AnalyticsEvent.Status.POSTING_ERROR);
        final Uri uri4 = saveEventWithStatus(AnalyticsEvent.Status.POSTED);

        sendAnalyticsApiRequest.setWillBeSuccessfulRequest(true);
        sendAnalyticsApiRequest
            .setRequestHook(new FakePCFPushSendAnalyticsApiRequest.RequestHook() {

                @Override
                public void onRequestMade(FakePCFPushSendAnalyticsApiRequest request,
                    List<Uri> uris) {
                    assertEquals(2, uris.size());
                    final List<Uri> expectedUrisInRequest = new ArrayList<Uri>(2);
                    expectedUrisInRequest.add(uri1);
                    expectedUrisInRequest.add(uri3);
                    MoreAsserts.assertContentsInAnyOrder(expectedUrisInRequest, uris);

                    // Checks that events have the POSTING status while they are being posted
                    assertEventHasStatus(uri1, AnalyticsEvent.Status.POSTING);
                    assertEventHasStatus(uri1, AnalyticsEvent.Status.POSTING);
                    assertEventHasStatus(uri1, AnalyticsEvent.Status.POSTING);
                    assertEventHasStatus(uri1, AnalyticsEvent.Status.POSTED);
                }
            });

        Assert.assertEquals(4, eventsStorage.getNumberOfEvents());

        final SendAnalyticsEventsJob job = new SendAnalyticsEventsJob();
        job.run(getJobParams(new JobResultListener() {

            @Override
            public void onJobComplete(int resultCode) {
                assertEquals(RESULT_SUCCESS, resultCode);
                semaphore.release();
            }
        }));

        semaphore.acquire();
        Assert.assertEquals(2, eventsStorage.getNumberOfEvents());
        assertTrue(sendAnalyticsApiRequest.wasRequestAttempted());
        Assert.assertEquals(2, sendAnalyticsApiRequest.numberOfEventsSent());
        assertFalse(alarmProvider.isAlarmEnabled());

        assertEventHasStatus(uri2, AnalyticsEvent.Status.POSTING);
        assertEventHasStatus(uri4, AnalyticsEvent.Status.POSTED);
        assertEventNotInStorage(uri1); // posted events should be removed
        assertEventNotInStorage(uri3);
    }

    @Test
    public void testFailedSendWithOneNotPostedEventInStorage() throws InterruptedException {

        final Uri uri = saveEventWithStatus(AnalyticsEvent.Status.NOT_POSTED);

        sendAnalyticsApiRequest.setWillBeSuccessfulRequest(false);
        sendAnalyticsApiRequest
            .setRequestHook(new FakePCFPushSendAnalyticsApiRequest.RequestHook() {

                @Override
                public void onRequestMade(FakePCFPushSendAnalyticsApiRequest request,
                    List<Uri> uris) {
                    assertEquals(1, uris.size());
                    assertEquals(uri, uris.get(0));
                    assertEventHasStatus(uri, AnalyticsEvent.Status.POSTING);
                }
            });

        final SendAnalyticsEventsJob job = new SendAnalyticsEventsJob();
        job.run(getJobParams(new JobResultListener() {

            @Override
            public void onJobComplete(int resultCode) {
                assertEquals(SendAnalyticsEventsJob.RESULT_FAILED_TO_SEND_RECEIPTS, resultCode);
                semaphore.release();
            }
        }));

        semaphore.acquire();
        Assert.assertEquals(1, eventsStorage.getNumberOfEvents());
        assertTrue(sendAnalyticsApiRequest.wasRequestAttempted());
        Assert.assertEquals(0, sendAnalyticsApiRequest.numberOfEventsSent());
        assertEventHasStatus(uri, AnalyticsEvent.Status.POSTING_ERROR);
        assertTrue(alarmProvider.isAlarmEnabled());
    }

    @Test
    public void testEquals() {
        final SendAnalyticsEventsJob job1 = new SendAnalyticsEventsJob();
        final SendAnalyticsEventsJob job2 = new SendAnalyticsEventsJob();
        Assert.assertEquals(event1, event1);
        assertEquals(job1, job2);
    }

    @Test
    public void testParcelsData() {
        final SendAnalyticsEventsJob inputJob = new SendAnalyticsEventsJob();
        final SendAnalyticsEventsJob outputJob = getJobViaParcel(inputJob);
        assertNotNull(outputJob);
        assertEquals(inputJob, outputJob);
    }
}
