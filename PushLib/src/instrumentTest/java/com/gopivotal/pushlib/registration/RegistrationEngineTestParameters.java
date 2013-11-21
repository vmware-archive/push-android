package com.gopivotal.pushlib.registration;

import android.content.Context;
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
import com.xtreme.commons.testing.DelayedLoop;

public class RegistrationEngineTestParameters {

    private static final long TEN_SECOND_TIMEOUT = 10000L;
    private static final String TEST_SENDER_ID = "TEST_SENDER_ID";
    private static final String TEST_RELEASE_SECRET = "TEST_RELEASE_SECRET";
    private static final String TEST_DEVICE_ALIAS = "TEST_DEVICE_ALIAS";

    private final Context context;
    private final DelayedLoop delayedLoop;

    private String gcmDeviceRegistrationIdInPrefs = null;
    private String gcmDeviceRegistrationIdFromServer = null;
    private String backEndDeviceRegistrationIdInPrefs = null;
    private String backEndDeviceRegistrationIdFromServer;
    private String releaseUuidInPrefs = null;
    private String releaseUuidFromUser = null;
    private String finalGcmDeviceRegistrationIdInPrefs = null;
    private String finalBackEndDeviceRegistrationIdInPrefs = null;
    private String finalReleaseUuidInPrefs = null;

    private boolean shouldGcmDeviceRegistrationSuccessful = false;
    private boolean shouldGcmDeviceRegistrationIdHaveBeenSaved = false;
    private boolean shouldGcmProviderRegisterHaveBeenCalled = false;
    private boolean shouldAppVersionHaveBeenSaved = false;
    private boolean shouldReleaseUuidHaveBeenSaved = false;
    private boolean shouldBackEndDeviceRegistrationBeSuccessful = false;
    private boolean shouldBackEndRegisterHaveBeenCalled = false;
    private boolean shouldBackEndUnregisterDeviceBeSuccessful = false;
    private boolean shouldBackEndUnregisterDeviceHaveBeenCalled = false;
    private boolean shouldRegistrationHaveSucceeded = true;

    private int appVersionInPrefs = 0;
    private int currentAppVersion = 0;

    public RegistrationEngineTestParameters(Context context) {
        this.context = context;
        delayedLoop = new DelayedLoop(TEN_SECOND_TIMEOUT);
    }

    public void run(AndroidTestCase testCase) {

        final FakeGcmProvider gcmProvider = new FakeGcmProvider(gcmDeviceRegistrationIdFromServer, !shouldGcmDeviceRegistrationSuccessful);
        final FakePreferencesProvider prefsProvider = new FakePreferencesProvider(gcmDeviceRegistrationIdInPrefs, backEndDeviceRegistrationIdInPrefs, appVersionInPrefs, releaseUuidInPrefs);
        final FakeGcmRegistrationApiRequest gcmRequest = new FakeGcmRegistrationApiRequest(gcmProvider);
        final GcmRegistrationApiRequestProvider gcmRequestProvider = new GcmRegistrationApiRequestProvider(gcmRequest);
        final FakeVersionProvider versionProvider = new FakeVersionProvider(currentAppVersion);
        final FakeBackEndRegistrationApiRequest dummyBackEndRegistrationApiRequest = new FakeBackEndRegistrationApiRequest(backEndDeviceRegistrationIdFromServer, shouldBackEndDeviceRegistrationBeSuccessful);
        final FakeBackEndUnregisterDeviceApiRequest dummyBackEndUnregisterDeviceApiRequest = new FakeBackEndUnregisterDeviceApiRequest(shouldBackEndUnregisterDeviceBeSuccessful);
        final BackEndRegistrationApiRequestProvider backEndRegistrationApiRequestProvider = new BackEndRegistrationApiRequestProvider(dummyBackEndRegistrationApiRequest);
        final BackEndUnregisterDeviceApiRequestProvider backEndUnregisterDeviceApiRequestProvider = new BackEndUnregisterDeviceApiRequestProvider(dummyBackEndUnregisterDeviceApiRequest);
        final RegistrationEngine engine = new RegistrationEngine(context, gcmProvider, prefsProvider, gcmRequestProvider, backEndRegistrationApiRequestProvider, backEndUnregisterDeviceApiRequestProvider, versionProvider);
        final PushLibParameters parameters = new PushLibParameters(TEST_SENDER_ID, releaseUuidFromUser, TEST_RELEASE_SECRET, TEST_DEVICE_ALIAS);

        // TODO write tests for when SENDER_ID, RELEASE_SECRET, or DEVICE_ALIAS changes.  These scenarios may demand re-registration.

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
        testCase.assertEquals(shouldBackEndRegisterHaveBeenCalled, dummyBackEndRegistrationApiRequest.wasRegisterCalled());
        testCase.assertEquals(shouldBackEndUnregisterDeviceHaveBeenCalled, dummyBackEndUnregisterDeviceApiRequest.wasUnregisterCalled());
        testCase.assertEquals(shouldAppVersionHaveBeenSaved, prefsProvider.wasAppVersionSaved());
        testCase.assertEquals(shouldGcmDeviceRegistrationIdHaveBeenSaved, prefsProvider.wasGcmDeviceRegistrationIdSaved());
        testCase.assertEquals(shouldReleaseUuidHaveBeenSaved, prefsProvider.wasReleaseUuidSaved());
        testCase.assertEquals(finalGcmDeviceRegistrationIdInPrefs, prefsProvider.loadGcmDeviceRegistrationId());
        testCase.assertEquals(finalBackEndDeviceRegistrationIdInPrefs, prefsProvider.loadBackEndDeviceRegistrationId());
        testCase.assertEquals(finalReleaseUuidInPrefs, prefsProvider.loadReleaseUuid());
    }

