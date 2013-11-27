package com.gopivotal.pushlib.gcm;

import android.test.AndroidTestCase;

import java.util.concurrent.Semaphore;

public class GcmUnregistrationApiRequestImplTest extends AndroidTestCase {

    private static final String TEST_GCM_DEVICE_REGISTRATION_ID = "SomeGcmDeviceUnregistrationId";

    private Semaphore semaphore;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        semaphore = new Semaphore(0);
    }

    public void testNullContext() {
        try {
            new GcmUnregistrationApiRequestImpl(null, new FakeGcmProvider(TEST_GCM_DEVICE_REGISTRATION_ID));
            fail();
        } catch (IllegalArgumentException e) {
            // Exception expected
        }
    }

    public void testNullGcmProvider() {
        try {
            new GcmUnregistrationApiRequestImpl(getContext(), null);
            fail();
        } catch (IllegalArgumentException e) {
            // Exception expected
        }
    }

    public void testNullListener() {
        try {
            final GcmUnregistrationApiRequest request = new GcmUnregistrationApiRequestImpl(getContext(), new FakeGcmProvider(TEST_GCM_DEVICE_REGISTRATION_ID));
            request.startUnregistration(null);
            fail();
        } catch (IllegalArgumentException e) {
            // Exception expected
        }
    }

    public void testSuccessfulInitialUnregistration() throws InterruptedException {
        final FakeGcmProvider gcm = new FakeGcmProvider(TEST_GCM_DEVICE_REGISTRATION_ID, true, false);
        final GcmUnregistrationApiRequestImpl request = new GcmUnregistrationApiRequestImpl(getContext(), gcm);
        final GcmUnregistrationListener listener = getGcmUnregistrationListener(true);
        request.startUnregistration(listener);
        semaphore.acquire();
        assertTrue(gcm.wasUnregisterCalled());
    }

    public void testFailedUnregistration() throws InterruptedException {
        final FakeGcmProvider gcm = new FakeGcmProvider(null, true, true);
        final GcmUnregistrationApiRequestImpl registrar = new GcmUnregistrationApiRequestImpl(getContext(), gcm);
        final GcmUnregistrationListener listener = getGcmUnregistrationListener(false);
        registrar.startUnregistration(listener);
        semaphore.acquire();
        assertTrue(gcm.wasUnregisterCalled());
    }

    private GcmUnregistrationListener getGcmUnregistrationListener(final boolean isSuccessfulUnregistration) {
        return new GcmUnregistrationListener() {
            @Override
            public void onGcmUnregistrationComplete() {
                assertTrue(isSuccessfulUnregistration);
                semaphore.release();
            }

            @Override
            public void onGcmUnregistrationFailed(String reason) {
                assertFalse(isSuccessfulUnregistration);
                semaphore.release();
            }
        };
    }
}
