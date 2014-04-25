package com.pivotal.cf.mobile.pushsdk.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.test.AndroidTestCase;

import com.pivotal.cf.mobile.pushsdk.model.BaseEvent;
import com.pivotal.cf.mobile.pushsdk.model.MessageReceiptEvent;

import java.util.LinkedList;
import java.util.List;

public class EventDatabaseWrapperTest extends AndroidTestCase {

    private static final String TEST_VARIANT_UUID_1 = "TEST-VARIANT-UUID-1";
    private static final String TEST_MESSAGE_UUID_1 = "TEST-MESSAGE-UUID-1";
    private static final String TEST_DEVICE_ID_1 = "TEST-DEVICE-ID-1";
    private static final String TEST_VARIANT_UUID_2 = "TEST-VARIANT-UUID-2";
    private static final String TEST_MESSAGE_UUID_2 = "TEST-MESSAGE-UUID-2";
    private static final String TEST_DEVICE_ID_2 = "TEST-DEVICE-ID-2";
	private MessageReceiptEvent MESSAGE_RECEIPT_EVENT_1;
	private MessageReceiptEvent MESSAGE_RECEIPT_EVENT_2;
//	private ApiValidationErrorEvent API_VALIDATION_ERROR_EVENT_1;
//	private ApiValidationErrorEvent API_VALIDATION_ERROR_EVENT_2;

	public EventDatabaseWrapperTest() {
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		MESSAGE_RECEIPT_EVENT_1 = MessageReceiptEvent.getMessageReceiptEvent(TEST_VARIANT_UUID_1, TEST_MESSAGE_UUID_1, TEST_DEVICE_ID_1);
		MESSAGE_RECEIPT_EVENT_2 = MessageReceiptEvent.getMessageReceiptEvent(TEST_VARIANT_UUID_2, TEST_MESSAGE_UUID_2, TEST_DEVICE_ID_2);
//		API_VALIDATION_ERROR_EVENT_1 = ApiValidationErrorEvent.getApiValidationErrorEvent(getContext(), TEST_URL, "POST", REQUEST_HEADERS, 400, "content/sad", "sad content", TEST_ERROR_MESSAGE_1, metadata);
//		API_VALIDATION_ERROR_EVENT_2 = ApiValidationErrorEvent.getApiValidationErrorEvent(getContext(), TEST_URL, "POST", REQUEST_HEADERS, 400, "content/sad", "sad content", TEST_ERROR_MESSAGE_2, metadata);
		resetDatabase();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		resetDatabase();
	}

	private void resetDatabase() {
		EventsDatabaseWrapper.delete(DatabaseConstants.MESSAGE_RECEIPTS_CONTENT_URI, null, null); // empty table
//		MonitoringDatabaseWrapper.delete(DatabaseConstants.API_VALIDATION_ERROR_CONTENT_URI, null, null); // empty table
		assertNoMessageReceiptsEventsInDatabase();
//		assertNoApiValidationErrorEventsInDatabase();
	}

	public void testInsertOneMessageReceiptEvent() {

		// Simple sanity test that inserts an Event into the table
		insertMessageReceiptEvent(MESSAGE_RECEIPT_EVENT_1);
		assertMessageReceiptEventCountInDatabase(1);
	}

//	public void testInsertOneApiValidationErrorEvent() {
//
//		// Simple sanity test that inserts an Event into the table
//		insertApiValidationErrorEvent(API_VALIDATION_ERROR_EVENT_1);
//		assertApiValidationErrorEventCountInDatabase(1);
//	}

