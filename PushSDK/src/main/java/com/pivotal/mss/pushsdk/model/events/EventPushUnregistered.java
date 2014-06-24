package com.pivotal.mss.pushsdk.model.events;

import com.pivotal.mss.analyticssdk.model.events.Event;

public class EventPushUnregistered {

    public static final String EVENT_TYPE = "event_push_unregistered";

    public static Event getEvent(String variantUuid, String deviceId) {
        return PushEventHelper.getEvent(EVENT_TYPE, variantUuid, deviceId);
    }
}
