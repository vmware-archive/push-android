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

package com.pivotal.cf.mobile.common.test.prefs;

import com.pivotal.cf.mobile.common.prefs.AnalyticsPreferencesProvider;

import java.net.URL;

public class FakeAnalyticsPreferencesProvider implements AnalyticsPreferencesProvider {

    private URL baseServerUrl;
    private boolean isAnalyticsEnabled;
    private boolean wasBaseServerUrlSaved = false;

    public FakeAnalyticsPreferencesProvider(boolean isAnalyticsEnabled, URL baseServerUrlToLoad) {
        this.isAnalyticsEnabled = isAnalyticsEnabled;
        this.baseServerUrl = baseServerUrlToLoad;
    }

    @Override
    public URL getBaseServerUrl() {
        return baseServerUrl;
    }

    @Override
    public void setBaseServerUrl(URL baseServerUrl) {
        this.baseServerUrl = baseServerUrl;
        wasBaseServerUrlSaved = true;
    }

    @Override
    public boolean isAnalyticsEnabled() {
        return isAnalyticsEnabled;
    }

    @Override
    public void setIsAnalyticsEnabled(boolean isAnalyticsEnabled) {
        this.isAnalyticsEnabled = isAnalyticsEnabled;
    }

    public boolean wasBaseServerUrlSaved() {
        return wasBaseServerUrlSaved;
    }
}
