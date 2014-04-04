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

import android.content.Context;
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
import org.omnia.pushsdk.prefs.PreferencesProvider;
import org.omnia.pushsdk.util.DelayedLoop;
import org.omnia.pushsdk.version.FakeVersionProvider;

public class RegistrationEngineTestParameters {

    private static final long TEN_SECOND_TIMEOUT = 10000L;

    private final Context context;
    private final DelayedLoop delayedLoop;

    private String gcmDeviceRegistrationIdInPrefs = null;
    private String gcmDeviceRegistrationIdFromServer = null;
    private String backEndDeviceRegistrationIdInPrefs = null;
    private String backEndDeviceRegistrationIdFromServer;
    private String gcmSenderIdInPrefs = null;
    private String gcmSenderIdFromUser = null;
    private String variantUuidInPrefs = null;
    private String variantUuidFromUser = null;
    private String variantSecretInPrefs = null;
    private String variantSecretFromUser = "S";
    private String deviceAliasInPrefs = null;
    private String deviceAliasFromUser = "S";
    private String packageNameInPrefs = null;
    private String packageNameFromUser = ".";
    private String finalGcmDeviceRegistrationIdInPrefs = null;
    private String finalBackEndDeviceRegistrationIdInPrefs = null;
    private String finalGcmSenderIdInPrefs = null;
    private String finalVariantUuidInPrefs = null;
    private String finalVariantSecretInPrefs = null;
    private String finalDeviceAliasInPrefs = null;
    private String finalPackageNameInPrefs = null;

    private boolean shouldGcmDeviceRegistrationBeSuccessful = false;
    private boolean shouldGcmDeviceUnregistrationBeSuccessful = false;
    private boolean shouldGcmDeviceRegistrationIdHaveBeenSaved = false;
    private boolean shouldGcmProviderRegisterHaveBeenCalled = false;
    private boolean shouldGcmProviderUnregisterHaveBeenCalled = false;
    private boolean shouldAppVersionHaveBeenSaved = false;
    private boolean shouldBackEndDeviceRegistrationHaveBeenSaved = false;
    private boolean shouldVariantUuidHaveBeenSaved = false;
    private boolean shouldVariantSecretHaveBeenSaved = false;
    private boolean shouldDeviceAliasHaveBeenSaved = false;
    private boolean shouldBackEndDeviceRegistrationBeSuccessful = false;
    private boolean shouldBackEndRegisterHaveBeenCalled = false;
    private boolean shouldBackEndUnregisterDeviceBeSuccessful = false;
    private boolean shouldBackEndUnregisterDeviceHaveBeenCalled = false;
    private boolean shouldGcmSenderIdHaveBeenSaved = false;
    private boolean shouldRegistrationHaveSucceeded = true;
    private boolean shouldPackageNameHaveBeenSaved = false;

    private int appVersionInPrefs = PreferencesProvider.NO_SAVED_VERSION;
    private int currentAppVersion = PreferencesProvider.NO_SAVED_VERSION;
    private int finalAppVersionInPrefs = PreferencesProvider.NO_SAVED_VERSION;

    public RegistrationEngineTestParameters(Context context) {
        this.context = context;
        delayedLoop = new DelayedLoop(TEN_SECOND_TIMEOUT);
    }

