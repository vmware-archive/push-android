package com.pivotal.cf.mobile.pushsdk.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.pivotal.cf.mobile.pushsdk.model.BaseEvent;
import com.pivotal.cf.mobile.pushsdk.model.utilities.EventHelper;

import java.util.LinkedList;
import java.util.List;

public class DatabaseEventsStorage implements EventsStorage {

	public DatabaseEventsStorage() {
	}

	@Override
	public Uri saveEvent(BaseEvent event) {
		final ContentValues contentValues = event.getContentValues();
		final Uri uri = EventsDatabaseWrapper.insert(EventHelper.getUriForEventType(), contentValues);
		return uri;
	}

	@Override
	public List<Uri> getEventUris() {
        return getGeneralQuery(null, null, null, null);
	}

	public List<Uri> getEventUrisWithStatus(int status) {
        return getGeneralQuery(null, "status = ?", new String[] { String.valueOf(status) }, null);
	}

	private List<Uri> getGeneralQuery(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		final Uri uri = EventHelper.getUriForEventType();
		Cursor cursor = null;
		try {
			cursor = EventsDatabaseWrapper.query(uri, projection, selection, selectionArgs, sortOrder);
			return getEventUrisFromCursor(cursor);
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
	}

	private List<Uri> getEventUrisFromCursor(final Cursor cursor) {
		final List<Uri> uris = new LinkedList<Uri>();
		if (cursor != null) {
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
				final int id = BaseEvent.getRowIdFromCursor(cursor);
				final Uri uri = Uri.withAppendedPath(EventHelper.getUriForEventType(), String.valueOf(id));
				uris.add(uri);
			}
		}
		return uris;
	}

	@Override
	public int getNumberOfEvents() {
		Cursor cursor = null;
		try {
			cursor = EventsDatabaseWrapper.query(EventHelper.getUriForEventType(), null, null, null, null);
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
	public void deleteEvents(List<Uri> eventUris) {
		EventsDatabaseWrapper.delete(eventUris, null, null);
	}

	@Override
	public void reset() {
        EventsDatabaseWrapper.delete(EventHelper.getUriForEventType(), null, null);
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
