/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.registration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import io.pivotal.android.push.PushParameters;
import io.pivotal.android.push.backend.api.FakePCFPushUnregisterDeviceApiRequest;
import io.pivotal.android.push.backend.api.PCFPushUnregisterDeviceApiRequestProvider;
import io.pivotal.android.push.geofence.GeofenceEngine;
import io.pivotal.android.push.geofence.GeofenceStatusUtil;
import io.pivotal.android.push.geofence.GeofenceUpdater;
import io.pivotal.android.push.prefs.Pivotal;
import io.pivotal.android.push.prefs.PushPreferencesFCM;
import io.pivotal.android.push.util.Logger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class UnregistrationEngineTest {

    private static final String TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1 = "TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1";
    private static final String TEST_PLATFORM_UUID = "TEST_PLATFORM_UUID";
    private static final String TEST_PLATFORM_SECRET = "TEST_PLATFORM_SECRET";
    private static final String TEST_DEVICE_ALIAS = "TEST_DEVICE_ALIAS";
    private static final String TEST_CUSTOM_USER_ID = "TEST_CUSTOM_USER_ID";
    private static final String TEST_SERVICE_URL = "http://test.com";
    private static final Set<String> TEST_TAGS = new HashSet<>();

    private PushPreferencesFCM pushPreferences;
    private PCFPushUnregisterDeviceApiRequestProvider pcfPushUnregisterDeviceApiRequestProvider;
    private PushParameters parameters;
    private Semaphore semaphore = new Semaphore(0);
    private GeofenceUpdater geofenceUpdater;
    private GeofenceStatusUtil geofenceStatusUtil;
    private Context context;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", InstrumentationRegistry.getContext().getCacheDir().getPath());

        TEST_TAGS.addAll(Arrays.asList("DONKEYS", "BURROS"));
        parameters = new PushParameters(TEST_PLATFORM_UUID, TEST_PLATFORM_SECRET, TEST_SERVICE_URL, TEST_DEVICE_ALIAS, TEST_CUSTOM_USER_ID, TEST_TAGS, true, true, Pivotal.SslCertValidationMode.DEFAULT, null, null);

        pushPreferences = mock(PushPreferencesFCM.class);

        pcfPushUnregisterDeviceApiRequestProvider = new PCFPushUnregisterDeviceApiRequestProvider(new FakePCFPushUnregisterDeviceApiRequest());
        geofenceUpdater = mock(GeofenceUpdater.class);
        geofenceStatusUtil = mock(GeofenceStatusUtil.class);
        context = mock(Context.class);
        when(context.checkCallingOrSelfPermission(anyString())).thenReturn(PackageManager.PERMISSION_GRANTED);    }

    @Test
    public void testNullContext() {
        try {
            new UnregistrationEngine(null, pushPreferences, pcfPushUnregisterDeviceApiRequestProvider, geofenceUpdater,
                geofenceStatusUtil);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    @Test
    public void testNullPushPreferences() {
        try {
            new UnregistrationEngine(context, null, pcfPushUnregisterDeviceApiRequestProvider, geofenceUpdater, geofenceStatusUtil);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    @Test
    public void testNullPCFPushApiUnregisterDeviceRequestProvider() {
        try {
            new UnregistrationEngine(context, pushPreferences, null, geofenceUpdater, geofenceStatusUtil);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    @Test
    public void testNullGeofenceUpdaterProvider() {
        try {
            new UnregistrationEngine(context, pushPreferences, pcfPushUnregisterDeviceApiRequestProvider, null,
                geofenceStatusUtil);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    @Test
    public void testNullGeofenceStatusUtil() {
        try {
            new UnregistrationEngine(context, pushPreferences, pcfPushUnregisterDeviceApiRequestProvider,
                geofenceUpdater, null);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    @Test
    public void testNullParameters() {
        try {
            final UnregistrationEngine engine = new UnregistrationEngine(context, pushPreferences,
                pcfPushUnregisterDeviceApiRequestProvider, geofenceUpdater, geofenceStatusUtil);
            engine.unregisterDevice(null, getListenerForUnregistration(false));
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    @Test
    public void testNullServiceUrl() {
        try {
            parameters = new PushParameters(TEST_PLATFORM_UUID, TEST_PLATFORM_SECRET, null, TEST_DEVICE_ALIAS, TEST_CUSTOM_USER_ID, null, true, true, Pivotal.SslCertValidationMode.DEFAULT, null, null);
            final UnregistrationEngine engine = new UnregistrationEngine(context, pushPreferences,
                pcfPushUnregisterDeviceApiRequestProvider, geofenceUpdater, geofenceStatusUtil);
            engine.unregisterDevice(parameters, getListenerForUnregistration(false));
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    @Test
    public void testSuccessfulUnregistrationWithGeofencesDisabled() throws Exception {
        UnregistrationEngineTestParameters testParams = new UnregistrationEngineTestParameters()
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, null)
                .setupGeofences(GeofenceEngine.NEVER_UPDATED_GEOFENCES, false, false, false, true)
                .setShouldHavePermissionForGeofences(true)
                .setupParameters(parameters)
                .setShouldUnregistrationHaveSucceeded(true);
        testParams.run();
    }

    @Test
    public void testSuccessfulUnregistrationWithGeofencesEnabled() throws Exception {
        UnregistrationEngineTestParameters testParams = new UnregistrationEngineTestParameters()
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, null)
                .setupGeofences(1337L, true, true, false, true)
                .setShouldHavePermissionForGeofences(true)
                .setupParameters(parameters)
                .setShouldUnregistrationHaveSucceeded(true);
        testParams.run();
    }

    @Test
    public void testSuccessfulUnregistrationWithGeofencesEnabledWithPermissionsForGeofencesRemoved() throws Exception {
        UnregistrationEngineTestParameters testParams = new UnregistrationEngineTestParameters()
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, null)
                .setupGeofences(1337L, true, false, true, true)
                .setShouldHavePermissionForGeofences(false)
                .setupParameters(parameters)
                .setShouldUnregistrationHaveSucceeded(true);
        testParams.run();
    }

    @Test
    public void testSuccessfulUnregistrationButClearGeofencesFailed() throws Exception {
        UnregistrationEngineTestParameters testParams = new UnregistrationEngineTestParameters()
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, null)
                .setupGeofences(1337L, true, true, false, false)
                .setShouldHavePermissionForGeofences(true)
                .setupParameters(parameters)
                .setShouldUnregistrationHaveSucceeded(false);
        testParams.run();
    }

    @Test
    public void testUnregistrationWhePCFPushUnregistrationFailsWithGeofencesDisabled() throws Exception {
        UnregistrationEngineTestParameters testParams = new UnregistrationEngineTestParameters()
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGeofences(GeofenceEngine.NEVER_UPDATED_GEOFENCES, false, false, false, true)
                .setShouldHavePermissionForGeofences(true)
                .setupParameters(parameters)
                .setShouldUnregistrationHaveSucceeded(false);
        testParams.run();
    }

    @Test
    public void testUnregistrationWhenPCFPushUnregistrationFailsWithGeofencesEnabled() throws Exception {
        UnregistrationEngineTestParameters testParams = new UnregistrationEngineTestParameters()
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGeofences(1337L, true, true, false, true)
                .setShouldHavePermissionForGeofences(true)
                .setupParameters(parameters)
                .setShouldUnregistrationHaveSucceeded(false);
        testParams.run();
    }

    @Test
    public void testUnregistrationWhenPCFPushUnregistrationFailsWithGeofencesEnabledAndPermissionsForGeofencesRemoved() throws Exception {
        UnregistrationEngineTestParameters testParams = new UnregistrationEngineTestParameters()
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGeofences(1337L, true, false, true, true)
                .setShouldHavePermissionForGeofences(false)
                .setupParameters(parameters)
                .setShouldUnregistrationHaveSucceeded(false);
        testParams.run();
    }

    @Test
    public void testUnregistrationPCFPushUnregistrationFailsAndClearGeofencesFails() throws Exception {
        UnregistrationEngineTestParameters testParams = new UnregistrationEngineTestParameters()
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGeofences(1337L, true, true, false, false)
                .setShouldHavePermissionForGeofences(true)
                .setupParameters(parameters)
                .setShouldUnregistrationHaveSucceeded(false);
        testParams.run();
    }

    @Test
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
