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
    private static final String TEST_DEVICE_UUID = "TEST_DEVICE_UUID";
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
            makePCFPushGeofenceUpdateApiRequestListener(true, 0, TEST_DEVICE_UUID);
            request.getGeofenceUpdates(-1, TEST_DEVICE_UUID, getParameters(), listener);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testNewDeviceRegistrationRequiresNonNullDeviceUuid() {
        try {
            final PCFPushGetGeofenceUpdatesApiRequest request = new PCFPushGetGeofenceUpdatesApiRequest(new FakeNetworkWrapper());
            makePCFPushGeofenceUpdateApiRequestListener(true, 0, TEST_DEVICE_UUID);
            request.getGeofenceUpdates(0, null, getParameters(), listener);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testNewDeviceRegistrationRequiresNonEmptyDeviceUuid() {
        try {
            final PCFPushGetGeofenceUpdatesApiRequest request = new PCFPushGetGeofenceUpdatesApiRequest(new FakeNetworkWrapper());
            makePCFPushGeofenceUpdateApiRequestListener(true, 0, TEST_DEVICE_UUID);
            request.getGeofenceUpdates(0, "", getParameters(), listener);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testNewDeviceRegistrationRequiresParameters() {
        try {
            final PCFPushGetGeofenceUpdatesApiRequest request = new PCFPushGetGeofenceUpdatesApiRequest(new FakeNetworkWrapper());
            makePCFPushGeofenceUpdateApiRequestListener(true, 0, TEST_DEVICE_UUID);
            request.getGeofenceUpdates(0, TEST_DEVICE_UUID, null, listener);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testNewDeviceRegistrationRequiresListener() {
        try {
            final PCFPushGetGeofenceUpdatesApiRequest request = new PCFPushGetGeofenceUpdatesApiRequest(new FakeNetworkWrapper());
            makePCFPushGeofenceUpdateApiRequestListener(true, 0, TEST_DEVICE_UUID);
            request.getGeofenceUpdates(0, TEST_DEVICE_UUID, getParameters(), null);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testSuccessfulGeofenceUpdateRequest() throws IOException {
        makeListenersForSuccessfulRequestFromNetwork(true, 200, "geofence_response_data_one_item.json", 99, TEST_DEVICE_UUID);
        final PCFPushGetGeofenceUpdatesApiRequest request = new PCFPushGetGeofenceUpdatesApiRequest(networkWrapper);
        request.getGeofenceUpdates(99, TEST_DEVICE_UUID, getParameters(), listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testNewDeviceRegistrationNullResponse() {
        makeListenersForSuccessfulNullResultFromNetwork(0, TEST_DEVICE_UUID);
        final PCFPushGetGeofenceUpdatesApiRequest request = new PCFPushGetGeofenceUpdatesApiRequest(networkWrapper);
        request.getGeofenceUpdates(0, TEST_DEVICE_UUID, getParameters(), listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testNewDeviceRegistrationSuccessful404() throws IOException {
        makeListenersForSuccessfulRequestFromNetwork(false, 404, "geofence_response_data_one_item.json", 0, TEST_DEVICE_UUID);
        final PCFPushGetGeofenceUpdatesApiRequest request = new PCFPushGetGeofenceUpdatesApiRequest(networkWrapper);
        request.getGeofenceUpdates(0, TEST_DEVICE_UUID, getParameters(), listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }
    public void testNewDeviceRegistrationCouldNotConnect() {
        makeListenersFromFailedRequestFromNetwork("Your server is busted", 0, 0, TEST_DEVICE_UUID);
        final PCFPushGetGeofenceUpdatesApiRequest request = new PCFPushGetGeofenceUpdatesApiRequest(networkWrapper);
        request.getGeofenceUpdates(0, TEST_DEVICE_UUID, getParameters(), listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testNewDeviceRegistrationBadNetworkResponse() {
        makeListenersWithBadNetworkResponse(0, TEST_DEVICE_UUID);
        final PCFPushGetGeofenceUpdatesApiRequest request = new PCFPushGetGeofenceUpdatesApiRequest(networkWrapper);
        request.getGeofenceUpdates(0, TEST_DEVICE_UUID, getParameters(), listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    private void makeListenersForSuccessfulRequestFromNetwork(boolean isSuccessfulResult,
                                                              int expectedHttpStatusCode,
                                                              String responseDataFilename,
                                                              long expectedTimestamp, String expectedDeviceUuid) throws IOException {


        final String responseDataString;
        if (responseDataFilename != null) {
            final PCFPushGeofenceResponseData responseData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), responseDataFilename);
            responseDataString = GsonUtil.getGson().toJson(responseData, PCFPushGeofenceResponseData.class);
        } else {
            responseDataString = "";
        }
        FakeHttpURLConnection.setResponseData(responseDataString);
        FakeHttpURLConnection.setResponseCode(expectedHttpStatusCode);
        makePCFPushGeofenceUpdateApiRequestListener(isSuccessfulResult, expectedTimestamp, expectedDeviceUuid);
    }

    private void makeListenersForSuccessfulNullResultFromNetwork(long expectedTimestamp, String expectedDeviceUuid) {
        FakeHttpURLConnection.setResponseData(null);
        FakeHttpURLConnection.setResponseCode(200);
        makePCFPushGeofenceUpdateApiRequestListener(false, expectedTimestamp, expectedDeviceUuid);
    }

    private void makeListenersWithBadNetworkResponse(long expectedTimestamp, String expectedDeviceUuid) {
        FakeHttpURLConnection.setResponseData("{{{{{{{");
        FakeHttpURLConnection.setResponseCode(200);
        makePCFPushGeofenceUpdateApiRequestListener(false, expectedTimestamp, expectedDeviceUuid);
    }

    private void makeListenersFromFailedRequestFromNetwork(String exceptionText,
                                                           int expectedHttpStatusCode,
                                                           long expectedTimestamp,
                                                           String expectedDeviceUuid) {

        IOException exception = null;
        if (exceptionText != null) {
            exception = new IOException(exceptionText);
        }
        FakeHttpURLConnection.setConnectionException(exception);
        FakeHttpURLConnection.willThrowConnectionException(true);
        FakeHttpURLConnection.setResponseCode(expectedHttpStatusCode);
        makePCFPushGeofenceUpdateApiRequestListener(false, expectedTimestamp, expectedDeviceUuid);
    }

    public void makePCFPushGeofenceUpdateApiRequestListener(final boolean isSuccessfulRequest,
                                                            final long expectedTimestamp,
                                                            final String expectedDeviceUuid) {

        listener = new PCFPushGetGeofenceUpdatesListener() {
            @Override
            public void onPCFPushGetGeofenceUpdatesSuccess(PCFPushGeofenceResponseData responseData) {
                assertTrue(isSuccessfulRequest);
                assertEquals("GET", FakeHttpURLConnection.getReceivedHttpMethod());
                assertTrue(FakeHttpURLConnection.getReceivedURL().toString().contains("timestamp=" + expectedTimestamp));
                assertTrue(FakeHttpURLConnection.getReceivedURL().toString().contains("device_uuid=" + expectedDeviceUuid));
                assertTrue(FakeHttpURLConnection.getReceivedURL().toString().contains("platform=android"));
                delayedLoop.flagSuccess();
            }

            @Override
            public void onPCFPushGetGeofenceUpdatesFailed(String reason) {
                assertFalse(isSuccessfulRequest);
                assertEquals("GET", FakeHttpURLConnection.getReceivedHttpMethod());
                assertTrue(FakeHttpURLConnection.getReceivedURL().toString().contains("timestamp=" + expectedTimestamp));
                assertTrue(FakeHttpURLConnection.getReceivedURL().toString().contains("device_uuid=" + expectedDeviceUuid));
                assertTrue(FakeHttpURLConnection.getReceivedURL().toString().contains("platform=android"));
                delayedLoop.flagSuccess();
            }
        };
    }

    private PushParameters getParameters() {
        return new PushParameters(null, TEST_PLATFORM_UUID, TEST_PLATFORM_SECRET, TEST_SERVICE_URL, null, null, true);
    }
}
