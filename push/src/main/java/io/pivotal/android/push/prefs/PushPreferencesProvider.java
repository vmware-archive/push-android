/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.prefs;

public interface PushPreferencesProvider {

    public static int NO_SAVED_VERSION = -1;

    String getGcmDeviceRegistrationId();

    void setGcmDeviceRegistrationId(String gcmDeviceRegistrationId);

    String getBackEndDeviceRegistrationId();

    void setBackEndDeviceRegistrationId(String backendDeviceRegistrationId);

    int getAppVersion();

    void setAppVersion(int appVersion);

    String getGcmSenderId();

    void setGcmSenderId(String gcmSenderId);

    String getVariantUuid();

    void setVariantUuid(String variantUuid);

    String getVariantSecret();

    void setVariantSecret(String variantUuid);

    String getDeviceAlias();

    void setDeviceAlias(String deviceAlias);

    String getPackageName();

    void setPackageName(String packageName);

    String getBaseServerUrl();

    void setBaseServerUrl(String baseServerUrl);
}
