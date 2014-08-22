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
        "registration_token": "provided_by_GCM",
        "tags": ["tag1", "tag2"]
}
*/

import com.google.gson.annotations.SerializedName;

import java.util.Set;

/**
 * Model used in the Pivotal Mobile Services Suite device registration API.
 */
public class BackEndApiRegistrationPostRequestData extends BaseBackEndApiRegistrationRequestData {

    @SerializedName("tags")
    private Set tags;

    public BackEndApiRegistrationPostRequestData() {
    }

    public Set getTags() {
        return tags;
    }

    public void setTags(Set tags) {
        this.tags = tags;
    }

}
