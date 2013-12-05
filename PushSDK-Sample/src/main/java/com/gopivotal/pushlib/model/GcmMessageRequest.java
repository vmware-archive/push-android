package com.gopivotal.pushlib.model;

import com.google.gson.annotations.SerializedName;
import com.gopivotal.pushlib.model.GcmMessageRequestData;

public class GcmMessageRequest {

    @SerializedName("registration_ids")
    public String[] registrationIds;

    @SerializedName("data")
    public GcmMessageRequestData data;

    public GcmMessageRequest(String[] registrationIds, String message) {
        this.registrationIds = registrationIds;
        this.data = new GcmMessageRequestData(message);
    }
}
