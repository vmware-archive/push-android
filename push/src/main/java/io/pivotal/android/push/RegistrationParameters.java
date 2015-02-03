/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push;

import java.util.HashSet;
import java.util.Set;

/**
 * Parameters used to register with the Pivotal CF Mobile Services Push server.
 */
public class RegistrationParameters {

    private final String gcmSenderId;
    private final String platformUuid;
    private final String platformSecret;
    private final String serviceUrl;
    private final String deviceAlias;
    private final Set<String> tags;

    /**
     * Sets up parameters used by the Pivotal CF Mobile Services Push SDK
     * @param gcmSenderId    The "sender ID" or "project ID", as defined by the Google Cloud Messaging.  May not be null or empty.
     *                       You can find it on the Google Cloud Console (https://cloud.google.com) for your project.
     * @param platformUuid   The "platform", as defined by Pivotal CF Mobile Services Push Services for your platform.  May not be null or empty.
     * @param platformSecret The "platform secret", as defined by Pivotal CF Mobile Services Push Services for your platform.  May not be null or empty.
     * @param serviceUrl     The Pivotal CF Mobile Services server used to provide push and related analytics services.
     * @param deviceAlias    A developer-defined "device alias" which can be used to designate this device, or class.
     *                       of devices, in push or notification campaigns. May not be set to `null`. May be set to empty.
     * @param tags           A set of tags to register to.  You should always register all tags that you want to listen to, even if you have
     *                       already subscribed to them.  If you exclude any subscribed tags in a registration request, then those tags
     *                       will be unsubscribed.
     */
    public RegistrationParameters(String gcmSenderId, String platformUuid, String platformSecret, String serviceUrl, String deviceAlias, Set<String> tags) {
        this.gcmSenderId = gcmSenderId;
        this.platformUuid = platformUuid;
        this.platformSecret = platformSecret;
        this.serviceUrl = serviceUrl;
        this.deviceAlias = deviceAlias;
        this.tags = tags;
    }

    public String getGcmSenderId() {
        return gcmSenderId;
    }

    public String getPlatformUuid() {
        return platformUuid;
    }

    public String getPlatformSecret() {
        return platformSecret;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public String getDeviceAlias() {
        return deviceAlias;
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

        if (platformUuid == null && other.platformUuid != null) {
            return false;
        }
        if (platformUuid != null && other.platformUuid == null) {
            return false;
        }
        if (platformUuid != null && other.platformUuid != null && !other.platformUuid.equals(platformUuid)) {
            return false;
        }

        if (platformSecret == null && other.platformSecret != null) {
            return false;
        }
        if (platformSecret != null && other.platformSecret == null) {
            return false;
        }
        if (platformSecret != null && other.platformSecret != null && !other.platformSecret.equals(platformSecret)) {
            return false;
        }

        if (serviceUrl == null && other.serviceUrl != null) {
            return false;
        }
        if (serviceUrl != null && other.serviceUrl == null) {
            return false;
        }
        if (serviceUrl != null && other.serviceUrl != null && !other.serviceUrl.equals(serviceUrl)) {
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
        result = (result * 31) + (platformUuid == null ? 0 : platformUuid.hashCode());
        result = (result * 31) + (platformSecret == null ? 0 : platformSecret.hashCode());
        result = (result * 31) + (serviceUrl == null ? 0 : serviceUrl.hashCode());
        result = (result * 31) + (deviceAlias == null ? 0 : deviceAlias.hashCode());
        result = (result * 31) + (tags == null ? 0 : tags.hashCode());
        return result;
    }
}
