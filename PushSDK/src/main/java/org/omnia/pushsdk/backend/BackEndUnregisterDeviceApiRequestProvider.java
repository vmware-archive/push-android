package org.omnia.pushsdk.backend;

public class BackEndUnregisterDeviceApiRequestProvider {

    private final BackEndUnregisterDeviceApiRequest dummyRequest;

    public BackEndUnregisterDeviceApiRequestProvider(BackEndUnregisterDeviceApiRequest dummyRequest) {
        this.dummyRequest = dummyRequest;
    }

    public BackEndUnregisterDeviceApiRequest getRequest() {
        return dummyRequest.copy();
    }
}
