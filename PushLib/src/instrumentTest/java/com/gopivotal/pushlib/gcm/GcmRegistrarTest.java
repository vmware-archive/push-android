package com.gopivotal.pushlib.gcm;

import android.test.AndroidTestCase;

import com.gopivotal.pushlib.gcm.GcmRegistrar;

import java.util.concurrent.Semaphore;

public class GcmRegistrarTest extends AndroidTestCase {

    private static final String TEST_SENDER_ID = "SomeSenderId";
    private static final String TEST_REGISTRATION_ID = "SomeRegistrationId";

    private Semaphore semaphore;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        semaphore = new Semaphore(0);
    }

    public void testNullContext() {
        try {
            new GcmRegistrar(null, TEST_SENDER_ID, new FakeGcmProvider(TEST_REGISTRATION_ID));
            fail();
        } catch (IllegalArgumentException e) {
            // Exception expected
        }
    }

    public void testNullSenderId() {
        try {
            new GcmRegistrar(getContext(), null, new FakeGcmProvider(TEST_REGISTRATION_ID));
            fail();
        } catch (IllegalArgumentException e) {
            // Exception expected
        }
    }

    public void testNullGcmProvider() {
        try {
            new GcmRegistrar(getContext(), TEST_SENDER_ID, null);
            fail();
        } catch (IllegalArgumentException e) {
            // Exception expected
        }
    }

    public void testSuccessfulRegistration() throws InterruptedException {
        final GcmRegistrar registrar = new GcmRegistrar(getContext(), TEST_SENDER_ID, new FakeGcmProvider(TEST_REGISTRATION_ID));
        final GcmRegistrarListener listener = getGcmRegistrarListener(true);
        registrar.startRegistration(listener);
        semaphore.wait();
    }

    private GcmRegistrarListener getGcmRegistrarListener(final boolean isSuccessfulRegistration) {
        return new GcmRegistrarListener() {
                @Override
                public void onRegistrationComplete(String registrationId) {
                    assertTrue(isSuccessfulRegistration);
                    assertEquals(TEST_REGISTRATION_ID, registrationId);
                    semaphore.release();
                }

                @Override
                public void onRegistrationFailed(String reason) {
                    assertFalse(isSuccessfulRegistration);
                    semaphore.release();
                }
            };
    }

    public void testFailedRegistration() throws InterruptedException {
        final GcmRegistrar registrar = new GcmRegistrar(getContext(), TEST_SENDER_ID, new FakeGcmProvider(null, true));
        final GcmRegistrarListener listener = getGcmRegistrarListener(false);
        registrar.startRegistration(listener);
        semaphore.wait();
    }
}
