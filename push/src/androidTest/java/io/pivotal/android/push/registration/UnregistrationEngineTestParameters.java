/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.registration;

import android.content.Context;
import android.content.pm.PackageManager;
import android.test.AndroidTestCase;
import android.test.MoreAsserts;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import io.pivotal.android.push.PushParameters;
import io.pivotal.android.push.backend.api.FakePCFPushUnregisterDeviceApiRequest;
import io.pivotal.android.push.backend.api.PCFPushUnregisterDeviceApiRequestProvider;
import io.pivotal.android.push.gcm.FakeGcmProvider;
import io.pivotal.android.push.gcm.FakeGcmUnregistrationApiRequest;
import io.pivotal.android.push.gcm.GcmUnregistrationApiRequestProvider;
import io.pivotal.android.push.geofence.GeofenceEngine;
import io.pivotal.android.push.geofence.GeofenceStatusUtil;
import io.pivotal.android.push.geofence.GeofenceUpdater;
import io.pivotal.android.push.prefs.FakePushPreferencesProvider;
import io.pivotal.android.push.util.DelayedLoop;
import io.pivotal.android.push.version.GeofenceStatus;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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
    private static final Set<String> TAGS_IN_PREFS = new HashSet<>();

    private final Context context;
    private final DelayedLoop delayedLoop;
    private boolean shouldGcmDeviceUnregistrationBeSuccessful;
    private boolean shouldUnregistrationHaveSucceeded;
    private boolean shouldPCFPushDeviceUnregistrationBeSuccessful;
    private String startingPCFPushDeviceRegistrationIdInPrefs;
    private String pcfPushDeviceRegistrationIdResultant;
    private boolean shouldPCFPushUnregisterHaveBeenCalled;
    private boolean areGeofencesEnabledInPrefs;
    private PushParameters parametersFromUser;
    private long lastGeofenceUpdateTimeInPrefs;
    private boolean shouldClearGeofencesFromMonitorAndStoreBeCalled;
    private boolean shouldClearGeofencesFromStoreOnlyBeCalled;
    private boolean shouldClearGeofencesBeSuccessful;
    private boolean shouldHavePermissionForGeofences = true;

    public UnregistrationEngineTestParameters() {
        this.context = mock(Context.class);
        delayedLoop = new DelayedLoop(TEN_SECOND_TIMEOUT);
        TAGS_IN_PREFS.addAll(Arrays.asList("BANANAS", "PAPAYAS"));
    }

    public void run() throws Exception {

        final FakeGcmProvider gcmProvider = new FakeGcmProvider(null, true, !shouldGcmDeviceUnregistrationBeSuccessful);
        final FakePushPreferencesProvider pushPreferencesProvider;
        final GeofenceUpdater geofenceUpdater = mock(GeofenceUpdater.class);
        final GeofenceStatusUtil geofenceStatusUtil = mock(GeofenceStatusUtil.class);

        if (shouldHavePermissionForGeofences) {
            when(context.checkCallingOrSelfPermission(anyString())).thenReturn(PackageManager.PERMISSION_GRANTED);
        } else {
            when(context.checkCallingOrSelfPermission(anyString())).thenReturn(PackageManager.PERMISSION_DENIED);
        }

        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                final GeofenceUpdater.GeofenceUpdaterListener listener = (GeofenceUpdater.GeofenceUpdaterListener) invocation.getArguments()[0];
                if (shouldClearGeofencesBeSuccessful) {
                    listener.onSuccess();
                } else {
                    listener.onFailure("Fake clear geofences failed fakely.");
                }
                return null;
            }

        }).when(geofenceUpdater).clearGeofencesFromMonitorAndStore(any(GeofenceUpdater.GeofenceUpdaterListener.class));

        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                final GeofenceUpdater.GeofenceUpdaterListener listener = (GeofenceUpdater.GeofenceUpdaterListener) invocation.getArguments()[0];
                if (shouldClearGeofencesBeSuccessful) {
                    listener.onSuccess();
                } else {
                    listener.onFailure("Fake clear failed fakely"); // TODO - note that clear geofences doesn't fail
                }
                return null;
            }

        }).when(geofenceUpdater).clearGeofencesFromStoreOnly(any(GeofenceUpdater.GeofenceUpdaterListener.class));

        if (startingPCFPushDeviceRegistrationIdInPrefs == null) {
            pushPreferencesProvider = new FakePushPreferencesProvider(null, startingPCFPushDeviceRegistrationIdInPrefs, -1, null, null, null, null, null, null, null, lastGeofenceUpdateTimeInPrefs, areGeofencesEnabledInPrefs);
        } else {
            pushPreferencesProvider = new FakePushPreferencesProvider(GCM_DEVICE_ID_IN_PREFS,
                    startingPCFPushDeviceRegistrationIdInPrefs,
                    APP_VERSION_IN_PREFS,
                    GCM_SENDER_ID_IN_PREFS,
                    PLATFORM_UUID_IN_PREFS,
                    PLATFORM_SECRET_IN_PREFS,
                    DEVICE_ALIAS_IN_PREFS,
                    PACKAGE_NAME_IN_PREFS,
                    SERVICE_URL_IN_PREFS,
                    TAGS_IN_PREFS,
                    lastGeofenceUpdateTimeInPrefs,
                    areGeofencesEnabledInPrefs);
        }

        final FakeGcmUnregistrationApiRequest gcmUnregistrationApiRequest = new FakeGcmUnregistrationApiRequest(gcmProvider);
        final GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider = new GcmUnregistrationApiRequestProvider(gcmUnregistrationApiRequest);
        final FakePCFPushUnregisterDeviceApiRequest fakePCFPushUnregisterDeviceApiRequest = new FakePCFPushUnregisterDeviceApiRequest(shouldPCFPushDeviceUnregistrationBeSuccessful);
        final PCFPushUnregisterDeviceApiRequestProvider PCFPushUnregisterDeviceApiRequestProvider = new PCFPushUnregisterDeviceApiRequestProvider(fakePCFPushUnregisterDeviceApiRequest);
        final UnregistrationEngine engine = new UnregistrationEngine(context, gcmProvider, pushPreferencesProvider, gcmUnregistrationApiRequestProvider, PCFPushUnregisterDeviceApiRequestProvider, geofenceUpdater, geofenceStatusUtil);

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

        if (shouldClearGeofencesFromMonitorAndStoreBeCalled) {
            if (shouldClearGeofencesBeSuccessful) {

                AndroidTestCase.assertEquals(GeofenceEngine.NEVER_UPDATED_GEOFENCES, pushPreferencesProvider.getLastGeofenceUpdate());
                AndroidTestCase.assertFalse(pushPreferencesProvider.areGeofencesEnabled());
            } else { // clear geofences not successful
                AndroidTestCase.assertEquals(lastGeofenceUpdateTimeInPrefs, pushPreferencesProvider.getLastGeofenceUpdate());
                AndroidTestCase.assertTrue(pushPreferencesProvider.areGeofencesEnabled());
            }
            verify(geofenceUpdater, times(1)).clearGeofencesFromMonitorAndStore(any(GeofenceUpdater.GeofenceUpdaterListener.class));

        } else if (shouldClearGeofencesFromStoreOnlyBeCalled) {
            if (shouldClearGeofencesBeSuccessful) {
                AndroidTestCase.assertEquals(GeofenceEngine.NEVER_UPDATED_GEOFENCES, pushPreferencesProvider.getLastGeofenceUpdate());
                AndroidTestCase.assertFalse(pushPreferencesProvider.areGeofencesEnabled());
            } else { // clear geofences not successful
                AndroidTestCase.assertEquals(lastGeofenceUpdateTimeInPrefs, pushPreferencesProvider.getLastGeofenceUpdate());
                AndroidTestCase.assertTrue(pushPreferencesProvider.areGeofencesEnabled());
            }
            verify(geofenceStatusUtil, times(1)).saveGeofenceStatusAndSendBroadcast(any(GeofenceStatus.class));
            verify(geofenceUpdater, times(1)).clearGeofencesFromStoreOnly(any(GeofenceUpdater.GeofenceUpdaterListener.class));

        } else { // clear geofences should not have been called (i.e.: geofences are not enabled)
            AndroidTestCase.assertEquals(GeofenceEngine.NEVER_UPDATED_GEOFENCES, pushPreferencesProvider.getLastGeofenceUpdate());
            AndroidTestCase.assertFalse(pushPreferencesProvider.areGeofencesEnabled());
            verify(geofenceUpdater, never()).clearGeofencesFromMonitorAndStore(any(GeofenceUpdater.GeofenceUpdaterListener.class));
        }

        AndroidTestCase.assertNull(pushPreferencesProvider.getPackageName());
        AndroidTestCase.assertEquals(shouldPCFPushUnregisterHaveBeenCalled, fakePCFPushUnregisterDeviceApiRequest.wasUnregisterCalled());
        AndroidTestCase.assertFalse(gcmProvider.wasRegisterCalled());
        AndroidTestCase.assertTrue(gcmProvider.wasUnregisterCalled());
        verifyNoMoreInteractions(geofenceUpdater);
        verifyNoMoreInteractions(geofenceStatusUtil);
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

    public UnregistrationEngineTestParameters setupParameters(PushParameters parameters) {
        parametersFromUser = parameters;
        return this;
    }

    public UnregistrationEngineTestParameters setupGeofences(long lastGeofenceUpdateTime, boolean areGeofencesEnabled, boolean shouldClearGeofencesFromMonitorAndStoreBeCalled, boolean shouldClearGeofencesFromStoreOnlyBeCalled, boolean shouldClearGeofencesBeSuccessful) {
        this.lastGeofenceUpdateTimeInPrefs = lastGeofenceUpdateTime;
        this.areGeofencesEnabledInPrefs = areGeofencesEnabled;
        this.shouldClearGeofencesFromMonitorAndStoreBeCalled = shouldClearGeofencesFromMonitorAndStoreBeCalled;
        this.shouldClearGeofencesFromStoreOnlyBeCalled = shouldClearGeofencesFromStoreOnlyBeCalled;
        this.shouldClearGeofencesBeSuccessful = shouldClearGeofencesBeSuccessful;
        return this;
    }

    public UnregistrationEngineTestParameters setShouldHavePermissionForGeofences(boolean b) {
        shouldHavePermissionForGeofences = b;
        return this;
    }
}