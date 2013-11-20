package com.gopivotal.pushlib.backend;

import android.test.AndroidTestCase;

import com.gopivotal.pushlib.PushLibParameters;
import com.gopivotal.pushlib.network.MockNetworkWrapper;
import com.xtreme.commons.testing.DelayedLoop;
import com.xtreme.network.MockNetworkRequestLauncher;
import com.xtreme.network.MockNetworkRequestListener;
import com.xtreme.network.MockNetworkResponse;
import com.xtreme.network.NetworkError;
import com.xtreme.network.NetworkRequest;
import com.xtreme.network.NetworkResponse;

import java.util.concurrent.Semaphore;

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
    private boolean wasRequestSuccessful;
    private Semaphore semaphore;
    private BackEndRegistrationListener backEndRegistrationListener;

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
            new BackEndRegistrationApiRequestImpl(null);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testRequiresGcmDeviceRegistrationId() {
        try {
            final BackEndRegistrationApiRequestImpl backEndRegistrationApiRequestImpl = new BackEndRegistrationApiRequestImpl(new MockNetworkWrapper());
            makeBackEndRegistrationApiRequestListener(true);
            backEndRegistrationApiRequestImpl.startDeviceRegistration(null, getParameters(), backEndRegistrationListener);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testRequiresParameters() {
        try {
            final BackEndRegistrationApiRequestImpl backEndRegistrationApiRequestImpl = new BackEndRegistrationApiRequestImpl(new MockNetworkWrapper());
            backEndRegistrationApiRequestImpl.startDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, null, backEndRegistrationListener);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testRequiresListener() {
        try {
            final BackEndRegistrationApiRequestImpl backEndRegistrationApiRequestImpl = new BackEndRegistrationApiRequestImpl(new MockNetworkWrapper());
            backEndRegistrationApiRequestImpl.startDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, getParameters(), null);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testSuccessfulRequest() {
        makeListenersForSuccessfulRequestFromNetwork(NO_DELAY, true, 200);
        final BackEndRegistrationApiRequestImpl registrar = new BackEndRegistrationApiRequestImpl(networkWrapper);
        registrar.startDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, getParameters(), backEndRegistrationListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testSuccessfulAsync() {
        makeListenersForSuccessfulRequestFromNetwork(ONE_SECOND_DELAY, true, 200);
        final BackEndRegistrationApiRequestImpl registrar = new BackEndRegistrationApiRequestImpl(networkWrapper);
        registrar.startDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, getParameters(), backEndRegistrationListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testNullResponse() {
        makeListenersForSuccessfulNullResultFromNetwork(NO_DELAY);
        final BackEndRegistrationApiRequestImpl registrar = new BackEndRegistrationApiRequestImpl(networkWrapper);
        registrar.startDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, getParameters(), backEndRegistrationListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testNoStatusLine() {
        makeListenersForSuccessfulWithNoStatusLineFromNetwork(NO_DELAY);
        final BackEndRegistrationApiRequestImpl registrar = new BackEndRegistrationApiRequestImpl(networkWrapper);
        registrar.startDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, getParameters(), backEndRegistrationListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testSuccessful404() {
        makeListenersForSuccessfulRequestFromNetwork(NO_DELAY, false, 404);
        final BackEndRegistrationApiRequestImpl registrar = new BackEndRegistrationApiRequestImpl(networkWrapper);
        registrar.startDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, getParameters(), backEndRegistrationListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testCouldNotConnect() {
        makeListenersFromFailedRequestFromNetwork(NO_DELAY, "Your server is busted", 0);
        final BackEndRegistrationApiRequestImpl registrar = new BackEndRegistrationApiRequestImpl(networkWrapper);
        registrar.startDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, getParameters(), backEndRegistrationListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testNullNetworkResponse() {
        makeListenersWithEmptyNetworkResponse(NO_DELAY);
        final BackEndRegistrationApiRequestImpl registrar = new BackEndRegistrationApiRequestImpl(networkWrapper);
        registrar.startDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, getParameters(), backEndRegistrationListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testBadNetworkResponse() {
        makeListenersWithBadNetworkResponse(NO_DELAY);
        final BackEndRegistrationApiRequestImpl registrar = new BackEndRegistrationApiRequestImpl(networkWrapper);
        registrar.startDeviceRegistration(TEST_GCM_DEVICE_REGISTRATION_ID, getParameters(), backEndRegistrationListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testNoDeviceUuidInResponse() {
        makeListenersWithNoDeviceUuidInResponse(NO_DELAY);
        final BackEndRegistrationApiRequestImpl registrar = new BackEndRegistrationApiRequestImpl(networkWrapper);
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

    private PushLibParameters getParameters() {
        return new PushLibParameters(TEST_SENDER_ID, TEST_RELEASE_UUID, TEST_RELEASE_SECRET, TEST_DEVICE_ALIAS);
    }
}
