/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.baidu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import io.pivotal.android.push.PushParameters;
import io.pivotal.android.push.backend.api.FakePCFPushRegistrationApiRequest;
import io.pivotal.android.push.backend.api.PCFPushRegistrationApiRequestProvider;
import io.pivotal.android.push.prefs.FakePushRequestHeaders;
import io.pivotal.android.push.prefs.Pivotal;
import io.pivotal.android.push.prefs.PushPreferencesBaidu;
import io.pivotal.android.push.registration.RegistrationListener;
import io.pivotal.android.push.util.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class RegistrationEngineTest {

    private final String TEST_BAIDU_CHANNEL_ID_1 = "TEST_BAIDU_CHANNEL_ID_1";
    private final String TEST_BAIDU_CHANNEL_ID_2 = "TEST_BAIDU_CHANNEL_ID_2";
    private final String TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1 = "TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1";
    private final String TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2 = "TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2";
    private final String TEST_DEVICE_ALIAS_1 = "TEST_DEVICE_ALIAS_1";
    private final String TEST_DEVICE_ALIAS_2 = "TEST_DEVICE_ALIAS_2";
    private final String TEST_CUSTOM_USER_ID_1 = "TEST_CUSTOM_USER_ID_1";
    private final String TEST_CUSTOM_USER_ID_2 = "TEST_CUSTOM_USER_ID_2";
    private final String TEST_PLATFORM_UUID_1 = "TEST_PLATFORM_UUID_1";
    private final String TEST_PLATFORM_UUID_2 = "TEST_PLATFORM_UUID_2";
    private final String TEST_PLATFORM_SECRET_1 = "TEST_PLATFORM_SECRET_1";
    private final String TEST_PLATFORM_SECRET_2 = "TEST_PLATFORM_SECRET_2";
    private final String TEST_SERVICE_URL_1 = "http://test1.com";
    private final String TEST_SERVICE_URL_2 = "http://test2.com";
    private final Set<String> TEST_TAGS1 = new HashSet<>();
    private final Set<String> TEST_TAGS1_LOWER = new HashSet<>();
    private final Set<String> TEST_TAGS2 = new HashSet<>();
    private final Set<String> TEST_TAGS2_LOWER = new HashSet<>();
    private final String TEST_PACKAGE_NAME = "TEST.PACKAGE.NAME";
    private final Set<String> EMPTY_SET = Collections.emptySet();
    private final String TEST_PLATFORM_TYPE = "some-platform";

    private PushPreferencesBaidu pushPreferences;
    private FakePushRequestHeaders pushRequestHeaders;
    private PCFPushRegistrationApiRequestProvider pcfPushRegistrationApiRequestProvider;
    private Semaphore semaphore = new Semaphore(0);
    private Context context;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache",
                InstrumentationRegistry.getContext().getCacheDir().getPath());
        TEST_TAGS1.addAll(Arrays.asList("CATS", "DOGS"));
        TEST_TAGS1_LOWER.addAll(Arrays.asList("cats", "dogs"));
        TEST_TAGS2.addAll(Arrays.asList("LEMURS", "MONKEYS"));
        TEST_TAGS2_LOWER.addAll(Arrays.asList("lemurs", "monkeys"));

        context = mock(Context.class);
        when(context.checkCallingOrSelfPermission(anyString()))
                .thenReturn(PackageManager.PERMISSION_GRANTED);

        pushPreferences = mock(PushPreferencesBaidu.class);

        pushRequestHeaders = new FakePushRequestHeaders();

        pcfPushRegistrationApiRequestProvider = new PCFPushRegistrationApiRequestProvider(
                new FakePCFPushRegistrationApiRequest(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1));
    }

    @Test
    public void testNullContext() {
        try {
            new RegistrationEngine(null, TEST_PACKAGE_NAME, pushPreferences, pushRequestHeaders,
                    pcfPushRegistrationApiRequestProvider);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    @Test
    public void testNullPackageName() {
        try {
            new RegistrationEngine(context, null, pushPreferences, pushRequestHeaders,
                    pcfPushRegistrationApiRequestProvider);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    @Test
    public void testNullPushPreferences() {
        try {
            new RegistrationEngine(context, TEST_PACKAGE_NAME, null, pushRequestHeaders,
                    pcfPushRegistrationApiRequestProvider);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    @Test
    public void testNullPushRequestHeaders() {
        try {
            new RegistrationEngine(context, TEST_PACKAGE_NAME, pushPreferences, null,
                    pcfPushRegistrationApiRequestProvider);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    @Test
    public void testNullPCFPushRegisterDeviceApiRequestProvider() {
        try {
            new RegistrationEngine(context, TEST_PACKAGE_NAME, pushPreferences, pushRequestHeaders,
                    null);
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    @Test
    public void testNullParameters() {
        try {
            final RegistrationEngine engine = new RegistrationEngine(context, TEST_PACKAGE_NAME, pushPreferences, pushRequestHeaders,
                    pcfPushRegistrationApiRequestProvider);
            engine.registerDevice(null, "some-channel-id", getListenerForRegistration(false));
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    @Test
    public void testNullPlatformUuid() {
        try {
            final RegistrationEngine engine = new RegistrationEngine(context, TEST_PACKAGE_NAME, pushPreferences, pushRequestHeaders,
                    pcfPushRegistrationApiRequestProvider);
            engine.registerDevice(
                    new PushParameters(null, TEST_PLATFORM_SECRET_1, TEST_SERVICE_URL_1, TEST_PLATFORM_TYPE,
                            TEST_DEVICE_ALIAS_1, TEST_CUSTOM_USER_ID_1, null, true, true,
                            Pivotal.SslCertValidationMode.DEFAULT, null, null),
                    "some-channel-id", getListenerForRegistration(false));
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    @Test
    public void testNullPlatformSecret() {
        try {
            final RegistrationEngine engine = new RegistrationEngine(context, TEST_PACKAGE_NAME, pushPreferences, pushRequestHeaders,
                    pcfPushRegistrationApiRequestProvider);
            engine.registerDevice(new PushParameters(TEST_PLATFORM_UUID_1, null, TEST_SERVICE_URL_1, TEST_PLATFORM_TYPE,
                            TEST_DEVICE_ALIAS_1, TEST_CUSTOM_USER_ID_1, null, true, true,
                            Pivotal.SslCertValidationMode.DEFAULT, null, null),
                    "some-channel-id", getListenerForRegistration(false));
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    @Test
    public void testNullDeviceAlias() throws InterruptedException {
        final RegistrationEngine engine = new RegistrationEngine(context, TEST_PACKAGE_NAME, pushPreferences, pushRequestHeaders,
                pcfPushRegistrationApiRequestProvider);
        engine.registerDevice(
                new PushParameters(TEST_PLATFORM_UUID_1, TEST_PLATFORM_SECRET_1, TEST_SERVICE_URL_1, TEST_PLATFORM_TYPE,
                        TEST_CUSTOM_USER_ID_1, null, null, true, true, Pivotal.SslCertValidationMode.DEFAULT,
                        null, null), "some-channel-id", getListenerForRegistration(true));
        semaphore.acquire();
    }

    @Test
    public void testEmptyDeviceAlias() throws InterruptedException {
        final RegistrationEngine engine = new RegistrationEngine(context, TEST_PACKAGE_NAME, pushPreferences, pushRequestHeaders,
                pcfPushRegistrationApiRequestProvider);
        engine.registerDevice(
                new PushParameters(TEST_PLATFORM_UUID_1, TEST_PLATFORM_SECRET_1, TEST_SERVICE_URL_1, TEST_PLATFORM_TYPE, "",
                        TEST_CUSTOM_USER_ID_1, null, true, true, Pivotal.SslCertValidationMode.DEFAULT, null,
                        null), "some-channel-id", getListenerForRegistration(true));
        semaphore.acquire();
    }

    @Test
    public void testNullCustomUserId() throws InterruptedException {
        final RegistrationEngine engine = new RegistrationEngine(context, TEST_PACKAGE_NAME, pushPreferences, pushRequestHeaders,
                pcfPushRegistrationApiRequestProvider);
        engine.registerDevice(
                new PushParameters(TEST_PLATFORM_UUID_1, TEST_PLATFORM_SECRET_1, TEST_SERVICE_URL_1, TEST_PLATFORM_TYPE,
                        TEST_DEVICE_ALIAS_1, null, null, true, true, Pivotal.SslCertValidationMode.DEFAULT, null,
                        null), "some-channel-id", getListenerForRegistration(true));
        semaphore.acquire();
    }

    @Test
    public void testEmptyCustomUserId() throws InterruptedException {
        final RegistrationEngine engine = new RegistrationEngine(context, TEST_PACKAGE_NAME, pushPreferences, pushRequestHeaders,
                pcfPushRegistrationApiRequestProvider);
        engine.registerDevice(
                new PushParameters(TEST_PLATFORM_UUID_1, TEST_PLATFORM_SECRET_1, TEST_SERVICE_URL_1, TEST_PLATFORM_TYPE,
                        TEST_DEVICE_ALIAS_1, "", null, true, true, Pivotal.SslCertValidationMode.DEFAULT, null,
                        null), "some-channel-id", getListenerForRegistration(true));
        semaphore.acquire();
    }

    @Test
    public void test255LongCustomUserId_isAccepted() throws InterruptedException {
        final String longString = getStringWithLength(255);
        final RegistrationEngine engine = new RegistrationEngine(context, TEST_PACKAGE_NAME, pushPreferences, pushRequestHeaders,
                pcfPushRegistrationApiRequestProvider);
        engine.registerDevice(
                new PushParameters(TEST_PLATFORM_UUID_1, TEST_PLATFORM_SECRET_1, TEST_SERVICE_URL_1, TEST_PLATFORM_TYPE,
                        TEST_DEVICE_ALIAS_1, longString, null, true, true, Pivotal.SslCertValidationMode.DEFAULT,
                        null, null), "some-channel-id", getListenerForRegistration(true));
        semaphore.acquire();
    }

    @Test
    public void test256LongCustomUserId_isRejected() {
        try {
            final String longString = getStringWithLength(256);
            final RegistrationEngine engine = new RegistrationEngine(context, TEST_PACKAGE_NAME, pushPreferences, pushRequestHeaders,
                    pcfPushRegistrationApiRequestProvider);
            engine.registerDevice(
                    new PushParameters(TEST_PLATFORM_UUID_1, TEST_PLATFORM_SECRET_1, TEST_SERVICE_URL_1, TEST_PLATFORM_TYPE,
                            TEST_DEVICE_ALIAS_1, longString, null, true, true,
                            Pivotal.SslCertValidationMode.DEFAULT, null, null),
                    "some-channel-id", getListenerForRegistration(false));
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    @Test
    public void testEmptyServiceUrl() throws InterruptedException {
        try {
            final RegistrationEngine engine = new RegistrationEngine(context, TEST_PACKAGE_NAME, pushPreferences, pushRequestHeaders,
                    pcfPushRegistrationApiRequestProvider);
            engine.registerDevice(
                    new PushParameters(TEST_PLATFORM_UUID_1, TEST_PLATFORM_SECRET_1, null, TEST_PLATFORM_TYPE,
                            TEST_DEVICE_ALIAS_1, TEST_CUSTOM_USER_ID_1, null, true, true,
                            Pivotal.SslCertValidationMode.DEFAULT, null, null),
                    "some-channel-id", getListenerForRegistration(true));
            semaphore.acquire();
            fail("should not have succeeded");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    @Test
    public void testSuccessfulInitialRegistration() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupBaiduChannelId(null, TEST_BAIDU_CHANNEL_ID_1,
                        TEST_BAIDU_CHANNEL_ID_1)
                .setupPCFPushDeviceRegistrationId(null, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1,
                        TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupPlatformUuid(null, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(null, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(null, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(null, null, EMPTY_SET, true)
                .setupServiceUrl(null, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(null, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setShouldBaiduChannelIdHaveBeenSaved(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    @Test
    public void testSuccessfulInitialRegistrationWithoutPermissionForGeofences() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupBaiduChannelId(null, TEST_BAIDU_CHANNEL_ID_1,
                        TEST_BAIDU_CHANNEL_ID_1)
                .setupPCFPushDeviceRegistrationId(null, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1,
                        TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupPlatformUuid(null, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(null, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(null, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(null, null, EMPTY_SET, true)
                .setupServiceUrl(null, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(null, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setShouldBaiduChannelIdHaveBeenSaved(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    @Test
    public void testSuccessfulInitialRegistrationWithGeofencesDisabled() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupBaiduChannelId(null, TEST_BAIDU_CHANNEL_ID_1,
                        TEST_BAIDU_CHANNEL_ID_1)
                .setupPCFPushDeviceRegistrationId(null, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1,
                        TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupPlatformUuid(null, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(null, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(null, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(null, null, EMPTY_SET, true)
                .setupServiceUrl(null, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(null, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setShouldBaiduChannelIdHaveBeenSaved(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    @Test
    public void testSuccessfulInitialRegistrationWithTags() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupBaiduChannelId(null, TEST_BAIDU_CHANNEL_ID_1,
                        TEST_BAIDU_CHANNEL_ID_1)
                .setupPCFPushDeviceRegistrationId(null, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1,
                        TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupPlatformUuid(null, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(null, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(null, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(null, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(null, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(null, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setShouldBaiduChannelIdHaveBeenSaved(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    @Test
    public void testSuccessfulInitialRegistrationWithTagsWithoutPermissionForGeofences() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupBaiduChannelId(null, TEST_BAIDU_CHANNEL_ID_1,
                        TEST_BAIDU_CHANNEL_ID_1)
                .setupPCFPushDeviceRegistrationId(null, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1,
                        TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupPlatformUuid(null, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, true)
                .setupPlatformSecret(null, TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(null, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, true)
                .setupTags(null, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(null, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(null, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setShouldBaiduChannelIdHaveBeenSaved(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }


    public void testInitialPCFPushRegistrationFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupBaiduChannelId(null, TEST_BAIDU_CHANNEL_ID_1,
                        TEST_BAIDU_CHANNEL_ID_1)
                .setupPCFPushDeviceRegistrationId(null, null, null)
                .setupPlatformUuid(null, TEST_PLATFORM_UUID_1, null, true)
                .setupPlatformSecret(null, TEST_PLATFORM_SECRET_1, null, true)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, null, true)
                .setupCustomUserId(null, TEST_CUSTOM_USER_ID_1, null, true)
                .setupTags(null, TEST_TAGS1, null, true)
                .setupServiceUrl(null, TEST_SERVICE_URL_1, null, true)
                .setupPackageName(null, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setShouldBaiduChannelIdHaveBeenSaved(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(false)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    @Test
    public void testServerReturnedNullPCFPushRegistrationId() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupBaiduChannelId(null, TEST_BAIDU_CHANNEL_ID_1,
                        TEST_BAIDU_CHANNEL_ID_1)
                .setupPCFPushDeviceRegistrationIdWithNullFromServer(null, null)
                .setupPlatformUuid(null, TEST_PLATFORM_UUID_1, null, true)
                .setupPlatformSecret(null, TEST_PLATFORM_SECRET_1, null, true)
                .setupDeviceAlias(null, TEST_DEVICE_ALIAS_1, null, true)
                .setupCustomUserId(null, TEST_CUSTOM_USER_ID_1, null, true)
                .setupTags(null, TEST_TAGS1, null, true)
                .setupServiceUrl(null, TEST_SERVICE_URL_1, null, true)
                .setupPackageName(null, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setShouldBaiduChannelIdHaveBeenSaved(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    @Test
    public void testWasAlreadyRegistered() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupBaiduChannelId(TEST_BAIDU_CHANNEL_ID_1, TEST_BAIDU_CHANNEL_ID_1,
                        TEST_BAIDU_CHANNEL_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1,
                        TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1,
                        false)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1,
                        TEST_PLATFORM_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1,
                        false)
                .setupTags(null, null, null, false)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setShouldBaiduChannelIdHaveBeenSaved(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(false)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    @Test
    public void testWasAlreadyRegisteredIncludingTags() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupBaiduChannelId(TEST_BAIDU_CHANNEL_ID_1, TEST_BAIDU_CHANNEL_ID_1,
                        TEST_BAIDU_CHANNEL_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1,
                        TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1,
                        false)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1,
                        TEST_PLATFORM_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1,
                        false)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, false)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setShouldBaiduChannelIdHaveBeenSaved(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(false)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    @Test
    public void testWasAlreadyRegisteredIncludingTagsAndTagsWereSavedUppercase() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupBaiduChannelId(TEST_BAIDU_CHANNEL_ID_1, TEST_BAIDU_CHANNEL_ID_1,
                        TEST_BAIDU_CHANNEL_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1,
                        TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1,
                        false)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1,
                        TEST_PLATFORM_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1,
                        false)
                .setupTags(TEST_TAGS1, TEST_TAGS1_LOWER, TEST_TAGS1, false)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setShouldBaiduChannelIdHaveBeenSaved(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(false)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    @Test
    public void testWasAlreadyRegisteredAndThePlatformSecretIsChanged() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupBaiduChannelId(TEST_BAIDU_CHANNEL_ID_1, TEST_BAIDU_CHANNEL_ID_1,
                        TEST_BAIDU_CHANNEL_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1,
                        TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1,
                        true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_2,
                        TEST_PLATFORM_SECRET_2, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1,
                        true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setShouldBaiduChannelIdHaveBeenSaved(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    @Test
    public void testWasAlreadyRegisteredAndTheDeviceAliasIsChanged() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupBaiduChannelId(TEST_BAIDU_CHANNEL_ID_1, TEST_BAIDU_CHANNEL_ID_1,
                        TEST_BAIDU_CHANNEL_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1,
                        TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1,
                        true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1,
                        TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_2, TEST_DEVICE_ALIAS_2, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1,
                        true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setShouldBaiduChannelIdHaveBeenSaved(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    @Test
    public void testWasAlreadyRegisteredAndTheDeviceAliasIsCleared() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupBaiduChannelId(TEST_BAIDU_CHANNEL_ID_1, TEST_BAIDU_CHANNEL_ID_1,
                        TEST_BAIDU_CHANNEL_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1,
                        TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1,
                        true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1,
                        TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, "", null, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1,
                        true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setShouldBaiduChannelIdHaveBeenSaved(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    @Test
    public void testWasAlreadyRegisteredAndTheDeviceAliasIsNulled() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupBaiduChannelId(TEST_BAIDU_CHANNEL_ID_1, TEST_BAIDU_CHANNEL_ID_1,
                        TEST_BAIDU_CHANNEL_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1,
                        TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1,
                        true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1,
                        TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, null, null, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1,
                        true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setShouldBaiduChannelIdHaveBeenSaved(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    @Test
    public void testWasAlreadyRegisteredTheCustomUserIdIsChanged() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupBaiduChannelId(TEST_BAIDU_CHANNEL_ID_1, TEST_BAIDU_CHANNEL_ID_1,
                        TEST_BAIDU_CHANNEL_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1,
                        TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1,
                        true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1,
                        TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_2, TEST_CUSTOM_USER_ID_2,
                        true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setShouldBaiduChannelIdHaveBeenSaved(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    @Test
    public void testWasAlreadyRegisteredAndTheCustomUserIdIsCleared() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupBaiduChannelId(TEST_BAIDU_CHANNEL_ID_1, TEST_BAIDU_CHANNEL_ID_1,
                        TEST_BAIDU_CHANNEL_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1,
                        TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1,
                        true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1,
                        TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, "", null, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setShouldBaiduChannelIdHaveBeenSaved(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    @Test
    public void testWasAlreadyRegisteredAndTheCustomUserIdIsNulled() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupBaiduChannelId(TEST_BAIDU_CHANNEL_ID_1, TEST_BAIDU_CHANNEL_ID_1,
                        TEST_BAIDU_CHANNEL_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1,
                        TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1,
                        true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1,
                        TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, null, null, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setShouldBaiduChannelIdHaveBeenSaved(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    @Test
    public void testWasAlreadyRegisteredAndThePlatformUuidIsChanged() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupBaiduChannelId(TEST_BAIDU_CHANNEL_ID_1, TEST_BAIDU_CHANNEL_ID_1,
                        TEST_BAIDU_CHANNEL_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1,
                        TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_2, TEST_PLATFORM_UUID_2,
                        true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1,
                        TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1,
                        true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setShouldBaiduChannelIdHaveBeenSaved(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    @Test
    public void testWasAlreadyRegisteredAndTheTagsAreChanged() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupBaiduChannelId(TEST_BAIDU_CHANNEL_ID_1, TEST_BAIDU_CHANNEL_ID_1,
                        TEST_BAIDU_CHANNEL_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1,
                        TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1,
                        true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1,
                        TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1,
                        true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS2, TEST_TAGS2_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setShouldBaiduChannelIdHaveBeenSaved(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    @Test
    public void testWasAlreadyRegisteredAndTheServiceUrlIsChanged() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupBaiduChannelId(TEST_BAIDU_CHANNEL_ID_1, TEST_BAIDU_CHANNEL_ID_1,
                        TEST_BAIDU_CHANNEL_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1,
                        TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1,
                        true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1,
                        TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1,
                        true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_2, TEST_SERVICE_URL_2, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setShouldBaiduChannelIdHaveBeenSaved(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    @Test
    public void testWasAlreadyRegisteredAndTheServiceUrlIsChangedButPermissionForGeofencesWasRemoved() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupBaiduChannelId(TEST_BAIDU_CHANNEL_ID_1, TEST_BAIDU_CHANNEL_ID_1,
                        TEST_BAIDU_CHANNEL_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1,
                        TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1,
                        true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1,
                        TEST_PLATFORM_SECRET_1, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1,
                        true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_2, TEST_SERVICE_URL_2, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setShouldBaiduChannelIdHaveBeenSaved(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    @Test
    public void testUpdateRegistrationWithTagsFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupBaiduChannelId(TEST_BAIDU_CHANNEL_ID_1, TEST_BAIDU_CHANNEL_ID_2,
                        TEST_BAIDU_CHANNEL_ID_2)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, null, null)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, null, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, null, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, null, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, null, true)
                .setupTags(TEST_TAGS1, TEST_TAGS2, null, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, null, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setShouldBaiduChannelIdHaveBeenSaved(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    @Test
    public void testPlatformSecretUpdatedAndRegisterFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupBaiduChannelId(TEST_BAIDU_CHANNEL_ID_1, TEST_BAIDU_CHANNEL_ID_1,
                        TEST_BAIDU_CHANNEL_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1,
                        TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1,
                        true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_2,
                        TEST_PLATFORM_SECRET_2, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1,
                        true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setShouldBaiduChannelIdHaveBeenSaved(true)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(true);
        testParams.run();
    }

    @Test
    public void testCustomUserIdUpdatedAndRegisterFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupBaiduChannelId(TEST_BAIDU_CHANNEL_ID_1, TEST_BAIDU_CHANNEL_ID_1,
                        TEST_BAIDU_CHANNEL_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1,
                        TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1,
                        false)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1,
                        TEST_PLATFORM_SECRET_1, false)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, false)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_2, TEST_CUSTOM_USER_ID_1,
                        false)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, TEST_TAGS1_LOWER, false)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, false)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setShouldBaiduChannelIdHaveBeenSaved(false)
                .setShouldPCFPushDeviceRegistrationBeSuccessful(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(false)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    @Test
    public void testDeviceAliasUpdatedAndRegisterFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupBaiduChannelId(TEST_BAIDU_CHANNEL_ID_1, TEST_BAIDU_CHANNEL_ID_1,
                        TEST_BAIDU_CHANNEL_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1,
                        null, null)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, null,
                        true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1,
                        null, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_2, null, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, null,
                        true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, null, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, null, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setShouldPCFPushDeviceRegistrationBeSuccessful(false)
                .setShouldBaiduChannelIdHaveBeenSaved(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    @Test
    public void testTagsUpdatedAndRegisterFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupBaiduChannelId(TEST_BAIDU_CHANNEL_ID_1, TEST_BAIDU_CHANNEL_ID_1, TEST_BAIDU_CHANNEL_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2, null)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_1, null, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, null, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, null, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, null, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS2, null, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, null, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setShouldPCFPushDeviceRegistrationBeSuccessful(false)
                .setShouldBaiduChannelIdHaveBeenSaved(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(false)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(true)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    @Test
    public void testPlatformUuidUpdatedAndRegisterFailed() {
        RegistrationEngineTestParameters testParams = new RegistrationEngineTestParameters()
                .setupBaiduChannelId(TEST_BAIDU_CHANNEL_ID_1, TEST_BAIDU_CHANNEL_ID_1, TEST_BAIDU_CHANNEL_ID_1)
                .setupPCFPushDeviceRegistrationId(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_1, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID_2, null)
                .setupPlatformUuid(TEST_PLATFORM_UUID_1, TEST_PLATFORM_UUID_2, null, true)
                .setupPlatformSecret(TEST_PLATFORM_SECRET_1, TEST_PLATFORM_SECRET_1, null, true)
                .setupDeviceAlias(TEST_DEVICE_ALIAS_1, TEST_DEVICE_ALIAS_1, null, true)
                .setupCustomUserId(TEST_CUSTOM_USER_ID_1, TEST_CUSTOM_USER_ID_1, null, true)
                .setupTags(TEST_TAGS1_LOWER, TEST_TAGS1, null, true)
                .setupServiceUrl(TEST_SERVICE_URL_1, TEST_SERVICE_URL_1, null, true)
                .setupPackageName(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME)
                .setShouldPCFPushDeviceRegistrationBeSuccessful(false)
                .setShouldBaiduChannelIdHaveBeenSaved(false)
                .setShouldPCFPushDeviceRegistrationHaveBeenSaved(true)
                .setShouldPCFPushNewRegistrationHaveBeenCalled(true)
                .setShouldPCFPushUpdateRegistrationHaveBeenCalled(false)
                .setShouldRegistrationHaveSucceeded(false);
        testParams.run();
    }

    private RegistrationListener getListenerForRegistration(
            final boolean isSuccessfulRegistration) {
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
