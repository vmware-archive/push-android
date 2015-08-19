package io.pivotal.android.push.backend.analytics;

import android.test.AndroidTestCase;

import java.io.IOException;

import io.pivotal.android.push.PushParameters;
import io.pivotal.android.push.prefs.FakePushPreferencesProvider;
import io.pivotal.android.push.prefs.Pivotal;
import io.pivotal.android.push.util.DelayedLoop;
import io.pivotal.android.push.util.FakeHttpURLConnection;
import io.pivotal.android.push.util.FakeNetworkWrapper;
import io.pivotal.android.push.util.NetworkWrapper;
import io.pivotal.android.push.version.Version;

public class PCFPushCheckBackEndVersionApiRequestImplTest extends AndroidTestCase {

    public enum RequestResult {
        SUCCESS,
        OLD,
        RETRYABLE_FAILURE,
        FATAL_FAILURE
    }

    private static final String TEST_SERVICE_URL = "http://test.server.url";
    private NetworkWrapper networkWrapper;
    private PCFPushCheckBackEndVersionListener listener;
    private DelayedLoop delayedLoop;
    private FakePushPreferencesProvider preferencesProvider;
    private static final long TEN_SECOND_TIMEOUT = 10000L;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        networkWrapper = new FakeNetworkWrapper();
        delayedLoop = new DelayedLoop(TEN_SECOND_TIMEOUT);
        preferencesProvider = new FakePushPreferencesProvider(null, null, 0, null, null, null, null, null, TEST_SERVICE_URL, null, 0, true);
        FakeHttpURLConnection.reset();
    }

    public void testRequiresContext() {
        try {
            new PCFPushCheckBackEndVersionApiRequestImpl(null, preferencesProvider, networkWrapper);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testRequiresNetworkWrapper() {
        try {
            new PCFPushCheckBackEndVersionApiRequestImpl(getContext(), preferencesProvider, null);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testRequiresListener() {
        try {
            final PCFPushCheckBackEndVersionApiRequestImpl request = new PCFPushCheckBackEndVersionApiRequestImpl(getContext(), preferencesProvider, networkWrapper);
            request.startCheckBackEndVersion(null);
            fail("Should not have succeeded");
        } catch (Exception e) {
            // Success
        }
    }

    public void testSuccessfulRequest() {
        makeListenersForSuccessfulRequestFromNetwork(RequestResult.SUCCESS, 200, "1.3.3.7");
        final PCFPushCheckBackEndVersionApiRequestImpl request = new PCFPushCheckBackEndVersionApiRequestImpl(getContext(), preferencesProvider, networkWrapper);
        request.startCheckBackEndVersion(listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testNullResponse() {
        makeListenersForSuccessfulRequestFromNetwork(RequestResult.RETRYABLE_FAILURE, 204, null);
        final PCFPushCheckBackEndVersionApiRequestImpl request = new PCFPushCheckBackEndVersionApiRequestImpl(getContext(), preferencesProvider, networkWrapper);
        request.startCheckBackEndVersion(listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testCouldNotConnect() {
        makeListenersFromFailedRequestFromNetwork("Your server is busted", 0);
        final PCFPushCheckBackEndVersionApiRequestImpl request = new PCFPushCheckBackEndVersionApiRequestImpl(getContext(), preferencesProvider, networkWrapper);
        request.startCheckBackEndVersion(listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testSuccessful400() {
        makeListenersForSuccessfulRequestFromNetwork(RequestResult.FATAL_FAILURE, 400, null);
        final PCFPushCheckBackEndVersionApiRequestImpl request = new PCFPushCheckBackEndVersionApiRequestImpl(getContext(), preferencesProvider, networkWrapper);
        request.startCheckBackEndVersion(listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testSuccessful500() {
        makeListenersForSuccessfulRequestFromNetwork(RequestResult.RETRYABLE_FAILURE, 500, null);
        final PCFPushCheckBackEndVersionApiRequestImpl request = new PCFPushCheckBackEndVersionApiRequestImpl(getContext(), preferencesProvider, networkWrapper);
        request.startCheckBackEndVersion(listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testSuccessfulOldVersion() {
        makeListenersForSuccessfulRequestFromNetwork(RequestResult.OLD, 404, null);
        final PCFPushCheckBackEndVersionApiRequestImpl request = new PCFPushCheckBackEndVersionApiRequestImpl(getContext(), preferencesProvider, networkWrapper);
        request.startCheckBackEndVersion(listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    private void makeListenersFromFailedRequestFromNetwork(String exceptionText, int expectedHttpStatusCode) {
        IOException exception = null;
        if (exceptionText != null) {
            exception = new IOException(exceptionText);
        }
        FakeHttpURLConnection.setConnectionException(exception);
        FakeHttpURLConnection.willThrowConnectionException(true);
        FakeHttpURLConnection.setResponseCode(expectedHttpStatusCode);
        makeCheckBackEndVersionListener(RequestResult.RETRYABLE_FAILURE, null);
    }

    private void makeListenersForSuccessfulRequestFromNetwork(RequestResult requestResult, int expectedHttpStatusCode, String expectedVersion) {
        FakeHttpURLConnection.setResponseCode(expectedHttpStatusCode);
        if (expectedVersion != null) {
            FakeHttpURLConnection.setResponseData("{\"version\":\"" + expectedVersion + "\"}");
            makeCheckBackEndVersionListener(requestResult, new Version(expectedVersion));
        } else {
            FakeHttpURLConnection.setResponseData("File not found. Oops.");
            makeCheckBackEndVersionListener(requestResult, null);
        }
    }

    private void makeCheckBackEndVersionListener(final RequestResult requestResult, final Version expectedVersion) {
        listener = new PCFPushCheckBackEndVersionListener() {

            @Override
            public void onCheckBackEndVersionSuccess(Version version) {
                assertFalse(FakeHttpURLConnection.getRequestPropertiesMap().containsKey("Authorization"));
                assertEquals(expectedVersion, version);
                if (requestResult == RequestResult.SUCCESS) {
                    delayedLoop.flagSuccess();
                } else {
                    fail("expected a SUCCESS result");
                    delayedLoop.flagFailure();
                }
            }

            @Override
            public void onCheckBackEndVersionIsOldVersion() {
                assertFalse(FakeHttpURLConnection.getRequestPropertiesMap().containsKey("Authorization"));
                if (requestResult == RequestResult.OLD) {
                    delayedLoop.flagSuccess();
                } else {
                    fail("expected an OLD result");
                    delayedLoop.flagFailure();
                }
            }

            @Override
            public void onCheckBackEndVersionRetryableFailure(String reason) {
                assertFalse(FakeHttpURLConnection.getRequestPropertiesMap().containsKey("Authorization"));
                if (requestResult == RequestResult.RETRYABLE_FAILURE) {
                    delayedLoop.flagSuccess();
                } else {
                    fail("expected a RETRYABLE_FAILURE result");
                    delayedLoop.flagFailure();
                }
            }

            @Override
            public void onCheckBackEndVersionFatalFailure(String reason) {
                assertFalse(FakeHttpURLConnection.getRequestPropertiesMap().containsKey("Authorization"));
                if (requestResult == RequestResult.FATAL_FAILURE) {
                    delayedLoop.flagSuccess();
                } else {
                    fail("expected a FATAL_FAILURE result");
                    delayedLoop.flagFailure();
                }
            }
        };
    }

    private PushParameters getParameters() {
        return new PushParameters(null, null, null, TEST_SERVICE_URL, null, null, false, Pivotal.SslCertValidationMode.DEFAULT, null, null);
    }
}