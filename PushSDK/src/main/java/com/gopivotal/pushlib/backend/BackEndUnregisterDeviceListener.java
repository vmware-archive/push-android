package com.gopivotal.pushlib.backend;

public interface BackEndUnregisterDeviceListener {
    void onBackEndUnregisterDeviceSuccess();
    void onBackEndUnregisterDeviceFailed(String reason);
}
