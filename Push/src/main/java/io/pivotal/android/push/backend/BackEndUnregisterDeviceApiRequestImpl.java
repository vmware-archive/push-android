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

package io.pivotal.android.push.backend;

import java.net.HttpURLConnection;
import java.net.URL;

import io.pivotal.android.push.RegistrationParameters;
import io.pivotal.android.push.util.ApiRequestImpl;
import io.pivotal.android.push.util.Const;
import io.pivotal.android.push.util.Logger;
import io.pivotal.android.push.util.NetworkWrapper;

/**
 * API request for unregistering a device from the Pivotal Mobile Services Suite back-end server.
 */
public class BackEndUnregisterDeviceApiRequestImpl extends ApiRequestImpl implements BackEndUnregisterDeviceApiRequest {

    public BackEndUnregisterDeviceApiRequestImpl(NetworkWrapper networkWrapper) {
        super(networkWrapper);
    }

    @Override
    public void startUnregisterDevice(String backEndDeviceRegistrationId, RegistrationParameters parameters, BackEndUnregisterDeviceListener listener) {

        verifyUnregistrationArguments(backEndDeviceRegistrationId, parameters, listener);

        try {
            Logger.v("Making network request to the back-end server to unregister the device ID:" + backEndDeviceRegistrationId);
            final URL url = new URL(parameters.getBaseServerUrl() + Const.BACKEND_REGISTRATION_REQUEST_ENDPOINT + "/" + backEndDeviceRegistrationId);
            final HttpURLConnection urlConnection = getHttpURLConnection(url);
            urlConnection.setRequestMethod("DELETE");
            urlConnection.addRequestProperty("Authorization", BackEndRegistrationApiRequestImpl.getBasicAuthorizationValue(parameters));
            urlConnection.connect();

            final int statusCode = urlConnection.getResponseCode();

            urlConnection.disconnect();

            onSuccessfulRequest(statusCode, listener);

        } catch (Exception e) {
            Logger.ex("Back-end device unregistration attempt failed", e);
            listener.onBackEndUnregisterDeviceFailed(e.getLocalizedMessage());
        }
    }

    private void verifyUnregistrationArguments(String backEndDeviceRegistrationId, RegistrationParameters parameters, BackEndUnregisterDeviceListener listener) {
        if (backEndDeviceRegistrationId == null) {
            throw new IllegalArgumentException("backEndDeviceRegistrationId may not be null");
        }
        if (parameters == null) {
            throw new IllegalArgumentException("parameters may not be null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("listener may not be null");
        }
    }

    public void onSuccessfulRequest(int statusCode, BackEndUnregisterDeviceListener listener) {

        // HTTP status 404 is not considered a failure.  If the server doesn't know about
        // this device registration ID then the device can already been considered unregistered.
        if (statusCode != 404 && isFailureStatusCode(statusCode)) {
            Logger.e("Back-end server unregistration failed: server returned HTTP status " + statusCode);
            listener.onBackEndUnregisterDeviceFailed("Back-end server returned HTTP status " + statusCode);
            return;
        }

        Logger.i("Back-end Server device unregistration succeeded.");
        listener.onBackEndUnregisterDeviceSuccess();
    }

    @Override
    public BackEndUnregisterDeviceApiRequest copy() {
        return new BackEndUnregisterDeviceApiRequestImpl(networkWrapper);
    }
}
