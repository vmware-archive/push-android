package com.gopivotal.pushlib.gcm;

public class GcmUnregistrationApiRequestProvider {

    private final GcmUnregistrationApiRequest dummyRequest;

    public GcmUnregistrationApiRequestProvider(GcmUnregistrationApiRequest dummyRequest) {
        this.dummyRequest = dummyRequest;
    }

    public GcmUnregistrationApiRequest getRequest() {
        return dummyRequest.copy();
    }
}
