package org.omnia.pushsdk.database;

import android.database.sqlite.SQLiteDatabase;

public interface DatabaseInitializer {

	public void onCreate(final SQLiteDatabase db);
	
	public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion);
	
	public void onDowngrade(final SQLiteDatabase db, final int oldVersion, final int newVersion);
	
}
