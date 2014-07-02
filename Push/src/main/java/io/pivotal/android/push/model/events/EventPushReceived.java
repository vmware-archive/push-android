package io.pivotal.android.push.model.events;

import io.pivotal.android.analytics.model.events.Event;

public class EventPushReceived {

    public static final String EVENT_TYPE = "event_push_received";
    public static final String MESSAGE_UUID = "msg_uuid";

    public static Event getEvent(String messageUuid, String variantUuid, String deviceId) {
        final Event event = PushEventHelper.getEvent(EVENT_TYPE, variantUuid, deviceId);
        event.getData().put(MESSAGE_UUID, messageUuid);
        return event;
    }
}
