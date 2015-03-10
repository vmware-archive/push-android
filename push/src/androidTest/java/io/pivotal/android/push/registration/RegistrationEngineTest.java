/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.registration;

import android.content.Intent;
import android.test.AndroidTestCase;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;

import io.pivotal.android.push.PushParameters;
import io.pivotal.android.push.backend.api.FakePCFPushRegistrationApiRequest;
import io.pivotal.android.push.backend.api.PCFPushRegistrationApiRequestProvider;
import io.pivotal.android.push.gcm.FakeGcmProvider;
import io.pivotal.android.push.gcm.FakeGcmRegistrationApiRequest;
import io.pivotal.android.push.gcm.FakeGcmUnregistrationApiRequest;
import io.pivotal.android.push.gcm.GcmRegistrationApiRequestProvider;
import io.pivotal.android.push.gcm.GcmUnregistrationApiRequestProvider;
import io.pivotal.android.push.geofence.GeofenceEngine;
import io.pivotal.android.push.geofence.GeofenceUpdater;
import io.pivotal.android.push.prefs.FakePushPreferencesProvider;
import io.pivotal.android.push.prefs.PushPreferencesProvider;
import io.pivotal.android.push.util.Logger;
import io.pivotal.android.push.version.FakeVersionProvider;
import io.pivotal.android.push.version.VersionProvider;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class RegistrationEngineTest extends AndroidTestCase {

    private static final String TEST_GCM_DEVICE_REGISTRATION_ID_1 = "TEST_GCM_DEVICE_REGISTRATION_ID_1";
    private static final String TEST_GCM_DEVICE_REGISTRATION_ID_2 = "TEST_GCM_DEVICE_REGISTRATION_ID_2";
    private static final String TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1 = "TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1";
    private static final String TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2 = "TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2";
    private static final String TEST_DEVICE_ALIAS_1 = "TEST_DEVICE_ALIAS_1";
    private static final String TEST_DEVICE_ALIAS_2 = "TEST_DEVICE_ALIAS_2";
    private static final String TEST_PLATFORM_UUID_1 = "TEST_PLATFORM_UUID_1";
    private static final String TEST_PLATFORM_UUID_2 = "TEST_PLATFORM_UUID_2";
    private static final String TEST_PLATFORM_SECRET_1 = "TEST_PLATFORM_SECRET_1";
    private static final String TEST_PLATFORM_SECRET_2 = "TEST_PLATFORM_SECRET_2";
    private static final String TEST_GCM_SENDER_ID_1 = "TEST_GCM_SENDER_ID_1";
    private static final String TEST_GCM_SENDER_ID_2 = "TEST_GCM_SENDER_ID_2";
    private static String TEST_SERVICE_URL_1 = "http://test1.com";
    private static String TEST_SERVICE_URL_2 = "http://test2.com";
    private static final Set<String> TEST_TAGS1 = new HashSet<>();
    private static final Set<String> TEST_TAGS2 = new HashSet<>();
    private static final String TEST_PACKAGE_NAME = "TEST.PACKAGE.NAME";
    private static final Set<String> EMPTY_SET = Collections.emptySet();
    private static final long NOT_USED = -1L; // Placeholder used to make some tests more readable.

    private FakePushPreferencesProvider pushPreferencesProvider;
    private GcmRegistrationApiRequestProvider gcmRegistrationApiRequestProvider;
    private GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider;
    private PCFPushRegistrationApiRequestProvider pcfPushRegistrationApiRequestProvider;
    private VersionProvider versionProvider;
    private FakeGcmProvider gcmProvider;
    private GeofenceUpdater geofenceUpdater;
    private Semaphore semaphore = new Semaphore(0);

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("dexmaker.dexcache", mContext.getCacheDir().getPath());
        TEST_TAGS1.addAll(Arrays.asList("CATS", "DOGS"));
        TEST_TAGS2.addAll(Arrays.asList("LEMURS", "MONKEYS"));
        gcmProvider = new FakeGcmProvider(TEST_GCM_DEVICE_REGISTRATION_ID_1);
        gcmRegistrationApiRequestProvider = new GcmRegistrationApiRequestProvider(new FakeGcmRegistrationApiRequest(gcmProvider));
        gcmUnregistrationApiRequestProvider = new GcmUnregistrationApiRequestProvider(new FakeGcmUnregistrationApiRequest(gcmProvider));
        pushPreferencesProvider = new FakePushPreferencesProvider();
        versionProvider = new FakeVersionProvider(10);
        pcfPushRegistrationApiRequestProvider = new PCFPushRegistrationApiRequestProvider(new FakePCFPushRegistrationApiRequest(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1));
        geofenceUpdater = mock(GeofenceUpdater.class);

        doAnswer(new Answer<Void>(){

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                final GeofenceUpdater.GeofenceUpdaterListener listener = (GeofenceUpdater.GeofenceUpdaterListener) invocation.getArguments()[2];
                listener.onSuccess();
                return null;
            }

        }).when(geofenceUpdater).startGeofenceUpdate(any(Intent.class), anyLong(), any(GeofenceUpdater.GeofenceUpdaterListener.class));
}

    public void testNullContext() {
        try {
            new RegistrationEngine(null, TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, pcfPushRegistrationApiRequestProvider, versionProvider, geofenceUpdater);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullPackageName() {
        try {
            new RegistrationEngine(getContext(), null, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, pcfPushRegistrationApiRequestProvider, versionProvider, geofenceUpdater);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullGcmProvider() {
        try {
            new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, null, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, pcfPushRegistrationApiRequestProvider, versionProvider, geofenceUpdater);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullPushPreferencesProvider() {
        try {
            new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, gcmProvider, null, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, pcfPushRegistrationApiRequestProvider, versionProvider, geofenceUpdater);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullGcmRegistrationApiRequestProvider() {
        try {
            new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, null, gcmUnregistrationApiRequestProvider, pcfPushRegistrationApiRequestProvider, versionProvider, geofenceUpdater);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullGcmUnregistrationApiRequestProvider() {
        try {
            new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, null, pcfPushRegistrationApiRequestProvider, versionProvider, geofenceUpdater);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullPCFPushRegisterDeviceApiRequestProvider() {
        try {
            new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, null, versionProvider, geofenceUpdater);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullVersionProvider() {
        try {
            new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, pcfPushRegistrationApiRequestProvider, null, geofenceUpdater);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullGeofenceUpdater() {
        try {
            new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, pcfPushRegistrationApiRequestProvider, versionProvider, null);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullParameters() {
        try {
            final RegistrationEngine engine = new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, pcfPushRegistrationApiRequestProvider, versionProvider, geofenceUpdater);
            engine.registerDevice(null, getListenerForRegistration(false));
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullSenderId() {
        try {
            final RegistrationEngine engine = new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, pcfPushRegistrationApiRequestProvider, versionProvider, geofenceUpdater);
            engine.registerDevice(new PushParameters(null, TEST_PLATFORM_UUID_1, TEST_PLATFORM_SECRET_1, TEST_SERVICE_URL_1, TEST_DEVICE_ALIAS_1, null), getListenerForRegistration(false));
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullPlatformUuid() {
        try {
            final RegistrationEngine engine = new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, pcfPushRegistrationApiRequestProvider, versionProvider, geofenceUpdater);
            engine.registerDevice(new PushParameters(TEST_GCM_SENDER_ID_1, null, TEST_PLATFORM_SECRET_1, TEST_SERVICE_URL_1, TEST_DEVICE_ALIAS_1, null), getListenerForRegistration(false));
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullPlatformSecret() {
        try {
            final RegistrationEngine engine = new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, pcfPushRegistrationApiRequestProvider, versionProvider, geofenceUpdater);
            engine.registerDevice(new PushParameters(TEST_GCM_SENDER_ID_1, TEST_PLATFORM_UUID_1, null, TEST_SERVICE_URL_1, TEST_DEVICE_ALIAS_1, null), getListenerForRegistration(false));
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullDeviceAlias() throws InterruptedException {
        final RegistrationEngine engine = new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, pcfPushRegistrationApiRequestProvider, versionProvider, geofenceUpdater);
        engine.registerDevice(new PushParameters(TEST_GCM_SENDER_ID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_SECRET_1, TEST_SERVICE_URL_1, null, null), getListenerForRegistration(true));
        semaphore.acquire();
    }

    public void testEmptyDeviceAlias() throws InterruptedException {
        final RegistrationEngine engine = new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, pcfPushRegistrationApiRequestProvider, versionProvider, geofenceUpdater);
        engine.registerDevice(new PushParameters(TEST_GCM_SENDER_ID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_SECRET_1, TEST_SERVICE_URL_1, "", null), getListenerForRegistration(true));
        semaphore.acquire();
    }

    public void testEmptyServiceUrl() throws InterruptedException {
        try {
            final RegistrationEngine engine = new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, pcfPushRegistrationApiRequestProvider, versionProvider, geofenceUpdater);
            engine.registerDevice(new PushParameters(TEST_GCM_SENDER_ID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_SECRET_1, null, TEST_DEVICE_ALIAS_1, null), getListenerForRegistration(true));
            semaphore.acquire();
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testGooglePlayServicesNotAvailable() throws InterruptedException {
        gcmProvider.setIsGooglePlayServicesInstalled(false);
        final RegistrationEngine engine = new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, pcfPushRegistrationApiRequestProvider, versionProvider, geofenceUpdater);
        final PushParameters parameters = new PushParameters(TEST_GCM_SENDER_ID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_SECRET_1, TEST_SERVICE_URL_1, TEST_DEVICE_ALIAS_1, null);
        engine.registerDevice(parameters, getListenerForRegistration(false));
        semaphore.acquire();
    }

    public void testSuccessfulInitialRegistration() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(null, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(null, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(null, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(null, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(null, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupTags(null, null, EMPTY_SET, true)
                .setupServiceUrl(null, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(null, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(GeofenceEngine.NEVER_UPDATED_GEOFENCES, 0L, 1337L, 1337L, true, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testSuccessfulInitialRegistrationWithTags() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(null, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(null, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(null, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(null, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(null, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupTags(null, TEST_TAGS1, TEST_TAGS1, true)
                .setupServiceUrl(null, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(null, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(GeofenceEngine.NEVER_UPDATED_GEOFENCES, 0L, 1337L, 1337L, true, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testFailedInitialGcmRegistration() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(null, null, null)
                .setupPCFPushDeviceRegistrationId(null, null, null)
                .setupGcmSenderId(null, TEST_GCM_SENDER_ID_1, null, false)
                .setupPlatformUuid(null, TEST_PLATFORM_UUID_1, null, false)
                .setupPlatformSecret(null, TEST_PLATFORM_SECRET_1, null, false)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, null, false)
                .setupTags(null, TEST_TAGS1, null, false)
                .setupServiceUrl(null, TEST_SERVICE_URL_1, null, false)
                .setupPackageName(null, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(GeofenceEngine.NEVER_UPDATED_GEOFENCES, 0L, NOT_USED, GeofenceEngine.NEVER_UPDATED_GEOFENCES, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(false)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testInitialGcmRegistrationPassedButInitialPCFPushRegistrationFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(null, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(null, null, null)
                .setupGcmSenderId(null, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(null, TEST_PLATFORM_UUID_1, null, true)
                .setupPlatformSecret(null, TEST_PLATFORM_SECRET_1, null, true)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, null, true)
                .setupTags(null, TEST_TAGS1, null, true)
                .setupServiceUrl(null, TEST_SERVICE_URL_1, null, true)
                .setupPackageName(null, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(GeofenceEngine.NEVER_UPDATED_GEOFENCES, 0L, NOT_USED, GeofenceEngine.NEVER_UPDATED_GEOFENCES, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testInitialGcmRegistrationPassedButServerReturnedNullPCFPushRegistrationId() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(null, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationIdWithNullFromServer(null, null)
                .setupGcmSenderId(null, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(null, TEST_PLATFORM_UUID_1, null, true)
                .setupPlatformSecret(null, TEST_PLATFORM_SECRET_1, null, true)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, null, true)
                .setupTags(null, TEST_TAGS1, null, true)
                .setupServiceUrl(null, TEST_SERVICE_URL_1, null, true)
                .setupPackageName(null, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(GeofenceEngine.NEVER_UPDATED_GEOFENCES, 0L, NOT_USED, GeofenceEngine.NEVER_UPDATED_GEOFENCES, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testWasAlreadyRegisteredWithGcmAndPCFPush() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, false)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupTags(null, null, null, false)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(false)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testWasAlreadyRegisteredIncludingTagsWithGcmAndPCFPush() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, false)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupTags(TEST_TAGS1, TEST_TAGS1, TEST_TAGS1, false)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(false)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testWasAlreadyRegisteredWithGcmAndPCFPushAndThePlatformSecretIsChanged() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_2, TEST_PLATFORM_SECRET_2, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupTags(TEST_TAGS1, TEST_TAGS1, TEST_TAGS1, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testWasAlreadyRegisteredWithGcmAndPCFPushAndTheDeviceAliasIsChanged() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_2, TEST_DEVICE_ALIAS_2, true)
                .setupTags(TEST_TAGS1, TEST_TAGS1, TEST_TAGS1, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testWasAlreadyRegisteredWithGcmAndPCFPushAndThePlatformUuidIsChanged() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_2, TEST_PLATFORM_UUID_2, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupTags(TEST_TAGS1, TEST_TAGS1, TEST_TAGS1, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testWasAlreadyRegisteredWithGcmAndPCFPushAndTheTagsAreChanged() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupTags(TEST_TAGS1, TEST_TAGS2, TEST_TAGS2, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testWasAlreadyRegisteredWithGcmAndPCFPushAndTheServiceUrlIsChanged() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupTags(TEST_TAGS1, TEST_TAGS1, TEST_TAGS1, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_2, TEST_SERVICE_URL_2, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 0L, 50L, 50L, true, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testWasAlreadyRegisteredWithGcmButNotPCFPushAndPCFPushRegistrationSucceeds() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, null, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(null, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(null, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(null, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupTags(null, null, EMPTY_SET, true)
                .setupServiceUrl(null, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(GeofenceEngine.NEVER_UPDATED_GEOFENCES, 0L, 1337L, 1337L, true, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testWasAlreadyRegisteredWithGcmButNotPCFPushAndPCFPushRegistrationSucceedsWithTags() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, null, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(null, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(null, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(null, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupTags(null, TEST_TAGS1, TEST_TAGS1, true)
                .setupServiceUrl(null, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(GeofenceEngine.NEVER_UPDATED_GEOFENCES, 0L, 1337L, 1337L, true, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testWasAlreadyRegisteredWithGcmButNotPCFPushAndPCFPushRegistrationFails() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, null, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(null, null, null)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(null, TEST_PLATFORM_UUID_1, null, true)
                .setupPlatformSecret(null, TEST_PLATFORM_SECRET_1, null, true)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, null, true)
                .setupTags(null, null, null, true)
                .setupServiceUrl(null, TEST_SERVICE_URL_1, null, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(GeofenceEngine.NEVER_UPDATED_GEOFENCES, 0L, NOT_USED, GeofenceEngine.NEVER_UPDATED_GEOFENCES, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testWasAlreadyRegisteredWithGcmButNotPCFPushAndPCFPushRegistrationFailsWithTags() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, null, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(null, null, null)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(null, TEST_PLATFORM_UUID_1, null, true)
                .setupPlatformSecret(null, TEST_PLATFORM_SECRET_1, null, true)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, null, true)
                .setupTags(null, TEST_TAGS1, null, true)
                .setupServiceUrl(null, TEST_SERVICE_URL_1, null, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(GeofenceEngine.NEVER_UPDATED_GEOFENCES, 0L, NOT_USED, GeofenceEngine.NEVER_UPDATED_GEOFENCES, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testWasAlreadyRegisteredWithGcmButNotPCFPushAndServerReturnsNullPCFPushRegistrationId() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, null, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationIdWithNullFromServer(null, null)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(null, TEST_PLATFORM_UUID_1, null, true)
                .setupPlatformSecret(null, TEST_PLATFORM_SECRET_1, null, true)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, null, true)
                .setupTags(null, null, null, true)
                .setupServiceUrl(null, TEST_SERVICE_URL_1, null, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(GeofenceEngine.NEVER_UPDATED_GEOFENCES, 0L, NOT_USED, GeofenceEngine.NEVER_UPDATED_GEOFENCES, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testAppUpdatedAndSameGcmRegistrationIdWasReturned() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, false)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupTags(TEST_TAGS1, TEST_TAGS1, TEST_TAGS1, false)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(false)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testAppUpdatedToLesserVersionAndSameGcmRegistrationIdWasReturned() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, false)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupTags(TEST_TAGS1, TEST_TAGS1, TEST_TAGS1, false)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(2, 1, 1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(false)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testAppUpdatedAndGcmReregistrationFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, null, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, null, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, false)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupTags(TEST_TAGS1, TEST_TAGS1, TEST_TAGS1, false)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(false)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testAppUpdatedAndGcmReregistrationReturnedNewIdButPCFPushReregistrationFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, null, null)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, null, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, null, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, null, true)
                .setupTags(TEST_TAGS1, TEST_TAGS1, null, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, null, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testAppUpdatedAndGcmReregistrationReturnedNewIdButServerReturnsNullPCFPushRegistrationId() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupPCFPushDeviceRegistrationIdWithNullFromServer(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, null)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, null, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, null, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, null, true)
                .setupTags(TEST_TAGS1, TEST_TAGS1, null, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, null, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testAppUpdatedAndDifferentGcmRegistrationIdWasReturned() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupTags(TEST_TAGS1, TEST_TAGS1, TEST_TAGS1, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testAppUpdatedAndDifferentGcmRegistrationIdWasReturnedAndTheresANewPCFPushServerUrlToo() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupTags(TEST_TAGS1, TEST_TAGS1, TEST_TAGS1, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_2, TEST_SERVICE_URL_2, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 0L, 50L, 50L, true, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testAppUpdatedAndDifferentGcmRegistrationIdWasReturnedAndTheresANewPCFPushServerUrlTooAndPCFPushRegistrationFails() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, null, null)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, null, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, null, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, null, true)
                .setupTags(TEST_TAGS1, TEST_TAGS1, null, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_2, null, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testAppUpdatedAndDifferentGcmRegistrationIdWasReturnedAndTheresANewPCFPushServerUrlTooAndTheServerReturnsANullPCFPushRegistrationId() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupPCFPushDeviceRegistrationIdWithNullFromServer(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, null)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, null, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, null, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, null, true)
                .setupTags(TEST_TAGS1, TEST_TAGS1, null, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_2, null, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testAppUpdatedAndDifferentGcmRegistrationIdWasReturnedButPlatformUuidFromPreviousRegistrationWasNotSaved() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(null, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupTags(TEST_TAGS1, TEST_TAGS1, TEST_TAGS1, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testAppUpdatedAndDifferentGcmRegistrationIdWasReturnedButPCFPushRegistrationIdWasNotSaved() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupPCFPushDeviceRegistrationId(null, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupTags(TEST_TAGS1, TEST_TAGS1, TEST_TAGS1, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 0L, 50L, 50L, true, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testAppUpdatedAndDifferentGcmRegistrationIdWasReturnedAndUnregisterFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupTags(TEST_TAGS1, TEST_TAGS1, TEST_TAGS1, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testPlatformSecretUpdatedAndUnregisterFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_2, TEST_PLATFORM_SECRET_2, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupTags(TEST_TAGS1, TEST_TAGS1, TEST_TAGS1, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testDeviceAliasUpdatedAndUnregisterFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_2, TEST_DEVICE_ALIAS_2, true)
                .setupTags(TEST_TAGS1, TEST_TAGS1, TEST_TAGS1, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testTagsUpdatedAndUnregisterFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupTags(TEST_TAGS1, TEST_TAGS2, TEST_TAGS2, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testPlatformUuidUpdatedAndUnregisterFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_2, TEST_PLATFORM_UUID_2, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupTags(TEST_TAGS1, TEST_TAGS1, TEST_TAGS1, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testAppUpdatedToLesserVersionAndDifferentGcmRegistrationIdWasReturned() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupTags(TEST_TAGS1, TEST_TAGS1, TEST_TAGS1, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(2, 1, 1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testSenderIdUpdatedAndGcmUnregistrationFailsAndThenGcmReturnsANewRegistrationId() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_2, TEST_GCM_SENDER_ID_2, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupTags(TEST_TAGS1, TEST_TAGS1, TEST_TAGS1, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false)
                .setupGcmUnregisterDevice(true, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testSenderIdUpdatedAndGcmUnregistrationFailsAndThenGcmRegistrationFailsToo() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, null, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_2, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, false)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupTags(TEST_TAGS1, TEST_TAGS1, TEST_TAGS1, false)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false)
                .setupGcmUnregisterDevice(true, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(false)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testSenderIdUpdatedAndGcmReturnedNewGcmDeviceRegistrationId() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_2, TEST_GCM_SENDER_ID_2, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupTags(TEST_TAGS1, TEST_TAGS1, TEST_TAGS1, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false)
                .setupGcmUnregisterDevice(true, true)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testSenderIdUpdatedAndGcmReturnedOldGcmDeviceRegistrationId() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_2, TEST_GCM_SENDER_ID_2, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, false)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupTags(TEST_TAGS1, TEST_TAGS1, TEST_TAGS1, false)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false)
                .setupGcmUnregisterDevice(true, true)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(false)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testSenderIdUpdatedAndGcmReregistrationFails() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, null, null)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_2, null, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, false)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupTags(TEST_TAGS1, TEST_TAGS1, TEST_TAGS1, false)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false)
                .setupGcmUnregisterDevice(true, true)
                .setupAppVersion(1, 1, PushPreferencesProvider.NO_SAVED_VERSION)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(false)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testSenderIdUpdatedAndGcmReturnedNewGcmDeviceRegistrationIdButPCFPushReregistrationFails() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_2, TEST_GCM_SENDER_ID_2, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupTags(TEST_TAGS1, TEST_TAGS1, TEST_TAGS1, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false)
                .setupGcmUnregisterDevice(true, true)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testSuccessfulInitialRegistrationButFailsToGetGeofences() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(null, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(null, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(null, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(null, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(null, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupTags(null, null, EMPTY_SET, true)
                .setupServiceUrl(null, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(null, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(GeofenceEngine.NEVER_UPDATED_GEOFENCES, 0L, 0L, GeofenceEngine.NEVER_UPDATED_GEOFENCES, false, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testSuccessfullyRegisteredButNeedsToUpdateGeofences() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters(getContext())
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, false)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupTags(EMPTY_SET, null, EMPTY_SET, false)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(GeofenceEngine.NEVER_UPDATED_GEOFENCES, 0L, 1337L, 1337L, true, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(false)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    private RegistrationListener getListenerForRegistration(final boolean isSuccessfulRegistration) {
        return new RegistrationListener() {
            @Override
            public void onRegistrationComplete() {
                assertTrue(isSuccessfulRegistration);
                semaphore.release();
            }

            @Override
            public void onRegistrationFailed(String reason) {
                if (isSuccessfulRegistration) {
                    Logger.e("Test failed due to error:" + reason);
                }
                assertFalse(isSuccessfulRegistration);
                semaphore.release();
            }
        };
    }
}