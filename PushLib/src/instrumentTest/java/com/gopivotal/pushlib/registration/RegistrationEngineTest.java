package com.gopivotal.pushlib.registration;

import android.test.AndroidTestCase;

import com.gopivotal.pushlib.PushLibParameters;
import com.gopivotal.pushlib.backend.BackEndRegistrationApiRequestProvider;
import com.gopivotal.pushlib.backend.FakeBackEndRegistrationApiRequest;
import com.gopivotal.pushlib.gcm.FakeGcmProvider;
import com.gopivotal.pushlib.gcm.FakeGcmRegistrationApiRequest;
import com.gopivotal.pushlib.gcm.GcmRegistrationApiRequestProvider;
import com.gopivotal.pushlib.prefs.FakePreferencesProvider;
import com.gopivotal.pushlib.version.FakeVersionProvider;
import com.gopivotal.pushlib.version.VersionProvider;

import java.util.concurrent.Semaphore;

public class RegistrationEngineTest extends AndroidTestCase {

    private static final String TEST_GCM_DEVICE_REGISTRATION_ID_1 = "TEST_GCM_DEVICE_REGISTRATION_ID_1";
    private static final String TEST_GCM_DEVICE_REGISTRATION_ID_2 = "TEST_GCM_DEVICE_REGISTRATION_ID_2";
    private static final String TEST_BACK_END_DEVICE_REGISTRATION_ID_1 = "TEST_BACK_END_DEVICE_REGISTRATION_ID_1";
    private static final String TEST_BACK_END_DEVICE_REGISTRATION_ID_2 = "TEST_BACK_END_DEVICE_REGISTRATION_ID_2";
    private static final String TEST_SENDER_ID = "TEST_SENDER_ID";
    private static final String TEST_RELEASE_UUID = "TEST_RELEASE_UUID";
    private static final String TEST_RELEASE_SECRET = "TEST_RELEASE_SECRET";

