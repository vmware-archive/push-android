/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.registration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.pm.PackageManager;
import io.pivotal.android.push.PushParameters;
import io.pivotal.android.push.backend.api.FakePCFPushUnregisterDeviceApiRequest;
import io.pivotal.android.push.backend.api.PCFPushUnregisterDeviceApiRequestProvider;
import io.pivotal.android.push.geofence.GeofenceConstants;
import io.pivotal.android.push.geofence.GeofenceEngine;
import io.pivotal.android.push.geofence.GeofenceStatusUtil;
import io.pivotal.android.push.geofence.GeofenceUpdater;
import io.pivotal.android.push.prefs.PushPreferencesFCM;
import io.pivotal.android.push.util.DelayedLoop;
import io.pivotal.android.push.version.GeofenceStatus;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class UnregistrationEngineTestParameters {

    private static final long TEN_SECOND_TIMEOUT = 10000L;
    private static final String PLATFORM_UUID_IN_PREFS = "VARIANT UUID";
    private static final String PLATFORM_SECRET_IN_PREFS = "VARIANT SECRET";
    private static final String DEVICE_ALIAS_IN_PREFS = "DEVICE ALIAS";
    private static final String CUSTOM_USER_ID_IN_PREFS = "CUSTOM USER ID";
    private static final String FCM_TOKEN_ID_IN_PREFS = "FCM TOKEN ID";
    private static final String PACKAGE_NAME_IN_PREFS = "PACKAGE.NAME";
    private static final String SERVICE_URL_IN_PREFS = "http://test.com";
    private static final Set<String> TAGS_IN_PREFS = new HashSet<>();

    private final Context context;
    private final DelayedLoop delayedLoop;
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

        final PushPreferencesFCM pushPreferences;
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

        pushPreferences = mock(PushPreferencesFCM.class);

        if (startingPCFPushDeviceRegistrationIdInPrefs == null) {
            when(pushPreferences.getPCFPushDeviceRegistrationId()).thenReturn(startingPCFPushDeviceRegistrationIdInPrefs);
            when(pushPreferences.getLastGeofenceUpdate()).thenReturn(lastGeofenceUpdateTimeInPrefs);
            when(pushPreferences.areGeofencesEnabled()).thenReturn(areGeofencesEnabledInPrefs);

        } else {
            when(pushPreferences.getFcmTokenId()).thenReturn(FCM_TOKEN_ID_IN_PREFS);
            when(pushPreferences.getPCFPushDeviceRegistrationId()).thenReturn(startingPCFPushDeviceRegistrationIdInPrefs);
            when(pushPreferences.getPlatformUuid()).thenReturn(PLATFORM_UUID_IN_PREFS);
            when(pushPreferences.getPlatformSecret()).thenReturn(PLATFORM_SECRET_IN_PREFS);
            when(pushPreferences.getPCFPushDeviceRegistrationId()).thenReturn(DEVICE_ALIAS_IN_PREFS);
            when(pushPreferences.getCustomUserId()).thenReturn(CUSTOM_USER_ID_IN_PREFS);
            when(pushPreferences.getPackageName()).thenReturn(PACKAGE_NAME_IN_PREFS);
            when(pushPreferences.getServiceUrl()).thenReturn(SERVICE_URL_IN_PREFS);
            when(pushPreferences.getTags()).thenReturn(TAGS_IN_PREFS);
            when(pushPreferences.getLastGeofenceUpdate()).thenReturn(lastGeofenceUpdateTimeInPrefs);
            when(pushPreferences.areGeofencesEnabled()).thenReturn(areGeofencesEnabledInPrefs);
        }

        final FakePCFPushUnregisterDeviceApiRequest fakePCFPushUnregisterDeviceApiRequest = new FakePCFPushUnregisterDeviceApiRequest(shouldPCFPushDeviceUnregistrationBeSuccessful);
        final PCFPushUnregisterDeviceApiRequestProvider PCFPushUnregisterDeviceApiRequestProvider = new PCFPushUnregisterDeviceApiRequestProvider(fakePCFPushUnregisterDeviceApiRequest);
        final UnregistrationEngine engine = new UnregistrationEngine(context, pushPreferences, PCFPushUnregisterDeviceApiRequestProvider, geofenceUpdater, geofenceStatusUtil);

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

        assertTrue(delayedLoop.isSuccess());

        verify(pushPreferences).setPackageName(isNull(String.class));
        verify(pushPreferences).setFcmTokenId(isNull(String.class));

        if (pcfPushDeviceRegistrationIdResultant == null) {
            verify(pushPreferences).setPCFPushDeviceRegistrationId(isNull(String.class));
            verify(pushPreferences).setDeviceAlias(isNull(String.class));
            verify(pushPreferences).setPlatformUuid(isNull(String.class));
            verify(pushPreferences).setPlatformSecret(isNull(String.class));
            verify(pushPreferences).setServiceUrl(isNull(String.class));
            verify(pushPreferences).setTags(isNull(Set.class));
        } else {
            // A failed unregistration should not modify existing stored preferences
            verify(pushPreferences, never()).setPCFPushDeviceRegistrationId(anyString());
            verify(pushPreferences, never()).setDeviceAlias(anyString());
            verify(pushPreferences, never()).setPlatformUuid(anyString());
            verify(pushPreferences, never()).setPlatformSecret(anyString());
            verify(pushPreferences, never()).setServiceUrl(anyString());
            verify(pushPreferences, never()).setTags(any(Set.class));
        }

        if (shouldClearGeofencesFromMonitorAndStoreBeCalled) {
            if (shouldClearGeofencesBeSuccessful) {
                verify(pushPreferences).setLastGeofenceUpdate(GeofenceConstants.NEVER_UPDATED_GEOFENCES);
                verify(pushPreferences).setAreGeofencesEnabled(eq(false));
            } else { // clear geofences not successful
                verify(pushPreferences, never()).setLastGeofenceUpdate(anyLong());
                verify(pushPreferences, never()).setAreGeofencesEnabled(anyBoolean());
            }
            verify(geofenceUpdater, times(1)).clearGeofencesFromMonitorAndStore(any(GeofenceUpdater.GeofenceUpdaterListener.class));

        } else if (shouldClearGeofencesFromStoreOnlyBeCalled) {
            if (shouldClearGeofencesBeSuccessful) {
                verify(pushPreferences).setLastGeofenceUpdate(GeofenceConstants.NEVER_UPDATED_GEOFENCES);
                verify(pushPreferences).setAreGeofencesEnabled(eq(false));
            } else { // clear geofences not successful
                verify(pushPreferences, never()).setLastGeofenceUpdate(anyLong());
                verify(pushPreferences, never()).setAreGeofencesEnabled(anyBoolean());
            }
            verify(geofenceStatusUtil, times(1)).saveGeofenceStatusAndSendBroadcast(any(GeofenceStatus.class));
            verify(geofenceUpdater, times(1)).clearGeofencesFromStoreOnly(any(GeofenceUpdater.GeofenceUpdaterListener.class));

        } else { // clear geofences should not have been called (i.e.: geofences are not enabled)
            verify(pushPreferences, never()).setLastGeofenceUpdate(anyLong());
            verify(pushPreferences, never()).setAreGeofencesEnabled(anyBoolean());
            verify(geofenceUpdater, never()).clearGeofencesFromMonitorAndStore(any(GeofenceUpdater.GeofenceUpdaterListener.class));
        }

        assertEquals(shouldPCFPushUnregisterHaveBeenCalled, fakePCFPushUnregisterDeviceApiRequest.wasUnregisterCalled());
        verifyNoMoreInteractions(geofenceUpdater);
        verifyNoMoreInteractions(geofenceStatusUtil);
    }

    public UnregistrationEngineTestParameters setShouldUnregistrationHaveSucceeded(boolean b) {
        shouldUnregistrationHaveSucceeded = b;
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