package com.gopivotal.pushlib.gcm;

import java.io.IOException;

public class FakeGcmProvider implements GcmProvider {

    private final String registrationId;
    private final boolean willThrow;
    private boolean wasRegisterCalled;

    public FakeGcmProvider(String registrationId) {
        this.registrationId = registrationId;
        this.willThrow = false;
        this.wasRegisterCalled = false;
    }

    public FakeGcmProvider(String registrationId, boolean willThrow) {
        this.registrationId = registrationId;
        this.willThrow = willThrow;
        this.wasRegisterCalled = false;
    }

    @Override
    public String register(String... senderIds) throws IOException {
        this.wasRegisterCalled = true;
        if (willThrow) {
            throw new IOException("Fake registration failed fakely");
        }
        return registrationId;
    }

    public boolean wasRegisterCalled() {
        return wasRegisterCalled;
    }
}
