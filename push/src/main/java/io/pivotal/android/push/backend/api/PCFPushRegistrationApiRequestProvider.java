/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.backend.api;

public class PCFPushRegistrationApiRequestProvider {

    private final PCFPushRegistrationApiRequest dummyRequest;

    public PCFPushRegistrationApiRequestProvider(PCFPushRegistrationApiRequest dummyRequest) {
        this.dummyRequest = dummyRequest;
    }

    public PCFPushRegistrationApiRequest getRequest() {
        return dummyRequest.copy();
    }
}
