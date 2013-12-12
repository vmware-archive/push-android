/* Copyright (c) 2013 Pivotal Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.omnia.pushsdk.activity;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;

import org.omnia.pushsdk.R;
import org.omnia.pushsdk.util.Settings;

public class SettingsActivity extends PreferenceActivity {

    private EditTextPreference gcmSenderIdPreference;
    private EditTextPreference releaseUuidPreference;
    private EditTextPreference releaseSecretPreference;
    private EditTextPreference deviceAliasPreference;
    private EditTextPreference gcmBrowserApiPreference;
    private EditTextPreference backEndAppUuidPreference;
    private EditTextPreference backEndAppSecretKeyPreference;
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        // NOTE - many of the method calls in this class show up as deprecated.  However, I still want my
        // app to run on old Android versions, so I'm going to leave them in here.
        addPreferencesFromResource(R.xml.preferences);
        gcmSenderIdPreference = (EditTextPreference) getPreferenceScreen().findPreference(Settings.GCM_SENDER_ID);
        releaseUuidPreference = (EditTextPreference) getPreferenceScreen().findPreference(Settings.RELEASE_UUID);
        releaseSecretPreference = (EditTextPreference) getPreferenceScreen().findPreference(Settings.RELEASE_SECRET);
        deviceAliasPreference = (EditTextPreference) getPreferenceScreen().findPreference(Settings.DEVICE_ALIAS);
        gcmBrowserApiPreference = (EditTextPreference) getPreferenceScreen().findPreference(Settings.GCM_BROWSER_API_KEY);
        backEndAppUuidPreference = (EditTextPreference) getPreferenceScreen().findPreference(Settings.BACK_END_APP_UUID);
        backEndAppSecretKeyPreference = (EditTextPreference) getPreferenceScreen().findPreference(Settings.BACK_END_APP_SECRET_KEY);
        preferenceChangeListener = getPreferenceChangeListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showCurrentPreferences();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            final ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    private SharedPreferences.OnSharedPreferenceChangeListener getPreferenceChangeListener() {
        return new SharedPreferences.OnSharedPreferenceChangeListener() {

            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                showCurrentPreferences();
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        } else if (id == R.id.action_reset_preferences) {
            resetPreferencesToDefault();
        }
        return super.onOptionsItemSelected(item);
    }

    private void resetPreferencesToDefault() {
        final SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();
        prefs.edit().clear().commit();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
        showCurrentPreferences();
    }

    private void showCurrentPreferences() {
        final SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();
        gcmSenderIdPreference.setSummary(prefs.getString(Settings.GCM_SENDER_ID, null));
        releaseUuidPreference.setSummary(prefs.getString(Settings.RELEASE_UUID, null));
        releaseSecretPreference.setSummary(prefs.getString(Settings.RELEASE_SECRET, null));
        deviceAliasPreference.setSummary(prefs.getString(Settings.DEVICE_ALIAS, null));
        gcmBrowserApiPreference.setSummary(prefs.getString(Settings.GCM_BROWSER_API_KEY, null));
        backEndAppUuidPreference.setSummary(prefs.getString(Settings.BACK_END_APP_UUID, null));
        backEndAppSecretKeyPreference.setSummary(prefs.getString(Settings.BACK_END_APP_SECRET_KEY, null));
    }
}
