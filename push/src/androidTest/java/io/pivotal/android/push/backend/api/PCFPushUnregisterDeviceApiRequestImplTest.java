/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.backend.api;

import android.test.AndroidTestCase;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.pivotal.android.push.PushParameters;
import io.pivotal.android.push.prefs.Pivotal;
import io.pivotal.android.push.util.DelayedLoop;
import io.pivotal.android.push.util.FakeHttpURLConnection;
import io.pivotal.android.push.util.FakeNetworkWrapper;

public class PCFPushUnregisterDeviceApiRequestImplTest extends AndroidTestCase {

    private static final long TEN_SECOND_TIMEOUT = 10000L;
    private static final String TEST_PCF_PUSH_DEVICE_REGISTRATION_ID = "TEST_PCF_PUSH_DEVICE_REGISTRATION_ID";
    private static final String TEST_PLATFORM_UUID = "TEST_PLATFORM_UUID";
    private static final String TEST_PLATFORM_SECRET = "TEST_PLATFORM_SECRET";
    private static final String TEST_DEVICE_ALIAS = "TEST_DEVICE_ALIAS";
    private static final String TEST_SERVICE_URL = "http://test.com";

    private PushParameters parameters;
    private FakeNetworkWrapper networkWrapper;
    private DelayedLoop delayedLoop;
    private io.pivotal.android.push.backend.api.PCFPushUnregisterDeviceListener PCFPushUnregisterDeviceListener;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        parameters = new PushParameters(TEST_PLATFORM_UUID, TEST_PLATFORM_SECRET, TEST_SERVICE_URL, "android-baidu",
            TEST_DEVICE_ALIAS, null, null, true, true, Pivotal.SslCertValidationMode.DEFAULT, null, null);
        networkWrapper = new FakeNetworkWrapper();
        delayedLoop = new DelayedLoop(TEN_SECOND_TIMEOUT);
        FakeHttpURLConnection.reset();
    }

    public void testRequiresContext() {
        try {
            new PCFPushUnregisterDeviceApiRequestImpl(null, networkWrapper);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testRequiresNetworkWrapper() {
        try {
            new PCFPushUnregisterDeviceApiRequestImpl(getContext(), null);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testRequiresPCFPushDeviceRegistrationId() {
        try {
            final PCFPushUnregisterDeviceApiRequestImpl request = new PCFPushUnregisterDeviceApiRequestImpl(getContext(), new FakeNetworkWrapper());
            makePCFPushUnegisterDeviceApiRequestListener(true, false, null);
            request.startUnregisterDevice(null, parameters, PCFPushUnregisterDeviceListener);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testRequiresParameters() {
        try {
            final PCFPushUnregisterDeviceApiRequestImpl request = new PCFPushUnregisterDeviceApiRequestImpl(getContext(), new FakeNetworkWrapper());
            makePCFPushUnegisterDeviceApiRequestListener(true, false, null);
            request.startUnregisterDevice(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID, null, PCFPushUnregisterDeviceListener);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testRequiresListener() {
        try {
            final PCFPushUnregisterDeviceApiRequestImpl request = new PCFPushUnregisterDeviceApiRequestImpl(getContext(), new FakeNetworkWrapper());
            request.startUnregisterDevice(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID, parameters, null);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testSuccessfulRequest() {
        makeListenersForSuccessfulRequestFromNetwork(true, 200, null);
        final PCFPushUnregisterDeviceApiRequestImpl request = new PCFPushUnregisterDeviceApiRequestImpl(getContext(), networkWrapper);
        request.startUnregisterDevice(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID, parameters, PCFPushUnregisterDeviceListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testSuccessfulRequestWithCustomRequestHeaders() {
        final Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("FUN HEADER", "FUN VALUE");
        requestHeaders.put("SAD HEADER", "SAD VALUE");
        makeListenersForSuccessfulRequestFromNetwork(true, 200, requestHeaders);
        parameters = new PushParameters(TEST_PLATFORM_UUID, TEST_PLATFORM_SECRET, TEST_SERVICE_URL, "android-baidu",
            TEST_DEVICE_ALIAS, null, null, true, true, Pivotal.SslCertValidationMode.DEFAULT, null, requestHeaders);
        final PCFPushUnregisterDeviceApiRequestImpl request = new PCFPushUnregisterDeviceApiRequestImpl(getContext(), networkWrapper);
        request.startUnregisterDevice(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID, parameters, PCFPushUnregisterDeviceListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testSuccessfulRequestSsl() {
        parameters = new PushParameters(TEST_PLATFORM_UUID, TEST_PLATFORM_SECRET, TEST_SERVICE_URL, "android-baidu",
            TEST_DEVICE_ALIAS, null, null, true, true, Pivotal.SslCertValidationMode.TRUST_ALL, null, null);
        makeListenersForSuccessfulRequestFromNetworkSsl(true, 200);
        final PCFPushUnregisterDeviceApiRequestImpl request = new PCFPushUnregisterDeviceApiRequestImpl(getContext(), networkWrapper);
        request.startUnregisterDevice(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID, parameters, PCFPushUnregisterDeviceListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testFailed405() {
        makeListenersForSuccessfulRequestFromNetwork(false, 405, null);
        final PCFPushUnregisterDeviceApiRequestImpl request = new PCFPushUnregisterDeviceApiRequestImpl(getContext(), networkWrapper);
        request.startUnregisterDevice(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID, parameters, PCFPushUnregisterDeviceListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    // 404 errors are not considered failures
    public void testSuccessful404() {
        makeListenersForSuccessfulRequestFromNetwork(true, 404, null);
        final PCFPushUnregisterDeviceApiRequestImpl request = new PCFPushUnregisterDeviceApiRequestImpl(getContext(), networkWrapper);
        request.startUnregisterDevice(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID, parameters, PCFPushUnregisterDeviceListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testFailed403() {
        makeListenersForSuccessfulRequestFromNetwork(false, 403, null);
        final PCFPushUnregisterDeviceApiRequestImpl request = new PCFPushUnregisterDeviceApiRequestImpl(getContext(), networkWrapper);
        request.startUnregisterDevice(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID, parameters, PCFPushUnregisterDeviceListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testCouldNotConnect() {
        makeListenersFromFailedRequestFromNetwork("Your server is busted");
        final PCFPushUnregisterDeviceApiRequestImpl request = new PCFPushUnregisterDeviceApiRequestImpl(getContext(), networkWrapper);
        request.startUnregisterDevice(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID, parameters, PCFPushUnregisterDeviceListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    private void makeListenersForSuccessfulRequestFromNetwork(boolean isSuccessfulResult, int expectedHttpStatusCode, Map<String, String> expectedRequestHeaders) {
        FakeHttpURLConnection.setResponseCode(expectedHttpStatusCode);
        makePCFPushUnegisterDeviceApiRequestListener(isSuccessfulResult, false, expectedRequestHeaders);
    }

    private void makeListenersForSuccessfulRequestFromNetworkSsl(boolean isSuccessfulResult, int expectedHttpStatusCode) {
        FakeHttpURLConnection.setResponseCode(expectedHttpStatusCode);
        makePCFPushUnegisterDeviceApiRequestListener(isSuccessfulResult, true, null);
    }

    private void makeListenersFromFailedRequestFromNetwork(String exceptionText) {
        IOException exception = null;
        if (exceptionText != null) {
            exception = new IOException(exceptionText);
        }
        FakeHttpURLConnection.willThrowConnectionException(true);
        FakeHttpURLConnection.setConnectionException(exception);
        makePCFPushUnegisterDeviceApiRequestListener(false, false, null);
    }

    public void makePCFPushUnegisterDeviceApiRequestListener(final boolean isSuccessfulRequest,
                                                             final boolean isTrustAllSslCertificates,
                                                             final Map<String, String> expectedRequestHeaders) {

        PCFPushUnregisterDeviceListener = new PCFPushUnregisterDeviceListener() {

            @Override
            public void onPCFPushUnregisterDeviceSuccess() {
                assertTrue(isSuccessfulRequest);
                assertEquals("DELETE", FakeHttpURLConnection.getReceivedHttpMethod());
                assertTrue(FakeHttpURLConnection.getRequestPropertiesMap().containsKey("Authorization"));
                assertEquals(isTrustAllSslCertificates, FakeHttpURLConnection.didCallSetSSLSocketFactory());

                if (expectedRequestHeaders != null) {
                    final Map<String, String> actualRequestHeaders = FakeHttpURLConnection.getRequestPropertiesMap();
                    for (Map.Entry<String, String> entry : expectedRequestHeaders.entrySet()) {
                        assertTrue(actualRequestHeaders.containsKey(entry.getKey()));
                        assertEquals(entry.getValue(), actualRequestHeaders.get(entry.getKey()));
                    }
                }

                if (isSuccessfulRequest) {
                    assertTrue(FakeHttpURLConnection.getReceivedURL().toString().endsWith(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID));
                    delayedLoop.flagSuccess();
                } else {
                    delayedLoop.flagFailure();
                }
            }

            @Override
            public void onPCFPushUnregisterDeviceFailed(String reason) {
                assertFalse(isSuccessfulRequest);
                if (isSuccessfulRequest) {
                    delayedLoop.flagFailure();
                } else {
                    delayedLoop.flagSuccess();
                }
            }
        };
    }
}
