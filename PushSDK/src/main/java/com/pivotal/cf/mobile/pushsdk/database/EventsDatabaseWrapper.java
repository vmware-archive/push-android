package com.pivotal.cf.mobile.pushsdk.database;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteFullException;
import android.database.sqlite.SQLiteQuery;
import android.net.Uri;

import com.pivotal.cf.mobile.pushsdk.database.urihelpers.DeleteParams;
import com.pivotal.cf.mobile.pushsdk.database.urihelpers.EventsUriHelper;
import com.pivotal.cf.mobile.pushsdk.database.urihelpers.QueryParams;
import com.pivotal.cf.mobile.pushsdk.database.urihelpers.UpdateParams;
import com.pivotal.cf.mobile.pushsdk.util.DebugUtil;
import com.pivotal.cf.mobile.pushsdk.util.PushLibLogger;

import java.util.List;
import java.util.Set;

public class EventsDatabaseWrapper {

    private static final int MAX_DATABASE_SIZE_RELEASE = 1024 * 1024; // 1 MB
    private static final int MAX_DATABASE_SIZE_DEBUG = 32 * 1024; // 32 kB
    private static final Object lock = new Object();
    private static SQLiteDatabase database;

    private static SQLiteDatabase getDatabase() {
        synchronized (lock) {
            return database;
        }
    }

    // Returns 'true' if the database instance was initialized.
    // Returns 'false' if the database instance was already initialized.
    public static boolean createDatabaseInstance(Context context) {

        if (EventsDatabaseHelper.needsInitializing()) {
            throw new IllegalStateException("EventsDatabaseHelper needs initializing.");
        }

        synchronized (lock) {
            if (database == null) {
                final DebugCursorFactory factory = new DebugCursorFactory();
                final EventsDatabaseHelper databaseHelper = new EventsDatabaseHelper(context, factory);
                database = databaseHelper.getWritableDatabase();

                final long maxDatabaseSize;
                if (DebugUtil.getInstance(context).isDebuggable()) {
                    maxDatabaseSize = MAX_DATABASE_SIZE_DEBUG;
                } else {
                    maxDatabaseSize = MAX_DATABASE_SIZE_RELEASE;
                }
                database.setMaximumSize(maxDatabaseSize);
                PushLibLogger.fd("Database has been initialized for package '%s' with maximum size %d kB.", context.getPackageName(), maxDatabaseSize / 1024);
                return true;
            } else {
                return false;
            }
        }
    }

    // Only used by unit tests
    public static void removeDatabaseInstance() {
        synchronized (lock) {
            database = null;
        }
    }

    public static Cursor query(final Uri uri, final String[] projection, final String whereClause, final String[] whereArgs, final String sortOrder) {
        final QueryParams queryParams = EventsUriHelper.getUriHelper(uri).getQueryParams(uri, projection, whereClause, whereArgs, sortOrder);
        return getDatabase().query(EventsUriHelper.getUriHelper(uri).getDefaultTableName(), queryParams.projection, queryParams.whereClause, queryParams.whereArgs, null, null, queryParams.sortOrder);
    }

    public static int update(Uri uri, ContentValues values, String whereClause, String[] whereArgs) {

        try {
            return tryUpdate(uri, values, whereClause, whereArgs);
        } catch (SQLiteFullException e) {
            PushLibLogger.w("Note: database is full. Cleaning up");
            if (cleanup()) {
                return tryUpdate(uri, values, whereClause, whereArgs);
            }
        } catch (SQLException e) {
            PushLibLogger.ex("Caught error upon updating into table " + EventsUriHelper.getUriHelper(uri).getDefaultTableName(), e);
        }
        return -1;
    }

    private static int tryUpdate(Uri uri, ContentValues values, String whereClause, String[] whereArgs) {
        final UpdateParams updateParams = EventsUriHelper.getUriHelper(uri).getUpdateParams(uri, whereClause, whereArgs);
        return getDatabase().update(EventsUriHelper.getUriHelper(uri).getDefaultTableName(), values, updateParams.whereClause, updateParams.whereArgs);
    }

    public static int delete(Uri uri, String whereClause, String[] whereArgs) {
        final DeleteParams deleteParams = EventsUriHelper.getUriHelper(uri).getDeleteParams(uri, whereClause, whereArgs);
        return getDatabase().delete(EventsUriHelper.getUriHelper(uri).getDefaultTableName(), deleteParams.whereClause, deleteParams.whereArgs);
    }

