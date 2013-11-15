package com.gopivotal.pushlib.backend;

public class BackEndRegistrationApiRequestProvider {

    private final BackEndRegistrationApiRequest dummyRequest;

    public BackEndRegistrationApiRequestProvider(BackEndRegistrationApiRequest dummyRequest) {
        this.dummyRequest = dummyRequest;
    }

    public BackEndRegistrationApiRequest getRequest() {
        return dummyRequest.copy();
    }
}
