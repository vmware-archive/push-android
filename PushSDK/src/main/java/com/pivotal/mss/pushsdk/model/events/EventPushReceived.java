package com.pivotal.mss.pushsdk.model.events;

import com.pivotal.mss.analyticssdk.model.events.Event;

import java.util.HashMap;

public class EventPushReceived {

    public static final String EVENT_TYPE = "event_push_received";
    public static final String MESSAGE_UUID = "msg_uuid";

    public static Event getEvent(String messageUuid, String variantUuid, String deviceId) {
        final Event event = PushEventHelper.getEvent(EVENT_TYPE, variantUuid, deviceId);
        final HashMap<String, Object> data = event.getData();
        data.put(MESSAGE_UUID, messageUuid);
        return event;
    }
}
