/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.registration;

import android.test.AndroidTestCase;

import java.util.concurrent.Semaphore;

import io.pivotal.android.push.RegistrationParameters;
import io.pivotal.android.push.backend.BackEndRegistrationApiRequestProvider;
import io.pivotal.android.push.backend.FakeBackEndRegistrationApiRequest;
import io.pivotal.android.push.gcm.FakeGcmProvider;
import io.pivotal.android.push.gcm.FakeGcmRegistrationApiRequest;
import io.pivotal.android.push.gcm.FakeGcmUnregistrationApiRequest;
import io.pivotal.android.push.gcm.GcmRegistrationApiRequestProvider;
import io.pivotal.android.push.gcm.GcmUnregistrationApiRequestProvider;
import io.pivotal.android.push.prefs.FakePushPreferencesProvider;
import io.pivotal.android.push.prefs.PushPreferencesProvider;
import io.pivotal.android.push.util.FakeServiceStarter;
import io.pivotal.android.push.util.Logger;
import io.pivotal.android.push.version.FakeVersionProvider;
import io.pivotal.android.push.version.VersionProvider;

public class RegistrationEngineTest extends AndroidTestCase {

    private static final String TEST_GCM_DEVICE_REGISTRATION_ID_1 = "TEST_GCM_DEVICE_REGISTRATION_ID_1";
    private static final String TEST_GCM_DEVICE_REGISTRATION_ID_2 = "TEST_GCM_DEVICE_REGISTRATION_ID_2";
    private static final String TEST_BACK_END_DEVICE_REGISTRATION_ID_1 = "TEST_BACK_END_DEVICE_REGISTRATION_ID_1";
    private static final String TEST_BACK_END_DEVICE_REGISTRATION_ID_2 = "TEST_BACK_END_DEVICE_REGISTRATION_ID_2";
    private static final String TEST_DEVICE_ALIAS_1 = "TEST_DEVICE_ALIAS_1";
    private static final String TEST_DEVICE_ALIAS_2 = "TEST_DEVICE_ALIAS_2";
    private static final String TEST_VARIANT_UUID_1 = "TEST_VARIANT_UUID_1";
    private static final String TEST_VARIANT_UUID_2 = "TEST_VARIANT_UUID_2";
    private static final String TEST_VARIANT_SECRET_1 = "TEST_VARIANT_SECRET_1";
    private static final String TEST_VARIANT_SECRET_2 = "TEST_VARIANT_SECRET_2";
    private static final String TEST_GCM_SENDER_ID_1 = "TEST_GCM_SENDER_ID_1";
    private static final String TEST_GCM_SENDER_ID_2 = "TEST_GCM_SENDER_ID_2";
    private static String TEST_BASE_SERVER_URL_1 = "http://test1.com";
    private static String TEST_BASE_SERVER_URL_2 = "http://test2.com";
    private static final String TEST_PACKAGE_NAME = "TEST.PACKAGE.NAME";

