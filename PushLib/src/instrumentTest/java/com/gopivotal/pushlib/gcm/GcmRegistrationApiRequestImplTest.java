package com.gopivotal.pushlib.gcm;

import android.test.AndroidTestCase;

import com.gopivotal.pushlib.prefs.FakePreferencesProvider;

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
            new GcmRegistrationApiRequestImpl(null, TEST_SENDER_ID, new FakeGcmProvider(TEST_GCM_DEVICE_REGISTRATION_ID), new FakePreferencesProvider(TEST_GCM_DEVICE_REGISTRATION_ID, null));
            fail();
        } catch (IllegalArgumentException e) {
            // Exception expected
        }
    }

    public void testNullSenderId() {
        try {
            new GcmRegistrationApiRequestImpl(getContext(), null, new FakeGcmProvider(TEST_GCM_DEVICE_REGISTRATION_ID), new FakePreferencesProvider(TEST_GCM_DEVICE_REGISTRATION_ID, null));
            fail();
        } catch (IllegalArgumentException e) {
            // Exception expected
        }
    }

    public void testNullGcmProvider() {
        try {
            new GcmRegistrationApiRequestImpl(getContext(), TEST_SENDER_ID, null, new FakePreferencesProvider(TEST_GCM_DEVICE_REGISTRATION_ID, null));
            fail();
        } catch (IllegalArgumentException e) {
            // Exception expected
        }
    }

    public void testNullPreferencesProvider() {
        try {
            new GcmRegistrationApiRequestImpl(getContext(), TEST_SENDER_ID, new FakeGcmProvider(TEST_GCM_DEVICE_REGISTRATION_ID), null);
            fail();
        } catch (IllegalArgumentException e) {
            // Exception expected
        }
    }

    public void testSuccessfulInitialRegistration() throws InterruptedException {
        final FakePreferencesProvider prefs = new FakePreferencesProvider(null, null);
        final FakeGcmProvider gcm = new FakeGcmProvider(TEST_GCM_DEVICE_REGISTRATION_ID);
        final GcmRegistrationApiRequestImpl registrar = new GcmRegistrationApiRequestImpl(getContext(), TEST_SENDER_ID, gcm, prefs);
        final GcmRegistrationListener listener = getGcmRegistrarListener(true);
        registrar.startRegistration(listener);
        semaphore.acquire();
        assertTrue(prefs.wasGcmDeviceRegistrationIdSaved());
        assertEquals(TEST_GCM_DEVICE_REGISTRATION_ID, prefs.getSavedGcmDeviceRegistrationId());
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

    public void testFailedInitialRegistration() throws InterruptedException {
        final FakePreferencesProvider prefs = new FakePreferencesProvider(null, null);
        final FakeGcmProvider gcm = new FakeGcmProvider(null, true);
        final GcmRegistrationApiRequestImpl registrar = new GcmRegistrationApiRequestImpl(getContext(), TEST_SENDER_ID, gcm, prefs);
        final GcmRegistrationListener listener = getGcmRegistrarListener(false);
        registrar.startRegistration(listener);
        semaphore.acquire();
        assertFalse(prefs.wasGcmDeviceRegistrationIdSaved());
        assertTrue(gcm.wasRegisterCalled());
    }

    public void testUseSavedRegistration() throws InterruptedException {
        final FakePreferencesProvider prefs = new FakePreferencesProvider(TEST_GCM_DEVICE_REGISTRATION_ID, null);
        final FakeGcmProvider gcm = new FakeGcmProvider(null, true);
        final GcmRegistrationApiRequestImpl registrar = new GcmRegistrationApiRequestImpl(getContext(), TEST_SENDER_ID, gcm, prefs);
        final GcmRegistrationListener listener = getGcmRegistrarListener(true);
        registrar.startRegistration(listener);
        semaphore.acquire();
        assertFalse(prefs.wasGcmDeviceRegistrationIdSaved());
        assertFalse(gcm.wasRegisterCalled());
    }
}
