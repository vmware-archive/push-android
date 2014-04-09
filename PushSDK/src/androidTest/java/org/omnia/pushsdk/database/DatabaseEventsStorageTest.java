package org.omnia.pushsdk.database;

import android.net.Uri;
import android.test.AndroidTestCase;

import org.omnia.pushsdk.model.EventBase;
import org.omnia.pushsdk.model.MessageReceiptEvent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DatabaseEventsStorageTest extends AndroidTestCase {

    private static final String TEST_VARIANT_UUID_1 = "TEST-VARIANT-UUID-1";
    private static final String TEST_MESSAGE_UUID_1 = "TEST-MESSAGE-UUID-1";
    private static final String TEST_VARIANT_UUID_2 = "TEST-VARIANT-UUID-2";
    private static final String TEST_MESSAGE_UUID_2 = "TEST-MESSAGE-UUID-2";
	private static final Uri NON_EXISTENT_FILE_1 = Uri.withAppendedPath(DatabaseConstants.MESSAGE_RECEIPTS_CONTENT_URI, "/999999");
//	private static final Uri NON_EXISTENT_FILE_2 = Uri.withAppendedPath(DatabaseConstants.API_VALIDATION_ERROR_CONTENT_URI, "/999999");
	private DatabaseEventsStorage databaseEventsStorage;
	private MessageReceiptEvent EVENT_1;
	private MessageReceiptEvent EVENT_2;
//	private ApiValidationErrorEvent EVENT_3;
//	private ApiValidationErrorEvent EVENT_4;
	private static final String TEST_DATABASE_PREFIX = "test_";
    private static final Map<String, String> REQUEST_HEADERS;

    static {
        REQUEST_HEADERS = new HashMap<String, String>();
        REQUEST_HEADERS.put("TEST_KEY1", "TEST_VALUE1");
        REQUEST_HEADERS.put("TEST_KEY2", "TEST_VALUE2");
    }

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		EventsDatabaseHelper.init();
		EVENT_1 = MessageReceiptEvent.getMessageReceiptEvent(TEST_VARIANT_UUID_1, TEST_MESSAGE_UUID_1);
		EVENT_2 = MessageReceiptEvent.getMessageReceiptEvent(TEST_VARIANT_UUID_2, TEST_MESSAGE_UUID_2);
//		EVENT_3 = ApiValidationErrorEvent.getApiValidationErrorEvent("URL", "POST", REQUEST_HEADERS, 44, "content/silly", "Ministry of Silly Walks", "ERROR_MESSAGE OF DOOM", metadata);
//		EVENT_4 = ApiValidationErrorEvent.getApiValidationErrorEvent("valid URL", "POST", REQUEST_HEADERS, 44, "content/silly", "Ministry of Silly Walks", "DOUBLE ERROR", metadata);
		databaseEventsStorage = new DatabaseEventsStorage();
		databaseEventsStorage.reset(EventsStorage.EventType.ALL);
		// databaseEventsStorage.reset(EventsStorage.EventType.MESSAGE_RECEIPT);
		// databaseEventsStorage.reset(EventsStorage.EventType.API_VALIDATION_ERROR);
	}

	public void testStartState() {
		assertNotNull(databaseEventsStorage);
		final List<Uri> files1 = databaseEventsStorage.getEventUris(EventsStorage.EventType.MESSAGE_RECEIPT);
		assertTrue(files1 == null || files1.size() == 0);
//		final List<Uri> files2 = databaseEventsStorage.getEventUris(EventsStorage.EventType.API_VALIDATION_ERROR);
//		assertTrue(files2 == null || files2.size() == 0);
		final List<Uri> files3 = databaseEventsStorage.getEventUris(EventsStorage.EventType.ALL);
		assertTrue(files3 == null || files3.size() == 0);
		assertEquals(0, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.MESSAGE_RECEIPT));
//		assertEquals(0, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.API_VALIDATION_ERROR));
		assertEquals(0, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
		assertFalse(EVENT_1.equals(EVENT_2));
//		assertFalse(EVENT_3.equals(EVENT_4));
	}

	public void testSaveMessageReceiptOnce() {
		final Uri saveResult = databaseEventsStorage.saveEvent(EVENT_1, EventsStorage.EventType.MESSAGE_RECEIPT);
		assertNotNull(saveResult);
		assertEquals(1, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.MESSAGE_RECEIPT));
