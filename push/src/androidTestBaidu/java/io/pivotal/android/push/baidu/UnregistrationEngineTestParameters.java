/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.baidu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import io.pivotal.android.push.PushParameters;
import io.pivotal.android.push.backend.api.FakePCFPushUnregisterDeviceApiRequest;
import io.pivotal.android.push.backend.api.PCFPushUnregisterDeviceApiRequestProvider;
import io.pivotal.android.push.prefs.PushPreferencesBaidu;
import io.pivotal.android.push.registration.UnregistrationListener;
import io.pivotal.android.push.util.DelayedLoop;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class UnregistrationEngineTestParameters {

    private static final long TEN_SECOND_TIMEOUT = 10000L;
    private static final String PLATFORM_UUID_IN_PREFS = "VARIANT UUID";
    private static final String PLATFORM_SECRET_IN_PREFS = "VARIANT SECRET";
    private static final String DEVICE_ALIAS_IN_PREFS = "DEVICE ALIAS";
    private static final String CUSTOM_USER_ID_IN_PREFS = "CUSTOM USER ID";
    private static final String BAIDU_CHANNEL_ID_IN_PREFS = "BAIDU CHANNEL ID";
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
    private PushParameters parametersFromUser;

    public UnregistrationEngineTestParameters() {
        this.context = mock(Context.class);
        delayedLoop = new DelayedLoop(TEN_SECOND_TIMEOUT);
        TAGS_IN_PREFS.addAll(Arrays.asList("BANANAS", "PAPAYAS"));
    }

    public void run() throws Exception {

        final PushPreferencesBaidu pushPreferences;

        pushPreferences = mock(PushPreferencesBaidu.class);

        if (startingPCFPushDeviceRegistrationIdInPrefs == null) {
            when(pushPreferences.getPCFPushDeviceRegistrationId()).thenReturn(startingPCFPushDeviceRegistrationIdInPrefs);
        } else {
            when(pushPreferences.getBaiduChannelId()).thenReturn(BAIDU_CHANNEL_ID_IN_PREFS);
            when(pushPreferences.getPCFPushDeviceRegistrationId()).thenReturn(startingPCFPushDeviceRegistrationIdInPrefs);
            when(pushPreferences.getPlatformUuid()).thenReturn(PLATFORM_UUID_IN_PREFS);
            when(pushPreferences.getPlatformSecret()).thenReturn(PLATFORM_SECRET_IN_PREFS);
            when(pushPreferences.getPCFPushDeviceRegistrationId()).thenReturn(DEVICE_ALIAS_IN_PREFS);
            when(pushPreferences.getCustomUserId()).thenReturn(CUSTOM_USER_ID_IN_PREFS);
            when(pushPreferences.getPackageName()).thenReturn(PACKAGE_NAME_IN_PREFS);
            when(pushPreferences.getServiceUrl()).thenReturn(SERVICE_URL_IN_PREFS);
            when(pushPreferences.getTags()).thenReturn(TAGS_IN_PREFS);
        }

        final FakePCFPushUnregisterDeviceApiRequest fakePCFPushUnregisterDeviceApiRequest = new FakePCFPushUnregisterDeviceApiRequest(shouldPCFPushDeviceUnregistrationBeSuccessful);
        final PCFPushUnregisterDeviceApiRequestProvider PCFPushUnregisterDeviceApiRequestProvider = new PCFPushUnregisterDeviceApiRequestProvider(fakePCFPushUnregisterDeviceApiRequest);
        final UnregistrationEngine engine = new UnregistrationEngine(context, pushPreferences, PCFPushUnregisterDeviceApiRequestProvider);

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
        verify(pushPreferences).setBaiduChannelId(isNull(String.class));

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

        // No geofence interactions
        verify(pushPreferences, never()).areGeofencesEnabled();
        verify(pushPreferences, never()).setLastGeofenceUpdate(anyLong());
        verify(pushPreferences, never()).setAreGeofencesEnabled(anyBoolean());

        assertEquals(shouldPCFPushUnregisterHaveBeenCalled, fakePCFPushUnregisterDeviceApiRequest.wasUnregisterCalled());
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
}