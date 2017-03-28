package io.pivotal.android.push.backend.analytics;

import android.net.Uri;
import android.test.AndroidTestCase;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import io.pivotal.android.push.PushParameters;
import io.pivotal.android.push.database.FakeAnalyticsEventsStorage;
import io.pivotal.android.push.model.analytics.AnalyticsEventTest;
import io.pivotal.android.push.prefs.FakePushPreferencesProvider;
import io.pivotal.android.push.prefs.FakePushRequestHeaders;
import io.pivotal.android.push.prefs.Pivotal;
import io.pivotal.android.push.util.DelayedLoop;
import io.pivotal.android.push.util.FakeHttpURLConnection;
import io.pivotal.android.push.util.FakeNetworkWrapper;
import io.pivotal.android.push.util.NetworkWrapper;

public class PCFPushSendAnalyticsApiRequestImplTest extends AndroidTestCase {

    private static final String TEST_PLATFORM_UUID = "TEST_PLATFORM_UUID";
    private static final String TEST_PLATFORM_SECRET = "TEST_PLATFORM_SECRET";
    private static final String TEST_DEVICE_ALIAS = "TEST_DEVICE_ALIAS";
    private static final String TEST_SERVICE_URL = "http://test.com";
    private NetworkWrapper networkWrapper;
    private PCFPushSendAnalyticsListener listener;
    private DelayedLoop delayedLoop;
    private FakeAnalyticsEventsStorage eventsStorage;
    private PushParameters parameters;
    private FakePushRequestHeaders pushRequestHeaders;
    private static final long TEN_SECOND_TIMEOUT = 10000L;

    private List<Uri> emptyList;
    private List<Uri> listWithOneItem;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        eventsStorage = new FakeAnalyticsEventsStorage();
        networkWrapper = new FakeNetworkWrapper();
        delayedLoop = new DelayedLoop(TEN_SECOND_TIMEOUT);

        parameters = new PushParameters(
                TEST_PLATFORM_UUID,
                TEST_PLATFORM_SECRET,
                TEST_SERVICE_URL,
                TEST_DEVICE_ALIAS,
                null,
                null,
                true,
                true,
                Pivotal.SslCertValidationMode.DEFAULT,
                null,
                null);

