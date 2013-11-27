package com.gopivotal.pushlib.registration;

import android.content.Context;
import android.test.AndroidTestCase;

import com.gopivotal.pushlib.RegistrationParameters;
import com.gopivotal.pushlib.backend.BackEndRegistrationApiRequestProvider;
import com.gopivotal.pushlib.backend.BackEndUnregisterDeviceApiRequestProvider;
import com.gopivotal.pushlib.backend.FakeBackEndRegistrationApiRequest;
import com.gopivotal.pushlib.backend.FakeBackEndUnregisterDeviceApiRequest;
import com.gopivotal.pushlib.gcm.FakeGcmProvider;
import com.gopivotal.pushlib.gcm.FakeGcmRegistrationApiRequest;
import com.gopivotal.pushlib.gcm.GcmRegistrationApiRequestProvider;
import com.gopivotal.pushlib.prefs.FakePreferencesProvider;
import com.gopivotal.pushlib.version.FakeVersionProvider;
import com.xtreme.commons.testing.DelayedLoop;

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
    private String releaseUuidInPrefs = null;
    private String releaseUuidFromUser = null;
    private String releaseSecretInPrefs = null;
    private String releaseSecretFromUser = "S";
    private String deviceAliasInPrefs = null;
    private String deviceAliasFromUser = "S";
    private String finalGcmDeviceRegistrationIdInPrefs = null;
    private String finalBackEndDeviceRegistrationIdInPrefs = null;
    private String finalGcmSenderIdInPrefs = null;
    private String finalReleaseUuidInPrefs = null;
    private String finalReleaseSecretInPrefs = null;
    private String finalDeviceAliasInPrefs = null;

    private boolean shouldGcmDeviceRegistrationBeSuccessful = false;
    private boolean shouldGcmDeviceRegistrationIdHaveBeenSaved = false;
    private boolean shouldGcmProviderRegisterHaveBeenCalled = false;
    private boolean shouldAppVersionHaveBeenSaved = false;
    private boolean shouldBackEndDeviceRegistrationHaveBeenSaved = false;
    private boolean shouldReleaseUuidHaveBeenSaved = false;
    private boolean shouldReleaseSecretHaveBeenSaved = false;
    private boolean shouldDeviceAliasHaveBeenSaved = false;
    private boolean shouldBackEndDeviceRegistrationBeSuccessful = false;
    private boolean shouldBackEndRegisterHaveBeenCalled = false;
    private boolean shouldBackEndUnregisterDeviceBeSuccessful = false;
    private boolean shouldBackEndUnregisterDeviceHaveBeenCalled = false;
    private boolean shouldGcmSenderIdHaveBeenSaved = false;
    private boolean shouldRegistrationHaveSucceeded = true;

    private int appVersionInPrefs = 0;
    private int currentAppVersion = 0;

    public RegistrationEngineTestParameters(Context context) {
        this.context = context;
        delayedLoop = new DelayedLoop(TEN_SECOND_TIMEOUT);
    }

    public void run(AndroidTestCase testCase) {

        final FakeGcmProvider gcmProvider = new FakeGcmProvider(gcmDeviceRegistrationIdFromServer, !shouldGcmDeviceRegistrationBeSuccessful);
        final FakePreferencesProvider prefsProvider = new FakePreferencesProvider(gcmDeviceRegistrationIdInPrefs, backEndDeviceRegistrationIdInPrefs, appVersionInPrefs, gcmSenderIdInPrefs, releaseUuidInPrefs, releaseSecretInPrefs, deviceAliasInPrefs);
        final FakeGcmRegistrationApiRequest gcmRequest = new FakeGcmRegistrationApiRequest(gcmProvider);
        final GcmRegistrationApiRequestProvider gcmRequestProvider = new GcmRegistrationApiRequestProvider(gcmRequest);
        final FakeVersionProvider versionProvider = new FakeVersionProvider(currentAppVersion);
        final FakeBackEndRegistrationApiRequest dummyBackEndRegistrationApiRequest = new FakeBackEndRegistrationApiRequest(backEndDeviceRegistrationIdFromServer, shouldBackEndDeviceRegistrationBeSuccessful);
        final FakeBackEndUnregisterDeviceApiRequest dummyBackEndUnregisterDeviceApiRequest = new FakeBackEndUnregisterDeviceApiRequest(shouldBackEndUnregisterDeviceBeSuccessful);
        final BackEndRegistrationApiRequestProvider backEndRegistrationApiRequestProvider = new BackEndRegistrationApiRequestProvider(dummyBackEndRegistrationApiRequest);
        final BackEndUnregisterDeviceApiRequestProvider backEndUnregisterDeviceApiRequestProvider = new BackEndUnregisterDeviceApiRequestProvider(dummyBackEndUnregisterDeviceApiRequest);
        final RegistrationEngine engine = new RegistrationEngine(context, gcmProvider, prefsProvider, gcmRequestProvider, backEndRegistrationApiRequestProvider, backEndUnregisterDeviceApiRequestProvider, versionProvider);
        final RegistrationParameters parameters = new RegistrationParameters(gcmSenderIdFromUser, releaseUuidFromUser, releaseSecretFromUser, deviceAliasFromUser);

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
        testCase.assertEquals(shouldBackEndDeviceRegistrationHaveBeenSaved, prefsProvider.wasBackEndDeviceRegistrationIdSaved());
        testCase.assertEquals(shouldBackEndRegisterHaveBeenCalled, dummyBackEndRegistrationApiRequest.wasRegisterCalled());
        testCase.assertEquals(shouldBackEndUnregisterDeviceHaveBeenCalled, dummyBackEndUnregisterDeviceApiRequest.wasUnregisterCalled());
        testCase.assertEquals(shouldAppVersionHaveBeenSaved, prefsProvider.wasAppVersionSaved());
        testCase.assertEquals(shouldGcmDeviceRegistrationIdHaveBeenSaved, prefsProvider.wasGcmDeviceRegistrationIdSaved());
        testCase.assertEquals(shouldGcmSenderIdHaveBeenSaved, prefsProvider.wasGcmSenderIdSaved());
        testCase.assertEquals(shouldReleaseUuidHaveBeenSaved, prefsProvider.wasReleaseUuidSaved());
        testCase.assertEquals(shouldReleaseSecretHaveBeenSaved, prefsProvider.wasReleaseSecretSaved());
        testCase.assertEquals(shouldDeviceAliasHaveBeenSaved, prefsProvider.wasDeviceAliasSaved());
        testCase.assertEquals(finalGcmDeviceRegistrationIdInPrefs, prefsProvider.loadGcmDeviceRegistrationId());
        testCase.assertEquals(finalBackEndDeviceRegistrationIdInPrefs, prefsProvider.loadBackEndDeviceRegistrationId());
        testCase.assertEquals(finalGcmSenderIdInPrefs, prefsProvider.loadGcmSenderId());
        testCase.assertEquals(finalReleaseUuidInPrefs, prefsProvider.loadReleaseUuid());
        testCase.assertEquals(finalReleaseSecretInPrefs, prefsProvider.loadReleaseSecret());
        testCase.assertEquals(finalDeviceAliasInPrefs, prefsProvider.loadDeviceAlias());
    }

    public RegistrationEngineTestParameters setupReleaseSecret(String inPrefs, String fromUser, String finalValue, boolean shouldHaveBeenSaved) {
        releaseSecretInPrefs = inPrefs;
        releaseSecretFromUser = fromUser;
        finalReleaseSecretInPrefs = finalValue;
        shouldReleaseSecretHaveBeenSaved = shouldHaveBeenSaved;
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

    public RegistrationEngineTestParameters setupReleaseUuid(String inPrefs, String fromUser, String finalValue, boolean shouldHaveBeenSaved) {
        releaseUuidInPrefs = inPrefs;
        releaseUuidFromUser = fromUser;
        finalReleaseUuidInPrefs = finalValue;
        shouldReleaseUuidHaveBeenSaved = shouldHaveBeenSaved;
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

    public RegistrationEngineTestParameters setupAppVersion(int versionInPrefs, int currentVersion) {
        appVersionInPrefs = versionInPrefs;
        currentAppVersion = currentVersion;
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

