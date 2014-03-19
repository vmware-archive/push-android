package org.omnia.pushsdk.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.test.ServiceTestCase;

import org.omnia.pushsdk.backend.BackEndMessageReceiptApiRequestProvider;
import org.omnia.pushsdk.backend.FakeBackEndMessageReceiptApiRequest;
import org.omnia.pushsdk.prefs.FakePreferencesProvider;

import java.util.concurrent.Semaphore;

public class GcmIntentServiceTest extends ServiceTestCase<GcmIntentService> {

    private static final String TEST_PACKAGE_NAME = "org.omnia.pushsdk.test";
    private static final String TEST_MESSAGE = "some fancy message";
    private static final String KEY_MESSAGE = "message";
    private static final String TEST_MESSAGE_UUID = "some-message-uuid";

    private int testResultCode = GcmIntentService.NO_RESULT;
    private Intent intent;
    private boolean didReceiveBroadcast = false;
    private TestResultReceiver testResultReceiver;
    private TestBroadcastReceiver testBroadcastReceiver;
    private Intent receivedIntent;
    private FakeBackEndMessageReceiptApiRequest backEndMessageReceiptApiRequest;

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

    // Registers for broadcasts sent by the service
    public class TestBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            receivedIntent = intent;
            didReceiveBroadcast = true;
            GcmIntentService.semaphore.release();
        }
    }

    public GcmIntentServiceTest() {
        super(GcmIntentService.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        GcmIntentService.semaphore = new Semaphore(0);
        GcmIntentService.preferencesProvider = new FakePreferencesProvider(null, null, 0, null, null, null, null, null);
        backEndMessageReceiptApiRequest = new FakeBackEndMessageReceiptApiRequest();
        GcmIntentService.backEndMessageReceiptApiRequestProvider = new BackEndMessageReceiptApiRequestProvider(backEndMessageReceiptApiRequest);
        testResultReceiver = new TestResultReceiver(null);
        testBroadcastReceiver = new TestBroadcastReceiver();
        final IntentFilter intentFilter = new IntentFilter(TEST_PACKAGE_NAME + GcmIntentService.BROADCAST_NAME_SUFFIX);
        getContext().registerReceiver(testBroadcastReceiver, intentFilter);
        intent = getServiceIntent();
    }

    public void testReceiveNullIntent() throws InterruptedException {
        startService(null);
        GcmIntentService.semaphore.acquire();
        assertEquals(GcmIntentService.NO_RESULT, testResultCode);
    }

    public void testReceiveEmptyIntent() throws InterruptedException {
        startService(intent);
        GcmIntentService.semaphore.acquire();
        assertEquals(GcmIntentService.RESULT_EMPTY_INTENT, testResultCode);
    }

    public void testEmptyPackageName() throws InterruptedException {
        intent.putExtra(KEY_MESSAGE, TEST_MESSAGE);
        startService(intent);
        GcmIntentService.semaphore.acquire();
        assertEquals(GcmIntentService.RESULT_EMPTY_PACKAGE_NAME, testResultCode);
    }

    public void testSendNotification() throws InterruptedException {
        intent.putExtra(KEY_MESSAGE, TEST_MESSAGE);
        GcmIntentService.preferencesProvider.savePackageName(TEST_PACKAGE_NAME);
        startService(intent);
        GcmIntentService.semaphore.acquire(2);

        assertEquals(GcmIntentService.RESULT_NOTIFIED_APPLICATION, testResultCode);
        assertTrue(didReceiveBroadcast);
        assertNotNull(receivedIntent);
        assertTrue(receivedIntent.hasExtra(GcmIntentService.KEY_GCM_INTENT));

        final Intent gcmIntent = receivedIntent.getParcelableExtra(GcmIntentService.KEY_GCM_INTENT);
        assertNotNull(gcmIntent);

        final Bundle extras = gcmIntent.getExtras();
        assertNotNull(extras);
        assertTrue(extras.containsKey(KEY_MESSAGE));
        assertEquals(TEST_MESSAGE, extras.getString(KEY_MESSAGE));
        assertFalse(backEndMessageReceiptApiRequest.wasRequestAttempted());
    }

    public void testSendsReceiptNotificationSuccessfully() throws InterruptedException {
        intent.putExtra(GcmIntentService.KEY_MESSAGE_UUID, TEST_MESSAGE_UUID);
        backEndMessageReceiptApiRequest.setWillBeSuccessfulRequest(true);
        GcmIntentService.preferencesProvider.savePackageName(TEST_PACKAGE_NAME);
        startService(intent);
        GcmIntentService.semaphore.acquire(2);

        assertEquals(GcmIntentService.RESULT_NOTIFIED_APPLICATION, testResultCode);
        assertTrue(didReceiveBroadcast);
        assertNotNull(receivedIntent);
        assertTrue(receivedIntent.hasExtra(GcmIntentService.KEY_GCM_INTENT));
        assertTrue(backEndMessageReceiptApiRequest.wasRequestAttempted());
        // no difference in success and fail behaviours, yet
    }

    public void testSendsReceiptNotificationFailure() throws InterruptedException {
        intent.putExtra(GcmIntentService.KEY_MESSAGE_UUID, TEST_MESSAGE_UUID);
        backEndMessageReceiptApiRequest.setWillBeSuccessfulRequest(false);
        GcmIntentService.preferencesProvider.savePackageName(TEST_PACKAGE_NAME);
        startService(intent);
        GcmIntentService.semaphore.acquire(2);

        assertEquals(GcmIntentService.RESULT_NOTIFIED_APPLICATION, testResultCode);
        assertTrue(didReceiveBroadcast);
        assertNotNull(receivedIntent);
        assertTrue(receivedIntent.hasExtra(GcmIntentService.KEY_GCM_INTENT));
        assertTrue(backEndMessageReceiptApiRequest.wasRequestAttempted());
        // no difference in success and fail behaviours, yet
    }

    private Intent getServiceIntent() {
        final Intent intent = new Intent(getContext(), GcmIntentService.class);
        intent.putExtra(GcmIntentService.KEY_RESULT_RECEIVER, testResultReceiver);
        return intent;
    }

    @Override
    protected void tearDown() throws Exception {
        GcmIntentService.semaphore = null;
        GcmIntentService.preferencesProvider = null;
        getContext().unregisterReceiver(testBroadcastReceiver);
        super.tearDown();
    }
}