    private FakePreferencesProvider preferencesProvider;
    private GcmRegistrationApiRequestProvider gcmApiRequestProvider;
    private BackEndRegistrationApiRequestProvider backEndRegistrationApiRequestProvider;
    private VersionProvider versionProvider;
    private FakeGcmProvider gcmProvider;
    private Semaphore semaphore = new Semaphore(0);

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        gcmProvider = new FakeGcmProvider(TEST_GCM_DEVICE_REGISTRATION_ID_1);
        gcmApiRequestProvider = new GcmRegistrationApiRequestProvider(new FakeGcmRegistrationApiRequest(gcmProvider));
        preferencesProvider = new FakePreferencesProvider(null, null, 0);
        versionProvider = new FakeVersionProvider(10);
        backEndRegistrationApiRequestProvider = new BackEndRegistrationApiRequestProvider(new FakeBackEndRegistrationApiRequest(TEST_BACK_END_DEVICE_REGISTRATION_ID_1));
    }

    public void testNullContext() {
        try {
            new RegistrationEngine(null, gcmProvider, preferencesProvider, gcmApiRequestProvider, backEndRegistrationApiRequestProvider, versionProvider);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullGcmProvider() {
        try {
            new RegistrationEngine(getContext(), null, preferencesProvider, gcmApiRequestProvider, backEndRegistrationApiRequestProvider, versionProvider);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullPreferencesProvider() {
        try {
            new RegistrationEngine(getContext(), gcmProvider, null, gcmApiRequestProvider, backEndRegistrationApiRequestProvider, versionProvider);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullGcmApiRequestProvider() {
        try {
            new RegistrationEngine(getContext(), gcmProvider, preferencesProvider, null, backEndRegistrationApiRequestProvider, versionProvider);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullBackEndApiRequestProvider() {
        try {
            new RegistrationEngine(getContext(), gcmProvider, preferencesProvider, gcmApiRequestProvider, null, versionProvider);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullVersionProvider() {
        try {
            new RegistrationEngine(getContext(), gcmProvider, preferencesProvider, gcmApiRequestProvider, backEndRegistrationApiRequestProvider, null);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullParameters() {
        try {
            final RegistrationEngine engine = new RegistrationEngine(getContext(), gcmProvider, preferencesProvider, gcmApiRequestProvider, backEndRegistrationApiRequestProvider, versionProvider);
            engine.registerDevice(null, getListenerForFailedRegistration());
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullSenderId() {
        try {
            final RegistrationEngine engine = new RegistrationEngine(getContext(), gcmProvider, preferencesProvider, gcmApiRequestProvider, backEndRegistrationApiRequestProvider, versionProvider);
            engine.registerDevice(new PushLibParameters(null, TEST_RELEASE_UUID, TEST_RELEASE_SECRET), getListenerForFailedRegistration());
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullReleaseUuid() {
        try {
            final RegistrationEngine engine = new RegistrationEngine(getContext(), gcmProvider, preferencesProvider, gcmApiRequestProvider, backEndRegistrationApiRequestProvider, versionProvider);
            engine.registerDevice(new PushLibParameters(TEST_SENDER_ID, null, TEST_RELEASE_SECRET), getListenerForFailedRegistration());
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullReleaseSecret() {
        try {
            final RegistrationEngine engine = new RegistrationEngine(getContext(), gcmProvider, preferencesProvider, gcmApiRequestProvider, backEndRegistrationApiRequestProvider, versionProvider);
            engine.registerDevice(new PushLibParameters(TEST_SENDER_ID, TEST_RELEASE_UUID, null), getListenerForFailedRegistration());
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testGooglePlayServicesNotAvailable() throws InterruptedException {
        gcmProvider.setIsGooglePlayServicesInstalled(false);
        final RegistrationEngine engine = new RegistrationEngine(getContext(), gcmProvider, preferencesProvider, gcmApiRequestProvider, backEndRegistrationApiRequestProvider, versionProvider);
        final PushLibParameters parameters = new PushLibParameters(TEST_SENDER_ID, TEST_RELEASE_UUID, TEST_RELEASE_SECRET);
        engine.registerDevice(parameters, getListenerForFailedRegistration());
        semaphore.acquire();
    }

    public void testSuccessfulInitialRegistration() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setGcmDeviceRegistrationIdInPreferences(null)
                .setBackEndDeviceRegistrationIdInPreferences(null)
                .setAppVersionInPreferences(1)
                .setCurrentAppVersion(1)
                .setGcmDeviceRegistrationIdFromServer(TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setFinalGcmDeviceRegistrationIdInPreferences(TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setBackEndDeviceRegistrationIdFromServer(TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setFinalBackEndDeviceRegistrationIdInPrefs(TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndRegisterHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(true);
         testParams.run(this);
    }

    public void testFailedInitialGcmRegistration() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setGcmDeviceRegistrationIdInPreferences(null)
                .setBackEndDeviceRegistrationIdInPreferences(null)
                .setAppVersionInPreferences(1)
                .setCurrentAppVersion(1)
                .setGcmDeviceRegistrationIdFromServer(null)
                .setFinalGcmDeviceRegistrationIdInPreferences(null)
                .setBackEndDeviceRegistrationIdFromServer(null)
                .setFinalBackEndDeviceRegistrationIdInPrefs(null)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndRegisterHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run(this);
    }

    // TODO - write test where there is a valid registered GCM id but the back-end is not registered

    public void testInitialGcmRegistrationPassedButInitialBackEndRegistrationFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setGcmDeviceRegistrationIdInPreferences(null)
                .setBackEndDeviceRegistrationIdInPreferences(null)
                .setAppVersionInPreferences(1)
                .setCurrentAppVersion(1)
                .setGcmDeviceRegistrationIdFromServer(TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setFinalGcmDeviceRegistrationIdInPreferences(TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setBackEndDeviceRegistrationIdFromServer(null)
                .setFinalBackEndDeviceRegistrationIdInPrefs(null)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndRegisterHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run(this);
    }

    public void testWasAlreadyRegisteredWithGcmAndBackend() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setGcmDeviceRegistrationIdInPreferences(TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setBackEndDeviceRegistrationIdInPreferences(TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setAppVersionInPreferences(1)
                .setCurrentAppVersion(1)
                .setGcmDeviceRegistrationIdFromServer(TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setFinalGcmDeviceRegistrationIdInPreferences(TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setBackEndDeviceRegistrationIdFromServer(TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setFinalBackEndDeviceRegistrationIdInPrefs(TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldBackEndRegisterHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run(this);
    }

    public void testWasAlreadyRegisteredWithGcmButNotBackEndAndBackEndRegistrationSucceeds() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setGcmDeviceRegistrationIdInPreferences(TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setBackEndDeviceRegistrationIdInPreferences(null)
                .setAppVersionInPreferences(1)
                .setCurrentAppVersion(1)
                .setGcmDeviceRegistrationIdFromServer(null)
                .setFinalGcmDeviceRegistrationIdInPreferences(TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setBackEndDeviceRegistrationIdFromServer(TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setFinalBackEndDeviceRegistrationIdInPrefs(TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldBackEndRegisterHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run(this);
    }

    public void testWasAlreadyRegisteredWithGcmButNotBackEndAndBackEndRegistrationFails() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setGcmDeviceRegistrationIdInPreferences(TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setBackEndDeviceRegistrationIdInPreferences(null)
                .setAppVersionInPreferences(1)
                .setCurrentAppVersion(1)
                .setGcmDeviceRegistrationIdFromServer(null)
                .setFinalGcmDeviceRegistrationIdInPreferences(TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setBackEndDeviceRegistrationIdFromServer(null)
                .setFinalBackEndDeviceRegistrationIdInPrefs(null)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldBackEndRegisterHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run(this);
    }

    public void testWasAlreadyRegisteredWithGcmButNotRegisteredWithTheBackEnd() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setGcmDeviceRegistrationIdInPreferences(TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setBackEndDeviceRegistrationIdInPreferences(null)
                .setAppVersionInPreferences(1)
                .setCurrentAppVersion(1)
                .setGcmDeviceRegistrationIdFromServer(TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setFinalGcmDeviceRegistrationIdInPreferences(TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setBackEndDeviceRegistrationIdFromServer(TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setFinalBackEndDeviceRegistrationIdInPrefs(TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldBackEndRegisterHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run(this);
    }

    public void testAppUpdatedAndSameGcmRegistrationIdWasReturned() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setGcmDeviceRegistrationIdInPreferences(TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setBackEndDeviceRegistrationIdInPreferences(TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setAppVersionInPreferences(1)
                .setCurrentAppVersion(2)
                .setGcmDeviceRegistrationIdFromServer(TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setFinalGcmDeviceRegistrationIdInPreferences(TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setBackEndDeviceRegistrationIdFromServer(TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setFinalBackEndDeviceRegistrationIdInPrefs(TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndRegisterHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run(this);
    }

    public void testAppUpdatedToLesserVersionAndSameGcmRegistrationIdWasReturned() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setGcmDeviceRegistrationIdInPreferences(TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setBackEndDeviceRegistrationIdInPreferences(TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setAppVersionInPreferences(2)
                .setCurrentAppVersion(1)
                .setGcmDeviceRegistrationIdFromServer(TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setFinalGcmDeviceRegistrationIdInPreferences(TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setBackEndDeviceRegistrationIdFromServer(TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setFinalBackEndDeviceRegistrationIdInPrefs(TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndRegisterHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run(this);
    }

    public void testAppUpdatedAndGcmReregistrationFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setGcmDeviceRegistrationIdInPreferences(TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setBackEndDeviceRegistrationIdInPreferences(TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setAppVersionInPreferences(1)
                .setCurrentAppVersion(2)
                .setGcmDeviceRegistrationIdFromServer(null)
                .setFinalGcmDeviceRegistrationIdInPreferences(TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setBackEndDeviceRegistrationIdFromServer(null)
                .setFinalBackEndDeviceRegistrationIdInPrefs(TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndRegisterHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run(this);
    }


    public void testAppUpdatedAndGcmReregistrationReturnedNewIdButBackEndReregistrationFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setGcmDeviceRegistrationIdInPreferences(TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setBackEndDeviceRegistrationIdInPreferences(TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setAppVersionInPreferences(1)
                .setCurrentAppVersion(2)
                .setGcmDeviceRegistrationIdFromServer(TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setFinalGcmDeviceRegistrationIdInPreferences(TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setBackEndDeviceRegistrationIdFromServer(null)
                .setFinalBackEndDeviceRegistrationIdInPrefs(TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndRegisterHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run(this);
    }

    public void testAppUpdatedAndDifferentGcmRegistrationIdWasReturned() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setGcmDeviceRegistrationIdInPreferences(TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setBackEndDeviceRegistrationIdInPreferences(TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setAppVersionInPreferences(1)
                .setCurrentAppVersion(2)
                .setGcmDeviceRegistrationIdFromServer(TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setFinalGcmDeviceRegistrationIdInPreferences(TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setBackEndDeviceRegistrationIdFromServer(TEST_BACK_END_DEVICE_REGISTRATION_ID_2)
                .setFinalBackEndDeviceRegistrationIdInPrefs(TEST_BACK_END_DEVICE_REGISTRATION_ID_2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndRegisterHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run(this);
    }

    public void testAppUpdatedToLesserVersionAndDifferentGcmRegistrationIdWasReturned() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setGcmDeviceRegistrationIdInPreferences(TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setBackEndDeviceRegistrationIdInPreferences(TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setAppVersionInPreferences(2)
                .setCurrentAppVersion(1)
                .setGcmDeviceRegistrationIdFromServer(TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setFinalGcmDeviceRegistrationIdInPreferences(TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setBackEndDeviceRegistrationIdFromServer(TEST_BACK_END_DEVICE_REGISTRATION_ID_2)
                .setFinalBackEndDeviceRegistrationIdInPrefs(TEST_BACK_END_DEVICE_REGISTRATION_ID_2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndRegisterHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run(this);
    }

    private RegistrationListener getListenerForFailedRegistration() {
        return new RegistrationListener() {
            @Override
            public void onRegistrationComplete() {
                fail("Registration should have failed");
                semaphore.release();
            }

            @Override
            public void onRegistrationFailed(String reason) {
                // success
                semaphore.release();
            }
        };
    }
}
