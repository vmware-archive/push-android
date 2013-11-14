package com.gopivotal.pushlib.gcm;

import java.io.IOException;

public class FakeGcmRegistrationApiRequest implements GcmRegistrationApiRequest {

    private final String gcmDeviceRegistrationId;
    private final boolean isSuccessful;
    private final FakeGcmProvider gcmProvider;

    public FakeGcmRegistrationApiRequest(boolean isSuccessful, String gcmDeviceRegistrationId, FakeGcmProvider gcmProvider) {
        this.isSuccessful = isSuccessful;
        this.gcmDeviceRegistrationId = gcmDeviceRegistrationId;
        this.gcmProvider = gcmProvider;
    }

    @Override
    public void startRegistration(String senderId, GcmRegistrationListener listener) {

        if (isSuccessful) {
            try {
                gcmProvider.register(senderId);
            } catch (IOException e) {
                listener.onGcmRegistrationFailed(e.getLocalizedMessage());
                return;
            }
            listener.onGcmRegistrationComplete(gcmDeviceRegistrationId);
        } else {
            listener.onGcmRegistrationFailed("Fake request failed fakely");
        }
    }

    @Override
    public GcmRegistrationApiRequest copy() {
        return new FakeGcmRegistrationApiRequest(isSuccessful, gcmDeviceRegistrationId, gcmProvider);
    }
}
