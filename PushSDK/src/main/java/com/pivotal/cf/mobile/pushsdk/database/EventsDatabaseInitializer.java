package com.pivotal.cf.mobile.pushsdk.database;

import android.database.sqlite.SQLiteDatabase;

import com.pivotal.cf.mobile.pushsdk.model.MessageReceiptEvent;

public class EventsDatabaseInitializer implements DatabaseInitializer {

    @Override
    public void onCreate(final SQLiteDatabase db) {
        final String[] createTableStatements = new String[] {
             MessageReceiptEvent.getCreateTableSqlStatement()
        };
        for (final String sql : createTableStatements) {
            db.execSQL(sql);
        }
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        // TODO - do something more sophisticated on upgrading database schema IF it
        // is important to keep data. Since this database is just a cache, it is unlikely
        // that keeping data is important.
        final String[] dropTableStatements = new String[] {
            MessageReceiptEvent.getDropTableSqlStatement()
        };
        for (final String dropTableStatement : dropTableStatements) {
            db.execSQL(dropTableStatement);
        }
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Downgrade is not supported.
    }
}