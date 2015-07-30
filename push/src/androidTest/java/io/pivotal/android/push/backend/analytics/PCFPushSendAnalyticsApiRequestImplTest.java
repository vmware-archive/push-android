package io.pivotal.android.push.backend.analytics;

import android.net.Uri;
import android.test.AndroidTestCase;

import java.util.LinkedList;
import java.util.List;

import io.pivotal.android.push.PushParameters;
import io.pivotal.android.push.database.FakeEventsStorage;
import io.pivotal.android.push.model.analytics.EventTest;
import io.pivotal.android.push.prefs.Pivotal;
import io.pivotal.android.push.util.DelayedLoop;
import io.pivotal.android.push.util.FakeHttpURLConnection;
import io.pivotal.android.push.util.FakeNetworkWrapper;
import io.pivotal.android.push.util.NetworkWrapper;

public class PCFPushSendAnalyticsApiRequestImplTest extends AndroidTestCase {

    private static final String TEST_SENDER_ID = "TEST_SENDER_ID";
    private static final String TEST_PLATFORM_UUID = "TEST_PLATFORM_UUID";
    private static final String TEST_PLATFORM_SECRET = "TEST_PLATFORM_SECRET";
    private static final String TEST_DEVICE_ALIAS = "TEST_DEVICE_ALIAS";
    private static final String TEST_SERVICE_URL = "http://test.com";
    private NetworkWrapper networkWrapper;
    private PCFPushSendAnalyticsListener listener;
    private DelayedLoop delayedLoop;
    private FakeEventsStorage eventsStorage;
    private PushParameters parameters;
    private static final long TEN_SECOND_TIMEOUT = 10000L;

    private List<Uri> emptyList;
    private List<Uri> listWithOneItem;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        eventsStorage = new FakeEventsStorage();
        networkWrapper = new FakeNetworkWrapper();
        delayedLoop = new DelayedLoop(TEN_SECOND_TIMEOUT);
        parameters = getParameters(true);
        FakeHttpURLConnection.reset();
        emptyList = new LinkedList<Uri>();
        listWithOneItem = new LinkedList<Uri>();
        final Uri uri = eventsStorage.saveEvent(EventTest.getEvent1());
        listWithOneItem.add(uri);
    }

    public void testRequiresContext() {
        try {
            new PCFPushSendAnalyticsApiRequestImpl(null, eventsStorage, parameters, networkWrapper);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testRequiresEventsStorage() {
        try {
            new PCFPushSendAnalyticsApiRequestImpl(getContext(), null, parameters, networkWrapper);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testRequiresPreferencesProvider() {
        try {
            new PCFPushSendAnalyticsApiRequestImpl(getContext(), eventsStorage, null, networkWrapper);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testRequiresNetworkWrapper() {
        try {
            new PCFPushSendAnalyticsApiRequestImpl(getContext(), eventsStorage, parameters, null);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testRequiresMessageReceipts() {
        try {
            final PCFPushSendAnalyticsApiRequestImpl request = new PCFPushSendAnalyticsApiRequestImpl(getContext(), eventsStorage, parameters, networkWrapper);
            makeBackEndMessageReceiptListener(true);
            request.startSendEvents(null, parameters, listener);
            fail("Should not have succeeded");
        } catch (Exception e) {
            // Success
        }
    }

    public void testMessageReceiptsMayNotBeEmpty() {
        try {
            final PCFPushSendAnalyticsApiRequestImpl request = new PCFPushSendAnalyticsApiRequestImpl(getContext(), eventsStorage, parameters, networkWrapper);
            makeBackEndMessageReceiptListener(true);
            request.startSendEvents(emptyList, parameters, listener);
            fail("Should not have succeeded");
        } catch (Exception e) {
            // Success
        }
    }

    public void testRequiresListener() {
        try {
            final PCFPushSendAnalyticsApiRequestImpl request = new PCFPushSendAnalyticsApiRequestImpl(getContext(), eventsStorage, parameters, networkWrapper);
            request.startSendEvents(listWithOneItem, parameters, null);
            fail("Should not have succeeded");
        } catch (Exception e) {
            // Success
        }
    }

    public void testSuccessfulRequest() {
        makeListenersForSuccessfulRequestFromNetwork(true, 200);
        final PCFPushSendAnalyticsApiRequestImpl request = new PCFPushSendAnalyticsApiRequestImpl(getContext(), eventsStorage, parameters, networkWrapper);
        request.startSendEvents(listWithOneItem, parameters, listener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    // TODO - restore test
//    public void testCouldNotConnect() {
//        makeListenersFromFailedRequestFromNetwork("Your server is busted", 0);
//        final BackEndMessageReceiptApiRequestImpl request = new BackEndMessageReceiptApiRequestImpl(networkWrapper);
//        request.startSendEvents(listWithOneItem, backEndMessageReceiptListener);
//        delayedLoop.startLoop();
//        assertTrue(delayedLoop.isSuccess());
//    }

    // TODO - restore test
//    public void testSuccessful400() {
//        makeListenersForSuccessfulRequestFromNetwork(false, 400);
//        final BackEndMessageReceiptApiRequestImpl request = new BackEndMessageReceiptApiRequestImpl(networkWrapper);
//        request.startSendEvents(listWithOneItem, backEndMessageReceiptListener);
//        delayedLoop.startLoop();
//        assertTrue(delayedLoop.isSuccess());
//    }

//    private void makeListenersFromFailedRequestFromNetwork(String exceptionText, int expectedHttpStatusCode) {
//        IOException exception = null;
//        if (exceptionText != null) {
//            exception = new IOException(exceptionText);
//        }
//        FakeHttpURLConnection.setConnectionException(exception);
//        FakeHttpURLConnection.willThrowConnectionException(true);
//        FakeHttpURLConnection.setResponseCode(expectedHttpStatusCode);
//        makeBackEndMessageReceiptListener(false);
//    }

    private void makeListenersForSuccessfulRequestFromNetwork(boolean isSuccessful, int expectedHttpStatusCode) {
        FakeHttpURLConnection.setResponseCode(expectedHttpStatusCode);
        makeBackEndMessageReceiptListener(isSuccessful);
    }

    private void makeBackEndMessageReceiptListener(final boolean isSuccessfulRequest) {
        listener = new PCFPushSendAnalyticsListener() {

            @Override
            public void onBackEndSendEventsSuccess() {
                assertTrue(isSuccessfulRequest);
                if (isSuccessfulRequest) {
                    delayedLoop.flagSuccess();
                } else {
                    delayedLoop.flagFailure();
                }
            }

            @Override
            public void onBackEndSendEventsFailed(String reason) {
                assertFalse(isSuccessfulRequest);
                if (isSuccessfulRequest) {
                    delayedLoop.flagFailure();
                } else {
                    delayedLoop.flagSuccess();
                }
            }
        };
    }

    private PushParameters getParameters(boolean areAnalyticsEnabled) {
        return new PushParameters(TEST_SENDER_ID, TEST_PLATFORM_UUID, TEST_PLATFORM_SECRET, TEST_SERVICE_URL, TEST_DEVICE_ALIAS, null, true, Pivotal.SslCertValidationMode.DEFAULT, null, null, areAnalyticsEnabled);
    }
}