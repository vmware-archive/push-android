package com.gopivotal.pushlib.prefs;

public class FakePreferencesProvider implements PreferencesProvider {

    private final String gcmDeviceRegistrationIdToLoad;
    private boolean wasSaved = false;
    private String savedGcmDeviceRegistrationId;

    public FakePreferencesProvider(String gcmDeviceRegistrationIdToLoad) {
        this.gcmDeviceRegistrationIdToLoad = gcmDeviceRegistrationIdToLoad;
        this.wasSaved = false;
        this.savedGcmDeviceRegistrationId = null;
    }

    @Override
    public String loadGcmDeviceRegistrationId() {
        return gcmDeviceRegistrationIdToLoad;
    }

    @Override
    public void saveGcmDeviceRegistrationId(String gcmDeviceRegistrationId) {
        this.savedGcmDeviceRegistrationId = gcmDeviceRegistrationId;
        wasSaved = true;
    }

    public boolean wasSaved() {
        return wasSaved;
    }

    public String getSavedGcmDeviceRegistrationId() {
        return savedGcmDeviceRegistrationId;
    }
}
