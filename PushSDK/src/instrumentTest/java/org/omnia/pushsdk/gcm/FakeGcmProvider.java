package org.omnia.pushsdk.gcm;

import android.content.Context;

import java.io.IOException;

public class FakeGcmProvider implements GcmProvider {

    private final String gcmDeviceRegistrationId;
    private final boolean willRegisterThrow;
    private boolean wasRegisterCalled = false;
    private boolean isGooglePlayServicesInstalled = true;
    private boolean wasUnregisterCalled = false;
    private boolean willUnregisterThrow = false;

    public FakeGcmProvider(String gcmDeviceRegistrationId) {
        this.gcmDeviceRegistrationId = gcmDeviceRegistrationId;
        this.willRegisterThrow = false;
    }

    public FakeGcmProvider(String gcmDeviceRegistrationId, boolean willRegisterThrow) {
        this.gcmDeviceRegistrationId = gcmDeviceRegistrationId;
        this.willRegisterThrow = willRegisterThrow;
    }

    public FakeGcmProvider(String gcmDeviceRegistrationId, boolean willRegisterThrow, boolean willUnregisterThrow) {
        this.gcmDeviceRegistrationId = gcmDeviceRegistrationId;
        this.willRegisterThrow = willRegisterThrow;
        this.willUnregisterThrow = willUnregisterThrow;
    }

    public void setIsGooglePlayServicesInstalled(boolean isGooglePlayServicesInstalled) {
        this.isGooglePlayServicesInstalled = isGooglePlayServicesInstalled;
    }

    @Override
    public String register(String... senderIds) throws IOException {
        this.wasRegisterCalled = true;
        if (willRegisterThrow) {
            throw new IOException("Fake GCM device registration failed fakely.");
        }
        return gcmDeviceRegistrationId;
    }

    @Override
    public void unregister() throws IOException {
        this.wasUnregisterCalled = true;
        if (willUnregisterThrow) {
            throw new IOException("Fake GCM device unregistration failed fakely.");
        }
    }

    @Override
    public boolean isGooglePlayServicesInstalled(Context context) {
        return isGooglePlayServicesInstalled;
    }

    public boolean wasRegisterCalled() {
        return wasRegisterCalled;
    }

    public boolean wasUnregisterCalled() {
        return wasUnregisterCalled;
    }
}
