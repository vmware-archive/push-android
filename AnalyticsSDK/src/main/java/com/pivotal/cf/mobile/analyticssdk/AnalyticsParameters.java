package com.pivotal.cf.mobile.analyticssdk;

import java.net.URL;

/**
 * Parameters used to generate analytics for Pivotal CF Mobile Services
 */
public class AnalyticsParameters {

    private final URL baseServerUrl;

    public AnalyticsParameters(URL baseServerUrl) {
        this.baseServerUrl = baseServerUrl;
    }

    public URL getBaseServerUrl() {
        return baseServerUrl;
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
        result = (result * 31) + (baseServerUrl == null ? 0 : baseServerUrl.hashCode());
        return result;
    }
}
