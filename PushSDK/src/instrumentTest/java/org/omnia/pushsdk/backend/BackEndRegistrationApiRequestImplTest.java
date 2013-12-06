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

package org.omnia.pushsdk.backend;

import android.test.AndroidTestCase;

import org.omnia.pushsdk.RegistrationParameters;
import org.omnia.pushsdk.network.MockNetworkWrapper;
import com.xtreme.commons.testing.DelayedLoop;
import com.xtreme.network.MockNetworkRequestLauncher;
import com.xtreme.network.MockNetworkRequestListener;
import com.xtreme.network.MockNetworkResponse;
import com.xtreme.network.NetworkError;
import com.xtreme.network.NetworkRequest;
import com.xtreme.network.NetworkResponse;

public class BackEndRegistrationApiRequestImplTest extends AndroidTestCase {

    private static final String TEST_BACK_END_DEVICE_REGISTRATION_ID = "TEST_BACK_END_DEVICE_REGISTRATION_ID";
    private static final String TEST_GCM_DEVICE_REGISTRATION_ID = "TEST_GCM_DEVICE_REGISTRATION_ID";
    private static final String TEST_SENDER_ID = "TEST_SENDER_ID";
    private static final String TEST_RELEASE_UUID = "TEST_RELEASE_UUID";
    private static final String TEST_RELEASE_SECRET = "TEST_RELEASE_SECRET";
    private static final String TEST_DEVICE_ALIAS = "TEST_DEVICE_ALIAS";
    private static final long TEN_SECOND_TIMEOUT = 10000L;
    private static final long NO_DELAY = 0L;
    private static final long ONE_SECOND_DELAY = 1000L;

    private MockNetworkWrapper networkWrapper;
    private MockNetworkRequestLauncher networkRequestLauncher;
    private DelayedLoop delayedLoop;
    private BackEndRegistrationListener backEndRegistrationListener;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        networkWrapper = new MockNetworkWrapper();
        networkRequestLauncher = (MockNetworkRequestLauncher) networkWrapper.getNetworkRequestLauncher();
        delayedLoop = new DelayedLoop(TEN_SECOND_TIMEOUT);
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

