/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.backend;

public class BackEndRegistrationApiRequestProvider {

    private final BackEndRegistrationApiRequest dummyRequest;

    public BackEndRegistrationApiRequestProvider(BackEndRegistrationApiRequest dummyRequest) {
        this.dummyRequest = dummyRequest;
    }

    public BackEndRegistrationApiRequest getRequest() {
        return dummyRequest.copy();
    }
}
