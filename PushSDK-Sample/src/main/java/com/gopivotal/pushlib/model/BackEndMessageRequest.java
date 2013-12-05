package com.gopivotal.pushlib.model;

import com.google.gson.annotations.SerializedName;

public class BackEndMessageRequest {

    @SerializedName("app_uuid")
    public String appUuid;

    @SerializedName("app_secret_key")
    public String appSecretKey;

    @SerializedName("message")
    public BackEndMessageRequestData message;

    @SerializedName("target")
    public BackEndMessageTarget target;

    public BackEndMessageRequest(String appUuid, String appSecretKey, String messageTitle, String messageBody, String platforms, String[] devices) {
        this.appUuid = appUuid;
        this.appSecretKey = appSecretKey;
        this.message = new BackEndMessageRequestData(messageTitle, messageBody);
        this.target = new BackEndMessageTarget(platforms, devices);
    }
}