	public void testInsertTwoMessageReceiptEvents() {

		// Simple test that inserts two Events into the table
		insertMessageReceiptEvent(MESSAGE_RECEIPT_EVENT_1);
		insertMessageReceiptEvent(MESSAGE_RECEIPT_EVENT_2);
		assertMessageReceiptEventCountInDatabase(2);
	}

//	public void testTheRestOfApiValidationErrorEvents() {
//
//		// Simple test that inserts two Events into the table
//		final Uri uri1 = insertApiValidationErrorEvent(API_VALIDATION_ERROR_EVENT_1);
//		final Uri uri2 = insertApiValidationErrorEvent(API_VALIDATION_ERROR_EVENT_2);
//		assertFalse(uri1.equals(uri2));
//		assertApiValidationErrorEventCountInDatabase(2);
//
//		// Read both back and check that they match
//		final ApiValidationErrorEvent apiValidationError1 = getApiValidationErrorEvent(uri1);
//		final ApiValidationErrorEvent apiValidationError2 = getApiValidationErrorEvent(uri2);
//		assertEquals(apiValidationError1, API_VALIDATION_ERROR_EVENT_1);
//		assertEquals(apiValidationError2, API_VALIDATION_ERROR_EVENT_2);
//
//		// Delete one of them
//		MonitoringDatabaseWrapper.delete(uri1, null, null);
//		assertApiValidationErrorEventCountInDatabase(1);
//
//		// Try to read it back again
//		final ApiValidationErrorEvent apiValidationError1x = getApiValidationErrorEvent(uri1);
//		assertNull(apiValidationError1x);
//
//		// Update the remaining one
//		apiValidationError2.setStatus(Event.Status.POSTED);
//		MonitoringDatabaseWrapper.update(uri2, apiValidationError2.getContentValues(), null, null);
//
//		// Read it back again
//		final ApiValidationErrorEvent apiValidationError2x = getApiValidationErrorEvent(uri2);
//		assertNotNull(apiValidationError2x);
//		assertEquals(apiValidationError2x, apiValidationError2);
//	}

	public void testInsertAndQueryOneMessageReceiptEvent() {

		// Insert an Event into the table
		final Uri uri = insertMessageReceiptEvent(MESSAGE_RECEIPT_EVENT_1);

		// Read it back and check that it matches
		final MessageReceiptEvent event = getMessageReceiptEvent(uri);
		assertEquals(MESSAGE_RECEIPT_EVENT_1, event);

		// Read it back using a different method and check that it matches
		final List<MessageReceiptEvent> events = getAllMessageReceiptEvents();
		assertEquals(1, events.size());
		assertEquals(MESSAGE_RECEIPT_EVENT_1, events.iterator().next());
	}

	public void testInsertAndQueryTwoMessageReceiptEvents() {

		// Insert two Events into the table
		final Uri uri1 = insertMessageReceiptEvent(MESSAGE_RECEIPT_EVENT_1);
		final Uri uri2 = insertMessageReceiptEvent(MESSAGE_RECEIPT_EVENT_2);

		// Read them back individually and check they are correct
		final MessageReceiptEvent event1 = getMessageReceiptEvent(uri1);
		final MessageReceiptEvent event2 = getMessageReceiptEvent(uri2);
		assertEquals(MESSAGE_RECEIPT_EVENT_1, event1);
		assertEquals(MESSAGE_RECEIPT_EVENT_2, event2);

		// Assert that the crash records table contents match if we
		// query them all at once
		final List<MessageReceiptEvent> unhandledExceptionEvents = getAllMessageReceiptEvents();
		assertEquals(2, unhandledExceptionEvents.size());
		final List<MessageReceiptEvent> comparisonSet = new LinkedList<MessageReceiptEvent>();
		comparisonSet.add(MESSAGE_RECEIPT_EVENT_1);
		comparisonSet.add(MESSAGE_RECEIPT_EVENT_2);
		assertEquals(unhandledExceptionEvents, comparisonSet);
	}

	public void testQueryNonExistentMessageReceiptEvent() {

		// Insert an Event into the table
		final Uri existingUri = insertMessageReceiptEvent(MESSAGE_RECEIPT_EVENT_1);

		// Make up a URI that shouldn't exist
		final Uri nonExistingUri = Uri.withAppendedPath(DatabaseConstants.MESSAGE_RECEIPTS_CONTENT_URI, "9999");
		assertFalse(existingUri.equals(nonExistingUri));

		// Try to read the non-existent URI from the table
		final MessageReceiptEvent unhandledExceptionEvent = getMessageReceiptEvent(nonExistingUri);
		assertNull(unhandledExceptionEvent);
	}

	public void testUpdateOneMessageReceiptEvent() {

		// Insert an Event into the table
		final Uri uri = insertMessageReceiptEvent(MESSAGE_RECEIPT_EVENT_1);

		// Read it back and change its status
		final MessageReceiptEvent event = getMessageReceiptEvent(uri);
		assertEquals(MESSAGE_RECEIPT_EVENT_1, event);
		event.setStatus(BaseEvent.Status.POSTED);
		assertFalse(MESSAGE_RECEIPT_EVENT_1.getStatus() == event.getStatus());

		// Update the Event in the table
		final int rowsAffected = EventsDatabaseWrapper.update(uri, event.getContentValues(), null, null);
		assertEquals(1, rowsAffected);

		// Read the Event back and verify that it was updated
		final MessageReceiptEvent updatedEvent = getMessageReceiptEvent(uri);
		assertEquals(event, updatedEvent);
	}

