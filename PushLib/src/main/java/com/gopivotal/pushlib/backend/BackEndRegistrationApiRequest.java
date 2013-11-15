package com.gopivotal.pushlib.backend;

public interface BackEndRegistrationApiRequest {
    void startDeviceRegistration(String gcmDeviceRegistrationId, BackEndRegistrationListener listener);
    BackEndRegistrationApiRequest copy();
}
