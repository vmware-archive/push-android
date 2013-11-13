package com.gopivotal.pushlib.api;

import android.test.AndroidTestCase;

import com.gopivotal.pushlib.network.MockNetworkWrapper;
import com.xtreme.commons.testing.DelayedLoop;
import com.xtreme.network.MockNetworkRequestLauncher;
import com.xtreme.network.MockNetworkRequestListener;
import com.xtreme.network.MockNetworkResponse;
import com.xtreme.network.NetworkError;
import com.xtreme.network.NetworkRequest;
import com.xtreme.network.NetworkResponse;

import java.util.concurrent.Semaphore;

public class ApiRegistrarTest extends AndroidTestCase {

    private static final String TEST_REGISTRATION_ID = "TEST_REGISTRATION_ID";
    private static final long TEN_SECOND_TIMEOUT = 10000L;
    private static final long NO_DELAY = 0L;
    private static final long ONE_SECOND_DELAY = 1000L;

    private MockNetworkWrapper networkWrapper;
    private MockNetworkRequestLauncher networkRequestLauncher;
    private DelayedLoop delayedLoop;
    private boolean wasRequestSuccessful;
    private Semaphore semaphore;
    private ApiRegistrarListener apiRegistrarListener;

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
            new ApiRegistrar(null);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testRequiresDeviceRegistrationId() {
        try {
            final ApiRegistrar apiRegistrar = new ApiRegistrar(new MockNetworkWrapper());
            makeApiRegistrarListener(true);
            apiRegistrar.startDeviceRegistration(null, apiRegistrarListener);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testRequiresListener() {
        try {
            final ApiRegistrar apiRegistrar = new ApiRegistrar(new MockNetworkWrapper());
            apiRegistrar.startDeviceRegistration(TEST_REGISTRATION_ID, null);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testSuccessfulRequest() {
        makeListenersForSuccessfulRequestFromNetwork(NO_DELAY, true, 200);
        final ApiRegistrar registrar = new ApiRegistrar(networkWrapper);
        registrar.startDeviceRegistration(TEST_REGISTRATION_ID, apiRegistrarListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testSuccessfulAsync() {
        makeListenersForSuccessfulRequestFromNetwork(ONE_SECOND_DELAY, true, 200);
        final ApiRegistrar registrar = new ApiRegistrar(networkWrapper);
        registrar.startDeviceRegistration(TEST_REGISTRATION_ID, apiRegistrarListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testNullResponse() {
        makeListenersForSuccessfulNullResultFromNetwork(NO_DELAY);
        final ApiRegistrar registrar = new ApiRegistrar(networkWrapper);
        registrar.startDeviceRegistration(TEST_REGISTRATION_ID, apiRegistrarListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testNoStatusLine() {
        makeListenersForSuccessfulWithNoStatusLineFromNetwork(NO_DELAY);
        final ApiRegistrar registrar = new ApiRegistrar(networkWrapper);
        registrar.startDeviceRegistration(TEST_REGISTRATION_ID, apiRegistrarListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testSuccessful404() {
        makeListenersForSuccessfulRequestFromNetwork(NO_DELAY, false, 404);
        final ApiRegistrar registrar = new ApiRegistrar(networkWrapper);
        registrar.startDeviceRegistration(TEST_REGISTRATION_ID, apiRegistrarListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testCouldNotConnect() {
        makeListenersFromFailedRequestFromNetwork(NO_DELAY, "Your server is busted", 0);
        final ApiRegistrar registrar = new ApiRegistrar(networkWrapper);
        registrar.startDeviceRegistration(TEST_REGISTRATION_ID, apiRegistrarListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    private void makeListenersForSuccessfulRequestFromNetwork(long delay, boolean isSuccessfulResult, int expectedHttpStatusCode) {
        final String resultantJson = "{}";
        final NetworkResponse response = networkRequestLauncher.getNetworkResponse(resultantJson, expectedHttpStatusCode);
        makeNetworkRequestListenerForSuccessfulRequest(delay, response);
        makeApiRegistrarListener(isSuccessfulResult);
    }

    private void makeListenersForSuccessfulNullResultFromNetwork(long delay) {
        makeNetworkRequestListenerForSuccessfulRequest(delay, null);
        makeApiRegistrarListener(false);
    }

    private void makeListenersForSuccessfulWithNoStatusLineFromNetwork(long delay) {
        final NetworkResponse response = networkRequestLauncher.getNetworkResponse(null, MockNetworkResponse.NO_STATUS_CODE);
        makeNetworkRequestListenerForSuccessfulRequest(delay, response);
        makeApiRegistrarListener(false);
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
        makeApiRegistrarListener(false);
    }

    public void makeApiRegistrarListener(final boolean isSuccessfulRequest) {
        apiRegistrarListener = new ApiRegistrarListener() {
            @Override
            public void onRegistrationSuccess() {
                assertTrue(isSuccessfulRequest);
                if (isSuccessfulRequest) {
                    delayedLoop.flagSuccess();
                } else {
                    delayedLoop.flagFailure();
                }
            }

            @Override
            public void onRegistrationFailed(String reason) {
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
