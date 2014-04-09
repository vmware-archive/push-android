package org.omnia.pushsdk.model.utilities;

import android.database.Cursor;
import android.net.Uri;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;

import org.omnia.pushsdk.database.DatabaseConstants;
import org.omnia.pushsdk.database.EventsStorage;
import org.omnia.pushsdk.database.urihelpers.EventsUriHelper;
import org.omnia.pushsdk.database.urihelpers.UriHelper;
import org.omnia.pushsdk.model.EventBase;
import org.omnia.pushsdk.model.MessageReceiptEvent;

public class EventHelper {

	public static Uri getUriForEventType(EventsStorage.EventType eventType) {
		switch (eventType) {
		case ALL:
			return null; // no valid URI for type 'ALL'.
		case MESSAGE_RECEIPT:
			return DatabaseConstants.MESSAGE_RECEIPTS_CONTENT_URI;
		default:
			return null;
		}
	}

	public static EventsStorage.EventType getEventTypeForUri(final Uri uri) {

		final UriHelper uriHelper = EventsUriHelper.getUriHelper(uri);
		if (uriHelper.getDefaultTableName().equals(DatabaseConstants.MESSAGE_RECEIPTS_TABLE_NAME)) {
			return EventsStorage.EventType.MESSAGE_RECEIPT;
		}
		return null;
	}
	
	public static EventBase makeEventFromCursor(final Cursor cursor, final EventsStorage.EventType eventType) {
		switch (eventType) {
		case ALL:
			return null; // can't make "all"
		case MESSAGE_RECEIPT:
			return new MessageReceiptEvent(cursor);
		}
		return null;
	}

	public static EventBase makeEventFromCursor(final Cursor cursor, final Uri uri) {

		final UriHelper uriHelper = EventsUriHelper.getUriHelper(uri);
		if (uriHelper.getDefaultTableName().equals(DatabaseConstants.MESSAGE_RECEIPTS_TABLE_NAME)) {
			return new MessageReceiptEvent(cursor);
		}
		return null;
	}
	
	public static EventBase deserializeEvent(String eventType, JsonElement jsonElement, JsonDeserializationContext jsonDeserializationContext) {
		if (eventType.equals(MessageReceiptEvent.TYPE)) {
			return (MessageReceiptEvent) jsonDeserializationContext.deserialize(jsonElement, MessageReceiptEvent.class);
		}
		return null;
	}
}
