package org.omnia.pushsdk.backend;

import org.omnia.pushsdk.RegistrationParameters;

public interface BackEndRegistrationApiRequest {
    void startDeviceRegistration(String gcmDeviceRegistrationId, RegistrationParameters parameters, BackEndRegistrationListener listener);
    BackEndRegistrationApiRequest copy();
}
