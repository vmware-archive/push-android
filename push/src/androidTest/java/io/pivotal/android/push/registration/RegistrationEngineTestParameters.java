/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.registration;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.test.AndroidTestCase;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.iid.FirebaseInstanceId;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Set;

import io.pivotal.android.push.PushParameters;
import io.pivotal.android.push.backend.api.FakePCFPushRegistrationApiRequest;
import io.pivotal.android.push.backend.api.PCFPushRegistrationApiRequestProvider;
import io.pivotal.android.push.geofence.GeofenceEngine;
import io.pivotal.android.push.geofence.GeofenceStatusUtil;
import io.pivotal.android.push.geofence.GeofenceUpdater;
import io.pivotal.android.push.prefs.FakePushPreferencesProvider;
import io.pivotal.android.push.prefs.FakePushRequestHeaders;
import io.pivotal.android.push.prefs.Pivotal;
import io.pivotal.android.push.util.DelayedLoop;
import io.pivotal.android.push.version.GeofenceStatus;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RegistrationEngineTestParameters {

    private static final long TEN_SECOND_TIMEOUT = 10000L;

    private final Context context;
    private final DelayedLoop delayedLoop;

    private String fcmTokenIdInPrefs = null;
    private String fcmTokenIdFromServer = null;
    private String pcfPushDeviceRegistrationIdInPrefs = null;
    private String pcfPushDeviceRegistrationIdFromServer;
    private String platformUuidInPrefs = null;
    private String platformUuidFromUser = null;
    private String platformSecretInPrefs = null;
    private String platformSecretFromUser = "S";
    private String deviceAliasInPrefs = null;
    private String deviceAliasFromUser = "S";
    private String customUserIdInPrefs = null;
    private String customUserIdFromUser = null;
    private String serviceUrlInPrefs = null;
    private String serviceUrlFromUser = null;
    private String packageNameInPrefs = null;
    private String packageNameFromUser = ".";
    private String finalFcmTokenIdInPrefs = null;
    private String finalPCFPushDeviceRegistrationIdInPrefs = null;
    private String finalPlatformUuidInPrefs = null;
    private String finalPlatformSecretInPrefs = null;
    private String finalDeviceAliasInPrefs = null;
    private String finalCustomUserIdInPrefs = null;
    private String finalPackageNameInPrefs = null;
    private String finalServiceUrlInPrefs = null;
    private Set<String> tagsFromUser = null;
    private Set<String> tagsInPrefs = null;
    private Set<String> finalTagsInPrefs = null;
    private boolean areGeofencesEnabledInPrefs;
    private boolean areGeofencesEnabledFromUser = true;
    private boolean finalAreGeofencesEnabled = true;
    private boolean shouldFcmTokenIdHaveBeenSaved = false;
    private boolean shouldGeofenceUpdateTimestampedHaveBeenCalled = false;
    private boolean shouldPCFPushDeviceRegistrationHaveBeenSaved = false;
    private boolean shouldPlatformUuidHaveBeenSaved = false;
    private boolean shouldPlatformSecretHaveBeenSaved = false;
    private boolean shouldDeviceAliasHaveBeenSaved = false;
    private boolean shouldCustomUserIdHaveBeenSaved = false;
    private boolean shouldTagsHaveBeenSaved = false;
    private boolean shouldPCFPushDeviceRegistrationBeSuccessful = false;
    private boolean shouldPCFPushNewRegistrationHaveBeenCalled = false;
    private boolean shouldPCFPushUpdateRegistrationHaveBeenCalled = false;
    private boolean shouldPackageNameHaveBeenSaved = false;
    private boolean shouldServiceUrlHaveBeenSaved = false;
    private boolean shouldRegistrationHaveSucceeded = true;
    private boolean shouldGeofenceUpdateBeSuccessful = true;
    private boolean shouldAreGeofencesEnabledHaveBeenSaved = false;
    private boolean shouldClearGeofencesFromMonitorAndStoreHaveBeenCalled = false;
    private boolean shouldClearGeofencesFromStoreOnlyHaveBeenCalled = false;
    private boolean shouldHavePermissionForGeofences = true;
    private boolean wasGeofenceUpdateTimestampCalled = false;
    private boolean wasClearGeofencesFromMonitorAndStoreCalled = false;
    private boolean wasClearGeofencesFromStoreOnlyCalled = false;
    private boolean wasGeofenceStatusUpdated = false;
    private int numberOfGeofenceReregistrations = 0;

    private long geofenceUpdateTimestampInPrefs = 0L;
    private long geofenceUpdateTimestampToServer = 0L;
    private long geofenceUpdateTimestampFromServer = 0L;
    private long finalGeofenceUpdateTimestampInPrefs = 0L;

    public RegistrationEngineTestParameters() {
        this.context = mock(Context.class);
        delayedLoop = new DelayedLoop(TEN_SECOND_TIMEOUT);
    }

    public void run() {

        if (shouldHavePermissionForGeofences) {
            when(context.checkCallingOrSelfPermission(anyString())).thenReturn(PackageManager.PERMISSION_GRANTED);
        } else {
            when(context.checkCallingOrSelfPermission(anyString())).thenReturn(PackageManager.PERMISSION_DENIED);
        }

        final FakePushPreferencesProvider pushPreferencesProvider = new FakePushPreferencesProvider(fcmTokenIdInPrefs, pcfPushDeviceRegistrationIdInPrefs, platformUuidInPrefs, platformSecretInPrefs, deviceAliasInPrefs, customUserIdInPrefs, packageNameInPrefs, serviceUrlInPrefs, tagsInPrefs, geofenceUpdateTimestampInPrefs, areGeofencesEnabledInPrefs);
        final FakePushRequestHeaders pushRequestHeaders = new FakePushRequestHeaders();
        final FakePCFPushRegistrationApiRequest fakePCFPushRegistrationApiRequest = new FakePCFPushRegistrationApiRequest(pcfPushDeviceRegistrationIdFromServer, shouldPCFPushDeviceRegistrationBeSuccessful);
        final PCFPushRegistrationApiRequestProvider PCFPushRegistrationApiRequestProvider = new PCFPushRegistrationApiRequestProvider(fakePCFPushRegistrationApiRequest);
        final GeofenceUpdater geofenceUpdater = mock(GeofenceUpdater.class);
        final GeofenceEngine geofenceEngine = mock(GeofenceEngine.class);
        final GeofenceStatusUtil geofenceStatusUtil = mock(GeofenceStatusUtil.class);
        final FirebaseInstanceId firebaseInstanceId = mock(FirebaseInstanceId.class);
        when(firebaseInstanceId.getToken()).thenReturn(fcmTokenIdFromServer);
        final GoogleApiAvailability googleApiAvailability = mock(GoogleApiAvailability.class);
        when(googleApiAvailability.isGooglePlayServicesAvailable(context)).thenReturn(ConnectionResult.SUCCESS);

        final RegistrationEngine engine = new RegistrationEngine(context, packageNameFromUser, firebaseInstanceId, googleApiAvailability, pushPreferencesProvider, pushRequestHeaders, PCFPushRegistrationApiRequestProvider, geofenceUpdater, geofenceEngine, geofenceStatusUtil);
        final PushParameters parameters = new PushParameters(platformUuidFromUser, platformSecretFromUser, serviceUrlFromUser, deviceAliasFromUser, customUserIdFromUser, tagsFromUser, areGeofencesEnabledFromUser, true, Pivotal.SslCertValidationMode.DEFAULT, null, null);

        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                wasGeofenceUpdateTimestampCalled = true;
                final GeofenceUpdater.GeofenceUpdaterListener listener = (GeofenceUpdater.GeofenceUpdaterListener) invocation.getArguments()[2];
                if (shouldGeofenceUpdateBeSuccessful) {
                    pushPreferencesProvider.setLastGeofenceUpdate(geofenceUpdateTimestampFromServer);
                    listener.onSuccess();
                }else {
                    listener.onFailure("Fake request failed fakely");
                }
                return null;
            }

        }).when(geofenceUpdater).startGeofenceUpdate(any(Intent.class), eq(geofenceUpdateTimestampToServer), any(GeofenceUpdater.GeofenceUpdaterListener.class));

        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                wasClearGeofencesFromMonitorAndStoreCalled = true;
                final GeofenceUpdater.GeofenceUpdaterListener listener = (GeofenceUpdater.GeofenceUpdaterListener) invocation.getArguments()[0];
                if (shouldGeofenceUpdateBeSuccessful) {
                    listener.onSuccess();
                } else {
                    listener.onFailure("Fake clear failed fakely"); // TODO - note that clear geofences doesn't fail
                }
                return null;
            }

        }).when(geofenceUpdater).clearGeofencesFromMonitorAndStore(any(GeofenceUpdater.GeofenceUpdaterListener.class));

        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                wasClearGeofencesFromStoreOnlyCalled = true;
                final GeofenceUpdater.GeofenceUpdaterListener listener = (GeofenceUpdater.GeofenceUpdaterListener) invocation.getArguments()[0];
                if (shouldGeofenceUpdateBeSuccessful) {
                    listener.onSuccess();
                } else {
                    listener.onFailure("Fake clear failed fakely"); // TODO - note that clear geofences doesn't fail
                }
                return null;
            }

        }).when(geofenceUpdater).clearGeofencesFromStoreOnly(any(GeofenceUpdater.GeofenceUpdaterListener.class));

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                wasGeofenceStatusUpdated = true;
                return null;
            }
        }).when(geofenceStatusUtil).saveGeofenceStatusAndSendBroadcast(any(GeofenceStatus.class));

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
        AndroidTestCase.assertEquals(shouldPCFPushUpdateRegistrationHaveBeenCalled, fakePCFPushRegistrationApiRequest.isUpdateRegistration());
        AndroidTestCase.assertEquals(shouldPCFPushNewRegistrationHaveBeenCalled, fakePCFPushRegistrationApiRequest.isNewRegistration());
        AndroidTestCase.assertEquals(shouldPCFPushDeviceRegistrationHaveBeenSaved, pushPreferencesProvider.wasPCFPushDeviceRegistrationIdSaved());
        AndroidTestCase.assertEquals(shouldFcmTokenIdHaveBeenSaved, pushPreferencesProvider.wasFcmTokenIdSaved());
        AndroidTestCase.assertEquals(shouldPlatformUuidHaveBeenSaved, pushPreferencesProvider.wasPlatformUuidSaved());
        AndroidTestCase.assertEquals(shouldPlatformSecretHaveBeenSaved, pushPreferencesProvider.wasPlatformSecretSaved());
        AndroidTestCase.assertEquals(shouldDeviceAliasHaveBeenSaved, pushPreferencesProvider.wasDeviceAliasSaved());
        AndroidTestCase.assertEquals(shouldCustomUserIdHaveBeenSaved, pushPreferencesProvider.wasCustomUserIdSaved());
        AndroidTestCase.assertEquals(shouldPackageNameHaveBeenSaved, pushPreferencesProvider.isWasPackageNameSaved());
        AndroidTestCase.assertEquals(shouldServiceUrlHaveBeenSaved, pushPreferencesProvider.wasServiceUrlSaved());
        AndroidTestCase.assertEquals(shouldTagsHaveBeenSaved, pushPreferencesProvider.wereTagsSaved());
        AndroidTestCase.assertEquals(shouldAreGeofencesEnabledHaveBeenSaved, pushPreferencesProvider.wasAreGeofencesEnabledSaved());
        AndroidTestCase.assertEquals(shouldGeofenceUpdateTimestampedHaveBeenCalled, wasGeofenceUpdateTimestampCalled);
        AndroidTestCase.assertEquals(shouldClearGeofencesFromMonitorAndStoreHaveBeenCalled, wasClearGeofencesFromMonitorAndStoreCalled);
        AndroidTestCase.assertEquals(shouldClearGeofencesFromStoreOnlyHaveBeenCalled, wasClearGeofencesFromStoreOnlyCalled);
        AndroidTestCase.assertEquals(shouldClearGeofencesFromStoreOnlyHaveBeenCalled, wasGeofenceStatusUpdated);
        AndroidTestCase.assertEquals(finalFcmTokenIdInPrefs, pushPreferencesProvider.getFcmTokenId());
        AndroidTestCase.assertEquals(finalPCFPushDeviceRegistrationIdInPrefs, pushPreferencesProvider.getPCFPushDeviceRegistrationId());
        AndroidTestCase.assertEquals(finalPlatformUuidInPrefs, pushPreferencesProvider.getPlatformUuid());
        AndroidTestCase.assertEquals(finalPlatformSecretInPrefs, pushPreferencesProvider.getPlatformSecret());
        AndroidTestCase.assertEquals(finalDeviceAliasInPrefs, pushPreferencesProvider.getDeviceAlias());
        AndroidTestCase.assertEquals(finalCustomUserIdInPrefs, pushPreferencesProvider.getCustomUserId());
        AndroidTestCase.assertEquals(finalServiceUrlInPrefs, pushPreferencesProvider.getServiceUrl());
        AndroidTestCase.assertEquals(finalPackageNameInPrefs, pushPreferencesProvider.getPackageName());
        AndroidTestCase.assertEquals(finalTagsInPrefs, pushPreferencesProvider.getTags());
        AndroidTestCase.assertEquals(finalGeofenceUpdateTimestampInPrefs, pushPreferencesProvider.getLastGeofenceUpdate());
        AndroidTestCase.assertEquals(finalAreGeofencesEnabled, pushPreferencesProvider.areGeofencesEnabled());
        verify(geofenceEngine, times(numberOfGeofenceReregistrations)).reregisterCurrentLocations(any(Set.class));
    }

    public RegistrationEngineTestParameters setupPackageName(String inPrefs, String fromUser, String finalValue) {
        packageNameInPrefs = inPrefs;
        packageNameFromUser = fromUser;
        finalPackageNameInPrefs = finalValue;
        shouldPackageNameHaveBeenSaved = true;
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

    public RegistrationEngineTestParameters setupCustomUserId(String inPrefs, String fromUser, String finalValue, boolean shouldHaveBeenSaved) {
        customUserIdInPrefs = inPrefs;
        customUserIdFromUser = fromUser;
        finalCustomUserIdInPrefs = finalValue;
        shouldCustomUserIdHaveBeenSaved = shouldHaveBeenSaved;
        return this;
    }

    public RegistrationEngineTestParameters setupServiceUrl(String inPrefs, String fromUser, String finalValue, boolean shouldHaveBeenSaved) {
        serviceUrlInPrefs = inPrefs;
        serviceUrlFromUser = fromUser;
        finalServiceUrlInPrefs = finalValue;
        shouldServiceUrlHaveBeenSaved = shouldHaveBeenSaved;
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

    public RegistrationEngineTestParameters setupFcmTokenId(String inPrefs, String fromServer, String finalValue) {
        fcmTokenIdInPrefs = inPrefs;
        fcmTokenIdFromServer = fromServer;
        finalFcmTokenIdInPrefs = finalValue;
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

    public RegistrationEngineTestParameters setupGeofenceUpdateTimestamp(long inPrefs,
                                                                         long toServer,
                                                                         long fromServer,
                                                                         long finalValue,
                                                                         boolean shouldBeSuccessful,
                                                                         boolean wasGeofenceUpdateCalled,
                                                                         boolean wasClearGeofencesFromMonitorAndStoreCalled,
                                                                         boolean wasClearGeofencesFromStoreOnlyCalled) {
        geofenceUpdateTimestampInPrefs = inPrefs;
        geofenceUpdateTimestampToServer = toServer;
        geofenceUpdateTimestampFromServer = fromServer;
        shouldGeofenceUpdateBeSuccessful = shouldBeSuccessful;
        finalGeofenceUpdateTimestampInPrefs = finalValue;
        shouldGeofenceUpdateTimestampedHaveBeenCalled = wasGeofenceUpdateCalled;
        shouldClearGeofencesFromMonitorAndStoreHaveBeenCalled = wasClearGeofencesFromMonitorAndStoreCalled;
        shouldClearGeofencesFromStoreOnlyHaveBeenCalled = wasClearGeofencesFromStoreOnlyCalled;
        return this;
    }

    public RegistrationEngineTestParameters setupAreGeofencesEnabled(boolean inPrefs, boolean fromUser, boolean finalValue, boolean shouldHaveBeenSaved) {
        areGeofencesEnabledInPrefs = inPrefs;
        areGeofencesEnabledFromUser = fromUser;
        finalAreGeofencesEnabled = finalValue;
        shouldAreGeofencesEnabledHaveBeenSaved = shouldHaveBeenSaved;
        return this;
    }

    public RegistrationEngineTestParameters setShouldRegistrationHaveSucceeded(boolean b) {
        shouldRegistrationHaveSucceeded = b;
        return this;
    }

    public RegistrationEngineTestParameters setShouldFcmTokenIdHaveBeenSaved(boolean b) {
        shouldFcmTokenIdHaveBeenSaved = b;
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

    public RegistrationEngineTestParameters setShouldPCFPushDeviceRegistrationHaveBeenSaved(boolean b) {
        shouldPCFPushDeviceRegistrationHaveBeenSaved = b;
        return this;
    }

    public RegistrationEngineTestParameters setShouldGeofencesHaveBeenReregistered(boolean b) {
        numberOfGeofenceReregistrations = b ? 1 : 0;
        return this;
    }

    public RegistrationEngineTestParameters setShouldHavePermissionForGeofences(boolean b) {
        shouldHavePermissionForGeofences = b;
        return this;
    }
}
