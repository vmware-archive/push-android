package com.xtreme.network;

/**
 * When making a {@link com.xtreme.network.NetworkRequest} you must specify this listener for ASYNCHRONOUS requests.
 * Once the request has been processed there are 2 possible responses
 * {@link #onSuccess(com.xtreme.network.NetworkRequest, com.xtreme.network.NetworkResponse)} and {@link #onFailure(com.xtreme.network.NetworkRequest, com.xtreme.network.NetworkError)}.
 */
public interface MockNetworkRequestListener {

    /**
     * This is the response you receive once the {@link com.xtreme.network.NetworkRequest} has been SUCCESSFULLY executed.
     *
     * @param networkRequest         The {@link com.xtreme.network.NetworkRequest} that generated this callback
     * @param networkRequestResponse The {@link com.xtreme.network.NetworkResponse} that wraps the input stream and the status.
     */
    public void onSuccess(NetworkRequest networkRequest, NetworkResponse networkRequestResponse);

    /**
     * This is the response you receive once the {@link NetworkRequest} has been UNSUCCESSFULLY executed.
     *
     * @param networkRequest The {@link NetworkRequest} that generated this callback
     * @param networkError   The {@link com.xtreme.network.NetworkError} that wraps the exception and the status encountered while processing the request.
     */
    public void onFailure(NetworkRequest networkRequest, NetworkError networkError);
}
