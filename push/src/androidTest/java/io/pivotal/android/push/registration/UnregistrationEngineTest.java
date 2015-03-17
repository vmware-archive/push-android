/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.registration;

import android.test.AndroidTestCase;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;

import io.pivotal.android.push.PushParameters;
import io.pivotal.android.push.backend.api.FakePCFPushUnregisterDeviceApiRequest;
import io.pivotal.android.push.backend.api.PCFPushUnregisterDeviceApiRequestProvider;
import io.pivotal.android.push.gcm.FakeGcmProvider;
import io.pivotal.android.push.gcm.FakeGcmUnregistrationApiRequest;
import io.pivotal.android.push.gcm.GcmUnregistrationApiRequestProvider;
import io.pivotal.android.push.geofence.GeofenceEngine;
import io.pivotal.android.push.geofence.GeofenceUpdater;
import io.pivotal.android.push.prefs.FakePushPreferencesProvider;
import io.pivotal.android.push.util.Logger;

import static org.mockito.Mockito.mock;

public class UnregistrationEngineTest extends AndroidTestCase {

    private static final String TEST_GCM_DEVICE_REGISTRATION_ID_1 = "TEST_GCM_DEVICE_REGISTRATION_ID_1";
    private static final String TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1 = "TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1";
    private static final String TEST_GCM_SENDER_ID = "TEST_GCM_SENDER_ID";
    private static final String TEST_PLATFORM_UUID = "TEST_PLATFORM_UUID";
    private static final String TEST_PLATFORM_SECRET = "TEST_PLATFORM_SECRET";
    private static final String TEST_DEVICE_ALIAS = "TEST_DEVICE_ALIAS";
    private static final String TEST_SERVICE_URL = "http://test.com";
    private static final Set<String> TEST_TAGS = new HashSet<>();

