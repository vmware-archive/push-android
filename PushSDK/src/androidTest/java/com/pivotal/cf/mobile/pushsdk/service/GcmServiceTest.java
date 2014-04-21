package com.pivotal.cf.mobile.pushsdk.service;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.test.ServiceTestCase;

import com.pivotal.cf.mobile.pushsdk.prefs.FakePreferencesProvider;
import com.pivotal.cf.mobile.pushsdk.util.FakeServiceStarter;

import java.util.concurrent.Semaphore;

public class GcmServiceTest extends ServiceTestCase<GcmService> {

    private static final String TEST_PACKAGE_NAME = "com.pivotal.cf.mobile.pushsdk.test";
    private static final String TEST_MESSAGE_UUID = "some-message-uuid";
    private static final String TEST_VARIANT_UUID = "some-variant-uuid";
    private static final String TEST_MESSAGE = "some fancy message";
    private static final String KEY_MESSAGE = "message";

    private int testResultCode = GcmService.NO_RESULT;
    private Intent intent;
    private boolean didReceiveBroadcast = false;
    private TestResultReceiver testResultReceiver;
    private TestBroadcastReceiver testBroadcastReceiver;
    private Intent receivedIntent;
    private FakePreferencesProvider preferencesProvider;
    private FakeServiceStarter serviceStarter;

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
            GcmService.semaphore.release();
        }
    }

    public GcmServiceTest() {
        super(GcmService.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        preferencesProvider = new FakePreferencesProvider(null, null, 0, null, TEST_VARIANT_UUID, null, null, null);
        serviceStarter = new FakeServiceStarter();
        GcmService.semaphore = new Semaphore(0);
        GcmService.preferencesProvider = preferencesProvider;
        GcmService.serviceStarter = serviceStarter;
        testResultReceiver = new TestResultReceiver(null);
        testBroadcastReceiver = new TestBroadcastReceiver();
        final IntentFilter intentFilter = new IntentFilter(TEST_PACKAGE_NAME + GcmService.BROADCAST_NAME_SUFFIX);
        getContext().registerReceiver(testBroadcastReceiver, intentFilter);
        intent = getServiceIntent();
    }

    @Override
    protected void tearDown() throws Exception {
        GcmService.semaphore = null;
        GcmService.preferencesProvider = null;
        GcmService.serviceStarter = null;
        getContext().unregisterReceiver(testBroadcastReceiver);
        super.tearDown();
    }

    public void testReceiveNullIntent() throws InterruptedException {
        startService(null);
        GcmService.semaphore.acquire();
        assertEquals(GcmService.NO_RESULT, testResultCode);
        assertFalse(serviceStarter.wasStarted());
    }

    public void testReceiveEmptyIntent() throws InterruptedException {
        startService(intent);
        GcmService.semaphore.acquire();
        assertEquals(GcmService.RESULT_EMPTY_INTENT, testResultCode);
        assertFalse(serviceStarter.wasStarted());
    }

    public void testEmptyPackageName() throws InterruptedException {
        intent.putExtra(KEY_MESSAGE, TEST_MESSAGE);
        startService(intent);
        GcmService.semaphore.acquire();
        assertEquals(GcmService.RESULT_EMPTY_PACKAGE_NAME, testResultCode);
        assertFalse(serviceStarter.wasStarted());
    }

    public void testSendNotification() throws InterruptedException {
        serviceStarter.setReturnedComponentName(new ComponentName(getContext(), EventService.class));
        intent.putExtra(KEY_MESSAGE, TEST_MESSAGE);
        GcmService.preferencesProvider.setPackageName(TEST_PACKAGE_NAME);
        startService(intent);
        GcmService.semaphore.acquire(2);

        assertEquals(GcmService.RESULT_NOTIFIED_APPLICATION, testResultCode);
        assertTrue(didReceiveBroadcast);
        assertNotNull(receivedIntent);
        assertTrue(receivedIntent.hasExtra(GcmService.KEY_GCM_INTENT));

        final Intent gcmIntent = receivedIntent.getParcelableExtra(GcmService.KEY_GCM_INTENT);
        assertNotNull(gcmIntent);

        final Bundle extras = gcmIntent.getExtras();
        assertNotNull(extras);
        assertTrue(extras.containsKey(KEY_MESSAGE));
        assertEquals(TEST_MESSAGE, extras.getString(KEY_MESSAGE));

        assertTrue(serviceStarter.wasStarted());
        assertEquals(EventService.class.getCanonicalName(), serviceStarter.getStartedIntent().getComponent().getClassName());
    }

    public void testQueuesReceiptNotificationWithMessageUuid() throws InterruptedException {
        serviceStarter.setReturnedComponentName(new ComponentName(getContext(), EventService.class));
        intent.putExtra(GcmService.KEY_MESSAGE_UUID, TEST_MESSAGE_UUID);
        GcmService.preferencesProvider.setPackageName(TEST_PACKAGE_NAME);
        startService(intent);
        GcmService.semaphore.acquire(2);

        assertEquals(GcmService.RESULT_NOTIFIED_APPLICATION, testResultCode);
        assertTrue(didReceiveBroadcast);
        assertNotNull(receivedIntent);
        assertTrue(receivedIntent.hasExtra(GcmService.KEY_GCM_INTENT));

        assertTrue(serviceStarter.wasStarted());
        assertEquals(EventService.class.getCanonicalName(), serviceStarter.getStartedIntent().getComponent().getClassName());
    }

    private Intent getServiceIntent() {
        final Intent intent = new Intent(getContext(), GcmService.class);
        intent.putExtra(GcmService.KEY_RESULT_RECEIVER, testResultReceiver);
        return intent;
    }
}
