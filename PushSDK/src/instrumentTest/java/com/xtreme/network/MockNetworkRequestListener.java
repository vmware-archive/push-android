package com.xtreme.network;

/**
 * When making a {@link NetworkRequest} you must specify this listener for ASYNCHRONOUS requests.
 * Once the request has been processed there are 2 possible responses
 * {@link #onSuccess(NetworkRequest, NetworkResponse)} and {@link #onFailure(NetworkRequest, NetworkError)}.
 */
public interface MockNetworkRequestListener {

    /**
     * This is the response you receive once the {@link NetworkRequest} has been SUCCESSFULLY executed.
     *
     * @param networkRequest         The {@link NetworkRequest} that generated this callback
     * @param networkRequestResponse The {@link NetworkResponse} that wraps the input stream and the status.
     */
    public void onSuccess(NetworkRequest networkRequest, NetworkResponse networkRequestResponse);

    /**
     * This is the response you receive once the {@link NetworkRequest} has been UNSUCCESSFULLY executed.
     *
     * @param networkRequest The {@link NetworkRequest} that generated this callback
     * @param networkError   The {@link NetworkError} that wraps the exception and the status encountered while processing the request.
     */
    public void onFailure(NetworkRequest networkRequest, NetworkError networkError);
}
