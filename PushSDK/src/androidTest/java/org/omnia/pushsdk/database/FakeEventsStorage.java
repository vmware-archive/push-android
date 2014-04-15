package org.omnia.pushsdk.database;

import android.net.Uri;

import org.omnia.pushsdk.model.BaseEvent;
import org.omnia.pushsdk.model.utilities.EventHelper;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FakeEventsStorage implements EventsStorage {

    private final Map<EventType, Map<Uri, BaseEvent>> events;
    private static int fileId = 0;
    private boolean willSaveFail;

    public FakeEventsStorage() {
        events = new HashMap<EventType, Map<Uri, BaseEvent>>();
        events.put(EventType.MESSAGE_RECEIPT, new HashMap<Uri, BaseEvent>());
    }

    public void setWillSaveFail(boolean willSaveFail) {
        this.willSaveFail = willSaveFail;
    }

    /**
     * Saves a {@link org.omnia.pushsdk.model.BaseEvent} object into the fake filesystem.
     *
     * @param event
     * @param eventType
     */
    @Override
    public Uri saveEvent(BaseEvent event, final EventType eventType) {
        if (willSaveFail) {
            return null;
        }
        if (eventType == EventType.ALL) {
            throw new IllegalArgumentException("Can not saveEvent for EventType.ALL");
        }
        final Uri uri = getNextFileId(eventType);
        final BaseEvent clonedEvent = EventHelper.copyEvent(event, eventType);
        events.get(eventType).put(uri, clonedEvent);
        return uri;
    }

    /**
     * Gets the filenames for all the {@link org.omnia.pushsdk.model.BaseEvent} objects currently in the fake filesystem.
     *
     * @param eventType
     */
    @Override
    public List<Uri> getEventUris(final EventType eventType) {
        final List<Uri> result = new LinkedList<Uri>();
        if (eventType == EventType.ALL) {
            result.addAll(events.get(EventType.MESSAGE_RECEIPT).keySet());
        } else {
            result.addAll(events.get(eventType).keySet());
        }
        return result;
    }

    /**
     * Gets the {@link org.omnia.pushsdk.model.BaseEvent} objects currently in the fake filesystem that match the given status
     *
     * @param eventType
     * @param status
     */
    @Override
    public List<Uri> getEventUrisWithStatus(EventType eventType, int status) {
        final List<Uri> result = new LinkedList<Uri>();
        if (eventType == EventType.ALL) {
            getEventUrisWithStatusForEventType(EventType.MESSAGE_RECEIPT, status, result);
        } else {
            getEventUrisWithStatusForEventType(eventType, status, result);
        }
        return result;
    }

    private void getEventUrisWithStatusForEventType(EventType eventType, int status, final List<Uri> result) {
        for (final Uri uri : events.get(eventType).keySet()) {
            final BaseEvent event = events.get(eventType).get(uri);
            if (event.getStatus() == status) {
                result.add(uri);
            }
        }
    }

    /**
     * Retrieves the {@link org.omnia.pushsdk.model.BaseEvent} object with the given filename from the fake filesystem.
     */
    @Override
    public BaseEvent readEvent(Uri uri) {
        final EventType eventType = EventHelper.getEventTypeForUri(uri);
        if (events.get(eventType).containsKey(uri)) {
            return events.get(eventType).get(uri);
        } else {
            throw new IllegalArgumentException("Could not find event with Uri " + uri.getPath());
        }
    }

    /**
     * Deletes the {@link org.omnia.pushsdk.model.BaseEvent} objects from the fake filesystem with the given list of filenames.
     */
    @Override
    public void deleteEvents(List<Uri> eventUris, EventType eventType) {
        if (eventType == EventType.ALL) {
            throw new IllegalArgumentException("Can not deleteEvents for EventType.ALL");
        }
        for (final Uri uri : eventUris) {
            if (events.get(eventType).containsKey(uri)) {
                events.get(eventType).remove(uri);
            }
        }
    }

    /**
     * Returns the number of {@link org.omnia.pushsdk.model.BaseEvent} objects currently in the fake filesystem.
     */
    public int getNumberOfEvents(EventType eventType) {
        if (eventType == EventType.ALL) {
            return events.get(EventType.MESSAGE_RECEIPT).size();
        }
        return events.get(eventType).size();
    }

    /**
     * Clears all {@link org.omnia.pushsdk.model.BaseEvent} objects from the fake filesystem.
     *
     * @param eventType
     */
    @Override
    public void reset(EventType eventType) {
        if (eventType == EventType.ALL) {
            events.get(EventType.MESSAGE_RECEIPT).clear();
        } else {
            events.get(eventType).clear();
        }
    }

    private static Uri getNextFileId(EventType eventType) {
        Uri baseUri = null;
        if (eventType == EventType.MESSAGE_RECEIPT) {
            baseUri = DatabaseConstants.MESSAGE_RECEIPTS_CONTENT_URI;
        }
        return Uri.withAppendedPath(baseUri, String.valueOf(fileId++));
    }

    @Override
    public void setEventStatus(Uri eventUri, int status) {
        final BaseEvent event = readEvent(eventUri);
        event.setStatus(status);
    }
}
