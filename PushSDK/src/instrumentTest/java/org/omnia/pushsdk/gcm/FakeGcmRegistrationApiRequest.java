package org.omnia.pushsdk.gcm;

public class FakeGcmRegistrationApiRequest implements GcmRegistrationApiRequest {

    private final FakeGcmProvider gcmProvider;

    public FakeGcmRegistrationApiRequest(FakeGcmProvider gcmProvider) {
        this.gcmProvider = gcmProvider;
    }

    @Override
    public void startRegistration(String senderId, GcmRegistrationListener listener) {
        try {
            final String registrationId = gcmProvider.register("Dummy Sender ID");
            listener.onGcmRegistrationComplete(registrationId);
        } catch (Exception e) {
            listener.onGcmRegistrationFailed(e.getLocalizedMessage());
        }
    }

    @Override
    public GcmRegistrationApiRequest copy() {
        return new FakeGcmRegistrationApiRequest(gcmProvider);
    }
}