//		assertEquals(0, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.API_VALIDATION_ERROR));
		assertEquals(1, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
	}

//	public void testSaveApiValidationErrorOnce() {
//		final Uri saveResult = databaseEventsStorage.saveEvent(EVENT_3, EventsStorage.EventType.API_VALIDATION_ERROR);
//		assertNotNull(saveResult);
//		assertEquals(1, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.API_VALIDATION_ERROR));
//		assertEquals(0, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.MESSAGE_RECEIPT));
//		assertEquals(1, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
//	}

	public void testSaveMessageReceiptTwice() {
		databaseEventsStorage.saveEvent(EVENT_1, EventsStorage.EventType.MESSAGE_RECEIPT);
		assertEquals(1, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.MESSAGE_RECEIPT));
		assertEquals(1, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
		final Uri saveResult = databaseEventsStorage.saveEvent(EVENT_2, EventsStorage.EventType.MESSAGE_RECEIPT);
		assertNotNull(saveResult);
		assertEquals(2, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.MESSAGE_RECEIPT));
		assertEquals(2, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
	}

//	public void testSaveApiValidationErrorTwice() {
//		databaseEventsStorage.saveEvent(EVENT_3, EventsStorage.EventType.API_VALIDATION_ERROR);
//		assertEquals(1, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.API_VALIDATION_ERROR));
//		assertEquals(1, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
//		final Uri saveResult = databaseEventsStorage.saveEvent(EVENT_4, EventsStorage.EventType.API_VALIDATION_ERROR);
//		assertNotNull(saveResult);
//		assertEquals(2, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.API_VALIDATION_ERROR));
//		assertEquals(2, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
//	}

	public void testSaveMessageReceiptAndRead() {
		databaseEventsStorage.saveEvent(EVENT_1, EventsStorage.EventType.MESSAGE_RECEIPT);
		final List<Uri> files1 = databaseEventsStorage.getEventUris(EventsStorage.EventType.MESSAGE_RECEIPT);
		assertEquals(1, files1.size());
		final List<Uri> files2 = databaseEventsStorage.getEventUris(EventsStorage.EventType.ALL);
		assertEquals(1, files2.size());
		final MessageReceiptEvent fileContents = (MessageReceiptEvent) databaseEventsStorage.readEvent(files1.get(0));
		assertEquals(EVENT_1, fileContents);
	}

//	public void testSaveApiValidationErrorAndRead() {
//		databaseEventsStorage.saveEvent(EVENT_3, EventsStorage.EventType.API_VALIDATION_ERROR);
//		final List<Uri> files1 = databaseEventsStorage.getEventUris(EventsStorage.EventType.API_VALIDATION_ERROR);
//		assertEquals(1, files1.size());
//		final List<Uri> files2 = databaseEventsStorage.getEventUris(EventsStorage.EventType.ALL);
//		assertEquals(1, files2.size());
//		final ApiValidationErrorEvent fileContents = (ApiValidationErrorEvent) databaseEventsStorage.readEvent(files2.get(0));
//		assertEquals(EVENT_3, fileContents);
//	}

	public void testReadNonExistentFile() {
		boolean exceptionThrown = false;
		try {
			final MessageReceiptEvent fileContents1 = (MessageReceiptEvent) databaseEventsStorage.readEvent(NON_EXISTENT_FILE_1);
			assertEquals(null, fileContents1);
		} catch (Exception e) {
			exceptionThrown = true;
		}
		assertTrue(exceptionThrown);

//		exceptionThrown = false;
//		try {
//			final ApiValidationErrorEvent fileContents2 = (ApiValidationErrorEvent) databaseEventsStorage.readEvent(NON_EXISTENT_FILE_2);
//			assertEquals(null, fileContents2);
//		} catch (Exception e) {
//			exceptionThrown = true;
//		}
//		assertTrue(exceptionThrown);
	}

	public void testSetStatusNonExistentFile() {
		boolean exceptionThrown = false;
		try {
			databaseEventsStorage.setEventStatus(NON_EXISTENT_FILE_1, EventBase.Status.POSTED);
		} catch (IllegalArgumentException e) {
			exceptionThrown = true;
		}
		assertTrue(exceptionThrown);

//		exceptionThrown = false;
//		try {
//			databaseEventsStorage.setEventStatus(NON_EXISTENT_FILE_2, EventBase.Status.POSTED);
//		} catch (IllegalArgumentException e) {
//			exceptionThrown = true;
//		}
//		assertTrue(exceptionThrown);
	}

	public void testSaveMessageReceiptAndDelete() {
		databaseEventsStorage.saveEvent(EVENT_1, EventsStorage.EventType.MESSAGE_RECEIPT);
		final List<Uri> files = databaseEventsStorage.getEventUris(EventsStorage.EventType.MESSAGE_RECEIPT);
		assertEquals(1, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.MESSAGE_RECEIPT));
		assertEquals(1, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
		databaseEventsStorage.deleteEvents(files, EventsStorage.EventType.MESSAGE_RECEIPT);
		assertEquals(0, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.MESSAGE_RECEIPT));
		assertEquals(0, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
	}