    private FakePushPreferencesProvider pushPreferencesProvider;
    private GcmRegistrationApiRequestProvider gcmRegistrationApiRequestProvider;
    private GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider;
    private BackEndRegistrationApiRequestProvider backEndRegistrationApiRequestProvider;
    private VersionProvider versionProvider;
    private FakeGcmProvider gcmProvider;
    private FakeServiceStarter serviceStarter;
    private Semaphore semaphore = new Semaphore(0);

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        gcmProvider = new FakeGcmProvider(TEST_GCM_DEVICE_REGISTRATION_ID_1);
        gcmRegistrationApiRequestProvider = new GcmRegistrationApiRequestProvider(new FakeGcmRegistrationApiRequest(gcmProvider));
        gcmUnregistrationApiRequestProvider = new GcmUnregistrationApiRequestProvider(new FakeGcmUnregistrationApiRequest(gcmProvider));
        pushPreferencesProvider = new FakePushPreferencesProvider();
        versionProvider = new FakeVersionProvider(10);
        serviceStarter = new FakeServiceStarter();
        backEndRegistrationApiRequestProvider = new BackEndRegistrationApiRequestProvider(new FakeBackEndRegistrationApiRequest(TEST_BACK_END_DEVICE_REGISTRATION_ID_1));
    }

    public void testNullContext() {
        try {
            new RegistrationEngine(null, TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, versionProvider, serviceStarter);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullPackageName() {
        try {
            new RegistrationEngine(getContext(), null, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, versionProvider, serviceStarter);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullGcmProvider() {
        try {
            new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, null, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, versionProvider, serviceStarter);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullPushPreferencesProvider() {
        try {
            new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, gcmProvider, null, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, versionProvider, serviceStarter);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullGcmRegistrationApiRequestProvider() {
        try {
            new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, null, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, versionProvider, serviceStarter);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullGcmUnregistrationApiRequestProvider() {
        try {
            new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, null, backEndRegistrationApiRequestProvider, versionProvider, serviceStarter);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullBackEndRegisterDeviceApiRequestProvider() {
        try {
            new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, null, versionProvider, serviceStarter);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullVersionProvider() {
        try {
            new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, null, serviceStarter);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullServiceStarter() {
        try {
            new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, versionProvider, null);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullParameters() {
        try {
            final RegistrationEngine engine = new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, versionProvider, serviceStarter);
            engine.registerDevice(null, getListenerForRegistration(false));
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullSenderId() {
        try {
            final RegistrationEngine engine = new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, versionProvider, serviceStarter);
            engine.registerDevice(new RegistrationParameters(null, TEST_VARIANT_UUID_1, TEST_VARIANT_SECRET_1, TEST_DEVICE_ALIAS_1, TEST_BASE_SERVER_URL_1), getListenerForRegistration(false));
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullVariantUuid() {
        try {
            final RegistrationEngine engine = new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, versionProvider, serviceStarter);
            engine.registerDevice(new RegistrationParameters(TEST_GCM_SENDER_ID_1, null, TEST_VARIANT_SECRET_1, TEST_DEVICE_ALIAS_1, TEST_BASE_SERVER_URL_1), getListenerForRegistration(false));
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullVariantSecret() {
        try {
            final RegistrationEngine engine = new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, versionProvider, serviceStarter);
            engine.registerDevice(new RegistrationParameters(TEST_GCM_SENDER_ID_1, TEST_VARIANT_UUID_1, null, TEST_DEVICE_ALIAS_1, TEST_BASE_SERVER_URL_1), getListenerForRegistration(false));
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullDeviceAlias() throws InterruptedException {
        try {
            final RegistrationEngine engine = new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, versionProvider, serviceStarter);
            engine.registerDevice(new RegistrationParameters(TEST_GCM_SENDER_ID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_SECRET_1, null, TEST_BASE_SERVER_URL_1), getListenerForRegistration(false));
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testEmptyDeviceAlias() throws InterruptedException {
        final RegistrationEngine engine = new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, versionProvider, serviceStarter);
        engine.registerDevice(new RegistrationParameters(TEST_GCM_SENDER_ID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_SECRET_1, "", TEST_BASE_SERVER_URL_1), getListenerForRegistration(true));
        semaphore.acquire();
    }

    public void testEmptyBaseServerUrl() throws InterruptedException {
        try {
            final RegistrationEngine engine = new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, versionProvider, serviceStarter);
            engine.registerDevice(new RegistrationParameters(TEST_GCM_SENDER_ID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_SECRET_1, TEST_DEVICE_ALIAS_1, null), getListenerForRegistration(true));
            semaphore.acquire();
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testGooglePlayServicesNotAvailable() throws InterruptedException {
        gcmProvider.setIsGooglePlayServicesInstalled(false);
        final RegistrationEngine engine = new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, versionProvider, serviceStarter);
        final RegistrationParameters parameters = new RegistrationParameters(TEST_GCM_SENDER_ID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_SECRET_1, TEST_DEVICE_ALIAS_1, TEST_BASE_SERVER_URL_1);
        engine.registerDevice(parameters, getListenerForRegistration(false));
        semaphore.acquire();
    }

    public void testSuccessfulInitialRegistration() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(null, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(null, TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(null, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupVariantUuid(null, TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, true)
                .setupVariantSecret(null, TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, true)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupBaseServerUrl(null, TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, true)
                .setupPackageName(null, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(true)
                .setShouldBackEndNewRegistrationHaveBeenCalled(true)
                .setShouldBackEndUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testFailedInitialGcmRegistration() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(null, null, null)
                .setupBackEndDeviceRegistrationId(null, null, null)
                .setupGcmSenderId(null, TEST_GCM_SENDER_ID_1, null, false)
                .setupVariantUuid(null, TEST_VARIANT_UUID_1, null, false)
                .setupVariantSecret(null, TEST_VARIANT_SECRET_1, null, false)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, null, false)
                .setupBaseServerUrl(null, TEST_BASE_SERVER_URL_1, null, false)
                .setupPackageName(null, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(false)
                .setShouldBackEndNewRegistrationHaveBeenCalled(false)
                .setShouldBackEndUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testInitialGcmRegistrationPassedButInitialBackEndRegistrationFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(null, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(null, null, null)
                .setupGcmSenderId(null, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupVariantUuid(null, TEST_VARIANT_UUID_1, null, true)
                .setupVariantSecret(null, TEST_VARIANT_SECRET_1, null, true)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, null, true)
                .setupBaseServerUrl(null, TEST_BASE_SERVER_URL_1, null, true)
                .setupPackageName(null, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(true)
                .setShouldBackEndNewRegistrationHaveBeenCalled(true)
                .setShouldBackEndUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testInitialGcmRegistrationPassedButServerReturnedNullBackEndRegistrationId() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(null, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationIdWithNullFromServer(null, null)
                .setupGcmSenderId(null, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupVariantUuid(null, TEST_VARIANT_UUID_1, null, true)
                .setupVariantSecret(null, TEST_VARIANT_SECRET_1, null, true)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, null, true)
                .setupBaseServerUrl(null, TEST_BASE_SERVER_URL_1, null, true)
                .setupPackageName(null, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(true)
                .setShouldBackEndNewRegistrationHaveBeenCalled(true)
                .setShouldBackEndUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testWasAlreadyRegisteredWithGcmAndBackend() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, false)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupBaseServerUrl(TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(false)
                .setShouldBackEndNewRegistrationHaveBeenCalled(false)
                .setShouldBackEndUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testWasAlreadyRegisteredWithGcmAndBackendAndTheVariantSecretIsChanged() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, true)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_2, TEST_VARIANT_SECRET_2, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupBaseServerUrl(TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(true)
                .setShouldBackEndNewRegistrationHaveBeenCalled(false)
                .setShouldBackEndUpdateRegistrationHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testWasAlreadyRegisteredWithGcmAndBackendAndTheDeviceAliasIsChanged() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, true)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_2, TEST_DEVICE_ALIAS_2, true)
                .setupBaseServerUrl(TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(true)
                .setShouldBackEndNewRegistrationHaveBeenCalled(false)
                .setShouldBackEndUpdateRegistrationHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testWasAlreadyRegisteredWithGcmAndBackendAndTheVariantUuidIsChanged() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_2, TEST_VARIANT_UUID_2, true)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupBaseServerUrl(TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(true)
                .setShouldBackEndNewRegistrationHaveBeenCalled(false)
                .setShouldBackEndUpdateRegistrationHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testWasAlreadyRegisteredWithGcmAndBackendAndTheBaseServerUrlIsChanged() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, true)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupBaseServerUrl(TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_2, TEST_BASE_SERVER_URL_2, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(true)
                .setShouldBackEndNewRegistrationHaveBeenCalled(true)
                .setShouldBackEndUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testWasAlreadyRegisteredWithGcmButNotBackEndAndBackEndRegistrationSucceeds() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, null, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(null, TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupVariantUuid(null, TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, true)
                .setupVariantSecret(null, TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, true)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupBaseServerUrl(null, TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(true)
                .setShouldBackEndNewRegistrationHaveBeenCalled(true)
                .setShouldBackEndUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testWasAlreadyRegisteredWithGcmButNotBackEndAndBackEndRegistrationFails() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, null, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(null, null, null)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupVariantUuid(null, TEST_VARIANT_UUID_1, null, true)
                .setupVariantSecret(null, TEST_VARIANT_SECRET_1, null, true)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, null, true)
                .setupBaseServerUrl(null, TEST_BASE_SERVER_URL_1, null, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(true)
                .setShouldBackEndNewRegistrationHaveBeenCalled(true)
                .setShouldBackEndUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testWasAlreadyRegisteredWithGcmButNotBackEndAndServerReturnsNullBackEndRegistrationId() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, null, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationIdWithNullFromServer(null, null)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupVariantUuid(null, TEST_VARIANT_UUID_1, null, true)
                .setupVariantSecret(null, TEST_VARIANT_SECRET_1, null, true)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, null, true)
                .setupBaseServerUrl(null, TEST_BASE_SERVER_URL_1, null, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(true)
                .setShouldBackEndNewRegistrationHaveBeenCalled(true)
                .setShouldBackEndUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testAppUpdatedAndSameGcmRegistrationIdWasReturned() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, false)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupBaseServerUrl(TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(false)
                .setShouldBackEndNewRegistrationHaveBeenCalled(false)
                .setShouldBackEndUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testAppUpdatedToLesserVersionAndSameGcmRegistrationIdWasReturned() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, false)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupBaseServerUrl(TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(2, 1, 1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(false)
                .setShouldBackEndNewRegistrationHaveBeenCalled(false)
                .setShouldBackEndUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testAppUpdatedAndGcmReregistrationFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, null, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, null, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, false)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupBaseServerUrl(TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(false)
                .setShouldBackEndNewRegistrationHaveBeenCalled(false)
                .setShouldBackEndUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testAppUpdatedAndGcmReregistrationReturnedNewIdButBackEndReregistrationFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, null, null)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, null, true)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, null, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, null, true)
                .setupBaseServerUrl(TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, null, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(true)
                .setShouldBackEndNewRegistrationHaveBeenCalled(false)
                .setShouldBackEndUpdateRegistrationHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testAppUpdatedAndGcmReregistrationReturnedNewIdButServerReturnsNullBackEndRegistrationId() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupBackEndDeviceRegistrationIdWithNullFromServer(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, null)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, null, true)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, null, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, null, true)
                .setupBaseServerUrl(TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, null, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(true)
                .setShouldBackEndNewRegistrationHaveBeenCalled(false)
                .setShouldBackEndUpdateRegistrationHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testAppUpdatedAndDifferentGcmRegistrationIdWasReturned() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_2, TEST_BACK_END_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, true)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupBaseServerUrl(TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(true)
                .setShouldBackEndNewRegistrationHaveBeenCalled(false)
                .setShouldBackEndUpdateRegistrationHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testAppUpdatedAndDifferentGcmRegistrationIdWasReturnedAndTheresANewBackEndServerUrlToo() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_2, TEST_BACK_END_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, true)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupBaseServerUrl(TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_2, TEST_BASE_SERVER_URL_2, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(true)
                .setShouldBackEndNewRegistrationHaveBeenCalled(true)
                .setShouldBackEndUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testAppUpdatedAndDifferentGcmRegistrationIdWasReturnedAndTheresANewBackEndServerUrlTooAndBackEndRegistrationFails() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, null, null)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, null, true)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, null, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, null, true)
                .setupBaseServerUrl(TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_2, null, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(true)
                .setShouldBackEndNewRegistrationHaveBeenCalled(true)
                .setShouldBackEndUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testAppUpdatedAndDifferentGcmRegistrationIdWasReturnedAndTheresANewBackEndServerUrlTooAndTheServerReturnsANullBackEndRegistrationId() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupBackEndDeviceRegistrationIdWithNullFromServer(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, null)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, null, true)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, null, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, null, true)
                .setupBaseServerUrl(TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_2, null, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(true)
                .setShouldBackEndNewRegistrationHaveBeenCalled(true)
                .setShouldBackEndUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testAppUpdatedAndDifferentGcmRegistrationIdWasReturnedButVariantUuidFromPreviousRegistrationWasNotSaved() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_2, TEST_BACK_END_DEVICE_REGISTRATION_ID_2, TEST_BACK_END_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupVariantUuid(null, TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, true)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupBaseServerUrl(TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(true)
                .setShouldBackEndNewRegistrationHaveBeenCalled(false)
                .setShouldBackEndUpdateRegistrationHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testAppUpdatedAndDifferentGcmRegistrationIdWasReturnedButBackEndRegistrationIdWasNotSaved() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupBackEndDeviceRegistrationId(null, TEST_BACK_END_DEVICE_REGISTRATION_ID_2, TEST_BACK_END_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, true)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupBaseServerUrl(TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(true)
                .setShouldBackEndNewRegistrationHaveBeenCalled(true)
                .setShouldBackEndUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testAppUpdatedAndDifferentGcmRegistrationIdWasReturnedAndUnregisterFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_2, TEST_BACK_END_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, true)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupBaseServerUrl(TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(true)
                .setShouldBackEndNewRegistrationHaveBeenCalled(false)
                .setShouldBackEndUpdateRegistrationHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testVariantSecretUpdatedAndUnregisterFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_2, TEST_BACK_END_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, true)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_2, TEST_VARIANT_SECRET_2, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupBaseServerUrl(TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(true)
                .setShouldBackEndNewRegistrationHaveBeenCalled(false)
                .setShouldBackEndUpdateRegistrationHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testDeviceAliasUpdatedAndUnregisterFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_2, TEST_BACK_END_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, true)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_2, TEST_DEVICE_ALIAS_2, true)
                .setupBaseServerUrl(TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(true)
                .setShouldBackEndNewRegistrationHaveBeenCalled(false)
                .setShouldBackEndUpdateRegistrationHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testVariantUuidUpdatedAndUnregisterFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_2, TEST_BACK_END_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_2, TEST_VARIANT_UUID_2, true)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupBaseServerUrl(TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(true)
                .setShouldBackEndNewRegistrationHaveBeenCalled(false)
                .setShouldBackEndUpdateRegistrationHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testAppUpdatedToLesserVersionAndDifferentGcmRegistrationIdWasReturned() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_2, TEST_BACK_END_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, true)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupBaseServerUrl(TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(2, 1, 1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(true)
                .setShouldBackEndNewRegistrationHaveBeenCalled(false)
                .setShouldBackEndUpdateRegistrationHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testSenderIdUpdatedAndGcmUnregistrationFailsAndThenGcmReturnsANewRegistrationId() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_2, TEST_GCM_SENDER_ID_2, true)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, true)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupBaseServerUrl(TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, true)
                .setupGcmUnregisterDevice(true, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(true)
                .setShouldBackEndNewRegistrationHaveBeenCalled(false)
                .setShouldBackEndUpdateRegistrationHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testSenderIdUpdatedAndGcmUnregistrationFailsAndThenGcmRegistrationFailsToo() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, null, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_2, TEST_GCM_SENDER_ID_1, false)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, false)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupBaseServerUrl(TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, true)
                .setupGcmUnregisterDevice(true, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(false)
                .setShouldBackEndNewRegistrationHaveBeenCalled(false)
                .setShouldBackEndUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testSenderIdUpdatedAndGcmReturnedNewGcmDeviceRegistrationId() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_2, TEST_GCM_SENDER_ID_2, true)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, true)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupBaseServerUrl(TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, true)
                .setupGcmUnregisterDevice(true, true)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(true)
                .setShouldBackEndNewRegistrationHaveBeenCalled(false)
                .setShouldBackEndUpdateRegistrationHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testSenderIdUpdatedAndGcmReturnedOldGcmDeviceRegistrationId() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_2, TEST_GCM_SENDER_ID_2, true)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, false)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupBaseServerUrl(TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, true)
                .setupGcmUnregisterDevice(true, true)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(false)
                .setShouldBackEndNewRegistrationHaveBeenCalled(false)
                .setShouldBackEndUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testSenderIdUpdatedAndGcmReregistrationFails() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, null, null)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_2, null, true)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, false)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupBaseServerUrl(TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, true)
                .setupGcmUnregisterDevice(true, true)
                .setupAppVersion(1, 1, PushPreferencesProvider.NO_SAVED_VERSION)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(false)
                .setShouldBackEndNewRegistrationHaveBeenCalled(false)
                .setShouldBackEndUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testSenderIdUpdatedAndGcmReturnedNewGcmDeviceRegistrationIdButBackendReregistrationFails() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_2, TEST_GCM_SENDER_ID_2, true)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, true)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupBaseServerUrl(TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, TEST_BASE_SERVER_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, true)
                .setupGcmUnregisterDevice(true, true)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(true)
                .setShouldBackEndNewRegistrationHaveBeenCalled(false)
                .setShouldBackEndUpdateRegistrationHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
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
                    Logger.e("Test failed due to error:" + reason);
                }
                assertFalse(isSuccessfulRegistration);
                semaphore.release();
            }
        };
    }
}