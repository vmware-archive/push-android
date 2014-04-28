package com.pivotal.cf.mobile.pushsdk.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashSet;
import java.util.Set;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static Set<DatabaseInitializer> initializers = new HashSet<DatabaseInitializer>();

    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "com.pivotal.cf.mobile.pushsdk.events.db";
    public static boolean isInitialized = false;

    public static synchronized void init() {
        if (needsInitializing()) {
            addInitializer(new EventsDatabaseInitializer());
            isInitialized = true;
        }
    }

	public static synchronized void addInitializer(final DatabaseInitializer initializer) {
		initializers.add(initializer);
	}

	public static synchronized boolean needsInitializing() {
		return !isInitialized;
	}

	public DatabaseHelper(final Context context, final CursorFactory factory) {
		super(context, DATABASE_NAME, factory, DATABASE_VERSION);
	}

	@Override
	public void onCreate(final SQLiteDatabase db) {
		for (final DatabaseInitializer initializer : initializers) {
			initializer.onCreate(db);
		}
	}

	@Override
	public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
		if (oldVersion < newVersion) {
			for (final DatabaseInitializer initializer : initializers) {
				initializer.onUpgrade(db, oldVersion, newVersion);
			}
		}
	}
	
	@Override
	public void onDowngrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
		if (newVersion < oldVersion) {
			for (final DatabaseInitializer initializer : initializers) {
				initializer.onDowngrade(db, oldVersion, newVersion);
			}
		}
	}
}
