package com.gopivotal.pushlib.api;

public interface BackEndRegistrationApiRequest {
    void startDeviceRegistration(String gcmDeviceRegistrationId, BackEndRegistrationListener listener);
}
