package com.gopivotal.pushlib.backend;

import com.gopivotal.pushlib.RegistrationParameters;

public interface BackEndRegistrationApiRequest {
    void startDeviceRegistration(String gcmDeviceRegistrationId, RegistrationParameters parameters, BackEndRegistrationListener listener);
    BackEndRegistrationApiRequest copy();
}
