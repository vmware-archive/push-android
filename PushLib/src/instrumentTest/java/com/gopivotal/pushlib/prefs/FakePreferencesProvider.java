package com.gopivotal.pushlib.prefs;

public class FakePreferencesProvider implements PreferencesProvider {

    private String gcmDeviceRegistrationId;
    private String backEndDeviceRegistrationId;
    private String releaseUuid;
    private int appVersion;
    private boolean wasGcmDeviceRegistrationIdSaved;
    private boolean wasBackEndDeviceRegistrationIdSaved;
    private boolean wasAppVersionSaved;
    private boolean wasReleaseUuidSaved;

    public FakePreferencesProvider(String gcmDeviceRegistrationIdToLoad, String backEndDeviceRegistrationIdToLoad, int appVersionToLoad, String releaseUuidToLoad) {
        this.gcmDeviceRegistrationId = gcmDeviceRegistrationIdToLoad;
        this.backEndDeviceRegistrationId = backEndDeviceRegistrationIdToLoad;
        this.appVersion = appVersionToLoad;
        this.releaseUuid = releaseUuidToLoad;
        this.wasGcmDeviceRegistrationIdSaved = false;
        this.wasBackEndDeviceRegistrationIdSaved = false;
        this.wasAppVersionSaved = false;
        this.wasReleaseUuidSaved = false;
    }

    @Override
    public String loadGcmDeviceRegistrationId() {
        return gcmDeviceRegistrationId;
    }

    @Override
    public String loadBackEndDeviceRegistrationId() {
        return backEndDeviceRegistrationId;
    }

    @Override
    public int loadAppVersion() {
        return appVersion;
    }

    @Override
    public String loadReleaseUuid() {
        return releaseUuid;
    }

    @Override
    public void saveGcmDeviceRegistrationId(String gcmDeviceRegistrationId) {
        this.gcmDeviceRegistrationId = gcmDeviceRegistrationId;
        wasGcmDeviceRegistrationIdSaved = true;
    }

    @Override
    public void saveBackEndDeviceRegistrationId(String backendDeviceRegistrationId) {
        this.backEndDeviceRegistrationId = backendDeviceRegistrationId;
        wasBackEndDeviceRegistrationIdSaved = true;
    }

    @Override
    public void saveAppVersion(int appVersion) {
        this.appVersion = appVersion;
        wasAppVersionSaved = true;
    }

    @Override
    public void saveReleaseUuid(String releaseUuid) {
        this.releaseUuid = releaseUuid;
        wasReleaseUuidSaved = true;
    }

    public boolean wasGcmDeviceRegistrationIdSaved() {
        return wasGcmDeviceRegistrationIdSaved;
    }

    public boolean wasBackEndDeviceRegistrationIdSaved() {
        return wasBackEndDeviceRegistrationIdSaved;
    }

    public boolean wasAppVersionSaved() {
        return wasAppVersionSaved;
    }

    public boolean wasReleaseUuidSaved() {
        return wasReleaseUuidSaved;
    }
}
