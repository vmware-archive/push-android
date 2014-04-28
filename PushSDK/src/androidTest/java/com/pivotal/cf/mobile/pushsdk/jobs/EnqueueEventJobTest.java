package com.pivotal.cf.mobile.pushsdk.jobs;

import android.net.Uri;
import android.test.MoreAsserts;

import com.pivotal.cf.mobile.pushsdk.model.events.Event;

import java.util.List;

public class EnqueueEventJobTest extends JobTest {

    public void testRequiresEvent() {
        try {
            new EnqueueEventJob(null);
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }


    public void testEnqueuesObject() throws InterruptedException {

        // Setup environment
        assertEquals(0, eventsStorage.getNumberOfEvents());
        assertFalse(alarmProvider.isAlarmEnabled());

        // Run job
        final EnqueueEventJob job = new EnqueueEventJob(event1);
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
        final Event savedEvent = eventsStorage.readEvent(uris.get(0));
        assertEquals(event1, savedEvent);

        // Ensure alarm was enabled
        assertTrue(alarmProvider.isAlarmEnabled());
    }

    public void testSaveFails() throws InterruptedException {

        // Setup environment
        eventsStorage.setWillSaveFail(true);
        assertEquals(0, eventsStorage.getNumberOfEvents());
        assertFalse(alarmProvider.isAlarmEnabled());

        // Run job
        final EnqueueEventJob job = new EnqueueEventJob(event1);
        job.run(getJobParams(new JobResultListener() {

            @Override
            public void onJobComplete(int resultCode) {
                assertEquals(EnqueueEventJob.RESULT_COULD_NOT_SAVE_EVENT_TO_STORAGE, resultCode);
                semaphore.release();
            }
        }));

        semaphore.acquire();

        // Ensure event did not made it into the database
        assertEquals(0, eventsStorage.getNumberOfEvents());

        // Ensure alarm was not enabled
        assertFalse(alarmProvider.isAlarmEnabled());
    }

    public void testEquals() {
        final EnqueueEventJob job1 = new EnqueueEventJob(event1);
        final EnqueueEventJob job2 = new EnqueueEventJob(event1);
        assertEquals(event1, event1);
        assertEquals(job1, job2);
    }

    public void testNotEquals() {
        final EnqueueEventJob job1 = new EnqueueEventJob(event1);
        final EnqueueEventJob job2 = new EnqueueEventJob(event2);
        MoreAsserts.assertNotEqual(event1, event2);
        MoreAsserts.assertNotEqual(job1, job2);
    }

    // TODO - test more event types after we define some.

    public void testParcelsData() {
        final EnqueueEventJob inputJob = new EnqueueEventJob(event1);
        final EnqueueEventJob outputJob = getJobViaParcel(inputJob);
        assertNotNull(outputJob);
        assertEquals(inputJob, outputJob);
    }
}
