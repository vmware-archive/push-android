package com.gopivotal.pushlib.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.gopivotal.pushlib.Const;
import com.xtreme.commons.Logger;

public class RealPreferencesProvider implements PreferencesProvider {

    private static final String PROPERTY_DEVICE_REGISTRATION_ID = "device_registration_id";
    private static final String PROPERTY_APP_VERSION = "app_version";

    private final Context context;

    public RealPreferencesProvider(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
        this.context = context;
    }

    @Override
    public String loadDeviceRegistrationId() {
        final SharedPreferences prefs = getGCMPreferences();
        final String deviceRegistrationId = prefs.getString(PROPERTY_DEVICE_REGISTRATION_ID, "");
        if (deviceRegistrationId.isEmpty()) {
            Logger.i("Device Registration ID not found. Device registration with GCM will be required.");
            return null;
        }

        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        final int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        final int currentVersion = getAppVersion();
        if (registeredVersion != currentVersion) {
            Logger.i("App version changed. Registration will be required.");
            return null;
        }
        return deviceRegistrationId;
    }


    @Override
    public void saveDeviceRegistrationId(String registrationId) {
        final SharedPreferences prefs = getGCMPreferences();
        final int appVersion = getAppVersion();
        Logger.i("Saving device registration ID for app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_DEVICE_REGISTRATION_ID, registrationId);
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

    private SharedPreferences getGCMPreferences() {
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
