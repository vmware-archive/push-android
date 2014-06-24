package com.pivotal.mss.pushsdk.registration;

import android.content.Context;
import android.content.Intent;
import android.test.AndroidTestCase;
import android.test.MoreAsserts;

import com.pivotal.mss.analyticssdk.jobs.EnqueueEventJob;
import com.pivotal.mss.analyticssdk.model.events.Event;
import com.pivotal.mss.analyticssdk.service.EventService;
import com.pivotal.mss.common.test.prefs.FakeAnalyticsPreferencesProvider;
import com.pivotal.mss.common.test.util.DelayedLoop;
import com.pivotal.mss.common.test.util.FakeServiceStarter;
import com.pivotal.mss.pushsdk.RegistrationParameters;
import com.pivotal.mss.pushsdk.backend.BackEndUnregisterDeviceApiRequestProvider;
import com.pivotal.mss.pushsdk.backend.FakeBackEndUnregisterDeviceApiRequest;
import com.pivotal.mss.pushsdk.gcm.FakeGcmProvider;
import com.pivotal.mss.pushsdk.gcm.FakeGcmUnregistrationApiRequest;
import com.pivotal.mss.pushsdk.gcm.GcmUnregistrationApiRequestProvider;
import com.pivotal.mss.pushsdk.model.events.EventPushUnregistered;
import com.pivotal.mss.pushsdk.model.events.PushEventHelper;
import com.pivotal.mss.pushsdk.prefs.FakePushPreferencesProvider;

import java.net.URL;
import java.util.HashMap;

public class UnregistrationEngineTestParameters {

    private static final long TEN_SECOND_TIMEOUT = 10000L;
    private static final String GCM_SENDER_ID_IN_PREFS = "GCM SENDER ID";
    private static final String VARIANT_UUID_IN_PREFS = "VARIANT UUID";
    private static final String VARIANT_SECRET_IN_PREFS = "VARIANT SECRET";
    private static final String DEVICE_ALIAS_IN_PREFS = "DEVICE ALIAS";
    private static final int APP_VERSION_IN_PREFS = 99;
    private static final String GCM_DEVICE_ID_IN_PREFS = "GCM DEVICE ID";
    private static final String PACKAGE_NAME_IN_PREFS = "PACKAGE.NAME";
    private static final String BASE_SERVER_URL_IN_PREFS = "http://test.com";

    private final Context context;
    private final DelayedLoop delayedLoop;
    private boolean shouldGcmDeviceUnregistrationBeSuccessful;
    private boolean shouldUnregistrationHaveSucceeded;
    private boolean shouldBackEndDeviceUnregistrationBeSuccessful;
    private String startingBackEndDeviceRegistrationIdInPrefs;
    private String backEndDeviceRegistrationIdResultant;
    private boolean shouldBackEndUnregisterHaveBeenCalled;
    private RegistrationParameters parametersFromUser;
    private String startingVariantUuidInPrefs;

    public UnregistrationEngineTestParameters(Context context) {
        this.context = context;
        delayedLoop = new DelayedLoop(TEN_SECOND_TIMEOUT);
    }

    public void run() throws Exception {

        final boolean shouldPushUnregisteredEventHaveBeenLogged =
                shouldBackEndUnregisterHaveBeenCalled &&
                        (backEndDeviceRegistrationIdResultant == null);

        runWithAnalyticsEnabled(false, shouldPushUnregisteredEventHaveBeenLogged);
        runWithAnalyticsEnabled(true, shouldPushUnregisteredEventHaveBeenLogged);
    }

