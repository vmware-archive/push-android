/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import io.pivotal.android.push.geofence.GeofenceEngine;
import io.pivotal.android.push.version.Version;

/**
 * Saves preferences to the SharedPreferences on the filesystem.
 */
public class PushPreferencesProviderImpl implements PushPreferencesProvider {

    public static final String TAG_NAME = "PivotalCFMSPush";

    // If you add or change any of these strings, then please also update their copies in the
    // sample app's MainActivity::clearRegistration method.
    private static final String PROPERTY_FCM_TOKEN_ID = "fcm_token_id";
    private static final String PROPERTY_PCF_PUSH_DEVICE_REGISTRATION_ID = "backend_device_registration_id";
    private static final String PROPERTY_PLATFORM_UUID = "variant_uuid";
    private static final String PROPERTY_PLATFORM_SECRET = "variant_secret";
    private static final String PROPERTY_DEVICE_ALIAS = "device_alias";
    private static final String PROPERTY_PACKAGE_NAME = "package_name";
    private static final String PROPERTY_SERVICE_URL = "base_server_url";
    private static final String PROPERTY_TAGS = "tags";
    private static final String PROPERTY_GEOFENCE_UPDATE = "geofence_update";
    private static final String PROPERTY_ARE_GEOFENCES_ENABLED = "are_geofences_enabled";
    private static final String PROPERTY_BACK_END_VERSION = "back_end_version";
    private static final String PROPERTY_BACK_END_VERSION_TIME_POLLED = "back_end_version_time_polled";
    private static final String PROPERTY_CUSTOM_USER_ID = "custom_user_id";

    private static final Version BACKEND_VERSION_WITH_ANALYTICS = new Version("1.3.2");

    private final Context context;

    public PushPreferencesProviderImpl(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
        this.context = context;
    }

    public void clear() {
        getSharedPreferences().edit().clear().commit();
    }

    @Override
    public String getFcmTokenId() {
        return getSharedPreferences().getString(PROPERTY_FCM_TOKEN_ID, null);
    }

