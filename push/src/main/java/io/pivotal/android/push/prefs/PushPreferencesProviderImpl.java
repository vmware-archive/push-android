/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.prefs;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import java.util.HashSet;
import java.util.Set;

/**
 * Saves preferences to the SharedPreferences on the filesystem.
 */
public class PushPreferencesProviderImpl implements PushPreferencesProvider {

    public static final String TAG_NAME = "PivotalCFMSPush";

    // If you add or change any of these strings, then please also update their copies in the
    // sample app's MainActivity::clearRegistration method.
    private static final String PROPERTY_GCM_DEVICE_REGISTRATION_ID = "gcm_device_registration_id";
    private static final String PROPERTY_PCF_PUSH_DEVICE_REGISTRATION_ID = "backend_device_registration_id";
    private static final String PROPERTY_APP_VERSION = "app_version";
    private static final String PROPERTY_GCM_SENDER_ID = "gcm_sender_id";
    private static final String PROPERTY_PLATFORM_UUID = "variant_uuid";
    private static final String PROPERTY_PLATFORM_SECRET = "variant_secret";
    private static final String PROPERTY_DEVICE_ALIAS = "device_alias";
    private static final String PROPERTY_PACKAGE_NAME = "package_name";
    private static final String PROPERTY_SERVICE_URL = "base_server_url";
    private static final String PROPERTY_TAGS = "tags";

    private final Context context;

    public PushPreferencesProviderImpl(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
        this.context = context;
    }

    @Override
    public String getGcmDeviceRegistrationId() {
        return getSharedPreferences().getString(PROPERTY_GCM_DEVICE_REGISTRATION_ID, null);
    }

    @Override
    public void setGcmDeviceRegistrationId(String gcmDeviceRegistrationId) {
        final SharedPreferences prefs = getSharedPreferences();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_GCM_DEVICE_REGISTRATION_ID, gcmDeviceRegistrationId);
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
    public int getAppVersion() {
        return getSharedPreferences().getInt(PROPERTY_APP_VERSION, NO_SAVED_VERSION);
    }

    @Override
    public void setAppVersion(int appVersion) {
        final SharedPreferences prefs = getSharedPreferences();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    @Override
    public String getGcmSenderId() {
        return getSharedPreferences().getString(PROPERTY_GCM_SENDER_ID, null);
    }

    @Override
    public void setGcmSenderId(String gcmSenderId) {
        final SharedPreferences prefs = getSharedPreferences();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_GCM_SENDER_ID, gcmSenderId);
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

    private SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences(TAG_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public void setTags(Set<String> tags) {
        final SharedPreferences prefs = getSharedPreferences();
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(PROPERTY_TAGS, tags);
        editor.commit();
    }
}