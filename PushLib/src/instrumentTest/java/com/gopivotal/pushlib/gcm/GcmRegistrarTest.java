package com.gopivotal.pushlib.gcm;

import android.test.AndroidTestCase;

import com.gopivotal.pushlib.prefs.FakePreferencesProvider;

import java.util.concurrent.Semaphore;

public class GcmRegistrarTest extends AndroidTestCase {

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
            new GcmRegistrar(null, TEST_SENDER_ID, new FakeGcmProvider(TEST_GCM_DEVICE_REGISTRATION_ID), new FakePreferencesProvider(TEST_GCM_DEVICE_REGISTRATION_ID));
            fail();
        } catch (IllegalArgumentException e) {
            // Exception expected
        }
    }

    public void testNullSenderId() {
        try {
            new GcmRegistrar(getContext(), null, new FakeGcmProvider(TEST_GCM_DEVICE_REGISTRATION_ID), new FakePreferencesProvider(TEST_GCM_DEVICE_REGISTRATION_ID));
            fail();
        } catch (IllegalArgumentException e) {
            // Exception expected
        }
    }

    public void testNullGcmProvider() {
        try {
            new GcmRegistrar(getContext(), TEST_SENDER_ID, null, new FakePreferencesProvider(TEST_GCM_DEVICE_REGISTRATION_ID));
            fail();
        } catch (IllegalArgumentException e) {
            // Exception expected
        }
    }

    public void testNullPreferencesProvider() {
        try {
            new GcmRegistrar(getContext(), TEST_SENDER_ID, new FakeGcmProvider(TEST_GCM_DEVICE_REGISTRATION_ID), null);
            fail();
        } catch (IllegalArgumentException e) {
            // Exception expected
        }
    }

    public void testSuccessfulInitialRegistration() throws InterruptedException {
        final FakePreferencesProvider prefs = new FakePreferencesProvider(null);
        final FakeGcmProvider gcm = new FakeGcmProvider(TEST_GCM_DEVICE_REGISTRATION_ID);
        final GcmRegistrar registrar = new GcmRegistrar(getContext(), TEST_SENDER_ID, gcm, prefs);
        final GcmRegistrarListener listener = getGcmRegistrarListener(true);
        registrar.startRegistration(listener);
        semaphore.acquire();
        assertTrue(prefs.wasSaved());
        assertEquals(TEST_GCM_DEVICE_REGISTRATION_ID, prefs.getSavedGcmDeviceRegistrationId());
        assertTrue(gcm.wasRegisterCalled());
    }

    private GcmRegistrarListener getGcmRegistrarListener(final boolean isSuccessfulRegistration) {
        return new GcmRegistrarListener() {
                @Override
                public void onRegistrationComplete(String gcmDeviceRegistrationId) {
                    assertTrue(isSuccessfulRegistration);
                    assertEquals(TEST_GCM_DEVICE_REGISTRATION_ID, gcmDeviceRegistrationId);
                    semaphore.release();
                }

                @Override
                public void onRegistrationFailed(String reason) {
                    assertFalse(isSuccessfulRegistration);
                    semaphore.release();
                }
            };
    }

    public void testFailedInitialRegistration() throws InterruptedException {
        final FakePreferencesProvider prefs = new FakePreferencesProvider(null);
        final FakeGcmProvider gcm = new FakeGcmProvider(null, true);
        final GcmRegistrar registrar = new GcmRegistrar(getContext(), TEST_SENDER_ID, gcm, prefs);
        final GcmRegistrarListener listener = getGcmRegistrarListener(false);
        registrar.startRegistration(listener);
        semaphore.acquire();
        assertFalse(prefs.wasSaved());
        assertTrue(gcm.wasRegisterCalled());
    }

    public void testUseSavedRegistration() throws InterruptedException {
        final FakePreferencesProvider prefs = new FakePreferencesProvider(TEST_GCM_DEVICE_REGISTRATION_ID);
        final FakeGcmProvider gcm = new FakeGcmProvider(null, true);
        final GcmRegistrar registrar = new GcmRegistrar(getContext(), TEST_SENDER_ID, gcm, prefs);
        final GcmRegistrarListener listener = getGcmRegistrarListener(true);
        registrar.startRegistration(listener);
        semaphore.acquire();
        assertFalse(prefs.wasSaved());
        assertFalse(gcm.wasRegisterCalled());
    }
}
