/* Copyright (c) 2013 Pivotal Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
