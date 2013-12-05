package com.gopivotal.pushlib.backend;

public interface BackEndRegistrationListener {
    void onBackEndRegistrationSuccess(String backEndDeviceRegistrationId);
    void onBackEndRegistrationFailed(String reason);
}
