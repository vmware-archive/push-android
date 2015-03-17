/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.backend.geofence;

import android.test.AndroidTestCase;

import java.io.IOException;

import io.pivotal.android.push.PushParameters;
import io.pivotal.android.push.model.geofence.PCFPushGeofenceResponseData;
import io.pivotal.android.push.util.DelayedLoop;
import io.pivotal.android.push.util.FakeHttpURLConnection;
import io.pivotal.android.push.util.FakeNetworkWrapper;
import io.pivotal.android.push.util.GsonUtil;
import io.pivotal.android.push.util.ModelUtil;

public class PCFPushGetGeofenceUpdatesApiRequestTest extends AndroidTestCase {

    private static final String TEST_PLATFORM_UUID = "TEST_PLATFORM_UUID";
    private static final String TEST_PLATFORM_SECRET = "TEST_PLATFORM_SECRET";
    private static final String TEST_SERVICE_URL = "http://test.com";
    private static final long TEN_SECOND_TIMEOUT = 10000L;

    private FakeNetworkWrapper networkWrapper;
    private DelayedLoop delayedLoop;
    private PCFPushGetGeofenceUpdatesListener listener;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        networkWrapper = new FakeNetworkWrapper();
        delayedLoop = new DelayedLoop(TEN_SECOND_TIMEOUT);
        FakeHttpURLConnection.reset();
    }

    public void testRequiresNetworkWrapper() {
        try {
            new PCFPushGetGeofenceUpdatesApiRequest(null);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testNewDeviceRegistrationRequiresNonNegativeTimestamp() {
        try {
            final PCFPushGetGeofenceUpdatesApiRequest request = new PCFPushGetGeofenceUpdatesApiRequest(new FakeNetworkWrapper());
            makePCFPushGeofenceUpdateApiRequestListener(true, 0);
            request.getGeofenceUpdates(-1, getParameters(), listener);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testNewDeviceRegistrationRequiresParameters() {
        try {
            final PCFPushGetGeofenceUpdatesApiRequest request = new PCFPushGetGeofenceUpdatesApiRequest(new FakeNetworkWrapper());
            makePCFPushGeofenceUpdateApiRequestListener(true, 0);
            request.getGeofenceUpdates(0, null, listener);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testNewDeviceRegistrationRequiresListener() {
        try {
            final PCFPushGetGeofenceUpdatesApiRequest request = new PCFPushGetGeofenceUpdatesApiRequest(new FakeNetworkWrapper());
            makePCFPushGeofenceUpdateApiRequestListener(true, 0);
            request.getGeofenceUpdates(0, getParameters(), null);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testSuccessfulGeofenceUpdateRequest() throws IOException {
        makeListenersForSuccessfulRequestFromNetwork(true, 200, "geofence_response_data_one_item.json", 99);
        final PCFPushGetGeofenceUpdatesApiRequest request = new PCFPushGetGeofenceUpdatesApiRequest(networkWrapper);
        request.getGeofenceUpdates(99, getParameters(), listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testNewDeviceRegistrationNullResponse() {
        makeListenersForSuccessfulNullResultFromNetwork(0);
        final PCFPushGetGeofenceUpdatesApiRequest request = new PCFPushGetGeofenceUpdatesApiRequest(networkWrapper);
        request.getGeofenceUpdates(0, getParameters(), listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testNewDeviceRegistrationSuccessful404() throws IOException {
        makeListenersForSuccessfulRequestFromNetwork(false, 404, "geofence_response_data_one_item.json", 0);
        final PCFPushGetGeofenceUpdatesApiRequest request = new PCFPushGetGeofenceUpdatesApiRequest(networkWrapper);
        request.getGeofenceUpdates(0, getParameters(), listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }
    public void testNewDeviceRegistrationCouldNotConnect() {
        makeListenersFromFailedRequestFromNetwork("Your server is busted", 0, 0);
        final PCFPushGetGeofenceUpdatesApiRequest request = new PCFPushGetGeofenceUpdatesApiRequest(networkWrapper);
        request.getGeofenceUpdates(0, getParameters(), listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testNewDeviceRegistrationBadNetworkResponse() {
        makeListenersWithBadNetworkResponse(0);
        final PCFPushGetGeofenceUpdatesApiRequest request = new PCFPushGetGeofenceUpdatesApiRequest(networkWrapper);
        request.getGeofenceUpdates(0, getParameters(), listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    private void makeListenersForSuccessfulRequestFromNetwork(boolean isSuccessfulResult,
                                                              int expectedHttpStatusCode,
                                                              String responseDataFilename,
                                                              long expectedTimestamp) throws IOException {


        final String responseDataString;
        if (responseDataFilename != null) {
            final PCFPushGeofenceResponseData responseData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), responseDataFilename);
            responseDataString = GsonUtil.getGson().toJson(responseData, PCFPushGeofenceResponseData.class);
        } else {
            responseDataString = "";
        }
        FakeHttpURLConnection.setResponseData(responseDataString);
        FakeHttpURLConnection.setResponseCode(expectedHttpStatusCode);
        makePCFPushGeofenceUpdateApiRequestListener(isSuccessfulResult, expectedTimestamp);
    }

    private void makeListenersForSuccessfulNullResultFromNetwork(long expectedTimestamp) {
        FakeHttpURLConnection.setResponseData(null);
        FakeHttpURLConnection.setResponseCode(200);
        makePCFPushGeofenceUpdateApiRequestListener(false, expectedTimestamp);
    }

    private void makeListenersWithBadNetworkResponse(long expectedTimestamp) {
        FakeHttpURLConnection.setResponseData("{{{{{{{");
        FakeHttpURLConnection.setResponseCode(200);
        makePCFPushGeofenceUpdateApiRequestListener(false, expectedTimestamp);
    }

    private void makeListenersFromFailedRequestFromNetwork(String exceptionText,
                                                           int expectedHttpStatusCode, long expectedTimestamp) {

        IOException exception = null;
        if (exceptionText != null) {
            exception = new IOException(exceptionText);
        }
        FakeHttpURLConnection.setConnectionException(exception);
        FakeHttpURLConnection.willThrowConnectionException(true);
        FakeHttpURLConnection.setResponseCode(expectedHttpStatusCode);
        makePCFPushGeofenceUpdateApiRequestListener(false, expectedTimestamp);
    }

    public void makePCFPushGeofenceUpdateApiRequestListener(final boolean isSuccessfulRequest, final long expectedTimestamp) {

        listener = new PCFPushGetGeofenceUpdatesListener() {
            @Override
            public void onPCFPushGetGeofenceUpdatesSuccess(PCFPushGeofenceResponseData responseData) {
                assertTrue(isSuccessfulRequest);
                assertEquals("GET", FakeHttpURLConnection.getReceivedHttpMethod());
                assertTrue(FakeHttpURLConnection.getReceivedURL().toString().contains("timestamp=" + expectedTimestamp));
                delayedLoop.flagSuccess();
            }

            @Override
            public void onPCFPushGetGeofenceUpdatesFailed(String reason) {
                assertFalse(isSuccessfulRequest);
                assertEquals("GET", FakeHttpURLConnection.getReceivedHttpMethod());
                assertTrue(FakeHttpURLConnection.getReceivedURL().toString().contains("timestamp=" + expectedTimestamp));
                delayedLoop.flagSuccess();
            }
        };
    }

    private PushParameters getParameters() {
        return new PushParameters(null, TEST_PLATFORM_UUID, TEST_PLATFORM_SECRET, TEST_SERVICE_URL, null, null, true);
    }
}
