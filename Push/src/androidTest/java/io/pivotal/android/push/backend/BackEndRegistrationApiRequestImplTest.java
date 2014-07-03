/* Copyright (c) 2013 Pivotal Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.pivotal.android.push.backend;

import android.test.AndroidTestCase;

import java.io.IOException;

import io.pivotal.android.common.test.network.FakeHttpURLConnection;
import io.pivotal.android.common.test.network.FakeNetworkWrapper;
import io.pivotal.android.common.test.util.DelayedLoop;
import io.pivotal.android.push.RegistrationParameters;

public class BackEndRegistrationApiRequestImplTest extends AndroidTestCase {

    private static final String TEST_BACK_END_DEVICE_REGISTRATION_ID = "TEST_BACK_END_DEVICE_REGISTRATION_ID";
    private static final String TEST_GCM_DEVICE_REGISTRATION_ID = "TEST_GCM_DEVICE_REGISTRATION_ID";
    private static final String TEST_SENDER_ID = "TEST_SENDER_ID";
    private static final String TEST_VARIANT_UUID = "TEST_VARIANT_UUID";
    private static final String TEST_VARIANT_SECRET = "TEST_VARIANT_SECRET";
    private static final String TEST_DEVICE_ALIAS = "TEST_DEVICE_ALIAS";
    private static final String TEST_BASE_SERVER_URL = "http://test.com";
    private static final String TEST_BASE64_ENCODED_AUTHORIZATION = "VEVTVF9WQVJJQU5UX1VVSUQ6VEVTVF9WQVJJQU5UX1NFQ1JFVA==";
    private static final long TEN_SECOND_TIMEOUT = 10000L;
    private static final String HTTP_POST = "POST";
    private static final String HTTP_PUT = "PUT";

    private FakeNetworkWrapper networkWrapper;
    private DelayedLoop delayedLoop;
    private BackEndRegistrationListener listener;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        networkWrapper = new FakeNetworkWrapper();
        delayedLoop = new DelayedLoop(TEN_SECOND_TIMEOUT);
        FakeHttpURLConnection.reset();
    }

    public void testRequiresContext() {
        try {
            new BackEndRegistrationApiRequestImpl(null, networkWrapper);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testRequiresNetworkWrapper() {
        try {
            new BackEndRegistrationApiRequestImpl(getContext(), null);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testNewDeviceRegistrationRequiresGcmDeviceRegistrationId() {
        try {
            final BackEndRegistrationApiRequestImpl request = new BackEndRegistrationApiRequestImpl(getContext(), new FakeNetworkWrapper());
            makeBackEndRegistrationApiRequestListener(true, HTTP_POST, null);
            request.startNewDeviceRegistration(null, getParameters(), listener);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testNewDeviceRegistrationRequiresParameters() {
        try {
            final BackEndRegistrationApiRequestImpl request = new BackEndRegistrationApiRequestImpl(getContext(), new FakeNetworkWrapper());
            makeBackEndRegistrationApiRequestListener(true, HTTP_POST, null);
            request.startNewDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, null, listener);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testNewDeviceRegistrationRequiresListener() {
        try {
            final BackEndRegistrationApiRequestImpl request = new BackEndRegistrationApiRequestImpl(getContext(), new FakeNetworkWrapper());
            request.startNewDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, getParameters(), null);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testUpdateDeviceRegistrationRequiresGcmDeviceRegistrationId() {
        try {
            final BackEndRegistrationApiRequestImpl request = new BackEndRegistrationApiRequestImpl(getContext(), new FakeNetworkWrapper());
            makeBackEndRegistrationApiRequestListener(true, HTTP_POST, TEST_BACK_END_DEVICE_REGISTRATION_ID);
            request.startUpdateDeviceRegistration(null, TEST_BACK_END_DEVICE_REGISTRATION_ID, getParameters(), listener);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testUpdateDeviceRegistrationRequiresBackEndDeviceRegistrationId() {
        try {
            final BackEndRegistrationApiRequestImpl request = new BackEndRegistrationApiRequestImpl(getContext(), new FakeNetworkWrapper());
            makeBackEndRegistrationApiRequestListener(true, HTTP_PUT, TEST_BACK_END_DEVICE_REGISTRATION_ID);
            request.startUpdateDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, null, getParameters(), listener);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testUpdateDeviceRegistrationRequiresParameters() {
        try {
            final BackEndRegistrationApiRequestImpl request = new BackEndRegistrationApiRequestImpl(getContext(), new FakeNetworkWrapper());
            makeBackEndRegistrationApiRequestListener(true, HTTP_PUT, TEST_BACK_END_DEVICE_REGISTRATION_ID);
            request.startUpdateDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, TEST_BACK_END_DEVICE_REGISTRATION_ID, null, listener);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testUpdateDeviceRegistrationRequiresListener() {
        try {
            final BackEndRegistrationApiRequestImpl request = new BackEndRegistrationApiRequestImpl(getContext(), new FakeNetworkWrapper());
            request.startUpdateDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, TEST_BACK_END_DEVICE_REGISTRATION_ID, getParameters(), null);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testSuccessfulNewDeviceRegistrationRequest() {
        makeListenersForSuccessfulRequestFromNetwork(true, 200, HTTP_POST, null);
        final BackEndRegistrationApiRequestImpl request = new BackEndRegistrationApiRequestImpl(getContext(), networkWrapper);
        request.startNewDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, getParameters(), listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testSuccessfulUpdateDeviceRegistrationRequest() {
        makeListenersForSuccessfulRequestFromNetwork(true, 200, HTTP_PUT, TEST_BACK_END_DEVICE_REGISTRATION_ID);
        final BackEndRegistrationApiRequestImpl request = new BackEndRegistrationApiRequestImpl(getContext(), networkWrapper);
        request.startUpdateDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, TEST_BACK_END_DEVICE_REGISTRATION_ID, getParameters(), listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testNewDeviceRegistrationNullResponse() {
        makeListenersForSuccessfulNullResultFromNetwork(HTTP_POST, null);
        final BackEndRegistrationApiRequestImpl request = new BackEndRegistrationApiRequestImpl(getContext(), networkWrapper);
        request.startNewDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, getParameters(), listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testUpdateDeviceRegistrationNullResponse() {
        makeListenersForSuccessfulNullResultFromNetwork(HTTP_PUT, TEST_BACK_END_DEVICE_REGISTRATION_ID);
        final BackEndRegistrationApiRequestImpl request = new BackEndRegistrationApiRequestImpl(getContext(), networkWrapper);
        request.startUpdateDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, TEST_BACK_END_DEVICE_REGISTRATION_ID, getParameters(), listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testNewDeviceRegistrationSuccessful404() {
        makeListenersForSuccessfulRequestFromNetwork(false, 404, HTTP_POST, null);
        final BackEndRegistrationApiRequestImpl request = new BackEndRegistrationApiRequestImpl(getContext(), networkWrapper);
        request.startNewDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, getParameters(), listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testUpdateDeviceRegistrationSuccessful404() {
        makeListenersForSuccessfulRequestFromNetwork(false, 404, HTTP_PUT, TEST_BACK_END_DEVICE_REGISTRATION_ID);
        final BackEndRegistrationApiRequestImpl request = new BackEndRegistrationApiRequestImpl(getContext(), networkWrapper);
        request.startUpdateDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, TEST_BACK_END_DEVICE_REGISTRATION_ID, getParameters(), listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testNewDeviceRegistrationCouldNotConnect() {
        makeListenersFromFailedRequestFromNetwork("Your server is busted", 0, HTTP_POST, null);
        final BackEndRegistrationApiRequestImpl request = new BackEndRegistrationApiRequestImpl(getContext(), networkWrapper);
        request.startNewDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, getParameters(), listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testUpdateDeviceRegistrationCouldNotConnect() {
        makeListenersFromFailedRequestFromNetwork("Your server is busted", 0, HTTP_PUT, TEST_BACK_END_DEVICE_REGISTRATION_ID);
        final BackEndRegistrationApiRequestImpl request = new BackEndRegistrationApiRequestImpl(getContext(), networkWrapper);
        request.startUpdateDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, TEST_BACK_END_DEVICE_REGISTRATION_ID, getParameters(), listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testNewDeviceRegistrationBadNetworkResponse() {
        makeListenersWithBadNetworkResponse(HTTP_POST, null);
        final BackEndRegistrationApiRequestImpl request = new BackEndRegistrationApiRequestImpl(getContext(), networkWrapper);
        request.startNewDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, getParameters(), listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testUpdateDeviceRegistrationBadNetworkResponse() {
        makeListenersWithBadNetworkResponse(HTTP_PUT, TEST_BACK_END_DEVICE_REGISTRATION_ID);
        final BackEndRegistrationApiRequestImpl request = new BackEndRegistrationApiRequestImpl(getContext(), networkWrapper);
        request.startUpdateDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, TEST_BACK_END_DEVICE_REGISTRATION_ID, getParameters(), listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testNewDeviceRegistrationNoDeviceUuidInResponse() {
        makeListenersWithNoDeviceUuidInResponse(HTTP_POST, null);
        final BackEndRegistrationApiRequestImpl request = new BackEndRegistrationApiRequestImpl(getContext(), networkWrapper);
        request.startNewDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, getParameters(), listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testUpdateDeviceRegistrationNoDeviceUuidInResponse() {
        makeListenersWithNoDeviceUuidInResponse(HTTP_PUT, TEST_BACK_END_DEVICE_REGISTRATION_ID);
        final BackEndRegistrationApiRequestImpl request = new BackEndRegistrationApiRequestImpl(getContext(), networkWrapper);
        request.startUpdateDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, TEST_BACK_END_DEVICE_REGISTRATION_ID, getParameters(), listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testAuthorization() {
        final String base64encodedAuthorization = BackEndRegistrationApiRequestImpl.getBasicAuthorizationValue(getParameters());
        assertEquals("Basic  " + TEST_BASE64_ENCODED_AUTHORIZATION, base64encodedAuthorization);
    }

    private void makeListenersForSuccessfulRequestFromNetwork(boolean isSuccessfulResult, int expectedHttpStatusCode, String expectedHttpMethod, String previousBackEndDeviceRegistrationId) {
        final String resultantJson = "{\"device_uuid\" : \"" + TEST_BACK_END_DEVICE_REGISTRATION_ID + "\"}";
        FakeHttpURLConnection.setResponseData(resultantJson);
        FakeHttpURLConnection.setResponseCode(expectedHttpStatusCode);
        makeBackEndRegistrationApiRequestListener(isSuccessfulResult, expectedHttpMethod, previousBackEndDeviceRegistrationId);
    }

    private void makeListenersForSuccessfulNullResultFromNetwork(String expectedHttpMethod, String previousBackEndDeviceRegistrationId) {
        FakeHttpURLConnection.setResponseData(null);
        FakeHttpURLConnection.setResponseCode(200);
        makeBackEndRegistrationApiRequestListener(false, expectedHttpMethod, previousBackEndDeviceRegistrationId);
    }

    private void makeListenersWithBadNetworkResponse(String expectedHttpMethod, String previousBackEndDeviceRegistrationId) {
        FakeHttpURLConnection.setResponseData("{{{{{{{");
        FakeHttpURLConnection.setResponseCode(200);
        makeBackEndRegistrationApiRequestListener(false, expectedHttpMethod, previousBackEndDeviceRegistrationId);
    }

    private void makeListenersWithNoDeviceUuidInResponse(String expectedHttpMethod, String previousBackEndDeviceRegistrationId) {
        FakeHttpURLConnection.setResponseData("{}");
        FakeHttpURLConnection.setResponseCode(200);
        makeBackEndRegistrationApiRequestListener(false, expectedHttpMethod, previousBackEndDeviceRegistrationId);
    }

    private void makeListenersFromFailedRequestFromNetwork(String exceptionText, int expectedHttpStatusCode, String expectedHttpMethod, String previousBackEndDeviceRegistrationId) {
        IOException exception = null;
        if (exceptionText != null) {
            exception = new IOException(exceptionText);
        }
        FakeHttpURLConnection.setConnectionException(exception);
        FakeHttpURLConnection.willThrowConnectionException(true);
        FakeHttpURLConnection.setResponseCode(expectedHttpStatusCode);
        makeBackEndRegistrationApiRequestListener(false, expectedHttpMethod, previousBackEndDeviceRegistrationId);
    }

    public void makeBackEndRegistrationApiRequestListener(final boolean isSuccessfulRequest, final String expectedHttpMethod, final String previousBackEndDeviceRegistrationId) {
        listener = new BackEndRegistrationListener() {

            @Override
            public void onBackEndRegistrationSuccess(String backEndDeviceRegistrationId) {
                assertTrue(isSuccessfulRequest);
                assertEquals(expectedHttpMethod, FakeHttpURLConnection.getReceivedHttpMethod());
                assertTrue(FakeHttpURLConnection.getRequestPropertiesMap().containsKey("Authorization"));
                if (previousBackEndDeviceRegistrationId != null) {
                    assertTrue(FakeHttpURLConnection.getReceivedURL().toString().endsWith(previousBackEndDeviceRegistrationId));
                }
                if (isSuccessfulRequest) {
                    delayedLoop.flagSuccess();
                    assertEquals(TEST_BACK_END_DEVICE_REGISTRATION_ID, backEndDeviceRegistrationId);
                } else {
                    delayedLoop.flagFailure();
                }
            }

            @Override
            public void onBackEndRegistrationFailed(String reason) {
                assertFalse(isSuccessfulRequest);
                assertEquals(expectedHttpMethod, FakeHttpURLConnection.getReceivedHttpMethod());
                if (previousBackEndDeviceRegistrationId != null) {
                    assertTrue(FakeHttpURLConnection.getReceivedURL().toString().endsWith(previousBackEndDeviceRegistrationId));
                }
                if (isSuccessfulRequest) {
                    delayedLoop.flagFailure();
                } else {
                    delayedLoop.flagSuccess();
                }
            }
        };
    }

    private RegistrationParameters getParameters() {
        return new RegistrationParameters(TEST_SENDER_ID, TEST_VARIANT_UUID, TEST_VARIANT_SECRET, TEST_DEVICE_ALIAS, TEST_BASE_SERVER_URL);
    }
}
