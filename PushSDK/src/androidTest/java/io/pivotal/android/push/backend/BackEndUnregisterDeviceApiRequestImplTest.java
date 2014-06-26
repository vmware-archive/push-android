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
import java.net.URL;

import io.pivotal.android.common.test.network.FakeHttpURLConnection;
import io.pivotal.android.common.test.network.FakeNetworkWrapper;
import io.pivotal.android.common.test.util.DelayedLoop;
import io.pivotal.android.push.RegistrationParameters;

public class BackEndUnregisterDeviceApiRequestImplTest extends AndroidTestCase {

    private static final String TEST_BACK_END_DEVICE_REGISTRATION_ID = "TEST_BACK_END_DEVICE_REGISTRATION_ID";
    private static final long TEN_SECOND_TIMEOUT = 10000L;
    private static final String TEST_GCM_SENDER_ID = "TEST_GCM_SENDER_ID";
    private static final String TEST_VARIANT_UUID = "TEST_VARIANT_UUID";
    private static final String TEST_VARIANT_SECRET = "TEST_VARIANT_SECRET";
    private static final String TEST_DEVICE_ALIAS = "TEST_DEVICE_ALIAS";
    private static final String TEST_BASE_SERVER_URL = "http://test.com";

    private RegistrationParameters parameters;
    private FakeNetworkWrapper networkWrapper;
    private DelayedLoop delayedLoop;
    private BackEndUnregisterDeviceListener backEndUnregisterDeviceListener;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final URL url = new URL(TEST_BASE_SERVER_URL);
        parameters = new RegistrationParameters(TEST_GCM_SENDER_ID, TEST_VARIANT_UUID, TEST_VARIANT_SECRET, TEST_DEVICE_ALIAS, url);
        networkWrapper = new FakeNetworkWrapper();
        delayedLoop = new DelayedLoop(TEN_SECOND_TIMEOUT);
        FakeHttpURLConnection.reset();
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
            final BackEndUnregisterDeviceApiRequestImpl backEndRegistrationApiRequestImpl = new BackEndUnregisterDeviceApiRequestImpl(new FakeNetworkWrapper());
            makeBackEndUnegisterDeviceApiRequestListener(true);
            backEndRegistrationApiRequestImpl.startUnregisterDevice(null, parameters, backEndUnregisterDeviceListener);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testRequiresParameters() {
        try {
            final BackEndUnregisterDeviceApiRequestImpl backEndRegistrationApiRequestImpl = new BackEndUnregisterDeviceApiRequestImpl(new FakeNetworkWrapper());
            makeBackEndUnegisterDeviceApiRequestListener(true);
            backEndRegistrationApiRequestImpl.startUnregisterDevice(TEST_BACK_END_DEVICE_REGISTRATION_ID, null, backEndUnregisterDeviceListener);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testRequiresListener() {
        try {
            final BackEndUnregisterDeviceApiRequestImpl backEndRegistrationApiRequestImpl = new BackEndUnregisterDeviceApiRequestImpl(new FakeNetworkWrapper());
            backEndRegistrationApiRequestImpl.startUnregisterDevice(TEST_BACK_END_DEVICE_REGISTRATION_ID, parameters, null);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testSuccessfulRequest() {
        makeListenersForSuccessfulRequestFromNetwork(true, 200);
        final BackEndUnregisterDeviceApiRequestImpl registrar = new BackEndUnregisterDeviceApiRequestImpl(networkWrapper);
        registrar.startUnregisterDevice(TEST_BACK_END_DEVICE_REGISTRATION_ID, parameters, backEndUnregisterDeviceListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testFailed405() {
        makeListenersForSuccessfulRequestFromNetwork(false, 405);
        final BackEndUnregisterDeviceApiRequestImpl registrar = new BackEndUnregisterDeviceApiRequestImpl(networkWrapper);
        registrar.startUnregisterDevice(TEST_BACK_END_DEVICE_REGISTRATION_ID, parameters, backEndUnregisterDeviceListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    // 404 errors are not considered failures
    public void testSuccessful404() {
        makeListenersForSuccessfulRequestFromNetwork(true, 404);
        final BackEndUnregisterDeviceApiRequestImpl registrar = new BackEndUnregisterDeviceApiRequestImpl(networkWrapper);
        registrar.startUnregisterDevice(TEST_BACK_END_DEVICE_REGISTRATION_ID, parameters, backEndUnregisterDeviceListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testFailed403() {
        makeListenersForSuccessfulRequestFromNetwork(false, 403);
        final BackEndUnregisterDeviceApiRequestImpl registrar = new BackEndUnregisterDeviceApiRequestImpl(networkWrapper);
        registrar.startUnregisterDevice(TEST_BACK_END_DEVICE_REGISTRATION_ID, parameters, backEndUnregisterDeviceListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testCouldNotConnect() {
        makeListenersFromFailedRequestFromNetwork("Your server is busted");
        final BackEndUnregisterDeviceApiRequestImpl registrar = new BackEndUnregisterDeviceApiRequestImpl(networkWrapper);
        registrar.startUnregisterDevice(TEST_BACK_END_DEVICE_REGISTRATION_ID, parameters, backEndUnregisterDeviceListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    private void makeListenersForSuccessfulRequestFromNetwork(boolean isSuccessfulResult, int expectedHttpStatusCode) {
        FakeHttpURLConnection.setResponseCode(expectedHttpStatusCode);
        makeBackEndUnegisterDeviceApiRequestListener(isSuccessfulResult);
    }

    private void makeListenersFromFailedRequestFromNetwork(String exceptionText) {
        IOException exception = null;
        if (exceptionText != null) {
            exception = new IOException(exceptionText);
        }
        FakeHttpURLConnection.willThrowConnectionException(true);
        FakeHttpURLConnection.setConnectionException(exception);
        makeBackEndUnegisterDeviceApiRequestListener(false);
    }

    public void makeBackEndUnegisterDeviceApiRequestListener(final boolean isSuccessfulRequest) {
        backEndUnregisterDeviceListener = new BackEndUnregisterDeviceListener() {

            @Override
            public void onBackEndUnregisterDeviceSuccess() {
                assertTrue(isSuccessfulRequest);
                assertEquals("DELETE", FakeHttpURLConnection.getReceivedHttpMethod());
                assertTrue(FakeHttpURLConnection.getRequestPropertiesMap().containsKey("Authorization"));
                if (isSuccessfulRequest) {
                    assertTrue(FakeHttpURLConnection.getReceivedURL().toString().endsWith(TEST_BACK_END_DEVICE_REGISTRATION_ID));
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