    private void runWithAnalyticsEnabled(boolean isAnalyticsEnabled, boolean shouldPushUnregisteredEventHaveBeenLogged) throws Exception {

        final FakeGcmProvider gcmProvider = new FakeGcmProvider(null, true, !shouldGcmDeviceUnregistrationBeSuccessful);
        final FakePushPreferencesProvider pushPreferencesProvider;

        if (startingBackEndDeviceRegistrationIdInPrefs == null) {
            startingVariantUuidInPrefs = null;
            pushPreferencesProvider = new FakePushPreferencesProvider(null, startingBackEndDeviceRegistrationIdInPrefs, -1, null, startingVariantUuidInPrefs, null, null, null, null);
        } else {
            final URL baseServerUrlInPrefs = new URL(BASE_SERVER_URL_IN_PREFS);
            startingVariantUuidInPrefs = VARIANT_UUID_IN_PREFS;
            pushPreferencesProvider = new FakePushPreferencesProvider(GCM_DEVICE_ID_IN_PREFS, startingBackEndDeviceRegistrationIdInPrefs, APP_VERSION_IN_PREFS, GCM_SENDER_ID_IN_PREFS, startingVariantUuidInPrefs, VARIANT_SECRET_IN_PREFS, DEVICE_ALIAS_IN_PREFS, PACKAGE_NAME_IN_PREFS, baseServerUrlInPrefs);
        }

        final FakeAnalyticsPreferencesProvider analyticsPreferencesProvider = new FakeAnalyticsPreferencesProvider(isAnalyticsEnabled, null);

        final FakeGcmUnregistrationApiRequest gcmUnregistrationApiRequest = new FakeGcmUnregistrationApiRequest(gcmProvider);
        final GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider = new GcmUnregistrationApiRequestProvider(gcmUnregistrationApiRequest);
        final FakeBackEndUnregisterDeviceApiRequest dummyBackEndUnregisterDeviceApiRequest = new FakeBackEndUnregisterDeviceApiRequest(shouldBackEndDeviceUnregistrationBeSuccessful);
        final BackEndUnregisterDeviceApiRequestProvider backEndUnregisterDeviceApiRequestProvider = new BackEndUnregisterDeviceApiRequestProvider(dummyBackEndUnregisterDeviceApiRequest);
        final FakeServiceStarter serviceStarter = new FakeServiceStarter();
        final UnregistrationEngine engine = new UnregistrationEngine(context, gcmProvider, serviceStarter, pushPreferencesProvider, analyticsPreferencesProvider, gcmUnregistrationApiRequestProvider, backEndUnregisterDeviceApiRequestProvider);

        engine.unregisterDevice(parametersFromUser, new UnregistrationListener() {

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

        AndroidTestCase.assertTrue(delayedLoop.isSuccess());

        if (shouldGcmDeviceUnregistrationBeSuccessful) {
            AndroidTestCase.assertNull(pushPreferencesProvider.getGcmDeviceRegistrationId());
            AndroidTestCase.assertNull(pushPreferencesProvider.getGcmSenderId());
            AndroidTestCase.assertEquals(-1, pushPreferencesProvider.getAppVersion());
        } else {
            AndroidTestCase.assertNotNull(pushPreferencesProvider.getGcmDeviceRegistrationId());
            AndroidTestCase.assertNotNull(pushPreferencesProvider.getGcmSenderId());
            MoreAsserts.assertNotEqual(-1, pushPreferencesProvider.getAppVersion());
        }

        if (backEndDeviceRegistrationIdResultant == null) {
            AndroidTestCase.assertNull(pushPreferencesProvider.getBackEndDeviceRegistrationId());
            AndroidTestCase.assertNull(pushPreferencesProvider.getDeviceAlias());
            AndroidTestCase.assertNull(pushPreferencesProvider.getVariantSecret());
            AndroidTestCase.assertNull(pushPreferencesProvider.getVariantSecret());
            AndroidTestCase.assertNull(pushPreferencesProvider.getBaseServerUrl());
        } else {
            AndroidTestCase.assertNotNull(pushPreferencesProvider.getBackEndDeviceRegistrationId());
            AndroidTestCase.assertNotNull(pushPreferencesProvider.getDeviceAlias());
            AndroidTestCase.assertNotNull(pushPreferencesProvider.getVariantSecret());
            AndroidTestCase.assertNotNull(pushPreferencesProvider.getVariantSecret());
            AndroidTestCase.assertNotNull(pushPreferencesProvider.getBaseServerUrl());
        }

        AndroidTestCase.assertNull(pushPreferencesProvider.getPackageName());
        AndroidTestCase.assertEquals(shouldBackEndUnregisterHaveBeenCalled, dummyBackEndUnregisterDeviceApiRequest.wasUnregisterCalled());
        AndroidTestCase.assertFalse(gcmProvider.wasRegisterCalled());
        AndroidTestCase.assertTrue(gcmProvider.wasUnregisterCalled());

        if (isAnalyticsEnabled && shouldPushUnregisteredEventHaveBeenLogged) {

            AndroidTestCase.assertTrue(serviceStarter.wasStarted());
            final Intent intent = serviceStarter.getStartedIntent();
            final EnqueueEventJob job = intent.getParcelableExtra(EventService.KEY_JOB);
            final Event event = job.getEvent();
            AndroidTestCase.assertEquals(EventPushUnregistered.EVENT_TYPE, event.getEventType());
            final HashMap<String, Object> data = event.getData();
            AndroidTestCase.assertEquals(startingVariantUuidInPrefs, data.get(PushEventHelper.VARIANT_UUID));
            AndroidTestCase.assertEquals(startingBackEndDeviceRegistrationIdInPrefs, data.get(PushEventHelper.DEVICE_ID));

        } else {
            AndroidTestCase.assertFalse(serviceStarter.wasStarted());
        }
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
        startingBackEndDeviceRegistrationIdInPrefs = inPrefs;
        backEndDeviceRegistrationIdResultant = resultantValue;
        shouldBackEndDeviceUnregistrationBeSuccessful = (resultantValue == null);
        shouldBackEndUnregisterHaveBeenCalled = (inPrefs != null);
        return this;
    }

    public UnregistrationEngineTestParameters setupParameters(RegistrationParameters parameters) {
        parametersFromUser = parameters;
        return this;
    }

}
