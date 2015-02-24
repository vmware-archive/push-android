/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.registration;

import android.content.Context;
import android.test.AndroidTestCase;

import java.util.Set;

import io.pivotal.android.push.PushParameters;
import io.pivotal.android.push.backend.api.PCFPushRegistrationApiRequestProvider;
import io.pivotal.android.push.backend.api.FakePCFPushRegistrationApiRequest;
import io.pivotal.android.push.gcm.FakeGcmProvider;
import io.pivotal.android.push.gcm.FakeGcmRegistrationApiRequest;
import io.pivotal.android.push.gcm.FakeGcmUnregistrationApiRequest;
import io.pivotal.android.push.gcm.GcmRegistrationApiRequestProvider;
import io.pivotal.android.push.gcm.GcmUnregistrationApiRequestProvider;
import io.pivotal.android.push.prefs.FakePushPreferencesProvider;
import io.pivotal.android.push.prefs.PushPreferencesProvider;
import io.pivotal.android.push.util.DelayedLoop;
import io.pivotal.android.push.version.FakeVersionProvider;

public class RegistrationEngineTestParameters {

    private static final long TEN_SECOND_TIMEOUT = 10000L;

    private final Context context;
    private final DelayedLoop delayedLoop;

    private String gcmDeviceRegistrationIdInPrefs = null;
    private String gcmDeviceRegistrationIdFromServer = null;
    private String pcfPushDeviceRegistrationIdInPrefs = null;
    private String pcfPushDeviceRegistrationIdFromServer;
    private String gcmSenderIdInPrefs = null;
    private String gcmSenderIdFromUser = null;
    private String platformUuidInPrefs = null;
    private String platformUuidFromUser = null;
    private String platformSecretInPrefs = null;
    private String platformSecretFromUser = "S";
    private String deviceAliasInPrefs = null;
    private String deviceAliasFromUser = "S";
    private String serviceUrlInPrefs = null;
    private String serviceUrlFromUser = null;
    private String packageNameInPrefs = null;
    private String packageNameFromUser = ".";
    private String finalGcmDeviceRegistrationIdInPrefs = null;
    private String finalPCFPushDeviceRegistrationIdInPrefs = null;
    private String finalGcmSenderIdInPrefs = null;
    private String finalPlatformUuidInPrefs = null;
    private String finalPlatformSecretInPrefs = null;
    private String finalDeviceAliasInPrefs = null;
    private String finalPackageNameInPrefs = null;
    private String finalServiceUrlInPrefs = null;
    private Set<String> tagsFromUser = null;
    private Set<String> tagsInPrefs = null;
    private Set<String> finalTagsInPrefs = null;

    private boolean shouldGcmDeviceRegistrationBeSuccessful = false;
    private boolean shouldGcmDeviceUnregistrationBeSuccessful = false;
    private boolean shouldGcmDeviceRegistrationIdHaveBeenSaved = false;
    private boolean shouldGcmProviderRegisterHaveBeenCalled = false;
    private boolean shouldGcmProviderUnregisterHaveBeenCalled = false;
    private boolean shouldAppVersionHaveBeenSaved = false;
    private boolean shouldPCFPushDeviceRegistrationHaveBeenSaved = false;
    private boolean shouldPlatformUuidHaveBeenSaved = false;
    private boolean shouldPlatformSecretHaveBeenSaved = false;
    private boolean shouldDeviceAliasHaveBeenSaved = false;
    private boolean shouldTagsHaveBeenSaved = false;
    private boolean shouldPCFPushDeviceRegistrationBeSuccessful = false;
    private boolean shouldPCFPushNewRegistrationHaveBeenCalled = false;
    private boolean shouldPCFPushUpdateRegistrationHaveBeenCalled = false;
    private boolean shouldGcmSenderIdHaveBeenSaved = false;
    private boolean shouldPackageNameHaveBeenSaved = false;
    private boolean shouldServiceUrlHaveBeenSaved = false;
    private boolean shouldRegistrationHaveSucceeded = true;

    private int appVersionInPrefs = PushPreferencesProvider.NO_SAVED_VERSION;
    private int currentAppVersion = PushPreferencesProvider.NO_SAVED_VERSION;
    private int finalAppVersionInPrefs = PushPreferencesProvider.NO_SAVED_VERSION;

    public RegistrationEngineTestParameters(Context context) {
        this.context = context;
        delayedLoop = new DelayedLoop(TEN_SECOND_TIMEOUT);
    }

