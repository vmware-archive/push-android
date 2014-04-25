package com.pivotal.cf.mobile.pushsdk.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class EventsDatabaseHelper extends DatabaseHelper {

	public static final int DATABASE_VERSION = 3;
	public static final String DATABASE_NAME = "com.pivotal.cf.mobile.pushsdk.events.db";

	public static void init() {
		if (needsInitializing()) {
			init(DATABASE_NAME, DATABASE_VERSION);
			addInitializer(new EventsDatabaseInitializer());
		}
	}

	public EventsDatabaseHelper(Context context, CursorFactory factory) {
		super(context, factory);
	}
}
