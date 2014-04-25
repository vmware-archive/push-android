package com.pivotal.cf.mobile.pushsdk.database;

import android.net.Uri;

public class DatabaseConstants {
	public static final String AUTHORITY = "com.pivotal.cf.mobile.pushsdk.providers.EventsDatabase";
	public static final String EVENTS_TABLE_NAME = "events";
	public static final Uri EVENTS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + EVENTS_TABLE_NAME);
}
