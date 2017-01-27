package io.pivotal.android.push;


import android.support.annotation.NonNull;

import static com.google.gson.internal.$Gson$Preconditions.checkArgument;

/**
 * Push Platform information required for various network calls.
 */
public class PushPlatformInfo {
    private String baseServerUrl;
    private String platformUuid;
    private String platformSecret;

    /**
     * Sets up platform information used by the Pivotal CF Mobile Services Push SDK.
     *
     * @param baseServerUrl The API URL as defined by Push Notification Service for PCF for your platform.
     *                      The url must include the "http://" or "https://" prefix.
     * @param platformUuid The platform UUID as defined by Push Notification Service for PCF for your platform.
     * @param platformSecret The platform secret as defined by Push Notification Service for PCF for your platform.
     */
    public PushPlatformInfo(@NonNull final String baseServerUrl,
                            @NonNull final String platformUuid,
                            @NonNull final String platformSecret) {
        checkArgument(baseServerUrl != null);
        checkArgument(platformUuid != null);
        checkArgument(platformSecret != null);
        checkArgument(baseServerUrl.startsWith("https://") | baseServerUrl.startsWith("http://"));

        this.baseServerUrl = baseServerUrl;
        this.platformUuid = platformUuid;
        this.platformSecret = platformSecret;
    }

    public String getBaseServerUrl() {
        return baseServerUrl;
    }

    public String getPlatformUuid() {
        return platformUuid;
    }

    public String getPlatformSecret() {
        return platformSecret;
    }
}