    public static void delete(final List<Uri> eventUris, final String whereClause, final String[] whereArgs) {
        final Runnable deleteRunnable = new Runnable() {

            @Override
            public void run() {
                for (Uri uri : eventUris) {
                    delete(uri, whereClause, whereArgs);
                }
            }
        };
        runInTransaction(deleteRunnable);
    }

    private static void runInTransaction(Runnable runnable) {
        SQLiteDatabase db = getDatabase();

        db.beginTransaction();
        try {
            runnable.run();
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public static Uri insert(final Uri uri, final ContentValues values) {
        try {
            return tryInsert(uri, values);
        } catch (SQLiteFullException e) {
            PushLibLogger.w("Note: database is full. Cleaning up");
            if (cleanup()) {
                return tryInsert(uri, values);
            }
        } catch (SQLiteException e) {
            if (e.getMessage().contains("cannot commit")) {
                PushLibLogger.w("Note: database is full. Cleaning up");
                if (cleanup()) {
                    return tryInsert(uri, values);
                }
            } else {
                PushLibLogger.ex("Caught error upon inserting into table " + EventsUriHelper.getUriHelper(uri).getDefaultTableName(), e);
            }
        }
        return null;
    }

    private static Uri tryInsert(final Uri uri, final ContentValues values) {
        long rowId = getDatabase().insertOrThrow(EventsUriHelper.getUriHelper(uri).getDefaultTableName(), null, values);
        return ContentUris.withAppendedId(uri, rowId);
    }

    private static boolean cleanup() {
//		boolean journalCleanup = cleanupJournal();
        boolean largestTableCleanUp = cleanupLargestTable();
        return /*journalCleanup ||*/ largestTableCleanUp;
    }

//	private static boolean cleanupJournal() {
//		boolean journalCleanup = cleanup(DatabaseConstants.JOURNAL_TABLE_NAME);
//		return journalCleanup;
//	}

    private static boolean cleanupLargestTable() {
        final String largestTableName = getLargestTable();
        return (largestTableName != null && cleanup(largestTableName));
    }

    public static String getLargestTable() {
        final Set<String> tableNames = EventsUriHelper.getAllTableNames();
        String largestTableName = null;
        int largestNumberOfRows = -1;
        for (final String tableName : tableNames) {
            int rowsInTable = getNumberOfRowsInTable(tableName);
            if (rowsInTable >= 0 && largestNumberOfRows < rowsInTable) {
                largestNumberOfRows = rowsInTable;
                largestTableName = tableName;
            }
        }
        return largestTableName;
    }

    public static int getNumberOfRowsInTable(String tableName) {
        String rowCountQuery = "SELECT COUNT(ROWID) FROM " + tableName;
        Cursor c1 = null;
        try {
            c1 = getDatabase().rawQuery(rowCountQuery, null);
            c1.moveToFirst();
            return c1.getInt(0);

        } catch (Exception e) {
            PushLibLogger.w(e);
            return -1;
        } finally {
            if (c1 != null) {
                c1.close();
            }
        }
    }

    /**
     * Deletes half of the items from the given table. The oldest items are deleted.
     *
     * @param tableName The table to clean up
     * @return success or failure
     */
    public static boolean cleanup(final String tableName) {
        String getAvgRowIdQuery = "SELECT AVG(ROWID) FROM " + tableName;
        Cursor c1 = null;
        int avgRowId;
        int rowsDeleted = 0;
        try {
            c1 = getDatabase().rawQuery(getAvgRowIdQuery, null);
            c1.moveToFirst();
            avgRowId = c1.getInt(0);

            rowsDeleted = getDatabase().delete(tableName, "ROWID <= ?", new String[]{String.valueOf(avgRowId)});
        } catch (Exception e) {
            PushLibLogger.w(e);
            return false;
        } finally {
            if (c1 != null) {
                c1.close();
            }
        }
        PushLibLogger.i("Database cleanup removed " + rowsDeleted + " rows from table " + tableName);
        return true;
    }

    private static class DebugCursorFactory implements CursorFactory {
        @Override
        @SuppressWarnings("deprecation")
        public Cursor newCursor(final SQLiteDatabase db, final SQLiteCursorDriver masterQuery, final String editTable, final SQLiteQuery query) {
            return new SQLiteCursor(db, masterQuery, editTable, query);
        }
    }
}
