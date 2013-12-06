package org.omnia.pushsdk.backend;
import org.omnia.pushsdk.network.NetworkWrapper;
import org.omnia.pushsdk.util.Const;
import org.omnia.pushsdk.util.PushLibLogger;
import com.xtreme.network.NetworkRequest;
import com.xtreme.network.NetworkResponse;

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
        final NetworkRequest request = getNetworkRequest(backEndDeviceRegistrationId, listener);
        try {
            final NetworkResponse networkResponse = networkWrapper.getNetworkRequestLauncher().executeRequestSynchronously(request);
            onSuccessfulRequest(networkResponse, listener);
        } catch(Exception e) {
            PushLibLogger.ex("Back-end device unregistration attempt failed", e);
            listener.onBackEndUnregisterDeviceFailed(e.getLocalizedMessage());
        }
    }

    private NetworkRequest getNetworkRequest(String backEndDeviceRegistrationId, BackEndUnregisterDeviceListener listener) {
        if (backEndDeviceRegistrationId == null) {
            throw new IllegalArgumentException("backEndDeviceRegistrationId may not be null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("listener may not be null");
        }

        PushLibLogger.v("Making network request to the back-end server to unregister the device ID:" + backEndDeviceRegistrationId);
        final NetworkRequest networkRequest = new NetworkRequest(Const.BACKEND_REGISTRATION_REQUEST_URL + "/" + backEndDeviceRegistrationId);
        networkRequest.setRequestType(NetworkRequest.RequestType.DELETE);
        return networkRequest;
    }

    public void onSuccessfulRequest(NetworkResponse networkResponse, BackEndUnregisterDeviceListener listener) {

        if (networkResponse == null) {
            PushLibLogger.e("Back-end server unregistration failed: no networkResponse");
            listener.onBackEndUnregisterDeviceFailed("No networkResponse from back-end server.");
            return;
        }

        if (networkResponse.getStatus() == null) {
            PushLibLogger.e("Back-end server unregistration failed: no statusLine in networkResponse");
            listener.onBackEndUnregisterDeviceFailed("Back-end no statusLine in networkResponse");
            return;
        }

        final int statusCode = networkResponse.getStatus().getStatusCode();
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
