package com.gopivotal.pushlib.registration;

import android.test.AndroidTestCase;

import com.gopivotal.pushlib.gcm.FakeGcmProvider;
import com.gopivotal.pushlib.gcm.FakeGcmRegistrationApiRequest;
import com.gopivotal.pushlib.gcm.GcmRegistrationApiRequestProvider;
import com.gopivotal.pushlib.prefs.FakePreferencesProvider;
import com.gopivotal.pushlib.version.FakeVersionProvider;
import com.gopivotal.pushlib.version.VersionProvider;

public class RegistrationEngineTest extends AndroidTestCase {

    private static final String TEST_GCM_DEVICE_REGISTRATION_ID_1 = "TEST_GCM_DEVICE_REGISTRATION_ID_1";
    private static final String TEST_GCM_DEVICE_REGISTRATION_ID_2 = "TEST_GCM_DEVICE_REGISTRATION_ID_2";
    private FakePreferencesProvider preferencesProvider;
    private GcmRegistrationApiRequestProvider gcmApiRequestProvider;
    private VersionProvider versionProvider;
    private FakeGcmProvider gcmProvider;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        gcmProvider = new FakeGcmProvider(TEST_GCM_DEVICE_REGISTRATION_ID_1);
        gcmApiRequestProvider = new GcmRegistrationApiRequestProvider(new FakeGcmRegistrationApiRequest(gcmProvider));
        preferencesProvider = new FakePreferencesProvider(null, null, 0);
        versionProvider = new FakeVersionProvider(10);
    }

    public void testNullContext() {
        try {
            new RegistrationEngine(null, gcmProvider, preferencesProvider, gcmApiRequestProvider, versionProvider);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullGcmProvider() {
        try {
            new RegistrationEngine(getContext(), null, preferencesProvider, gcmApiRequestProvider, versionProvider);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullPreferencesProvider() {
        try {
            new RegistrationEngine(getContext(), gcmProvider, null, gcmApiRequestProvider, versionProvider);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullGcmApiRequestProvider() {
        try {
            new RegistrationEngine(getContext(), gcmProvider, preferencesProvider, null, versionProvider);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullVersionProvider() {
        try {
            new RegistrationEngine(getContext(), gcmProvider, preferencesProvider, gcmApiRequestProvider, null);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testSuccessfulInitialRegistration() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setGcmDeviceRegistrationIdInPreferences(null)
                .setBackEndDeviceRegistrationIdInPreferences(null)
                .setAppVersionInPreferences(1)
                .setCurrentAppVersion(1)
                .setGcmDeviceRegistrationIdFromServer(TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setFinalGcmDeviceRegistrationIdInPreferences(TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(true);
         testParams.run(this);
    }

    public void testWasAlreadyRegisteredWithGcm() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setGcmDeviceRegistrationIdInPreferences(TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setBackEndDeviceRegistrationIdInPreferences(null)
                .setAppVersionInPreferences(1)
                .setCurrentAppVersion(1)
                .setGcmDeviceRegistrationIdFromServer(TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setFinalGcmDeviceRegistrationIdInPreferences(TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run(this);
        // TODO - ensure that backend registration was not called
    }

    public void testAppUpdatedAndSameRegistrationIdWasReturned() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setGcmDeviceRegistrationIdInPreferences(TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setBackEndDeviceRegistrationIdInPreferences(null)
                .setAppVersionInPreferences(1)
                .setCurrentAppVersion(2)
                .setGcmDeviceRegistrationIdFromServer(TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setFinalGcmDeviceRegistrationIdInPreferences(TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run(this);
        // TODO - ensure that backend registration was not called
    }

    public void testAppUpdatedAndDifferentRegistrationIdWasReturned() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setGcmDeviceRegistrationIdInPreferences(TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setBackEndDeviceRegistrationIdInPreferences(null)
                .setAppVersionInPreferences(1)
                .setCurrentAppVersion(2)
                .setGcmDeviceRegistrationIdFromServer(TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setFinalGcmDeviceRegistrationIdInPreferences(TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run(this);
        // TODO - ensure that backend registration still occurs
    }
}