	public void testUpdateAllMessageReceiptEvents() {

		// Insert three Events into the table and ensure they all have different URIs
		final Uri uri1 = insertMessageReceiptEvent(MESSAGE_RECEIPT_EVENT_1);
		final Uri uri2 = insertMessageReceiptEvent(MESSAGE_RECEIPT_EVENT_1);
		final Uri uri3 = insertMessageReceiptEvent(MESSAGE_RECEIPT_EVENT_1);
		assertFalse(uri1.equals(uri2));
		assertFalse(uri2.equals(uri3));
		assertFalse(uri1.equals(uri3));
		assertFalse(MESSAGE_RECEIPT_EVENT_1.equals(MESSAGE_RECEIPT_EVENT_2));

		// Update them all at once
		final int rowsAffected = EventsDatabaseWrapper.update(DatabaseConstants.MESSAGE_RECEIPTS_CONTENT_URI, MESSAGE_RECEIPT_EVENT_2.getContentValues(), null, null);
		assertEquals(3, rowsAffected);

		// Read them all back and verify they were updated
		final MessageReceiptEvent updatedEvent1 = getMessageReceiptEvent(uri1);
		assertEquals(updatedEvent1, MESSAGE_RECEIPT_EVENT_2);
		final MessageReceiptEvent updatedEvent2 = getMessageReceiptEvent(uri2);
		assertEquals(updatedEvent2, MESSAGE_RECEIPT_EVENT_2);
		final MessageReceiptEvent updatedEvent3 = getMessageReceiptEvent(uri3);
		assertEquals(updatedEvent3, MESSAGE_RECEIPT_EVENT_2);
	}

	public void testDeleteAll() {

		// Insert a bunch of Events into the table
		insertMessageReceiptEvent(MESSAGE_RECEIPT_EVENT_1);
		insertMessageReceiptEvent(MESSAGE_RECEIPT_EVENT_1);
		insertMessageReceiptEvent(MESSAGE_RECEIPT_EVENT_1);
		assertMessageReceiptEventCountInDatabase(3);

		// Delete all of them
		final int rowsDeleted = EventsDatabaseWrapper.delete(DatabaseConstants.MESSAGE_RECEIPTS_CONTENT_URI, null, null);
		assertEquals(3, rowsDeleted);
		assertNoMessageReceiptsEventsInDatabase();
	}

	public void testUpdateOneOfTwoMessageReceiptEvents() {

		// Insert two Events into the table
		final Uri uri1 = insertMessageReceiptEvent(MESSAGE_RECEIPT_EVENT_1);
		final Uri uri2 = insertMessageReceiptEvent(MESSAGE_RECEIPT_EVENT_2);
		assertFalse(uri1.equals(uri2));

		// Read the first one back and change its status
		final MessageReceiptEvent event1 = getMessageReceiptEvent(uri1);
		assertEquals(MESSAGE_RECEIPT_EVENT_1, event1);
		event1.setStatus(BaseEvent.Status.POSTED);
		assertFalse(MESSAGE_RECEIPT_EVENT_1.getStatus() == event1.getStatus());
		final int rowsAffected1 = EventsDatabaseWrapper.update(uri1, event1.getContentValues(), null, null);
		assertEquals(1, rowsAffected1);

		// Assert that the update record was modified correctly after reading back
		final MessageReceiptEvent updatedEvent1 = getMessageReceiptEvent(uri1);
		assertEquals(event1, updatedEvent1);

		// Assert that the other record in the table wasn't changed
		final MessageReceiptEvent event2 = getMessageReceiptEvent(uri2);
		assertEquals(MESSAGE_RECEIPT_EVENT_2, event2);
		assertFalse(event2.equals(updatedEvent1));
		assertFalse(MESSAGE_RECEIPT_EVENT_1.equals(updatedEvent1));
	}

