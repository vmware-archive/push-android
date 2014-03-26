package org.omnia.pushsdk.registration;

import android.test.AndroidTestCase;

import org.omnia.pushsdk.backend.BackEndUnregisterDeviceApiRequestProvider;
import org.omnia.pushsdk.backend.FakeBackEndUnregisterDeviceApiRequest;
import org.omnia.pushsdk.gcm.FakeGcmProvider;
import org.omnia.pushsdk.gcm.FakeGcmUnregistrationApiRequest;
import org.omnia.pushsdk.gcm.GcmUnregistrationApiRequestProvider;
import org.omnia.pushsdk.prefs.FakePreferencesProvider;
import org.omnia.pushsdk.sample.util.PushLibLogger;

import java.util.concurrent.Semaphore;

public class UnregistrationEngineTest extends AndroidTestCase {

    private static final String TEST_GCM_DEVICE_REGISTRATION_ID_1 = "TEST_GCM_DEVICE_REGISTRATION_ID_1";
    private static final String TEST_BACK_END_DEVICE_REGISTRATION_ID_1 = "TEST_BACK_END_DEVICE_REGISTRATION_ID_1";
    private FakeGcmProvider gcmProvider;
    private GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider;
    private FakePreferencesProvider preferencesProvider;
    private BackEndUnregisterDeviceApiRequestProvider backEndUnregisterDeviceApiRequestProvider;
    private Semaphore semaphore = new Semaphore(0);

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        gcmProvider = new FakeGcmProvider(TEST_GCM_DEVICE_REGISTRATION_ID_1);
        gcmUnregistrationApiRequestProvider = new GcmUnregistrationApiRequestProvider(new FakeGcmUnregistrationApiRequest(gcmProvider));
        preferencesProvider = new FakePreferencesProvider(null, null, 0, null, null, null, null, null);
        backEndUnregisterDeviceApiRequestProvider = new BackEndUnregisterDeviceApiRequestProvider(new FakeBackEndUnregisterDeviceApiRequest());
    }

    public void testNullContext() {
        try {
            new UnregistrationEngine(null, gcmProvider, preferencesProvider, gcmUnregistrationApiRequestProvider, backEndUnregisterDeviceApiRequestProvider);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullGcmProvider() {
        try {
            new UnregistrationEngine(getContext(), null, preferencesProvider, gcmUnregistrationApiRequestProvider, backEndUnregisterDeviceApiRequestProvider);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullPreferencesProvider() {
        try {
            new UnregistrationEngine(getContext(), gcmProvider, null, gcmUnregistrationApiRequestProvider, backEndUnregisterDeviceApiRequestProvider);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullGcmUnregistrationApiRequestProvider() {
        try {
            new UnregistrationEngine(getContext(), gcmProvider, preferencesProvider, null, backEndUnregisterDeviceApiRequestProvider);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullBackEndApiUnregisterDeviceRequestProvider() {
        try {
            new UnregistrationEngine(getContext(), gcmProvider, preferencesProvider, gcmUnregistrationApiRequestProvider, null);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testGooglePlayServicesNotAvailable() throws InterruptedException {
        gcmProvider.setIsGooglePlayServicesInstalled(false);
        final UnregistrationEngine engine = new UnregistrationEngine(getContext(), gcmProvider, preferencesProvider, gcmUnregistrationApiRequestProvider, backEndUnregisterDeviceApiRequestProvider);
        engine.unregisterDevice(getListenerForUnregistration(false));
        semaphore.acquire();
    }

    public void testSuccessfulUnregistration() {
        UnregistrationEngineTestParameters testParams = new UnregistrationEngineTestParameters(getContext())
                .setShouldGcmDeviceUnregistrationBeSuccessful(true)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, null)
                .setShouldUnregistrationHaveSucceeded(true);
        testParams.run(this);
    }

    public void testSuccessfulUnregistrationFromGcmOnly() {
        UnregistrationEngineTestParameters testParams = new UnregistrationEngineTestParameters(getContext())
                .setShouldGcmDeviceUnregistrationBeSuccessful(true)
                .setupBackEndDeviceRegistrationId(null, null)
                .setShouldUnregistrationHaveSucceeded(true);
        testParams.run(this);
    }

    public void testUnregistrationWhenGcmUnregistrationFails() {
        UnregistrationEngineTestParameters testParams = new UnregistrationEngineTestParameters(getContext())
                .setShouldGcmDeviceUnregistrationBeSuccessful(false)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, null)
                .setShouldUnregistrationHaveSucceeded(true);
        testParams.run(this);
    }

    public void testUnregistrationWhenGcmUnregistrationFailsAndBackEndUnregistrationFails() {
        UnregistrationEngineTestParameters testParams = new UnregistrationEngineTestParameters(getContext())
                .setShouldGcmDeviceUnregistrationBeSuccessful(false)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setShouldUnregistrationHaveSucceeded(false);
        testParams.run(this);
    }

    public void testUnregistrationWhenGcmUnregistrationSucceedsAndBackEndUnregistrationFails() {
        UnregistrationEngineTestParameters testParams = new UnregistrationEngineTestParameters(getContext())
                .setShouldGcmDeviceUnregistrationBeSuccessful(true)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setShouldUnregistrationHaveSucceeded(false);
        testParams.run(this);
    }

    private UnregistrationListener getListenerForUnregistration(final boolean isSuccessfulUnregistration) {
        return new UnregistrationListener() {

            @Override
            public void onUnregistrationComplete() {
                assertTrue(isSuccessfulUnregistration);
                semaphore.release();
            }

            @Override
            public void onUnregistrationFailed(String reason) {
                if (isSuccessfulUnregistration) {
                    PushLibLogger.e("Test failed due to error:" + reason);
                }
                assertFalse(isSuccessfulUnregistration);
                semaphore.release();
            }
        };
    }
}