//	public void testSaveApiValidationErrorAndDelete() {
//		databaseEventsStorage.saveEvent(EVENT_3, EventsStorage.EventType.API_VALIDATION_ERROR);
//		final List<Uri> files = databaseEventsStorage.getEventUris(EventsStorage.EventType.API_VALIDATION_ERROR);
//		assertEquals(1, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.API_VALIDATION_ERROR));
//		assertEquals(1, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
//		databaseEventsStorage.deleteEvents(files, EventsStorage.EventType.API_VALIDATION_ERROR);
//		assertEquals(0, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.API_VALIDATION_ERROR));
//		assertEquals(0, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
//	}

	public void testDeleteNonExistingFile() {
		databaseEventsStorage.saveEvent(EVENT_1, EventsStorage.EventType.MESSAGE_RECEIPT);
		assertEquals(1, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.MESSAGE_RECEIPT));
		assertEquals(1, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
		final List<Uri> bogusList = new LinkedList<Uri>();
		bogusList.add(NON_EXISTENT_FILE_1);
		databaseEventsStorage.deleteEvents(bogusList, EventsStorage.EventType.MESSAGE_RECEIPT);
		assertEquals(1, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.MESSAGE_RECEIPT));
		assertEquals(1, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));

//		databaseEventsStorage.saveEvent(EVENT_3, EventsStorage.EventType.API_VALIDATION_ERROR);
//		assertEquals(1, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.API_VALIDATION_ERROR));
//		assertEquals(2, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
//		final List<Uri> blahList = new LinkedList<Uri>();
//		blahList.add(NON_EXISTENT_FILE_2);
//		databaseEventsStorage.deleteEvents(blahList, EventsStorage.EventType.API_VALIDATION_ERROR);
//		assertEquals(1, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.API_VALIDATION_ERROR));
//		assertEquals(2, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
	}

	public void testDeleteOneOfTwoMessageReceiptFiles() {

		databaseEventsStorage.saveEvent(EVENT_1, EventsStorage.EventType.MESSAGE_RECEIPT);
		assertEquals(1, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.MESSAGE_RECEIPT));

		databaseEventsStorage.saveEvent(EVENT_2, EventsStorage.EventType.MESSAGE_RECEIPT);
		assertEquals(2, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.MESSAGE_RECEIPT));
		assertEquals(2, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));

		final List<Uri> files1 = databaseEventsStorage.getEventUris(EventsStorage.EventType.MESSAGE_RECEIPT);
		final List<Uri> filesRemaining = new LinkedList<Uri>();
		filesRemaining.add(files1.get(1));
		files1.remove(1);

		databaseEventsStorage.deleteEvents(files1, EventsStorage.EventType.MESSAGE_RECEIPT);
		assertEquals(1, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.MESSAGE_RECEIPT));
		assertEquals(1, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));

		final List<Uri> files2 = databaseEventsStorage.getEventUris(EventsStorage.EventType.MESSAGE_RECEIPT);
		assertEquals(files1.size(), files2.size());
		assertEquals(1, files2.size());
		assertEquals(filesRemaining.get(0), files2.get(0));
	}

