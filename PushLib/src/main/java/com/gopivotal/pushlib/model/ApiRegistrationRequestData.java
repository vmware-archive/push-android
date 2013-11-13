package com.gopivotal.pushlib.model;

/*
{
        "replicant_uuid": "9e60c311-f5c7-4416-aea2-d07bbc94f208",
        "secret": "3c676b20-3c49-4215-be1a-3932e3458514",
        "device_alias": "andoidtest",
        "device_type": "phone",
        "device_model": "meh",
        "os": "android",
        "os_version": "version",
        "registration_token": "SomeString"
        }
*/

import com.google.gson.annotations.SerializedName;

public class ApiRegistrationRequestData {

    @SerializedName("replication_uuid")
    private String replicantUuid;

    @SerializedName("secret")
    private String secret;

    @SerializedName("device_alias")
    private String deviceAlias;

    @SerializedName("device_type")
    private String deviceType;

    @SerializedName("device_model")
    private String deviceModel;

    @SerializedName("os")
    private String os;

    @SerializedName("os_version")
    private String osVersion;

    @SerializedName("registration_token")
    private String registrationToken;

    public ApiRegistrationRequestData() {
    }

    public String getReplicantUuid() {
        return replicantUuid;
    }

    public void setReplicantUuid(String replicantUuid) {
        this.replicantUuid = replicantUuid;
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

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
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
}
