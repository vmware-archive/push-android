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

package com.pivotal.cf.mobile.analyticssdk.sample.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Preferences {

    public static final String IS_ANALYTICS_ENABLED = "test_is_analytics_enabled";
    public static final String ANALYTICS_BASE_SERVER_URL = "test_analytics_base_server_url";

    public static final String[] PREFERENCE_NAMES = {
            IS_ANALYTICS_ENABLED,
            ANALYTICS_BASE_SERVER_URL
    };

    public static boolean isAnalyticsEnabled(Context context) {
        return getSharedPreferences(context).getBoolean(IS_ANALYTICS_ENABLED, true);
    }

    public static String getAnalyticsBaseServerUrl(Context context) {
        return getSharedPreferences(context).getString(ANALYTICS_BASE_SERVER_URL, null);
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
