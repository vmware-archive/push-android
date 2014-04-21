package com.pivotal.cf.mobile.pushsdk.database;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static Set<DatabaseInitializer> initializers = new HashSet<DatabaseInitializer>();

	private static String databaseName = null;
	private static int databaseVersion = -1;

	public static synchronized void init(final String databaseName, final int databaseVersion) {
		DatabaseHelper.databaseName = databaseName;
		DatabaseHelper.databaseVersion = databaseVersion;
	}

	public static synchronized void addInitializer(final DatabaseInitializer initializer) {
		initializers.add(initializer);
	}

	public static synchronized boolean needsInitializing() {
		return databaseName == null || databaseVersion < 0;
	}

	public DatabaseHelper(final Context context, final CursorFactory factory) {
		super(context, databaseName, factory, databaseVersion);
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
