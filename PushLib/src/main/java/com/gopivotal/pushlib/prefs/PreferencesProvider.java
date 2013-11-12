package com.gopivotal.pushlib.prefs;

public interface PreferencesProvider {
    String loadRegistrationId();
    void saveRegistrationId(String registrationId);
}
