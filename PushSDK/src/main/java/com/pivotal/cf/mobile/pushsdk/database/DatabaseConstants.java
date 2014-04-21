package com.pivotal.cf.mobile.pushsdk.database;

import android.net.Uri;

public class DatabaseConstants {
	public static final String AUTHORITY = "com.pivotal.cf.mobile.pushsdk.providers.EventsDatabase";
	public static final String MESSAGE_RECEIPTS_TABLE_NAME = "message_receipts";
	public static final Uri MESSAGE_RECEIPTS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + MESSAGE_RECEIPTS_TABLE_NAME);
}
