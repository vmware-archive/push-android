package com.gopivotal.pushlib.prefs;

public interface PreferencesProvider {

    String loadGcmDeviceRegistrationId();

    void saveGcmDeviceRegistrationId(String gcmDeviceRegistrationId);

    String loadBackEndDeviceRegistrationId();

    void saveBackEndDeviceRegistrationId(String backendDeviceRegistrationId);

    int loadAppVersion();

    void saveAppVersion(int appVersion);

    String loadGcmSenderId();

    void saveGcmSenderId(String gcmSenderId);

    String loadReleaseUuid();

    void saveReleaseUuid(String releaseUuid);

    String loadReleaseSecret();

    void saveReleaseSecret(String releaseUuid);

    String loadDeviceAlias();

    void saveDeviceAlias(String deviceAlias);
}
