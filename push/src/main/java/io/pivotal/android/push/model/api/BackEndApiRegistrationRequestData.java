/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.model.api;

/*
{
        "variant_uuid": "guid_provided_by_developer",
        "variant_secret": "guid_provided_by_developer",
        "device_alias": "developer-specific",
        "device_manufacturer": "ACME INC."
        "device_model": "Nexus 5",
        "os": "android",
        "os_version": "4.4",
        "registration_token": "provided_by_GCM"
        }
*/

import com.google.gson.annotations.SerializedName;

import java.util.Set;

/**
 * Model used in the Pivotal Mobile Services Suite device registration API.
 */
public class BackEndApiRegistrationRequestData {

    @SerializedName("variant_uuid")
    private String variantUuid;

    @SerializedName("secret")
    private String secret;

    @SerializedName("device_alias")
    private String deviceAlias;

    @SerializedName("device_manufacturer")
    private String deviceManufacturer;

    @SerializedName("device_model")
    private String deviceModel;

    @SerializedName("os")
    private String os;

    @SerializedName("os_version")
    private String osVersion;

    @SerializedName("registration_token")
    private String registrationToken;

    @SerializedName("tags")
    private Tags tags;

    public BackEndApiRegistrationRequestData() {
    }

    public String getVariantUuid() {
        return variantUuid;
    }

    public void setVariantUuid(String variantUuid) {
        this.variantUuid = variantUuid;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getDeviceAlias() {
        return deviceAlias;
    }

    public void setDeviceAlias(String deviceAlias) {
        this.deviceAlias = deviceAlias;
    }

    public String getDeviceManufacturer() {
        return deviceManufacturer;
    }

    public void setDeviceManufacturer(String deviceManufacturer) {
        this.deviceManufacturer = deviceManufacturer;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getRegistrationToken() {
        return registrationToken;
    }

    public void setRegistrationToken(String registrationToken) {
        this.registrationToken = registrationToken;
    }

    public Tags getTags() {
        return tags;
    }

    public void setTags(Tags tags) {
        this.tags = tags;
    }

    public static class Tags {

        @SerializedName("subscribe")
        private Set<String> subscribe;

        @SerializedName("unsubscribe")
        private Set<String> unsubscribe;

        public Tags(Set<String> subscribe, Set<String> unsubscribe) {
            this.subscribe = subscribe;
            this.unsubscribe = unsubscribe;
        }

        public Set<String> getSubscribeTags() {
            return subscribe;
        }

        public Set<String> getUnsubscripedTags() {
            return subscribe;
        }
    }
}
