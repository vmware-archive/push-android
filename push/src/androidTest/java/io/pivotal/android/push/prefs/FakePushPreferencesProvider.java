/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.prefs;

import io.pivotal.android.push.geofence.GeofenceConstants;
import java.util.Set;

public class FakePushPreferencesProvider implements PushPreferencesProvider {

    private String fcmTokenId;
    private String pcfPushDeviceRegistrationId;
    private String platformUuid;
    private String platformSecret;
    private String deviceAlias;
    private String customUserId;
    private String packageName;
    private String serviceUrl;
    private Set<String> tags;
    private long lastGeofenceUpdate = GeofenceConstants.NEVER_UPDATED_GEOFENCES;
    private boolean areGeofencesEnabled;
    private boolean wasFcmTokenIdSaved = false;
    private boolean wasPCFPushDeviceRegistrationIdSaved = false;
    private boolean wasPlatformUuidSaved = false;
    private boolean wasPlatformSecretSaved = false;
    private boolean wasDeviceAliasSaved = false;
    private boolean wasCustomUserIdSaved = false;
    private boolean wasPackageNameSaved = false;
    private boolean wasServiceUrlSaved = false;
    private boolean wasTagsSaved = false;
    private boolean wasLastGeofenceUpdateSaved = false;
    private boolean wasAreGeofencesEnabledSaved = false;
    private boolean areAnalyticsEnabled = false;

    public FakePushPreferencesProvider() {
    }

    public FakePushPreferencesProvider(String fcmTokenIdToLoad,
                                       String pcfPushDeviceRegistrationIdToLoad,
                                       String platformUuidToLoad,
                                       String platformSecretToLoad,
                                       String deviceAliasToLoad,
                                       String customUserIdToLoad,
                                       String packageNameToLoad,
                                       String serviceUrlToLoad,
                                       Set<String> tagsToLoad,
                                       long lastGeofenceUpdateToLoad,
                                       boolean areGeofencesEnabled) {

        this.fcmTokenId = fcmTokenIdToLoad;
        this.pcfPushDeviceRegistrationId = pcfPushDeviceRegistrationIdToLoad;
        this.platformUuid = platformUuidToLoad;
        this.platformSecret = platformSecretToLoad;
        this.deviceAlias = deviceAliasToLoad;
        this.customUserId = customUserIdToLoad;
        this.packageName = packageNameToLoad;
        this.serviceUrl = serviceUrlToLoad;
        this.lastGeofenceUpdate = lastGeofenceUpdateToLoad;
        this.areGeofencesEnabled = areGeofencesEnabled;
        this.tags = tagsToLoad;
    }

    @Override
    public String getFcmTokenId() {
        return fcmTokenId;
    }

    @Override
    public String getPCFPushDeviceRegistrationId() {
        return pcfPushDeviceRegistrationId;
    }

    @Override
    public String getPlatformUuid() {
        return platformUuid;
    }

    @Override
    public String getPlatformSecret() {
        return platformSecret;
    }

    @Override
    public String getDeviceAlias() {
        return deviceAlias;
    }

    public String getCustomUserId() {
        return customUserId;
    }

    @Override
    public String getPackageName() {
        return packageName;
    }

    @Override
    public String getServiceUrl() {
        return serviceUrl;
    }

    @Override
    public long getLastGeofenceUpdate() {
        return lastGeofenceUpdate;
    }

    @Override
    public Set<String> getTags() {
        return tags;
    }

    @Override
    public boolean areGeofencesEnabled() {
        return areGeofencesEnabled;
    }

    @Override
    public void setPCFPushDeviceRegistrationId(String pcfPushDeviceRegistrationId) {
        this.pcfPushDeviceRegistrationId = pcfPushDeviceRegistrationId;
        wasPCFPushDeviceRegistrationIdSaved = true;
    }

    @Override
    public void setFcmTokenId(String fcmTokenId) {
        this.fcmTokenId = fcmTokenId;
        wasFcmTokenIdSaved = true;
    }

    @Override
    public void setPlatformUuid(String platformUuid) {
        this.platformUuid = platformUuid;
        wasPlatformUuidSaved = true;
    }

    @Override
    public void setPlatformSecret(String platformSecret) {
        this.platformSecret = platformSecret;
        wasPlatformSecretSaved = true;
    }

    @Override
    public void setDeviceAlias(String deviceAlias) {
        this.deviceAlias = deviceAlias;
        wasDeviceAliasSaved = true;
    }

    public void setCustomUserId(String customUserId) {
        this.customUserId = customUserId;
        wasCustomUserIdSaved = true;
    }

    @Override
    public void setPackageName(String packageName) {
        this.packageName = packageName;
        wasPackageNameSaved = true;
    }

    @Override
    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
        wasServiceUrlSaved = true;
    }

    @Override
    public void setTags(Set<String> tags) {
        this.tags = tags;
        wasTagsSaved = true;
    }

    @Override
    public void setLastGeofenceUpdate(long timestamp) {
        this.lastGeofenceUpdate = timestamp;
        wasLastGeofenceUpdateSaved = true;
    }

    @Override
    public void setAreGeofencesEnabled(boolean areGeofencesEnabled) {
        this.areGeofencesEnabled = areGeofencesEnabled;
        wasAreGeofencesEnabledSaved = true;
    }

    public void setAreAnalyticsEnabled(boolean areAnalyticsEnabled) {
        this.areAnalyticsEnabled = areAnalyticsEnabled;
    }

    @Override
    public boolean areAnalyticsEnabled() {
        return areAnalyticsEnabled;
    }

    public boolean wasFcmTokenIdSaved() {
        return wasFcmTokenIdSaved;
    }

    public boolean wasPCFPushDeviceRegistrationIdSaved() {
        return wasPCFPushDeviceRegistrationIdSaved;
    }

    public boolean wasPlatformUuidSaved() {
        return wasPlatformUuidSaved;
    }

    public boolean wasPlatformSecretSaved() {
        return wasPlatformSecretSaved;
    }

    public boolean wasDeviceAliasSaved() {
        return wasDeviceAliasSaved;
    }

    public boolean wasCustomUserIdSaved() {
        return wasCustomUserIdSaved;
    }

    public boolean isWasPackageNameSaved() {
        return wasPackageNameSaved;
    }

    public boolean wasServiceUrlSaved() {
        return wasServiceUrlSaved;
    }

    public boolean wereTagsSaved() {
        return wasTagsSaved;
    }

    public boolean wasLastGeofenceUpdateSaved() {
        return wasLastGeofenceUpdateSaved;
    }

    public boolean wasAreGeofencesEnabledSaved() {
        return wasAreGeofencesEnabledSaved;
    }
}
