package com.gopivotal.pushlib.prefs;

public class FakePreferencesProvider implements PreferencesProvider {

    private final String registrationIdToLoad;
    private boolean wasSaved = false;
    private String savedRegistrationId;

    public FakePreferencesProvider(String registrationIdToLoad) {
        this.registrationIdToLoad = registrationIdToLoad;
        this.wasSaved = false;
        this.savedRegistrationId = null;
    }

    @Override
    public String loadRegistrationId() {
        return registrationIdToLoad;
    }

    @Override
    public void saveRegistrationId(String registrationId) {
        this.savedRegistrationId = registrationId;
        wasSaved = true;
    }

    public boolean wasSaved() {
        return wasSaved;
    }

    public String getSavedRegistrationId() {
        return savedRegistrationId;
    }
}
