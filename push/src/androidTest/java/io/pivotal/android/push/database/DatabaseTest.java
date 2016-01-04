package io.pivotal.android.push.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.test.MoreAsserts;
import android.test.RenamingDelegatingContext;

import io.pivotal.android.push.model.analytics.AnalyticsEvent;
import io.pivotal.android.push.model.analytics.DummyEvent;

public class DatabaseTest extends AndroidTestCase {

    private static final String TABLE_NAME = Database.EVENTS_TABLE_NAME;
    private static final String TEST_FILE_PREFIX = "test_";
    private static final String TEST_DEVICE_UUID_1 = "TEST-DEVICE-UUID-1";
    private static final String TEST_DEVICE_UUID_2 = "TEST-DEVICE-UUID-2";
    private Context context;
    private SQLiteDatabase database;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        context = new RenamingDelegatingContext(getContext(), TEST_FILE_PREFIX);
    }

    public void testDatabaseSettings() {
        database = getWritableDatabase(Database.DATABASE_VERSION);
        assertEquals(Database.DATABASE_VERSION, database.getVersion());
    }

    public void testDatabaseOpen() {
        database = getWritableDatabase(Database.DATABASE_VERSION);
        assertTrue(database.isOpen());
    }

    public void testDatabaseWriteable() {
        database = getWritableDatabase(Database.DATABASE_VERSION);
        assertFalse(database.isReadOnly());
    }

    public void testDatabaseEmpty() {
        database = getWritableDatabase(Database.DATABASE_VERSION);
        assertTableEmpty(TABLE_NAME);
    }

    public void testInsertOneRow() {
        database = getWritableDatabase(Database.DATABASE_VERSION);
        final AnalyticsEvent event = DummyEvent.getEvent(TEST_DEVICE_UUID_1);
        long rowId = database.insert(TABLE_NAME, null, event.getContentValues(Database.DATABASE_VERSION));
        MoreAsserts.assertNotEqual(-1, rowId);
        assertTableRowCount(1, TABLE_NAME);
    }

    public void testInsertTwoRows() {
        database = getWritableDatabase(Database.DATABASE_VERSION);

        final AnalyticsEvent event1 = DummyEvent.getEvent(TEST_DEVICE_UUID_1);
        long rowId1 = database.insert(TABLE_NAME, null, event1.getContentValues(Database.DATABASE_VERSION));
        MoreAsserts.assertNotEqual(-1, rowId1);

        final AnalyticsEvent event2 = DummyEvent.getEvent(TEST_DEVICE_UUID_2);
        long rowId2 = database.insert(TABLE_NAME, null, event2.getContentValues(Database.DATABASE_VERSION));
        MoreAsserts.assertNotEqual(-1, rowId2);

        assertTableRowCount(2, TABLE_NAME);
    }

    public void testMigrateFromVersion1ToVersion2() {

        database = getWritableDatabase(1);
        assertEquals(1, database.getVersion());

        final String schema1 = getTableSchema();
        assertFalse(schema1.contains("sdkVersion"));
        assertFalse(schema1.contains("platformType"));
        assertFalse(schema1.contains("platformUuid"));

        final AnalyticsEvent event1 = DummyEvent.getEvent(TEST_DEVICE_UUID_1);
        long rowId1 = database.insert(TABLE_NAME, null, event1.getContentValues(1));
        MoreAsserts.assertNotEqual(-1, rowId1);
        assertTableRowCount(1, TABLE_NAME);

        database = getWritableDatabase(2);

        assertEquals(2, database.getVersion());

        final String schema2 = getTableSchema();
        assertTrue(schema2.contains("sdkVersion"));
        assertFalse(schema2.contains("platformType"));
        assertFalse(schema2.contains("platformUuid"));

        assertTableRowCount(1, TABLE_NAME);  // Assert that the database did not drop the row added above
    }

    public void testMigrateFromVersion2ToVersion3() {

        database = getWritableDatabase(2);
        assertEquals(2, database.getVersion());

        final String schema2 = getTableSchema();
        assertTrue(schema2.contains("sdkVersion"));
        assertFalse(schema2.contains("platformType"));
        assertFalse(schema2.contains("platformUuid"));

        final AnalyticsEvent event1 = DummyEvent.getEvent(TEST_DEVICE_UUID_1);
        long rowId1 = database.insert(TABLE_NAME, null, event1.getContentValues(2));
        MoreAsserts.assertNotEqual(-1, rowId1);
        assertTableRowCount(1, TABLE_NAME);

        database = getWritableDatabase(3);

        assertEquals(3, database.getVersion());

        final String schema3 = getTableSchema();
        assertTrue(schema3.contains("sdkVersion"));
        assertTrue(schema3.contains("platformType"));
        assertTrue(schema3.contains("platformUuid"));

        assertTableRowCount(1, TABLE_NAME);  // Assert that the database did not drop the row added above
    }

    public void testMigrateFromVersion1ToVersion3() {

        database = getWritableDatabase(1);
        assertEquals(1, database.getVersion());

        final String schema1 = getTableSchema();
        assertFalse(schema1.contains("sdkVersion"));
        assertFalse(schema1.contains("platformType"));
        assertFalse(schema1.contains("platformUuid"));

        final AnalyticsEvent event1 = DummyEvent.getEvent(TEST_DEVICE_UUID_1);
        long rowId1 = database.insert(TABLE_NAME, null, event1.getContentValues(1));
        MoreAsserts.assertNotEqual(-1, rowId1);
        assertTableRowCount(1, TABLE_NAME);

        database = getWritableDatabase(3);

        assertEquals(3, database.getVersion());

        final String schema3 = getTableSchema();
        assertTrue(schema3.contains("sdkVersion"));
        assertTrue(schema3.contains("platformType"));
        assertTrue(schema3.contains("platformUuid"));

        assertTableRowCount(1, TABLE_NAME);  // Assert that the database did not drop the row added above
    }

    private String getTableSchema() {
        final String sql = "SELECT sql FROM sqlite_master WHERE type='table' AND name=?";
        final Cursor cursor = database.rawQuery(sql, new String[] { Database.EVENTS_TABLE_NAME });
        assertNotNull(cursor);
        cursor.moveToFirst();
        assertEquals(1, cursor.getCount());
        return cursor.getString(0);
    }

    private SQLiteDatabase getWritableDatabase(int databaseVersion) {
        final Database helper = new Database(context, null, databaseVersion);
        assertNotNull(helper);
        final SQLiteDatabase database = helper.getWritableDatabase();
        assertNotNull(database);
        return database;
    }

    private void assertTableEmpty(final String tableName) {
        assertTableRowCount(0, tableName);
    }

    private void assertTableRowCount(final int rowCount, final String tableName) {
        Cursor c = null;
        try {
            c = database.rawQuery("SELECT * FROM " + tableName, null);
            assertNotNull(c);
            c.moveToFirst();
            assertEquals(rowCount, c.getCount());
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }
}
