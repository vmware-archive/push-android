/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.backend.api;

import android.test.AndroidTestCase;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.pivotal.android.push.PushParameters;
import io.pivotal.android.push.model.api.PCFPushApiRegistrationPostRequestData;
import io.pivotal.android.push.model.api.PCFPushApiRegistrationPutRequestData;
import io.pivotal.android.push.prefs.Pivotal;
import io.pivotal.android.push.util.ApiRequestImpl;
import io.pivotal.android.push.util.DelayedLoop;
import io.pivotal.android.push.util.FakeHttpURLConnection;
import io.pivotal.android.push.util.FakeNetworkWrapper;

public class PCFPushRegistrationApiRequestImplTest extends AndroidTestCase {

    private static final String TEST_PCF_PUSH_DEVICE_REGISTRATION_ID = "TEST_PCF_PUSH_DEVICE_REGISTRATION_ID";
    private static final String TEST_GCM_DEVICE_REGISTRATION_ID = "TEST_GCM_DEVICE_REGISTRATION_ID";
    private static final String TEST_SENDER_ID = "TEST_SENDER_ID";
    private static final String TEST_PLATFORM_UUID = "TEST_PLATFORM_UUID";
    private static final String TEST_PLATFORM_SECRET = "TEST_PLATFORM_SECRET";
    private static final String TEST_DEVICE_ALIAS = "TEST_DEVICE_ALIAS";
    private static final String TEST_SERVICE_URL = "http://test.com";
    private static final String TEST_BASE64_ENCODED_AUTHORIZATION = "VEVTVF9QTEFURk9STV9VVUlEOlRFU1RfUExBVEZPUk1fU0VDUkVU";
    private static final String HTTP_POST = "POST";
    private static final String HTTP_PUT = "PUT";
    private static final long TEN_SECOND_TIMEOUT = 10000L;

