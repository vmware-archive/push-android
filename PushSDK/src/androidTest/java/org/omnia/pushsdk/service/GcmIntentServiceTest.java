package org.omnia.pushsdk.service;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.test.ServiceTestCase;

import com.xtreme.commons.Logger;

import java.util.concurrent.Semaphore;

public class GcmIntentServiceTest extends ServiceTestCase<GcmIntentService> {

    private int testResultCode = GcmIntentService.NO_RESULT;

    // Captures result codes from the service itself
    public class TestResultReceiver extends ResultReceiver {

        public TestResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            testResultCode = resultCode;
        }
    }

    private TestResultReceiver testResultReceiver;

    public GcmIntentServiceTest() {
        super(GcmIntentService.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        GcmIntentService.semaphore = new Semaphore(0);
        testResultReceiver = new TestResultReceiver(null);
    }

    public void testReceiveNullIntent() throws InterruptedException {
        startService(null);
        GcmIntentService.semaphore.acquire();
        assertEquals(GcmIntentService.NO_RESULT, testResultCode);
    }

    public void testReceiveEmptyIntent() throws InterruptedException {
        final Intent intent = getServiceIntent();
        startService(intent);
        GcmIntentService.semaphore.acquire();
        assertEquals(GcmIntentService.RESULT_EMPTY_INTENT, testResultCode);
    }

    private Intent getServiceIntent() {
        final Intent intent = new Intent(getContext(), GcmIntentService.class);
        intent.putExtra(GcmIntentService.KEY_RESULT_RECEIVER, testResultReceiver);
        return intent;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        GcmIntentService.semaphore = null;
    }
}
