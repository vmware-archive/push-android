package com.pivotal.cf.mobile.analyticssdk.prefs;

import java.net.URL;

public interface PreferencesProvider {

    URL getBaseServerUrl();

    void setBaseServerUrl(URL baseServerUrl);
}
