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

package org.omnia.pushsdk.registration;

import android.test.AndroidTestCase;

import org.omnia.pushsdk.RegistrationParameters;
import org.omnia.pushsdk.backend.BackEndRegistrationApiRequestProvider;
import org.omnia.pushsdk.backend.BackEndUnregisterDeviceApiRequestProvider;
import org.omnia.pushsdk.backend.FakeBackEndRegistrationApiRequest;
import org.omnia.pushsdk.backend.FakeBackEndUnregisterDeviceApiRequest;
import org.omnia.pushsdk.gcm.FakeGcmProvider;
import org.omnia.pushsdk.gcm.FakeGcmRegistrationApiRequest;
import org.omnia.pushsdk.gcm.FakeGcmUnregistrationApiRequest;
import org.omnia.pushsdk.gcm.GcmRegistrationApiRequestProvider;
import org.omnia.pushsdk.gcm.GcmUnregistrationApiRequestProvider;
import org.omnia.pushsdk.prefs.FakePreferencesProvider;
import org.omnia.pushsdk.version.FakeVersionProvider;
import org.omnia.pushsdk.version.VersionProvider;
import com.xtreme.commons.Logger;

import java.util.concurrent.Semaphore;

public class RegistrationEngineTest extends AndroidTestCase {

    private static final String TEST_GCM_DEVICE_REGISTRATION_ID_1 = "TEST_GCM_DEVICE_REGISTRATION_ID_1";
    private static final String TEST_GCM_DEVICE_REGISTRATION_ID_2 = "TEST_GCM_DEVICE_REGISTRATION_ID_2";
    private static final String TEST_BACK_END_DEVICE_REGISTRATION_ID_1 = "TEST_BACK_END_DEVICE_REGISTRATION_ID_1";
    private static final String TEST_BACK_END_DEVICE_REGISTRATION_ID_2 = "TEST_BACK_END_DEVICE_REGISTRATION_ID_2";
    private static final String TEST_DEVICE_ALIAS_1 = "TEST_DEVICE_ALIAS_1";
    private static final String TEST_DEVICE_ALIAS_2 = "TEST_DEVICE_ALIAS_2";
    private static final String TEST_RELEASE_UUID_1 = "TEST_RELEASE_UUID_1";
    private static final String TEST_RELEASE_UUID_2 = "TEST_RELEASE_UUID_2";
    private static final String TEST_RELEASE_SECRET_1 = "TEST_RELEASE_SECRET_1";
    private static final String TEST_RELEASE_SECRET_2 = "TEST_RELEASE_SECRET_2";
    private static final String TEST_GCM_SENDER_ID_1 = "TEST_GCM_SENDER_ID_1";
    private static final String TEST_GCM_SENDER_ID_2 = "TEST_GCM_SENDER_ID_2";

