package io.pivotal.android.push.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import java.util.List;

import io.pivotal.android.push.model.analytics.AnalyticsEvent;
import io.pivotal.android.push.util.Logger;

public class Database extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_FILENAME = "io.pivotal.android.push.events.db";
    public static final String AUTHORITY = "io.pivotal.android.push.providers.EventsDatabase";
    public static final String EVENTS_TABLE_NAME = "events";
    public static final Uri EVENTS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + EVENTS_TABLE_NAME);
    private final int databaseVersion;

    public Database(final Context context, final CursorFactory factory, int databaseVersion) {
        super(context, DATABASE_FILENAME, factory, databaseVersion);
        this.databaseVersion = databaseVersion;
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        final String[] createTableStatements = new String[] {
            AnalyticsEvent.getCreateTableSqlStatement(databaseVersion)
        };
        for (final String sql : createTableStatements) {
            db.execSQL(sql);
        }
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {

        try {
            final List<String> upgradeStatements = AnalyticsEvent.getDatabaseMigrationCommands(oldVersion, newVersion);

            if (upgradeStatements == null) {
                Logger.i("Unknown database migration. Version " + oldVersion + " to version " + newVersion + ". Recreating database.");
                recreateDatabase(db);

            } else {

                Logger.i("Migrating EVENTS table from version " + oldVersion + " to version " + newVersion + ".");
                for (final String sql : upgradeStatements) {
                    db.execSQL(sql);
                }
            }
        } catch (SQLException e) {
            Logger.ex("Exception while migrating events table from version " + oldVersion + " to version " + newVersion + ". Recreating database.", e);
            recreateDatabase(db);
        }
    }

    @Override
    public void onDowngrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        if (newVersion < oldVersion) {
            Logger.w("Downgrading the database from version " + oldVersion + " to version " + newVersion + ". Recreating database.");
            recreateDatabase(db);
        }
    }

    private void recreateDatabase(SQLiteDatabase db) {
        final String[] dropTableStatements = new String[] {
                AnalyticsEvent.getDropTableSqlStatement()
        };
        for (final String dropTableStatement : dropTableStatements) {
            db.execSQL(dropTableStatement);
        }
        onCreate(db);
    }
}
