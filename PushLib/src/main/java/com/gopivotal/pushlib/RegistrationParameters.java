package com.gopivotal.pushlib;

/**
 * Parameters used to initialize the Cloud Foundry Push SDK
 */
public class RegistrationParameters {

    private final String gcmSenderId;
    private final String releaseUuid;
    private final String releaseSecret;
    private final String deviceAlias;

    /**
     * Sets up parameters used by the Cloud Foundry Push SDK
     *
     * @param gcmSenderId   The "sender" or "project ID", as defined by the Google Cloud Messaging
     * @param releaseUuid   The "release_uuid", as defined by Cloud Foundry Push Services for your release.
     * @param releaseSecret The "release secret", as defined by Cloud Foundry Push Services for your release.
     * @param deviceAlias   A developer-defined "device alias" which can be used to designate this device, or class
     *                      of devices, in push or notification campaigns. Optional. Set to `null` if not you are
     *                      not using the deviceAlias.
     */
    public RegistrationParameters(String gcmSenderId, String releaseUuid, String releaseSecret, String deviceAlias) {
        this.gcmSenderId = gcmSenderId;
        this.releaseUuid = releaseUuid;
        this.releaseSecret = releaseSecret;
        this.deviceAlias = deviceAlias;
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

    public String getDeviceAlias() {
        return deviceAlias;
    }

    @Override
    public boolean equals(Object o) {

        if (o == null) {
            return false;
        }

        if (!(o instanceof RegistrationParameters)) {
            return false;
        }

        RegistrationParameters other = (RegistrationParameters)o;
        if (!other.gcmSenderId.equals(gcmSenderId)) {
            return false;
        }
        if (!other.releaseUuid.equals(releaseUuid)) {
            return false;
        }
        if (!other.releaseSecret.equals(releaseSecret)) {
            return false;
        }
        if (!other.deviceAlias.equals(deviceAlias)) {
            return false;
        }

        return true;
    }
}
