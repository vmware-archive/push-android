package com.pivotal.cf.mobile.analyticssdk.model.events;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;

public class EventList {

    private static final String DEVICE_ID = "device_id";
    private static final String EVENTS = "events";

    @SerializedName(DEVICE_ID)
    private String deviceId;

    @SerializedName(EVENTS)
    private List<Event> events;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public List<Event> getEvents() {
        return Collections.unmodifiableList(events);
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final EventList eventList = (EventList) o;

        if (deviceId != null ? !deviceId.equals(eventList.deviceId) : eventList.deviceId != null) {
            return false;
        }
        if (events != null ? !events.equals(eventList.events) : eventList.events != null) {
            // Do we need to do a deep equals comparison?
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = deviceId != null ? deviceId.hashCode() : 0;
        result = 31 * result + (events != null ? events.hashCode() : 0);
        return result;
    }
}
