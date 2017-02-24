package io.pivotal.android.push.prefs;


import android.content.Context;
import android.content.SharedPreferences;
import io.pivotal.android.push.geofence.GeofenceEngine;
import io.pivotal.android.push.prefs.Pivotal.SslCertValidationMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PushPreferences {
    private static final String TAG_NAME = "PivotalCFMSPush";

    // If you add or change any of these strings, then please also update their copies in the
    // sample app's MainActivity::clearRegistration method.
    protected static final String PROPERTY_PCF_PUSH_DEVICE_REGISTRATION_ID = "backend_device_registration_id";
    protected static final String PROPERTY_PLATFORM_UUID = "variant_uuid";
    protected static final String PROPERTY_PLATFORM_SECRET = "variant_secret";
    protected static final String PROPERTY_DEVICE_ALIAS = "device_alias";
    protected static final String PROPERTY_PACKAGE_NAME = "package_name";
    protected static final String PROPERTY_SERVICE_URL = "base_server_url";
    protected static final String PROPERTY_TAGS = "tags";
    protected static final String PROPERTY_GEOFENCE_UPDATE = "geofence_update";
    protected static final String PROPERTY_ARE_GEOFENCES_ENABLED = "are_geofences_enabled";
    protected static final String PROPERTY_CUSTOM_USER_ID = "custom_user_id";
    protected static final String PROPERTY_ARE_ANALYTICS_ENABLED = "are_analytics_enabled";
    protected static final String PROPERTY_SSL_CERT_VALIDATION_MODE = "ssl_cert_validation_mode";
    protected static final String PROPERTY_PINNED_CERTIFICATE_NAMES = "pinned_certificate_names";

    private final Context context;

    public PushPreferences(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
        this.context = context;
    }

    public void clear() {
        getSharedPreferences().edit().clear().commit();
    }

    public String getPCFPushDeviceRegistrationId() {
        return getSharedPreferences().getString(PROPERTY_PCF_PUSH_DEVICE_REGISTRATION_ID, null);
    }

    public void setPCFPushDeviceRegistrationId(String pcfPushDeviceRegistrationId) {
        saveSharedPreferenceString(PROPERTY_PCF_PUSH_DEVICE_REGISTRATION_ID, pcfPushDeviceRegistrationId);
    }

    public String getPlatformUuid() {
        return getSharedPreferences().getString(PROPERTY_PLATFORM_UUID, null);
    }

    public void setPlatformUuid(String platformUuid) {
        saveSharedPreferenceString(PROPERTY_PLATFORM_UUID, platformUuid);
    }

    public String getPlatformSecret() {
        return getSharedPreferences().getString(PROPERTY_PLATFORM_SECRET, null);
    }

    public void setPlatformSecret(String platformSecret) {
        saveSharedPreferenceString(PROPERTY_PLATFORM_SECRET, platformSecret);

    }

    public String getDeviceAlias() {
        return getSharedPreferences().getString(PROPERTY_DEVICE_ALIAS, null);
    }

    public void setDeviceAlias(String deviceAlias) {
        saveSharedPreferenceString(PROPERTY_DEVICE_ALIAS, deviceAlias);
    }

    public String getPackageName() {
        return getSharedPreferences().getString(PROPERTY_PACKAGE_NAME, null);
    }

    public void setPackageName(String packageName) {
        saveSharedPreferenceString(PROPERTY_PACKAGE_NAME, packageName);
    }

    public String getServiceUrl() {
        return getSharedPreferences().getString(PROPERTY_SERVICE_URL, null);
    }

    public void setServiceUrl(String serviceUrl) {
        final SharedPreferences prefs = getSharedPreferences();
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_SERVICE_URL, serviceUrl);
        editor.commit();
    }

    public Set<String> getTags() {
        return getSharedPreferences().getStringSet(PROPERTY_TAGS, new HashSet<String>());
    }

    public void setTags(Set<String> tags) {
        final SharedPreferences prefs = getSharedPreferences();
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(PROPERTY_TAGS, tags);
        editor.commit();
    }

    public long getLastGeofenceUpdate() {
        return getSharedPreferences()
            .getLong(PROPERTY_GEOFENCE_UPDATE, GeofenceEngine.NEVER_UPDATED_GEOFENCES);
    }

    public void setLastGeofenceUpdate(long timestamp) {
        final SharedPreferences prefs = getSharedPreferences();
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(PROPERTY_GEOFENCE_UPDATE, timestamp);
        editor.commit();
    }

    public boolean areGeofencesEnabled() {
        return getSharedPreferences().getBoolean(PROPERTY_ARE_GEOFENCES_ENABLED, false);
    }

    public void setAreGeofencesEnabled(boolean areGeofencesEnabled) {
        final SharedPreferences prefs = getSharedPreferences();
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PROPERTY_ARE_GEOFENCES_ENABLED, areGeofencesEnabled);
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

    public void setAreAnalyticsEnabled(boolean areAnalyticsEnabled) {
        final SharedPreferences prefs = getSharedPreferences();
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PROPERTY_ARE_ANALYTICS_ENABLED, areAnalyticsEnabled);

        editor.commit();
    }

    public boolean areAnalyticsEnabled() {
        return getSharedPreferences().getBoolean(PROPERTY_ARE_ANALYTICS_ENABLED, true);
    }

    public void setSslCertValidationMode(Pivotal.SslCertValidationMode validationMode) {
        final SharedPreferences prefs = getSharedPreferences();
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_SSL_CERT_VALIDATION_MODE, validationMode.toString());

        editor.commit();
    }

    public Pivotal.SslCertValidationMode getSslCertValidationMode() {
        final String stringValue = getSharedPreferences()
            .getString(PROPERTY_SSL_CERT_VALIDATION_MODE, SslCertValidationMode.DEFAULT.toString());

        return Pivotal.SslCertValidationMode.valueOf(stringValue);
    }

    public void setPinnedCertificateNames(final List<String> certificateNames) {
        final SharedPreferences prefs = getSharedPreferences();
        final SharedPreferences.Editor editor = prefs.edit();

        editor.putStringSet(PROPERTY_PINNED_CERTIFICATE_NAMES, new HashSet<>(certificateNames));
        editor.commit();
    }

    public List<String> getPinnedCertificateNames() {
        final Set<String> stringSet = getSharedPreferences().getStringSet(PROPERTY_PINNED_CERTIFICATE_NAMES, new HashSet<String>());

        return new ArrayList<>(stringSet);
    }

    SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences(TAG_NAME, Context.MODE_PRIVATE);
    }


    private void saveSharedPreferenceString(final String key, final String value) {
        final SharedPreferences prefs = getSharedPreferences();
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }
}