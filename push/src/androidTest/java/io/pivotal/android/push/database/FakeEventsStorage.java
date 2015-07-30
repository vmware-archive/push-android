package io.pivotal.android.push.database;

import android.net.Uri;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.pivotal.android.push.model.analytics.Event;


public class FakeEventsStorage implements EventsStorage {

    private final Map<Uri, Event> events;
    private static int fileId = 0;
    private boolean willSaveFail;

    public FakeEventsStorage() {
        events = new HashMap<Uri, Event>();
    }

    public void setWillSaveFail(boolean willSaveFail) {
        this.willSaveFail = willSaveFail;
    }

    /**
     * Saves a {@link io.pivotal.android.push.model.analytics.Event} object into the fake filesystem.
     *
     * @param event
     */
    @Override
    public Uri saveEvent(Event event) {
        if (willSaveFail) {
            return null;
        }
        final Uri uri = getNextFileId();
        final Event clonedEvent = new Event(event);
        events.put(uri, clonedEvent);
        return uri;
    }

    /**
     * Gets the filenames for all the {@link io.pivotal.android.push.model.analytics.Event} objects currently in the fake filesystem.
     */
    @Override
    public List<Uri> getEventUris() {
        return new LinkedList<Uri>(events.keySet());
    }

    /**
     * Gets the {@link io.pivotal.android.push.model.analytics.Event} objects currently in the fake filesystem that match the given status
     *
     * @param status
     */
    @Override
    public List<Uri> getEventUrisWithStatus(int status) {
        final List<Uri> result = new LinkedList<Uri>();
        getEventUrisWithStatusForEventType(status, result);
        return result;
    }

    private void getEventUrisWithStatusForEventType(int status, final List<Uri> result) {
        for (final Uri uri : events.keySet()) {
            final Event event = events.get(uri);
            if (event.getStatus() == status) {
                result.add(uri);
            }
        }
    }

    /**
     * Retrieves the {@link io.pivotal.android.push.model.analytics.Event} object with the given filename from the fake filesystem.
     */
    @Override
    public Event readEvent(Uri uri) {
        if (events.containsKey(uri)) {
            return events.get(uri);
        } else {
            throw new IllegalArgumentException("Could not find event with Uri " + uri.getPath());
        }
    }

    /**
     * Deletes the {@link io.pivotal.android.push.model.analytics.Event} objects from the fake filesystem with the given list of filenames.
     */
    @Override
    public void deleteEvents(List<Uri> eventUris) {
        for (final Uri uri : eventUris) {
            if (events.containsKey(uri)) {
                events.remove(uri);
            }
        }
    }

    /**
     * Returns the number of {@link io.pivotal.android.push.model.analytics.Event} objects currently in the fake filesystem.
     */
    public int getNumberOfEvents() {
        return events.size();
    }

    /**
     * Clears all {@link io.pivotal.android.push.model.analytics.Event} objects from the fake filesystem.
     */
    @Override
    public void reset() {
        events.clear();
    }

    private static Uri getNextFileId() {
        Uri baseUri = Database.EVENTS_CONTENT_URI;
        return Uri.withAppendedPath(baseUri, String.valueOf(fileId++));
    }

    @Override
    public void setEventStatus(Uri eventUri, int status) {
        final Event event = readEvent(eventUri);
        event.setStatus(status);
    }
}
