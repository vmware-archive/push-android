package com.gopivotal.pushlib.registration;

import android.test.AndroidTestCase;

import com.gopivotal.pushlib.gcm.FakeGcmProvider;
import com.gopivotal.pushlib.gcm.FakeGcmRegistrationApiRequest;
import com.gopivotal.pushlib.gcm.GcmRegistrationApiRequestProvider;
import com.gopivotal.pushlib.prefs.FakePreferencesProvider;
import com.xtreme.commons.testing.DelayedLoop;

public class RegistrationEngineTest extends AndroidTestCase {

    private static final String TEST_GCM_DEVICE_REGISTRATION_ID = "TEST_GCM_DEVICE_REGISTRATION_ID";
    private static final String TEST_SENDER_ID = "TEST_SENDER_ID";
    private static final long TEN_SECOND_TIMEOUT = 10000L;
    private static final long NO_DELAY = 0L;
    private static final long ONE_SECOND_DELAY = 1000L;

    private GcmRegistrationApiRequestProvider gcmApiRequestProvider;
    private DelayedLoop delayedLoop;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        FakeGcmProvider gcmProvider = new FakeGcmProvider(TEST_GCM_DEVICE_REGISTRATION_ID);
        gcmApiRequestProvider = new GcmRegistrationApiRequestProvider(new FakeGcmRegistrationApiRequest(true, TEST_GCM_DEVICE_REGISTRATION_ID, gcmProvider));
        delayedLoop = new DelayedLoop(TEN_SECOND_TIMEOUT);
    }

    public void testNullContext() {
        try {
            new RegistrationEngine(null, new FakeGcmProvider(null), new FakePreferencesProvider(null, null, 0), gcmApiRequestProvider);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullGcmProvider() {
        try {
            new RegistrationEngine(getContext(), null, new FakePreferencesProvider(null, null, 0), gcmApiRequestProvider);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullPreferencesProvider() {
        try {
            new RegistrationEngine(getContext(), new FakeGcmProvider(null), null, gcmApiRequestProvider);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullGcmApiRequestProvider() {
        try {
            new RegistrationEngine(getContext(), new FakeGcmProvider(null), new FakePreferencesProvider(null, null, 0), null);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testSuccessfulInitialRegistration() {
        final FakeGcmProvider gcmProvider = new FakeGcmProvider(null);
        final FakePreferencesProvider prefsProvider = new FakePreferencesProvider(null, null, 0);
        final FakeGcmRegistrationApiRequest gcmRequest = new FakeGcmRegistrationApiRequest(true, TEST_GCM_DEVICE_REGISTRATION_ID, gcmProvider);
        final GcmRegistrationApiRequestProvider gcmRequestProvider = new GcmRegistrationApiRequestProvider(gcmRequest);
        final RegistrationEngine engine = new RegistrationEngine(getContext(), gcmProvider, prefsProvider, gcmRequestProvider);
        engine.registerDevice(TEST_SENDER_ID, new RegistrationListener() {

            @Override
            public void onRegistrationComplete() {
                delayedLoop.flagSuccess();
            }

            @Override
            public void onRegistrationFailed(String reason) {
                fail();
            }
        });
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
        assertTrue(gcmProvider.wasRegisterCalled());
        assertTrue(prefsProvider.wasAppVersionSaved());
        assertTrue(prefsProvider.wasGcmDeviceRegistrationIdSaved());
        assertEquals(TEST_GCM_DEVICE_REGISTRATION_ID, prefsProvider.loadGcmDeviceRegistrationId());
    }
}
