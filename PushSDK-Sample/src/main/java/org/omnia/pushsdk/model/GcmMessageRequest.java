package org.omnia.pushsdk.model;

import com.google.gson.annotations.SerializedName;

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
