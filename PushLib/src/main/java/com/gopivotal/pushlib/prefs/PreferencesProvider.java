package com.gopivotal.pushlib.prefs;

public interface PreferencesProvider {
    String loadDeviceRegistrationId();
    void saveDeviceRegistrationId(String deviceRegistrationId);
}