//	public void testDeleteOneOfTwoApiValidationErrorFiles() {
//
//		databaseEventsStorage.saveEvent(EVENT_3, EventsStorage.EventType.API_VALIDATION_ERROR);
//		assertEquals(1, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.API_VALIDATION_ERROR));
//
//		databaseEventsStorage.saveEvent(EVENT_4, EventsStorage.EventType.API_VALIDATION_ERROR);
//		assertEquals(2, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.API_VALIDATION_ERROR));
//		assertEquals(2, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
//
//		final List<Uri> files1 = databaseEventsStorage.getEventUris(EventsStorage.EventType.API_VALIDATION_ERROR);
//		assertEquals(2, files1.size());
//		final List<Uri> filesRemaining = new LinkedList<Uri>();
//		filesRemaining.add(files1.get(1));
//		files1.remove(1);
//		assertEquals(1, files1.size());
//
//		databaseEventsStorage.deleteEvents(files1, EventsStorage.EventType.API_VALIDATION_ERROR);
//		assertEquals(1, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.API_VALIDATION_ERROR));
//		assertEquals(1, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
//
//		final List<Uri> files2 = databaseEventsStorage.getEventUris(EventsStorage.EventType.API_VALIDATION_ERROR);
//		assertEquals(1, files2.size());
//		assertEquals(filesRemaining.get(0), files2.get(0));
//	}

	public void testReset1() {

		databaseEventsStorage.saveEvent(EVENT_1, EventsStorage.EventType.MESSAGE_RECEIPT);
//		databaseEventsStorage.saveEvent(EVENT_3, EventsStorage.EventType.API_VALIDATION_ERROR);
		assertEquals(1, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.MESSAGE_RECEIPT));
//		assertEquals(1, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.API_VALIDATION_ERROR));
		assertEquals(1, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));

		databaseEventsStorage.reset(EventsStorage.EventType.MESSAGE_RECEIPT);
//		databaseEventsStorage.reset(EventsStorage.EventType.API_VALIDATION_ERROR);
		assertEquals(0, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.MESSAGE_RECEIPT));
//		assertEquals(0, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.API_VALIDATION_ERROR));
		assertEquals(0, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
	}

	public void testReset2() {

		databaseEventsStorage.saveEvent(EVENT_1, EventsStorage.EventType.MESSAGE_RECEIPT);
//		databaseEventsStorage.saveEvent(EVENT_3, EventsStorage.EventType.API_VALIDATION_ERROR);
		assertEquals(1, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.MESSAGE_RECEIPT));
//		assertEquals(1, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.API_VALIDATION_ERROR));
		assertEquals(1, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));

		databaseEventsStorage.reset(EventsStorage.EventType.ALL);
		assertEquals(0, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.MESSAGE_RECEIPT));
//		assertEquals(0, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.API_VALIDATION_ERROR));
		assertEquals(0, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
	}

	public void testSetStatus() {

		final Uri uri1 = databaseEventsStorage.saveEvent(EVENT_1, EventsStorage.EventType.MESSAGE_RECEIPT);
		assertEquals(EventBase.Status.NOT_POSTED, EVENT_1.getStatus());

		databaseEventsStorage.setEventStatus(uri1, EventBase.Status.POSTING);
		assertEquals(EventBase.Status.POSTING, databaseEventsStorage.readEvent(uri1).getStatus());

		databaseEventsStorage.setEventStatus(uri1, EventBase.Status.POSTED);
		assertEquals(EventBase.Status.POSTED, databaseEventsStorage.readEvent(uri1).getStatus());

//		final Uri uri2 = databaseEventsStorage.saveEvent(EVENT_3, EventsStorage.EventType.API_VALIDATION_ERROR);
//		assertEquals(EventBase.Status.NOT_POSTED, EVENT_3.getStatus());
//
//		databaseEventsStorage.setEventStatus(uri2, EventBase.Status.POSTING);
//		assertEquals(EventBase.Status.POSTING, databaseEventsStorage.readEvent(uri2).getStatus());
//
//		databaseEventsStorage.setEventStatus(uri2, EventBase.Status.POSTED);
//		assertEquals(EventBase.Status.POSTED, databaseEventsStorage.readEvent(uri2).getStatus());
	}

	public void testGetMessageReceiptEventUrisWithStatus() {
		EVENT_1.setStatus(EventBase.Status.POSTED);
		EVENT_2.setStatus(EventBase.Status.POSTING_ERROR);
		databaseEventsStorage.saveEvent(EVENT_1, EventsStorage.EventType.MESSAGE_RECEIPT);
		databaseEventsStorage.saveEvent(EVENT_2, EventsStorage.EventType.MESSAGE_RECEIPT);

		final List<Uri> uris1 = databaseEventsStorage.getEventUrisWithStatus(EventsStorage.EventType.MESSAGE_RECEIPT, EventBase.Status.NOT_POSTED);
		assertEquals(0, uris1.size());

		final List<Uri> uris2 = databaseEventsStorage.getEventUrisWithStatus(EventsStorage.EventType.MESSAGE_RECEIPT, EventBase.Status.POSTING);
		assertEquals(0, uris2.size());

		final List<Uri> uris3 = databaseEventsStorage.getEventUrisWithStatus(EventsStorage.EventType.MESSAGE_RECEIPT, EventBase.Status.POSTING_ERROR);
		assertEquals(1, uris3.size());
	}

