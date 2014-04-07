package org.omnia.pushsdk.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import org.omnia.pushsdk.model.MessageReceiptEvent;

public class MonitoringDatabaseHelperTest extends AndroidTestCase {

	private static final String TABLE_NAME = DatabaseConstants.MESSAGE_RECEIPTS_TABLE_NAME;
	private static final String TEST_FILE_PREFIX = "test_";
    private static final String TEST_VARIANT_UUID_1 = "TEST-VARIANT-UUID-1";
    private static final String TEST_MESSAGE_UUID_1 = "TEST-MESSAGE-UUID-1";
    private static final String TEST_VARIANT_UUID_2 = "TEST-VARIANT-UUID-2";
    private static final String TEST_MESSAGE_UUID_2 = "TEST-MESSAGE-UUID-2";
    private Context CONTEXT;
	private SQLiteDatabase database;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		CONTEXT = new RenamingDelegatingContext(getContext(), TEST_FILE_PREFIX);
		database = getWritableDatabase();
	}

	public void testDatabaseSettings() {
		assertEquals(1, database.getVersion());
	}

	public void testDatabaseOpen() {
		assertTrue(database.isOpen());
	}

	public void testDatabaseWriteable() {
		assertFalse(database.isReadOnly());
	}

	public void testDatabaseEmpty() {
		assertTableEmpty(TABLE_NAME);
	}

	public void testInsertOneRow() {
        final MessageReceiptEvent event = MessageReceiptEvent.getMessageReceiptEvent(TEST_VARIANT_UUID_1, TEST_MESSAGE_UUID_1);
		long rowId = database.insert(TABLE_NAME, null, event.getContentValues());
		assertFalse(-1 == rowId);
		assertTableRowCount(1, TABLE_NAME);
	}

	public void testInsertTwoRows() {
        final MessageReceiptEvent event1 = MessageReceiptEvent.getMessageReceiptEvent(TEST_VARIANT_UUID_1, TEST_MESSAGE_UUID_1);
		long rowId1 = database.insert(TABLE_NAME, null, event1.getContentValues());
		assertFalse(-1 == rowId1);
        final MessageReceiptEvent event2 = MessageReceiptEvent.getMessageReceiptEvent(TEST_VARIANT_UUID_2, TEST_MESSAGE_UUID_2);
		long rowId2 = database.insert(TABLE_NAME, null, event2.getContentValues());
		assertFalse(-1 == rowId2);
		assertTableRowCount(2, TABLE_NAME);
	}

	private SQLiteDatabase getWritableDatabase() {
		final EventsDatabaseHelper helper = new EventsDatabaseHelper(CONTEXT, null);
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
				c = null;
			}
		}
	}

}
