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

package org.omnia.pushsdk.backend;
import org.omnia.pushsdk.network.NetworkWrapper;
import org.omnia.pushsdk.util.Const;
import org.omnia.pushsdk.util.PushLibLogger;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * API request for unregistering a device from the Omnia Mobile Services back-end server.
 */
public class BackEndUnregisterDeviceApiRequestImpl implements BackEndUnregisterDeviceApiRequest {

    private NetworkWrapper networkWrapper;

    public BackEndUnregisterDeviceApiRequestImpl(NetworkWrapper networkWrapper) {
        verifyArguments(networkWrapper);
        saveArguments(networkWrapper);
    }

    private void verifyArguments(NetworkWrapper networkWrapper) {
        if (networkWrapper == null) {
            throw new IllegalArgumentException("networkWrapper may not be null");
        }
    }

    private void saveArguments(NetworkWrapper networkWrapper) {
        this.networkWrapper = networkWrapper;
    }

    @Override
    public void startUnregisterDevice(String backEndDeviceRegistrationId, BackEndUnregisterDeviceListener listener) {

        verifyUnregistrationArguments(backEndDeviceRegistrationId, listener);

        try {
            PushLibLogger.v("Making network request to the back-end server to unregister the device ID:" + backEndDeviceRegistrationId);
            final URL url = new URL(Const.BACKEND_REGISTRATION_REQUEST_URL + "/" + backEndDeviceRegistrationId);
            final HttpURLConnection urlConnection = networkWrapper.getHttpURLConnection(url);
            urlConnection.setRequestMethod("DELETE");
            urlConnection.setReadTimeout(60000);
            urlConnection.setConnectTimeout(60000);
            urlConnection.setChunkedStreamingMode(0);
            urlConnection.connect();

            final int statusCode = urlConnection.getResponseCode();

            onSuccessfulRequest(statusCode, listener);

        } catch (Exception e) {
            PushLibLogger.ex("Back-end device unregistration attempt failed", e);
            listener.onBackEndUnregisterDeviceFailed(e.getLocalizedMessage());
        }
    }

    private void verifyUnregistrationArguments(String backEndDeviceRegistrationId, BackEndUnregisterDeviceListener listener) {
        if (backEndDeviceRegistrationId == null) {
            throw new IllegalArgumentException("backEndDeviceRegistrationId may not be null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("listener may not be null");
        }
    }

    public void onSuccessfulRequest(int statusCode, BackEndUnregisterDeviceListener listener) {

        if (isFailureStatusCode(statusCode)) {
            PushLibLogger.e("Back-end server unregistration failed: server returned HTTP status " + statusCode);
            listener.onBackEndUnregisterDeviceFailed("Back-end server returned HTTP status " + statusCode);
            return;
        }

        PushLibLogger.i("Back-end Server device unregistration succeeded.");
        listener.onBackEndUnregisterDeviceSuccess();
    }

    private boolean isFailureStatusCode(int statusCode) {
        return (statusCode < 200 || statusCode >= 300);
    }

    @Override
    public BackEndUnregisterDeviceApiRequest copy() {
        return new BackEndUnregisterDeviceApiRequestImpl(networkWrapper);
    }
}
