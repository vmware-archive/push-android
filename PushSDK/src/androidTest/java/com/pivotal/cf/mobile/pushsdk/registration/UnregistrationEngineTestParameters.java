package com.pivotal.cf.mobile.pushsdk.registration;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.MoreAsserts;

import com.pivotal.cf.mobile.pushsdk.backend.BackEndUnregisterDeviceApiRequestProvider;
import com.pivotal.cf.mobile.pushsdk.backend.FakeBackEndUnregisterDeviceApiRequest;
import com.pivotal.cf.mobile.pushsdk.gcm.FakeGcmProvider;
import com.pivotal.cf.mobile.pushsdk.gcm.FakeGcmUnregistrationApiRequest;
import com.pivotal.cf.mobile.pushsdk.gcm.GcmUnregistrationApiRequestProvider;
import com.pivotal.cf.mobile.pushsdk.prefs.FakePreferencesProvider;
import com.pivotal.cf.mobile.pushsdk.util.DelayedLoop;

public class UnregistrationEngineTestParameters {

    private static final long TEN_SECOND_TIMEOUT = 10000L;
    private static final String GCM_SENDER_ID_IN_PREFS = "GCM SENDER ID";
    private static final String VARIANT_UUID_IN_PREFS = "VARIANT UUID";
    private static final String VARIANT_SECRET_IN_PREFS = "VARIANT SECRET";
    private static final String DEVICE_ALIAS_IN_PREFS = "DEVICE ALIAS";
    private static final int APP_VERSION_IN_PREFS = 99;
    private static final String GCM_DEVICE_ID_IN_PREFS = "GCM DEVICE ID";
    private static final String PACKAGE_NAME_IN_PREFS = "PACKAGE.NAME";

    private final Context context;
    private final DelayedLoop delayedLoop;
    private boolean shouldGcmDeviceUnregistrationBeSuccessful;
    private boolean shouldUnregistrationHaveSucceeded;
    private boolean shouldBackEndDeviceUnregistrationBeSuccessful;
    private String backEndDeviceRegistrationIdInPrefs;
    private String backEndDeviceRegistrationIdResultant;
    private boolean shouldBackEndUnregisterHaveBeenCalled;

    public UnregistrationEngineTestParameters(Context context) {
        this.context = context;
        delayedLoop = new DelayedLoop(TEN_SECOND_TIMEOUT);
    }

    public void run(AndroidTestCase testCase) {

        final FakeGcmProvider gcmProvider = new FakeGcmProvider(null, true, !shouldGcmDeviceUnregistrationBeSuccessful);
        final FakePreferencesProvider prefsProvider;
        if (backEndDeviceRegistrationIdInPrefs == null) {
            prefsProvider = new FakePreferencesProvider(null, backEndDeviceRegistrationIdInPrefs, -1, null, null, null, null, null);
        } else {
            prefsProvider = new FakePreferencesProvider(GCM_DEVICE_ID_IN_PREFS, backEndDeviceRegistrationIdInPrefs, APP_VERSION_IN_PREFS, GCM_SENDER_ID_IN_PREFS, VARIANT_UUID_IN_PREFS, VARIANT_SECRET_IN_PREFS, DEVICE_ALIAS_IN_PREFS, PACKAGE_NAME_IN_PREFS);
        }
        final FakeGcmUnregistrationApiRequest gcmUnregistrationApiRequest = new FakeGcmUnregistrationApiRequest(gcmProvider);
        final GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider = new GcmUnregistrationApiRequestProvider(gcmUnregistrationApiRequest);
        final FakeBackEndUnregisterDeviceApiRequest dummyBackEndUnregisterDeviceApiRequest = new FakeBackEndUnregisterDeviceApiRequest(shouldBackEndDeviceUnregistrationBeSuccessful);
        final BackEndUnregisterDeviceApiRequestProvider backEndUnregisterDeviceApiRequestProvider = new BackEndUnregisterDeviceApiRequestProvider(dummyBackEndUnregisterDeviceApiRequest);
        final UnregistrationEngine engine = new UnregistrationEngine(context, gcmProvider, prefsProvider, gcmUnregistrationApiRequestProvider, backEndUnregisterDeviceApiRequestProvider);

        engine.unregisterDevice(new UnregistrationListener() {

            @Override
            public void onUnregistrationComplete() {
                if (shouldUnregistrationHaveSucceeded) {
                    delayedLoop.flagSuccess();
                } else {
                    delayedLoop.flagFailure();
                }
            }

            @Override
            public void onUnregistrationFailed(String reason) {
                if (!shouldUnregistrationHaveSucceeded) {
                    delayedLoop.flagSuccess();
                } else {
                    delayedLoop.flagFailure();
                }
            }
        });
        delayedLoop.startLoop();

        testCase.assertTrue(delayedLoop.isSuccess());

        if (shouldGcmDeviceUnregistrationBeSuccessful) {
            testCase.assertNull(prefsProvider.getGcmDeviceRegistrationId());
            testCase.assertNull(prefsProvider.getGcmSenderId());
            testCase.assertEquals(-1, prefsProvider.getAppVersion());
        } else {
            testCase.assertNotNull(prefsProvider.getGcmDeviceRegistrationId());
            testCase.assertNotNull(prefsProvider.getGcmSenderId());
            MoreAsserts.assertNotEqual(-1, prefsProvider.getAppVersion());
        }

        if (backEndDeviceRegistrationIdResultant == null) {
            testCase.assertNull(prefsProvider.getBackEndDeviceRegistrationId());
            testCase.assertNull(prefsProvider.getDeviceAlias());
            testCase.assertNull(prefsProvider.getVariantSecret());
            testCase.assertNull(prefsProvider.getVariantSecret());
        } else {
            testCase.assertNotNull(prefsProvider.getBackEndDeviceRegistrationId());
            testCase.assertNotNull(prefsProvider.getDeviceAlias());
            testCase.assertNotNull(prefsProvider.getVariantSecret());
            testCase.assertNotNull(prefsProvider.getVariantSecret());
        }

        testCase.assertNull(prefsProvider.getPackageName());
        testCase.assertEquals(shouldBackEndUnregisterHaveBeenCalled, dummyBackEndUnregisterDeviceApiRequest.wasUnregisterCalled());
        testCase.assertFalse(gcmProvider.wasRegisterCalled());
        testCase.assertTrue(gcmProvider.wasUnregisterCalled());
    }

    public UnregistrationEngineTestParameters setShouldUnregistrationHaveSucceeded(boolean b) {
        shouldUnregistrationHaveSucceeded = b;
        return this;
    }

    public UnregistrationEngineTestParameters setShouldGcmDeviceUnregistrationBeSuccessful(boolean b) {
        shouldGcmDeviceUnregistrationBeSuccessful = b;
        return this;
    }

    public UnregistrationEngineTestParameters setupBackEndDeviceRegistrationId(String inPrefs, String resultantValue) {
        backEndDeviceRegistrationIdInPrefs = inPrefs;
        backEndDeviceRegistrationIdResultant = resultantValue;
        shouldBackEndDeviceUnregistrationBeSuccessful = (resultantValue == null);
        shouldBackEndUnregisterHaveBeenCalled = (inPrefs != null);
        return this;
    }

}