    @Override
    public void setFcmTokenId(String fcmTokenId) {
        final SharedPreferences prefs = getSharedPreferences();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_FCM_TOKEN_ID, fcmTokenId);
        editor.commit();
    }

    @Override
    public String getPCFPushDeviceRegistrationId() {
        return getSharedPreferences().getString(PROPERTY_PCF_PUSH_DEVICE_REGISTRATION_ID, null);
    }

    @Override
    public void setPCFPushDeviceRegistrationId(String pcfPushDeviceRegistrationId) {
        final SharedPreferences prefs = getSharedPreferences();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_PCF_PUSH_DEVICE_REGISTRATION_ID, pcfPushDeviceRegistrationId);
        editor.commit();
    }

    @Override
    public String getPlatformUuid() {
        return getSharedPreferences().getString(PROPERTY_PLATFORM_UUID, null);
    }

    @Override
    public void setPlatformUuid(String platformUuid) {
        final SharedPreferences prefs = getSharedPreferences();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_PLATFORM_UUID, platformUuid);
        editor.commit();
    }

    @Override
    public String getPlatformSecret() {
        return getSharedPreferences().getString(PROPERTY_PLATFORM_SECRET, null);
    }

    @Override
    public void setPlatformSecret(String platformSecret) {
        final SharedPreferences prefs = getSharedPreferences();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_PLATFORM_SECRET, platformSecret);
        editor.commit();
    }

    @Override
    public String getDeviceAlias() {
        return getSharedPreferences().getString(PROPERTY_DEVICE_ALIAS, null);
    }

    @Override
    public void setDeviceAlias(String deviceAlias) {
        final SharedPreferences prefs = getSharedPreferences();
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_DEVICE_ALIAS, deviceAlias);
        editor.commit();
    }

    @Override
    public String getPackageName() {
        return getSharedPreferences().getString(PROPERTY_PACKAGE_NAME, null);
    }

    @Override
    public void setPackageName(String packageName) {
        final SharedPreferences prefs = getSharedPreferences();
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_PACKAGE_NAME, packageName);
        editor.commit();
    }

    @Override
    public String getServiceUrl() {
        return getSharedPreferences().getString(PROPERTY_SERVICE_URL, null);
    }

    @Override
    public void setServiceUrl(String serviceUrl) {
        final SharedPreferences prefs = getSharedPreferences();
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_SERVICE_URL, serviceUrl);
        editor.commit();
    }

    @Override
    public Set<String> getTags() {
        return getSharedPreferences().getStringSet(PROPERTY_TAGS, new HashSet<String>());
    }

    @Override
    public void setTags(Set<String> tags) {
        final SharedPreferences prefs = getSharedPreferences();
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(PROPERTY_TAGS, tags);
        editor.commit();
    }

    @Override
    public long getLastGeofenceUpdate() {
        return getSharedPreferences().getLong(PROPERTY_GEOFENCE_UPDATE, GeofenceEngine.NEVER_UPDATED_GEOFENCES);
    }

    @Override
    public void setLastGeofenceUpdate(long timestamp) {
        final SharedPreferences prefs = getSharedPreferences();
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(PROPERTY_GEOFENCE_UPDATE, timestamp);
        editor.commit();
    }

    @Override
    public boolean areGeofencesEnabled() {
        return getSharedPreferences().getBoolean(PROPERTY_ARE_GEOFENCES_ENABLED, false);
    }

    @Override
    public void setAreGeofencesEnabled(boolean areGeofencesEnabled) {
        final SharedPreferences prefs = getSharedPreferences();
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PROPERTY_ARE_GEOFENCES_ENABLED, areGeofencesEnabled);
        editor.commit();
    }

    @Override
    public Version getBackEndVersion() {
        final String s = getSharedPreferences().getString(PROPERTY_BACK_END_VERSION, null);
        if (s != null) {
            return new Version(s);
        } else {
            return null;
        }
    }

    @Override
    public void setBackEndVersion(Version version) {
        final SharedPreferences prefs = getSharedPreferences();
        final SharedPreferences.Editor editor = prefs.edit();
        if (version != null) {
            editor.putString(PROPERTY_BACK_END_VERSION, version.toString());
        } else {
            editor.remove(PROPERTY_BACK_END_VERSION);
        }
        editor.commit();
    }

    @Override
    public Date getBackEndVersionTimePolled() {
        return new Date(getSharedPreferences().getLong(PROPERTY_BACK_END_VERSION_TIME_POLLED, 0));
    }

    @Override
    public void setBackEndVersionTimePolled(Date timestamp) {
        final SharedPreferences prefs = getSharedPreferences();
        final SharedPreferences.Editor editor = prefs.edit();
        if (timestamp != null) {
            editor.putLong(PROPERTY_BACK_END_VERSION_TIME_POLLED, timestamp.getTime());
        } else {
            editor.remove(PROPERTY_BACK_END_VERSION_TIME_POLLED);
        }
        editor.commit();
    }

    public String getCustomUserId() {
        return getSharedPreferences().getString(PROPERTY_CUSTOM_USER_ID, null);
    }

    public void setCustomUserId(String customUserId) {
        final SharedPreferences prefs = getSharedPreferences();
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_CUSTOM_USER_ID, customUserId);
        editor.commit();
    }

    @Override
    public boolean areAnalyticsEnabled() {
        if (!Pivotal.getAreAnalyticsEnabled(context)) {
            return false;
        } else {
            final Version backendVersion = getBackEndVersion();
            if (backendVersion == null) {
                return false;
            } else {
                return backendVersion.compareTo(BACKEND_VERSION_WITH_ANALYTICS) >= 0;
            }
        }
    }

    private SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences(TAG_NAME, Context.MODE_PRIVATE);
    }
}