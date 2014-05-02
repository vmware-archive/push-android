package com.pivotal.cf.mobile.analyticssdk.model.events;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class DummyEvent {

    public static final String EVENT_TYPE = "event_dummy";

    public static final String MESSAGE_UUID = "msg_uuid";

    public static Event getEvent(String variantUuid, String messageUuid, String deviceId) {
        final String eventId = UUID.randomUUID().toString();
        final Date time = new Date();
        return getEvent(eventId, variantUuid, messageUuid, deviceId, time);
    }

    public static Event getEvent(String eventId, String variantUuid, String messageUuid, String deviceId, Date time) {
        final Event event = new Event();
        event.setEventType(EVENT_TYPE);
        event.setEventId(eventId);
        event.setVariantUuid(variantUuid);
        event.setTime(time);
        event.setDeviceId(deviceId);
        event.setStatus(Event.Status.NOT_POSTED);
        final HashMap<String, Object> data = new HashMap<String, Object>();
        data.put(MESSAGE_UUID, messageUuid);
        event.setData(data);
        return event;
    }}
