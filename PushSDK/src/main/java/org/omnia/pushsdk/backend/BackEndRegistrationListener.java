package org.omnia.pushsdk.backend;

public interface BackEndRegistrationListener {
    void onBackEndRegistrationSuccess(String backEndDeviceRegistrationId);
    void onBackEndRegistrationFailed(String reason);
}
