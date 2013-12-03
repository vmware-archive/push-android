package com.gopivotal.pushlib.model;

import com.google.gson.annotations.SerializedName;

public class GcmMessageRequestData {

    @SerializedName("message")
    public String message;

    public GcmMessageRequestData(String message) {
        this.message = message;
    }
}
