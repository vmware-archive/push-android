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
        "tags": [
            "subscribe": ["tag1", "tag2"],
            "unsubscribe": ["tag3", "tag4"]
        }
    }
*/

import com.google.gson.annotations.SerializedName;

import java.util.Set;

/**
 * Model used in the Pivotal CF Mobile Services device registration API.
 */
public class PCFPushApiRegistrationPutRequestData extends BasePCFPushApiRegistrationRequestData {

    @SerializedName("tags")
    private Tags tags;

    public PCFPushApiRegistrationPutRequestData() {
    }

    public Tags getTags() {
        return tags;
    }

    public void setTags(Tags tags) {
        this.tags = tags;
    }

    public static class Tags {

        @SerializedName("subscribe")
        private Set<String> subscribe;

        @SerializedName("unsubscribe")
        private Set<String> unsubscribe;

        public Tags(Set<String> subscribe, Set<String> unsubscribe) {
            this.subscribe = subscribe;
            this.unsubscribe = unsubscribe;
        }

        public Set<String> getSubscribeTags() {
            return subscribe;
        }

        public Set<String> getUnsubscribedTags() {
            return unsubscribe;
        }
    }
}
