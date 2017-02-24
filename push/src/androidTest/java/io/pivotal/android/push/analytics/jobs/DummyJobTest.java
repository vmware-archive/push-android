package io.pivotal.android.push.analytics.jobs;

import static android.test.MoreAsserts.assertNotEqual;
import static org.junit.Assert.assertEquals;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class DummyJobTest extends JobTest {

    private static final int DUMMY_RESULT_CODE = 876;

    @Test
    public void testSetResultCode() throws InterruptedException {
        final DummyJob job = new DummyJob();
        job.setResultCode(DUMMY_RESULT_CODE);
        job.run(getJobParams(new JobResultListener() {

            @Override
            public void onJobComplete(int resultCode) {
                assertEquals(DUMMY_RESULT_CODE, resultCode);
                semaphore.release();
            }
        }));
        semaphore.acquire();
    }

    @Test
    public void testIsParcelable1() {
        final DummyJob inputJob = new DummyJob();
        inputJob.setResultCode(DUMMY_RESULT_CODE);
        inputJob.setWillInterrupt(true);
        final DummyJob outputJob = getJobViaParcel(inputJob);
        assertEquals(DUMMY_RESULT_CODE, outputJob.getResultCode());
        assertEquals(true, outputJob.willInterrupt());
    }

    @Test
    public void testIsParcelable2() {
        final DummyJob inputJob = new DummyJob();
        inputJob.setWillInterrupt(false);
        final DummyJob outputJob = getJobViaParcel(inputJob);
        assertEquals(false, outputJob.willInterrupt());
    }

    @Test
    public void testIsEquals1() {
        final DummyJob job1 = new DummyJob();
        job1.setResultCode(DUMMY_RESULT_CODE);
        final DummyJob job2 = new DummyJob();
        assertNotEqual(job1, job2);
        job2.setResultCode(DUMMY_RESULT_CODE);
        assertEquals(job1, job2);
    }

    @Test
    public void testIsEquals2() {
        final DummyJob job1 = new DummyJob();
        job1.setWillInterrupt(true);
        final DummyJob job2 = new DummyJob();
        assertNotEqual(job1, job2);
        job2.setWillInterrupt(true);
        assertEquals(job1, job2);
    }

    @Test
    public void testIsEquals3() {
        final DummyJob job = new DummyJob();
        assertNotEqual(job, null);
        assertNotEqual(null, job);
        assertNotEqual(job, "NOT A JOB");
    }
}
