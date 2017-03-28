package io.pivotal.android.push;

import java.util.ArrayList;
import java.util.List;

import io.pivotal.android.push.prefs.Pivotal;

import static com.google.gson.internal.$Gson$Preconditions.checkArgument;

/**
 * Push Platform information required for various network calls.
 */
public class PushServiceInfo {
    private final String serviceUrl;
    private final String platformUuid;
    private final String platformSecret;
    private final Pivotal.SslCertValidationMode sslCertValidationMode;
    private final List<String> pinnedSslCertificateNames;
    private final boolean areAnalyticsEnabled;

    /**
     * Sets up platform information used by the Pivotal CF Mobile Services Push SDK.
     *
     * @param serviceUrl The API URL as defined by Push Notification Service for PCF for your platform.
     *                      The url must include the "http://" or "https://" prefix.
     * @param platformUuid The platform UUID as defined by Push Notification Service for PCF for your platform.
     * @param platformSecret The platform secret as defined by Push Notification Service for PCF for your platform.
     */
    private PushServiceInfo(String serviceUrl, String platformUuid, String platformSecret, Pivotal.SslCertValidationMode sslCertValidationMode, List<String> pinnedSslCertificateNames, boolean areAnalyticsEnabled) {
        checkArgument(serviceUrl != null);
        checkArgument(platformUuid != null);
        checkArgument(platformSecret != null);

        this.serviceUrl = serviceUrl;
        this.platformUuid = platformUuid;
        this.platformSecret = platformSecret;
        this.sslCertValidationMode = sslCertValidationMode;
        this.pinnedSslCertificateNames = pinnedSslCertificateNames;
        this.areAnalyticsEnabled = areAnalyticsEnabled;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public String getPlatformUuid() {
        return platformUuid;
    }

    public String getPlatformSecret() {
        return platformSecret;
    }

    public Pivotal.SslCertValidationMode getSslCertValidationMode() {
        return sslCertValidationMode;
    }

    public List<String> getPinnedSslCertificateNames() {
        return pinnedSslCertificateNames;
    }

    public boolean areAnalyticsEnabled() {
        return areAnalyticsEnabled;
    }

    public static PushServiceInfoBuilder Builder() {
        return new PushServiceInfoBuilder();
    }

    public static class PushServiceInfoBuilder {
        private String serviceUrl;
        private String platformUuid;
        private String platformSecret;
        private Pivotal.SslCertValidationMode sslCertValidationMode;
        private List<String> pinnedSslCertificateNames;
        private Boolean areAnalyticsEnabled;

        private PushServiceInfoBuilder() {
            sslCertValidationMode = Pivotal.SslCertValidationMode.DEFAULT;
            areAnalyticsEnabled = true;
            pinnedSslCertificateNames = new ArrayList<>();
        }

        public PushServiceInfo build() {
            return new PushServiceInfo(serviceUrl,
                    platformUuid,
                    platformSecret,
                    sslCertValidationMode,
                    pinnedSslCertificateNames,
                    areAnalyticsEnabled);
        }

        public PushServiceInfoBuilder setServiceUrl(final String serviceUrl) {
            this.serviceUrl = serviceUrl;
            return this;
        }

        public PushServiceInfoBuilder setPlatformUuid(final String platformUuid) {
            this.platformUuid = platformUuid;
            return this;
        }

        public PushServiceInfoBuilder setPlatformSecret(final String platformSecret) {
            this.platformSecret = platformSecret;
            return this;
        }

        public PushServiceInfoBuilder setSSLCertValidationMode(final Pivotal.SslCertValidationMode sslCertValidationMode) {
            this.sslCertValidationMode = sslCertValidationMode;
            return this;
        }

        public PushServiceInfoBuilder setPinnedCertificateNames(final List<String> pinnedSslCertificateNames) {
            this.pinnedSslCertificateNames = pinnedSslCertificateNames;
            return this;
        }

        public PushServiceInfoBuilder setAnalyticsEnabled(final Boolean areAnalyticsEnabled) {
            this.areAnalyticsEnabled = areAnalyticsEnabled;
            return this;
        }
    }
}
