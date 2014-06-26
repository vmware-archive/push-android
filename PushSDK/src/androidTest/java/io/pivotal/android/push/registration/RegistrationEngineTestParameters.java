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

package io.pivotal.android.push.registration;

import android.content.Context;
import android.content.Intent;
import android.test.AndroidTestCase;

import java.net.URL;
import java.util.HashMap;

import io.pivotal.android.analytics.jobs.EnqueueEventJob;
import io.pivotal.android.analytics.model.events.Event;
import io.pivotal.android.analytics.service.EventService;
import io.pivotal.android.common.test.prefs.FakeAnalyticsPreferencesProvider;
import io.pivotal.android.common.test.util.DelayedLoop;
import io.pivotal.android.common.test.util.FakeServiceStarter;
import io.pivotal.android.push.RegistrationParameters;
import io.pivotal.android.push.backend.BackEndRegistrationApiRequestProvider;
import io.pivotal.android.push.backend.FakeBackEndRegistrationApiRequest;
import io.pivotal.android.push.gcm.FakeGcmProvider;
import io.pivotal.android.push.gcm.FakeGcmRegistrationApiRequest;
import io.pivotal.android.push.gcm.FakeGcmUnregistrationApiRequest;
import io.pivotal.android.push.gcm.GcmRegistrationApiRequestProvider;
import io.pivotal.android.push.gcm.GcmUnregistrationApiRequestProvider;
import io.pivotal.android.push.model.events.EventPushRegistered;
import io.pivotal.android.push.model.events.PushEventHelper;
import io.pivotal.android.push.prefs.FakePushPreferencesProvider;
import io.pivotal.android.push.prefs.PushPreferencesProvider;
import io.pivotal.android.push.version.FakeVersionProvider;

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
    private URL baseServerUrlInPrefs = null;
    private URL baseServerUrlFromUser = null;
    private String packageNameInPrefs = null;
    private String packageNameFromUser = ".";
    private String finalGcmDeviceRegistrationIdInPrefs = null;
    private String finalBackEndDeviceRegistrationIdInPrefs = null;
    private String finalGcmSenderIdInPrefs = null;
    private String finalVariantUuidInPrefs = null;
    private String finalVariantSecretInPrefs = null;
    private String finalDeviceAliasInPrefs = null;
    private String finalPackageNameInPrefs = null;
    private URL finalBaseServerUrlInPrefs = null;

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
    private boolean shouldBackEndNewRegistrationHaveBeenCalled = false;
    private boolean shouldBackEndUpdateRegistrationHaveBeenCalled = false;
    private boolean shouldGcmSenderIdHaveBeenSaved = false;
    private boolean shouldPackageNameHaveBeenSaved = false;
    private boolean shouldBaseServerUrlHaveBeenSaved = false;
    private boolean shouldRegistrationHaveSucceeded = true;
    private boolean shouldPushRegisteredEventHaveBeenLogged = false;

    private int appVersionInPrefs = PushPreferencesProvider.NO_SAVED_VERSION;
    private int currentAppVersion = PushPreferencesProvider.NO_SAVED_VERSION;
    private int finalAppVersionInPrefs = PushPreferencesProvider.NO_SAVED_VERSION;

    public RegistrationEngineTestParameters(Context context) {
        this.context = context;
        delayedLoop = new DelayedLoop(TEN_SECOND_TIMEOUT);
    }

    public void run() {

        shouldPushRegisteredEventHaveBeenLogged =
                shouldBackEndDeviceRegistrationBeSuccessful &&
                (backEndDeviceRegistrationIdFromServer != null) &&
                (shouldBackEndNewRegistrationHaveBeenCalled || shouldBackEndUpdateRegistrationHaveBeenCalled);

        runWithAnalyticsEnabled(false, shouldPushRegisteredEventHaveBeenLogged);
        runWithAnalyticsEnabled(true, shouldPushRegisteredEventHaveBeenLogged);
    }

    private void runWithAnalyticsEnabled(boolean isAnalyticsEnabled, boolean shouldPushRegisteredEventHaveBeenLogged) {

        final FakeGcmProvider gcmProvider = new FakeGcmProvider(gcmDeviceRegistrationIdFromServer, !shouldGcmDeviceRegistrationBeSuccessful, !shouldGcmDeviceUnregistrationBeSuccessful);
        final FakePushPreferencesProvider pushPreferencesProvider = new FakePushPreferencesProvider(gcmDeviceRegistrationIdInPrefs, backEndDeviceRegistrationIdInPrefs, appVersionInPrefs, gcmSenderIdInPrefs, variantUuidInPrefs, variantSecretInPrefs, deviceAliasInPrefs, packageNameInPrefs, baseServerUrlInPrefs);
        final FakeAnalyticsPreferencesProvider analyticsPreferencesProvider = new FakeAnalyticsPreferencesProvider(isAnalyticsEnabled, null);
        final FakeGcmRegistrationApiRequest gcmRegistrationApiRequest = new FakeGcmRegistrationApiRequest(gcmProvider);
        final GcmRegistrationApiRequestProvider gcmRegistrationApiRequestProvider = new GcmRegistrationApiRequestProvider(gcmRegistrationApiRequest);
        final FakeGcmUnregistrationApiRequest gcmUnregistrationApiRequest = new FakeGcmUnregistrationApiRequest(gcmProvider);
        final GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider = new GcmUnregistrationApiRequestProvider(gcmUnregistrationApiRequest);
        final FakeVersionProvider versionProvider = new FakeVersionProvider(currentAppVersion);
        final FakeServiceStarter serviceStarter = new FakeServiceStarter();
        final FakeBackEndRegistrationApiRequest dummyBackEndRegistrationApiRequest = new FakeBackEndRegistrationApiRequest(backEndDeviceRegistrationIdFromServer, shouldBackEndDeviceRegistrationBeSuccessful);
        final BackEndRegistrationApiRequestProvider backEndRegistrationApiRequestProvider = new BackEndRegistrationApiRequestProvider(dummyBackEndRegistrationApiRequest);
        final RegistrationEngine engine = new RegistrationEngine(context, packageNameFromUser, gcmProvider, pushPreferencesProvider, analyticsPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, versionProvider, serviceStarter);
        final RegistrationParameters parameters = new RegistrationParameters(gcmSenderIdFromUser, variantUuidFromUser, variantSecretFromUser, deviceAliasFromUser, baseServerUrlFromUser);

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

        AndroidTestCase.assertTrue(delayedLoop.isSuccess());
        AndroidTestCase.assertEquals(shouldGcmProviderRegisterHaveBeenCalled, gcmProvider.wasRegisterCalled());
        AndroidTestCase.assertEquals(shouldGcmProviderUnregisterHaveBeenCalled, gcmProvider.wasUnregisterCalled());
        AndroidTestCase.assertEquals(shouldBackEndDeviceRegistrationHaveBeenSaved, pushPreferencesProvider.wasBackEndDeviceRegistrationIdSaved());
        AndroidTestCase.assertEquals(shouldBackEndNewRegistrationHaveBeenCalled, dummyBackEndRegistrationApiRequest.isNewRegistration());
        AndroidTestCase.assertEquals(shouldBackEndUpdateRegistrationHaveBeenCalled, dummyBackEndRegistrationApiRequest.isUpdateRegistration());
        AndroidTestCase.assertEquals(shouldAppVersionHaveBeenSaved, pushPreferencesProvider.wasAppVersionSaved());
        AndroidTestCase.assertEquals(shouldGcmDeviceRegistrationIdHaveBeenSaved, pushPreferencesProvider.wasGcmDeviceRegistrationIdSaved());
        AndroidTestCase.assertEquals(shouldGcmSenderIdHaveBeenSaved, pushPreferencesProvider.wasGcmSenderIdSaved());
        AndroidTestCase.assertEquals(shouldVariantUuidHaveBeenSaved, pushPreferencesProvider.wasVariantUuidSaved());
        AndroidTestCase.assertEquals(shouldVariantSecretHaveBeenSaved, pushPreferencesProvider.wasVariantSecretSaved());
        AndroidTestCase.assertEquals(shouldDeviceAliasHaveBeenSaved, pushPreferencesProvider.wasDeviceAliasSaved());
        AndroidTestCase.assertEquals(shouldPackageNameHaveBeenSaved, pushPreferencesProvider.isWasPackageNameSaved());
        AndroidTestCase.assertEquals(shouldBaseServerUrlHaveBeenSaved, pushPreferencesProvider.wasBaseServerUrlSaved());
        AndroidTestCase.assertEquals(finalGcmDeviceRegistrationIdInPrefs, pushPreferencesProvider.getGcmDeviceRegistrationId());
        AndroidTestCase.assertEquals(finalBackEndDeviceRegistrationIdInPrefs, pushPreferencesProvider.getBackEndDeviceRegistrationId());
        AndroidTestCase.assertEquals(finalGcmSenderIdInPrefs, pushPreferencesProvider.getGcmSenderId());
        AndroidTestCase.assertEquals(finalVariantUuidInPrefs, pushPreferencesProvider.getVariantUuid());
        AndroidTestCase.assertEquals(finalVariantSecretInPrefs, pushPreferencesProvider.getVariantSecret());
        AndroidTestCase.assertEquals(finalDeviceAliasInPrefs, pushPreferencesProvider.getDeviceAlias());
        AndroidTestCase.assertEquals(finalBaseServerUrlInPrefs, pushPreferencesProvider.getBaseServerUrl());
        AndroidTestCase.assertEquals(finalAppVersionInPrefs, pushPreferencesProvider.getAppVersion());
        AndroidTestCase.assertEquals(finalPackageNameInPrefs, pushPreferencesProvider.getPackageName());

        if (isAnalyticsEnabled) {
            AndroidTestCase.assertEquals(shouldPushRegisteredEventHaveBeenLogged, serviceStarter.wasStarted());
            if (shouldPushRegisteredEventHaveBeenLogged) {
                final Intent intent = serviceStarter.getStartedIntent();
                final EnqueueEventJob job = intent.getParcelableExtra(EventService.KEY_JOB);
                final Event event = job.getEvent();
                AndroidTestCase.assertEquals(EventPushRegistered.EVENT_TYPE, event.getEventType());
                final HashMap<String, Object> data = event.getData();
                AndroidTestCase.assertEquals(finalVariantUuidInPrefs, data.get(PushEventHelper.VARIANT_UUID));
                AndroidTestCase.assertEquals(finalBackEndDeviceRegistrationIdInPrefs, data.get(PushEventHelper.DEVICE_ID));
            }
        } else {
            AndroidTestCase.assertFalse(serviceStarter.wasStarted());
        }
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

    public RegistrationEngineTestParameters setupBaseServerUrl(URL inPrefs, URL fromUser, URL finalValue, boolean shouldHaveBeenSaved) {
        baseServerUrlInPrefs = inPrefs;
        baseServerUrlFromUser = fromUser;
        finalBaseServerUrlInPrefs = finalValue;
        shouldBaseServerUrlHaveBeenSaved = shouldHaveBeenSaved;
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

    // Useful for when you want to test a null value returned from the server in the 'success' callbacks in the RegistrationEngine
    public RegistrationEngineTestParameters setupBackEndDeviceRegistrationIdWithNullFromServer(String inPrefs, String finalValue) {
        backEndDeviceRegistrationIdInPrefs = inPrefs;
        backEndDeviceRegistrationIdFromServer = null;
        shouldBackEndDeviceRegistrationBeSuccessful = true;
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

    public RegistrationEngineTestParameters setShouldBackEndNewRegistrationHaveBeenCalled(boolean b) {
        shouldBackEndNewRegistrationHaveBeenCalled = b;
        return this;
    }

    public RegistrationEngineTestParameters setShouldBackEndUpdateRegistrationHaveBeenCalled(boolean b) {
        shouldBackEndUpdateRegistrationHaveBeenCalled = b;
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

