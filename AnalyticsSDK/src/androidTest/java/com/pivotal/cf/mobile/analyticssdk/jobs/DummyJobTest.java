package com.pivotal.cf.mobile.analyticssdk.jobs;

import android.test.MoreAsserts;

public class DummyJobTest extends JobTest {

    private static final int DUMMY_RESULT_CODE = 876;

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

    public void testIsParcelable1() {
        final DummyJob inputJob = new DummyJob();
        inputJob.setResultCode(DUMMY_RESULT_CODE);
        inputJob.setWillInterrupt(true);
        final DummyJob outputJob = getJobViaParcel(inputJob);
        assertEquals(DUMMY_RESULT_CODE, outputJob.getResultCode());
        assertEquals(true, outputJob.willInterrupt());
    }

    public void testIsParcelable2() {
        final DummyJob inputJob = new DummyJob();
        inputJob.setWillInterrupt(false);
        final DummyJob outputJob = getJobViaParcel(inputJob);
        assertEquals(false, outputJob.willInterrupt());
    }

    public void testIsEquals1() {
        final DummyJob job1 = new DummyJob();
        job1.setResultCode(DUMMY_RESULT_CODE);
        final DummyJob job2 = new DummyJob();
        MoreAsserts.assertNotEqual(job1, job2);
        job2.setResultCode(DUMMY_RESULT_CODE);
        assertEquals(job1, job2);
    }

    public void testIsEquals2() {
        final DummyJob job1 = new DummyJob();
        job1.setWillInterrupt(true);
        final DummyJob job2 = new DummyJob();
        MoreAsserts.assertNotEqual(job1, job2);
        job2.setWillInterrupt(true);
        assertEquals(job1, job2);
    }

    public void testIsEquals3() {
        final DummyJob job = new DummyJob();
        MoreAsserts.assertNotEqual(job, null);
        MoreAsserts.assertNotEqual(null, job);
        MoreAsserts.assertNotEqual(job, "NOT A JOB");
    }
}
