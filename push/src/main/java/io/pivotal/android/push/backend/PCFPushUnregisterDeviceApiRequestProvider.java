/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.backend;

public class PCFPushUnregisterDeviceApiRequestProvider {

    private final PCFPushUnregisterDeviceApiRequest dummyRequest;

    public PCFPushUnregisterDeviceApiRequestProvider(PCFPushUnregisterDeviceApiRequest dummyRequest) {
        this.dummyRequest = dummyRequest;
    }

    public PCFPushUnregisterDeviceApiRequest getRequest() {
        return dummyRequest.copy();
    }
}
