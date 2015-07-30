package io.pivotal.android.push.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import io.pivotal.android.push.model.analytics.Event;
import io.pivotal.android.push.util.Logger;

public class Database extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_FILENAME = "io.pivotal.android.push.events.db";
    public static final String AUTHORITY = "io.pivotal.android.push.providers.EventsDatabase";
    public static final String EVENTS_TABLE_NAME = "events";
    public static final Uri EVENTS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + EVENTS_TABLE_NAME);

    public Database(final Context context, final CursorFactory factory) {
        super(context, DATABASE_FILENAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        final String[] createTableStatements = new String[]{
            Event.getCreateTableSqlStatement()
        };
        for (final String sql : createTableStatements) {
            db.execSQL(sql);
        }
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        if (oldVersion < newVersion) {

            // TODO - do something more sophisticated on upgrading database schema IF it is important to keep
            // data. Since this database is just a cache, it is unlikely that keeping data is important.
            final String[] dropTableStatements = new String[] {
                Event.getDropTableSqlStatement()
            };
            for (final String dropTableStatement : dropTableStatements) {
                db.execSQL(dropTableStatement);
            }
            onCreate(db);
        }
    }

    @Override
    public void onDowngrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        if (newVersion < oldVersion) {
            Logger.w("Downgrading the database is not supported.");
        }
    }
}
