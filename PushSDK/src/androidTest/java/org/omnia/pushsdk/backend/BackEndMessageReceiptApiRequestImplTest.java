package org.omnia.pushsdk.backend;

import android.test.AndroidTestCase;

import org.omnia.pushsdk.network.MockHttpURLConnection;
import org.omnia.pushsdk.network.MockNetworkWrapper;
import org.omnia.pushsdk.network.NetworkWrapper;
import org.omnia.pushsdk.sample.model.MessageReceiptData;
import org.omnia.pushsdk.sample.model.MessageReceiptDataTest;
import org.omnia.pushsdk.sample.util.DelayedLoop;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class BackEndMessageReceiptApiRequestImplTest extends AndroidTestCase {

    private static final String TEST_MESSAGE_UUID = "TEST-MESSAGE-UUID";
    private NetworkWrapper networkWrapper;
    private BackEndMessageReceiptListener backEndMessageReceiptListener;
    private DelayedLoop delayedLoop;
    private static final long TEN_SECOND_TIMEOUT = 10000L;

    private List<MessageReceiptData> emptyList;
    private List<MessageReceiptData> listWithOneItem;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        networkWrapper = new MockNetworkWrapper();
        delayedLoop = new DelayedLoop(TEN_SECOND_TIMEOUT);
        MockHttpURLConnection.reset();
        emptyList = new LinkedList<MessageReceiptData>();
        listWithOneItem = new LinkedList<MessageReceiptData>();
        listWithOneItem.add(MessageReceiptDataTest.getMessageReceiptData1());
    }

    public void testRequiresNetworkWrapper() {
        try {
            new BackEndMessageReceiptApiRequestImpl(null);
            fail("Should not have succeeded");
        } catch (IllegalArgumentException ex) {
            // Success
        }
    }

    public void testRequiresMessageReceipts() {
        try {
            final BackEndMessageReceiptApiRequestImpl request = new BackEndMessageReceiptApiRequestImpl(networkWrapper);
            makeBackEndMessageReceiptListener(true);
            request.startMessageReceipt(null, backEndMessageReceiptListener);
            fail("Should not have succeeded");
        } catch (Exception e) {
            // Success
        }
    }

    public void testMessageReceiptsMayNotBeEmpty() {
        try {
            final BackEndMessageReceiptApiRequestImpl request = new BackEndMessageReceiptApiRequestImpl(networkWrapper);
            makeBackEndMessageReceiptListener(true);
            request.startMessageReceipt(emptyList, backEndMessageReceiptListener);
            fail("Should not have succeeded");
        } catch (Exception e) {
            // Success
        }
    }

    public void testRequiresListener() {
        try {
            final BackEndMessageReceiptApiRequestImpl request = new BackEndMessageReceiptApiRequestImpl(networkWrapper);
            request.startMessageReceipt(listWithOneItem, null);
            fail("Should not have succeeded");
        } catch (Exception e) {
            // Success
        }
    }

    public void testSuccessfulRequest() {
        makeListenersForSuccessfulRequestFromNetwork(true, 200);
        final BackEndMessageReceiptApiRequestImpl request = new BackEndMessageReceiptApiRequestImpl(networkWrapper);
        request.startMessageReceipt(listWithOneItem, backEndMessageReceiptListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testCouldNotConnect() {
        makeListenersFromFailedRequestFromNetwork("Your server is busted", 0);
        final BackEndMessageReceiptApiRequestImpl request = new BackEndMessageReceiptApiRequestImpl(networkWrapper);
        request.startMessageReceipt(listWithOneItem, backEndMessageReceiptListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    public void testSuccessful404() {
        makeListenersForSuccessfulRequestFromNetwork(false, 404);
        final BackEndMessageReceiptApiRequestImpl request = new BackEndMessageReceiptApiRequestImpl(networkWrapper);
        request.startMessageReceipt(listWithOneItem, backEndMessageReceiptListener);
        delayedLoop.startLoop();
        assertTrue(delayedLoop.isSuccess());
    }

    private void makeListenersFromFailedRequestFromNetwork(String exceptionText, int expectedHttpStatusCode) {
        IOException exception = null;
        if (exceptionText != null) {
            exception = new IOException(exceptionText);
        }
        MockHttpURLConnection.setConnectionException(exception);
        MockHttpURLConnection.willThrowConnectionException(true);
        MockHttpURLConnection.setResponseCode(expectedHttpStatusCode);
        makeBackEndMessageReceiptListener(false);
    }

    private void makeListenersForSuccessfulRequestFromNetwork(boolean isSuccessful, int expectedHttpStatusCode) {
        MockHttpURLConnection.setResponseCode(expectedHttpStatusCode);
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