package com.pivotal.cf.mobile.pushsdk.model.utilities;

import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.pivotal.cf.mobile.pushsdk.database.DatabaseConstants;
import com.pivotal.cf.mobile.pushsdk.database.urihelpers.EventsUriHelper;
import com.pivotal.cf.mobile.pushsdk.database.urihelpers.UriHelper;
import com.pivotal.cf.mobile.pushsdk.model.BaseEvent;

public class EventHelper {

	public static Uri getUriForEventType() {
        return DatabaseConstants.EVENTS_CONTENT_URI;
	}

	public static BaseEvent makeEventFromCursor(final Cursor cursor) {
        return new BaseEvent(cursor);
	}

	public static BaseEvent makeEventFromCursor(final Cursor cursor, final Uri uri) {

		final UriHelper uriHelper = EventsUriHelper.getUriHelper(uri);
		if (uriHelper.getDefaultTableName().equals(DatabaseConstants.EVENTS_TABLE_NAME)) {
			return new BaseEvent(cursor);
		}
		return null;
	}

    public static BaseEvent copyEvent(BaseEvent source) {
        return new BaseEvent(source);
    }

	public static BaseEvent deserializeEvent(JsonElement jsonElement, JsonDeserializationContext jsonDeserializationContext) {
        return (BaseEvent) jsonDeserializationContext.deserialize(jsonElement, BaseEvent.class);
	}

    public static BaseEvent readEventFromParcel(Parcel parcel) {
        return parcel.readParcelable(BaseEvent.class.getClassLoader());
    }
}