//	public void testGetApiValidationErrorEventUrisWithStatus() {
//		EVENT_3.setStatus(EventBase.Status.POSTING);
//		EVENT_4.setStatus(EventBase.Status.NOT_POSTED);
//		databaseEventsStorage.saveEvent(EVENT_3, EventsStorage.EventType.API_VALIDATION_ERROR);
//		databaseEventsStorage.saveEvent(EVENT_4, EventsStorage.EventType.API_VALIDATION_ERROR);
//
//		final List<Uri> uris1 = databaseEventsStorage.getEventUrisWithStatus(EventsStorage.EventType.API_VALIDATION_ERROR, EventBase.Status.NOT_POSTED);
//		assertEquals(1, uris1.size());
//
//		final List<Uri> uris2 = databaseEventsStorage.getEventUrisWithStatus(EventsStorage.EventType.API_VALIDATION_ERROR, EventBase.Status.POSTING);
//		assertEquals(1, uris2.size());
//
//		final List<Uri> uris3 = databaseEventsStorage.getEventUrisWithStatus(EventsStorage.EventType.API_VALIDATION_ERROR, EventBase.Status.POSTING_ERROR);
//		assertEquals(0, uris3.size());
//	}

	public void testGetMixedEventUrisWithStatus() {
		EVENT_1.setStatus(EventBase.Status.NOT_POSTED);
		EVENT_2.setStatus(EventBase.Status.POSTING);
//		EVENT_3.setStatus(EventBase.Status.NOT_POSTED);
//		EVENT_4.setStatus(EventBase.Status.POSTED);
		databaseEventsStorage.saveEvent(EVENT_1, EventsStorage.EventType.MESSAGE_RECEIPT);
		databaseEventsStorage.saveEvent(EVENT_2, EventsStorage.EventType.MESSAGE_RECEIPT);
//		databaseEventsStorage.saveEvent(EVENT_3, EventsStorage.EventType.API_VALIDATION_ERROR);
//		databaseEventsStorage.saveEvent(EVENT_4, EventsStorage.EventType.API_VALIDATION_ERROR);

		final List<Uri> uris1 = databaseEventsStorage.getEventUrisWithStatus(EventsStorage.EventType.ALL, EventBase.Status.NOT_POSTED);
		assertEquals(1, uris1.size());

		final List<Uri> uris2 = databaseEventsStorage.getEventUrisWithStatus(EventsStorage.EventType.ALL, EventBase.Status.POSTING);
		assertEquals(1, uris2.size());

		final List<Uri> uris3 = databaseEventsStorage.getEventUrisWithStatus(EventsStorage.EventType.ALL, EventBase.Status.POSTED);
		assertEquals(0, uris3.size());

		final List<Uri> uris4 = databaseEventsStorage.getEventUrisWithStatus(EventsStorage.EventType.ALL, EventBase.Status.POSTING_ERROR);
		assertEquals(0, uris4.size());
	}

    public void testNotAllowedToSaveWithTypeAll() {
        try {
            databaseEventsStorage.saveEvent(EVENT_1, EventsStorage.EventType.ALL); // should throw
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
        assertEquals(0, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
    }

    public void testNotAllowedToDeleteWithTypeAll() {
        try {
            final Uri uri1 = databaseEventsStorage.saveEvent(EVENT_1, EventsStorage.EventType.MESSAGE_RECEIPT);
            final Uri uri2 = databaseEventsStorage.saveEvent(EVENT_2, EventsStorage.EventType.MESSAGE_RECEIPT);
            final List<Uri> list = new LinkedList<Uri>();
            list.add(uri1);
            list.add(uri2);
            assertEquals(2, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
            databaseEventsStorage.deleteEvents(list, EventsStorage.EventType.ALL); // should throw
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
        assertEquals(2, databaseEventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
    }

}
