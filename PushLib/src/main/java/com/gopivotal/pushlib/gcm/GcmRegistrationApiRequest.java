package com.gopivotal.pushlib.gcm;

public interface GcmRegistrationApiRequest {
    void startRegistration(String senderId, GcmRegistrationListener listener);
    GcmRegistrationApiRequest copy();
}
