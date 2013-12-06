package org.omnia.pushsdk.backend;

public interface BackEndUnregisterDeviceListener {
    void onBackEndUnregisterDeviceSuccess();
    void onBackEndUnregisterDeviceFailed(String reason);
}
