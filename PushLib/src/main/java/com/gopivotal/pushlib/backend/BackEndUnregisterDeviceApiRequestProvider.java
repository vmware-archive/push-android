package com.gopivotal.pushlib.backend;

public class BackEndUnregisterDeviceApiRequestProvider {

    private final BackEndUnregisterDeviceApiRequest dummyRequest;

    public BackEndUnregisterDeviceApiRequestProvider(BackEndUnregisterDeviceApiRequest dummyRequest) {
        this.dummyRequest = dummyRequest;
    }

    public BackEndUnregisterDeviceApiRequest getRequest() {
        return dummyRequest.copy();
    }
}
