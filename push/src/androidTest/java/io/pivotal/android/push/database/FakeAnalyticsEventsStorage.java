package io.pivotal.android.push.database;

import android.net.Uri;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.pivotal.android.push.model.analytics.AnalyticsEvent;


public class FakeAnalyticsEventsStorage implements AnalyticsEventsStorage {

    private final Map<Uri, AnalyticsEvent> events;
    private static int fileId = 0;
    private boolean willSaveFail;

    public FakeAnalyticsEventsStorage() {
        events = new HashMap<>();
    }

    public void setWillSaveFail(boolean willSaveFail) {
        this.willSaveFail = willSaveFail;
    }

    /**
     * Saves a {@link AnalyticsEvent} object into the fake filesystem.
     *
     * @param event
     */
    @Override
    public Uri saveEvent(AnalyticsEvent event) {
        if (willSaveFail) {
            return null;
        }
        final Uri uri = getNextFileId();
        final AnalyticsEvent clonedEvent = new AnalyticsEvent(event);
        events.put(uri, clonedEvent);
        return uri;
    }

    /**
     * Gets the filenames for all the {@link AnalyticsEvent} objects currently in the fake filesystem.
     */
    @Override
    public List<Uri> getEventUris() {
        return new LinkedList<>(events.keySet());
    }

    /**
     * Gets the {@link AnalyticsEvent} objects currently in the fake filesystem that match the given status
     *
     * @param status
     */
    @Override
    public List<Uri> getEventUrisWithStatus(int status) {
        final List<Uri> result = new LinkedList<>();
        getEventUrisWithStatusForEventType(status, result);
        return result;
    }

    @Override
    public List<Uri> getEventUrisWithType(String eventType) {
        final List<Uri> result = new LinkedList<>();
        for (final Uri uri : events.keySet()) {
            final AnalyticsEvent event = events.get(uri);
            if (event.getEventType().equalsIgnoreCase(eventType)) {
                result.add(uri);
            }
        }
        return result;
    }

    private void getEventUrisWithStatusForEventType(int status, final List<Uri> result) {
        for (final Uri uri : events.keySet()) {
            final AnalyticsEvent event = events.get(uri);
            if (event.getStatus() == status) {
                result.add(uri);
            }
        }
    }

    /**
     * Retrieves the {@link AnalyticsEvent} object with the given filename from the fake filesystem.
     */
    @Override
    public AnalyticsEvent readEvent(Uri uri) {
        if (events.containsKey(uri)) {
            return events.get(uri);
        } else {
            throw new IllegalArgumentException("Could not find event with Uri " + uri.getPath());
        }
    }

    /**
     * Deletes the {@link AnalyticsEvent} objects from the fake filesystem with the given list of filenames.
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
     * Returns the number of {@link AnalyticsEvent} objects currently in the fake filesystem.
     */
    public int getNumberOfEvents() {
        return events.size();
    }

    /**
     * Clears all {@link AnalyticsEvent} objects from the fake filesystem.
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
        final AnalyticsEvent event = readEvent(eventUri);
        event.setStatus(status);
    }
}
