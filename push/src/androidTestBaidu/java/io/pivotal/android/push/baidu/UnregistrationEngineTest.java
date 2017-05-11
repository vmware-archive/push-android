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
import io.pivotal.android.push.baidu.UnregistrationEngine;
import io.pivotal.android.push.baidu.UnregistrationEngineTestParameters;
import io.pivotal.android.push.prefs.Pivotal;
import io.pivotal.android.push.prefs.PushPreferencesBaidu;
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

    private final String TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1 = "TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1";
    private final String TEST_PLATFORM_UUID = "TEST_PLATFORM_UUID";
    private final String TEST_PLATFORM_SECRET = "TEST_PLATFORM_SECRET";
    private final String TEST_DEVICE_ALIAS = "TEST_DEVICE_ALIAS";
    private final String TEST_CUSTOM_USER_ID = "TEST_CUSTOM_USER_ID";
    private final String TEST_SERVICE_URL = "http://test.com";
    private final Set<String> TEST_TAGS = new HashSet<>();
    private final String TEST_PLATFORM_TYPE = "some-platform-type";

    private PushPreferencesBaidu pushPreferences;
    private PCFPushUnregisterDeviceApiRequestProvider pcfPushUnregisterDeviceApiRequestProvider;
    private PushParameters parameters;
    private Semaphore semaphore = new Semaphore(0);
    private Context context;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", InstrumentationRegistry.getContext().getCacheDir().getPath());

        TEST_TAGS.addAll(Arrays.asList("DONKEYS", "BURROS"));
        parameters = new PushParameters(TEST_PLATFORM_UUID, TEST_PLATFORM_SECRET, TEST_SERVICE_URL, TEST_PLATFORM_TYPE, TEST_DEVICE_ALIAS, TEST_CUSTOM_USER_ID, TEST_TAGS, true, true, Pivotal.SslCertValidationMode.DEFAULT, null, null);

        pushPreferences = mock(PushPreferencesBaidu.class);

        pcfPushUnregisterDeviceApiRequestProvider = new PCFPushUnregisterDeviceApiRequestProvider(new FakePCFPushUnregisterDeviceApiRequest());
        context = mock(Context.class);
        when(context.checkCallingOrSelfPermission(anyString())).thenReturn(PackageManager.PERMISSION_GRANTED);    }

    @Test
    public void testNullContext() {
        try {
            new UnregistrationEngine(null, pushPreferences, pcfPushUnregisterDeviceApiRequestProvider);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    @Test
    public void testNullPushPreferences() {
        try {
            new UnregistrationEngine(context, null, pcfPushUnregisterDeviceApiRequestProvider);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    @Test
    public void testNullPCFPushApiUnregisterDeviceRequestProvider() {
        try {
            new UnregistrationEngine(context, pushPreferences, null);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    @Test
    public void testNullParameters() {
        try {
            final UnregistrationEngine engine = new UnregistrationEngine(context, pushPreferences,
                pcfPushUnregisterDeviceApiRequestProvider);
            engine.unregisterDevice(null, getListenerForUnregistration(false));
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    @Test
    public void testNullServiceUrl() {
        try {
            parameters = new PushParameters(TEST_PLATFORM_UUID, TEST_PLATFORM_SECRET, null, TEST_PLATFORM_TYPE, TEST_DEVICE_ALIAS, TEST_CUSTOM_USER_ID, null, true, true, Pivotal.SslCertValidationMode.DEFAULT, null, null);
            final UnregistrationEngine engine = new UnregistrationEngine(context, pushPreferences,
                pcfPushUnregisterDeviceApiRequestProvider);
            engine.unregisterDevice(parameters, getListenerForUnregistration(false));
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    @Test
    public void testSuccessfulUnregistration() throws Exception {
        UnregistrationEngineTestParameters testParams = new UnregistrationEngineTestParameters()
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, null)
                .setupParameters(parameters)
                .setShouldUnregistrationHaveSucceeded(true);
        testParams.run();
    }

    @Test
    public void testUnregistrationWhePCFPushUnregistrationFails() throws Exception {
        UnregistrationEngineTestParameters testParams = new UnregistrationEngineTestParameters()
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
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
