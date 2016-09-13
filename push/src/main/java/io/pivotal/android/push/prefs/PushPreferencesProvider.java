/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.prefs;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import io.pivotal.android.push.version.Version;

public interface PushPreferencesProvider {

    String getFcmTokenId();

    void setFcmTokenId(String fcmTokenId);

    String getPCFPushDeviceRegistrationId();

    void setPCFPushDeviceRegistrationId(String pcfPushDeviceRegistrationId);

    String getPlatformUuid();

    void setPlatformUuid(String platformUuid);

    String getPlatformSecret();

    void setPlatformSecret(String platformSecret);

    String getDeviceAlias();

    void setDeviceAlias(String deviceAlias);

    String getPackageName();

    void setPackageName(String packageName);

    String getServiceUrl();

    void setServiceUrl(String serviceUrl);

    Set<String> getTags();

    void setTags(Set<String> tags);

    long getLastGeofenceUpdate();

    void setLastGeofenceUpdate(long timestamp);

    boolean areGeofencesEnabled();

    void setAreGeofencesEnabled(boolean areGeofencesEnabled);

    Map<String, String> getRequestHeaders();

    void setRequestHeaders(Map<String, String> requestHeaders);

    Version getBackEndVersion();

    void setBackEndVersion(Version version);

    Date getBackEndVersionTimePolled();

    void setBackEndVersionTimePolled(Date timestamp);

    String getCustomUserId();

    void setCustomUserId(String customUserId);

    boolean areAnalyticsEnabled();
}
