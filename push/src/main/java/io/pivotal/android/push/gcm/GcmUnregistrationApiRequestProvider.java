/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.gcm;

public class GcmUnregistrationApiRequestProvider {

    private final GcmUnregistrationApiRequest dummyRequest;

    public GcmUnregistrationApiRequestProvider(GcmUnregistrationApiRequest dummyRequest) {
        this.dummyRequest = dummyRequest;
    }

    public GcmUnregistrationApiRequest getRequest() {
        return dummyRequest.copy();
    }
}
