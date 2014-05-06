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

package com.pivotal.cf.mobile.analyticssdk.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.pivotal.cf.mobile.common.util.Logger;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Saves preferences to the SharedPreferences on the filesystem.
 */
public class PreferencesProviderImpl implements PreferencesProvider {

    public static final String TAG_NAME = "PivotalCFMSAnalyticsSDK";

    // If you add or change any of these strings, then please also update their copies in the
    // sample app's MainActivity::clearRegistration method.
    private static final String PROPERTY_BASE_SERVER_URL = "base_server_url";

    private final Context context;

    public PreferencesProviderImpl(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
        this.context = context;
    }

    @Override
    public URL getBaseServerUrl() {
        final String setting = getSharedPreferences().getString(PROPERTY_BASE_SERVER_URL, null);
        if (setting == null) {
            return null;
        }
        try {
            return new URL(setting);
        } catch (MalformedURLException e) {
            Logger.w("Invalid base server URL stored in settings: " + setting);
            return null;
        }
    }

    @Override
    public void setBaseServerUrl(URL baseServerUrl) {
        final SharedPreferences prefs = getSharedPreferences();
        final SharedPreferences.Editor editor = prefs.edit();
        if (baseServerUrl != null) {
            editor.putString(PROPERTY_BASE_SERVER_URL, baseServerUrl.toString());
        } else {
            editor.putString(PROPERTY_BASE_SERVER_URL, null);
        }
        editor.commit();
    }

    private SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences(TAG_NAME, Context.MODE_PRIVATE);
    }
}