    public RegistrationEngineTestParameters setReleaseUuidFromUser(String id) {
        releaseUuidFromUser = id;
        return this;
    }

    public RegistrationEngineTestParameters setGcmDeviceRegistrationIdInPreferences(String id) {
        gcmDeviceRegistrationIdInPrefs = id;
        return this;
    }

    public RegistrationEngineTestParameters setBackEndDeviceRegistrationIdInPreferences(String id) {
        backEndDeviceRegistrationIdInPrefs = id;
        return this;
    }

    public RegistrationEngineTestParameters setReleaseUuidInPreferences(String id) {
        releaseUuidInPrefs = id;
        return this;
    }

    public RegistrationEngineTestParameters setAppVersionInPreferences(int ver) {
        appVersionInPrefs = ver;
        return this;
    }

    public RegistrationEngineTestParameters setCurrentAppVersion(int ver) {
        currentAppVersion = ver;
        return this;
    }

    public RegistrationEngineTestParameters setGcmDeviceRegistrationIdFromServer(String id) {
        gcmDeviceRegistrationIdFromServer = id;
        shouldGcmDeviceRegistrationSuccessful = id != null;
        return this;
    }

    public RegistrationEngineTestParameters setBackEndDeviceRegistrationIdFromServer(String id) {
        backEndDeviceRegistrationIdFromServer = id;
        shouldBackEndDeviceRegistrationBeSuccessful = id != null;
        return this;
    }

    public RegistrationEngineTestParameters setShouldRegistrationHaveSucceeded(boolean b) {
        shouldRegistrationHaveSucceeded = b;
        return this;
    }

    public RegistrationEngineTestParameters setFinalGcmDeviceRegistrationIdInPreferences(String id) {
        finalGcmDeviceRegistrationIdInPrefs = id;
        return this;
    }

    public RegistrationEngineTestParameters setFinalBackEndDeviceRegistrationIdInPrefs(String id) {
        finalBackEndDeviceRegistrationIdInPrefs = id;
        return this;
    }

    public RegistrationEngineTestParameters setFinalReleaseUuidInPrefs(String id) {
        finalReleaseUuidInPrefs = id;
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

    public RegistrationEngineTestParameters setShouldReleaseUuidHaveBeenSaved(boolean b) {
        shouldReleaseUuidHaveBeenSaved = b;
        return this;
    }
}
