package com.gopivotal.pushlib;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;

import java.util.List;

public class SettingsActivity extends PreferenceActivity {

    public static final String PREFERENCE_GCM_SENDER_ID = "test_gcm_sender_id";
    public static final String PREFERENCE_RELEASE_UUID = "test_release_uuid";
    public static final String PREFERENCE_RELEASE_SECRET = "test_release_secret";
    public static final String PREFERENCE_DEVICE_ALIAS = "test_device_alias";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        addPreferencesFromResource(R.xml.pref_general);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    //    Preference pref = findPreference(key);

    //    if (pref instanceof EditTextPreference) {
    //        final EditTextPreference editTextPreference = (EditTextPreference) pref;
    //        pref.setSummary(editTextPreference.getEntry());
    //    }
    //}

    //public static String getGcmSenderId(Context context) {
    //    PreferencesManager pm = (PreferencesMana)
    //    return context.getDe
    //}

}