	public void testDeleteTwoMessageReceiptEvents() {

		// Insert two Events into the table
		final Uri uri1 = insertMessageReceiptEvent(MESSAGE_RECEIPT_EVENT_1);
		final Uri uri2 = insertMessageReceiptEvent(MESSAGE_RECEIPT_EVENT_2);
		assertFalse(uri1.equals(uri2));
		assertMessageReceiptEventCountInDatabase(2);

		// Delete the first one
		final int rowsAffected1 = EventsDatabaseWrapper.delete(uri1, null, null);
		assertMessageReceiptEventCountInDatabase(1);
		assertEquals(1, rowsAffected1);

		// Delete the other one
		final int rowsAffected2 = EventsDatabaseWrapper.delete(uri2, null, null);
		assertEquals(1, rowsAffected2);
		assertMessageReceiptEventCountInDatabase(0);
	}

	private List<MessageReceiptEvent> getAllMessageReceiptEvents() {
		Cursor c = null;
		try {
			List<MessageReceiptEvent> events = new LinkedList<MessageReceiptEvent>();
			c = EventsDatabaseWrapper.query(DatabaseConstants.MESSAGE_RECEIPTS_CONTENT_URI, null, null, null, null);
			assertNotNull(c);
			for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
				final MessageReceiptEvent unhandledExceptionEvent = new MessageReceiptEvent(c);
				events.add(unhandledExceptionEvent);
			}
			return events;
		} finally {
			if (c != null) {
				c.close();
				c = null;
			}
		}
	}

	private MessageReceiptEvent getMessageReceiptEvent(final Uri uri) {
		Cursor c = null;
		try {
			c = EventsDatabaseWrapper.query(uri, null, null, null, null);
			assertNotNull(c);
			c.moveToFirst();
			if (c.isAfterLast())
				return null;
			final MessageReceiptEvent unhandledExceptionEvent = new MessageReceiptEvent(c);
			return unhandledExceptionEvent;
		} finally {
			if (c != null) {
				c.close();
				c = null;
			}
		}
	}

//	private ApiValidationErrorEvent getApiValidationErrorEvent(final Uri uri) {
//		Cursor c = null;
//		try {
//			c = MonitoringDatabaseWrapper.query(uri, null, null, null, null);
//			assertNotNull(c);
//			c.moveToFirst();
//			if (c.isAfterLast())
//				return null;
//			final ApiValidationErrorEvent apiValidationErrorEvent = new ApiValidationErrorEvent(c);
//			return apiValidationErrorEvent;
//		} finally {
//			if (c != null) {
//				c.close();
//				c = null;
//			}
//		}
//	}

	private Uri insertMessageReceiptEvent(MessageReceiptEvent event) {
		final Uri uri = EventsDatabaseWrapper.insert(DatabaseConstants.MESSAGE_RECEIPTS_CONTENT_URI, event.getContentValues());
		assertNotNull(uri);
		return uri;
	}

//	private Uri insertApiValidationErrorEvent(ApiValidationErrorEvent apiValidationErrorEvent) {
//		final Uri uri = MonitoringDatabaseWrapper.insert(DatabaseConstants.API_VALIDATION_ERROR_CONTENT_URI, apiValidationErrorEvent.getContentValues());
//		assertNotNull(uri);
//		return uri;
//	}

//	private void addApiValidationErrorEventsToDatabase(int n) {
//		for (int i = 0; i < n; i += 1) {
//			insertApiValidationErrorEvent(API_VALIDATION_ERROR_EVENT_1);
//		}
//	}

	private void addMessageReceiptEventsToDatabase(int n) {
		for (int i = 0; i < n; i += 1) {
			insertMessageReceiptEvent(MESSAGE_RECEIPT_EVENT_1);
		}
	}

//	private void assertNoApiValidationErrorEventsInDatabase() {
//		assertApiValidationErrorEventCountInDatabase(0);
//	}

//	private void assertApiValidationErrorEventCountInDatabase(int rowCount) {
//		assertEquals(rowCount, MonitoringDatabaseWrapper.getNumberOfRowsInTable(DatabaseConstants.API_VALIDATION_ERROR_TABLE_NAME));
//	}

	private void assertNoMessageReceiptsEventsInDatabase() {
		assertMessageReceiptEventCountInDatabase(0);
	}

	private void assertMessageReceiptEventCountInDatabase(int rowCount) {
		assertEquals(rowCount, EventsDatabaseWrapper.getNumberOfRowsInTable(DatabaseConstants.MESSAGE_RECEIPTS_TABLE_NAME));
	}

