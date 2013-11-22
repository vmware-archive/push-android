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
import com.xtreme.commons.Logger;

import java.util.concurrent.Semaphore;

public class RegistrationEngineTest extends AndroidTestCase {

    private static final String TEST_GCM_DEVICE_REGISTRATION_ID_1 = "TEST_GCM_DEVICE_REGISTRATION_ID_1";
    private static final String TEST_GCM_DEVICE_REGISTRATION_ID_2 = "TEST_GCM_DEVICE_REGISTRATION_ID_2";
    private static final String TEST_BACK_END_DEVICE_REGISTRATION_ID_1 = "TEST_BACK_END_DEVICE_REGISTRATION_ID_1";
    private static final String TEST_BACK_END_DEVICE_REGISTRATION_ID_2 = "TEST_BACK_END_DEVICE_REGISTRATION_ID_2";
    private static final String TEST_DEVICE_ALIAS_1 = "TEST_DEVICE_ALIAS_1";
    private static final String TEST_DEVICE_ALIAS_2 = "TEST_DEVICE_ALIAS_2";
    private static final String TEST_SENDER_ID = "TEST_SENDER_ID";
    private static final String TEST_RELEASE_UUID_1 = "TEST_RELEASE_UUID_1";
    private static final String TEST_RELEASE_UUID_2 = "TEST_RELEASE_UUID_2";
    private static final String TEST_RELEASE_SECRET_1 = "TEST_RELEASE_SECRET_1";
    private static final String TEST_RELEASE_SECRET_2 = "TEST_RELEASE_SECRET_2";

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
        preferencesProvider = new FakePreferencesProvider(null, null, 0, null, null, null);
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
            engine.registerDevice(new PushLibParameters(null, TEST_RELEASE_UUID_1, TEST_RELEASE_SECRET_1, TEST_DEVICE_ALIAS_1), getListenerForRegistration(false));
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullReleaseUuid() {
        try {
            final RegistrationEngine engine = new RegistrationEngine(getContext(), gcmProvider, preferencesProvider, gcmApiRequestProvider, backEndRegistrationApiRequestProvider, backEndUnregisterDeviceApiRequestProvider, versionProvider);
            engine.registerDevice(new PushLibParameters(TEST_SENDER_ID, null, TEST_RELEASE_SECRET_1, TEST_DEVICE_ALIAS_1), getListenerForRegistration(false));
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullReleaseSecret() {
        try {
            final RegistrationEngine engine = new RegistrationEngine(getContext(), gcmProvider, preferencesProvider, gcmApiRequestProvider, backEndRegistrationApiRequestProvider, backEndUnregisterDeviceApiRequestProvider, versionProvider);
            engine.registerDevice(new PushLibParameters(TEST_SENDER_ID, TEST_RELEASE_UUID_1, null, TEST_DEVICE_ALIAS_1), getListenerForRegistration(false));
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullDeviceAlias() throws InterruptedException {
        final RegistrationEngine engine = new RegistrationEngine(getContext(), gcmProvider, preferencesProvider, gcmApiRequestProvider, backEndRegistrationApiRequestProvider, backEndUnregisterDeviceApiRequestProvider, versionProvider);
        engine.registerDevice(new PushLibParameters(TEST_SENDER_ID, TEST_RELEASE_UUID_1, TEST_RELEASE_SECRET_1, null), getListenerForRegistration(true));
        semaphore.acquire();
    }

    public void testGooglePlayServicesNotAvailable() throws InterruptedException {
        gcmProvider.setIsGooglePlayServicesInstalled(false);
        final RegistrationEngine engine = new RegistrationEngine(getContext(), gcmProvider, preferencesProvider, gcmApiRequestProvider, backEndRegistrationApiRequestProvider, backEndUnregisterDeviceApiRequestProvider, versionProvider);
        final PushLibParameters parameters = new PushLibParameters(TEST_SENDER_ID, TEST_RELEASE_UUID_1, TEST_RELEASE_SECRET_1, TEST_DEVICE_ALIAS_1);
        engine.registerDevice(parameters, getListenerForRegistration(false));
        semaphore.acquire();
    }

    public void testSuccessfulInitialRegistration() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(null, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(null, TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupReleaseUuid(null, TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, true)
                .setupReleaseSecret(null, TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, true)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupAppVersion(1, 1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(true)
                .setShouldBackEndRegisterHaveBeenCalled(true)
                .setShouldBackEndUnregisterDeviceHaveBeenCalled(false)
                .setShouldBackEndUnregisterDeviceBeSuccessful(false)
                .setShouldRegistrationHaveSucceeded(true);
         testParams.run(this);
    }

    public void testFailedInitialGcmRegistration() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(null, null, null)
                .setupBackEndDeviceRegistrationId(null, null, null)
                .setupReleaseUuid(null, TEST_RELEASE_UUID_1, null, false)
                .setupReleaseSecret(null, TEST_RELEASE_SECRET_1, null, false)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, null, false)
                .setupAppVersion(1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(false)
                .setShouldBackEndRegisterHaveBeenCalled(false)
                .setShouldBackEndUnregisterDeviceHaveBeenCalled(false)
                .setShouldBackEndUnregisterDeviceBeSuccessful(false)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run(this);
    }

    public void testInitialGcmRegistrationPassedButInitialBackEndRegistrationFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(null, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(null, null, null)
                .setupReleaseUuid(null, TEST_RELEASE_UUID_1, null, false)
                .setupReleaseSecret(null, TEST_RELEASE_SECRET_1, null, false)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, null, false)
                .setupAppVersion(1, 1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(false)
                .setShouldBackEndRegisterHaveBeenCalled(true)
                .setShouldBackEndUnregisterDeviceHaveBeenCalled(false)
                .setShouldBackEndUnregisterDeviceBeSuccessful(false)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run(this);
    }

    public void testWasAlreadyRegisteredWithGcmAndBackend() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupReleaseUuid(TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, false)
                .setupReleaseSecret(TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupAppVersion(1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(false)
                .setShouldBackEndRegisterHaveBeenCalled(false)
                .setShouldBackEndUnregisterDeviceHaveBeenCalled(false)
                .setShouldBackEndUnregisterDeviceBeSuccessful(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run(this);
    }

    public void testWasAlreadyRegisteredWithGcmAndBackendAndTheReleaseSecretIsChanged() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupReleaseUuid(TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, true)
                .setupReleaseSecret(TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_2, TEST_RELEASE_SECRET_2, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupAppVersion(1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(true)
                .setShouldBackEndRegisterHaveBeenCalled(true)
                .setShouldBackEndUnregisterDeviceHaveBeenCalled(true)
                .setShouldBackEndUnregisterDeviceBeSuccessful(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run(this);
    }

    public void testWasAlreadyRegisteredWithGcmAndBackendAndTheDeviceAliasIsChanged() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupReleaseUuid(TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, true)
                .setupReleaseSecret(TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_2, TEST_DEVICE_ALIAS_2, true)
                .setupAppVersion(1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(true)
                .setShouldBackEndRegisterHaveBeenCalled(true)
                .setShouldBackEndUnregisterDeviceHaveBeenCalled(true)
                .setShouldBackEndUnregisterDeviceBeSuccessful(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run(this);
    }

    public void testWasAlreadyRegisteredWithGcmAndBackendAndTheReleaseUuidIsChanged() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupReleaseUuid(TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_2, TEST_RELEASE_UUID_2, true)
                .setupReleaseSecret(TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupAppVersion(1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(true)
                .setShouldBackEndRegisterHaveBeenCalled(true)
                .setShouldBackEndUnregisterDeviceHaveBeenCalled(true)
                .setShouldBackEndUnregisterDeviceBeSuccessful(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run(this);
    }

    public void testWasAlreadyRegisteredWithGcmButNotBackEndAndBackEndRegistrationSucceeds() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, null, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(null, TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupReleaseUuid(null, TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, true)
                .setupReleaseSecret(null, TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, true)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupAppVersion(1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(true)
                .setShouldBackEndRegisterHaveBeenCalled(true)
                .setShouldBackEndUnregisterDeviceHaveBeenCalled(false)
                .setShouldBackEndUnregisterDeviceBeSuccessful(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run(this);
    }

    public void testWasAlreadyRegisteredWithGcmButNotBackEndAndBackEndRegistrationFails() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, null, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(null, null, null)
                .setupReleaseUuid(null, TEST_RELEASE_UUID_1, null, false)
                .setupReleaseSecret(null, TEST_RELEASE_SECRET_1, null, false)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, null, false)
                .setupAppVersion(1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(false)
                .setShouldBackEndRegisterHaveBeenCalled(true)
                .setShouldBackEndUnregisterDeviceHaveBeenCalled(false)
                .setShouldBackEndUnregisterDeviceBeSuccessful(false)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run(this);
    }

    public void testAppUpdatedAndSameGcmRegistrationIdWasReturned() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupReleaseUuid(TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, false)
                .setupReleaseSecret(TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupAppVersion(1, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(false)
                .setShouldBackEndRegisterHaveBeenCalled(false)
                .setShouldBackEndUnregisterDeviceHaveBeenCalled(false)
                .setShouldBackEndUnregisterDeviceBeSuccessful(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run(this);
    }

    public void testAppUpdatedToLesserVersionAndSameGcmRegistrationIdWasReturned() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupReleaseUuid(TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, false)
                .setupReleaseSecret(TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupAppVersion(2, 1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(false)
                .setShouldBackEndRegisterHaveBeenCalled(false)
                .setShouldBackEndUnregisterDeviceHaveBeenCalled(false)
                .setShouldBackEndUnregisterDeviceBeSuccessful(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run(this);
    }

    public void testAppUpdatedAndGcmReregistrationFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, null, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, null, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupReleaseUuid(TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, false)
                .setupReleaseSecret(TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupAppVersion(1, 2)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(false)
                .setShouldBackEndRegisterHaveBeenCalled(false)
                .setShouldBackEndUnregisterDeviceHaveBeenCalled(false)
                .setShouldBackEndUnregisterDeviceBeSuccessful(false)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run(this);
    }

    public void testAppUpdatedAndGcmReregistrationReturnedNewIdButBackEndReregistrationFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, null, null)
                .setupReleaseUuid(TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, null, true)
                .setupReleaseSecret(TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, null, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, null, true)
                .setupAppVersion(1, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(true)
                .setShouldBackEndRegisterHaveBeenCalled(true)
                .setShouldBackEndUnregisterDeviceHaveBeenCalled(true)
                .setShouldBackEndUnregisterDeviceBeSuccessful(true)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run(this);
    }

    public void testAppUpdatedAndDifferentGcmRegistrationIdWasReturned() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_2, TEST_BACK_END_DEVICE_REGISTRATION_ID_2)
                .setupReleaseUuid(TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, true)
                .setupReleaseSecret(TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupAppVersion(1, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(true)
                .setShouldBackEndRegisterHaveBeenCalled(true)
                .setShouldBackEndUnregisterDeviceHaveBeenCalled(true)
                .setShouldBackEndUnregisterDeviceBeSuccessful(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run(this);
    }

    public void testAppUpdatedAndDifferentGcmRegistrationIdWasReturnedButReleaseUuidFromPreviousRegistrationWasNotSaved() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_2, TEST_BACK_END_DEVICE_REGISTRATION_ID_2, TEST_BACK_END_DEVICE_REGISTRATION_ID_2)
                .setupReleaseUuid(null, TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, true)
                .setupReleaseSecret(TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupAppVersion(1, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(true)
                .setShouldBackEndRegisterHaveBeenCalled(true)
                .setShouldBackEndUnregisterDeviceHaveBeenCalled(true)
                .setShouldBackEndUnregisterDeviceBeSuccessful(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run(this);
    }

    public void testAppUpdatedAndDifferentGcmRegistrationIdWasReturnedButBackEndRegistrationIdWasNotSaved() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupBackEndDeviceRegistrationId(null, TEST_BACK_END_DEVICE_REGISTRATION_ID_2, TEST_BACK_END_DEVICE_REGISTRATION_ID_2)
                .setupReleaseUuid(TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, true)
                .setupReleaseSecret(TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupAppVersion(1, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(true)
                .setShouldBackEndRegisterHaveBeenCalled(true)
                .setShouldBackEndUnregisterDeviceHaveBeenCalled(false)
                .setShouldBackEndUnregisterDeviceBeSuccessful(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run(this);
    }

    public void testAppUpdatedAndDifferentGcmRegistrationIdWasReturnedAndUnregisterFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_2, TEST_BACK_END_DEVICE_REGISTRATION_ID_2)
                .setupReleaseUuid(TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, true)
                .setupReleaseSecret(TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupAppVersion(1, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(true)
                .setShouldBackEndRegisterHaveBeenCalled(true)
                .setShouldBackEndUnregisterDeviceHaveBeenCalled(true)
                .setShouldBackEndUnregisterDeviceBeSuccessful(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run(this);
    }

    public void testReleaseSecretUpdatedAndUnregisterFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_2, TEST_BACK_END_DEVICE_REGISTRATION_ID_2)
                .setupReleaseUuid(TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, true)
                .setupReleaseSecret(TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_2, TEST_RELEASE_SECRET_2, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupAppVersion(1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(true)
                .setShouldBackEndRegisterHaveBeenCalled(true)
                .setShouldBackEndUnregisterDeviceHaveBeenCalled(true)
                .setShouldBackEndUnregisterDeviceBeSuccessful(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run(this);
    }

    public void testDeviceAliasUpdatedAndUnregisterFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_2, TEST_BACK_END_DEVICE_REGISTRATION_ID_2)
                .setupReleaseUuid(TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, true)
                .setupReleaseSecret(TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_2, TEST_DEVICE_ALIAS_2, true)
                .setupAppVersion(1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(true)
                .setShouldBackEndRegisterHaveBeenCalled(true)
                .setShouldBackEndUnregisterDeviceHaveBeenCalled(true)
                .setShouldBackEndUnregisterDeviceBeSuccessful(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run(this);
    }

    public void testReleaseUuidUpdatedAndUnregisterFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_2, TEST_BACK_END_DEVICE_REGISTRATION_ID_2)
                .setupReleaseUuid(TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_2, TEST_RELEASE_UUID_2, true)
                .setupReleaseSecret(TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupAppVersion(1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(true)
                .setShouldBackEndRegisterHaveBeenCalled(true)
                .setShouldBackEndUnregisterDeviceHaveBeenCalled(true)
                .setShouldBackEndUnregisterDeviceBeSuccessful(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run(this);
    }

    public void testAppUpdatedToLesserVersionAndDifferentGcmRegistrationIdWasReturned() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_2, TEST_BACK_END_DEVICE_REGISTRATION_ID_2)
                .setupReleaseUuid(TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, true)
                .setupReleaseSecret(TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupAppVersion(2, 1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(true)
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
                if (isSuccessfulRegistration) {
                    Logger.e("Test failed due to exception:" + reason);
                }
                assertFalse(isSuccessfulRegistration);
                semaphore.release();
            }
        };
    }
}
