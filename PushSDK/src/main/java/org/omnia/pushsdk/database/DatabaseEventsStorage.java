package org.omnia.pushsdk.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import org.omnia.pushsdk.model.BaseEvent;
import org.omnia.pushsdk.model.utilities.EventHelper;

import java.util.LinkedList;
import java.util.List;

public class DatabaseEventsStorage implements EventsStorage {

	public DatabaseEventsStorage() {
	}

	@Override
	public Uri saveEvent(BaseEvent event, EventType eventType) {
		if (eventType == EventType.ALL) {
			throw new IllegalArgumentException("Can not saveEvent for EventType.ALL");
		}
		final ContentValues contentValues = event.getContentValues();
		final Uri uri = EventsDatabaseWrapper.insert(EventHelper.getUriForEventType(eventType), contentValues);
		return uri;
	}

	@Override
	public List<Uri> getEventUris(EventType eventType) {
		if (eventType == EventType.ALL) {
			final List<Uri> results = new LinkedList<Uri>();
			for (EventType type : EventType.values()) {
				if (type != EventType.ALL) {
					results.addAll(getGeneralQuery(type, null, null, null, null));
				}
			}
			return results;
		} else {
			return getGeneralQuery(eventType, null, null, null, null);
		}
	}

	public List<Uri> getEventUrisWithStatus(EventType eventType, int status) {
		if (eventType == EventType.ALL) {
			final List<Uri> results = new LinkedList<Uri>();
			results.addAll(getGeneralQuery(EventType.MESSAGE_RECEIPT, null, "status = ?", new String[] { String.valueOf(status) }, null));
//			results.addAll(getGeneralQuery(EventType.UNHANDLED_EXCEPTION, null, "status = ?", new String[] { String.valueOf(status) }, null));
			return results;
		} else {
			return getGeneralQuery(eventType, null, "status = ?", new String[] { String.valueOf(status) }, null);
		}
	}

	private List<Uri> getGeneralQuery(EventType eventType, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		final Uri uri = EventHelper.getUriForEventType(eventType);
		Cursor cursor = null;
		try {
			cursor = EventsDatabaseWrapper.query(uri, projection, selection, selectionArgs, sortOrder);
			return getEventUrisFromCursor(cursor, eventType);
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
	}

	private List<Uri> getEventUrisFromCursor(final Cursor cursor, final EventType eventType) {
		final List<Uri> uris = new LinkedList<Uri>();
		if (cursor != null) {
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
				final int id = BaseEvent.getRowIdFromCursor(cursor);
				final Uri uri = Uri.withAppendedPath(EventHelper.getUriForEventType(eventType), String.valueOf(id));
				uris.add(uri);
			}
		}
		return uris;
	}

	@Override
	public int getNumberOfEvents(EventType eventType) {
		if (eventType == EventType.ALL) {
			return getNumberOfEventsByEventType(EventType.MESSAGE_RECEIPT)/* + getNumberOfEventsByEventType(EventType.UNHANDLED_EXCEPTION)*/;
		} else {
			return getNumberOfEventsByEventType(eventType);
		}
	}

	private int getNumberOfEventsByEventType(EventType eventType) {
		Cursor cursor = null;
		try {
			cursor = EventsDatabaseWrapper.query(EventHelper.getUriForEventType(eventType), null, null, null, null);
			if (cursor != null) {
				cursor.moveToFirst();
				return cursor.getCount();
			}
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
		return 0;
	}

	@Override
	public BaseEvent readEvent(Uri uri) {
		Cursor cursor = null;
		try {
			cursor = EventsDatabaseWrapper.query(uri, null, null, null, null);
			if (cursor != null) {
				cursor.moveToFirst();
				if (cursor.getCount() > 0) {
					final BaseEvent event = EventHelper.makeEventFromCursor(cursor, uri);
					return event;
				}
			}
			throw new IllegalArgumentException("Could not find event with Uri " + uri.getPath());
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
	}

	@Override
	public void deleteEvents(List<Uri> eventUris, EventType eventType) {
		if (eventType == EventType.ALL) {
			throw new IllegalArgumentException("Can not deleteEvents for EventType.ALL");
		}
		EventsDatabaseWrapper.delete(eventUris, null, null);
	}

	@Override
	public void reset(EventType eventType) {
		if (eventType == EventType.ALL) {
			for (EventType et : EventType.values()) {
				if (et != EventType.ALL) {
                    EventsDatabaseWrapper.delete(EventHelper.getUriForEventType(et), null, null);
				}
			}
		} else {
            EventsDatabaseWrapper.delete(EventHelper.getUriForEventType(eventType), null, null);
		}
	}

	@Override
	public void setEventStatus(Uri eventUri, int status) {
		final ContentValues values = new ContentValues();
		values.put(BaseEvent.Columns.STATUS, status);
		final int numberOfRowsUpdated = EventsDatabaseWrapper.update(eventUri, values, null, null);
		if (numberOfRowsUpdated == 0) {
			throw new IllegalArgumentException("Could not find event with Uri " + eventUri.getPath());
		}
	}
}