    public void run(AndroidTestCase testCase) {

        final FakeGcmProvider gcmProvider = new FakeGcmProvider(gcmDeviceRegistrationIdFromServer, !shouldGcmDeviceRegistrationBeSuccessful, !shouldGcmDeviceUnregistrationBeSuccessful);
        final FakePreferencesProvider prefsProvider = new FakePreferencesProvider(gcmDeviceRegistrationIdInPrefs, backEndDeviceRegistrationIdInPrefs, appVersionInPrefs, gcmSenderIdInPrefs, variantUuidInPrefs, variantSecretInPrefs, deviceAliasInPrefs, packageNameInPrefs);
        final FakeGcmRegistrationApiRequest gcmRegistrationApiRequest = new FakeGcmRegistrationApiRequest(gcmProvider);
        final GcmRegistrationApiRequestProvider gcmRegistrationApiRequestProvider = new GcmRegistrationApiRequestProvider(gcmRegistrationApiRequest);
        final FakeGcmUnregistrationApiRequest gcmUnregistrationApiRequest = new FakeGcmUnregistrationApiRequest(gcmProvider);
        final GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider = new GcmUnregistrationApiRequestProvider(gcmUnregistrationApiRequest);
        final FakeVersionProvider versionProvider = new FakeVersionProvider(currentAppVersion);
        final FakeBackEndRegistrationApiRequest dummyBackEndRegistrationApiRequest = new FakeBackEndRegistrationApiRequest(backEndDeviceRegistrationIdFromServer, shouldBackEndDeviceRegistrationBeSuccessful);
        final FakeBackEndUnregisterDeviceApiRequest dummyBackEndUnregisterDeviceApiRequest = new FakeBackEndUnregisterDeviceApiRequest(shouldBackEndUnregisterDeviceBeSuccessful);
        final BackEndRegistrationApiRequestProvider backEndRegistrationApiRequestProvider = new BackEndRegistrationApiRequestProvider(dummyBackEndRegistrationApiRequest);
        final BackEndUnregisterDeviceApiRequestProvider backEndUnregisterDeviceApiRequestProvider = new BackEndUnregisterDeviceApiRequestProvider(dummyBackEndUnregisterDeviceApiRequest);
        final RegistrationEngine engine = new RegistrationEngine(context, packageNameFromUser, gcmProvider, prefsProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, backEndUnregisterDeviceApiRequestProvider, versionProvider);
        final RegistrationParameters parameters = new RegistrationParameters(gcmSenderIdFromUser, variantUuidFromUser, variantSecretFromUser, deviceAliasFromUser);

        engine.registerDevice(parameters, new RegistrationListener() {

            @Override
            public void onRegistrationComplete() {
                if (shouldRegistrationHaveSucceeded) {
                    delayedLoop.flagSuccess();
                } else {
                    delayedLoop.flagFailure();
                }
            }

            @Override
            public void onRegistrationFailed(String reason) {
                if (!shouldRegistrationHaveSucceeded) {
                    delayedLoop.flagSuccess();
                } else {
                    delayedLoop.flagFailure();
                }
            }
        });
        delayedLoop.startLoop();

        testCase.assertTrue(delayedLoop.isSuccess());
        testCase.assertEquals(shouldGcmProviderRegisterHaveBeenCalled, gcmProvider.wasRegisterCalled());
        testCase.assertEquals(shouldGcmProviderUnregisterHaveBeenCalled, gcmProvider.wasUnregisterCalled());
        testCase.assertEquals(shouldBackEndDeviceRegistrationHaveBeenSaved, prefsProvider.wasBackEndDeviceRegistrationIdSaved());
        testCase.assertEquals(shouldBackEndRegisterHaveBeenCalled, dummyBackEndRegistrationApiRequest.wasRegisterCalled());
        testCase.assertEquals(shouldBackEndUnregisterDeviceHaveBeenCalled, dummyBackEndUnregisterDeviceApiRequest.wasUnregisterCalled());
        testCase.assertEquals(shouldAppVersionHaveBeenSaved, prefsProvider.wasAppVersionSaved());
        testCase.assertEquals(shouldGcmDeviceRegistrationIdHaveBeenSaved, prefsProvider.wasGcmDeviceRegistrationIdSaved());
        testCase.assertEquals(shouldGcmSenderIdHaveBeenSaved, prefsProvider.wasGcmSenderIdSaved());
        testCase.assertEquals(shouldVariantUuidHaveBeenSaved, prefsProvider.wasVariantUuidSaved());
        testCase.assertEquals(shouldVariantSecretHaveBeenSaved, prefsProvider.wasVariantSecretSaved());
        testCase.assertEquals(shouldDeviceAliasHaveBeenSaved, prefsProvider.wasDeviceAliasSaved());
        testCase.assertEquals(shouldPackageNameHaveBeenSaved, prefsProvider.isWasPackageNameSaved());
        testCase.assertEquals(finalGcmDeviceRegistrationIdInPrefs, prefsProvider.loadGcmDeviceRegistrationId());
        testCase.assertEquals(finalBackEndDeviceRegistrationIdInPrefs, prefsProvider.loadBackEndDeviceRegistrationId());
        testCase.assertEquals(finalGcmSenderIdInPrefs, prefsProvider.loadGcmSenderId());
        testCase.assertEquals(finalVariantUuidInPrefs, prefsProvider.loadVariantUuid());
        testCase.assertEquals(finalVariantSecretInPrefs, prefsProvider.loadVariantSecret());
        testCase.assertEquals(finalDeviceAliasInPrefs, prefsProvider.loadDeviceAlias());
        testCase.assertEquals(finalAppVersionInPrefs, prefsProvider.loadAppVersion());
        testCase.assertEquals(finalPackageNameInPrefs, prefsProvider.loadPackageName());
    }

    public RegistrationEngineTestParameters setupPackageName(String inPrefs, String fromUser, String finalValue, boolean shouldHaveBeenSaved) {
        packageNameInPrefs = inPrefs;
        packageNameFromUser = fromUser;
        finalPackageNameInPrefs = finalValue;
        shouldPackageNameHaveBeenSaved = shouldHaveBeenSaved;
        return this;
    }