    private FakeGcmProvider gcmProvider;
    private GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider;
    private FakePushPreferencesProvider pushPreferencesProvider;
    private PCFPushUnregisterDeviceApiRequestProvider pcfPushUnregisterDeviceApiRequestProvider;
    private PushParameters parameters;
    private Semaphore semaphore = new Semaphore(0);
    private GeofenceUpdater geofenceUpdater;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("dexmaker.dexcache", mContext.getCacheDir().getPath());
        TEST_TAGS.addAll(Arrays.asList("DONKEYS", "BURROS"));
        parameters = new PushParameters(TEST_GCM_SENDER_ID, TEST_PLATFORM_UUID, TEST_PLATFORM_SECRET, TEST_SERVICE_URL, TEST_DEVICE_ALIAS, TEST_TAGS, true);
        pushPreferencesProvider = new FakePushPreferencesProvider(null, null, 0, null, null, null, null, null, null, null, 0, false);
        gcmProvider = new FakeGcmProvider(TEST_GCM_DEVICE_REGISTRATION_ID_1);
        gcmUnregistrationApiRequestProvider = new GcmUnregistrationApiRequestProvider(new FakeGcmUnregistrationApiRequest(gcmProvider));
        pcfPushUnregisterDeviceApiRequestProvider = new PCFPushUnregisterDeviceApiRequestProvider(new FakePCFPushUnregisterDeviceApiRequest());
        geofenceUpdater = mock(GeofenceUpdater.class);
    }

    public void testNullContext() {
        try {
            new UnregistrationEngine(null, gcmProvider, pushPreferencesProvider, gcmUnregistrationApiRequestProvider, pcfPushUnregisterDeviceApiRequestProvider, geofenceUpdater);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullGcmProvider() {
        try {
            new UnregistrationEngine(getContext(), null, pushPreferencesProvider, gcmUnregistrationApiRequestProvider, pcfPushUnregisterDeviceApiRequestProvider, geofenceUpdater);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullPushPreferencesProvider() {
        try {
            new UnregistrationEngine(getContext(), gcmProvider, null, gcmUnregistrationApiRequestProvider, pcfPushUnregisterDeviceApiRequestProvider, geofenceUpdater);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullGcmUnregistrationApiRequestProvider() {
        try {
            new UnregistrationEngine(getContext(), gcmProvider, pushPreferencesProvider, null, pcfPushUnregisterDeviceApiRequestProvider, geofenceUpdater);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullPCFPushApiUnregisterDeviceRequestProvider() {
        try {
            new UnregistrationEngine(getContext(), gcmProvider, pushPreferencesProvider, gcmUnregistrationApiRequestProvider, null, geofenceUpdater);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullGeofenceUpdaterProvider() {
        try {
            new UnregistrationEngine(getContext(), gcmProvider, pushPreferencesProvider, gcmUnregistrationApiRequestProvider, pcfPushUnregisterDeviceApiRequestProvider, null);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullParameters() {
        try {
            final UnregistrationEngine engine = new UnregistrationEngine(getContext(), gcmProvider, pushPreferencesProvider, gcmUnregistrationApiRequestProvider, pcfPushUnregisterDeviceApiRequestProvider, geofenceUpdater);
            engine.unregisterDevice(null, getListenerForUnregistration(false));
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testNullServiceUrl() {
        try {
            final UnregistrationEngine engine = new UnregistrationEngine(getContext(),gcmProvider, pushPreferencesProvider, gcmUnregistrationApiRequestProvider, pcfPushUnregisterDeviceApiRequestProvider, geofenceUpdater);
            parameters = new PushParameters(TEST_GCM_SENDER_ID, TEST_PLATFORM_UUID, TEST_PLATFORM_SECRET, null, TEST_DEVICE_ALIAS, null, true);
            engine.unregisterDevice(parameters, getListenerForUnregistration(false));
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testGooglePlayServicesNotAvailable() throws InterruptedException {
        gcmProvider.setIsGooglePlayServicesInstalled(false);
        final UnregistrationEngine engine = new UnregistrationEngine(getContext(), gcmProvider, pushPreferencesProvider, gcmUnregistrationApiRequestProvider, pcfPushUnregisterDeviceApiRequestProvider, geofenceUpdater);
        engine.unregisterDevice(parameters, getListenerForUnregistration(false));
        semaphore.acquire();
    }

    public void testSuccessfulUnregistrationWithGeofencesDisabled() throws Exception {
        UnregistrationEngineTestParameters testParams = new UnregistrationEngineTestParameters(getContext())
                .setShouldGcmDeviceUnregistrationBeSuccessful(true)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, null)
                .setupGeofences(GeofenceEngine.NEVER_UPDATED_GEOFENCES, false, false, true)
                .setupParameters(parameters)
                .setShouldUnregistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testSuccessfulUnregistrationWithGeofencesEnabled() throws Exception {
        UnregistrationEngineTestParameters testParams = new UnregistrationEngineTestParameters(getContext())
                .setShouldGcmDeviceUnregistrationBeSuccessful(true)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, null)
                .setupGeofences(1337L, true, true, true)
                .setupParameters(parameters)
                .setShouldUnregistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testSuccessfulUnregistrationButClearGeofencesFailed() throws Exception {
        UnregistrationEngineTestParameters testParams = new UnregistrationEngineTestParameters(getContext())
                .setShouldGcmDeviceUnregistrationBeSuccessful(true)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, null)
                .setupGeofences(1337L, true, true, false)
                .setupParameters(parameters)
                .setShouldUnregistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testSuccessfulUnregistrationFromGcmOnlyWithGeofencesDisabled() throws Exception {
        UnregistrationEngineTestParameters testParams = new UnregistrationEngineTestParameters(getContext())
                .setShouldGcmDeviceUnregistrationBeSuccessful(true)
                .setupPCFPushDeviceRegistrationId(null, null)
                .setupGeofences(GeofenceEngine.NEVER_UPDATED_GEOFENCES, false, false, true)
                .setupParameters(parameters)
                .setShouldUnregistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testSuccessfulUnregistrationFromGcmOnlyWithGeofencesEnabled() throws Exception {
        UnregistrationEngineTestParameters testParams = new UnregistrationEngineTestParameters(getContext())
                .setShouldGcmDeviceUnregistrationBeSuccessful(true)
                .setupPCFPushDeviceRegistrationId(null, null)
                .setupGeofences(1337L, true, true, true)
                .setupParameters(parameters)
                .setShouldUnregistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testSuccessfulUnregistrationFromGcmOnlyButClearGeofencesDisabled() throws Exception {
        UnregistrationEngineTestParameters testParams = new UnregistrationEngineTestParameters(getContext())
                .setShouldGcmDeviceUnregistrationBeSuccessful(true)
                .setupPCFPushDeviceRegistrationId(null, null)
                .setupGeofences(1337L, true, true, false)
                .setupParameters(parameters)
                .setShouldUnregistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testUnregistrationWhenGcmUnregistrationFailsWithGeofencesDisabled() throws Exception {
        UnregistrationEngineTestParameters testParams = new UnregistrationEngineTestParameters(getContext())
                .setShouldGcmDeviceUnregistrationBeSuccessful(false)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, null)
                .setupGeofences(GeofenceEngine.NEVER_UPDATED_GEOFENCES, false, false, true)
                .setupParameters(parameters)
                .setShouldUnregistrationHaveSucceeded(true);
        testParams.run();
    }

    public void testUnregistrationWhenGcmUnregistrationFailsWithGeofencesEnabled() throws Exception {
        UnregistrationEngineTestParameters testParams = new UnregistrationEngineTestParameters(getContext())
                .setShouldGcmDeviceUnregistrationBeSuccessful(false)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, null)
                .setupGeofences(1337L, true, true, true)
                .setupParameters(parameters)
                .setShouldUnregistrationHaveSucceeded(true);
        testParams.run();
    }
    public void testUnregistrationWhenGcmUnregistrationFailsAndClearGeofencesFails() throws Exception {
        UnregistrationEngineTestParameters testParams = new UnregistrationEngineTestParameters(getContext())
                .setShouldGcmDeviceUnregistrationBeSuccessful(false)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, null)
                .setupGeofences(1337L, true, true, false)
                .setupParameters(parameters)
                .setShouldUnregistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testUnregistrationWhenGcmUnregistrationFailsAndPCFPushUnregistrationFailsWithGeofencesDisabled() throws Exception {
        UnregistrationEngineTestParameters testParams = new UnregistrationEngineTestParameters(getContext())
                .setShouldGcmDeviceUnregistrationBeSuccessful(false)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGeofences(GeofenceEngine.NEVER_UPDATED_GEOFENCES, false, false, true)
                .setupParameters(parameters)
                .setShouldUnregistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testUnregistrationWhenGcmUnregistrationFailsAndPCFPushUnregistrationFailsWithGeofencesEnabled() throws Exception {
        UnregistrationEngineTestParameters testParams = new UnregistrationEngineTestParameters(getContext())
                .setShouldGcmDeviceUnregistrationBeSuccessful(false)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGeofences(1337L, true, true, true)
                .setupParameters(parameters)
                .setShouldUnregistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testUnregistrationWhenGcmUnregistrationFailsAndPCFPushUnregistrationFailsAndClearGeofencesFails() throws Exception {
        UnregistrationEngineTestParameters testParams = new UnregistrationEngineTestParameters(getContext())
                .setShouldGcmDeviceUnregistrationBeSuccessful(false)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGeofences(1337L, true, true, false)
                .setupParameters(parameters)
                .setShouldUnregistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testUnregistrationWhenGcmUnregistrationSucceedsAndPCFPushUnregistrationFailsWithGeofencesDisabled() throws Exception {
        UnregistrationEngineTestParameters testParams = new UnregistrationEngineTestParameters(getContext())
                .setShouldGcmDeviceUnregistrationBeSuccessful(true)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGeofences(GeofenceEngine.NEVER_UPDATED_GEOFENCES, false, false, true)
                .setupParameters(parameters)
                .setShouldUnregistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testUnregistrationWhenGcmUnregistrationSucceedsAndPCFPushUnregistrationFailsWithGeofencesEnabled() throws Exception {
        UnregistrationEngineTestParameters testParams = new UnregistrationEngineTestParameters(getContext())
                .setShouldGcmDeviceUnregistrationBeSuccessful(true)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGeofences(1337L, true, true, true)
                .setupParameters(parameters)
                .setShouldUnregistrationHaveSucceeded(false);
        testParams.run();
    }

    public void testUnregistrationWhenGcmUnregistrationSucceedsAndPCFPushUnregistrationFailsAndClearGeofencesFails() throws Exception {
        UnregistrationEngineTestParameters testParams = new UnregistrationEngineTestParameters(getContext())
                .setShouldGcmDeviceUnregistrationBeSuccessful(true)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupGeofences(1337L, true, true, false)
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
