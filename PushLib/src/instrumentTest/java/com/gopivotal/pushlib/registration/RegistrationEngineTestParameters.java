package com.gopivotal.pushlib.registration;

import android.content.Context;
import android.test.AndroidTestCase;

import com.gopivotal.pushlib.gcm.FakeGcmProvider;
import com.gopivotal.pushlib.gcm.FakeGcmRegistrationApiRequest;
import com.gopivotal.pushlib.gcm.GcmRegistrationApiRequestProvider;
import com.gopivotal.pushlib.prefs.FakePreferencesProvider;
import com.gopivotal.pushlib.version.FakeVersionProvider;
import com.xtreme.commons.testing.DelayedLoop;

public class RegistrationEngineTestParameters {

    private static final long TEN_SECOND_TIMEOUT = 10000L;
    private static final String TEST_SENDER_ID = "TEST_SENDER_ID";

    private final Context context;
    private final DelayedLoop delayedLoop;
    private String gcmDeviceRegistrationIdInPrefs = null;
    private String backEndDeviceRegistrationIdInPrefs = null;
    private String finalGcmDeviceRegistrationIdInPrefs = null;
    private int appVersionInPrefs = 0;
    private int currentAppVersion = 0;
    private String gcmDeviceRegistrationIdFromServer = null;
    private boolean shouldGcmDeviceRegistrationSuccessful = false;
    private boolean shouldRegistrationHaveSucceeded = true;
    private boolean shouldGcmDeviceRegistrationIdHaveBeenSaved = false;
    private boolean shouldGcmProviderRegisterHaveBeenCalled = false;
    private boolean shouldAppVersionHaveBeenSaved = false;

    public RegistrationEngineTestParameters(Context context) {
        this.context = context;
        delayedLoop = new DelayedLoop(TEN_SECOND_TIMEOUT);
    }

    public void run(AndroidTestCase testCase) {
        final FakeGcmProvider gcmProvider = new FakeGcmProvider(gcmDeviceRegistrationIdFromServer, !shouldGcmDeviceRegistrationSuccessful);
        final FakePreferencesProvider prefsProvider = new FakePreferencesProvider(gcmDeviceRegistrationIdInPrefs, backEndDeviceRegistrationIdInPrefs, appVersionInPrefs);
        final FakeGcmRegistrationApiRequest gcmRequest = new FakeGcmRegistrationApiRequest(gcmProvider);
        final GcmRegistrationApiRequestProvider gcmRequestProvider = new GcmRegistrationApiRequestProvider(gcmRequest);
        final FakeVersionProvider versionProvider = new FakeVersionProvider(currentAppVersion);
        final RegistrationEngine engine = new RegistrationEngine(context, gcmProvider, prefsProvider, gcmRequestProvider, versionProvider);
        engine.registerDevice(TEST_SENDER_ID, new RegistrationListener() {

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
        testCase.assertEquals(shouldAppVersionHaveBeenSaved, prefsProvider.wasAppVersionSaved());
        testCase.assertEquals(shouldGcmDeviceRegistrationIdHaveBeenSaved, prefsProvider.wasGcmDeviceRegistrationIdSaved());
        testCase.assertEquals(finalGcmDeviceRegistrationIdInPrefs, prefsProvider.loadGcmDeviceRegistrationId());
    }

    public RegistrationEngineTestParameters setGcmDeviceRegistrationIdInPreferences(String id) {
        gcmDeviceRegistrationIdInPrefs = id;
        return this;
    }

    public RegistrationEngineTestParameters setBackEndDeviceRegistrationIdInPreferences(String id) {
        backEndDeviceRegistrationIdInPrefs = id;
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

    public RegistrationEngineTestParameters setShouldRegistrationHaveSucceeded(boolean b) {
        shouldRegistrationHaveSucceeded = b;
        return this;
    }

    public RegistrationEngineTestParameters setFinalGcmDeviceRegistrationIdInPreferences(String id) {
        finalGcmDeviceRegistrationIdInPrefs = id;
        return this;
    }

    public RegistrationEngineTestParameters setShouldGcmDeviceRegistrationIdHaveBeenSaved(boolean b) {
        this.shouldGcmDeviceRegistrationIdHaveBeenSaved = b;
        return this;
    }

    public RegistrationEngineTestParameters setShouldGcmProviderRegisterHaveBeenCalled(boolean b) {
        shouldGcmProviderRegisterHaveBeenCalled = b;
        return this;
    }

    public RegistrationEngineTestParameters setShouldAppVersionHaveBeenSaved(boolean b) {
        shouldAppVersionHaveBeenSaved = b;
        return this;
    }
}
