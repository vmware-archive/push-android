/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.registration;

import android.content.Context;
import android.content.pm.PackageManager;
import android.test.AndroidTestCase;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;

import io.pivotal.android.push.PushParameters;
import io.pivotal.android.push.backend.api.FakePCFPushUnregisterDeviceApiRequest;
import io.pivotal.android.push.backend.api.PCFPushUnregisterDeviceApiRequestProvider;
import io.pivotal.android.push.geofence.GeofenceEngine;
import io.pivotal.android.push.geofence.GeofenceStatusUtil;
import io.pivotal.android.push.geofence.GeofenceUpdater;
import io.pivotal.android.push.prefs.FakePushPreferencesProvider;
import io.pivotal.android.push.prefs.Pivotal;
import io.pivotal.android.push.util.Logger;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UnregistrationEngineTest extends AndroidTestCase {

    private static final String TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1 = "TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1";
    private static final String TEST_PLATFORM_UUID = "TEST_PLATFORM_UUID";
    private static final String TEST_PLATFORM_SECRET = "TEST_PLATFORM_SECRET";
    private static final String TEST_DEVICE_ALIAS = "TEST_DEVICE_ALIAS";
    private static final String TEST_CUSTOM_USER_ID = "TEST_CUSTOM_USER_ID";
    private static final String TEST_SERVICE_URL = "http://test.com";
    private static final Set<String> TEST_TAGS = new HashSet<>();

    private FakePushPreferencesProvider pushPreferencesProvider;
    private PCFPushUnregisterDeviceApiRequestProvider pcfPushUnregisterDeviceApiRequestProvider;
    private PushParameters parameters;
    private Semaphore semaphore = new Semaphore(0);
    private GeofenceUpdater geofenceUpdater;
    private GeofenceStatusUtil geofenceStatusUtil;
    private Context context;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("dexmaker.dexcache", mContext.getCacheDir().getPath());
        TEST_TAGS.addAll(Arrays.asList("DONKEYS", "BURROS"));
        parameters = new PushParameters(TEST_PLATFORM_UUID, TEST_PLATFORM_SECRET, TEST_SERVICE_URL, TEST_DEVICE_ALIAS, TEST_CUSTOM_USER_ID, TEST_TAGS, true, true, Pivotal.SslCertValidationMode.DEFAULT, null, null);
        pushPreferencesProvider = new FakePushPreferencesProvider(null, null, null, null, null, null, null, null, null, 0, false);
        pcfPushUnregisterDeviceApiRequestProvider = new PCFPushUnregisterDeviceApiRequestProvider(new FakePCFPushUnregisterDeviceApiRequest());
        geofenceUpdater = mock(GeofenceUpdater.class);
        geofenceStatusUtil = mock(GeofenceStatusUtil.class);
        context = mock(Context.class);
        when(context.checkCallingOrSelfPermission(anyString())).thenReturn(PackageManager.PERMISSION_GRANTED);    }

    public void testNullContext() {
        try {
            new UnregistrationEngine(null, pushPreferencesProvider, pcfPushUnregisterDeviceApiRequestProvider, geofenceUpdater, geofenceStatusUtil);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullPushPreferencesProvider() {
        try {
            new UnregistrationEngine(context, null, pcfPushUnregisterDeviceApiRequestProvider, geofenceUpdater, geofenceStatusUtil);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }


    public void testNullPCFPushApiUnregisterDeviceRequestProvider() {
        try {
            new UnregistrationEngine(context, pushPreferencesProvider, null, geofenceUpdater, geofenceStatusUtil);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullGeofenceUpdaterProvider() {
        try {
            new UnregistrationEngine(context, pushPreferencesProvider, pcfPushUnregisterDeviceApiRequestProvider, null, geofenceStatusUtil);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullGeofenceStatusUtil() {
        try {
            new UnregistrationEngine(context, pushPreferencesProvider, pcfPushUnregisterDeviceApiRequestProvider, geofenceUpdater, null);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullParameters() {
        try {
            final UnregistrationEngine engine = new UnregistrationEngine(context, pushPreferencesProvider, pcfPushUnregisterDeviceApiRequestProvider, geofenceUpdater, geofenceStatusUtil);
            engine.unregisterDevice(null, getListenerForUnregistration(false));
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullServiceUrl() {
        try {
            final UnregistrationEngine engine = new UnregistrationEngine(context, pushPreferencesProvider, pcfPushUnregisterDeviceApiRequestProvider, geofenceUpdater, geofenceStatusUtil);
            parameters = new PushParameters(TEST_PLATFORM_UUID, TEST_PLATFORM_SECRET, null, TEST_DEVICE_ALIAS, TEST_CUSTOM_USER_ID, null, true, true, Pivotal.SslCertValidationMode.DEFAULT, null, null);
            engine.unregisterDevice(parameters, getListenerForUnregistration(false));
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }
    public void testSuccessfulUnregistrationWithGeofencesDisabled() throws Exception {
        UnregistrationEngineTestParameters testParams = new UnregistrationEngineTestParameters()
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, null)
                .setupGeofences(GeofenceEngine.NEVER_UPDATED_GEOFENCES, false, false, false, true)
                .setShouldHavePermissionForGeofences(true)
                .setupParameters(parameters)
                .setShouldUnregistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testSuccessfulUnregistrationWithGeofencesEnabled() throws Exception {
        UnregistrationEngineTestParameters testParams = new UnregistrationEngineTestParameters()
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, null)
                .setupGeofences(1337L, true, true, false, true)
                .setShouldHavePermissionForGeofences(true)
                .setupParameters(parameters)
                .setShouldUnregistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testSuccessfulUnregistrationWithGeofencesEnabledWithPermissionsForGeofencesRemoved() throws Exception {
        UnregistrationEngineTestParameters testParams = new UnregistrationEngineTestParameters()
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, null)
                .setupGeofences(1337L, true, false, true, true)
                .setShouldHavePermissionForGeofences(false)
                .setupParameters(parameters)
                .setShouldUnregistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testSuccessfulUnregistrationButClearGeofencesFailed() throws Exception {
        UnregistrationEngineTestParameters testParams = new UnregistrationEngineTestParameters()
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, null)
                .setupGeofences(1337L, true, true, false, false)
                .setShouldHavePermissionForGeofences(true)
                .setupParameters(parameters)
                .setShouldUnregistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testUnregistrationWhePCFPushUnregistrationFailsWithGeofencesDisabled() throws Exception {
        UnregistrationEngineTestParameters testParams = new UnregistrationEngineTestParameters()
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGeofences(GeofenceEngine.NEVER_UPDATED_GEOFENCES, false, false, false, true)
                .setShouldHavePermissionForGeofences(true)
                .setupParameters(parameters)
                .setShouldUnregistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testUnregistrationWhenPCFPushUnregistrationFailsWithGeofencesEnabled() throws Exception {
        UnregistrationEngineTestParameters testParams = new UnregistrationEngineTestParameters()
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGeofences(1337L, true, true, false, true)
                .setShouldHavePermissionForGeofences(true)
                .setupParameters(parameters)
                .setShouldUnregistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testUnregistrationWhenPCFPushUnregistrationFailsWithGeofencesEnabledAndPermissionsForGeofencesRemoved() throws Exception {
        UnregistrationEngineTestParameters testParams = new UnregistrationEngineTestParameters()
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGeofences(1337L, true, false, true, true)
                .setShouldHavePermissionForGeofences(false)
                .setupParameters(parameters)
                .setShouldUnregistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testUnregistrationPCFPushUnregistrationFailsAndClearGeofencesFails() throws Exception {
        UnregistrationEngineTestParameters testParams = new UnregistrationEngineTestParameters()
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGeofences(1337L, true, true, false, false)
                .setShouldHavePermissionForGeofences(true)
                .setupParameters(parameters)
                .setShouldUnregistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testUnregistrationPCFPushUnregistrationFailsAndClearGeofencesFailsWithPermissionsForGeofencesRemoved() throws Exception {
        UnregistrationEngineTestParameters testParams = new UnregistrationEngineTestParameters()
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGeofences(1337L, true, false, true, false)
                .setShouldHavePermissionForGeofences(false)
                .setupParameters(parameters)
                .setShouldUnregistrationHaveSucceeded(false);
        testParams.run();
    }

    private UnregistrationListener getListenerForUnregistration(final boolean isSuccessfulUnregistration) {
        return new UnregistrationListener() {

            @Override
            public void onUnregistrationComplete() {
                assertTrue(isSuccessfulUnregistration);
                semaphore.release();
            }

            @Override
            public void onUnregistrationFailed(String reason) {
                if (isSuccessfulUnregistration) {
                    Logger.e("Test failed due to error:" + reason);
                }
                assertFalse(isSuccessfulUnregistration);
                semaphore.release();
            }
        };
    }
}