        pushRequestHeaders = new FakePushRequestHeaders();
        FakeHttpURLConnection.reset();
        emptyList = new LinkedList<>();
        listWithOneItem = new LinkedList<>();
        final Uri uri = eventsStorage.saveEvent(AnalyticsEventTest.getEvent1());
        listWithOneItem.add(uri);
    }

    public void testRequiresContext() {
        try {
            new PCFPushSendAnalyticsApiRequestImpl(null, parameters, eventsStorage, pushRequestHeaders, networkWrapper);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testRequiresEventsStorage() {
        try {
            new PCFPushSendAnalyticsApiRequestImpl(getContext(), parameters, null, pushRequestHeaders, networkWrapper);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testRequiresPushParameters() {
        try {
            new PCFPushSendAnalyticsApiRequestImpl(getContext(), null, eventsStorage, pushRequestHeaders, networkWrapper);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testRequiresNetworkWrapper() {
        try {
            new PCFPushSendAnalyticsApiRequestImpl(getContext(), parameters, eventsStorage, pushRequestHeaders, null);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testRequiresMessageReceipts() {
        try {
            final PCFPushSendAnalyticsApiRequestImpl request = new PCFPushSendAnalyticsApiRequestImpl(getContext(), parameters, eventsStorage, pushRequestHeaders, networkWrapper);
            makeBackEndMessageReceiptListener(true);
            request.startSendEvents(null, listener);
            fail("Should not have succeeded");
        } catch (Exception e) {
            // Success
        }
    }

    public void testMessageReceiptsMayNotBeEmpty() {
        try {
            final PCFPushSendAnalyticsApiRequestImpl request = new PCFPushSendAnalyticsApiRequestImpl(getContext(), parameters, eventsStorage, pushRequestHeaders, networkWrapper);
            makeBackEndMessageReceiptListener(true);
            request.startSendEvents(emptyList, listener);
            fail("Should not have succeeded");
        } catch (Exception e) {
            // Success
        }
    }

    public void testRequiresListener() {
        try {
            final PCFPushSendAnalyticsApiRequestImpl request = new PCFPushSendAnalyticsApiRequestImpl(getContext(), parameters, eventsStorage, pushRequestHeaders, networkWrapper);
            request.startSendEvents(listWithOneItem, null);
            fail("Should not have succeeded");
        } catch (Exception e) {
            // Success
        }
    }

    public void testSuccessfulRequest() {
        makeListenersForSuccessfulRequestFromNetwork(true, 200);
        final PCFPushSendAnalyticsApiRequestImpl request = new PCFPushSendAnalyticsApiRequestImpl(getContext(), parameters, eventsStorage, pushRequestHeaders, networkWrapper);
        request.startSendEvents(listWithOneItem, listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testAreAnalyticsDisabled() {
        parameters = new PushParameters(
                TEST_PLATFORM_UUID,
                TEST_PLATFORM_SECRET,
                TEST_SERVICE_URL,
                TEST_DEVICE_ALIAS,
                null,
                null,
                false,
                true,
                Pivotal.SslCertValidationMode.DEFAULT,
                null,
                null);

        try {
            final PCFPushSendAnalyticsApiRequestImpl request = new PCFPushSendAnalyticsApiRequestImpl(getContext(), parameters, eventsStorage, pushRequestHeaders, networkWrapper);
            request.startSendEvents(listWithOneItem, null);
            fail("Should not have succeeded");
        } catch (Exception e) {
            // Success
        }
    }

    public void testCouldNotConnect() {
        makeListenersFromFailedRequestFromNetwork("Your server is busted", 0);
        final PCFPushSendAnalyticsApiRequestImpl request = new PCFPushSendAnalyticsApiRequestImpl(getContext(), parameters, eventsStorage, pushRequestHeaders, networkWrapper);
        request.startSendEvents(listWithOneItem, listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testSuccessful400() {
        makeListenersForSuccessfulRequestFromNetwork(false, 400);
        final PCFPushSendAnalyticsApiRequestImpl request = new PCFPushSendAnalyticsApiRequestImpl(getContext(), parameters, eventsStorage, pushRequestHeaders, networkWrapper);
        request.startSendEvents(listWithOneItem, listener);
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
        makeBackEndMessageReceiptListener(false);
    }

    private void makeListenersForSuccessfulRequestFromNetwork(boolean isSuccessful, int expectedHttpStatusCode) {
        FakeHttpURLConnection.setResponseCode(expectedHttpStatusCode);
        makeBackEndMessageReceiptListener(isSuccessful);
    }

    private void makeBackEndMessageReceiptListener(final boolean isSuccessfulRequest) {
        listener = new PCFPushSendAnalyticsListener() {

            @Override
            public void onBackEndSendEventsSuccess() {
                assertTrue(isSuccessfulRequest);
                assertTrue(FakeHttpURLConnection.getRequestPropertiesMap().containsKey("Authorization"));
                assertEquals("application/json", FakeHttpURLConnection.getRequestPropertiesMap().get("Content-Type"));
                if (isSuccessfulRequest) {
                    delayedLoop.flagSuccess();
                } else {
                    delayedLoop.flagFailure();
                }
            }

            @Override
            public void onBackEndSendEventsFailed(String reason) {
                assertFalse(isSuccessfulRequest);
                assertTrue(FakeHttpURLConnection.getRequestPropertiesMap().containsKey("Authorization"));
                assertEquals("application/json", FakeHttpURLConnection.getRequestPropertiesMap().get("Content-Type"));
                if (isSuccessfulRequest) {
                    delayedLoop.flagFailure();
                } else {
                    delayedLoop.flagSuccess();
                }
            }
        };
    }
}