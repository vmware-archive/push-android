/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.registration;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
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
import io.pivotal.android.push.geofence.GeofenceStatusUtil;
import io.pivotal.android.push.geofence.GeofenceUpdater;
import io.pivotal.android.push.prefs.FakePushPreferencesProvider;
import io.pivotal.android.push.prefs.Pivotal;
import io.pivotal.android.push.prefs.PushPreferencesProvider;
import io.pivotal.android.push.util.Logger;
import io.pivotal.android.push.version.FakeVersionProvider;
import io.pivotal.android.push.version.VersionProvider;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RegistrationEngineTest extends AndroidTestCase {

    private static final String TEST_GCM_DEVICE_REGISTRATION_ID_1 = "TEST_GCM_DEVICE_REGISTRATION_ID_1";
    private static final String TEST_GCM_DEVICE_REGISTRATION_ID_2 = "TEST_GCM_DEVICE_REGISTRATION_ID_2";
    private static final String TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1 = "TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1";
    private static final String TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2 = "TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2";
    private static final String TEST_DEVICE_ALIAS_1 = "TEST_DEVICE_ALIAS_1";
    private static final String TEST_DEVICE_ALIAS_2 = "TEST_DEVICE_ALIAS_2";
    private static final String TEST_CUSTOM_USER_ID_1 = "TEST_CUSTOM_USER_ID_1";
    private static final String TEST_CUSTOM_USER_ID_2 = "TEST_CUSTOM_USER_ID_2";
    private static final String TEST_PLATFORM_UUID_1 = "TEST_PLATFORM_UUID_1";
    private static final String TEST_PLATFORM_UUID_2 = "TEST_PLATFORM_UUID_2";
    private static final String TEST_PLATFORM_SECRET_1 = "TEST_PLATFORM_SECRET_1";
    private static final String TEST_PLATFORM_SECRET_2 = "TEST_PLATFORM_SECRET_2";
    private static final String TEST_GCM_SENDER_ID_1 = "TEST_GCM_SENDER_ID_1";
    private static final String TEST_GCM_SENDER_ID_2 = "TEST_GCM_SENDER_ID_2";
    private static String TEST_SERVICE_URL_1 = "http://test1.com";
    private static String TEST_SERVICE_URL_2 = "http://test2.com";
    private static final Set<String> TEST_TAGS1 = new HashSet<>();
    private static final Set<String> TEST_TAGS1_LOWER = new HashSet<>();
    private static final Set<String> TEST_TAGS2 = new HashSet<>();
    private static final Set<String> TEST_TAGS2_LOWER = new HashSet<>();
    private static final String TEST_PACKAGE_NAME = "TEST.PACKAGE.NAME";
    private static final Set<String> EMPTY_SET = Collections.emptySet();
    private static final long NOT_USED = -1L; // Placeholder used to make some tests more readable.

    private FakePushPreferencesProvider pushPreferencesProvider;
    private GcmRegistrationApiRequestProvider gcmRegistrationApiRequestProvider;
    private GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider;
    private PCFPushRegistrationApiRequestProvider pcfPushRegistrationApiRequestProvider;
    private GeofenceStatusUtil geofenceStatusUtil;
    private VersionProvider versionProvider;
    private FakeGcmProvider gcmProvider;
    private GeofenceUpdater geofenceUpdater;
    private GeofenceEngine geofenceEngine;
    private Semaphore semaphore = new Semaphore(0);
    private Context context;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("dexmaker.dexcache", mContext.getCacheDir().getPath());
        TEST_TAGS1.addAll(Arrays.asList("CATS", "DOGS"));
        TEST_TAGS1_LOWER.addAll(Arrays.asList("cats", "dogs"));
        TEST_TAGS2.addAll(Arrays.asList("LEMURS", "MONKEYS"));
        TEST_TAGS2_LOWER.addAll(Arrays.asList("lemurs", "monkeys"));
        gcmProvider = new FakeGcmProvider(TEST_GCM_DEVICE_REGISTRATION_ID_1);
        gcmRegistrationApiRequestProvider = new GcmRegistrationApiRequestProvider(new FakeGcmRegistrationApiRequest(gcmProvider));
        gcmUnregistrationApiRequestProvider = new GcmUnregistrationApiRequestProvider(new FakeGcmUnregistrationApiRequest(gcmProvider));
        pushPreferencesProvider = new FakePushPreferencesProvider();
        versionProvider = new FakeVersionProvider(10);
        pcfPushRegistrationApiRequestProvider = new PCFPushRegistrationApiRequestProvider(new FakePCFPushRegistrationApiRequest(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1));
        geofenceUpdater = mock(GeofenceUpdater.class);
        geofenceEngine = mock(GeofenceEngine.class);
        geofenceStatusUtil = mock(GeofenceStatusUtil.class);
        context = mock(Context.class);
        when(context.checkCallingOrSelfPermission(anyString())).thenReturn(PackageManager.PERMISSION_GRANTED);

        doAnswer(new Answer<Void>() {

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
            new RegistrationEngine(null, TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, pcfPushRegistrationApiRequestProvider, versionProvider, geofenceUpdater, geofenceEngine, geofenceStatusUtil);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullPackageName() {
        try {
            new RegistrationEngine(context, null, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, pcfPushRegistrationApiRequestProvider, versionProvider, geofenceUpdater, geofenceEngine, geofenceStatusUtil);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullGcmProvider() {
        try {
            new RegistrationEngine(context, TEST_PACKAGE_NAME, null, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, pcfPushRegistrationApiRequestProvider, versionProvider, geofenceUpdater, geofenceEngine, geofenceStatusUtil);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullPushPreferencesProvider() {
        try {
            new RegistrationEngine(context, TEST_PACKAGE_NAME, gcmProvider, null, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, pcfPushRegistrationApiRequestProvider, versionProvider, geofenceUpdater, geofenceEngine, geofenceStatusUtil);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullGcmRegistrationApiRequestProvider() {
        try {
            new RegistrationEngine(context, TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, null, gcmUnregistrationApiRequestProvider, pcfPushRegistrationApiRequestProvider, versionProvider, geofenceUpdater, geofenceEngine, geofenceStatusUtil);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullGcmUnregistrationApiRequestProvider() {
        try {
            new RegistrationEngine(context, TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, null, pcfPushRegistrationApiRequestProvider, versionProvider, geofenceUpdater, geofenceEngine, geofenceStatusUtil);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullPCFPushRegisterDeviceApiRequestProvider() {
        try {
            new RegistrationEngine(context, TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, null, versionProvider, geofenceUpdater, geofenceEngine, geofenceStatusUtil);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullVersionProvider() {
        try {
            new RegistrationEngine(context, TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, pcfPushRegistrationApiRequestProvider, null, geofenceUpdater, geofenceEngine, geofenceStatusUtil);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullGeofenceUpdater() {
        try {
            new RegistrationEngine(context, TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, pcfPushRegistrationApiRequestProvider, versionProvider, null, geofenceEngine, geofenceStatusUtil);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullGeofenceEngine() {
        try {
            new RegistrationEngine(context, TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, pcfPushRegistrationApiRequestProvider, versionProvider, geofenceUpdater, null, geofenceStatusUtil);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullGeofenceStatusUtil() {
        try {
            new RegistrationEngine(context, TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, pcfPushRegistrationApiRequestProvider, versionProvider, geofenceUpdater, geofenceEngine, null);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullParameters() {
        try {
            final RegistrationEngine engine = new RegistrationEngine(context, TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, pcfPushRegistrationApiRequestProvider, versionProvider, geofenceUpdater, geofenceEngine, geofenceStatusUtil);
            engine.registerDevice(null, getListenerForRegistration(false));
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullSenderId() {
        try {
            final RegistrationEngine engine = new RegistrationEngine(context, TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, pcfPushRegistrationApiRequestProvider, versionProvider, geofenceUpdater, geofenceEngine, geofenceStatusUtil);
            engine.registerDevice(new PushParameters(null, TEST_PLATFORM_UUID_1, TEST_PLATFORM_SECRET_1, TEST_SERVICE_URL_1, TEST_DEVICE_ALIAS_1, TEST_CUSTOM_USER_ID_1, null, true, Pivotal.SslCertValidationMode.DEFAULT, null, null), getListenerForRegistration(false));
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullPlatformUuid() {
        try {
            final RegistrationEngine engine = new RegistrationEngine(context, TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, pcfPushRegistrationApiRequestProvider, versionProvider, geofenceUpdater, geofenceEngine, geofenceStatusUtil);
            engine.registerDevice(new PushParameters(TEST_GCM_SENDER_ID_1, null, TEST_PLATFORM_SECRET_1, TEST_SERVICE_URL_1, TEST_DEVICE_ALIAS_1, TEST_CUSTOM_USER_ID_1, null, true, Pivotal.SslCertValidationMode.DEFAULT, null, null), getListenerForRegistration(false));
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullPlatformSecret() {
        try {
            final RegistrationEngine engine = new RegistrationEngine(context, TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, pcfPushRegistrationApiRequestProvider, versionProvider, geofenceUpdater, geofenceEngine, geofenceStatusUtil);
            engine.registerDevice(new PushParameters(TEST_GCM_SENDER_ID_1, TEST_PLATFORM_UUID_1, null, TEST_SERVICE_URL_1, TEST_DEVICE_ALIAS_1, TEST_CUSTOM_USER_ID_1, null, true, Pivotal.SslCertValidationMode.DEFAULT, null, null), getListenerForRegistration(false));
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullDeviceAlias() throws InterruptedException {
        final RegistrationEngine engine = new RegistrationEngine(context, TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, pcfPushRegistrationApiRequestProvider, versionProvider, geofenceUpdater, geofenceEngine, geofenceStatusUtil);
        engine.registerDevice(new PushParameters(TEST_GCM_SENDER_ID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_SECRET_1, TEST_SERVICE_URL_1, TEST_CUSTOM_USER_ID_1, null, null, true, Pivotal.SslCertValidationMode.DEFAULT, null, null), getListenerForRegistration(true));
        semaphore.acquire();
    }

    public void testEmptyDeviceAlias() throws InterruptedException {
        final RegistrationEngine engine = new RegistrationEngine(context, TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, pcfPushRegistrationApiRequestProvider, versionProvider, geofenceUpdater, geofenceEngine, geofenceStatusUtil);
        engine.registerDevice(new PushParameters(TEST_GCM_SENDER_ID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_SECRET_1, TEST_SERVICE_URL_1, "", TEST_CUSTOM_USER_ID_1, null, true, Pivotal.SslCertValidationMode.DEFAULT, null, null), getListenerForRegistration(true));
        semaphore.acquire();
    }

    public void testNullCustomUserId() throws InterruptedException {
        final RegistrationEngine engine = new RegistrationEngine(context, TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, pcfPushRegistrationApiRequestProvider, versionProvider, geofenceUpdater, geofenceEngine, geofenceStatusUtil);
        engine.registerDevice(new PushParameters(TEST_GCM_SENDER_ID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_SECRET_1, TEST_SERVICE_URL_1, TEST_DEVICE_ALIAS_1, null, null, true, Pivotal.SslCertValidationMode.DEFAULT, null, null), getListenerForRegistration(true));
        semaphore.acquire();
    }

    public void testEmptyCustomUserId() throws InterruptedException {
        final RegistrationEngine engine = new RegistrationEngine(context, TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, pcfPushRegistrationApiRequestProvider, versionProvider, geofenceUpdater, geofenceEngine, geofenceStatusUtil);
        engine.registerDevice(new PushParameters(TEST_GCM_SENDER_ID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_SECRET_1, TEST_SERVICE_URL_1, TEST_DEVICE_ALIAS_1, "", null, true, Pivotal.SslCertValidationMode.DEFAULT, null, null), getListenerForRegistration(true));
        semaphore.acquire();
    }

    public void test254LongCustomUserId() throws InterruptedException {
        final String longString = getStringWithLength(254);
        final RegistrationEngine engine = new RegistrationEngine(context, TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, pcfPushRegistrationApiRequestProvider, versionProvider, geofenceUpdater, geofenceEngine, geofenceStatusUtil);
        engine.registerDevice(new PushParameters(TEST_GCM_SENDER_ID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_SECRET_1, TEST_SERVICE_URL_1, TEST_DEVICE_ALIAS_1, longString, null, true, Pivotal.SslCertValidationMode.DEFAULT, null, null), getListenerForRegistration(true));
        semaphore.acquire();
    }

    public void test255LongCustomUserId() throws InterruptedException {
        final String longString = getStringWithLength(255);
        final RegistrationEngine engine = new RegistrationEngine(context, TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, pcfPushRegistrationApiRequestProvider, versionProvider, geofenceUpdater, geofenceEngine, geofenceStatusUtil);
        engine.registerDevice(new PushParameters(TEST_GCM_SENDER_ID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_SECRET_1, TEST_SERVICE_URL_1, TEST_DEVICE_ALIAS_1, longString, null, true, Pivotal.SslCertValidationMode.DEFAULT, null, null), getListenerForRegistration(true));
        semaphore.acquire();
    }

    public void test256LongCustomUserId() {
        try {
            final String longString = getStringWithLength(256);
            final RegistrationEngine engine = new RegistrationEngine(context, TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, pcfPushRegistrationApiRequestProvider, versionProvider, geofenceUpdater, geofenceEngine, geofenceStatusUtil);
            engine.registerDevice(new PushParameters(TEST_GCM_SENDER_ID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_SECRET_1, TEST_SERVICE_URL_1, TEST_DEVICE_ALIAS_1, longString, null, true, Pivotal.SslCertValidationMode.DEFAULT, null, null), getListenerForRegistration(false));
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testEmptyServiceUrl() throws InterruptedException {
        try {
            final RegistrationEngine engine = new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, pcfPushRegistrationApiRequestProvider, versionProvider, geofenceUpdater, geofenceEngine, geofenceStatusUtil);
            engine.registerDevice(new PushParameters(TEST_GCM_SENDER_ID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_SECRET_1, null, TEST_DEVICE_ALIAS_1, TEST_CUSTOM_USER_ID_1, null, true, Pivotal.SslCertValidationMode.DEFAULT, null, null), getListenerForRegistration(true));
            semaphore.acquire();
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testGooglePlayServicesNotAvailable() throws InterruptedException {
        gcmProvider.setIsGooglePlayServicesInstalled(false);
        final RegistrationEngine engine = new RegistrationEngine(getContext(), TEST_PACKAGE_NAME, gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, pcfPushRegistrationApiRequestProvider, versionProvider, geofenceUpdater, geofenceEngine, geofenceStatusUtil);
        final PushParameters parameters = new PushParameters(TEST_GCM_SENDER_ID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_SECRET_1, TEST_SERVICE_URL_1, TEST_DEVICE_ALIAS_1, TEST_CUSTOM_USER_ID_1, null, true, Pivotal.SslCertValidationMode.DEFAULT, null, null);
        engine.registerDevice(parameters, getListenerForRegistration(false));
        semaphore.acquire();
    }

    public void testSuccessfulInitialRegistration() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(null, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(null, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(null, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(null, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(null, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(null, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(null, null, EMPTY_SET, true)
                .setupServiceUrl(null, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(null, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(GeofenceEngine.NEVER_UPDATED_GEOFENCES, 0L, 1337L, 1337L, true, true, false, false)
                .setupAreGeofencesEnabled(false, true, true, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testSuccessfulInitialRegistrationWithoutPermissionForGeofences() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(null, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(null, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(null, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(null, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(null, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(null, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(null, null, EMPTY_SET, true)
                .setupServiceUrl(null, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(null, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(GeofenceEngine.NEVER_UPDATED_GEOFENCES, NOT_USED, NOT_USED, GeofenceEngine.NEVER_UPDATED_GEOFENCES, true, false, false, true)
                .setupAreGeofencesEnabled(false, true, false, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testSuccessfulInitialRegistrationWithGeofencesDisabled() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(null, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(null, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(null, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(null, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(null, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(null, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(null, null, EMPTY_SET, true)
                .setupServiceUrl(null, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(null, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(GeofenceEngine.NEVER_UPDATED_GEOFENCES, NOT_USED, NOT_USED, GeofenceEngine.NEVER_UPDATED_GEOFENCES, true, false, false, false)
                .setupAreGeofencesEnabled(false, false, false, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testSuccessfulInitialRegistrationWithTags() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(null, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(null, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(null, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(null, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(null, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(null, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(null, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(null, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(null, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(GeofenceEngine.NEVER_UPDATED_GEOFENCES, 0L, 1337L, 1337L, true, true, false, false)
                .setupAreGeofencesEnabled(false, true, true, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testSuccessfulInitialRegistrationWithTagsWithoutPermissionForGeofences() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(null, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(null, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(null, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(null, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(null, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(null, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(null, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(null, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(null, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(GeofenceEngine.NEVER_UPDATED_GEOFENCES, NOT_USED, NOT_USED, GeofenceEngine.NEVER_UPDATED_GEOFENCES, true, false, false, true)
                .setupAreGeofencesEnabled(false, true, false, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testFailedInitialGcmRegistration() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(null, null, null)
                .setupPCFPushDeviceRegistrationId(null, null, null)
                .setupGcmSenderId(null, TEST_GCM_SENDER_ID_1, null, false)
                .setupPlatformUuid(null, TEST_PLATFORM_UUID_1, null, false)
                .setupPlatformSecret(null, TEST_PLATFORM_SECRET_1, null, false)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, null, false)
                .setupCustomUserId(null, TEST_CUSTOM_USER_ID_1, null, false)
                .setupTags(null, TEST_TAGS1, null, false)
                .setupServiceUrl(null, TEST_SERVICE_URL_1, null, false)
                .setupPackageName(null, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(GeofenceEngine.NEVER_UPDATED_GEOFENCES, 0L, NOT_USED, GeofenceEngine.NEVER_UPDATED_GEOFENCES, true, false, false, false)
                .setupAreGeofencesEnabled(false, true, false, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(false)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testInitialGcmRegistrationPassedButInitialPCFPushRegistrationFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(null, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(null, null, null)
                .setupGcmSenderId(null, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(null, TEST_PLATFORM_UUID_1, null, true)
                .setupPlatformSecret(null, TEST_PLATFORM_SECRET_1, null, true)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, null, true)
                .setupCustomUserId(null, TEST_CUSTOM_USER_ID_1, null, true)
                .setupTags(null, TEST_TAGS1, null, true)
                .setupServiceUrl(null, TEST_SERVICE_URL_1, null, true)
                .setupPackageName(null, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(GeofenceEngine.NEVER_UPDATED_GEOFENCES, 0L, NOT_USED, GeofenceEngine.NEVER_UPDATED_GEOFENCES, true, false, false, false)
                .setupAreGeofencesEnabled(false, true, false, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testInitialGcmRegistrationPassedButServerReturnedNullPCFPushRegistrationId() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(null, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationIdWithNullFromServer(null, null)
                .setupGcmSenderId(null, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(null, TEST_PLATFORM_UUID_1, null, true)
                .setupPlatformSecret(null, TEST_PLATFORM_SECRET_1, null, true)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, null, true)
                .setupCustomUserId(null, TEST_CUSTOM_USER_ID_1, null, true)
                .setupTags(null, TEST_TAGS1, null, true)
                .setupServiceUrl(null, TEST_SERVICE_URL_1, null, true)
                .setupPackageName(null, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(GeofenceEngine.NEVER_UPDATED_GEOFENCES, 0L, NOT_USED, GeofenceEngine.NEVER_UPDATED_GEOFENCES, true, false, false, false)
                .setupAreGeofencesEnabled(false, true, false, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testWasAlreadyRegisteredWithGcmAndPCFPush() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, false)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, false)
                .setupTags(null, null, null, false)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, NOT_USED, NOT_USED, 1337L, true, false, false, false)
                .setupAreGeofencesEnabled(true, true, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(false)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testWasAlreadyRegisteredWithGcmAndPCFPushButPermissionForGeofencesWasRemoved() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, false)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, false)
                .setupTags(null, null, null, false)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, NOT_USED, NOT_USED, GeofenceEngine.NEVER_UPDATED_GEOFENCES, true, false, false, true)
                .setupAreGeofencesEnabled(true, true, false, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(false)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testWasAlreadyRegisteredAndGeofencesAreNowEnabled() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, false)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, false)
                .setupTags(null, null, null, false)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(GeofenceEngine.NEVER_UPDATED_GEOFENCES, 0L, 1337L, 1337L, true, true, false, false)
                .setupAreGeofencesEnabled(false, true, true, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(false)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testWasAlreadyRegisteredAndGeofencesAreNowEnabledButThereIsNoPermissionForGeofences() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, false)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, false)
                .setupTags(null, null, null, false)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(GeofenceEngine.NEVER_UPDATED_GEOFENCES, NOT_USED, NOT_USED, GeofenceEngine.NEVER_UPDATED_GEOFENCES, true, false, false, true)
                .setupAreGeofencesEnabled(false, true, false, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(false)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testWasAlreadyRegisteredAndGeofencesAreNowDisabled() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, false)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, false)
                .setupTags(null, null, null, false)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, NOT_USED, NOT_USED, GeofenceEngine.NEVER_UPDATED_GEOFENCES, true, false, true, false)
                .setupAreGeofencesEnabled(true, false, false, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(false)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testWasAlreadyRegisteredAndGeofencesAreNowDisabledButClearGeofencesFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, false)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, false)
                .setupTags(null, null, null, false)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, NOT_USED, NOT_USED, 1337L, false, false, true, false)
                .setupAreGeofencesEnabled(true, false, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(false)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testWasAlreadyRegisteredAndGeofencesAreNowDisabledAndPermissionWasRemoved() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, false)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, false)
                .setupTags(null, null, null, false)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, NOT_USED, NOT_USED, GeofenceEngine.NEVER_UPDATED_GEOFENCES, true, false, false, true)
                .setupAreGeofencesEnabled(true, false, false, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(false)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testWasAlreadyRegisteredAndGeofencesAreNowDisabledAndPermissionWasRemovedButClearGeofencesFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, false)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, false)
                .setupTags(null, null, null, false)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, NOT_USED, NOT_USED, 1337L, false, false, false, true)
                .setupAreGeofencesEnabled(true, false, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(false)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(false)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testWasAlreadyRegisteredIncludingTagsWithGcmAndPCFPush() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, false)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, false)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, false)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false, false, false)
                .setupAreGeofencesEnabled(true, true, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(false)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testWasAlreadyRegisteredIncludingTagsWithGcmAndPCFPushAndTagsWereSavedUppercase() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, false)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, false)
                .setupTags(TEST_TAGS1, TEST_TAGS1_LOWER, TEST_TAGS1, false)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false, false, false)
                .setupAreGeofencesEnabled(true, true, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(false)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testWasAlreadyRegisteredWithGcmAndPCFPushAndThePlatformSecretIsChanged() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_2, TEST_PLATFORM_SECRET_2, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 0L, 50L, 50L, true, true, false, false) // Note that new geofences will be downloaded since the platform secret changed
                .setupAreGeofencesEnabled(true, true, true, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testWasAlreadyRegisteredWithGcmAndPCFPushAndTheDeviceAliasIsChanged() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_2, TEST_DEVICE_ALIAS_2, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false, false, false)
                .setupAreGeofencesEnabled(true, true, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testWasAlreadyRegisteredWithGcmAndPCFPushAndTheDeviceAliasIsCleared() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, "", null, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false, false, false)
                .setupAreGeofencesEnabled(true, true, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testWasAlreadyRegisteredWithGcmAndPCFPushAndTheDeviceAliasIsNulled() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, null, null, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false, false, false)
                .setupAreGeofencesEnabled(true, true, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testWasAlreadyRegisteredWithGcmAndPCFPushAndTheCustomUserIdIsChanged() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_2, TEST_CUSTOM_USER_ID_2, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false, false, false)
                .setupAreGeofencesEnabled(true, true, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testWasAlreadyRegisteredWithGcmAndPCFPushAndTheCustomUserIdIsCleared() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, "", null, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false, false, false)
                .setupAreGeofencesEnabled(true, true, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testWasAlreadyRegisteredWithGcmAndPCFPushAndTheCustomUserIdIsNulled() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, null, null, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false, false, false)
                .setupAreGeofencesEnabled(true, true, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testWasAlreadyRegisteredWithGcmAndPCFPushAndThePlatformUuidIsChanged() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_2, TEST_PLATFORM_UUID_2, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 0L, 50L, 50L, true, true, false, false) // Note that new geofences will be downloaded since the platform uuid changed
                .setupAreGeofencesEnabled(true, true, true, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testWasAlreadyRegisteredWithGcmAndPCFPushAndTheTagsAreChanged() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS2, TEST_TAGS2_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, NOT_USED, NOT_USED, 1337L, true, false, false, false)
                .setupAreGeofencesEnabled(true, true, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldGeofencesHaveBeenReregistered(true)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testWasAlreadyRegisteredWithGcmAndPCFPushAndTheTagsAreChangedButPermissionForGeofencesWasRemoved() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS2, TEST_TAGS2_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, NOT_USED, NOT_USED, GeofenceEngine.NEVER_UPDATED_GEOFENCES, true, false, false, true)
                .setupAreGeofencesEnabled(true, true, false, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testWasAlreadyRegisteredWithGcmAndPCFPushAndTheServiceUrlIsChanged() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_2, TEST_SERVICE_URL_2, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 0L, 50L, 50L, true, true, false, false)
                .setupAreGeofencesEnabled(true, true, true, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testWasAlreadyRegisteredWithGcmAndPCFPushAndTheServiceUrlIsChangedButPermissionForGeofencesWasRemoved() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_2, TEST_SERVICE_URL_2, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, NOT_USED, NOT_USED, GeofenceEngine.NEVER_UPDATED_GEOFENCES, true, false, false, true)
                .setupAreGeofencesEnabled(true, true, false, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testWasAlreadyRegisteredWithGcmButNotPCFPushAndPCFPushRegistrationSucceeds() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, null, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(null, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(null, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(null, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(null, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(null, null, EMPTY_SET, true)
                .setupServiceUrl(null, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(GeofenceEngine.NEVER_UPDATED_GEOFENCES, 0L, 1337L, 1337L, true, true, false, false)
                .setupAreGeofencesEnabled(false, true, true, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testWasAlreadyRegisteredWithGcmButNotPCFPushAndPCFPushRegistrationSucceedsWithoutPermissionsForGeofences() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, null, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(null, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(null, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(null, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(null, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(null, null, EMPTY_SET, true)
                .setupServiceUrl(null, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(GeofenceEngine.NEVER_UPDATED_GEOFENCES, NOT_USED, NOT_USED, GeofenceEngine.NEVER_UPDATED_GEOFENCES, true, false, false, true)
                .setupAreGeofencesEnabled(false, true, false, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testWasAlreadyRegisteredWithGcmButNotPCFPushAndPCFPushRegistrationSucceedsWithTags() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, null, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(null, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(null, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(null, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(null, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(null, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(null, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(GeofenceEngine.NEVER_UPDATED_GEOFENCES, 0L, 1337L, 1337L, true, true, false, false)
                .setupAreGeofencesEnabled(false, true, true, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testWasAlreadyRegisteredWithGcmButNotPCFPushAndPCFPushRegistrationFails() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, null, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(null, null, null)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(null, TEST_PLATFORM_UUID_1, null, true)
                .setupPlatformSecret(null, TEST_PLATFORM_SECRET_1, null, true)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, null, true)
                .setupCustomUserId(null, TEST_CUSTOM_USER_ID_1, null, true)
                .setupTags(null, null, null, true)
                .setupServiceUrl(null, TEST_SERVICE_URL_1, null, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(GeofenceEngine.NEVER_UPDATED_GEOFENCES, NOT_USED, NOT_USED, GeofenceEngine.NEVER_UPDATED_GEOFENCES, true, false, false, false)
                .setupAreGeofencesEnabled(false, true, false, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testWasAlreadyRegisteredWithGcmButNotPCFPushAndPCFPushRegistrationFailsWithTags() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, null, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(null, null, null)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(null, TEST_PLATFORM_UUID_1, null, true)
                .setupPlatformSecret(null, TEST_PLATFORM_SECRET_1, null, true)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, null, true)
                .setupCustomUserId(null, TEST_CUSTOM_USER_ID_1, null, true)
                .setupTags(null, TEST_TAGS1, null, true)
                .setupServiceUrl(null, TEST_SERVICE_URL_1, null, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(GeofenceEngine.NEVER_UPDATED_GEOFENCES, NOT_USED, NOT_USED, GeofenceEngine.NEVER_UPDATED_GEOFENCES, true, false, false, false)
                .setupAreGeofencesEnabled(false, true, false, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testWasAlreadyRegisteredWithGcmButNotPCFPushAndServerReturnsNullPCFPushRegistrationId() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, null, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationIdWithNullFromServer(null, null)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(null, TEST_PLATFORM_UUID_1, null, true)
                .setupPlatformSecret(null, TEST_PLATFORM_SECRET_1, null, true)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, null, true)
                .setupCustomUserId(null, TEST_CUSTOM_USER_ID_1, null, true)
                .setupTags(null, null, null, true)
                .setupServiceUrl(null, TEST_SERVICE_URL_1, null, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(GeofenceEngine.NEVER_UPDATED_GEOFENCES, NOT_USED, NOT_USED, GeofenceEngine.NEVER_UPDATED_GEOFENCES, true, false, false, false)
                .setupAreGeofencesEnabled(false, true, false, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testAppUpdatedAndSameGcmRegistrationIdWasReturned() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, false)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, false)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, false)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false, false, false)
                .setupAreGeofencesEnabled(true, true, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(false)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testAppUpdatedAndSameGcmRegistrationIdWasReturnedButPermissionForGeofencesWasRemoved() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, false)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, false)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, false)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, NOT_USED, NOT_USED, GeofenceEngine.NEVER_UPDATED_GEOFENCES, true, false, false, true)
                .setupAreGeofencesEnabled(true, true, false, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(false)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testAppUpdatedAndSameGcmRegistrationIdWasReturnedAndGeofencesAreNowEnabled() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, false)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, false)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, false)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(GeofenceEngine.NEVER_UPDATED_GEOFENCES, 0L, 1337L, 1337L, true, true, false, false)
                .setupAreGeofencesEnabled(false, true, true, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(false)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testAppUpdatedAndSameGcmRegistrationIdWasReturnedAndGeofencesAreNowEnabledWithoutPermissionForGeofences() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, false)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, false)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, false)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(GeofenceEngine.NEVER_UPDATED_GEOFENCES, NOT_USED, NOT_USED, GeofenceEngine.NEVER_UPDATED_GEOFENCES, true, false, false, true)
                .setupAreGeofencesEnabled(false, true, false, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(false)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testAppUpdatedAndSameGcmRegistrationIdWasReturnedAndGeofencesAreNowDisabled() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, false)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, false)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, false)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, NOT_USED, NOT_USED, GeofenceEngine.NEVER_UPDATED_GEOFENCES, true, false, true, false)
                .setupAreGeofencesEnabled(true, false, false, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(false)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testAppUpdatedAndSameGcmRegistrationIdWasReturnedAndGeofencesAreNowDisabledButPermissionForGeofencesIsRemoved() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, false)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, false)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, false)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, NOT_USED, NOT_USED, GeofenceEngine.NEVER_UPDATED_GEOFENCES, true, false, false, true)
                .setupAreGeofencesEnabled(true, false, false, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(false)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testAppUpdatedToLesserVersionAndSameGcmRegistrationIdWasReturned() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, false)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, false)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, false)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false, false, false)
                .setupAreGeofencesEnabled(true, true, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(2, 1, 1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(false)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testAppUpdatedAndGcmReregistrationFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, null, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, null, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, false)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, false)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, false)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false, false, false)
                .setupAreGeofencesEnabled(true, true, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(false)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testAppUpdatedAndGcmReregistrationReturnedNewIdButPCFPushReregistrationFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, null, null)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, null, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, null, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, null, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, null, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, null, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, null, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false, false, false)
                .setupAreGeofencesEnabled(true, true, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testAppUpdatedAndGcmReregistrationReturnedNewIdButPCFPushReregistrationFailedAndPermissionForGeofencesWasRemoved() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, null, null)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, null, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, null, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, null, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, null, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, null, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, null, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, NOT_USED, NOT_USED, GeofenceEngine.NEVER_UPDATED_GEOFENCES, true, false, false, true)
                .setupAreGeofencesEnabled(true, true, false, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(false)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testAppUpdatedAndGcmReregistrationReturnedNewIdButServerReturnsNullPCFPushRegistrationId() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupPCFPushDeviceRegistrationIdWithNullFromServer(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, null)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, null, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, null, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, null, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, null, true)
                .setupTags(TEST_TAGS1, TEST_TAGS1, null, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, null, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false, false, false)
                .setupAreGeofencesEnabled(true, true, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testUpdateRegistrationWithTagsFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, null, null)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, null, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, null, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, null, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, null, true)
                .setupTags(TEST_TAGS1, TEST_TAGS2, null, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, null, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false, false, false)
                .setupAreGeofencesEnabled(true, true, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldGeofencesHaveBeenReregistered(true)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testAppUpdatedAndDifferentGcmRegistrationIdWasReturned() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false, false, false)
                .setupAreGeofencesEnabled(true, true, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testAppUpdatedAndDifferentGcmRegistrationIdWasReturnedAndGeofencesAreNowDisabled() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, NOT_USED, NOT_USED, GeofenceEngine.NEVER_UPDATED_GEOFENCES, true, false, true, false)
                .setupAreGeofencesEnabled(true, false, false, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testAppUpdatedAndDifferentGcmRegistrationIdWasReturnedAndGeofencesAreNowDisabledButPermissionForGeofencesWasRemoved() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, NOT_USED, NOT_USED, GeofenceEngine.NEVER_UPDATED_GEOFENCES, true, false, false, true)
                .setupAreGeofencesEnabled(true, false, false, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testAppUpdatedAndDifferentGcmRegistrationIdWasReturnedAndGeofencesAreNowEnabled() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(GeofenceEngine.NEVER_UPDATED_GEOFENCES, 0L, 1337L, 1337L, true, true, false, false)
                .setupAreGeofencesEnabled(false, true, true, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testAppUpdatedAndDifferentGcmRegistrationIdWasReturnedAndGeofencesAreNowEnabledButPermissionsForGeofencesIsDenied() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(GeofenceEngine.NEVER_UPDATED_GEOFENCES, NOT_USED, NOT_USED, GeofenceEngine.NEVER_UPDATED_GEOFENCES, true, false, false, true)
                .setupAreGeofencesEnabled(false, true, false, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testAppUpdatedAndDifferentGcmRegistrationIdWasReturnedAndTheresANewPCFPushServerUrlToo() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_2, TEST_SERVICE_URL_2, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 0L, 50L, 50L, true, true, false, false)
                .setupAreGeofencesEnabled(true, true, true, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testAppUpdatedAndDifferentGcmRegistrationIdWasReturnedAndTheresANewPCFPushServerUrlTooButGeofencePermissionsAreDenied() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_2, TEST_SERVICE_URL_2, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, NOT_USED, NOT_USED, GeofenceEngine.NEVER_UPDATED_GEOFENCES, true, false, false, true)
                .setupAreGeofencesEnabled(true, true, false, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testAppUpdatedAndDifferentGcmRegistrationIdWasReturnedAndTheresANewPCFPushServerUrlTooAndPCFPushRegistrationFails() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, null, null)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, null, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, null, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, null, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, null, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, null, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_2, null, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false, false, false)
                .setupAreGeofencesEnabled(true, true, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testAppUpdatedAndDifferentGcmRegistrationIdWasReturnedAndTheresANewPCFPushServerUrlTooAndPCFPushRegistrationFailsAndPermissionForGeofencesWasRemoved() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, null, null)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, null, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, null, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, null, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, null, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, null, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_2, null, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, NOT_USED, NOT_USED, GeofenceEngine.NEVER_UPDATED_GEOFENCES, true, false, false, true)
                .setupAreGeofencesEnabled(true, true, false, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(false)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testAppUpdatedAndDifferentGcmRegistrationIdWasReturnedAndTheresANewPCFPushServerUrlTooAndTheServerReturnsANullPCFPushRegistrationId() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupPCFPushDeviceRegistrationIdWithNullFromServer(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, null)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, null, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, null, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, null, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, null, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, null, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_2, null, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false, false, false)
                .setupAreGeofencesEnabled(true, true, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testAppUpdatedAndDifferentGcmRegistrationIdWasReturnedButPlatformUuidFromPreviousRegistrationWasNotSaved() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(null, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 0L, 50L, 50L, true, true, false, false)
                .setupAreGeofencesEnabled(true, true, true, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testAppUpdatedAndDifferentGcmRegistrationIdWasReturnedButPCFPushRegistrationIdWasNotSaved() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupPCFPushDeviceRegistrationId(null, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 0L, 50L, 50L, true, true, false, false)
                .setupAreGeofencesEnabled(true, true, true, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testAppUpdatedAndDifferentGcmRegistrationIdWasReturnedButPCFPushRegistrationIdWasNotSavedAndGeofencesAreDisabled() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupPCFPushDeviceRegistrationId(null, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(GeofenceEngine.NEVER_UPDATED_GEOFENCES, NOT_USED, NOT_USED, GeofenceEngine.NEVER_UPDATED_GEOFENCES, true, false, false, false)
                .setupAreGeofencesEnabled(false, false, false, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testAppUpdatedAndDifferentGcmRegistrationIdWasReturnedButPCFPushRegistrationIdWasNotSavedAndGeofencesAreNowEnabled() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupPCFPushDeviceRegistrationId(null, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(GeofenceEngine.NEVER_UPDATED_GEOFENCES, 0L, 1337L, 1337L, true, true, false, false)
                .setupAreGeofencesEnabled(false, true, true, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testAppUpdatedAndDifferentGcmRegistrationIdWasReturnedButPCFPushRegistrationIdWasNotSavedAndGeofencesAreNowDisabled() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupPCFPushDeviceRegistrationId(null, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, NOT_USED, NOT_USED, GeofenceEngine.NEVER_UPDATED_GEOFENCES, true, false, true, false)
                .setupAreGeofencesEnabled(true, false, false, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testAppUpdatedAndDifferentGcmRegistrationIdWasReturnedAndUnregisterFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false, false, false)
                .setupAreGeofencesEnabled(true, true, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 2, 2)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testPlatformSecretUpdatedAndUnregisterFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_2, TEST_PLATFORM_SECRET_2, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 0L, 50L, 50L, true, true, false, false)
                .setupAreGeofencesEnabled(true, true, true, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testCustomUserIdUpdatedAndUnregisterFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_2, TEST_CUSTOM_USER_ID_2, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false, false, false)
                .setupAreGeofencesEnabled(true, true, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testDeviceAliasUpdatedAndUnregisterFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_2, TEST_DEVICE_ALIAS_2, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false, false, false)
                .setupAreGeofencesEnabled(true, true, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testTagsUpdatedAndUnregisterFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS2, TEST_TAGS2_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false, false, false)
                .setupAreGeofencesEnabled(true, true, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldGeofencesHaveBeenReregistered(true)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testPlatformUuidUpdatedAndUnregisterFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_2, TEST_PLATFORM_UUID_2, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 0L, 50L, 50L, true, true, false, false)
                .setupAreGeofencesEnabled(true, true, true, true)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testAppUpdatedToLesserVersionAndDifferentGcmRegistrationIdWasReturned() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false, false, false)
                .setupAreGeofencesEnabled(true, true, true, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(2, 1, 1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testSenderIdUpdatedAndGcmUnregistrationFailsAndThenGcmReturnsANewRegistrationId() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_2, TEST_GCM_SENDER_ID_2, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false, false, false)
                .setupAreGeofencesEnabled(true, true, true, false)
                .setupGcmUnregisterDevice(true, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testSenderIdUpdatedAndGcmUnregistrationFailsAndThenGcmReturnsANewRegistrationIdButPermissionForGeofencesWasRemoved() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_2, TEST_GCM_SENDER_ID_2, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, NOT_USED, NOT_USED, GeofenceEngine.NEVER_UPDATED_GEOFENCES, true, false, false, true)
                .setupAreGeofencesEnabled(true, true, false, true)
                .setupGcmUnregisterDevice(true, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testSenderIdUpdatedAndGcmUnregistrationFailsAndThenGcmRegistrationFailsToo() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, null, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_2, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, false)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, false)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, false)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false, false, false)
                .setupAreGeofencesEnabled(true, true, true, false)
                .setupGcmUnregisterDevice(true, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(false)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testSenderIdUpdatedAndGcmReturnedNewGcmDeviceRegistrationId() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_2, TEST_GCM_SENDER_ID_2, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false, false, false)
                .setupAreGeofencesEnabled(true, true, true, false)
                .setupGcmUnregisterDevice(true, true)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testSenderIdUpdatedAndGcmReturnedOldGcmDeviceRegistrationId() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_2, TEST_GCM_SENDER_ID_2, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, false)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, false)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, false)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false, false, false)
                .setupAreGeofencesEnabled(true, true, true, false)
                .setupGcmUnregisterDevice(true, true)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(false)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testSenderIdUpdatedAndGcmReturnedOldGcmDeviceRegistrationIdWithoutPermissionForGeofences() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_2, TEST_GCM_SENDER_ID_2, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, false)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, false)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, false)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, NOT_USED, NOT_USED, GeofenceEngine.NEVER_UPDATED_GEOFENCES, true, false, false, true)
                .setupAreGeofencesEnabled(true, true, false, true)
                .setupGcmUnregisterDevice(true, true)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(false)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testSenderIdUpdatedAndGcmReregistrationFails() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, null, null)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_2, null, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, false)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, false)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, false)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false, false, false)
                .setupAreGeofencesEnabled(true, true, true, false)
                .setupGcmUnregisterDevice(true, true)
                .setupAppVersion(1, 1, PushPreferencesProvider.NO_SAVED_VERSION)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(false)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testSenderIdUpdatedAndGcmReturnedNewGcmDeviceRegistrationIdButPCFPushReregistrationFails() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_2, TEST_GCM_SENDER_ID_2, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, 1337L, NOT_USED, 1337L, true, false, false, false)
                .setupAreGeofencesEnabled(true, true, true, false)
                .setupGcmUnregisterDevice(true, true)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testSenderIdUpdatedAndGcmReturnedNewGcmDeviceRegistrationIdButPCFPushReregistrationFailsAndPermissionForGeofencesRemoved() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_2, TEST_GCM_DEVICE_REGISTRATION_ID_2)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_2, TEST_GCM_SENDER_ID_2, true)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(1337L, NOT_USED, NOT_USED, GeofenceEngine.NEVER_UPDATED_GEOFENCES, true, false, false, true)
                .setupAreGeofencesEnabled(true, true, false, true)
                .setupGcmUnregisterDevice(true, true)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testSuccessfulInitialRegistrationButFailsToGetGeofences() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(null, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(null, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(null, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, true)
                .setupPlatformUuid(null, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(null, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(null, null, EMPTY_SET, true)
                .setupServiceUrl(null, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(null, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(GeofenceEngine.NEVER_UPDATED_GEOFENCES, 0L, 0L, GeofenceEngine.NEVER_UPDATED_GEOFENCES, false, true, false, false)
                .setupAreGeofencesEnabled(false, true, false, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(true)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(true)
                .setShouldGcmProviderRegisterHaveBeenCalled(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testSuccessfullyRegisteredWithNoGeofencesAndGeofencesAreDisabled() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, false)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, false)
                .setupTags(EMPTY_SET, null, EMPTY_SET, false)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(GeofenceEngine.NEVER_UPDATED_GEOFENCES, NOT_USED, NOT_USED, GeofenceEngine.NEVER_UPDATED_GEOFENCES, true, false, false, false)
                .setupAreGeofencesEnabled(false, false, false, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(false)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testSuccessfullyRegisteredWithNoGeofencesAndGeofencesAreDisabledAndThereIsNoPermissionForGeofences() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupGcmDeviceRegistrationId(TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1, TEST_GCM_DEVICE_REGISTRATION_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGcmSenderId(TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, TEST_GCM_SENDER_ID_1, false)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, false)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, false)
                .setupTags(EMPTY_SET, null, EMPTY_SET, false)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setupGeofenceUpdateTimestamp(GeofenceEngine.NEVER_UPDATED_GEOFENCES, NOT_USED, NOT_USED, GeofenceEngine.NEVER_UPDATED_GEOFENCES, true, false, false, false)
                .setupAreGeofencesEnabled(false, false, false, false)
                .setupGcmUnregisterDevice(false, false)
                .setupAppVersion(1, 1, 1)
                .setShouldAppVersionHaveBeenSaved(false)
                .setShouldGcmDeviceRegistrationIdHaveBeenSaved(false)
                .setShouldGcmProviderRegisterHaveBeenCalled(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(false)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldGeofencesHaveBeenReregistered(false)
                .setShouldHavePermissionForGeofences(false)
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

    @NonNull
    private String getStringWithLength(int length) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append("a");
        }
        final String result = sb.toString();
        assertEquals(length, result.length());
        return result;
    }
}
