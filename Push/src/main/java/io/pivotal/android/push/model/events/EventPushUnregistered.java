package io.pivotal.android.push.model.events;

import io.pivotal.android.analytics.model.events.Event;

public class EventPushUnregistered {

    public static final String EVENT_TYPE = "event_push_unregistered";

    public static Event getEvent(String variantUuid, String deviceId) {
        return PushEventHelper.getEvent(EVENT_TYPE, variantUuid, deviceId);
    }
}
