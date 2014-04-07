package org.omnia.pushsdk.database;

import android.content.Context;
import android.net.Uri;

import org.omnia.pushsdk.model.EventBase;
import org.omnia.pushsdk.model.utilities.EventHelper;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Mock class used to isolate the crash handler from the filesystem in unit tests.
 * 
 * @author rob
 * 
 */
public class MockEventsStorage implements EventsStorage {

	private final Map<EventType, Map<Uri, EventBase>> events;
	private static int fileId = 0;

	public MockEventsStorage() {
		events = new HashMap<EventType, Map<Uri, EventBase>>();
		events.put(EventType.MESSAGE_RECEIPTS, new HashMap<Uri, EventBase>());
	}

	/**
	 * Saves a {@link EventBase} object into the fake filesystem.
	 *
     * @param context
     * @param event
	 * @param eventType
	 */
	@Override
	public Uri saveEvent(Context context, EventBase event, final EventType eventType) {
		if (eventType == EventType.ALL) {
			throw new IllegalArgumentException("Can not saveEvent for EventType.ALL");
		}
		final Uri uri = getNextFileId(eventType);
		events.get(eventType).put(uri, event);
		return uri;
	}

	/**
	 * Gets the filenames for all the {@link EventBase} objects currently in the fake filesystem.
     *
     * @param context
     * @param eventType
	 */
	@Override
	public List<Uri> getEventUris(Context context, final EventType eventType) {
		final List<Uri> result = new LinkedList<Uri>();
		if (eventType == EventType.ALL) {
			result.addAll(events.get(EventType.MESSAGE_RECEIPTS).keySet());
		} else {
			result.addAll(events.get(eventType).keySet());
		}
		return result;
	}

	/**
	 * Gets the {@link EventBase} objects currently in the fake filesystem that match the given status
     *
     * @param context
     * @param eventType
     * @param status
     */
	@Override
	public List<Uri> getEventUrisWithStatus(Context context, EventType eventType, int status) {
		final List<Uri> result = new LinkedList<Uri>();
		if (eventType == EventType.ALL) {
			getEventUrisWithStatusForEventType(EventType.MESSAGE_RECEIPTS, status, result);
		} else {
			getEventUrisWithStatusForEventType(eventType, status, result);
		}
		return result;
	}

	private void getEventUrisWithStatusForEventType(EventType eventType, int status, final List<Uri> result) {
		for (final Uri uri : events.get(eventType).keySet()) {
			final EventBase event = events.get(eventType).get(uri);
			if (event.getStatus() == status) {
				result.add(uri);
			}
		}
	}

	/**
	 * Retrieves the {@link EventBase} object with the given filename from the fake filesystem.
	 */
	@Override
	public EventBase readEvent(Context context, Uri uri) {
		final EventType eventType = EventHelper.getEventTypeForUri(uri);
		if (events.get(eventType).containsKey(uri)) {
			return events.get(eventType).get(uri);
		} else {
			throw new IllegalArgumentException("Could not find event with Uri " + uri.getPath());
		}
	}

	/**
	 * Deletes the {@link EventBase} objects from the fake filesystem with the given list of filenames.
	 */
	@Override
	public void deleteEvents(Context context, List<Uri> eventUris, EventType eventType) {
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
	 * Returns the number of {@link EventBase} objects currently in the fake filesystem.
	 */
	public int getNumberOfEvents(Context context, EventType eventType) {
		if (eventType == EventType.ALL) {
			return events.get(EventType.MESSAGE_RECEIPTS).size();
		}
		return events.get(eventType).size();
	}

	/**
	 * Clears all {@link EventBase} objects from the fake filesystem.
	 * 
	 * @param context
	 * @param eventType
	 */
	@Override
	public void reset(Context context, EventType eventType) {
		if (eventType == EventType.ALL) {
			events.get(EventType.MESSAGE_RECEIPTS).clear();
		} else {
			events.get(eventType).clear();
		}
	}

	private static Uri getNextFileId(EventType eventType) {
		Uri baseUri = null;
		if (eventType == EventType.MESSAGE_RECEIPTS) {
			baseUri = DatabaseConstants.MESSAGE_RECEIPTS_CONTENT_URI;
		}
		return Uri.withAppendedPath(baseUri, String.valueOf(fileId++));
	}

	@Override
	public void setEventStatus(Context context, Uri eventUri, int status) {
		final EventBase event = readEvent(context, eventUri);
		event.setStatus(status);
	}
}
