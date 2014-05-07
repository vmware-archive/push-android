package com.pivotal.cf.mobile.pushsdk.model.events;

import com.pivotal.cf.mobile.analyticssdk.model.events.Event;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class PushEventHelper {

    public static final String VARIANT_UUID = "variant_uuid";
    public static final String DEVICE_ID = "device_id";

    public static Event getEvent(String eventType, String variantUuid, String deviceId) {
        final String eventId = UUID.randomUUID().toString();
        final Date time = new Date();
        return getEvent(eventType, eventId, variantUuid, deviceId, time);
    }

    public static Event getEvent(String eventType, String eventId, String variantUuid, String deviceId, Date time) {
        final Event event = new Event();
        event.setEventType(eventType);
        event.setEventId(eventId);
        event.setTime(time);
        event.setStatus(Event.Status.NOT_POSTED);
        final HashMap<String, Object> data = new HashMap<String, Object>();
        data.put(VARIANT_UUID, variantUuid);
        data.put(DEVICE_ID, deviceId);
        event.setData(data);
        return event;
    }
}
