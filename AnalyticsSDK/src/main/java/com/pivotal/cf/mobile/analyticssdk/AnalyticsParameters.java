package com.pivotal.cf.mobile.analyticssdk;

import java.net.URL;

/**
 * Parameters used to generate analytics for Pivotal CF Mobile Services
 */
public class AnalyticsParameters {

    private final boolean isAnalyticsEnabled;
    private final URL baseServerUrl;

    public AnalyticsParameters(boolean isAnalyticsEnabled, URL baseServerUrl) {
        this.isAnalyticsEnabled = isAnalyticsEnabled;
        this.baseServerUrl = baseServerUrl;
    }

    public URL getBaseServerUrl() {
        return baseServerUrl;
    }

    public boolean isAnalyticsEnabled() {
        return isAnalyticsEnabled;
    }

    @Override
    public boolean equals(Object o) {

        if (o == null) {
            return false;
        }

        if (!(o instanceof AnalyticsParameters)) {
            return false;
        }

        AnalyticsParameters other = (AnalyticsParameters)o;

        if (isAnalyticsEnabled != other.isAnalyticsEnabled) {
            return false;
        }

        if (baseServerUrl == null && other.baseServerUrl != null) {
            return false;
        }
        if (baseServerUrl != null && other.baseServerUrl == null) {
            return false;
        }
        if (baseServerUrl != null && other.baseServerUrl != null && !other.baseServerUrl.equals(baseServerUrl)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = (result * 31) + (isAnalyticsEnabled ? 1 : 0);
        result = (result * 31) + (baseServerUrl == null ? 0 : baseServerUrl.hashCode());
        return result;
    }
}