    private FakePreferencesProvider preferencesProvider;
    private GcmRegistrationApiRequestProvider gcmRegistrationApiRequestProvider;
    private GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider;
    private BackEndRegistrationApiRequestProvider backEndRegistrationApiRequestProvider;
    private BackEndUnregisterDeviceApiRequestProvider backEndUnregisterDeviceApiRequestProvider;
    private VersionProvider versionProvider;
    private FakeGcmProvider gcmProvider;
    private Semaphore semaphore = new Semaphore(0);

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        gcmProvider = new FakeGcmProvider(TEST_GCM_DEVICE_REGISTRATION_ID_1);
        gcmRegistrationApiRequestProvider = new GcmRegistrationApiRequestProvider(new FakeGcmRegistrationApiRequest(gcmProvider));
        gcmUnregistrationApiRequestProvider = new GcmUnregistrationApiRequestProvider(new FakeGcmUnregistrationApiRequest(gcmProvider));
        preferencesProvider = new FakePreferencesProvider(null, null, 0, null, null, null, null);
        versionProvider = new FakeVersionProvider(10);
        backEndRegistrationApiRequestProvider = new BackEndRegistrationApiRequestProvider(new FakeBackEndRegistrationApiRequest(TEST_BACK_END_DEVICE_REGISTRATION_ID_1));
        backEndUnregisterDeviceApiRequestProvider = new BackEndUnregisterDeviceApiRequestProvider(new FakeBackEndUnregisterDeviceApiRequest());
    }

    public void testNullContext() {
        try {
            new RegistrationEngine(null, gcmProvider, preferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, backEndUnregisterDeviceApiRequestProvider, versionProvider);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullGcmProvider() {
        try {
            new RegistrationEngine(getContext(), null, preferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, backEndUnregisterDeviceApiRequestProvider, versionProvider);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullPreferencesProvider() {
        try {
            new RegistrationEngine(getContext(), gcmProvider, null, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, backEndUnregisterDeviceApiRequestProvider, versionProvider);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullGcmRegistrationApiRequestProvider() {
        try {
            new RegistrationEngine(getContext(), gcmProvider, preferencesProvider, null, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, backEndUnregisterDeviceApiRequestProvider, versionProvider);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullGcmUnregistrationApiRequestProvider() {
        try {
            new RegistrationEngine(getContext(), gcmProvider, preferencesProvider, gcmRegistrationApiRequestProvider, null, backEndRegistrationApiRequestProvider, backEndUnregisterDeviceApiRequestProvider, versionProvider);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullBackEndRegisterDeviceApiRequestProvider() {
        try {
            new RegistrationEngine(getContext(), gcmProvider, preferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, null, backEndUnregisterDeviceApiRequestProvider, versionProvider);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullBackEndApiUnregisterDeviceRequestProvider() {
        try {
            new RegistrationEngine(getContext(), gcmProvider, preferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, null, versionProvider);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullVersionProvider() {
        try {
            new RegistrationEngine(getContext(), gcmProvider, preferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, backEndUnregisterDeviceApiRequestProvider, null);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullParameters() {
        try {
            final RegistrationEngine engine = new RegistrationEngine(getContext(), gcmProvider, preferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, backEndUnregisterDeviceApiRequestProvider, versionProvider);
            engine.registerDevice(null, getListenerForRegistration(false));
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullSenderId() {
        try {
            final RegistrationEngine engine = new RegistrationEngine(getContext(), gcmProvider, preferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, backEndUnregisterDeviceApiRequestProvider, versionProvider);
            engine.registerDevice(new RegistrationParameters(null, TEST_RELEASE_UUID_1, TEST_RELEASE_SECRET_1, TEST_DEVICE_ALIAS_1), getListenerForRegistration(false));
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullReleaseUuid() {
        try {
            final RegistrationEngine engine = new RegistrationEngine(getContext(), gcmProvider, preferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, backEndUnregisterDeviceApiRequestProvider, versionProvider);
            engine.registerDevice(new RegistrationParameters(TEST_GCM_SENDER_ID_1, null, TEST_RELEASE_SECRET_1, TEST_DEVICE_ALIAS_1), getListenerForRegistration(false));
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullReleaseSecret() {
        try {
            final RegistrationEngine engine = new RegistrationEngine(getContext(), gcmProvider, preferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, backEndUnregisterDeviceApiRequestProvider, versionProvider);
            engine.registerDevice(new RegistrationParameters(TEST_GCM_SENDER_ID_1, TEST_RELEASE_UUID_1, null, TEST_DEVICE_ALIAS_1), getListenerForRegistration(false));
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullDeviceAlias() throws InterruptedException {
        try {
            final RegistrationEngine engine = new RegistrationEngine(getContext(), gcmProvider, preferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, backEndUnregisterDeviceApiRequestProvider, versionProvider);
            engine.registerDevice(new RegistrationParameters(TEST_GCM_SENDER_ID_1, TEST_RELEASE_UUID_1, TEST_RELEASE_SECRET_1, null), getListenerForRegistration(false));
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testEmptyDeviceAlias() throws InterruptedException {
        final RegistrationEngine engine = new RegistrationEngine(getContext(), gcmProvider, preferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, backEndUnregisterDeviceApiRequestProvider, versionProvider);
        engine.registerDevice(new RegistrationParameters(TEST_GCM_SENDER_ID_1, TEST_RELEASE_UUID_1, TEST_RELEASE_SECRET_1, ""), getListenerForRegistration(true));
        semaphore.acquire();
    }

    public void testGooglePlayServicesNotAvailable() throws InterruptedException {
        gcmProvider.setIsGooglePlayServicesInstalled(false);
        final RegistrationEngine engine = new RegistrationEngine(getContext(), gcmProvider, preferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, backEndUnregisterDeviceApiRequestProvider, versionProvider);
        final RegistrationParameters parameters = new RegistrationParameters(TEST_GCM_SENDER_ID_1, TEST_RELEASE_UUID_1, TEST_RELEASE_SECRET_1, TEST_DEVICE_ALIAS_1);
        engine.registerDevice(parameters, getListenerForRegistration(false));
        semaphore.acquire();
    }

    public void testSuccessfulInitialRegistration() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(null, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(null, TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(null, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupReleaseUuid(null, TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, true)
                .setupReleaseSecret(null, TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, true)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupGcmUnregisterDevice(false, false)
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
                .setupGcmSenderId(null, TEST_GCM_SENDER_ID_1, null, false)
                .setupReleaseUuid(null, TEST_RELEASE_UUID_1, null, false)
                .setupReleaseSecret(null, TEST_RELEASE_SECRET_1, null, false)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, null, false)
                .setupGcmUnregisterDevice(false, false)
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
                .setupGcmSenderId(null, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupReleaseUuid(null, TEST_RELEASE_UUID_1, null, false)
                .setupReleaseSecret(null, TEST_RELEASE_SECRET_1, null, false)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, null, false)
                .setupGcmUnregisterDevice(false, false)
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
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupReleaseUuid(TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, false)
                .setupReleaseSecret(TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupGcmUnregisterDevice(false, false)
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
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupReleaseUuid(TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, true)
                .setupReleaseSecret(TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_2, TEST_RELEASE_SECRET_2, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupGcmUnregisterDevice(false, false)
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
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupReleaseUuid(TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, true)
                .setupReleaseSecret(TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_2, TEST_DEVICE_ALIAS_2, true)
                .setupGcmUnregisterDevice(false, false)
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
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupReleaseUuid(TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_2, TEST_RELEASE_UUID_2, true)
                .setupReleaseSecret(TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupGcmUnregisterDevice(false, false)
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
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupReleaseUuid(null, TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, true)
                .setupReleaseSecret(null, TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, true)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupGcmUnregisterDevice(false, false)
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
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupReleaseUuid(null, TEST_RELEASE_UUID_1, null, false)
                .setupReleaseSecret(null, TEST_RELEASE_SECRET_1, null, false)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, null, false)
                .setupGcmUnregisterDevice(false, false)
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
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupReleaseUuid(TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, false)
                .setupReleaseSecret(TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
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
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupReleaseUuid(TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, false)
                .setupReleaseSecret(TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(2, 1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
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
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupReleaseUuid(TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, false)
                .setupReleaseSecret(TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2) // TODO - test that the version saved in the preferences is still 1?
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
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupReleaseUuid(TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, null, true)
                .setupReleaseSecret(TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, null, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, null, true)
                .setupGcmUnregisterDevice(false, false)
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
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupReleaseUuid(TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, true)
                .setupReleaseSecret(TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupGcmUnregisterDevice(false, false)
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
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupReleaseUuid(null, TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, true)
                .setupReleaseSecret(TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupGcmUnregisterDevice(false, false)
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
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupReleaseUuid(TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, true)
                .setupReleaseSecret(TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupGcmUnregisterDevice(false, false)
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
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupReleaseUuid(TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, true)
                .setupReleaseSecret(TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupGcmUnregisterDevice(false, false)
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
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupReleaseUuid(TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, true)
                .setupReleaseSecret(TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_2, TEST_RELEASE_SECRET_2, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupGcmUnregisterDevice(false, false)
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
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupReleaseUuid(TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, true)
                .setupReleaseSecret(TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_2, TEST_DEVICE_ALIAS_2, true)
                .setupGcmUnregisterDevice(false, false)
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
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupReleaseUuid(TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_2, TEST_RELEASE_UUID_2, true)
                .setupReleaseSecret(TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupGcmUnregisterDevice(false, false)
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
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupReleaseUuid(TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, true)
                .setupReleaseSecret(TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupGcmUnregisterDevice(false, false)
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

    public void testSenderIdUpdatedAndGcmReturnedNewGcmDeviceRegistrationId() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_2, TEST_GCM_SENDER_ID_2, true)
                .setupReleaseUuid(TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, true)
                .setupReleaseSecret(TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupGcmUnregisterDevice(true, true)
                .setupAppVersion(1, 1)
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

    public void testSenderIdUpdatedAndGcmReturnedOldGcmDeviceRegistrationId() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_2, TEST_GCM_SENDER_ID_2, true)
                .setupReleaseUuid(TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, false)
                .setupReleaseSecret(TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupGcmUnregisterDevice(true, true)
                .setupAppVersion(1, 1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(false)
                .setShouldBackEndRegisterHaveBeenCalled(false)
                .setShouldBackEndUnregisterDeviceHaveBeenCalled(false)
                .setShouldBackEndUnregisterDeviceBeSuccessful(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run(this);
    }

    public void testSenderIdUpdatedAndGcmReregistrationFails() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, null, null)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_2, null, true)
                .setupReleaseUuid(TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, false)
                .setupReleaseSecret(TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupGcmUnregisterDevice(true, true)
                .setupAppVersion(1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldBackEndDeviceRegistrationHaveBeenSaved(false)
                .setShouldBackEndRegisterHaveBeenCalled(false)
                .setShouldBackEndUnregisterDeviceHaveBeenCalled(false)
                .setShouldBackEndUnregisterDeviceBeSuccessful(false)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run(this);
    }

    public void testSenderIdUpdatedAndGcmReturnedNewGcmDeviceRegistrationIdButBackendUnregistrationFails() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_2, TEST_GCM_SENDER_ID_2, true)
                .setupReleaseUuid(TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, true)
                .setupReleaseSecret(TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupGcmUnregisterDevice(true, true)
                .setupAppVersion(1, 1)
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

    public void testSenderIdUpdatedAndGcmReturnedNewGcmDeviceRegistrationIdButBackendReregistrationFails() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupBackEndDeviceRegistrationId(TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1, TEST_BACK_END_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_2, TEST_GCM_SENDER_ID_2, true)
                .setupReleaseUuid(TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, TEST_RELEASE_UUID_1, true)
                .setupReleaseSecret(TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, TEST_RELEASE_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupGcmUnregisterDevice(true, true)
                .setupAppVersion(1, 1)
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
                    Logger.e("Test failed due to error:" + reason);
                }
                assertFalse(isSuccessfulRegistration);
                semaphore.release();
            }
        };
    }
}
