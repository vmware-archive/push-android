/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push;

import java.util.HashSet;
import java.util.Set;

/**
 * Parameters used to register with the Pivotal Mobile Services Suite Push server.
 */
public class RegistrationParameters {

    private final String gcmSenderId;
    private final String variantUuid;
    private final String variantSecret;
    private final String deviceAlias;
    private final String baseServerUrl;
    private final Set<String> tags;

    /**
     * Sets up parameters used by the Pivotal Mobile Services Suite Push SDK
     * @param gcmSenderId   The "sender ID" or "project ID", as defined by the Google Cloud Messaging.  May not be null or empty.
     *                      You can find it on the Google Cloud Console (https://cloud.google.com) for your project.
     * @param variantUuid   The "variant_uuid", as defined by Pivotal Mobile Services Suite Push Services for your variant.  May not be null or empty.
     * @param variantSecret The "variant secret", as defined by Pivotal Mobile Services Suite Push Services for your variant.  May not be null or empty.
     * @param deviceAlias   A developer-defined "device alias" which can be used to designate this device, or class.
*                           of devices, in push or notification campaigns. May not be set to `null`. May be set to empty.
     * @param baseServerUrl The Pivotal Mobile Services Suite server used to provide push and related analytics services.
     * @param tags          A set of tags to register to.  You should always register all tags that you want to listen to, even if you have
     *                      already subscribed to them.  If you exclude any subscribed tags in a registration request, then those tags
     *                      will be "unsubscribed" from.
     */
    public RegistrationParameters(String gcmSenderId, String variantUuid, String variantSecret, String deviceAlias, String baseServerUrl, Set<String> tags) {
        this.gcmSenderId = gcmSenderId;
        this.variantUuid = variantUuid;
        this.variantSecret = variantSecret;
        this.deviceAlias = deviceAlias;
        this.baseServerUrl = baseServerUrl;
        this.tags = tags;
    }

    public String getGcmSenderId() {
        return gcmSenderId;
    }

    public String getVariantUuid() {
        return variantUuid;
    }

    public String getVariantSecret() {
        return variantSecret;
    }

    public String getDeviceAlias() {
        return deviceAlias;
    }

    public String getBaseServerUrl() {
        return baseServerUrl;
    }

    public Set<String> getTags() {
        return tags != null ? tags : new HashSet<String>();
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
        
        if (gcmSenderId == null && other.gcmSenderId != null) {
            return false;
        }
        if (gcmSenderId != null && other.gcmSenderId == null) {
            return false;
        }
        if (gcmSenderId != null && other.gcmSenderId != null && !other.gcmSenderId.equals(gcmSenderId)) {
            return false;
        }

        if (variantUuid == null && other.variantUuid != null) {
            return false;
        }
        if (variantUuid != null && other.variantUuid == null) {
            return false;
        }
        if (variantUuid != null && other.variantUuid != null && !other.variantUuid.equals(variantUuid)) {
            return false;
        }

        if (variantSecret == null && other.variantSecret != null) {
            return false;
        }
        if (variantSecret != null && other.variantSecret == null) {
            return false;
        }
        if (variantSecret != null && other.variantSecret != null && !other.variantSecret.equals(variantSecret)) {
            return false;
        }

        if (deviceAlias == null && other.deviceAlias != null) {
            return false;
        }
        if (deviceAlias != null && other.deviceAlias == null) {
            return false;
        }
        if (deviceAlias != null && other.deviceAlias != null && !other.deviceAlias.equals(deviceAlias)) {
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

        if (tags == null && other.tags != null) {
            return false;
        }
        if (tags != null && other.tags == null) {
            return false;
        }
        if (tags != null && other.tags != null && !other.tags.equals(tags)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = (result * 31) + (gcmSenderId == null ? 0 : gcmSenderId.hashCode());
        result = (result * 31) + (variantUuid == null ? 0 : variantUuid.hashCode());
        result = (result * 31) + (variantSecret == null ? 0 : variantSecret.hashCode());
        result = (result * 31) + (deviceAlias == null ? 0 : deviceAlias.hashCode());
        result = (result * 31) + (baseServerUrl == null ? 0 : baseServerUrl.hashCode());
        result = (result * 31) + (tags == null ? 0 : tags.hashCode());
        return result;
    }
}
