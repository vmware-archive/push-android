package com.gopivotal.pushlib.gcm;

import java.io.IOException;

public class FakeGcmProvider implements GcmProvider {

    private final String gcmDeviceRegistrationId;
    private final boolean willThrow;
    private boolean wasRegisterCalled;

    public FakeGcmProvider(String gcmDeviceRegistrationId) {
        this.gcmDeviceRegistrationId = gcmDeviceRegistrationId;
        this.willThrow = false;
        this.wasRegisterCalled = false;
    }

    public FakeGcmProvider(String gcmDeviceRegistrationId, boolean willThrow) {
        this.gcmDeviceRegistrationId = gcmDeviceRegistrationId;
        this.willThrow = willThrow;
        this.wasRegisterCalled = false;
    }

    @Override
    public String register(String... senderIds) throws IOException {
        this.wasRegisterCalled = true;
        if (willThrow) {
            throw new IOException("Fake GCM device registration failed fakely");
        }
        return gcmDeviceRegistrationId;
    }

    public boolean wasRegisterCalled() {
        return wasRegisterCalled;
    }

    // TODO - needs unregister method
}