//	public void testCleanup1() {
//		assertApiValidationErrorEventCountInDatabase(0);
//		addApiValidationErrorEventsToDatabase(8);
//		assertApiValidationErrorEventCountInDatabase(8);
//		MonitoringDatabaseWrapper.cleanup(DatabaseConstants.API_VALIDATION_ERROR_TABLE_NAME);
//		assertApiValidationErrorEventCountInDatabase(4);
//		MonitoringDatabaseWrapper.cleanup(DatabaseConstants.API_VALIDATION_ERROR_TABLE_NAME);
//		assertApiValidationErrorEventCountInDatabase(2);
//		MonitoringDatabaseWrapper.cleanup(DatabaseConstants.API_VALIDATION_ERROR_TABLE_NAME);
//		assertApiValidationErrorEventCountInDatabase(1);
//	}
//
//	public void testCleanupEmpty() {
//		assertApiValidationErrorEventCountInDatabase(0);
//		MonitoringDatabaseWrapper.cleanup(DatabaseConstants.API_VALIDATION_ERROR_TABLE_NAME);
//		assertApiValidationErrorEventCountInDatabase(0);
//	}
//
//	public void testGetLargestTableName1() {
//		assertApiValidationErrorEventCountInDatabase(0);
//		assertJournalItemCountInDatabase(0);
//		assertMessageReceiptEventCountInDatabase(0);
//
//		addApiValidationErrorEventsToDatabase(8);
//		addMessageReceiptEventsToDatabase(3);
//		addJournalItemsToDatabase(15);
//
//		assertApiValidationErrorEventCountInDatabase(8);
//		assertMessageReceiptEventCountInDatabase(3);
//		assertJournalItemCountInDatabase(15);
//
//		assertEquals(DatabaseConstants.JOURNAL_TABLE_NAME, MonitoringDatabaseWrapper.getLargestTable());
//	}
//
//	public void testGetLargestTableName2() {
//		assertApiValidationErrorEventCountInDatabase(0);
//		assertJournalItemCountInDatabase(0);
//		assertMessageReceiptEventCountInDatabase(0);
//		addApiValidationErrorEventsToDatabase(20);
//		addMessageReceiptEventsToDatabase(2);
//		addJournalItemsToDatabase(15);
//
//		assertApiValidationErrorEventCountInDatabase(20);
//		assertMessageReceiptEventCountInDatabase(2);
//		assertJournalItemCountInDatabase(15);
//
//		assertEquals(DatabaseConstants.API_VALIDATION_ERROR_TABLE_NAME, MonitoringDatabaseWrapper.getLargestTable());
//	}
//
//	public void testGetLargestTableName3() {
//		assertApiValidationErrorEventCountInDatabase(0);
//		assertJournalItemCountInDatabase(0);
//		assertMessageReceiptEventCountInDatabase(0);
//
//		addApiValidationErrorEventsToDatabase(4);
//		addMessageReceiptEventsToDatabase(17);
//		addJournalItemsToDatabase(0);
//
//		assertApiValidationErrorEventCountInDatabase(4);
//		assertMessageReceiptEventCountInDatabase(17);
//		assertJournalItemCountInDatabase(0);
//
//		assertEquals(DatabaseConstants.UNHANDLED_EXCEPTION_TABLE_NAME, MonitoringDatabaseWrapper.getLargestTable());
//	}

	public void testGetNumberOfRowsInTableForBadTablenames() {
		assertEquals(-1, EventsDatabaseWrapper.getNumberOfRowsInTable(null));
		assertEquals(-1, EventsDatabaseWrapper.getNumberOfRowsInTable("COBRA COMMANDER WUZ HERE"));
	}

	public void testBadInsert() {
		ContentValues values = MESSAGE_RECEIPT_EVENT_1.getContentValues();
		values.put(BaseColumns._ID, "THIS AIN'T NO ID");
		assertNull(EventsDatabaseWrapper.insert(DatabaseConstants.MESSAGE_RECEIPTS_CONTENT_URI, values));
	}

	public void testBadUpdate() {
		ContentValues values = MESSAGE_RECEIPT_EVENT_1.getContentValues();
		final Uri uri = EventsDatabaseWrapper.insert(DatabaseConstants.MESSAGE_RECEIPTS_CONTENT_URI, values);
		assertNotNull(uri);
		values.put(BaseColumns._ID, "THIS AIN'T NO ID");
		assertEquals(-1, EventsDatabaseWrapper.update(uri, values, null, null));
	}
}
