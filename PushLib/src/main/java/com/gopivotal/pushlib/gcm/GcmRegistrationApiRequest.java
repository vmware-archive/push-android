package com.gopivotal.pushlib.gcm;

public interface GcmRegistrationApiRequest {
    void startRegistration(GcmRegistrationListener listener);
}
