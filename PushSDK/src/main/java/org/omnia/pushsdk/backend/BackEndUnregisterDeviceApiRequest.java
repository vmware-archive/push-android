package org.omnia.pushsdk.backend;

public interface BackEndUnregisterDeviceApiRequest {
    void startUnregisterDevice(String backEndDeviceRegistrationId, BackEndUnregisterDeviceListener listener);
    BackEndUnregisterDeviceApiRequest copy();
}
