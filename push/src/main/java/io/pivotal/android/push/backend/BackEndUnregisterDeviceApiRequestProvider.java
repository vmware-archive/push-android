/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.backend;

public class BackEndUnregisterDeviceApiRequestProvider {

    private final BackEndUnregisterDeviceApiRequest dummyRequest;

    public BackEndUnregisterDeviceApiRequestProvider(BackEndUnregisterDeviceApiRequest dummyRequest) {
        this.dummyRequest = dummyRequest;
    }

    public BackEndUnregisterDeviceApiRequest getRequest() {
        return dummyRequest.copy();
    }
}
