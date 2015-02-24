/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.backend.api;

import io.pivotal.android.push.RegistrationParameters;

public class FakePCFPushUnregisterDeviceApiRequest implements PCFPushUnregisterDeviceApiRequest {

    private final FakePCFPushUnregisterDeviceApiRequest originatingRequest;
    private final boolean willBeSuccessfulRequest;
    private boolean wasUnregisterCalled = false;

    public FakePCFPushUnregisterDeviceApiRequest() {
        this.originatingRequest = null;
        this.willBeSuccessfulRequest = true;
    }

    public FakePCFPushUnregisterDeviceApiRequest(boolean willBeSuccessfulRequest) {
        this.originatingRequest = null;
        this.willBeSuccessfulRequest = willBeSuccessfulRequest;
    }

    public FakePCFPushUnregisterDeviceApiRequest(FakePCFPushUnregisterDeviceApiRequest originatingRequest, boolean willBeSuccessfulRequest) {
        this.originatingRequest = originatingRequest;
        this.willBeSuccessfulRequest = willBeSuccessfulRequest;
    }

    @Override
    public void startUnregisterDevice(String pcfPushDeviceRegistrationId, RegistrationParameters parameters, PCFPushUnregisterDeviceListener listener) {
        wasUnregisterCalled = true;
        if (originatingRequest != null) {
            originatingRequest.wasUnregisterCalled = true;
        }

        if (willBeSuccessfulRequest) {
            listener.onPCFPushUnregisterDeviceSuccess();
        } else {
            listener.onPCFPushUnregisterDeviceFailed("Fake PCF Push registration failed fakely");
        }
    }

    @Override
    public PCFPushUnregisterDeviceApiRequest copy() {
        return new FakePCFPushUnregisterDeviceApiRequest(this, willBeSuccessfulRequest);
    }

    public boolean wasUnregisterCalled() {
        return wasUnregisterCalled;
    }
}