    public void run() {

        final FakeGcmProvider gcmProvider = new FakeGcmProvider(gcmDeviceRegistrationIdFromServer, !shouldGcmDeviceRegistrationBeSuccessful, !shouldGcmDeviceUnregistrationBeSuccessful);
        final FakePushPreferencesProvider pushPreferencesProvider = new FakePushPreferencesProvider(gcmDeviceRegistrationIdInPrefs, pcfPushDeviceRegistrationIdInPrefs, appVersionInPrefs, gcmSenderIdInPrefs, platformUuidInPrefs, platformSecretInPrefs, deviceAliasInPrefs, packageNameInPrefs, serviceUrlInPrefs, tagsInPrefs);
        final FakeGcmRegistrationApiRequest gcmRegistrationApiRequest = new FakeGcmRegistrationApiRequest(gcmProvider);
        final GcmRegistrationApiRequestProvider gcmRegistrationApiRequestProvider = new GcmRegistrationApiRequestProvider(gcmRegistrationApiRequest);
        final FakeGcmUnregistrationApiRequest gcmUnregistrationApiRequest = new FakeGcmUnregistrationApiRequest(gcmProvider);
        final GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider = new GcmUnregistrationApiRequestProvider(gcmUnregistrationApiRequest);
        final FakeVersionProvider versionProvider = new FakeVersionProvider(currentAppVersion);
        final FakePCFPushRegistrationApiRequest fakePCFPushRegistrationApiRequest = new FakePCFPushRegistrationApiRequest(pcfPushDeviceRegistrationIdFromServer, shouldPCFPushDeviceRegistrationBeSuccessful);
        final PCFPushRegistrationApiRequestProvider PCFPushRegistrationApiRequestProvider = new PCFPushRegistrationApiRequestProvider(fakePCFPushRegistrationApiRequest);
        final RegistrationEngine engine = new RegistrationEngine(context, packageNameFromUser, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, PCFPushRegistrationApiRequestProvider, versionProvider);
        final PushParameters parameters = new PushParameters(gcmSenderIdFromUser, platformUuidFromUser, platformSecretFromUser, serviceUrlFromUser, deviceAliasFromUser, tagsFromUser);

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
        AndroidTestCase.assertEquals(shouldPCFPushDeviceRegistrationHaveBeenSaved, pushPreferencesProvider.wasPCFPushDeviceRegistrationIdSaved());
        AndroidTestCase.assertEquals(shouldPCFPushNewRegistrationHaveBeenCalled, fakePCFPushRegistrationApiRequest.isNewRegistration());
        AndroidTestCase.assertEquals(shouldPCFPushUpdateRegistrationHaveBeenCalled, fakePCFPushRegistrationApiRequest.isUpdateRegistration());
        AndroidTestCase.assertEquals(shouldAppVersionHaveBeenSaved, pushPreferencesProvider.wasAppVersionSaved());
        AndroidTestCase.assertEquals(shouldGcmDeviceRegistrationIdHaveBeenSaved, pushPreferencesProvider.wasGcmDeviceRegistrationIdSaved());
        AndroidTestCase.assertEquals(shouldGcmSenderIdHaveBeenSaved, pushPreferencesProvider.wasGcmSenderIdSaved());
        AndroidTestCase.assertEquals(shouldPlatformUuidHaveBeenSaved, pushPreferencesProvider.wasPlatformUuidSaved());
        AndroidTestCase.assertEquals(shouldPlatformSecretHaveBeenSaved, pushPreferencesProvider.wasPlatformSecretSaved());
        AndroidTestCase.assertEquals(shouldDeviceAliasHaveBeenSaved, pushPreferencesProvider.wasDeviceAliasSaved());
        AndroidTestCase.assertEquals(shouldPackageNameHaveBeenSaved, pushPreferencesProvider.isWasPackageNameSaved());
        AndroidTestCase.assertEquals(shouldServiceUrlHaveBeenSaved, pushPreferencesProvider.wasServiceUrlSaved());
        AndroidTestCase.assertEquals(shouldTagsHaveBeenSaved, pushPreferencesProvider.wereTagsSaved());
        AndroidTestCase.assertEquals(finalGcmDeviceRegistrationIdInPrefs, pushPreferencesProvider.getGcmDeviceRegistrationId());
        AndroidTestCase.assertEquals(finalPCFPushDeviceRegistrationIdInPrefs, pushPreferencesProvider.getPCFPushDeviceRegistrationId());
        AndroidTestCase.assertEquals(finalGcmSenderIdInPrefs, pushPreferencesProvider.getGcmSenderId());
        AndroidTestCase.assertEquals(finalPlatformUuidInPrefs, pushPreferencesProvider.getPlatformUuid());
        AndroidTestCase.assertEquals(finalPlatformSecretInPrefs, pushPreferencesProvider.getPlatformSecret());
        AndroidTestCase.assertEquals(finalDeviceAliasInPrefs, pushPreferencesProvider.getDeviceAlias());
        AndroidTestCase.assertEquals(finalServiceUrlInPrefs, pushPreferencesProvider.getServiceUrl());
        AndroidTestCase.assertEquals(finalAppVersionInPrefs, pushPreferencesProvider.getAppVersion());
        AndroidTestCase.assertEquals(finalPackageNameInPrefs, pushPreferencesProvider.getPackageName());
        AndroidTestCase.assertEquals(finalTagsInPrefs, pushPreferencesProvider.getTags());
    }

