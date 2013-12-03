package com.gopivotal.pushlib;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Settings {

    public static final String GCM_SENDER_ID = "test_gcm_sender_id";
    public static final String RELEASE_UUID = "test_release_uuid";
    public static final String RELEASE_SECRET = "test_release_secret";
    public static final String DEVICE_ALIAS = "test_device_alias";
    public static final String GCM_BROWSER_API_KEY = "test_gcm_browser_api_key";
    public static final String BACK_END_APP_UUID = "test_app_uuid";
    public static final String BACK_END_APP_SECRET_KEY = "test_app_secret_key";

    public static String getGcmSenderId(Context context) {
        return getSharedPreferences(context).getString(GCM_SENDER_ID, null);
    }

    public static String getReleaseUuid(Context context) {
        return getSharedPreferences(context).getString(RELEASE_UUID, null);
    }

    public static String getReleaseSecret(Context context) {
        return getSharedPreferences(context).getString(RELEASE_SECRET, null);
    }

    public static String getDeviceAlias(Context context) {
        return getSharedPreferences(context).getString(DEVICE_ALIAS, null);
    }

    public static String getGcmBrowserApiKey(Context context) {
        return getSharedPreferences(context).getString(GCM_BROWSER_API_KEY, null);
    }

    public static String getBackEndAppUuid(Context context) {
        return getSharedPreferences(context).getString(BACK_END_APP_UUID, null);
    }

    public static String getBackEndAppSecretKey(Context context) {
        return getSharedPreferences(context).getString(BACK_END_APP_SECRET_KEY, null);
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