    private FakeNetworkWrapper networkWrapper;
    private DelayedLoop delayedLoop;
    private PCFPushRegistrationListener listener;
    private Map<String, String> EXPECTED_REQUEST_HEADERS = new HashMap<>();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        networkWrapper = new FakeNetworkWrapper();
        delayedLoop = new DelayedLoop(TEN_SECOND_TIMEOUT);
        FakeHttpURLConnection.reset();
        EXPECTED_REQUEST_HEADERS.put("COOKIES", "COMPOST COOKIES");
        EXPECTED_REQUEST_HEADERS.put("CANDY", "CHOCOLATE ALMONDS SO ADDICTING");
    }

    public void testRequiresContext() {
        try {
            new PCFPushRegistrationApiRequestImpl(null, networkWrapper);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testRequiresNetworkWrapper() {
        try {
            new PCFPushRegistrationApiRequestImpl(getContext(), null);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testNewDeviceRegistrationRequiresGcmDeviceRegistrationId() {
        try {
            final PCFPushRegistrationApiRequestImpl request = new PCFPushRegistrationApiRequestImpl(getContext(), new FakeNetworkWrapper());
            makePCFPushRegistrationApiRequestListener(true, HTTP_POST, null, null, null, false, null);
            request.startNewDeviceRegistration(null, null, getParameters(), listener);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testNewDeviceRegistrationRequiresParameters() {
        try {
            final PCFPushRegistrationApiRequestImpl request = new PCFPushRegistrationApiRequestImpl(getContext(), new FakeNetworkWrapper());
            makePCFPushRegistrationApiRequestListener(true, HTTP_POST, null, null, null, false, null);
            request.startNewDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, null, null, listener);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testNewDeviceRegistrationRequiresListener() {
        try {
            final PCFPushRegistrationApiRequestImpl request = new PCFPushRegistrationApiRequestImpl(getContext(), new FakeNetworkWrapper());
            request.startNewDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, null, getParameters(), null);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testUpdateDeviceRegistrationRequiresGcmDeviceRegistrationId() {
        try {
            final PCFPushRegistrationApiRequestImpl request = new PCFPushRegistrationApiRequestImpl(getContext(), new FakeNetworkWrapper());
            makePCFPushRegistrationApiRequestListener(true, HTTP_POST, null, null, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID, false, null);
            request.startUpdateDeviceRegistration(null, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID, null, getParameters(), listener);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testUpdateDeviceRegistrationRequiresPCFPushDeviceRegistrationId() {
        try {
            final PCFPushRegistrationApiRequestImpl request = new PCFPushRegistrationApiRequestImpl(getContext(), new FakeNetworkWrapper());
            makePCFPushRegistrationApiRequestListener(true, HTTP_PUT, null, null, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID, false, null);
            request.startUpdateDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, null, null, getParameters(), listener);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testUpdateDeviceRegistrationRequiresParameters() {
        try {
            final PCFPushRegistrationApiRequestImpl request = new PCFPushRegistrationApiRequestImpl(getContext(), new FakeNetworkWrapper());
            makePCFPushRegistrationApiRequestListener(true, HTTP_PUT, null, null, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID, false, null);
            request.startUpdateDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID, null, null, listener);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testUpdateDeviceRegistrationRequiresListener() {
        try {
            final PCFPushRegistrationApiRequestImpl request = new PCFPushRegistrationApiRequestImpl(getContext(), new FakeNetworkWrapper());
            request.startUpdateDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID, null, getParameters(), null);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testSuccessfulNewDeviceRegistrationRequest() {
        makeListenersForSuccessfulRequestFromNetwork(true, 200, HTTP_POST, null, null, null);
        final PCFPushRegistrationApiRequestImpl request = new PCFPushRegistrationApiRequestImpl(getContext(), networkWrapper);
        request.startNewDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, null, getParameters(), listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testSuccessfulNewDeviceRegistrationRequestSsl() {
        makeListenersForSuccessfulRequestFromNetworkSsl(true, 200, HTTP_POST, null, null, null);
        final PCFPushRegistrationApiRequestImpl request = new PCFPushRegistrationApiRequestImpl(getContext(), networkWrapper);
        request.startNewDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, null, getParameters(Pivotal.SslCertValidationMode.TRUST_ALL), listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testSuccessfulNewDeviceRegistrationRequestWithTags() {
        final Set<String> expectedSubscribeTags = makeSet("candy tag", "cookies tag");
        makeListenersForSuccessfulRequestFromNetwork(true, 200, HTTP_POST, expectedSubscribeTags, null, null);
        final PCFPushRegistrationApiRequestImpl request = new PCFPushRegistrationApiRequestImpl(getContext(), networkWrapper);
        request.startNewDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, null, getParameters(expectedSubscribeTags), listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testSuccessfulUpdateDeviceRegistrationRequest() {
        makeListenersForSuccessfulRequestFromNetwork(true, 200, HTTP_PUT, null, null, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID);
        final PCFPushRegistrationApiRequestImpl request = new PCFPushRegistrationApiRequestImpl(getContext(), networkWrapper);
        request.startUpdateDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID, null, getParameters(), listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testSuccessfulUpdateDeviceRegistrationRequestSubscribeToTags() {
        final Set<String> expectedSubscribeTags = makeSet("taco tag", "burrito tag");
        final Set<String> expectedUnsubscribeTags = makeSet();
        makeListenersForSuccessfulRequestFromNetwork(true, 200, HTTP_PUT, expectedSubscribeTags, expectedUnsubscribeTags, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID);
        final PCFPushRegistrationApiRequestImpl request = new PCFPushRegistrationApiRequestImpl(getContext(), networkWrapper);
        request.startUpdateDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID, null, getParameters(expectedSubscribeTags), listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testSuccessfulUpdateDeviceRegistrationRequestUnsubscribeFromTags() {
        final Set<String> expectedSubscribeTags = makeSet();
        final Set<String> expectedUnsubscribeTags = makeSet("donut tag", "cupcake tag");
        makeListenersForSuccessfulRequestFromNetwork(true, 200, HTTP_PUT, expectedSubscribeTags, expectedUnsubscribeTags, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID);
        final PCFPushRegistrationApiRequestImpl request = new PCFPushRegistrationApiRequestImpl(getContext(), networkWrapper);
        request.startUpdateDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID, expectedUnsubscribeTags, getParameters(), listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testSuccessfulUpdateDeviceRegistrationRequestSubscribeAndUnsubscribeFromTags() {
        makeListenersForSuccessfulRequestFromNetwork(true, 200, HTTP_PUT, makeSet("new1"), makeSet("remove1"), TEST_PCF_PUSH_DEVICE_REGISTRATION_ID);
        final PCFPushRegistrationApiRequestImpl request = new PCFPushRegistrationApiRequestImpl(getContext(), networkWrapper);
        request.startUpdateDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID, makeSet("remove1", "keep1"), getParameters(makeSet("KEEP1", "NEW1")), listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testNewDeviceRegistrationNullResponse() {
        makeListenersForSuccessfulNullResultFromNetwork(HTTP_POST, null, null, null);
        final PCFPushRegistrationApiRequestImpl request = new PCFPushRegistrationApiRequestImpl(getContext(), networkWrapper);
        request.startNewDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, null, getParameters(), listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testUpdateDeviceRegistrationNullResponse() {
        makeListenersForSuccessfulNullResultFromNetwork(HTTP_PUT, null, null, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID);
        final PCFPushRegistrationApiRequestImpl request = new PCFPushRegistrationApiRequestImpl(getContext(), networkWrapper);
        request.startUpdateDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID, null, getParameters(), listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testNewDeviceRegistrationSuccessful404() {
        makeListenersForSuccessfulRequestFromNetwork(false, 404, HTTP_POST, null, null, null);
        final PCFPushRegistrationApiRequestImpl request = new PCFPushRegistrationApiRequestImpl(getContext(), networkWrapper);
        request.startNewDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, null, getParameters(), listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testUpdateDeviceRegistrationSuccessful404() {
        makeListenersForSuccessfulRequestFromNetwork(false, 404, HTTP_PUT, null, null, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID);
        final PCFPushRegistrationApiRequestImpl request = new PCFPushRegistrationApiRequestImpl(getContext(), networkWrapper);
        request.startUpdateDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID, null, getParameters(), listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testNewDeviceRegistrationCouldNotConnect() {
        makeListenersFromFailedRequestFromNetwork("Your server is busted", 0, HTTP_POST, null, null, null);
        final PCFPushRegistrationApiRequestImpl request = new PCFPushRegistrationApiRequestImpl(getContext(), networkWrapper);
        request.startNewDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, null, getParameters(), listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testUpdateDeviceRegistrationCouldNotConnect() {
        makeListenersFromFailedRequestFromNetwork("Your server is busted", 0, HTTP_PUT, null, null, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID);
        final PCFPushRegistrationApiRequestImpl request = new PCFPushRegistrationApiRequestImpl(getContext(), networkWrapper);
        request.startUpdateDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID, null, getParameters(), listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testNewDeviceRegistrationBadNetworkResponse() {
        makeListenersWithBadNetworkResponse(HTTP_POST, null, null, null);
        final PCFPushRegistrationApiRequestImpl request = new PCFPushRegistrationApiRequestImpl(getContext(), networkWrapper);
        request.startNewDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, null, getParameters(), listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testUpdateDeviceRegistrationBadNetworkResponse() {
        makeListenersWithBadNetworkResponse(HTTP_PUT, null, null, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID);
        final PCFPushRegistrationApiRequestImpl request = new PCFPushRegistrationApiRequestImpl(getContext(), networkWrapper);
        request.startUpdateDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID, null, getParameters(), listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testNewDeviceRegistrationNoDeviceUuidInResponse() {
        makeListenersWithNoDeviceUuidInResponse(HTTP_POST, null, null, null);
        final PCFPushRegistrationApiRequestImpl request = new PCFPushRegistrationApiRequestImpl(getContext(), networkWrapper);
        request.startNewDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, null, getParameters(), listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testUpdateDeviceRegistrationNoDeviceUuidInResponse() {
        makeListenersWithNoDeviceUuidInResponse(HTTP_PUT, null, null, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID);
        final PCFPushRegistrationApiRequestImpl request = new PCFPushRegistrationApiRequestImpl(getContext(), networkWrapper);
        request.startUpdateDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID, null, getParameters(), listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testNewDeviceRegistrationWithCustomHeaders() {
        makeListenersForSuccessfulRequestWithCustomHeaders(true, 200, HTTP_POST, null, null, null, EXPECTED_REQUEST_HEADERS);
        final PCFPushRegistrationApiRequestImpl request = new PCFPushRegistrationApiRequestImpl(getContext(), networkWrapper);
        request.startNewDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, null, getParameters(EXPECTED_REQUEST_HEADERS), listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testUpdateDeviceRegistrationWithCustomHeaders() {
        makeListenersForSuccessfulRequestWithCustomHeaders(true, 200, HTTP_PUT, null, null, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID, EXPECTED_REQUEST_HEADERS);
        final PCFPushRegistrationApiRequestImpl request = new PCFPushRegistrationApiRequestImpl(getContext(), networkWrapper);
        request.startUpdateDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID, null, getParameters(EXPECTED_REQUEST_HEADERS), listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testAuthorization() {
        final String base64encodedAuthorization = ApiRequestImpl.getBasicAuthorizationValue(getParameters());
        assertEquals("Basic  " + TEST_BASE64_ENCODED_AUTHORIZATION, base64encodedAuthorization);
    }

    private void makeListenersForSuccessfulRequestFromNetwork(boolean isSuccessfulResult,
                                                              int expectedHttpStatusCode,
                                                              String expectedHttpMethod,
                                                              Set<String> expectedSubscribeTags,
                                                              Set<String> expectedUnsubscribeTags,
                                                              String previousPCFPushDeviceRegistrationId) {

        final String resultantJson = "{\"device_uuid\" : \"" + TEST_PCF_PUSH_DEVICE_REGISTRATION_ID + "\"}";
        FakeHttpURLConnection.setResponseData(resultantJson);
        FakeHttpURLConnection.setResponseCode(expectedHttpStatusCode);
        makePCFPushRegistrationApiRequestListener(isSuccessfulResult, expectedHttpMethod, expectedSubscribeTags, expectedUnsubscribeTags, previousPCFPushDeviceRegistrationId, false, null);
    }

    private void makeListenersForSuccessfulRequestFromNetworkSsl(boolean isSuccessfulResult,
                                                                 int expectedHttpStatusCode,
                                                                 String expectedHttpMethod,
                                                                 Set<String> expectedSubscribeTags,
                                                                 Set<String> expectedUnsubscribeTags,
                                                                 String previousPCFPushDeviceRegistrationId) {

        final String resultantJson = "{\"device_uuid\" : \"" + TEST_PCF_PUSH_DEVICE_REGISTRATION_ID + "\"}";
        FakeHttpURLConnection.setResponseData(resultantJson);
        FakeHttpURLConnection.setResponseCode(expectedHttpStatusCode);
        makePCFPushRegistrationApiRequestListener(isSuccessfulResult, expectedHttpMethod, expectedSubscribeTags, expectedUnsubscribeTags, previousPCFPushDeviceRegistrationId, true, null);
    }

    private void makeListenersForSuccessfulRequestWithCustomHeaders(boolean isSuccessfulResult,
                                                                    int expectedHttpStatusCode,
                                                                    String expectedHttpMethod,
                                                                    Set<String> expectedSubscribeTags,
                                                                    Set<String> expectedUnsubscribeTags,
                                                                    String previousPCFPushDeviceRegistrationId,
                                                                    Map<String, String> expectedRequestHeaders) {

        final String resultantJson = "{\"device_uuid\" : \"" + TEST_PCF_PUSH_DEVICE_REGISTRATION_ID + "\"}";
        FakeHttpURLConnection.setResponseData(resultantJson);
        FakeHttpURLConnection.setResponseCode(expectedHttpStatusCode);
        makePCFPushRegistrationApiRequestListener(isSuccessfulResult, expectedHttpMethod, expectedSubscribeTags, expectedUnsubscribeTags, previousPCFPushDeviceRegistrationId, false, expectedRequestHeaders);
    }

    private void makeListenersForSuccessfulNullResultFromNetwork(String expectedHttpMethod,
                                                                 Set<String> expectedSubscribeTags,
                                                                 Set<String> expectedUnsubscribeTags,
                                                                 String previousPCFPushDeviceRegistrationId) {
        FakeHttpURLConnection.setResponseData(null);
        FakeHttpURLConnection.setResponseCode(200);
        makePCFPushRegistrationApiRequestListener(false, expectedHttpMethod, expectedSubscribeTags, expectedUnsubscribeTags, previousPCFPushDeviceRegistrationId, false, null);
    }

    private void makeListenersWithBadNetworkResponse(String expectedHttpMethod,
                                                     Set<String> expectedSubscribeTags,
                                                     Set<String> expectedUnsubscribeTags,
                                                     String previousPCFPushDeviceRegistrationId) {

        FakeHttpURLConnection.setResponseData("{{{{{{{");
        FakeHttpURLConnection.setResponseCode(200);
        makePCFPushRegistrationApiRequestListener(false, expectedHttpMethod, expectedSubscribeTags, expectedUnsubscribeTags, previousPCFPushDeviceRegistrationId, false, null);
    }

    private void makeListenersWithNoDeviceUuidInResponse(String expectedHttpMethod,
                                                         Set<String> expectedSubscribeTags,
                                                         Set<String> expectedUnsubscribeTags,
                                                         String previousPCFPushDeviceRegistrationId) {

        FakeHttpURLConnection.setResponseData("{}");
        FakeHttpURLConnection.setResponseCode(200);
        makePCFPushRegistrationApiRequestListener(false, expectedHttpMethod, expectedSubscribeTags, expectedUnsubscribeTags, previousPCFPushDeviceRegistrationId, false, null);
    }

    private void makeListenersFromFailedRequestFromNetwork(String exceptionText,
                                                           int expectedHttpStatusCode,
                                                           String expectedHttpMethod,
                                                           Set<String> expectedSubscribeTags,
                                                           Set<String> expectedUnsubscribeTags,
                                                           String previousPCFPushDeviceRegistrationId) {

        IOException exception = null;
        if (exceptionText != null) {
            exception = new IOException(exceptionText);
        }
        FakeHttpURLConnection.setConnectionException(exception);
        FakeHttpURLConnection.willThrowConnectionException(true);
        FakeHttpURLConnection.setResponseCode(expectedHttpStatusCode);
        makePCFPushRegistrationApiRequestListener(false, expectedHttpMethod, expectedSubscribeTags, expectedUnsubscribeTags, previousPCFPushDeviceRegistrationId, false, null);
    }

    public void makePCFPushRegistrationApiRequestListener(final boolean isSuccessfulRequest,
                                                          final String expectedHttpMethod,
                                                          final Set<String> expectedSubscribeTags,
                                                          final Set<String> expectedUnsubscribeTags,
                                                          final String previousPCFPushDeviceRegistrationId,
                                                          final boolean isTrustAllSslCertificates,
                                                          final Map<String, String> expectedRequestHeaders) {

        listener = new PCFPushRegistrationListener() {

            @Override
            public void onPCFPushRegistrationSuccess(String pcfPushDeviceRegistrationId) {
                assertTrue(isSuccessfulRequest);
                assertEquals(expectedHttpMethod, FakeHttpURLConnection.getReceivedHttpMethod());
                assertTrue(FakeHttpURLConnection.getRequestPropertiesMap().containsKey("Authorization"));
                if (previousPCFPushDeviceRegistrationId != null) {
                    assertTrue(FakeHttpURLConnection.getReceivedURL().toString().endsWith(previousPCFPushDeviceRegistrationId));
                }
                assertEquals(isTrustAllSslCertificates, FakeHttpURLConnection.didCallSetSSLSocketFactory());

                if (expectedRequestHeaders != null) {
                    final Map<String, String> actualRequestHeaders = FakeHttpURLConnection.getRequestPropertiesMap();
                    for (Map.Entry<String, String> entry : expectedRequestHeaders.entrySet()) {
                        assertTrue(actualRequestHeaders.containsKey(entry.getKey()));
                        assertEquals(entry.getValue(), actualRequestHeaders.get(entry.getKey()));
                    }
                }

                final byte[] requestData = FakeHttpURLConnection.getRequestData();
                assertNotNull(requestData);
                if (FakeHttpURLConnection.getReceivedHttpMethod().equals("PUT")) {
                    final Gson gson = new Gson();
                    final PCFPushApiRegistrationPutRequestData model = gson.fromJson(new String(requestData), PCFPushApiRegistrationPutRequestData.class);
                    assertNotNull(model);

                    if (expectedSubscribeTags == null) {
                        assertTrue(model.getTags().getSubscribeTags().isEmpty());
                    } else {
                        assertEquals(expectedSubscribeTags, model.getTags().getSubscribeTags());
                    }

                    if (expectedUnsubscribeTags == null) {
                        assertTrue(model.getTags().getUnsubscribedTags().isEmpty());
                    } else {
                        assertEquals(expectedUnsubscribeTags, model.getTags().getUnsubscribedTags());
                    }

                } else if (FakeHttpURLConnection.getReceivedHttpMethod().equals("POST")) {

                    final Gson gson = new Gson();
                    final PCFPushApiRegistrationPostRequestData model = gson.fromJson(new String(requestData), PCFPushApiRegistrationPostRequestData.class);
                    assertNotNull(model);

                    if (expectedSubscribeTags == null) {
                        assertTrue(model.getTags().isEmpty());
                    } else {
                        assertEquals(expectedSubscribeTags, model.getTags());
                    }

                } else {
                    fail("Unexpected HTTP method: " + FakeHttpURLConnection.getReceivedHttpMethod());
                }

                assertEquals(TEST_PCF_PUSH_DEVICE_REGISTRATION_ID, pcfPushDeviceRegistrationId);
                delayedLoop.flagSuccess();
            }

            @Override
            public void onPCFPushRegistrationFailed(String reason) {
                assertFalse(isSuccessfulRequest);
                assertEquals(expectedHttpMethod, FakeHttpURLConnection.getReceivedHttpMethod());
                if (previousPCFPushDeviceRegistrationId != null) {
                    assertTrue(FakeHttpURLConnection.getReceivedURL().toString().endsWith(previousPCFPushDeviceRegistrationId));
                }
                delayedLoop.flagSuccess();
            }
        };
    }

    private PushParameters getParameters() {
        return new PushParameters(TEST_SENDER_ID, TEST_PLATFORM_UUID, TEST_PLATFORM_SECRET, TEST_SERVICE_URL, TEST_DEVICE_ALIAS, null, true, Pivotal.SslCertValidationMode.DEFAULT, null, null);
    }

    private PushParameters getParameters(Set<String> tags) {
        return new PushParameters(TEST_SENDER_ID, TEST_PLATFORM_UUID, TEST_PLATFORM_SECRET, TEST_SERVICE_URL, TEST_DEVICE_ALIAS, tags, true, Pivotal.SslCertValidationMode.DEFAULT, null, null);
    }

    private PushParameters getParameters(Pivotal.SslCertValidationMode sslCertValidationMode) {
        return new PushParameters(TEST_SENDER_ID, TEST_PLATFORM_UUID, TEST_PLATFORM_SECRET, TEST_SERVICE_URL, TEST_DEVICE_ALIAS, null, true, sslCertValidationMode, null, null);
    }

    private PushParameters getParameters(Map<String, String> requestHeaders) {
        return new PushParameters(TEST_SENDER_ID, TEST_PLATFORM_UUID, TEST_PLATFORM_SECRET, TEST_SERVICE_URL, TEST_DEVICE_ALIAS, null, true, Pivotal.SslCertValidationMode.DEFAULT, null, requestHeaders);
    }

    private Set<String> makeSet(String... strings) {
        return new HashSet<>(Arrays.asList(strings));
    }
}
