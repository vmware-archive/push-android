/* Copyright (c) 2013 Pivotal Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pivotal.cf.mobile.pushsdk.registration;

import android.test.AndroidTestCase;

import com.pivotal.cf.mobile.pushsdk.RegistrationParameters;
import com.pivotal.cf.mobile.pushsdk.backend.BackEndRegistrationApiRequestProvider;
import com.pivotal.cf.mobile.pushsdk.backend.FakeBackEndRegistrationApiRequest;
import com.pivotal.cf.mobile.pushsdk.gcm.FakeGcmProvider;
import com.pivotal.cf.mobile.pushsdk.gcm.FakeGcmRegistrationApiRequest;
import com.pivotal.cf.mobile.pushsdk.gcm.FakeGcmUnregistrationApiRequest;
import com.pivotal.cf.mobile.pushsdk.gcm.GcmRegistrationApiRequestProvider;
import com.pivotal.cf.mobile.pushsdk.gcm.GcmUnregistrationApiRequestProvider;
import com.pivotal.cf.mobile.pushsdk.prefs.FakePreferencesProvider;
import com.pivotal.cf.mobile.pushsdk.prefs.PreferencesProvider;
import com.pivotal.cf.mobile.pushsdk.util.PushLibLogger;
import com.pivotal.cf.mobile.pushsdk.version.FakeVersionProvider;
import com.pivotal.cf.mobile.pushsdk.version.VersionProvider;

import java.util.concurrent.Semaphore;

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
    private static final String TEST_PACKAGE_NAME = "TEST.PACKAGE.NAME";

    private FakePreferencesProvider preferencesProvider;
    private GcmRegistrationApiRequestProvider gcmRegistrationApiRequestProvider;
    private GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider;
    private BackEndRegistrationApiRequestProvider backEndRegistrationApiRequestProvider;
    private VersionProvider versionProvider;
    private FakeGcmProvider gcmProvider;
    private Semaphore semaphore = new Semaphore(0);

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        gcmProvider = new FakeGcmProvider(TEST_GCM_DEVICE_REGISTRATION_ID_1);
        gcmRegistrationApiRequestProvider = new GcmRegistrationApiRequestProvider(new FakeGcmRegistrationApiRequest(gcmProvider));
        gcmUnregistrationApiRequestProvider = new GcmUnregistrationApiRequestProvider(new FakeGcmUnregistrationApiRequest(gcmProvider));
        preferencesProvider = new FakePreferencesProvider(null, null, 0, null, null, null, null, null);
        versionProvider = new FakeVersionProvider(10);
        backEndRegistrationApiRequestProvider = new BackEndRegistrationApiRequestProvider(new FakeBackEndRegistrationApiRequest(TEST_BACK_END_DEVICE_REGISTRATION_ID_1));
    }

    public void testNullContext() {
        try {
            new RegistrationEngine(null, TEST_PACKAGE_NAME, gcmProvider, preferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, versionProvider);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullPackageName() {
        try {
            new RegistrationEngine(getContext(), null, gcmProvider, preferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, versionProvider);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullGcmProvider() {
        try {
            new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, null, preferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, versionProvider);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullPreferencesProvider() {
        try {
            new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, gcmProvider, null, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, versionProvider);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullGcmRegistrationApiRequestProvider() {
        try {
            new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, gcmProvider, preferencesProvider, null, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, versionProvider);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullGcmUnregistrationApiRequestProvider() {
        try {
            new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, gcmProvider, preferencesProvider, gcmRegistrationApiRequestProvider, null, backEndRegistrationApiRequestProvider, versionProvider);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullBackEndRegisterDeviceApiRequestProvider() {
        try {
            new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, gcmProvider, preferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, null, versionProvider);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullVersionProvider() {
        try {
            new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, gcmProvider, preferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, null);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullParameters() {
        try {
            final RegistrationEngine engine = new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, gcmProvider, preferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, versionProvider);
            engine.registerDevice(null, getListenerForRegistration(false));
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullSenderId() {
        try {
            final RegistrationEngine engine = new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, gcmProvider, preferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, versionProvider);
            engine.registerDevice(new RegistrationParameters(null, TEST_VARIANT_UUID_1, TEST_VARIANT_SECRET_1, TEST_DEVICE_ALIAS_1), getListenerForRegistration(false));
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullVariantUuid() {
        try {
            final RegistrationEngine engine = new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, gcmProvider, preferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, versionProvider);
            engine.registerDevice(new RegistrationParameters(TEST_GCM_SENDER_ID_1, null, TEST_VARIANT_SECRET_1, TEST_DEVICE_ALIAS_1), getListenerForRegistration(false));
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullVariantSecret() {
        try {
            final RegistrationEngine engine = new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, gcmProvider, preferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, versionProvider);
            engine.registerDevice(new RegistrationParameters(TEST_GCM_SENDER_ID_1, TEST_VARIANT_UUID_1, null, TEST_DEVICE_ALIAS_1), getListenerForRegistration(false));
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullDeviceAlias() throws InterruptedException {
        try {
            final RegistrationEngine engine = new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, gcmProvider, preferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, versionProvider);
            engine.registerDevice(new RegistrationParameters(TEST_GCM_SENDER_ID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_SECRET_1, null), getListenerForRegistration(false));
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testEmptyDeviceAlias() throws InterruptedException {
        final RegistrationEngine engine = new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, gcmProvider, preferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, versionProvider);
        engine.registerDevice(new RegistrationParameters(TEST_GCM_SENDER_ID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_SECRET_1, ""), getListenerForRegistration(true));
        semaphore.acquire();
    }

    public void testGooglePlayServicesNotAvailable() throws InterruptedException {
        gcmProvider.setIsGooglePlayServicesInstalled(false);
        final RegistrationEngine engine = new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, gcmProvider, preferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, versionProvider);
        final RegistrationParameters parameters = new RegistrationParameters(TEST_GCM_SENDER_ID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_SECRET_1, TEST_DEVICE_ALIAS_1);
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
         testParams.run(this);
    }

    public void testFailedInitialGcmRegistration() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(null, null, null)
                .setupBackEndDeviceRegistrationId(null, null, null)
                .setupGcmSenderId(null, TEST_GCM_SENDER_ID_1, null, false)
                .setupVariantUuid(null, TEST_VARIANT_UUID_1, null, false)
                .setupVariantSecret(null, TEST_VARIANT_SECRET_1, null, false)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, null, false)
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
        testParams.run(this);
    }

    public void testInitialGcmRegistrationPassedButInitialBackEndRegistrationFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(null, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(null, null, null)
                .setupGcmSenderId(null, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupVariantUuid(null, TEST_VARIANT_UUID_1, null, false)
                .setupVariantSecret(null, TEST_VARIANT_SECRET_1, null, false)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, null, false)
                .setupPackageName(null, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(false)
                .setShouldBackEndNewRegistrationHaveBeenCalled(true)
                .setShouldBackEndUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run(this);
    }

    public void testWasAlreadyRegisteredWithGcmAndBackend() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, false)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
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
        testParams.run(this);
    }

    public void testWasAlreadyRegisteredWithGcmAndBackendAndTheVariantSecretIsChanged() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, true)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_2, TEST_VARIANT_SECRET_2, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
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
        testParams.run(this);
    }

    public void testWasAlreadyRegisteredWithGcmAndBackendAndTheDeviceAliasIsChanged() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, true)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_2, TEST_DEVICE_ALIAS_2, true)
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
        testParams.run(this);
    }

    public void testWasAlreadyRegisteredWithGcmAndBackendAndTheVariantUuidIsChanged() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_2, TEST_VARIANT_UUID_2, true)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
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
        testParams.run(this);
    }

    public void testWasAlreadyRegisteredWithGcmButNotBackEndAndBackEndRegistrationSucceeds() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, null, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(null, TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupVariantUuid(null, TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, true)
                .setupVariantSecret(null, TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, true)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
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
        testParams.run(this);
    }

    public void testWasAlreadyRegisteredWithGcmButNotBackEndAndBackEndRegistrationFails() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, null, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(null, null, null)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupVariantUuid(null, TEST_VARIANT_UUID_1, null, false)
                .setupVariantSecret(null, TEST_VARIANT_SECRET_1, null, false)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, null, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(false)
                .setShouldBackEndNewRegistrationHaveBeenCalled(true)
                .setShouldBackEndUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run(this);
    }

    public void testAppUpdatedAndSameGcmRegistrationIdWasReturned() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, false)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
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
        testParams.run(this);
    }

    public void testAppUpdatedToLesserVersionAndSameGcmRegistrationIdWasReturned() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, false)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
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
        testParams.run(this);
    }

    public void testAppUpdatedAndGcmReregistrationFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, null, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, null, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, false)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
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
        testParams.run(this);
    }

    public void testAppUpdatedAndGcmReregistrationReturnedNewIdButBackEndReregistrationFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, null, null)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, null, true)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, null, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, null, true)
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
        testParams.run(this);
    }

    public void testAppUpdatedAndDifferentGcmRegistrationIdWasReturned() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_2, TEST_BACK_END_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, true)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
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
        testParams.run(this);
    }

    public void testAppUpdatedAndDifferentGcmRegistrationIdWasReturnedButVariantUuidFromPreviousRegistrationWasNotSaved() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_2, TEST_BACK_END_DEVICE_REGISTRATION_ID_2, TEST_BACK_END_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupVariantUuid(null, TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, true)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
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
        testParams.run(this);
    }

    public void testAppUpdatedAndDifferentGcmRegistrationIdWasReturnedButBackEndRegistrationIdWasNotSaved() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupBackEndDeviceRegistrationId(null, TEST_BACK_END_DEVICE_REGISTRATION_ID_2, TEST_BACK_END_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, true)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
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
        testParams.run(this);
    }

    public void testAppUpdatedAndDifferentGcmRegistrationIdWasReturnedAndUnregisterFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_2, TEST_BACK_END_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, true)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
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
        testParams.run(this);
    }

    public void testVariantSecretUpdatedAndUnregisterFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_2, TEST_BACK_END_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, true)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_2, TEST_VARIANT_SECRET_2, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
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
        testParams.run(this);
    }

    public void testDeviceAliasUpdatedAndUnregisterFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_2, TEST_BACK_END_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, true)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_2, TEST_DEVICE_ALIAS_2, true)
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
        testParams.run(this);
    }

    public void testVariantUuidUpdatedAndUnregisterFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_2, TEST_BACK_END_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_2, TEST_VARIANT_UUID_2, true)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
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
        testParams.run(this);
    }

    public void testAppUpdatedToLesserVersionAndDifferentGcmRegistrationIdWasReturned() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_2, TEST_BACK_END_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, true)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
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
        testParams.run(this);
    }

    public void testSenderIdUpdatedAndGcmUnregistrationFailsAndThenGcmReturnsANewRegistrationId() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_2, TEST_GCM_SENDER_ID_2, true)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, true)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
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
        testParams.run(this);
    }

    public void testSenderIdUpdatedAndGcmUnregistrationFailsAndThenGcmRegistrationFailsToo() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, null, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_2, TEST_GCM_SENDER_ID_1, false)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, false)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
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
        testParams.run(this);
    }

    public void testSenderIdUpdatedAndGcmReturnedNewGcmDeviceRegistrationId() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_2, TEST_GCM_SENDER_ID_2, true)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, true)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
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
        testParams.run(this);
    }

    public void testSenderIdUpdatedAndGcmReturnedOldGcmDeviceRegistrationId() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_2, TEST_GCM_SENDER_ID_2, true)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, false)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
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
        testParams.run(this);
    }

    public void testSenderIdUpdatedAndGcmReregistrationFails() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, null, null)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_2, null, true)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, false)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, true)
                .setupGcmUnregisterDevice(true, true)
                .setupAppVersion(1, 1, PreferencesProvider.NO_SAVED_VERSION)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(false)
                .setShouldBackEndNewRegistrationHaveBeenCalled(false)
                .setShouldBackEndUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run(this);
    }

    public void testSenderIdUpdatedAndGcmReturnedNewGcmDeviceRegistrationIdButBackendReregistrationFails() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_2, TEST_GCM_SENDER_ID_2, true)
                .setupVariantUuid(TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, TEST_VARIANT_UUID_1, true)
                .setupVariantSecret(TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, TEST_VARIANT_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
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
                    PushLibLogger.e("Test failed due to error:" + reason);
                }
                assertFalse(isSuccessfulRegistration);
                semaphore.release();
            }
        };
    }
}
