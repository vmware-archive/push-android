/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.baidu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import io.pivotal.android.push.PushParameters;
import io.pivotal.android.push.backend.api.FakePCFPushRegistrationApiRequest;
import io.pivotal.android.push.backend.api.PCFPushRegistrationApiRequestProvider;
import io.pivotal.android.push.geofence.GeofenceConstants;
import io.pivotal.android.push.prefs.FakePushRequestHeaders;
import io.pivotal.android.push.prefs.Pivotal;
import io.pivotal.android.push.prefs.PushPreferencesBaidu;
import io.pivotal.android.push.registration.RegistrationListener;
import io.pivotal.android.push.util.DelayedLoop;
import java.util.Set;

public class RegistrationEngineTestParameters {

    private static final long TEN_SECOND_TIMEOUT = 10000L;

    private final Context context;
    private final DelayedLoop delayedLoop;

    private String baiduChannelIdInPrefs = null;
    private String baiduChanneldPassedIn = null;
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
    private String finalBaiduChannelIdInPrefs = null;
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
    private boolean shouldBaiduChannelIdHaveBeenSaved = false;
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

    public RegistrationEngineTestParameters() {
        this.context = mock(Context.class);
        delayedLoop = new DelayedLoop(TEN_SECOND_TIMEOUT);
    }

    public void run() {
        final PushPreferencesBaidu pushPreferences = getMockPushPreferences(baiduChannelIdInPrefs, pcfPushDeviceRegistrationIdInPrefs, platformUuidInPrefs, platformSecretInPrefs, deviceAliasInPrefs, customUserIdInPrefs, packageNameInPrefs, serviceUrlInPrefs, tagsInPrefs,
            GeofenceConstants.NEVER_UPDATED_GEOFENCES, false);

        final FakePushRequestHeaders pushRequestHeaders = new FakePushRequestHeaders();
        final FakePCFPushRegistrationApiRequest fakePCFPushRegistrationApiRequest = new FakePCFPushRegistrationApiRequest(pcfPushDeviceRegistrationIdFromServer, shouldPCFPushDeviceRegistrationBeSuccessful);
        final PCFPushRegistrationApiRequestProvider PCFPushRegistrationApiRequestProvider = new PCFPushRegistrationApiRequestProvider(fakePCFPushRegistrationApiRequest);

        final RegistrationEngine engine = new RegistrationEngine(context, packageNameFromUser, pushPreferences, pushRequestHeaders, PCFPushRegistrationApiRequestProvider);
        final PushParameters parameters = new PushParameters(platformUuidFromUser, platformSecretFromUser, serviceUrlFromUser, "some-platform-type", deviceAliasFromUser, customUserIdFromUser, tagsFromUser,
            false, true, Pivotal.SslCertValidationMode.DEFAULT, null, null);

        engine.registerDevice(parameters, baiduChanneldPassedIn, new RegistrationListener() {

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


        assertTrue(delayedLoop.isSuccess());
        assertEquals(shouldPCFPushUpdateRegistrationHaveBeenCalled, fakePCFPushRegistrationApiRequest.isUpdateRegistration());
        assertEquals(shouldPCFPushNewRegistrationHaveBeenCalled, fakePCFPushRegistrationApiRequest.isNewRegistration());

        verify(pushPreferences, times(shouldBaiduChannelIdHaveBeenSaved ? 1 : 0)).setBaiduChannelId(finalBaiduChannelIdInPrefs);
        verify(pushPreferences, times(shouldPCFPushDeviceRegistrationHaveBeenSaved ? 1 : 0)).setPCFPushDeviceRegistrationId(finalPCFPushDeviceRegistrationIdInPrefs);
        verify(pushPreferences, times(shouldPlatformUuidHaveBeenSaved ? 1 : 0)).setPlatformUuid(finalPlatformUuidInPrefs);
        verify(pushPreferences, times(shouldPlatformSecretHaveBeenSaved ? 1 : 0)).setPlatformSecret(finalPlatformSecretInPrefs);
        verify(pushPreferences, times(shouldDeviceAliasHaveBeenSaved ? 1 : 0)).setDeviceAlias(finalDeviceAliasInPrefs);
        verify(pushPreferences, times(shouldCustomUserIdHaveBeenSaved ? 1 : 0)).setCustomUserId(finalCustomUserIdInPrefs);
        verify(pushPreferences, times(shouldPackageNameHaveBeenSaved ? 1 : 0)).setPackageName(finalPackageNameInPrefs);
        verify(pushPreferences, times(shouldServiceUrlHaveBeenSaved ? 1 : 0)).setServiceUrl(finalServiceUrlInPrefs);
        verify(pushPreferences, times(shouldTagsHaveBeenSaved ? 1 : 0)).setTags(finalTagsInPrefs);
    }

    private PushPreferencesBaidu getMockPushPreferences(String baiduChannelIdInPrefs,
        String pcfPushDeviceRegistrationIdInPrefs, String platformUuidInPrefs,
        String platformSecretInPrefs, String deviceAliasInPrefs, String customUserIdInPrefs,
        String packageNameInPrefs, String serviceUrlInPrefs, Set<String> tagsInPrefs,
        long geofenceUpdateTimestampInPrefs, boolean areGeofencesEnabledInPrefs) {

        PushPreferencesBaidu pushPreferences = mock(PushPreferencesBaidu.class);
        when(pushPreferences.getBaiduChannelId()).thenReturn(baiduChannelIdInPrefs);
        when(pushPreferences.getPlatformUuid()).thenReturn(platformUuidInPrefs);
        when(pushPreferences.getPlatformSecret()).thenReturn(platformSecretInPrefs);
        when(pushPreferences.getPCFPushDeviceRegistrationId()).thenReturn(pcfPushDeviceRegistrationIdInPrefs);
        when(pushPreferences.getDeviceAlias()).thenReturn(deviceAliasInPrefs);
        when(pushPreferences.getCustomUserId()).thenReturn(customUserIdInPrefs);
        when(pushPreferences.getPackageName()).thenReturn(packageNameInPrefs);
        when(pushPreferences.getServiceUrl()).thenReturn(serviceUrlInPrefs);
        when(pushPreferences.getTags()).thenReturn(tagsInPrefs);
        when(pushPreferences.getLastGeofenceUpdate()).thenReturn(geofenceUpdateTimestampInPrefs);
        when(pushPreferences.areGeofencesEnabled()).thenReturn(areGeofencesEnabledInPrefs);

        return pushPreferences;
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

    public RegistrationEngineTestParameters setupBaiduChannelId(String inPrefs, String passedIn, String finalValue) {
        baiduChannelIdInPrefs = inPrefs;
        baiduChanneldPassedIn = passedIn;
        finalBaiduChannelIdInPrefs = finalValue;
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

    public RegistrationEngineTestParameters setShouldPCFPushDeviceRegistrationBeSuccessful(boolean b) {
        shouldPCFPushDeviceRegistrationBeSuccessful = b;
        return this;
    }

    public RegistrationEngineTestParameters setShouldRegistrationHaveSucceeded(boolean b) {
        shouldRegistrationHaveSucceeded = b;
        return this;
    }

    public RegistrationEngineTestParameters setShouldBaiduChannelIdHaveBeenSaved(boolean b) {
        shouldBaiduChannelIdHaveBeenSaved = b;
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
}
