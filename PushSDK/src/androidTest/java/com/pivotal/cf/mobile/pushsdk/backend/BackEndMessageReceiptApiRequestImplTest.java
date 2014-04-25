package com.pivotal.cf.mobile.pushsdk.backend;

import android.net.Uri;
import android.test.AndroidTestCase;

import com.pivotal.cf.mobile.pushsdk.database.FakeEventsStorage;
import com.pivotal.cf.mobile.pushsdk.model.BaseEventTest;
import com.pivotal.cf.mobile.pushsdk.network.FakeHttpURLConnection;
import com.pivotal.cf.mobile.pushsdk.network.FakeNetworkWrapper;
import com.pivotal.cf.mobile.pushsdk.network.NetworkWrapper;
import com.pivotal.cf.mobile.pushsdk.util.DelayedLoop;

import java.util.LinkedList;
import java.util.List;

public class BackEndMessageReceiptApiRequestImplTest extends AndroidTestCase {

    private static final String TEST_MESSAGE_UUID = "TEST-MESSAGE-UUID";
    private NetworkWrapper networkWrapper;
    private BackEndMessageReceiptListener backEndMessageReceiptListener;
    private DelayedLoop delayedLoop;
    private FakeEventsStorage eventsStorage;
    private static final long TEN_SECOND_TIMEOUT = 10000L;

    private List<Uri> emptyList;
    private List<Uri> listWithOneItem;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        eventsStorage = new FakeEventsStorage();
        networkWrapper = new FakeNetworkWrapper();
        delayedLoop = new DelayedLoop(TEN_SECOND_TIMEOUT);
        FakeHttpURLConnection.reset();
        emptyList = new LinkedList<Uri>();
        listWithOneItem = new LinkedList<Uri>();
        final Uri uri = eventsStorage.saveEvent(BaseEventTest.getBaseEvent1());
        listWithOneItem.add(uri);
    }

    public void testRequiresContext() {
        try {
            new BackEndMessageReceiptApiRequestImpl(null, eventsStorage, networkWrapper);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testRequiresEventsStorage() {
        try {
            new BackEndMessageReceiptApiRequestImpl(getContext(), null, networkWrapper);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testRequiresNetworkWrapper() {
        try {
            new BackEndMessageReceiptApiRequestImpl(getContext(), eventsStorage, null);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testRequiresMessageReceipts() {
        try {
            final BackEndMessageReceiptApiRequestImpl request = new BackEndMessageReceiptApiRequestImpl(getContext(), eventsStorage, networkWrapper);
            makeBackEndMessageReceiptListener(true);
            request.startSendMessageReceipts(null, backEndMessageReceiptListener);
            fail("Should not have succeeded");
        } catch (Exception e) {
            // Success
        }
    }

    public void testMessageReceiptsMayNotBeEmpty() {
        try {
            final BackEndMessageReceiptApiRequestImpl request = new BackEndMessageReceiptApiRequestImpl(getContext(), eventsStorage, networkWrapper);
            makeBackEndMessageReceiptListener(true);
            request.startSendMessageReceipts(emptyList, backEndMessageReceiptListener);
            fail("Should not have succeeded");
        } catch (Exception e) {
            // Success
        }
    }

    public void testRequiresListener() {
        try {
            final BackEndMessageReceiptApiRequestImpl request = new BackEndMessageReceiptApiRequestImpl(getContext(), eventsStorage, networkWrapper);
            request.startSendMessageReceipts(listWithOneItem, null);
            fail("Should not have succeeded");
        } catch (Exception e) {
            // Success
        }
    }

    public void testSuccessfulRequest() {
        makeListenersForSuccessfulRequestFromNetwork(true, 200);
        final BackEndMessageReceiptApiRequestImpl request = new BackEndMessageReceiptApiRequestImpl(getContext(), eventsStorage, networkWrapper);
        request.startSendMessageReceipts(listWithOneItem, backEndMessageReceiptListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    // TODO - restore test
//    public void testCouldNotConnect() {
//        makeListenersFromFailedRequestFromNetwork("Your server is busted", 0);
//        final BackEndMessageReceiptApiRequestImpl request = new BackEndMessageReceiptApiRequestImpl(networkWrapper);
//        request.startSendMessageReceipts(listWithOneItem, backEndMessageReceiptListener);
//        delayedLoop.startLoop();
//        assertTrue(delayedLoop.isSuccess());
//    }

    // TODO - restore test
//    public void testSuccessful400() {
//        makeListenersForSuccessfulRequestFromNetwork(false, 400);
//        final BackEndMessageReceiptApiRequestImpl request = new BackEndMessageReceiptApiRequestImpl(networkWrapper);
//        request.startSendMessageReceipts(listWithOneItem, backEndMessageReceiptListener);
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
        backEndMessageReceiptListener = new BackEndMessageReceiptListener() {

            @Override
            public void onBackEndMessageReceiptSuccess() {
                assertTrue(isSuccessfulRequest);
                if (isSuccessfulRequest) {
                    delayedLoop.flagSuccess();
                } else {
                    delayedLoop.flagFailure();
                }
            }

            @Override
            public void onBackEndMessageReceiptFailed(String reason) {
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