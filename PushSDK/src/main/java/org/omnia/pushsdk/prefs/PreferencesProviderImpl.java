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

package org.omnia.pushsdk.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import org.omnia.pushsdk.util.Const;

/**
 * Saves preferences to the SharedPreferences on the filesystem.
 */
public class PreferencesProviderImpl implements PreferencesProvider {

    // If you add or change any of these strings, then please also update their copies in the
    // sample app's MainActivity::clearRegistration method.
    private static final String PROPERTY_GCM_DEVICE_REGISTRATION_ID = "gcm_device_registration_id";
    private static final String PROPERTY_BACKEND_DEVICE_REGISTRATION_ID = "backend_device_registration_id";
    private static final String PROPERTY_APP_VERSION = "app_version";
    private static final String PROPERTY_GCM_SENDER_ID = "gcm_sender_id";
    private static final String PROPERTY_RELEASE_UUID = "release_uuid";
    private static final String PROPERTY_RELEASE_SECRET = "release_secret";
    private static final String PROPERTY_DEVICE_ALIAS = "device_alias";
    private static final String PROPERTY_PACKAGE_NAME = "package_name";

    private final Context context;

    public PreferencesProviderImpl(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
        this.context = context;
    }

    @Override
    public String loadGcmDeviceRegistrationId() {
        return getSharedPreferences().getString(PROPERTY_GCM_DEVICE_REGISTRATION_ID, null);
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
        return getSharedPreferences().getString(PROPERTY_BACKEND_DEVICE_REGISTRATION_ID, null);
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
        return getSharedPreferences().getInt(PROPERTY_APP_VERSION, NO_SAVED_VERSION);
    }

    @Override
    public void saveAppVersion(int appVersion) {
        final SharedPreferences prefs = getSharedPreferences();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    @Override
    public String loadGcmSenderId() {
        return getSharedPreferences().getString(PROPERTY_GCM_SENDER_ID, null);
    }

    @Override
    public void saveGcmSenderId(String gcmSenderId) {
        final SharedPreferences prefs = getSharedPreferences();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_GCM_SENDER_ID, gcmSenderId);
        editor.commit();
    }

    @Override
    public String loadReleaseUuid() {
        return getSharedPreferences().getString(PROPERTY_RELEASE_UUID, null);
    }

    @Override
    public void saveReleaseUuid(String releaseUuid) {
        final SharedPreferences prefs = getSharedPreferences();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_RELEASE_UUID, releaseUuid);
        editor.commit();
    }

    @Override
    public String loadReleaseSecret() {
        return getSharedPreferences().getString(PROPERTY_RELEASE_SECRET, null);
    }

    @Override
    public void saveReleaseSecret(String releaseUuid) {
        final SharedPreferences prefs = getSharedPreferences();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_RELEASE_SECRET, releaseUuid);
        editor.commit();
    }

    @Override
    public String loadDeviceAlias() {
        return getSharedPreferences().getString(PROPERTY_DEVICE_ALIAS, null);
    }

    @Override
    public void saveDeviceAlias(String deviceAlias) {
        final SharedPreferences prefs = getSharedPreferences();
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_DEVICE_ALIAS, deviceAlias);
        editor.commit();
    }

    @Override
    public String loadPackageName() {
        return getSharedPreferences().getString(PROPERTY_PACKAGE_NAME, null);
    }

    @Override
    public void savePackageName(String packageName) {
        final SharedPreferences prefs = getSharedPreferences();
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_PACKAGE_NAME, packageName);
        editor.commit();
    }

    private SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences(Const.TAG_NAME, Context.MODE_PRIVATE);
    }
}