    public RegistrationEngineTestParameters setupVariantSecret(String inPrefs, String fromUser, String finalValue, boolean shouldHaveBeenSaved) {
        variantSecretInPrefs = inPrefs;
        variantSecretFromUser = fromUser;
        finalVariantSecretInPrefs = finalValue;
        shouldVariantSecretHaveBeenSaved = shouldHaveBeenSaved;
        return this;
    }

    public RegistrationEngineTestParameters setupDeviceAlias(String inPrefs, String fromUser, String finalValue, boolean shouldHaveBeenSaved) {
        deviceAliasInPrefs = inPrefs;
        deviceAliasFromUser = fromUser;
        finalDeviceAliasInPrefs = finalValue;
        shouldDeviceAliasHaveBeenSaved = shouldHaveBeenSaved;
        return this;
    }

    public RegistrationEngineTestParameters setupGcmSenderId(String inPrefs, String fromUser, String finalValue, boolean shouldHaveBeenSaved) {
        gcmSenderIdInPrefs = inPrefs;
        gcmSenderIdFromUser = fromUser;
        finalGcmSenderIdInPrefs = finalValue;
        shouldGcmSenderIdHaveBeenSaved = shouldHaveBeenSaved;
        return this;
    }

    public RegistrationEngineTestParameters setupVariantUuid(String inPrefs, String fromUser, String finalValue, boolean shouldHaveBeenSaved) {
        variantUuidInPrefs = inPrefs;
        variantUuidFromUser = fromUser;
        finalVariantUuidInPrefs = finalValue;
        shouldVariantUuidHaveBeenSaved = shouldHaveBeenSaved;
        return this;
    }

    public RegistrationEngineTestParameters setupGcmDeviceRegistrationId(String inPrefs, String fromServer, String finalValue) {
        gcmDeviceRegistrationIdInPrefs = inPrefs;
        gcmDeviceRegistrationIdFromServer = fromServer;
        finalGcmDeviceRegistrationIdInPrefs = finalValue;
        shouldGcmDeviceRegistrationBeSuccessful = fromServer != null;
        return this;
    }

    public RegistrationEngineTestParameters setupBackEndDeviceRegistrationId(String inPrefs, String fromServer, String finalValue) {
        backEndDeviceRegistrationIdInPrefs = inPrefs;
        backEndDeviceRegistrationIdFromServer = fromServer;
        shouldBackEndDeviceRegistrationBeSuccessful = fromServer != null;
        finalBackEndDeviceRegistrationIdInPrefs = finalValue;
        return this;
    }

    public RegistrationEngineTestParameters setupGcmUnregisterDevice(boolean shouldHaveBeenCalled, boolean shouldBeSuccessful) {
        shouldGcmProviderUnregisterHaveBeenCalled = shouldHaveBeenCalled;
        shouldGcmDeviceUnregistrationBeSuccessful = shouldBeSuccessful;
        return this;
    }

    public RegistrationEngineTestParameters setupAppVersion(int versionInPrefs, int currentVersion, int finalValue) {
        appVersionInPrefs = versionInPrefs;
        currentAppVersion = currentVersion;
        finalAppVersionInPrefs = finalValue;
        return this;
    }

    public RegistrationEngineTestParameters setShouldRegistrationHaveSucceeded(boolean b) {
        shouldRegistrationHaveSucceeded = b;
        return this;
    }

    public RegistrationEngineTestParameters setShouldGcmDeviceRegistrationIdHaveBeenSaved(boolean b) {
        shouldGcmDeviceRegistrationIdHaveBeenSaved = b;
        return this;
    }

    public RegistrationEngineTestParameters setShouldGcmProviderRegisterHaveBeenCalled(boolean b) {
        shouldGcmProviderRegisterHaveBeenCalled = b;
        return this;
    }

    public RegistrationEngineTestParameters setShouldBackEndRegisterHaveBeenCalled(boolean b) {
        shouldBackEndRegisterHaveBeenCalled = b;
        return this;
    }

    public RegistrationEngineTestParameters setShouldBackEndUnregisterDeviceHaveBeenCalled(boolean b) {
        shouldBackEndUnregisterDeviceHaveBeenCalled = b;
        return this;
    }

    public RegistrationEngineTestParameters setShouldBackEndUnregisterDeviceBeSuccessful(boolean b) {
        shouldBackEndUnregisterDeviceBeSuccessful = b;
        return this;
    }

    public RegistrationEngineTestParameters setShouldAppVersionHaveBeenSaved(boolean b) {
        shouldAppVersionHaveBeenSaved = b;
        return this;
    }

    public RegistrationEngineTestParameters setShouldBackEndDeviceRegistrationHaveBeenSaved(boolean b) {
        shouldBackEndDeviceRegistrationHaveBeenSaved = b;
        return this;
    }
}

