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

import org.omnia.pushsdk.network.MockNetworkWrapper;
import com.xtreme.commons.testing.DelayedLoop;
import com.xtreme.network.MockNetworkRequestLauncher;
import com.xtreme.network.MockNetworkRequestListener;
import com.xtreme.network.MockNetworkResponse;
import com.xtreme.network.NetworkError;
import com.xtreme.network.NetworkRequest;
import com.xtreme.network.NetworkResponse;

import java.util.concurrent.Semaphore;

public class BackEndUnregisterDeviceApiRequestImplTest extends AndroidTestCase {

    private static final String TEST_BACK_END_DEVICE_REGISTRATION_ID = "TEST_BACK_END_DEVICE_REGISTRATION_ID";
    private static final long TEN_SECOND_TIMEOUT = 10000L;
    private static final long NO_DELAY = 0L;
    private static final long ONE_SECOND_DELAY = 1000L;

    private MockNetworkWrapper networkWrapper;
    private MockNetworkRequestLauncher networkRequestLauncher;
    private DelayedLoop delayedLoop;
    private boolean wasRequestSuccessful;
    private Semaphore semaphore;
    private BackEndUnregisterDeviceListener backEndUnregisterDeviceListener;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        semaphore = new Semaphore(0);
        networkWrapper = new MockNetworkWrapper();
        networkRequestLauncher = (MockNetworkRequestLauncher) networkWrapper.getNetworkRequestLauncher();
        delayedLoop = new DelayedLoop(TEN_SECOND_TIMEOUT);
    }

    public void testRequiresNetworkWrapper() {
        try {
            new BackEndUnregisterDeviceApiRequestImpl(null);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testRequiresBackEndDeviceRegistrationId() {
        try {
            final BackEndUnregisterDeviceApiRequestImpl backEndRegistrationApiRequestImpl = new BackEndUnregisterDeviceApiRequestImpl(new MockNetworkWrapper());
            makeBackEndUnegisterDeviceApiRequestListener(true);
            backEndRegistrationApiRequestImpl.startUnregisterDevice(null, backEndUnregisterDeviceListener);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testRequiresListener() {
        try {
            final BackEndUnregisterDeviceApiRequestImpl backEndRegistrationApiRequestImpl = new BackEndUnregisterDeviceApiRequestImpl(new MockNetworkWrapper());
            backEndRegistrationApiRequestImpl.startUnregisterDevice(TEST_BACK_END_DEVICE_REGISTRATION_ID, null);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testSuccessfulRequest() {
        makeListenersForSuccessfulRequestFromNetwork(NO_DELAY, true, 200);
        final BackEndUnregisterDeviceApiRequestImpl registrar = new BackEndUnregisterDeviceApiRequestImpl(networkWrapper);
        registrar.startUnregisterDevice(TEST_BACK_END_DEVICE_REGISTRATION_ID, backEndUnregisterDeviceListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testSuccessfulAsync() {
        makeListenersForSuccessfulRequestFromNetwork(ONE_SECOND_DELAY, true, 200);
        final BackEndUnregisterDeviceApiRequestImpl registrar = new BackEndUnregisterDeviceApiRequestImpl(networkWrapper);
        registrar.startUnregisterDevice(TEST_BACK_END_DEVICE_REGISTRATION_ID, backEndUnregisterDeviceListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testNullResponse() {
        makeListenersForSuccessfulNullResultFromNetwork(NO_DELAY);
        final BackEndUnregisterDeviceApiRequestImpl registrar = new BackEndUnregisterDeviceApiRequestImpl(networkWrapper);
        registrar.startUnregisterDevice(TEST_BACK_END_DEVICE_REGISTRATION_ID, backEndUnregisterDeviceListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testNoStatusLine() {
        makeListenersForSuccessfulWithNoStatusLineFromNetwork(NO_DELAY);
        final BackEndUnregisterDeviceApiRequestImpl registrar = new BackEndUnregisterDeviceApiRequestImpl(networkWrapper);
        registrar.startUnregisterDevice(TEST_BACK_END_DEVICE_REGISTRATION_ID, backEndUnregisterDeviceListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testSuccessful404() {
        makeListenersForSuccessfulRequestFromNetwork(NO_DELAY, false, 404);
        final BackEndUnregisterDeviceApiRequestImpl registrar = new BackEndUnregisterDeviceApiRequestImpl(networkWrapper);
        registrar.startUnregisterDevice(TEST_BACK_END_DEVICE_REGISTRATION_ID, backEndUnregisterDeviceListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testCouldNotConnect() {
        makeListenersFromFailedRequestFromNetwork(NO_DELAY, "Your server is busted", 0);
        final BackEndUnregisterDeviceApiRequestImpl registrar = new BackEndUnregisterDeviceApiRequestImpl(networkWrapper);
        registrar.startUnregisterDevice(TEST_BACK_END_DEVICE_REGISTRATION_ID, backEndUnregisterDeviceListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }
    private void makeListenersForSuccessfulRequestFromNetwork(long delay, boolean isSuccessfulResult, int expectedHttpStatusCode) {
        final NetworkResponse response = networkRequestLauncher.getNetworkResponse("", expectedHttpStatusCode);
        makeNetworkRequestListenerForSuccessfulRequest(delay, response);
        makeBackEndUnegisterDeviceApiRequestListener(isSuccessfulResult);
    }

    private void makeListenersForSuccessfulNullResultFromNetwork(long delay) {
        makeNetworkRequestListenerForSuccessfulRequest(delay, null);
        makeBackEndUnegisterDeviceApiRequestListener(false);
    }

    private void makeListenersForSuccessfulWithNoStatusLineFromNetwork(long delay) {
        final NetworkResponse response = networkRequestLauncher.getNetworkResponse(null, MockNetworkResponse.NO_STATUS_CODE);
        makeNetworkRequestListenerForSuccessfulRequest(delay, response);
        makeBackEndUnegisterDeviceApiRequestListener(false);
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
        makeBackEndUnegisterDeviceApiRequestListener(false);
    }

    public void makeBackEndUnegisterDeviceApiRequestListener(final boolean isSuccessfulRequest) {
        backEndUnregisterDeviceListener = new BackEndUnregisterDeviceListener() {

            @Override
            public void onBackEndUnregisterDeviceSuccess() {
                assertTrue(isSuccessfulRequest);
                if (isSuccessfulRequest) {
                    delayedLoop.flagSuccess();
                } else {
                    delayedLoop.flagFailure();
                }
            }

            @Override
            public void onBackEndUnregisterDeviceFailed(String reason) {
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
