package io.pivotal.android.push.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.test.AndroidTestCase;

import java.util.LinkedList;
import java.util.List;

import io.pivotal.android.push.model.analytics.DummyEvent;
import io.pivotal.android.push.model.analytics.AnalyticsEvent;

public class DatabaseWrapperTest extends AndroidTestCase {

    private static final String TEST_DEVICE_UUID_1 = "TEST-DEVICE-UUID-1";
    private static final String TEST_DEVICE_UUID_2 = "TEST-DEVICE-UUID-2";
	private AnalyticsEvent EVENT_1;
	private AnalyticsEvent EVENT_2;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		EVENT_1 = DummyEvent.getEvent(TEST_DEVICE_UUID_1);
		EVENT_2 = DummyEvent.getEvent(TEST_DEVICE_UUID_2);
		resetDatabase();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		resetDatabase();
	}

	private void resetDatabase() {
		DatabaseWrapper.delete(Database.EVENTS_CONTENT_URI, null, null); // empty table
		assertNoEventsInDatabase();
	}

	public void testInsertOneEvent() {

		// Simple sanity test that inserts an Event into the table
		insertEvent(EVENT_1);
		assertEventCountInDatabase(1);
	}

	public void testInsertTwoEvents() {

		// Simple test that inserts two Events into the table
		insertEvent(EVENT_1);
		insertEvent(EVENT_2);
		assertEventCountInDatabase(2);
	}

	public void testInsertAndQueryOneEvent() {

		// Insert an Event into the table
		final Uri uri = insertEvent(EVENT_1);

		// Read it back and check that it matches
		final AnalyticsEvent event = getEvent(uri);
		assertEquals(EVENT_1, event);

		// Read it back using a different method and check that it matches
		final List<AnalyticsEvent> events = getAllEvents();
		assertEquals(1, events.size());
		assertEquals(EVENT_1, events.iterator().next());
	}

	public void testInsertAndQueryTwoEvents() {

		// Insert two Events into the table
		final Uri uri1 = insertEvent(EVENT_1);
		final Uri uri2 = insertEvent(EVENT_2);

		// Read them back individually and check they are correct
		final AnalyticsEvent event1 = getEvent(uri1);
		final AnalyticsEvent event2 = getEvent(uri2);
		assertEquals(EVENT_1, event1);
		assertEquals(EVENT_2, event2);

		// Assert that the crash records table contents match if we
		// query them all at once
		final List<AnalyticsEvent> events = getAllEvents();
		assertEquals(2, events.size());
		final List<AnalyticsEvent> comparisonSet = new LinkedList<AnalyticsEvent>();
		comparisonSet.add(EVENT_1);
		comparisonSet.add(EVENT_2);
		assertEquals(events, comparisonSet);
	}

	public void testQueryNonExistentEvent() {

		// Insert an Event into the table
		final Uri existingUri = insertEvent(EVENT_1);

		// Make up a URI that shouldn't exist
		final Uri nonExistingUri = Uri.withAppendedPath(Database.EVENTS_CONTENT_URI, "9999");
		assertFalse(existingUri.equals(nonExistingUri));

		// Try to read the non-existent URI from the table
		final AnalyticsEvent event = getEvent(nonExistingUri);
		assertNull(event);
	}

	public void testUpdateOneEvent() {

		// Insert an Event into the table
		final Uri uri = insertEvent(EVENT_1);

		// Read it back and change its status
		final AnalyticsEvent event = getEvent(uri);
		assertEquals(EVENT_1, event);
		event.setStatus(AnalyticsEvent.Status.POSTED);
		assertFalse(EVENT_1.getStatus() == event.getStatus());

		// Update the Event in the table
		final int rowsAffected = DatabaseWrapper.update(uri, event.getContentValues(), null, null);
		assertEquals(1, rowsAffected);

		// Read the Event back and verify that it was updated
		final AnalyticsEvent updatedEvent = getEvent(uri);
		assertEquals(event, updatedEvent);
	}

	public void testUpdateAllEvents() {

		// Insert three Events into the table and ensure they all have different URIs
		final Uri uri1 = insertEvent(EVENT_1);
		final Uri uri2 = insertEvent(EVENT_1);
		final Uri uri3 = insertEvent(EVENT_1);
		assertFalse(uri1.equals(uri2));
		assertFalse(uri2.equals(uri3));
		assertFalse(uri1.equals(uri3));
		assertFalse(EVENT_1.equals(EVENT_2));

		// Update them all at once
		final int rowsAffected = DatabaseWrapper.update(Database.EVENTS_CONTENT_URI, EVENT_2.getContentValues(), null, null);
		assertEquals(3, rowsAffected);

		// Read them all back and verify they were updated
		final AnalyticsEvent updatedEvent1 = getEvent(uri1);
		assertEquals(updatedEvent1, EVENT_2);
		final AnalyticsEvent updatedEvent2 = getEvent(uri2);
		assertEquals(updatedEvent2, EVENT_2);
		final AnalyticsEvent updatedEvent3 = getEvent(uri3);
		assertEquals(updatedEvent3, EVENT_2);
	}

	public void testDeleteAll() {

		// Insert a bunch of Events into the table
		insertEvent(EVENT_1);
		insertEvent(EVENT_1);
		insertEvent(EVENT_1);
		assertEventCountInDatabase(3);

		// Delete all of them
		final int rowsDeleted = DatabaseWrapper.delete(Database.EVENTS_CONTENT_URI, null, null);
		assertEquals(3, rowsDeleted);
		assertNoEventsInDatabase();
	}

	public void testUpdateOneOfTwoEvents() {

		// Insert two Events into the table
		final Uri uri1 = insertEvent(EVENT_1);
		final Uri uri2 = insertEvent(EVENT_2);
		assertFalse(uri1.equals(uri2));

		// Read the first one back and change its status
		final AnalyticsEvent event1 = getEvent(uri1);
		assertEquals(EVENT_1, event1);
		event1.setStatus(AnalyticsEvent.Status.POSTED);
		assertFalse(EVENT_1.getStatus() == event1.getStatus());
		final int rowsAffected1 = DatabaseWrapper.update(uri1, event1.getContentValues(), null, null);
		assertEquals(1, rowsAffected1);

		// Assert that the update record was modified correctly after reading back
		final AnalyticsEvent updatedEvent1 = getEvent(uri1);
		assertEquals(event1, updatedEvent1);

		// Assert that the other record in the table wasn't changed
		final AnalyticsEvent event2 = getEvent(uri2);
		assertEquals(EVENT_2, event2);
		assertFalse(event2.equals(updatedEvent1));
		assertFalse(EVENT_1.equals(updatedEvent1));
	}

	public void testDeleteTwoEvents() {

		// Insert two Events into the table
		final Uri uri1 = insertEvent(EVENT_1);
		final Uri uri2 = insertEvent(EVENT_2);
		assertFalse(uri1.equals(uri2));
		assertEventCountInDatabase(2);

		// Delete the first one
		final int rowsAffected1 = DatabaseWrapper.delete(uri1, null, null);
		assertEventCountInDatabase(1);
		assertEquals(1, rowsAffected1);

		// Delete the other one
		final int rowsAffected2 = DatabaseWrapper.delete(uri2, null, null);
		assertEquals(1, rowsAffected2);
		assertEventCountInDatabase(0);
	}

	private List<AnalyticsEvent> getAllEvents() {
		Cursor c = null;
		try {
			List<AnalyticsEvent> events = new LinkedList<AnalyticsEvent>();
			c = DatabaseWrapper.query(Database.EVENTS_CONTENT_URI, null, null, null, null);
			assertNotNull(c);
			for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
				final AnalyticsEvent event = new AnalyticsEvent(c);
				events.add(event);
			}
			return events;
		} finally {
			if (c != null) {
				c.close();
				c = null;
			}
		}
	}

	private AnalyticsEvent getEvent(final Uri uri) {
		Cursor c = null;
		try {
			c = DatabaseWrapper.query(uri, null, null, null, null);
			assertNotNull(c);
			c.moveToFirst();
			if (c.isAfterLast())
				return null;
			final AnalyticsEvent event = new AnalyticsEvent(c);
			return event;
		} finally {
			if (c != null) {
				c.close();
				c = null;
			}
		}
	}

	private Uri insertEvent(AnalyticsEvent event) {
		final Uri uri = DatabaseWrapper.insert(Database.EVENTS_CONTENT_URI, event.getContentValues());
		assertNotNull(uri);
		return uri;
	}

	private void assertNoEventsInDatabase() {
		assertEventCountInDatabase(0);
	}

	private void assertEventCountInDatabase(int rowCount) {
		assertEquals(rowCount, DatabaseWrapper.getNumberOfRowsInTable(Database.EVENTS_TABLE_NAME));
	}

	public void testGetNumberOfRowsInTableForBadTablenames() {
		assertEquals(-1, DatabaseWrapper.getNumberOfRowsInTable(null));
		assertEquals(-1, DatabaseWrapper.getNumberOfRowsInTable("COBRA COMMANDER WUZ HERE"));
	}

	public void testBadInsert() {
		ContentValues values = EVENT_1.getContentValues();
		values.put(BaseColumns._ID, "THIS AIN'T NO ID");
		assertNull(DatabaseWrapper.insert(Database.EVENTS_CONTENT_URI, values));
	}

	public void testBadUpdate() {
		ContentValues values = EVENT_1.getContentValues();
		final Uri uri = DatabaseWrapper.insert(Database.EVENTS_CONTENT_URI, values);
		assertNotNull(uri);
		values.put(BaseColumns._ID, "THIS AIN'T NO ID");
		assertEquals(-1, DatabaseWrapper.update(uri, values, null, null));
	}
}
