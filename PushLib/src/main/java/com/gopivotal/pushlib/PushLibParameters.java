package com.gopivotal.pushlib;

/**
 * Parameters used to initialize the Cloud Foundry Push SDK
 */
public class PushLibParameters {

    private final String gcmSenderId;
    private final String releaseUuid;
    private final String releaseSecret;

    /**
     * Sets up parameters used by the Cloud Foundry Push SDK
     *
     * @param gcmSenderId   The "sender" or "project ID", as defined by the Google Cloud Messaging
     * @param releaseUuid   The "release_uuid", as defined by Cloud Foundry Push Services for your release.
     * @param releaseSecret The "release secret", as defined by Cloud Foundry Push Services for your release.
     */
    public PushLibParameters(String gcmSenderId, String releaseUuid, String releaseSecret) {
        this.gcmSenderId = gcmSenderId;
        this.releaseUuid = releaseUuid;
        this.releaseSecret = releaseSecret;
    }

    public String getGcmSenderId() {
        return gcmSenderId;
    }

    public String getReleaseUuid() {
        return releaseUuid;
    }

    public String getReleaseSecret() {
        return releaseSecret;
    }
}
