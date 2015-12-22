package io.pivotal.android.push.analytics.jobs;

import android.net.Uri;
import android.test.MoreAsserts;

import junit.framework.Assert;

import java.util.List;

import io.pivotal.android.push.model.analytics.AnalyticsEvent;

public class EnqueueAnalyticsEventJobTest extends JobTest {

    public void testRequiresEvent() {
        try {
            new EnqueueAnalyticsEventJob(null);
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testEnqueuesObject() throws InterruptedException {

        // Setup environment
        Assert.assertEquals(0, eventsStorage.getNumberOfEvents());
        assertFalse(alarmProvider.isAlarmEnabled());

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
        Assert.assertEquals(1, eventsStorage.getNumberOfEvents());
        final List<Uri> uris = eventsStorage.getEventUris();
        assertEquals(1, uris.size());
        final AnalyticsEvent savedEvent = eventsStorage.readEvent(uris.get(0));
        assertEquals(event1, savedEvent);

        // Ensure alarm was enabled
        assertTrue(alarmProvider.isAlarmEnabled());
    }

    public void testEnqueuesHeartbeat() throws InterruptedException {

        // Setup environment
        Assert.assertEquals(0, eventsStorage.getNumberOfEvents());
        assertFalse(alarmProvider.isAlarmEnabled());

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
        Assert.assertEquals(1, eventsStorage.getNumberOfEvents());
        final List<Uri> uris = eventsStorage.getEventUris();
        assertEquals(1, uris.size());
        final AnalyticsEvent savedEvent = eventsStorage.readEvent(uris.get(0));
        assertEquals(heartbeatEvent, savedEvent);

        // Ensure alarm was enabled
        assertTrue(alarmProvider.isAlarmEnabledImmediately());
    }

    public void testSaveFails() throws InterruptedException {

        // Setup environment
        eventsStorage.setWillSaveFail(true);
        Assert.assertEquals(0, eventsStorage.getNumberOfEvents());
        assertFalse(alarmProvider.isAlarmEnabled());

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
        Assert.assertEquals(0, eventsStorage.getNumberOfEvents());

        // Ensure alarm was not enabled
        assertFalse(alarmProvider.isAlarmEnabled());
    }

    public void testEquals() {
        final EnqueueAnalyticsEventJob job1 = new EnqueueAnalyticsEventJob(event1);
        final EnqueueAnalyticsEventJob job2 = new EnqueueAnalyticsEventJob(event1);
        assertEquals(event1, event1);
        assertEquals(job1, job2);
    }

    public void testNotEquals() {
        final EnqueueAnalyticsEventJob job1 = new EnqueueAnalyticsEventJob(event1);
        final EnqueueAnalyticsEventJob job2 = new EnqueueAnalyticsEventJob(event2);
        MoreAsserts.assertNotEqual(event1, event2);
        MoreAsserts.assertNotEqual(job1, job2);
    }

    public void testParcelsData() {
        final EnqueueAnalyticsEventJob inputJob = new EnqueueAnalyticsEventJob(event1);
        final EnqueueAnalyticsEventJob outputJob = getJobViaParcel(inputJob);
        assertNotNull(outputJob);
        assertEquals(inputJob, outputJob);
    }
}
