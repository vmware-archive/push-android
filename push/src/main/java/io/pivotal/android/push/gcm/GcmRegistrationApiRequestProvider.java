/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.gcm;

public class GcmRegistrationApiRequestProvider {

    private final GcmRegistrationApiRequest dummyRequest;

    public GcmRegistrationApiRequestProvider(GcmRegistrationApiRequest dummyRequest) {
        this.dummyRequest = dummyRequest;
    }

    public GcmRegistrationApiRequest getRequest() {
        return dummyRequest.copy();
    }
}
