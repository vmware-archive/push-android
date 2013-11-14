package com.gopivotal.pushlib.prefs;

public class FakePreferencesProvider implements PreferencesProvider {

    private final String gcmDeviceRegistrationIdToLoad;
    private final String backEndDeviceRegistrationIdToLoad;
    private boolean wasGcmDeviceRegistrationIdSaved;
    private boolean wasBackEndDeviceRegistrationIdSaved;
    private String savedGcmDeviceRegistrationId;
    private String savedBackEndDeviceRegistrationId;

    public FakePreferencesProvider(String gcmDeviceRegistrationIdToLoad, String backendDeviceRegistrationId) {
        this.gcmDeviceRegistrationIdToLoad = gcmDeviceRegistrationIdToLoad;
        this.backEndDeviceRegistrationIdToLoad = backendDeviceRegistrationId;
        this.wasGcmDeviceRegistrationIdSaved = false;
        this.wasBackEndDeviceRegistrationIdSaved = false;
        this.savedGcmDeviceRegistrationId = null;
        this.savedBackEndDeviceRegistrationId = null;
    }

    @Override
    public String loadGcmDeviceRegistrationId() {
        return gcmDeviceRegistrationIdToLoad;
    }

    @Override
    public void saveGcmDeviceRegistrationId(String gcmDeviceRegistrationId) {
        savedGcmDeviceRegistrationId = gcmDeviceRegistrationId;
        wasGcmDeviceRegistrationIdSaved = true;
    }

    @Override
    public String loadBackEndDeviceRegistrationId() {
        return backEndDeviceRegistrationIdToLoad;
    }

    @Override
    public void saveBackEndDeviceRegistrationId(String backendDeviceRegistrationId) {
        savedBackEndDeviceRegistrationId = backendDeviceRegistrationId;
        wasBackEndDeviceRegistrationIdSaved = true;
    }

    public boolean wasGcmDeviceRegistrationIdSaved() {
        return wasGcmDeviceRegistrationIdSaved;
    }

    public boolean wasBackEndDeviceRegistrationIdSaved() {
        return wasBackEndDeviceRegistrationIdSaved;
    }

    public String getSavedGcmDeviceRegistrationId() {
        return savedGcmDeviceRegistrationId;
    }

    public String getSavedBackEndDeviceRegistrationId() {
        return savedBackEndDeviceRegistrationId;
    }
}
