package com.gopivotal.pushlib.model;

import com.google.gson.annotations.SerializedName;

public class BackEndMessageTarget {

    @SerializedName("platforms")
    public String platforms;

    @SerializedName("devices")
    public String[] devices;

    public BackEndMessageTarget(String platforms, String[] devices) {
        this.platforms = platforms;
        this.devices = devices;
    }
}
