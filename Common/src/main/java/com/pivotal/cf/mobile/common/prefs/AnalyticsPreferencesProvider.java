package com.pivotal.cf.mobile.common.prefs;

import java.net.URL;

public interface AnalyticsPreferencesProvider {

    URL getBaseServerUrl();

    void setBaseServerUrl(URL baseServerUrl);

    boolean isAnalyticsEnabled();

    void setIsAnalyticsEnabled(boolean isAnalyticsEnabled);
}
