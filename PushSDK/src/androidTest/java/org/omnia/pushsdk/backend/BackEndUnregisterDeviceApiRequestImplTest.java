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

import org.omnia.pushsdk.network.MockHttpURLConnection;
import org.omnia.pushsdk.network.MockNetworkWrapper;
import com.xtreme.commons.testing.DelayedLoop;

import java.io.IOException;

public class BackEndUnregisterDeviceApiRequestImplTest extends AndroidTestCase {

    private static final String TEST_BACK_END_DEVICE_REGISTRATION_ID = "TEST_BACK_END_DEVICE_REGISTRATION_ID";
    private static final long TEN_SECOND_TIMEOUT = 10000L;

    private MockNetworkWrapper networkWrapper;
    private DelayedLoop delayedLoop;
    private BackEndUnregisterDeviceListener backEndUnregisterDeviceListener;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        networkWrapper = new MockNetworkWrapper();
        delayedLoop = new DelayedLoop(TEN_SECOND_TIMEOUT);
        MockHttpURLConnection.reset();
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
        makeListenersForSuccessfulRequestFromNetwork(true, 200);
        final BackEndUnregisterDeviceApiRequestImpl registrar = new BackEndUnregisterDeviceApiRequestImpl(networkWrapper);
        registrar.startUnregisterDevice(TEST_BACK_END_DEVICE_REGISTRATION_ID, backEndUnregisterDeviceListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testSuccessful404() {
        makeListenersForSuccessfulRequestFromNetwork(false, 404);
        final BackEndUnregisterDeviceApiRequestImpl registrar = new BackEndUnregisterDeviceApiRequestImpl(networkWrapper);
        registrar.startUnregisterDevice(TEST_BACK_END_DEVICE_REGISTRATION_ID, backEndUnregisterDeviceListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testCouldNotConnect() {
        makeListenersFromFailedRequestFromNetwork("Your server is busted", 0);
        final BackEndUnregisterDeviceApiRequestImpl registrar = new BackEndUnregisterDeviceApiRequestImpl(networkWrapper);
        registrar.startUnregisterDevice(TEST_BACK_END_DEVICE_REGISTRATION_ID, backEndUnregisterDeviceListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    private void makeListenersForSuccessfulRequestFromNetwork(boolean isSuccessfulResult, int expectedHttpStatusCode) {
        MockHttpURLConnection.setResponseCode(expectedHttpStatusCode);
        makeBackEndUnegisterDeviceApiRequestListener(isSuccessfulResult);
    }

    private void makeListenersFromFailedRequestFromNetwork(String exceptionText, int expectedHttpStatusCode) {
        IOException exception = null;
        if (exceptionText != null) {
            exception = new IOException(exceptionText);
        }
        MockHttpURLConnection.willThrowConnectionException(true);
        MockHttpURLConnection.setConnectionException(exception);
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