    public void testRequiresGcmDeviceRegistrationId() {
        try {
            final BackEndRegistrationApiRequestImpl backEndRegistrationApiRequestImpl = new BackEndRegistrationApiRequestImpl(getContext(), new MockNetworkWrapper());
            makeBackEndRegistrationApiRequestListener(true);
            backEndRegistrationApiRequestImpl.startDeviceRegistration(null, getParameters(), backEndRegistrationListener);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testRequiresParameters() {
        try {
            final BackEndRegistrationApiRequestImpl backEndRegistrationApiRequestImpl = new BackEndRegistrationApiRequestImpl(getContext(), new MockNetworkWrapper());
            backEndRegistrationApiRequestImpl.startDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, null, backEndRegistrationListener);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testRequiresListener() {
        try {
            final BackEndRegistrationApiRequestImpl backEndRegistrationApiRequestImpl = new BackEndRegistrationApiRequestImpl(getContext(), new MockNetworkWrapper());
            backEndRegistrationApiRequestImpl.startDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, getParameters(), null);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testSuccessfulRequest() {
        makeListenersForSuccessfulRequestFromNetwork(NO_DELAY, true, 200);
        final BackEndRegistrationApiRequestImpl registrar = new BackEndRegistrationApiRequestImpl(getContext(), networkWrapper);
        registrar.startDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, getParameters(), backEndRegistrationListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testSuccessfulAsync() {
        makeListenersForSuccessfulRequestFromNetwork(ONE_SECOND_DELAY, true, 200);
        final BackEndRegistrationApiRequestImpl registrar = new BackEndRegistrationApiRequestImpl(getContext(), networkWrapper);
        registrar.startDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, getParameters(), backEndRegistrationListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testNullResponse() {
        makeListenersForSuccessfulNullResultFromNetwork(NO_DELAY);
        final BackEndRegistrationApiRequestImpl registrar = new BackEndRegistrationApiRequestImpl(getContext(), networkWrapper);
        registrar.startDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, getParameters(), backEndRegistrationListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testNoStatusLine() {
        makeListenersForSuccessfulWithNoStatusLineFromNetwork(NO_DELAY);
        final BackEndRegistrationApiRequestImpl registrar = new BackEndRegistrationApiRequestImpl(getContext(), networkWrapper);
        registrar.startDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, getParameters(), backEndRegistrationListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testSuccessful404() {
        makeListenersForSuccessfulRequestFromNetwork(NO_DELAY, false, 404);
        final BackEndRegistrationApiRequestImpl registrar = new BackEndRegistrationApiRequestImpl(getContext(), networkWrapper);
        registrar.startDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, getParameters(), backEndRegistrationListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testCouldNotConnect() {
        makeListenersFromFailedRequestFromNetwork(NO_DELAY, "Your server is busted", 0);
        final BackEndRegistrationApiRequestImpl registrar = new BackEndRegistrationApiRequestImpl(getContext(), networkWrapper);
        registrar.startDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, getParameters(), backEndRegistrationListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testNullNetworkResponse() {
        makeListenersWithEmptyNetworkResponse(NO_DELAY);
        final BackEndRegistrationApiRequestImpl registrar = new BackEndRegistrationApiRequestImpl(getContext(), networkWrapper);
        registrar.startDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, getParameters(), backEndRegistrationListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testBadNetworkResponse() {
        makeListenersWithBadNetworkResponse(NO_DELAY);
        final BackEndRegistrationApiRequestImpl registrar = new BackEndRegistrationApiRequestImpl(getContext(), networkWrapper);
        registrar.startDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, getParameters(), backEndRegistrationListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testNoDeviceUuidInResponse() {
        makeListenersWithNoDeviceUuidInResponse(NO_DELAY);
        final BackEndRegistrationApiRequestImpl registrar = new BackEndRegistrationApiRequestImpl(getContext(), networkWrapper);
        registrar.startDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, getParameters(), backEndRegistrationListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    private void makeListenersForSuccessfulRequestFromNetwork(long delay, boolean isSuccessfulResult, int expectedHttpStatusCode) {
        final String resultantJson = "{\"device_uuid\" : \"" + TEST_BACK_END_DEVICE_REGISTRATION_ID + "\"}";
        final NetworkResponse response = networkRequestLauncher.getNetworkResponse(resultantJson, expectedHttpStatusCode);
        makeNetworkRequestListenerForSuccessfulRequest(delay, response);
        makeBackEndRegistrationApiRequestListener(isSuccessfulResult);
    }

    private void makeListenersForSuccessfulNullResultFromNetwork(long delay) {
        makeNetworkRequestListenerForSuccessfulRequest(delay, null);
        makeBackEndRegistrationApiRequestListener(false);
    }

    private void makeListenersForSuccessfulWithNoStatusLineFromNetwork(long delay) {
        final NetworkResponse response = networkRequestLauncher.getNetworkResponse(null, MockNetworkResponse.NO_STATUS_CODE);
        makeNetworkRequestListenerForSuccessfulRequest(delay, response);
        makeBackEndRegistrationApiRequestListener(false);
    }

    private void makeListenersWithEmptyNetworkResponse(long delay) {
        final NetworkResponse response = networkRequestLauncher.getNetworkResponse(null, 200);
        makeNetworkRequestListenerForSuccessfulRequest(delay, response);
        makeBackEndRegistrationApiRequestListener(false);
    }

    private void makeListenersWithBadNetworkResponse(long delay) {
        final NetworkResponse response = networkRequestLauncher.getNetworkResponse("{{{{{{{", 200);
        makeNetworkRequestListenerForSuccessfulRequest(delay, response);
        makeBackEndRegistrationApiRequestListener(false);
    }

    private void makeListenersWithNoDeviceUuidInResponse(long delay) {
        final NetworkResponse response = networkRequestLauncher.getNetworkResponse("{}", 200);
        makeNetworkRequestListenerForSuccessfulRequest(delay, response);
        makeBackEndRegistrationApiRequestListener(false);
    }

    private void makeNetworkRequestListenerForSuccessfulRequest(final long delay, final NetworkResponse response) {
        networkRequestLauncher.setNextRequestResolution(new MockNetworkRequestListener() {

            @Override
            public void onSuccess(NetworkRequest networkRequest, NetworkResponse networkRequestResponse) {
            }

            @Override
            public void onFailure(NetworkRequest networkRequest, NetworkError networkError) {
                fail();
            }
        }, response, delay);
    }

    private void makeNetworkRequestListenerForFailedRequest(final NetworkError error, final long delay) {
        networkRequestLauncher.setNextRequestResolution(new MockNetworkRequestListener() {

            @Override
            public void onSuccess(NetworkRequest networkRequest, NetworkResponse networkRequestResponse) {
                fail();
            }

            @Override
            public void onFailure(NetworkRequest networkRequest, NetworkError networkError) {
            }
        }, error, delay);
    }

    private void makeListenersFromFailedRequestFromNetwork(long delay, String exceptionText, int expectedHttpStatusCode) {
        Exception exception = null;
        if (exceptionText != null) {
            exception = new Exception(exceptionText);
        }
        final NetworkError error = networkRequestLauncher.getNetworkError(exception, expectedHttpStatusCode);
        makeNetworkRequestListenerForFailedRequest(error, delay);
        makeBackEndRegistrationApiRequestListener(false);
    }

    public void makeBackEndRegistrationApiRequestListener(final boolean isSuccessfulRequest) {
        backEndRegistrationListener = new BackEndRegistrationListener() {

            @Override
            public void onBackEndRegistrationSuccess(String backEndDeviceRegistrationId) {
                assertTrue(isSuccessfulRequest);
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
                if (isSuccessfulRequest) {
                    delayedLoop.flagFailure();
                } else {
                    delayedLoop.flagSuccess();
                }
            }
        };
    }

    private RegistrationParameters getParameters() {
        return new RegistrationParameters(TEST_SENDER_ID, TEST_RELEASE_UUID, TEST_RELEASE_SECRET, TEST_DEVICE_ALIAS);
    }
}
