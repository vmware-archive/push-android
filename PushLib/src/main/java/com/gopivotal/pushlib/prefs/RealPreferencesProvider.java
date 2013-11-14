package com.gopivotal.pushlib.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.gopivotal.pushlib.Const;
import com.xtreme.commons.Logger;

public class RealPreferencesProvider implements PreferencesProvider {

    private static final String PROPERTY_GCM_DEVICE_REGISTRATION_ID = "gcm_device_registration_id";
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
        final String gcmDeviceRegistrationId = prefs.getString(PROPERTY_GCM_DEVICE_REGISTRATION_ID, "");
        if (gcmDeviceRegistrationId.isEmpty()) {
            Logger.i("Device Registration ID not found. Device registration with GCM will be required.");
            return null;
        }

        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        final int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        final int currentVersion = getAppVersion();
        if (registeredVersion != currentVersion) {
            Logger.i("App version changed. Device registration with GCM  will be required.");
            return null;
        }
        return gcmDeviceRegistrationId;
    }


    @Override
    public void saveGcmDeviceRegistrationId(String gcmDeviceRegistrationId) {
        final SharedPreferences prefs = getSharedPreferences();
        final int appVersion = getAppVersion();
        Logger.i("Saving GCM device registration ID for app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_GCM_DEVICE_REGISTRATION_ID, gcmDeviceRegistrationId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
        //PrintWriter p = null;
        //try {
        //    // TODO - save regid to app cache directory
        //    p = new PrintWriter("/mnt/sdcard/regid.txt");
        //    p.print(regId);
        //    p.close();
        //} catch (FileNotFoundException e) {
        //    e.printStackTrace();
        //}
    }

    private SharedPreferences getSharedPreferences() {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return context.getSharedPreferences(Const.TAG_NAME, Context.MODE_PRIVATE);
    }

    private int getAppVersion() {
        try {
            final PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
}
