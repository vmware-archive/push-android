package io.pivotal.android.push.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import java.util.LinkedList;
import java.util.List;

import io.pivotal.android.push.model.analytics.AnalyticsEvent;


public class DatabaseAnalyticsEventsStorage implements AnalyticsEventsStorage {

	public DatabaseAnalyticsEventsStorage() {
	}

	@Override
	public Uri saveEvent(AnalyticsEvent event) {
		final ContentValues contentValues = event.getContentValues();
		final Uri uri = DatabaseWrapper.insert(Database.EVENTS_CONTENT_URI, contentValues);
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
		Cursor cursor = null;
		try {
			cursor = DatabaseWrapper.query(Database.EVENTS_CONTENT_URI, projection, selection, selectionArgs, sortOrder);
			return getEventUrisFromCursor(cursor);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	private List<Uri> getEventUrisFromCursor(final Cursor cursor) {
		final List<Uri> uris = new LinkedList<Uri>();
		if (cursor != null) {
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
				final int id = AnalyticsEvent.getRowIdFromCursor(cursor);
				final Uri uri = Uri.withAppendedPath(Database.EVENTS_CONTENT_URI, String.valueOf(id));
				uris.add(uri);
			}
		}
		return uris;
	}

	@Override
	public int getNumberOfEvents() {
		Cursor cursor = null;
		try {
			cursor = DatabaseWrapper.query(Database.EVENTS_CONTENT_URI, null, null, null, null);
			if (cursor != null) {
				cursor.moveToFirst();
				return cursor.getCount();
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return 0;
	}

	@Override
	public AnalyticsEvent readEvent(Uri uri) {
		Cursor cursor = null;
		try {
			cursor = DatabaseWrapper.query(uri, null, null, null, null);
			if (cursor != null) {
				cursor.moveToFirst();
				if (cursor.getCount() > 0) {
					final AnalyticsEvent event = new AnalyticsEvent(cursor);
					return event;
				}
			}
			throw new IllegalArgumentException("Could not find event with Uri " + uri.getPath());
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	@Override
	public void deleteEvents(List<Uri> eventUris) {
		DatabaseWrapper.delete(eventUris, null, null);
	}

	@Override
	public void reset() {
        DatabaseWrapper.delete(Database.EVENTS_CONTENT_URI, null, null);
	}

	@Override
	public void setEventStatus(Uri eventUri, int status) {
		final ContentValues values = new ContentValues();
		values.put(AnalyticsEvent.Columns.STATUS, status);
		final int numberOfRowsUpdated = DatabaseWrapper.update(eventUri, values, null, null);
		if (numberOfRowsUpdated == 0) {
			throw new IllegalArgumentException("Could not find event with Uri " + eventUri.getPath());
		}
	}
}
