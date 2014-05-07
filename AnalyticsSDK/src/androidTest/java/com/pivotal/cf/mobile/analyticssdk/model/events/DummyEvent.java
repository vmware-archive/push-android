package com.pivotal.cf.mobile.analyticssdk.model.events;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class DummyEvent {

    public static final String EVENT_TYPE = "event_dummy";

    public static final String MESSAGE_UUID = "msg_uuid";
    private static final String VARIANT_UUID = "variant_uuid";
    private static final String DEVICE_ID = "device_id";

    public static Event getEvent(String variantUuid, String messageUuid, String deviceId) {
        final String eventId = UUID.randomUUID().toString();
        final Date time = new Date();
        return getEvent(eventId, variantUuid, messageUuid, deviceId, time);
    }

    public static Event getEvent(String eventId, String variantUuid, String messageUuid, String deviceId, Date time) {
        final Event event = new Event();
        event.setEventType(EVENT_TYPE);
        event.setEventId(eventId);
        event.setTime(time);
        event.setStatus(Event.Status.NOT_POSTED);
        final HashMap<String, Object> data = new HashMap<String, Object>();
        data.put(MESSAGE_UUID, messageUuid);
        data.put(VARIANT_UUID, variantUuid);
        data.put(DEVICE_ID, deviceId);
        event.setData(data);
        return event;
    }}
