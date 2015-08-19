/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.prefs;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import io.pivotal.android.push.geofence.GeofenceEngine;
import io.pivotal.android.push.version.Version;

public class FakePushPreferencesProvider implements PushPreferencesProvider {

    private String gcmDeviceRegistrationId;
    private String pcfPushDeviceRegistrationId;
    private String gcmSenderId;
    private String platformUuid;
    private String platformSecret;
    private String deviceAlias;
    private String packageName;
    private String serviceUrl;
    private Set<String> tags;
    private Map<String, String> requestHeaders;
    private int appVersion;
    private long lastGeofenceUpdate = GeofenceEngine.NEVER_UPDATED_GEOFENCES;
    private Version backEndVersion;
    private Date backEndVersionTimePolled;
    private boolean areGeofencesEnabled;
    private boolean wasGcmDeviceRegistrationIdSaved = false;
    private boolean wasPCFPushDeviceRegistrationIdSaved = false;
    private boolean wasAppVersionSaved = false;
    private boolean wasGcmSenderIdSaved = false;
    private boolean wasPlatformUuidSaved = false;
    private boolean wasPlatformSecretSaved = false;
    private boolean wasDeviceAliasSaved = false;
    private boolean wasPackageNameSaved = false;
    private boolean wasServiceUrlSaved = false;
    private boolean wasTagsSaved = false;
    private boolean wasLastGeofenceUpdateSaved = false;
    private boolean wasAreGeofencesEnabledSaved = false;
    private boolean areAnalyticsEnabled = false;

    public FakePushPreferencesProvider() {
    }

    public FakePushPreferencesProvider(String gcmDeviceRegistrationIdToLoad,
                                       String pcfPushDeviceRegistrationIdToLoad,
                                       int appVersionToLoad,
                                       String gcmSenderIdToLoad,
                                       String platformUuidToLoad,
                                       String platformSecretToLoad,
                                       String deviceAliasToLoad,
                                       String packageNameToLoad,
                                       String serviceUrlToLoad,
                                       Set<String> tagsToLoad,
                                       long lastGeofenceUpdateToLoad,
                                       boolean areGeofencesEnabled) {

        this.gcmDeviceRegistrationId = gcmDeviceRegistrationIdToLoad;
        this.pcfPushDeviceRegistrationId = pcfPushDeviceRegistrationIdToLoad;
        this.appVersion = appVersionToLoad;
        this.gcmSenderId = gcmSenderIdToLoad;
        this.platformUuid = platformUuidToLoad;
        this.platformSecret = platformSecretToLoad;
        this.deviceAlias = deviceAliasToLoad;
        this.packageName = packageNameToLoad;
        this.serviceUrl = serviceUrlToLoad;
        this.lastGeofenceUpdate = lastGeofenceUpdateToLoad;
        this.areGeofencesEnabled = areGeofencesEnabled;
        this.tags = tagsToLoad;
    }

    public FakePushPreferencesProvider(String gcmDeviceRegistrationIdToLoad,
                                       String pcfPushDeviceRegistrationIdToLoad,
                                       int appVersionToLoad,
                                       String gcmSenderIdToLoad,
                                       String platformUuidToLoad,
                                       String platformSecretToLoad,
                                       String deviceAliasToLoad,
                                       String packageNameToLoad,
                                       String serviceUrlToLoad,
                                       Set<String> tagsToLoad,
                                       long lastGeofenceUpdateToLoad,
                                       boolean areGeofencesEnabled,
                                       Version backEndVersion,
                                       Date backEndVersionTimePolled) {

        this.gcmDeviceRegistrationId = gcmDeviceRegistrationIdToLoad;
        this.pcfPushDeviceRegistrationId = pcfPushDeviceRegistrationIdToLoad;
        this.appVersion = appVersionToLoad;
        this.gcmSenderId = gcmSenderIdToLoad;
        this.platformUuid = platformUuidToLoad;
        this.platformSecret = platformSecretToLoad;
        this.deviceAlias = deviceAliasToLoad;
        this.packageName = packageNameToLoad;
        this.serviceUrl = serviceUrlToLoad;
        this.lastGeofenceUpdate = lastGeofenceUpdateToLoad;
        this.areGeofencesEnabled = areGeofencesEnabled;
        this.tags = tagsToLoad;
        this.backEndVersion = backEndVersion;
        this.backEndVersionTimePolled = backEndVersionTimePolled;
    }

    @Override
    public String getGcmDeviceRegistrationId() {
        return gcmDeviceRegistrationId;
    }

    @Override
    public String getPCFPushDeviceRegistrationId() {
        return pcfPushDeviceRegistrationId;
    }

    @Override
    public int getAppVersion() {
        return appVersion;
    }

    @Override
    public String getGcmSenderId() {
        return gcmSenderId;
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
    public Map<String, String> getRequestHeaders()
    {
        return requestHeaders;
    }

    @Override
    public Version getBackEndVersion() {
        return backEndVersion;
    }

    @Override
    public Date getBackEndVersionTimePolled() {
        return backEndVersionTimePolled;
    }

    @Override
    public void setPCFPushDeviceRegistrationId(String pcfPushDeviceRegistrationId) {
        this.pcfPushDeviceRegistrationId = pcfPushDeviceRegistrationId;
        wasPCFPushDeviceRegistrationIdSaved = true;
    }

    @Override
    public void setAppVersion(int appVersion) {
        this.appVersion = appVersion;
        wasAppVersionSaved = true;
    }

    @Override
    public void setGcmSenderId(String gcmSenderId) {
        this.gcmSenderId = gcmSenderId;
        wasGcmSenderIdSaved = true;
    }

    @Override
    public void setGcmDeviceRegistrationId(String gcmDeviceRegistrationId) {
        this.gcmDeviceRegistrationId = gcmDeviceRegistrationId;
        wasGcmDeviceRegistrationIdSaved = true;
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
    public void setRequestHeaders(Map<String, String> requestHeaders)
    {
        this.requestHeaders = requestHeaders;
    }

    @Override
    public void setBackEndVersion(Version version) {
        this.backEndVersion = version;
    }

    @Override
    public void setBackEndVersionTimePolled(Date timestamp) {
        this.backEndVersionTimePolled = timestamp;
    }

    @Override
    public boolean areAnalyticsEnabled() {
        return areAnalyticsEnabled;
    }

    public boolean wasGcmDeviceRegistrationIdSaved() {
        return wasGcmDeviceRegistrationIdSaved;
    }

    public boolean wasPCFPushDeviceRegistrationIdSaved() {
        return wasPCFPushDeviceRegistrationIdSaved;
    }

    public boolean wasAppVersionSaved() {
        return wasAppVersionSaved;
    }

    public boolean wasGcmSenderIdSaved() {
        return wasGcmSenderIdSaved;
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
