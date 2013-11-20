package com.gopivotal.pushlib.backend;

public interface BackEndUnregisterDeviceApiRequest {
    void startUnregisterDevice(String backEndDeviceRegistrationId, BackEndUnregisterDeviceListener listener);
    BackEndUnregisterDeviceApiRequest copy();
}
