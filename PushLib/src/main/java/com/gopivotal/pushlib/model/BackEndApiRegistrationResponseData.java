package com.gopivotal.pushlib.model;

/*
{
        "replicant_uuid": "9e60c311-f5c7-4416-aea2-d07bbc94f208",
        "secret": "3c676b20-3c49-4215-be1a-3932e3458514",
        "device_alias": "andoidtest",
        "device_manufactur": "Phone Corp",
        "device_model": "Ultimate Phono 1999",
        "os": "android",
        "os_version": "version",
        "registration_token": "SomeString"
        }
*/

import com.google.gson.annotations.SerializedName;

public class BackEndApiRegistrationResponseData {

    @SerializedName("replicant_id")
    private String replicantId;

    @SerializedName("device_uuid")
    private String deviceUuid;

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

    public BackEndApiRegistrationResponseData() {
    }

    public String getReplicantId() {
        return replicantId;
    }

    public void setReplicantId(String replicantId) {
        this.replicantId = replicantId;
    }

    public String getDeviceUuid() {
        return deviceUuid;
    }

    public void setDeviceUuid(String deviceUuid) {
        this.deviceUuid = deviceUuid;
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
}
