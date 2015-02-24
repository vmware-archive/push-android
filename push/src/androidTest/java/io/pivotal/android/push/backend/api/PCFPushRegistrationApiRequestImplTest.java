/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.backend.api;

import android.test.AndroidTestCase;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import io.pivotal.android.push.RegistrationParameters;
import io.pivotal.android.push.model.api.PCFPushApiRegistrationPostRequestData;
import io.pivotal.android.push.model.api.PCFPushApiRegistrationPutRequestData;
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

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        networkWrapper = new FakeNetworkWrapper();
        delayedLoop = new DelayedLoop(TEN_SECOND_TIMEOUT);
        FakeHttpURLConnection.reset();
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
            makePCFPushRegistrationApiRequestListener(true, HTTP_POST, null, null, null);
            request.startNewDeviceRegistration(null, null, getParameters(), listener);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testNewDeviceRegistrationRequiresParameters() {
        try {
            final PCFPushRegistrationApiRequestImpl request = new PCFPushRegistrationApiRequestImpl(getContext(), new FakeNetworkWrapper());
            makePCFPushRegistrationApiRequestListener(true, HTTP_POST, null, null, null);
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
            makePCFPushRegistrationApiRequestListener(true, HTTP_POST, null, null, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID);
            request.startUpdateDeviceRegistration(null, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID, null, getParameters(), listener);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testUpdateDeviceRegistrationRequiresPCFPushDeviceRegistrationId() {
        try {
            final PCFPushRegistrationApiRequestImpl request = new PCFPushRegistrationApiRequestImpl(getContext(), new FakeNetworkWrapper());
            makePCFPushRegistrationApiRequestListener(true, HTTP_PUT, null, null, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID);
            request.startUpdateDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, null, null, getParameters(), listener);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testUpdateDeviceRegistrationRequiresParameters() {
        try {
            final PCFPushRegistrationApiRequestImpl request = new PCFPushRegistrationApiRequestImpl(getContext(), new FakeNetworkWrapper());
            makePCFPushRegistrationApiRequestListener(true, HTTP_PUT, null, null, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID);
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

    public void testSuccessfulNewDeviceRegistrationRequestWithTags() {
        final Set<String> expectedSubscribeTags = makeSet("CANDY TAG", "COOKIES TAG");
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
        final Set<String> expectedSubscribeTags = makeSet("TACO TAG", "BURRITO TAG");
        final Set<String> expectedUnsubscribeTags = makeSet();
        makeListenersForSuccessfulRequestFromNetwork(true, 200, HTTP_PUT, expectedSubscribeTags, expectedUnsubscribeTags, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID);
        final PCFPushRegistrationApiRequestImpl request = new PCFPushRegistrationApiRequestImpl(getContext(), networkWrapper);
        request.startUpdateDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID, null, getParameters(expectedSubscribeTags), listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testSuccessfulUpdateDeviceRegistrationRequestUnsubscribeFromTags() {
        final Set<String> expectedSubscribeTags = makeSet();
        final Set<String> expectedUnsubscribeTags = makeSet("DONUT TAG", "CUPCAKE TAG");
        makeListenersForSuccessfulRequestFromNetwork(true, 200, HTTP_PUT, expectedSubscribeTags, expectedUnsubscribeTags, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID);
        final PCFPushRegistrationApiRequestImpl request = new PCFPushRegistrationApiRequestImpl(getContext(), networkWrapper);
        request.startUpdateDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID, expectedUnsubscribeTags, getParameters(), listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testSuccessfulUpdateDeviceRegistrationRequestSubscribeAndUnsubscribeFromTags() {
        makeListenersForSuccessfulRequestFromNetwork(true, 200, HTTP_PUT, makeSet("NEW1"), makeSet("REMOVE1"), TEST_PCF_PUSH_DEVICE_REGISTRATION_ID);
        final PCFPushRegistrationApiRequestImpl request = new PCFPushRegistrationApiRequestImpl(getContext(), networkWrapper);
        request.startUpdateDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, TEST_PCF_PUSH_DEVICE_REGISTRATION_ID, makeSet("REMOVE1", "KEEP1"), getParameters(makeSet("KEEP1", "NEW1")), listener);
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
        makePCFPushRegistrationApiRequestListener(isSuccessfulResult, expectedHttpMethod, expectedSubscribeTags, expectedUnsubscribeTags, previousPCFPushDeviceRegistrationId);
    }

    private void makeListenersForSuccessfulNullResultFromNetwork(String expectedHttpMethod,
                                                                 Set<String> expectedSubscribeTags,
                                                                 Set<String> expectedUnsubscribeTags,
                                                                 String previousPCFPushDeviceRegistrationId) {
        FakeHttpURLConnection.setResponseData(null);
        FakeHttpURLConnection.setResponseCode(200);
        makePCFPushRegistrationApiRequestListener(false, expectedHttpMethod, expectedSubscribeTags, expectedUnsubscribeTags, previousPCFPushDeviceRegistrationId);
    }

    private void makeListenersWithBadNetworkResponse(String expectedHttpMethod,
                                                     Set<String> expectedSubscribeTags,
                                                     Set<String> expectedUnsubscribeTags,
                                                     String previousPCFPushDeviceRegistrationId) {

        FakeHttpURLConnection.setResponseData("{{{{{{{");
        FakeHttpURLConnection.setResponseCode(200);
        makePCFPushRegistrationApiRequestListener(false, expectedHttpMethod, expectedSubscribeTags, expectedUnsubscribeTags, previousPCFPushDeviceRegistrationId);
    }

    private void makeListenersWithNoDeviceUuidInResponse(String expectedHttpMethod,
                                                         Set<String> expectedSubscribeTags,
                                                         Set<String> expectedUnsubscribeTags,
                                                         String previousPCFPushDeviceRegistrationId) {

        FakeHttpURLConnection.setResponseData("{}");
        FakeHttpURLConnection.setResponseCode(200);
        makePCFPushRegistrationApiRequestListener(false, expectedHttpMethod, expectedSubscribeTags, expectedUnsubscribeTags, previousPCFPushDeviceRegistrationId);
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
        makePCFPushRegistrationApiRequestListener(false, expectedHttpMethod, expectedSubscribeTags, expectedUnsubscribeTags, previousPCFPushDeviceRegistrationId);
    }

    public void makePCFPushRegistrationApiRequestListener(final boolean isSuccessfulRequest,
                                                          final String expectedHttpMethod,
                                                          final Set<String> expectedSubscribeTags,
                                                          final Set<String> expectedUnsubscribeTags,
                                                          final String previousPCFPushDeviceRegistrationId) {

        listener = new PCFPushRegistrationListener() {

            @Override
            public void onPCFPushRegistrationSuccess(String pcfPushDeviceRegistrationId) {
                assertTrue(isSuccessfulRequest);
                assertEquals(expectedHttpMethod, FakeHttpURLConnection.getReceivedHttpMethod());
                assertTrue(FakeHttpURLConnection.getRequestPropertiesMap().containsKey("Authorization"));
                if (previousPCFPushDeviceRegistrationId != null) {
                    assertTrue(FakeHttpURLConnection.getReceivedURL().toString().endsWith(previousPCFPushDeviceRegistrationId));
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

    private RegistrationParameters getParameters() {
        return new RegistrationParameters(TEST_SENDER_ID, TEST_PLATFORM_UUID, TEST_PLATFORM_SECRET, TEST_SERVICE_URL, TEST_DEVICE_ALIAS, null);
    }

    private RegistrationParameters getParameters(Set<String> tags) {
        return new RegistrationParameters(TEST_SENDER_ID, TEST_PLATFORM_UUID, TEST_PLATFORM_SECRET, TEST_SERVICE_URL, TEST_DEVICE_ALIAS, tags);
    }

    private Set<String> makeSet(String... strings) {
        return new HashSet<String>(Arrays.asList(strings));
    }
}
