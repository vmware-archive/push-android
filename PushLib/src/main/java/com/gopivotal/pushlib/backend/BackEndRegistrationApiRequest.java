package com.gopivotal.pushlib.backend;

import com.gopivotal.pushlib.PushLibParameters;

public interface BackEndRegistrationApiRequest {
    void startDeviceRegistration(String gcmDeviceRegistrationId, PushLibParameters parameters, BackEndRegistrationListener listener);
    BackEndRegistrationApiRequest copy();
}
