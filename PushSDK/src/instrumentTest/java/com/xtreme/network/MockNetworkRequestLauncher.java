package com.xtreme.network;

import android.os.AsyncTask;

import com.xtreme.commons.ThreadUtil;

import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This mock class is used in unit tests to isolate the crash handler program from the network and to make the results
 * of network requests deterministic.
 * <p/>
 * Prior to using either the executeRequest or executeRequestSimultaneously methods to execute a {@link com.xtreme.network.NetworkRequest},
 * preselect your desired outcome by providing the result, error, or exception with the setNetworkRequest method.
 *
 * @author rob
 */
public class MockNetworkRequestLauncher implements INetworkRequestLauncher {

    private MockNetworkRequestListener _mockListener;
    private long _delay;
    private NetworkResponse _networkResponse;
    private NetworkError _networkError;
    private IOException _ioException;
    private boolean _isSuccessfulRequest;

    private int numberRequestsProcessed = 0;

    private class FailedInputStream extends InputStream {

        @Override
        public int read() throws IOException {
            throw new IOException("THIS INPUT STREAM IS SUPPOSED TO FAIL");
        }
    }

    /**
     * Gets the number of requests that have been processed.
     */
    public int getNumberRequestsProcessed() {
        return numberRequestsProcessed;
    }

    /**
     * Resets the number of processed requests to zero.
     */
    public void resetNumberRequestsProcessed() {
        numberRequestsProcessed = 0;
    }

    /**
     * Selects a 'successful' outcome for all future network requests executed by this mock launcher.
     *
     * @param mockListener    Callbacks to indicate request or failure
     * @param networkResponse The success response to pass to the request callback
     * @param delay           The delay, in milliseconds, before the response is called. Use a delay of zero if the response should
     *                        be immediate.
     */
    public void setNextRequestResolution(MockNetworkRequestListener mockListener, NetworkResponse networkResponse, long delay) {
        _mockListener = mockListener;
        _networkResponse = networkResponse;
        _networkError = null;
        _ioException = null;
        _delay = delay;
        _isSuccessfulRequest = true;
    }

    /**
     * Selects a 'failure' outcome for all future network requests executed by this mock launcher.
     *
     * @param mockListener Callbacks to indicate request or failure
     * @param networkError An error response to pass to the request callback
     * @param delay        The delay, in milliseconds, before the response is called. Use a delay of zero if the response should
     *                     be immediate.
     */
    public void setNextRequestResolution(MockNetworkRequestListener mockListener, NetworkError networkError, long delay) {
        _mockListener = mockListener;
        _networkResponse = null;
        _networkError = networkError;
        _ioException = null;
        _delay = delay;
        _isSuccessfulRequest = false;
    }

    /**
     * Selects a 'failure' (via a thrown exception) outcome for all future network requests executed by this mock
     * launcher.
     *
     * @param mockListener Callbacks to indicate request or failure
     * @param ioException  An exception that will be thrown when a request is launched
     * @param delay        The delay, in milliseconds, before the response is called. Use a delay of zero if the response should
     *                     be immediate.
     */
    public void setNextRequestResolution(MockNetworkRequestListener mockListener, IOException ioException, long delay) {
        _mockListener = mockListener;
        _networkResponse = null;
        _networkError = null;
        _ioException = ioException;
        _delay = delay;
        _isSuccessfulRequest = false;
    }

    /**
     * Fakely executes a network request. i.e.: does not actually execute the response on the network, but in a
     * deterministic manner by using the outcome provided by the most recent call to one of the
     * setNetworkRequestResolution methods.
     */
    @Override
    public void executeRequest(final NetworkRequest networkRequest) {

        numberRequestsProcessed += 1;

        if (_delay == 0) {
            if (_isSuccessfulRequest) {
                networkRequest.onSuccess(_networkResponse);
                _mockListener.onSuccess(networkRequest, _networkResponse);
            } else {
                networkRequest.onFailure(_networkError);
                _mockListener.onFailure(networkRequest, _networkError);
            }
        } else if (_delay > 0) {

            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... notUsed) {

                    ThreadUtil.sleep(_delay);

                    if (_isSuccessfulRequest) {
                        networkRequest.onSuccess(_networkResponse);
                        _mockListener.onSuccess(networkRequest, _networkResponse);
                    } else {
                        networkRequest.onFailure(_networkError);
                        _mockListener.onFailure(networkRequest, _networkError);
                    }

                    return null;
                }

            }.execute();
        }
    }

    /**
     * Fakely executes a network request. i.e.: does not actually execute the response on the network, but in a
     * deterministic manner by using the outcome provided by the most recent call to one of the
     * setNetworkRequestResolution methods.
     * <p/>
     * The response is always returned right away, rather than using whatever delay was specified by the most resent
     * call to one of the setNetworkRequestResolution methods.
     */
    @Override
    public NetworkResponse executeRequestSynchronously(NetworkRequest networkRequest) throws ClientProtocolException, IOException {

        numberRequestsProcessed += 1;

        if (_isSuccessfulRequest) {
            if (_mockListener != null) {
                _mockListener.onSuccess(networkRequest, _networkResponse);
            }
            return _networkResponse;
        } else {
            if (_mockListener != null) {
                _mockListener.onFailure(networkRequest, _networkError);
            }
            throw _ioException;
        }
    }

    /**
     * Does nothing.
     */
    @Override
    public void cancelRequest(NetworkRequest request) {
    }

    /**
     * Factory method for manufacturing NetworkResponse objects.
     *
     * @param responseData A string that can be returned via the NetworkResponse object's InputStream
     * @param statusCode   The HTTP status code that will be returned by the the StatusLine in the NetworkResponse.
     * @return A shiny new NetworkResponse object.
     */
    public NetworkResponse getNetworkResponse(final String responseData, final int statusCode) {

        InputStream inputStream = null;
        if (responseData != null) {
            inputStream = new ByteArrayInputStream(responseData.getBytes());
        } else {
            inputStream = new FailedInputStream();
        }

        return new MockNetworkResponse(inputStream, statusCode);
    }

    /**
     * Factory method for manufacturing NetworkError objects.
     *
     * @param exception    An exception to include in the NetworkError object.
     * @param responseCode An HTTP status code to include in the NetworkError's StatusLine object.
     * @return A fancy new NetworkError object.
     */
    public NetworkError getNetworkError(final Exception exception, final int responseCode) {

        return new NetworkError(new StatusLine() {

            @Override
            public int getStatusCode() {
                return responseCode;
            }

            @Override
            public String getReasonPhrase() {
                return null;
            }

            @Override
            public ProtocolVersion getProtocolVersion() {
                return null;
            }
        }, exception);
    }
}
