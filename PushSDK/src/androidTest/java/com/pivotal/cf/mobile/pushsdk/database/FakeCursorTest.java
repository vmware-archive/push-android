package com.pivotal.cf.mobile.pushsdk.database;

import android.test.AndroidTestCase;

public class FakeCursorTest extends AndroidTestCase {

	private static final String COLUMN_NAME_1 = "FAKE";
	private static final String COLUMN_VALUE_1 = "fake";
	private static final int COLUMN_INDEX_1 = 0;
	
	private static final String COLUMN_NAME_2 = "BEANS";
	private static final int COLUMN_VALUE_2 = 5;
	private static final int COLUMN_INDEX_2 = 1;

	public void testAddField() {
		final FakeCursor fakeCursor = new FakeCursor();
		assertEquals(0, fakeCursor.getColumnCount());

		fakeCursor.addField(COLUMN_NAME_1, COLUMN_VALUE_1);

		assertEquals(1, fakeCursor.getColumnCount());
		assertEquals(COLUMN_NAME_1, fakeCursor.getColumnName(COLUMN_INDEX_1));
		assertEquals(COLUMN_INDEX_1, fakeCursor.getColumnIndex(COLUMN_NAME_1));
		assertEquals(-1, fakeCursor.getColumnIndex("BLAH BLAH"));
		assertEquals(COLUMN_INDEX_1, fakeCursor.getColumnIndexOrThrow(COLUMN_NAME_1));
		
		final String[] columnNames = fakeCursor.getColumnNames();
		assertEquals(1, columnNames.length);
		assertEquals(COLUMN_NAME_1, columnNames[0]);

		boolean exceptionCaught = false;
		try {
			fakeCursor.getColumnIndexOrThrow("BLAH BLAH");
		} catch (IllegalArgumentException e) {
			exceptionCaught = true;
		}
		assertTrue(exceptionCaught);
		
		assertEquals(COLUMN_VALUE_1, fakeCursor.getString(COLUMN_INDEX_1));
		assertNull(fakeCursor.getString(fakeCursor.getColumnCount()));

		fakeCursor.close();
	}
	
	public void testAddSomeFields() {
		final FakeCursor fakeCursor = new FakeCursor();
		
		fakeCursor.addField(COLUMN_NAME_1, COLUMN_VALUE_1);
		fakeCursor.addField(COLUMN_NAME_2, COLUMN_VALUE_2);
		
		assertEquals(2, fakeCursor.getColumnCount());
		
		assertEquals(COLUMN_INDEX_1, fakeCursor.getColumnIndex(COLUMN_NAME_1));
		assertEquals(COLUMN_NAME_1, fakeCursor.getColumnName(COLUMN_INDEX_1));
		assertEquals(COLUMN_VALUE_1, fakeCursor.getString(COLUMN_INDEX_1));
		
		assertEquals(COLUMN_INDEX_2, fakeCursor.getColumnIndex(COLUMN_NAME_2));
		assertEquals(COLUMN_NAME_2, fakeCursor.getColumnName(COLUMN_INDEX_2));
		assertEquals(COLUMN_VALUE_2, fakeCursor.getInt(COLUMN_INDEX_2));
		
		fakeCursor.close();
	}
}
