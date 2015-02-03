/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.registration;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.MoreAsserts;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import io.pivotal.android.push.RegistrationParameters;
import io.pivotal.android.push.backend.FakePCFPushUnregisterDeviceApiRequest;
import io.pivotal.android.push.backend.PCFPushUnregisterDeviceApiRequestProvider;
import io.pivotal.android.push.gcm.FakeGcmProvider;
import io.pivotal.android.push.gcm.FakeGcmUnregistrationApiRequest;
import io.pivotal.android.push.gcm.GcmUnregistrationApiRequestProvider;
import io.pivotal.android.push.prefs.FakePushPreferencesProvider;
import io.pivotal.android.push.util.DelayedLoop;

public class UnregistrationEngineTestParameters {

    private static final long TEN_SECOND_TIMEOUT = 10000L;
    private static final String GCM_SENDER_ID_IN_PREFS = "GCM SENDER ID";
    private static final String PLATFORM_UUID_IN_PREFS = "VARIANT UUID";
    private static final String PLATFORM_SECRET_IN_PREFS = "VARIANT SECRET";
    private static final String DEVICE_ALIAS_IN_PREFS = "DEVICE ALIAS";
    private static final int APP_VERSION_IN_PREFS = 99;
    private static final String GCM_DEVICE_ID_IN_PREFS = "GCM DEVICE ID";
    private static final String PACKAGE_NAME_IN_PREFS = "PACKAGE.NAME";
    private static final String SERVICE_URL_IN_PREFS = "http://test.com";
    private static final Set<String> TAGS_IN_PREFS = new HashSet<String>();

    private final Context context;
    private final DelayedLoop delayedLoop;
    private boolean shouldGcmDeviceUnregistrationBeSuccessful;
    private boolean shouldUnregistrationHaveSucceeded;
    private boolean shouldPCFPushDeviceUnregistrationBeSuccessful;
    private String startingPCFPushDeviceRegistrationIdInPrefs;
    private String pcfPushDeviceRegistrationIdResultant;
    private boolean shouldPCFPushUnregisterHaveBeenCalled;
    private RegistrationParameters parametersFromUser;

    public UnregistrationEngineTestParameters(Context context) {
        this.context = context;
        delayedLoop = new DelayedLoop(TEN_SECOND_TIMEOUT);
        TAGS_IN_PREFS.addAll(Arrays.asList("BANANAS", "PAPAYAS"));
    }

    public void run() throws Exception {

        final FakeGcmProvider gcmProvider = new FakeGcmProvider(null, true, !shouldGcmDeviceUnregistrationBeSuccessful);
        final FakePushPreferencesProvider pushPreferencesProvider;

        if (startingPCFPushDeviceRegistrationIdInPrefs == null) {
            pushPreferencesProvider = new FakePushPreferencesProvider(null, startingPCFPushDeviceRegistrationIdInPrefs, -1, null, null, null, null, null, null, null);
        } else {
            pushPreferencesProvider = new FakePushPreferencesProvider(GCM_DEVICE_ID_IN_PREFS, startingPCFPushDeviceRegistrationIdInPrefs, APP_VERSION_IN_PREFS, GCM_SENDER_ID_IN_PREFS, PLATFORM_UUID_IN_PREFS, PLATFORM_SECRET_IN_PREFS, DEVICE_ALIAS_IN_PREFS, PACKAGE_NAME_IN_PREFS, SERVICE_URL_IN_PREFS, TAGS_IN_PREFS);
        }

        final FakeGcmUnregistrationApiRequest gcmUnregistrationApiRequest = new FakeGcmUnregistrationApiRequest(gcmProvider);
        final GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider = new GcmUnregistrationApiRequestProvider(gcmUnregistrationApiRequest);
        final FakePCFPushUnregisterDeviceApiRequest fakePCFPushUnregisterDeviceApiRequest = new FakePCFPushUnregisterDeviceApiRequest(shouldPCFPushDeviceUnregistrationBeSuccessful);
        final PCFPushUnregisterDeviceApiRequestProvider PCFPushUnregisterDeviceApiRequestProvider = new PCFPushUnregisterDeviceApiRequestProvider(fakePCFPushUnregisterDeviceApiRequest);
        final UnregistrationEngine engine = new UnregistrationEngine(context, gcmProvider, pushPreferencesProvider, gcmUnregistrationApiRequestProvider, PCFPushUnregisterDeviceApiRequestProvider);

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

        if (pcfPushDeviceRegistrationIdResultant == null) {
            AndroidTestCase.assertNull(pushPreferencesProvider.getPCFPushDeviceRegistrationId());
            AndroidTestCase.assertNull(pushPreferencesProvider.getDeviceAlias());
            AndroidTestCase.assertNull(pushPreferencesProvider.getPlatformSecret());
            AndroidTestCase.assertNull(pushPreferencesProvider.getPlatformSecret());
            AndroidTestCase.assertNull(pushPreferencesProvider.getServiceUrl());
            AndroidTestCase.assertNull(pushPreferencesProvider.getTags());
        } else {
            AndroidTestCase.assertNotNull(pushPreferencesProvider.getPCFPushDeviceRegistrationId());
            AndroidTestCase.assertNotNull(pushPreferencesProvider.getDeviceAlias());
            AndroidTestCase.assertNotNull(pushPreferencesProvider.getPlatformSecret());
            AndroidTestCase.assertNotNull(pushPreferencesProvider.getPlatformSecret());
            AndroidTestCase.assertNotNull(pushPreferencesProvider.getServiceUrl());
            AndroidTestCase.assertNotNull(pushPreferencesProvider.getTags());
        }

        AndroidTestCase.assertNull(pushPreferencesProvider.getPackageName());
        AndroidTestCase.assertEquals(shouldPCFPushUnregisterHaveBeenCalled, fakePCFPushUnregisterDeviceApiRequest.wasUnregisterCalled());
        AndroidTestCase.assertFalse(gcmProvider.wasRegisterCalled());
        AndroidTestCase.assertTrue(gcmProvider.wasUnregisterCalled());
    }

    public UnregistrationEngineTestParameters setShouldUnregistrationHaveSucceeded(boolean b) {
        shouldUnregistrationHaveSucceeded = b;
        return this;
    }

    public UnregistrationEngineTestParameters setShouldGcmDeviceUnregistrationBeSuccessful(boolean b) {
        shouldGcmDeviceUnregistrationBeSuccessful = b;
        return this;
    }

    public UnregistrationEngineTestParameters setupPCFPushDeviceRegistrationId(String inPrefs, String resultantValue) {
        startingPCFPushDeviceRegistrationIdInPrefs = inPrefs;
        pcfPushDeviceRegistrationIdResultant = resultantValue;
        shouldPCFPushDeviceUnregistrationBeSuccessful = (resultantValue == null);
        shouldPCFPushUnregisterHaveBeenCalled = (inPrefs != null);
        return this;
    }

    public UnregistrationEngineTestParameters setupParameters(RegistrationParameters parameters) {
        parametersFromUser = parameters;
        return this;
    }

}