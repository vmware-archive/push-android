/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.pivotal.android.push.prefs.Pivotal;
import io.pivotal.android.push.prefs.PushPreferencesProvider;
import io.pivotal.android.push.util.Util;

/**
 * Parameters used to register with the Pivotal CF Mobile Services Push server.
 */
public class PushParameters {

    private final String gcmSenderId;
    private final String platformUuid;
    private final String platformSecret;
    private final String serviceUrl;
    private final String deviceAlias;
    private final Set<String> tags;
    private final boolean areGeofencesEnabled;
    private final Pivotal.SslCertValidationMode sslCertValidationMode;
    private final List<String> pinnedSslCertificateNames;
    private final Map<String, String> requestHeaders;

    /**
     * Sets up parameters used by the Pivotal CF Mobile Services Push SDK
     * @param gcmSenderId    The "sender ID" or "project ID", as defined by the Google Cloud Messaging.  May not be null or empty.
     *                       You can find it on the Google Cloud Console (https://cloud.google.com) for your project.  See
     *                       the "pivotal.push.gcmSenderId" property.
     * @param platformUuid   The "platform", as defined by Pivotal CF Mobile Services Push Services for your platform.  May not be null or empty.
     *                       See the "pivotal.push.platformUuid" property.
     * @param platformSecret The "platform secret", as defined by Pivotal CF Mobile Services Push Services for your platform.  May not be null or empty.
     *                       See the pivotal.push.platformSecret property.
     * @param serviceUrl     The Pivotal CF Mobile Services server used to provide push and related analytics services.
     *                       See the pivotal.push.serviceUrl" property.
     * @param deviceAlias    A developer-defined "device alias" which can be used to designate this device, or class.
     *                       of devices, in push or notification campaigns. May not be set to `null`. May be set to empty.
     * @param tags           A set of tags to register to.  You should always register all tags that you want to listen to, even if you have
     *                       already subscribed to them.  If you exclude any subscribed tags in a registration request, then those tags
     *                       will be unsubscribed.
     * @param areGeofencesEnabled        Are geofences available (see the "pivotal.push.geofencesEnabled" property).
     * @param sslCertValidationMode      The SSL validation mode (see documentation in the 'Pivotal.properties' file)
     *                       already subscribed to them.  If you exclude any subscribed tags in a registration request, then those tags
     *                       will be unsubscribed.
     * @param pinnedSslCertificateNames  The list of pinned SSL certificates.  May be null or empty.
     * @param requestHeaders             The list of extra request headers to inject into the request
     */
    public PushParameters(@NonNull String gcmSenderId,
                          @NonNull String platformUuid,
                          @NonNull String platformSecret,
                          @NonNull String serviceUrl,
                          @Nullable String deviceAlias,
                          @Nullable Set<String> tags,
                          boolean areGeofencesEnabled,
                          Pivotal.SslCertValidationMode sslCertValidationMode,
                          @Nullable List<String> pinnedSslCertificateNames,
                          @Nullable Map<String, String> requestHeaders) {

        this.gcmSenderId = gcmSenderId;
        this.platformUuid = platformUuid;
        this.platformSecret = platformSecret;
        this.serviceUrl = serviceUrl;
        this.deviceAlias = deviceAlias;
        this.tags = Util.lowercaseTags(tags);
        this.areGeofencesEnabled = areGeofencesEnabled;
        this.sslCertValidationMode = sslCertValidationMode;
        this.pinnedSslCertificateNames = pinnedSslCertificateNames;
        this.requestHeaders = requestHeaders;
    }

    /**
     * Returns a PushParameters object by reading some of its field from the Pivotal.properties file and some from the PushPreferences
     */
    public PushParameters(@NonNull Context context,
                          @NonNull PushPreferencesProvider preferencesProvider,
                          @Nullable String deviceAlias,
                          @Nullable Set<String> tags) {

        this.gcmSenderId = Pivotal.getGcmSenderId(context);
        this.platformUuid = Pivotal.getPlatformUuid(context);
        this.platformSecret = Pivotal.getPlatformSecret(context);
        this.serviceUrl = Pivotal.getServiceUrl(context);
        this.areGeofencesEnabled = preferencesProvider.areGeofencesEnabled();
        this.sslCertValidationMode = Pivotal.getSslCertValidationMode(context);
        this.pinnedSslCertificateNames = Pivotal.getPinnedSslCertificateNames(context);
        this.requestHeaders = preferencesProvider.getRequestHeaders();
        this.tags = Util.lowercaseTags(tags);
        this.deviceAlias = deviceAlias;
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

    public boolean areGeofencesEnabled() {
        return areGeofencesEnabled;
    }

    public List<String> getPinnedSslCertificateNames() {
        return pinnedSslCertificateNames != null ? Collections.unmodifiableList(pinnedSslCertificateNames) : null;
    }

    public Pivotal.SslCertValidationMode getSslCertValidationMode() {
        return sslCertValidationMode;
    }

    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PushParameters that = (PushParameters) o;

        if (areGeofencesEnabled != that.areGeofencesEnabled) return false;
        if (gcmSenderId != null ? !gcmSenderId.equals(that.gcmSenderId) : that.gcmSenderId != null)
            return false;
        if (platformUuid != null ? !platformUuid.equals(that.platformUuid) : that.platformUuid != null)
            return false;
        if (platformSecret != null ? !platformSecret.equals(that.platformSecret) : that.platformSecret != null)
            return false;
        if (serviceUrl != null ? !serviceUrl.equals(that.serviceUrl) : that.serviceUrl != null)
            return false;
        if (deviceAlias != null ? !deviceAlias.equals(that.deviceAlias) : that.deviceAlias != null)
            return false;
        if (tags != null ? !tags.equals(that.tags) : that.tags != null) return false;
        if (sslCertValidationMode != that.sslCertValidationMode) return false;
        if (pinnedSslCertificateNames != null ? !pinnedSslCertificateNames.equals(that.pinnedSslCertificateNames) : that.pinnedSslCertificateNames != null)
            return false;
        return !(requestHeaders != null ? !requestHeaders.equals(that.requestHeaders) : that.requestHeaders != null);
    }

    @Override
    public int hashCode() {
        int result = gcmSenderId != null ? gcmSenderId.hashCode() : 0;
        result = 31 * result + (platformUuid != null ? platformUuid.hashCode() : 0);
        result = 31 * result + (platformSecret != null ? platformSecret.hashCode() : 0);
        result = 31 * result + (serviceUrl != null ? serviceUrl.hashCode() : 0);
        result = 31 * result + (deviceAlias != null ? deviceAlias.hashCode() : 0);
        result = 31 * result + (tags != null ? tags.hashCode() : 0);
        result = 31 * result + (areGeofencesEnabled ? 1 : 0);
        result = 31 * result + (sslCertValidationMode != null ? sslCertValidationMode.hashCode() : 0);
        result = 31 * result + (pinnedSslCertificateNames != null ? pinnedSslCertificateNames.hashCode() : 0);
        result = 31 * result + (requestHeaders != null ? requestHeaders.hashCode() : 0);
        return result;
    }
}
