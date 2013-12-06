package org.omnia.pushsdk.model;

import com.google.gson.annotations.SerializedName;

public class BackEndMessageRequestData {

    @SerializedName("title")
    public String title;

    @SerializedName("body")
    public String body;

    public BackEndMessageRequestData(String title, String body) {
        this.title = title;
        this.body = body;
    }
}
