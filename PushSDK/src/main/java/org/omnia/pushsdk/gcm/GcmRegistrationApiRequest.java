package org.omnia.pushsdk.gcm;

public interface GcmRegistrationApiRequest {
    void startRegistration(String senderId, GcmRegistrationListener listener);
    GcmRegistrationApiRequest copy();
}
