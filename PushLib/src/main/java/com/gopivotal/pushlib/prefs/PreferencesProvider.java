package com.gopivotal.pushlib.prefs;

public interface PreferencesProvider {

    String loadGcmDeviceRegistrationId();

    void saveGcmDeviceRegistrationId(String gcmDeviceRegistrationId);

}
