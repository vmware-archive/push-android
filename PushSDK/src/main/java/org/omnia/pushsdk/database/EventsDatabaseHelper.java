package org.omnia.pushsdk.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

import org.omnia.pushsdk.util.PushLibLogger;

public class EventsDatabaseHelper extends DatabaseHelper {

	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "org.omnia.pushsdk.events.db";

	public static void init() {
		if (needsInitializing()) {
			PushLibLogger.d("Initializing EventsDatabaseHelper");
			init(DATABASE_NAME, DATABASE_VERSION);
			addInitializer(new EventsDatabaseInitializer());
		}
	}

	public EventsDatabaseHelper(Context context, CursorFactory factory) {
		super(context, factory);
	}
}
