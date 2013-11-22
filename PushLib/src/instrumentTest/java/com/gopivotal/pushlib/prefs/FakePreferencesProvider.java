package com.gopivotal.pushlib.prefs;

public class FakePreferencesProvider implements PreferencesProvider {

    private String gcmDeviceRegistrationId;
    private String backEndDeviceRegistrationId;
    private String releaseUuid;
    private String releaseSecret;
    private String deviceAlias;
    private int appVersion;
    private boolean wasGcmDeviceRegistrationIdSaved = false;
    private boolean wasBackEndDeviceRegistrationIdSaved = false;
    private boolean wasAppVersionSaved = false;
    private boolean wasReleaseUuidSaved = false;
    private boolean wasReleaseSecretSaved = false;
    private boolean wasDeviceAliasSaved = false;

    public FakePreferencesProvider(String gcmDeviceRegistrationIdToLoad, String backEndDeviceRegistrationIdToLoad, int appVersionToLoad, String releaseSecretToLoad, String releaseUuidToLoad, String deviceAliasToLoad) {
        this.gcmDeviceRegistrationId = gcmDeviceRegistrationIdToLoad;
        this.backEndDeviceRegistrationId = backEndDeviceRegistrationIdToLoad;
        this.appVersion = appVersionToLoad;
        this.releaseUuid = releaseUuidToLoad;
        this.releaseSecret = releaseSecret;
        this.deviceAlias = deviceAlias;
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
    public String loadReleaseSecret() {
        return releaseSecret;
    }

    @Override
    public String loadDeviceAlias() {
        return deviceAlias;
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

    @Override
    public void saveReleaseSecret(String releaseSecret) {
        this.releaseSecret = releaseSecret;
        wasReleaseSecretSaved = true;
    }

    @Override
    public void saveDeviceAlias(String deviceAlias) {
        this.deviceAlias = deviceAlias;
        wasDeviceAliasSaved = true;
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

    public boolean wasReleaseSecretSaved() {
        return wasReleaseSecretSaved;
    }

    public boolean wasDeviceAliasSaved() {
        return wasDeviceAliasSaved;
    }
}
