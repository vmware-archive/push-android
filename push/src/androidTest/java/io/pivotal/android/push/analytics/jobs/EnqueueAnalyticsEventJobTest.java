package io.pivotal.android.push.analytics.jobs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import android.net.Uri;
import android.support.test.runner.AndroidJUnit4;
import android.test.MoreAsserts;

import java.util.List;

import io.pivotal.android.push.model.analytics.AnalyticsEvent;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EnqueueAnalyticsEventJobTest extends JobTest {

    public void testRequiresEvent() {
        try {
            new EnqueueAnalyticsEventJob(null);
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    @Test
    public void testEnqueuesObject() throws InterruptedException {

        // Setup environment
        assertEquals(0, eventsStorage.getNumberOfEvents());

        // Run job
        final EnqueueAnalyticsEventJob job = new EnqueueAnalyticsEventJob(event1);
        job.run(getJobParams(new JobResultListener() {

            @Override
            public void onJobComplete(int resultCode) {
                assertEquals(RESULT_SUCCESS, resultCode);
                semaphore.release();
            }
        }));

        semaphore.acquire();

        // Ensure event made it into the database
        assertEquals(1, eventsStorage.getNumberOfEvents());
        final List<Uri> uris = eventsStorage.getEventUris();
        assertEquals(1, uris.size());
        final AnalyticsEvent savedEvent = eventsStorage.readEvent(uris.get(0));
        assertEquals(event1, savedEvent);
    }

    @Test
    public void testEnqueuesHeartbeat() throws InterruptedException {

        // Setup environment
        assertEquals(0, eventsStorage.getNumberOfEvents());

        // Run job
        final EnqueueAnalyticsEventJob job = new EnqueueAnalyticsEventJob(heartbeatEvent);
        job.run(getJobParams(new JobResultListener() {

            @Override
            public void onJobComplete(int resultCode) {
                assertEquals(RESULT_SUCCESS, resultCode);
                semaphore.release();
            }
        }));

        semaphore.acquire();

        // Ensure event made it into the database
        assertEquals(1, eventsStorage.getNumberOfEvents());
        final List<Uri> uris = eventsStorage.getEventUris();
        assertEquals(1, uris.size());
        final AnalyticsEvent savedEvent = eventsStorage.readEvent(uris.get(0));
        assertEquals(heartbeatEvent, savedEvent);
    }

    @Test
    public void testSaveFails() throws InterruptedException {

        // Setup environment
        eventsStorage.setWillSaveFail(true);
        assertEquals(0, eventsStorage.getNumberOfEvents());

        // Run job
        final EnqueueAnalyticsEventJob job = new EnqueueAnalyticsEventJob(event1);
        job.run(getJobParams(new JobResultListener() {

            @Override
            public void onJobComplete(int resultCode) {
                assertEquals(EnqueueAnalyticsEventJob.RESULT_COULD_NOT_SAVE_EVENT_TO_STORAGE, resultCode);
                semaphore.release();
            }
        }));

        semaphore.acquire();

        // Ensure event did not made it into the database
        assertEquals(0, eventsStorage.getNumberOfEvents());
    }

    @Test
    public void testEquals() {
        final EnqueueAnalyticsEventJob job1 = new EnqueueAnalyticsEventJob(event1);
        final EnqueueAnalyticsEventJob job2 = new EnqueueAnalyticsEventJob(event1);
        assertEquals(event1, event1);
        assertEquals(job1, job2);
    }

    @Test
    public void testNotEquals() {
        final EnqueueAnalyticsEventJob job1 = new EnqueueAnalyticsEventJob(event1);
        final EnqueueAnalyticsEventJob job2 = new EnqueueAnalyticsEventJob(event2);
        MoreAsserts.assertNotEqual(event1, event2);
        MoreAsserts.assertNotEqual(job1, job2);
    }

    @Test
    public void testParcelsData() {
        final EnqueueAnalyticsEventJob inputJob = new EnqueueAnalyticsEventJob(event1);
        final EnqueueAnalyticsEventJob outputJob = getJobViaParcel(inputJob);
        assertNotNull(outputJob);
        assertEquals(inputJob, outputJob);
    }
}
