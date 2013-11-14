package com.gopivotal.pushlib.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.gopivotal.pushlib.util.Const;

public class RealPreferencesProvider implements PreferencesProvider {

    private static final String PROPERTY_GCM_DEVICE_REGISTRATION_ID = "gcm_device_registration_id";
    private static final String PROPERTY_BACKEND_DEVICE_REGISTRATION_ID = "backend_device_registration_id";
    private static final String PROPERTY_APP_VERSION = "app_version";

    private final Context context;

    public RealPreferencesProvider(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
        this.context = context;
    }

    @Override
    public String loadGcmDeviceRegistrationId() {
        final SharedPreferences prefs = getSharedPreferences();
        return prefs.getString(PROPERTY_GCM_DEVICE_REGISTRATION_ID, null);
    }

    @Override
    public void saveGcmDeviceRegistrationId(String gcmDeviceRegistrationId) {
        final SharedPreferences prefs = getSharedPreferences();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_GCM_DEVICE_REGISTRATION_ID, gcmDeviceRegistrationId);
        editor.commit();
    }

    @Override
    public String loadBackEndDeviceRegistrationId() {
        final SharedPreferences prefs = getSharedPreferences();
        return prefs.getString(PROPERTY_BACKEND_DEVICE_REGISTRATION_ID, null);
    }

    @Override
    public void saveBackEndDeviceRegistrationId(String backendDeviceRegistrationId) {
        final SharedPreferences prefs = getSharedPreferences();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_BACKEND_DEVICE_REGISTRATION_ID, backendDeviceRegistrationId);
        editor.commit();
    }

    @Override
    public int loadAppVersion() {
        final SharedPreferences prefs = getSharedPreferences();
        return prefs.getInt(PROPERTY_APP_VERSION, -1);
    }

    @Override
    public void saveAppVersion(int appVersion) {
        final SharedPreferences prefs = getSharedPreferences();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    private SharedPreferences getSharedPreferences() {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return context.getSharedPreferences(Const.TAG_NAME, Context.MODE_PRIVATE);
    }

}
