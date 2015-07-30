package io.pivotal.android.push.model.analytics;

import java.util.Date;

public class DummyEvent {

    public static final String EVENT_TYPE = "event_dummy";

    public static Event getEvent(String deviceUuid) {
        final Date time = new Date();
        return getEvent(deviceUuid, time);
    }

    public static Event getEvent(String deviceUuid, Date time) {
        final Event event = new Event();
        event.setEventType(EVENT_TYPE);
        event.setDeviceUuid(deviceUuid);
        event.setEventTime(time);
        event.setStatus(Event.Status.NOT_POSTED);
        return event;
    }
}
