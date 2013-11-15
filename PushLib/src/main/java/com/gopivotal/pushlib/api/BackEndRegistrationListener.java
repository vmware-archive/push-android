package com.gopivotal.pushlib.api;

public interface BackEndRegistrationListener {
    void onBackEndRegistrationSuccess(String backEndDeviceRegistrationId);
    void onBackEndRegistrationFailed(String reason);
}
