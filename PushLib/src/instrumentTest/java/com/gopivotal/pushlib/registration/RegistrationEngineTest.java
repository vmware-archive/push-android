package com.gopivotal.pushlib.registration;

import android.test.AndroidTestCase;

import com.gopivotal.pushlib.PushLibParameters;
import com.gopivotal.pushlib.backend.BackEndRegistrationApiRequestProvider;
import com.gopivotal.pushlib.backend.BackEndUnregisterDeviceApiRequestProvider;
import com.gopivotal.pushlib.backend.FakeBackEndRegistrationApiRequest;
import com.gopivotal.pushlib.backend.FakeBackEndUnregisterDeviceApiRequest;
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
    private static final String TEST_DEVICE_ALIAS = "TEST_DEVICE_ALIAS";
    private static final String TEST_SENDER_ID = "TEST_SENDER_ID";
    private static final String TEST_RELEASE_UUID_1 = "TEST_RELEASE_UUID_1";
    private static final String TEST_RELEASE_UUID_2 = "TEST_RELEASE_UUID_2";
    private static final String TEST_RELEASE_SECRET = "TEST_RELEASE_SECRET";

    private FakePreferencesProvider preferencesProvider;
    private GcmRegistrationApiRequestProvider gcmApiRequestProvider;
    private BackEndRegistrationApiRequestProvider backEndRegistrationApiRequestProvider;
    private BackEndUnregisterDeviceApiRequestProvider backEndUnregisterDeviceApiRequestProvider;
    private VersionProvider versionProvider;
    private FakeGcmProvider gcmProvider;
    private Semaphore semaphore = new Semaphore(0);

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        gcmProvider = new FakeGcmProvider(TEST_GCM_DEVICE_REGISTRATION_ID_1);
        gcmApiRequestProvider = new GcmRegistrationApiRequestProvider(new FakeGcmRegistrationApiRequest(gcmProvider));
        preferencesProvider = new FakePreferencesProvider(null, null, 0, null);
        versionProvider = new FakeVersionProvider(10);
        backEndRegistrationApiRequestProvider = new BackEndRegistrationApiRequestProvider(new FakeBackEndRegistrationApiRequest(TEST_BACK_END_DEVICE_REGISTRATION_ID_1));
        backEndUnregisterDeviceApiRequestProvider = new BackEndUnregisterDeviceApiRequestProvider(new FakeBackEndUnregisterDeviceApiRequest());
    }

    public void testNullContext() {
        try {
            new RegistrationEngine(null, gcmProvider, preferencesProvider, gcmApiRequestProvider, backEndRegistrationApiRequestProvider, backEndUnregisterDeviceApiRequestProvider, versionProvider);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullGcmProvider() {
        try {
            new RegistrationEngine(getContext(), null, preferencesProvider, gcmApiRequestProvider, backEndRegistrationApiRequestProvider, backEndUnregisterDeviceApiRequestProvider, versionProvider);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullPreferencesProvider() {
        try {
            new RegistrationEngine(getContext(), gcmProvider, null, gcmApiRequestProvider, backEndRegistrationApiRequestProvider, backEndUnregisterDeviceApiRequestProvider, versionProvider);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullGcmApiRequestProvider() {
        try {
            new RegistrationEngine(getContext(), gcmProvider, preferencesProvider, null, backEndRegistrationApiRequestProvider, backEndUnregisterDeviceApiRequestProvider, versionProvider);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullBackEndRegisterDeviceApiRequestProvider() {
        try {
            new RegistrationEngine(getContext(), gcmProvider, preferencesProvider, gcmApiRequestProvider, null, backEndUnregisterDeviceApiRequestProvider, versionProvider);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullBackEndApiUnregisterDeviceRequestProvider() {
        try {
            new RegistrationEngine(getContext(), gcmProvider, preferencesProvider, gcmApiRequestProvider, backEndRegistrationApiRequestProvider, null, versionProvider);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullVersionProvider() {
        try {
            new RegistrationEngine(getContext(), gcmProvider, preferencesProvider, gcmApiRequestProvider, backEndRegistrationApiRequestProvider, backEndUnregisterDeviceApiRequestProvider, null);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullParameters() {
        try {
            final RegistrationEngine engine = new RegistrationEngine(getContext(), gcmProvider, preferencesProvider, gcmApiRequestProvider, backEndRegistrationApiRequestProvider, backEndUnregisterDeviceApiRequestProvider, versionProvider);
            engine.registerDevice(null, getListenerForRegistration(false));
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullSenderId() {
        try {
            final RegistrationEngine engine = new RegistrationEngine(getContext(), gcmProvider, preferencesProvider, gcmApiRequestProvider, backEndRegistrationApiRequestProvider, backEndUnregisterDeviceApiRequestProvider, versionProvider);
            engine.registerDevice(new PushLibParameters(null, TEST_RELEASE_UUID_1, TEST_RELEASE_SECRET, TEST_DEVICE_ALIAS), getListenerForRegistration(false));
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullReleaseUuid() {
        try {
            final RegistrationEngine engine = new RegistrationEngine(getContext(), gcmProvider, preferencesProvider, gcmApiRequestProvider, backEndRegistrationApiRequestProvider, backEndUnregisterDeviceApiRequestProvider, versionProvider);
            engine.registerDevice(new PushLibParameters(TEST_SENDER_ID, null, TEST_RELEASE_SECRET, TEST_DEVICE_ALIAS), getListenerForRegistration(false));
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullReleaseSecret() {
        try {
            final RegistrationEngine engine = new RegistrationEngine(getContext(), gcmProvider, preferencesProvider, gcmApiRequestProvider, backEndRegistrationApiRequestProvider, backEndUnregisterDeviceApiRequestProvider, versionProvider);
            engine.registerDevice(new PushLibParameters(TEST_SENDER_ID, TEST_RELEASE_UUID_1, null, TEST_DEVICE_ALIAS), getListenerForRegistration(false));
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullDeviceAlias() throws InterruptedException {
        final RegistrationEngine engine = new RegistrationEngine(getContext(), gcmProvider, preferencesProvider, gcmApiRequestProvider, backEndRegistrationApiRequestProvider, backEndUnregisterDeviceApiRequestProvider, versionProvider);
        engine.registerDevice(new PushLibParameters(TEST_SENDER_ID, TEST_RELEASE_UUID_1, TEST_RELEASE_SECRET, null), getListenerForRegistration(true));
        semaphore.acquire();
    }

    public void testGooglePlayServicesNotAvailable() throws InterruptedException {
        gcmProvider.setIsGooglePlayServicesInstalled(false);
        final RegistrationEngine engine = new RegistrationEngine(getContext(), gcmProvider, preferencesProvider, gcmApiRequestProvider, backEndRegistrationApiRequestProvider, backEndUnregisterDeviceApiRequestProvider, versionProvider);
        final PushLibParameters parameters = new PushLibParameters(TEST_SENDER_ID, TEST_RELEASE_UUID_1, TEST_RELEASE_SECRET, TEST_DEVICE_ALIAS);
        engine.registerDevice(parameters, getListenerForRegistration(false));
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
                .setReleaseUuidFromUser(TEST_RELEASE_UUID_1)
                .setReleaseUuidInPreferences(null)
                .setFinalReleaseUuidInPrefs(TEST_RELEASE_UUID_1)
                .setShouldReleaseUuidHaveBeenSaved(true)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndRegisterHaveBeenCalled(true)
                .setShouldBackEndUnregisterDeviceHaveBeenCalled(false)
                .setShouldBackEndUnregisterDeviceBeSuccessful(false)
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
                .setReleaseUuidFromUser(TEST_RELEASE_UUID_1)
                .setReleaseUuidInPreferences(null)
                .setFinalReleaseUuidInPrefs(null)
                .setShouldReleaseUuidHaveBeenSaved(false)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndRegisterHaveBeenCalled(false)
                .setShouldBackEndUnregisterDeviceHaveBeenCalled(false)
                .setShouldBackEndUnregisterDeviceBeSuccessful(false)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run(this);
    }

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
                .setReleaseUuidFromUser(TEST_RELEASE_UUID_1)
                .setReleaseUuidInPreferences(null)
                .setFinalReleaseUuidInPrefs(null)
                .setShouldReleaseUuidHaveBeenSaved(false)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndRegisterHaveBeenCalled(true)
                .setShouldBackEndUnregisterDeviceHaveBeenCalled(false)
                .setShouldBackEndUnregisterDeviceBeSuccessful(false)
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
                .setReleaseUuidFromUser(TEST_RELEASE_UUID_1)
                .setReleaseUuidInPreferences(TEST_RELEASE_UUID_1)
                .setFinalReleaseUuidInPrefs(TEST_RELEASE_UUID_1)
                .setShouldReleaseUuidHaveBeenSaved(false)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldBackEndRegisterHaveBeenCalled(false)
                .setShouldBackEndUnregisterDeviceHaveBeenCalled(false)
                .setShouldBackEndUnregisterDeviceBeSuccessful(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run(this);
    }

    public void testWasAlreadyRegisteredWithGcmAndBackendAndTheReleaseUuidIsChanged() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setGcmDeviceRegistrationIdInPreferences(TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setBackEndDeviceRegistrationIdInPreferences(TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setAppVersionInPreferences(1)
                .setCurrentAppVersion(1)
                .setGcmDeviceRegistrationIdFromServer(TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setFinalGcmDeviceRegistrationIdInPreferences(TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setBackEndDeviceRegistrationIdFromServer(TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setFinalBackEndDeviceRegistrationIdInPrefs(TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setReleaseUuidFromUser(TEST_RELEASE_UUID_2)
                .setReleaseUuidInPreferences(TEST_RELEASE_UUID_1)
                .setFinalReleaseUuidInPrefs(TEST_RELEASE_UUID_2)
                .setShouldReleaseUuidHaveBeenSaved(true)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldBackEndRegisterHaveBeenCalled(true)
                .setShouldBackEndUnregisterDeviceHaveBeenCalled(true)
                .setShouldBackEndUnregisterDeviceBeSuccessful(true)
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
                .setReleaseUuidFromUser(TEST_RELEASE_UUID_1)
                .setReleaseUuidInPreferences(null)
                .setFinalReleaseUuidInPrefs(TEST_RELEASE_UUID_1)
                .setShouldReleaseUuidHaveBeenSaved(true)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldBackEndRegisterHaveBeenCalled(true)
                .setShouldBackEndUnregisterDeviceHaveBeenCalled(false)
                .setShouldBackEndUnregisterDeviceBeSuccessful(false)
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
                .setReleaseUuidFromUser(TEST_RELEASE_UUID_1)
                .setReleaseUuidInPreferences(null)
                .setFinalReleaseUuidInPrefs(null)
                .setShouldReleaseUuidHaveBeenSaved(false)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldBackEndRegisterHaveBeenCalled(true)
                .setShouldBackEndUnregisterDeviceHaveBeenCalled(false)
                .setShouldBackEndUnregisterDeviceBeSuccessful(false)
                .setShouldRegistrationHaveSucceeded(false);
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
                .setReleaseUuidFromUser(TEST_RELEASE_UUID_1)
                .setReleaseUuidInPreferences(TEST_RELEASE_UUID_1)
                .setFinalReleaseUuidInPrefs(TEST_RELEASE_UUID_1)
                .setShouldReleaseUuidHaveBeenSaved(false)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndRegisterHaveBeenCalled(false)
                .setShouldBackEndUnregisterDeviceHaveBeenCalled(false)
                .setShouldBackEndUnregisterDeviceBeSuccessful(false)
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
                .setReleaseUuidFromUser(TEST_RELEASE_UUID_1)
                .setReleaseUuidInPreferences(TEST_RELEASE_UUID_1)
                .setFinalReleaseUuidInPrefs(TEST_RELEASE_UUID_1)
                .setShouldReleaseUuidHaveBeenSaved(false)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndRegisterHaveBeenCalled(false)
                .setShouldBackEndUnregisterDeviceHaveBeenCalled(false)
                .setShouldBackEndUnregisterDeviceBeSuccessful(false)
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
                .setReleaseUuidFromUser(TEST_RELEASE_UUID_1)
                .setReleaseUuidInPreferences(TEST_RELEASE_UUID_1)
                .setFinalReleaseUuidInPrefs(TEST_RELEASE_UUID_1)
                .setShouldReleaseUuidHaveBeenSaved(false)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndRegisterHaveBeenCalled(false)
                .setShouldBackEndUnregisterDeviceHaveBeenCalled(false)
                .setShouldBackEndUnregisterDeviceBeSuccessful(false)
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
                .setFinalBackEndDeviceRegistrationIdInPrefs(null)
                .setReleaseUuidFromUser(TEST_RELEASE_UUID_1)
                .setReleaseUuidInPreferences(TEST_RELEASE_UUID_1)
                .setFinalReleaseUuidInPrefs(null)
                .setShouldReleaseUuidHaveBeenSaved(true)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndRegisterHaveBeenCalled(true)
                .setShouldBackEndUnregisterDeviceHaveBeenCalled(true)
                .setShouldBackEndUnregisterDeviceBeSuccessful(true)
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
                .setReleaseUuidFromUser(TEST_RELEASE_UUID_1)
                .setReleaseUuidInPreferences(TEST_RELEASE_UUID_1)
                .setFinalReleaseUuidInPrefs(TEST_RELEASE_UUID_1)
                .setShouldReleaseUuidHaveBeenSaved(true)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndRegisterHaveBeenCalled(true)
                .setShouldBackEndUnregisterDeviceHaveBeenCalled(true)
                .setShouldBackEndUnregisterDeviceBeSuccessful(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run(this);
    }

    public void testAppUpdatedAndDifferentGcmRegistrationIdWasReturnedButReleaseUuidFromPreviousRegistrationWasNotSaved() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setGcmDeviceRegistrationIdInPreferences(TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setBackEndDeviceRegistrationIdInPreferences(TEST_BACK_END_DEVICE_REGISTRATION_ID_2)
                .setAppVersionInPreferences(1)
                .setCurrentAppVersion(2)
                .setGcmDeviceRegistrationIdFromServer(TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setFinalGcmDeviceRegistrationIdInPreferences(TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setBackEndDeviceRegistrationIdFromServer(TEST_BACK_END_DEVICE_REGISTRATION_ID_2)
                .setFinalBackEndDeviceRegistrationIdInPrefs(TEST_BACK_END_DEVICE_REGISTRATION_ID_2)
                .setReleaseUuidFromUser(TEST_RELEASE_UUID_1)
                .setReleaseUuidInPreferences(null)
                .setFinalReleaseUuidInPrefs(TEST_RELEASE_UUID_1)
                .setShouldReleaseUuidHaveBeenSaved(true)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndRegisterHaveBeenCalled(true)
                .setShouldBackEndUnregisterDeviceHaveBeenCalled(false)
                .setShouldBackEndUnregisterDeviceBeSuccessful(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run(this);
    }

    public void testAppUpdatedAndDifferentGcmRegistrationIdWasReturnedButBackEndRegistrationIdWasNotSaved() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setGcmDeviceRegistrationIdInPreferences(TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setBackEndDeviceRegistrationIdInPreferences(null)
                .setAppVersionInPreferences(1)
                .setCurrentAppVersion(2)
                .setGcmDeviceRegistrationIdFromServer(TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setFinalGcmDeviceRegistrationIdInPreferences(TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setBackEndDeviceRegistrationIdFromServer(TEST_BACK_END_DEVICE_REGISTRATION_ID_2)
                .setFinalBackEndDeviceRegistrationIdInPrefs(TEST_BACK_END_DEVICE_REGISTRATION_ID_2)
                .setReleaseUuidFromUser(TEST_RELEASE_UUID_1)
                .setReleaseUuidInPreferences(TEST_RELEASE_UUID_1)
                .setFinalReleaseUuidInPrefs(TEST_RELEASE_UUID_1)
                .setShouldReleaseUuidHaveBeenSaved(true)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndRegisterHaveBeenCalled(true)
                .setShouldBackEndUnregisterDeviceHaveBeenCalled(false)
                .setShouldBackEndUnregisterDeviceBeSuccessful(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run(this);
    }

    public void testAppUpdatedAndDifferentGcmRegistrationIdWasReturnedAndUnregisterFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setGcmDeviceRegistrationIdInPreferences(TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setBackEndDeviceRegistrationIdInPreferences(TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setAppVersionInPreferences(1)
                .setCurrentAppVersion(2)
                .setGcmDeviceRegistrationIdFromServer(TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setFinalGcmDeviceRegistrationIdInPreferences(TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setBackEndDeviceRegistrationIdFromServer(TEST_BACK_END_DEVICE_REGISTRATION_ID_2)
                .setFinalBackEndDeviceRegistrationIdInPrefs(TEST_BACK_END_DEVICE_REGISTRATION_ID_2)
                .setReleaseUuidFromUser(TEST_RELEASE_UUID_1)
                .setReleaseUuidInPreferences(TEST_RELEASE_UUID_1)
                .setFinalReleaseUuidInPrefs(TEST_RELEASE_UUID_1)
                .setShouldReleaseUuidHaveBeenSaved(true)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndRegisterHaveBeenCalled(true)
                .setShouldBackEndUnregisterDeviceHaveBeenCalled(true)
                .setShouldBackEndUnregisterDeviceBeSuccessful(false)
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
                .setReleaseUuidFromUser(TEST_RELEASE_UUID_1)
                .setReleaseUuidInPreferences(TEST_RELEASE_UUID_1)
                .setFinalReleaseUuidInPrefs(TEST_RELEASE_UUID_1)
                .setShouldReleaseUuidHaveBeenSaved(true)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndRegisterHaveBeenCalled(true)
                .setShouldBackEndUnregisterDeviceHaveBeenCalled(true)
                .setShouldBackEndUnregisterDeviceBeSuccessful(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run(this);
    }

    private RegistrationListener getListenerForRegistration(final boolean isSuccessfulRegistration) {
        return new RegistrationListener() {
            @Override
            public void onRegistrationComplete() {
                assertTrue(isSuccessfulRegistration);
                semaphore.release();
            }

            @Override
            public void onRegistrationFailed(String reason) {
                assertFalse(isSuccessfulRegistration);
                semaphore.release();
            }
        };
    }
}
