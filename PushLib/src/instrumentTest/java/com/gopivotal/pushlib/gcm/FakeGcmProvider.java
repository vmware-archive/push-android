package com.gopivotal.pushlib.gcm;

import android.content.Context;

import java.io.IOException;

public class FakeGcmProvider implements GcmProvider {

    private final String gcmDeviceRegistrationId;
    private final boolean willThrow;
    private boolean wasRegisterCalled = false;
    private boolean isGooglePlayServicesInstalled = true;

    public FakeGcmProvider(String gcmDeviceRegistrationId) {
        this.gcmDeviceRegistrationId = gcmDeviceRegistrationId;
        this.willThrow = false;
    }

    public FakeGcmProvider(String gcmDeviceRegistrationId, boolean willThrow) {
        this.gcmDeviceRegistrationId = gcmDeviceRegistrationId;
        this.willThrow = willThrow;
    }

    public void setIsGooglePlayServicesInstalled(boolean isGooglePlayServicesInstalled) {
        this.isGooglePlayServicesInstalled = isGooglePlayServicesInstalled;
    }

    @Override
    public String register(String... senderIds) throws IOException {
        this.wasRegisterCalled = true;
        if (willThrow) {
            throw new IOException("Fake GCM device registration failed fakely");
        }
        return gcmDeviceRegistrationId;
    }

    @Override
    public boolean isGooglePlayServicesInstalled(Context context) {
        return isGooglePlayServicesInstalled;
    }

    public boolean wasRegisterCalled() {
        return wasRegisterCalled;
    }

    // TODO - needs unregister method
}