    public RegistrationEngineTestParameters setupPackageName(String inPrefs, String fromUser, String finalValue, boolean shouldHaveBeenSaved) {
        packageNameInPrefs = inPrefs;
        packageNameFromUser = fromUser;
        finalPackageNameInPrefs = finalValue;
        shouldPackageNameHaveBeenSaved = shouldHaveBeenSaved;
        return this;
    }

    public RegistrationEngineTestParameters setupPlatformSecret(String inPrefs, String fromUser, String finalValue, boolean shouldHaveBeenSaved) {
        platformSecretInPrefs = inPrefs;
        platformSecretFromUser = fromUser;
        finalPlatformSecretInPrefs = finalValue;
        shouldPlatformSecretHaveBeenSaved = shouldHaveBeenSaved;
        return this;
    }

    public RegistrationEngineTestParameters setupDeviceAlias(String inPrefs, String fromUser, String finalValue, boolean shouldHaveBeenSaved) {
        deviceAliasInPrefs = inPrefs;
        deviceAliasFromUser = fromUser;
        finalDeviceAliasInPrefs = finalValue;
        shouldDeviceAliasHaveBeenSaved = shouldHaveBeenSaved;
        return this;
    }

    public RegistrationEngineTestParameters setupServiceUrl(String inPrefs, String fromUser, String finalValue, boolean shouldHaveBeenSaved) {
        serviceUrlInPrefs = inPrefs;
        serviceUrlFromUser = fromUser;
        finalServiceUrlInPrefs = finalValue;
        shouldServiceUrlHaveBeenSaved = shouldHaveBeenSaved;
        return this;
    }

    public RegistrationEngineTestParameters setupGcmSenderId(String inPrefs, String fromUser, String finalValue, boolean shouldHaveBeenSaved) {
        gcmSenderIdInPrefs = inPrefs;
        gcmSenderIdFromUser = fromUser;
        finalGcmSenderIdInPrefs = finalValue;
        shouldGcmSenderIdHaveBeenSaved = shouldHaveBeenSaved;
        return this;
    }

    public RegistrationEngineTestParameters setupPlatformUuid(String inPrefs, String fromUser, String finalValue, boolean shouldHaveBeenSaved) {
        platformUuidInPrefs = inPrefs;
        platformUuidFromUser = fromUser;
        finalPlatformUuidInPrefs = finalValue;
        shouldPlatformUuidHaveBeenSaved = shouldHaveBeenSaved;
        return this;
    }

    public RegistrationEngineTestParameters setupTags(Set<String> inPrefs, Set<String> fromUser, Set<String> finalValue, boolean shouldHaveBeenSaved) {
        tagsInPrefs = inPrefs;
        tagsFromUser = fromUser;
        finalTagsInPrefs = finalValue;
        shouldTagsHaveBeenSaved = shouldHaveBeenSaved;
        return this;
    }

    public RegistrationEngineTestParameters setupGcmDeviceRegistrationId(String inPrefs, String fromServer, String finalValue) {
        gcmDeviceRegistrationIdInPrefs = inPrefs;
        gcmDeviceRegistrationIdFromServer = fromServer;
        finalGcmDeviceRegistrationIdInPrefs = finalValue;
        shouldGcmDeviceRegistrationBeSuccessful = fromServer != null;
        return this;
    }

    public RegistrationEngineTestParameters setupPCFPushDeviceRegistrationId(String inPrefs, String fromServer, String finalValue) {
        pcfPushDeviceRegistrationIdInPrefs = inPrefs;
        pcfPushDeviceRegistrationIdFromServer = fromServer;
        shouldPCFPushDeviceRegistrationBeSuccessful = fromServer != null;
        finalPCFPushDeviceRegistrationIdInPrefs = finalValue;
        return this;
    }

    // Useful for when you want to test a null value returned from the server in the 'success' callbacks in the RegistrationEngine
    public RegistrationEngineTestParameters setupPCFPushDeviceRegistrationIdWithNullFromServer(String inPrefs, String finalValue) {
        pcfPushDeviceRegistrationIdInPrefs = inPrefs;
        pcfPushDeviceRegistrationIdFromServer = null;
        shouldPCFPushDeviceRegistrationBeSuccessful = true;
        finalPCFPushDeviceRegistrationIdInPrefs = finalValue;
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

    public RegistrationEngineTestParameters setShouldPCFPushNewRegistrationHaveBeenCalled(boolean b) {
        shouldPCFPushNewRegistrationHaveBeenCalled = b;
        return this;
    }

    public RegistrationEngineTestParameters setShouldPCFPushUpdateRegistrationHaveBeenCalled(boolean b) {
        shouldPCFPushUpdateRegistrationHaveBeenCalled = b;
        return this;
    }

    public RegistrationEngineTestParameters setShouldAppVersionHaveBeenSaved(boolean b) {
        shouldAppVersionHaveBeenSaved = b;
        return this;
    }

    public RegistrationEngineTestParameters setShouldPCFPushDeviceRegistrationHaveBeenSaved(boolean b) {
        shouldPCFPushDeviceRegistrationHaveBeenSaved = b;
        return this;
    }
}