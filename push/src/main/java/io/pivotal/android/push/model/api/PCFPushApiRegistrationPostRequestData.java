/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.model.api;

/*
    {
        "device_alias": "developer-specific",
        "device_manufacturer": "ACME INC."
        "device_model": "Nexus 5",
        "os": "android",
        "os_version": "4.4",
        "registration_token": "provided_by_FCM",
        "tags": ["tag1", "tag2"]
    }
*/

import com.google.gson.annotations.SerializedName;

import java.util.Set;

/**
 * Model used in the Pivotal CF Mobile Services device registration API.
 */
public class PCFPushApiRegistrationPostRequestData extends BasePCFPushApiRegistrationRequestData {

    @SerializedName("tags")
    private Set<String> tags;

    public PCFPushApiRegistrationPostRequestData() {
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

}
