/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.gcm;

import android.test.AndroidTestCase;

import java.util.concurrent.Semaphore;

public class GcmRegistrationApiRequestImplTest extends AndroidTestCase {

    private static final String TEST_SENDER_ID = "SomeSenderId";
    private static final String TEST_GCM_DEVICE_REGISTRATION_ID = "SomeGcmDeviceRegistrationId";

    private Semaphore semaphore;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        semaphore = new Semaphore(0);
    }

    public void testNullContext() {
        try {
            new GcmRegistrationApiRequestImpl(null, new FakeGcmProvider(TEST_GCM_DEVICE_REGISTRATION_ID));
            fail();
        } catch (IllegalArgumentException e) {
            // Exception expected
        }
    }

    public void testNullGcmProvider() {
        try {
            new GcmRegistrationApiRequestImpl(getContext(), null);
            fail();
        } catch (IllegalArgumentException e) {
            // Exception expected
        }
    }

    public void testNullSenderId() {
        try {
            final GcmRegistrationApiRequest request = new GcmRegistrationApiRequestImpl(getContext(), new FakeGcmProvider(TEST_GCM_DEVICE_REGISTRATION_ID));
            request.startRegistration(null, getGcmRegistrarListener(false));
            fail();
        } catch (IllegalArgumentException e) {
            // Exception expected
        }
    }

    public void testNullListener() {
        try {
            final GcmRegistrationApiRequest request = new GcmRegistrationApiRequestImpl(getContext(), new FakeGcmProvider(TEST_GCM_DEVICE_REGISTRATION_ID));
            request.startRegistration(TEST_SENDER_ID, null);
            fail();
        } catch (IllegalArgumentException e) {
            // Exception expected
        }
    }

    public void testSuccessfulInitialRegistration() throws InterruptedException {
        final FakeGcmProvider gcm = new FakeGcmProvider(TEST_GCM_DEVICE_REGISTRATION_ID);
        final GcmRegistrationApiRequestImpl registrar = new GcmRegistrationApiRequestImpl(getContext(), gcm);
        final GcmRegistrationListener listener = getGcmRegistrarListener(true);
        registrar.startRegistration(TEST_SENDER_ID, listener);
        semaphore.acquire();
        assertTrue(gcm.wasRegisterCalled());
    }

    public void testFailedRegistration() throws InterruptedException {
        final FakeGcmProvider gcm = new FakeGcmProvider(null, true);
        final GcmRegistrationApiRequestImpl registrar = new GcmRegistrationApiRequestImpl(getContext(), gcm);
        final GcmRegistrationListener listener = getGcmRegistrarListener(false);
        registrar.startRegistration(TEST_SENDER_ID, listener);
        semaphore.acquire();
        assertTrue(gcm.wasRegisterCalled());
    }

    private GcmRegistrationListener getGcmRegistrarListener(final boolean isSuccessfulRegistration) {
        return new GcmRegistrationListener() {
            @Override
            public void onGcmRegistrationComplete(String gcmDeviceRegistrationId) {
                assertTrue(isSuccessfulRegistration);
                assertEquals(TEST_GCM_DEVICE_REGISTRATION_ID, gcmDeviceRegistrationId);
                semaphore.release();
            }

            @Override
            public void onGcmRegistrationFailed(String reason) {
                assertFalse(isSuccessfulRegistration);
                semaphore.release();
            }
        };
    }
}
