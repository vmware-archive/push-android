package com.pivotal.cf.mobile.pushsdk.model.utilities;

import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.pivotal.cf.mobile.pushsdk.database.DatabaseConstants;
import com.pivotal.cf.mobile.pushsdk.database.EventsStorage;
import com.pivotal.cf.mobile.pushsdk.database.urihelpers.EventsUriHelper;
import com.pivotal.cf.mobile.pushsdk.database.urihelpers.UriHelper;
import com.pivotal.cf.mobile.pushsdk.model.BaseEvent;
import com.pivotal.cf.mobile.pushsdk.model.MessageReceiptEvent;

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
	
	public static BaseEvent makeEventFromCursor(final Cursor cursor, final EventsStorage.EventType eventType) {
		switch (eventType) {
		case ALL:
			return null; // can't make "all"
		case MESSAGE_RECEIPT:
			return new MessageReceiptEvent(cursor);
		}
		return null;
	}

	public static BaseEvent makeEventFromCursor(final Cursor cursor, final Uri uri) {

		final UriHelper uriHelper = EventsUriHelper.getUriHelper(uri);
		if (uriHelper.getDefaultTableName().equals(DatabaseConstants.MESSAGE_RECEIPTS_TABLE_NAME)) {
			return new MessageReceiptEvent(cursor);
		}
		return null;
	}

    public static BaseEvent copyEvent(BaseEvent source, EventsStorage.EventType eventType) {
        if (eventType == EventsStorage.EventType.MESSAGE_RECEIPT) {
            return new MessageReceiptEvent((MessageReceiptEvent) source);
        } else {
            throw new IllegalArgumentException("unsupported eventType: " + eventType);
        }
    }

	public static BaseEvent deserializeEvent(String eventType, JsonElement jsonElement, JsonDeserializationContext jsonDeserializationContext) {
		if (eventType.equals(MessageReceiptEvent.TYPE)) {
			return (MessageReceiptEvent) jsonDeserializationContext.deserialize(jsonElement, MessageReceiptEvent.class);
		}
		return null;
	}

    public static BaseEvent readEventFromParcel(Parcel parcel, EventsStorage.EventType eventType) {
        if (eventType == EventsStorage.EventType.MESSAGE_RECEIPT) {
            return parcel.readParcelable(MessageReceiptEvent.class.getClassLoader());
        }
        return null;
    }
}
