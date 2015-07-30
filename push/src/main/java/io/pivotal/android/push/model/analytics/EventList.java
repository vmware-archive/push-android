package io.pivotal.android.push.model.analytics;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;

public class EventList {

    private static final String EVENTS = "events";

    @SerializedName(EVENTS)
    private List<Event> events;

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

        if (events != null ? !events.equals(eventList.events) : eventList.events != null) {
            // Do we need to do a deep equals comparison?
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = (events != null ? events.hashCode() : 0);
        return result;
    }
}
