package com.gopivotal.pushlib.gcm;

import java.io.IOException;

public class FakeGcmProvider implements GcmProvider {

    private final String registrationId;
    private final boolean willThrow;

    public FakeGcmProvider(String registrationId) {
        this.registrationId = registrationId;
        this.willThrow = false;
    }

    public FakeGcmProvider(String registrationId, boolean willThrow) {
        this.registrationId = registrationId;
        this.willThrow = willThrow;
    }

    @Override
    public String register(String... senderIds) throws IOException {
        if (willThrow) {
            throw new IOException("Fake registration failed fakely");
        }
        return registrationId;
    }
}
