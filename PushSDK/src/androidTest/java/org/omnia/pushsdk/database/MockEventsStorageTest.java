package org.omnia.pushsdk.database;

import android.net.Uri;
import android.test.AndroidTestCase;

import org.omnia.pushsdk.model.EventBase;
import org.omnia.pushsdk.model.MessageReceiptEvent;

import java.util.LinkedList;
import java.util.List;

public class MockEventsStorageTest extends AndroidTestCase {

	private static final Uri NON_EXISTENT_FILE_1 = Uri.parse("file://message_receipts/this_name_does_not_exist_come_on!");
    private static final String TEST_VARIANT_ID_1 = "TEST_VARIANT_ID_1";
    private static final String TEST_VARIANT_ID_2 = "TEST_VARIANT_ID_2";
    private static final String TEST_VARIANT_ID_3 = "TEST_VARIANT_ID_3";
    private static final String TEST_MESSAGE_UUID_1 = "TEST_MESSAGE_UUID_1";
    private static final String TEST_MESSAGE_UUID_2 = "TEST_MESSAGE_UUID_2";
    private static final String TEST_MESSAGE_UUID_3 = "TEST_MESSAGE_UUID_3";
    //	private static final Uri NON_EXISTENT_FILE_2 = Uri.parse("file://api_validation_error/this_name_does_not_exist_come_on!");
	private MessageReceiptEvent EVENT_1;
	private MessageReceiptEvent EVENT_2;
	private MessageReceiptEvent EVENT_3;
//	private ApiValidationErrorEvent EVENT_4;
//	private ApiValidationErrorEvent EVENT_5;
//	private ApiValidationErrorEvent EVENT_6;
	private MockEventsStorage storage;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		EVENT_1 = MessageReceiptEvent.getMessageReceiptEvent(TEST_VARIANT_ID_1, TEST_MESSAGE_UUID_1);
		EVENT_2 = MessageReceiptEvent.getMessageReceiptEvent(TEST_VARIANT_ID_2, TEST_MESSAGE_UUID_2);
		EVENT_3 = MessageReceiptEvent.getMessageReceiptEvent(TEST_VARIANT_ID_3, TEST_MESSAGE_UUID_3);
//		EVENT_4 = ApiValidationErrorEvent.getApiValidationErrorEvent(getContext(), "URL", "POST", REQUEST_HEADERS, 456, "content/sneh", "SNEEEH", "ERROR_MESSAGE OF DOOM", metadata);
//		EVENT_5 = ApiValidationErrorEvent.getApiValidationErrorEvent(getContext(), "URL of truth", "POST", REQUEST_HEADERS, 456, "content/sneh", "SNEEEH", "DUAL ERRORs", metadata);
//		EVENT_6 = ApiValidationErrorEvent.getApiValidationErrorEvent(getContext(), "valid URL", "POST", REQUEST_HEADERS, 456, "content/sneh", "SNEEEH", "TRIPLE THREAT ERRORS", metadata);
		storage = new MockEventsStorage();
	}

	public void testStartState() {

		// Tests the initial state of the fake filesystem
		assertEquals(0, storage.getNumberOfEvents(getContext(), EventsStorage.EventType.MESSAGE_RECEIPTS));
//		assertEquals(0, storage.getNumberOfEvents(getContext(), EventType.API_VALIDATION_ERROR));
		assertEquals(0, storage.getNumberOfEvents(getContext(), EventsStorage.EventType.ALL));
	}

	public void testSaveUnhandledExceptionEventOnce() {

		// Tests saving one file into the fake filesystem
		final Uri saveResult = storage.saveEvent(getContext(), EVENT_1, EventsStorage.EventType.MESSAGE_RECEIPTS);
		assertNotNull(saveResult);
		assertEquals(1, storage.getNumberOfEvents(getContext(), EventsStorage.EventType.MESSAGE_RECEIPTS));
//		assertEquals(0, storage.getNumberOfEvents(getContext(), EventType.API_VALIDATION_ERROR));
		assertEquals(1, storage.getNumberOfEvents(getContext(), EventsStorage.EventType.ALL));
	}

//	public void testSaveApiValidationErrorEventOnce() {
//
//		// Tests saving one file into the fake filesystem
//		final Uri saveResult = storage.saveEvent(getContext(), EVENT_4, EventType.API_VALIDATION_ERROR);
//		assertNotNull(saveResult);
//		assertEquals(0, storage.getNumberOfEvents(getContext(), EventType.UNHANDLED_EXCEPTION));
//		assertEquals(1, storage.getNumberOfEvents(getContext(), EventType.API_VALIDATION_ERROR));
//		assertEquals(1, storage.getNumberOfEvents(getContext(), EventType.ALL));
//	}

	public void testSaveUnhandledExceptionEventTwice() {

		// Save one file into the fake filesystem
		final Uri saveResult1 = storage.saveEvent(getContext(), EVENT_1, EventsStorage.EventType.MESSAGE_RECEIPTS);
		assertNotNull(saveResult1);
		assertEquals(1, storage.getNumberOfEvents(getContext(), EventsStorage.EventType.MESSAGE_RECEIPTS));
//		assertEquals(0, storage.getNumberOfEvents(getContext(), EventsStorage.EventType.API_VALIDATION_ERROR));
		assertEquals(1, storage.getNumberOfEvents(getContext(), EventsStorage.EventType.ALL));

		// Save another file into the fake filesystem
		final Uri saveResult2 = storage.saveEvent(getContext(), EVENT_2, EventsStorage.EventType.MESSAGE_RECEIPTS);
		assertNotNull(saveResult2);
		assertEquals(2, storage.getNumberOfEvents(getContext(), EventsStorage.EventType.MESSAGE_RECEIPTS));
//		assertEquals(0, storage.getNumberOfEvents(getContext(), EventsStorage.EventType.API_VALIDATION_ERROR));
		assertEquals(2, storage.getNumberOfEvents(getContext(), EventsStorage.EventType.ALL));
	}

//	public void testSaveApiValidationErrorEventTwice() {
//
//		// Save one file into the fake filesystem
//		final Uri saveResult1 = storage.saveEvent(getContext(), EVENT_4, EventType.API_VALIDATION_ERROR);
//		assertNotNull(saveResult1);
//		assertEquals(1, storage.getNumberOfEvents(getContext(), EventType.API_VALIDATION_ERROR));
//		assertEquals(0, storage.getNumberOfEvents(getContext(), EventType.UNHANDLED_EXCEPTION));
//		assertEquals(1, storage.getNumberOfEvents(getContext(), EventType.ALL));
//
//		// Save another file into the fake filesystem
//		final Uri saveResult2 = storage.saveEvent(getContext(), EVENT_5, EventType.API_VALIDATION_ERROR);
//		assertNotNull(saveResult2);
//		assertEquals(2, storage.getNumberOfEvents(getContext(), EventType.API_VALIDATION_ERROR));
//		assertEquals(0, storage.getNumberOfEvents(getContext(), EventType.UNHANDLED_EXCEPTION));
//		assertEquals(2, storage.getNumberOfEvents(getContext(), EventType.ALL));
//	}

	public void testSaveUnhandledExceptionAndRead() {

		// Save a file into the fake filesystem
		storage.saveEvent(getContext(), EVENT_1, EventsStorage.EventType.MESSAGE_RECEIPTS);
		final List<Uri> files1 = storage.getEventUris(getContext(), EventsStorage.EventType.MESSAGE_RECEIPTS);
		assertEquals(1, files1.size());
		final List<Uri> files2 = storage.getEventUris(getContext(), EventsStorage.EventType.ALL);
		assertEquals(1, files2.size());
		assertEquals(files1.get(0), files2.get(0));

		// Read the file back and confirm that it matches
		final MessageReceiptEvent fileContents = (MessageReceiptEvent) storage.readEvent(getContext(), files2.get(0));
		assertEquals(EVENT_1, fileContents);
	}

//	public void testSaveApiValidationErrorAndRead() {
//
//		// Save a file into the fake filesystem
//		storage.saveEvent(getContext(), EVENT_4, EventType.API_VALIDATION_ERROR);
//		final List<Uri> files1 = storage.getEventUris(getContext(), EventType.API_VALIDATION_ERROR);
//		assertEquals(1, files1.size());
//		final List<Uri> files2 = storage.getEventUris(getContext(), EventType.ALL);
//		assertEquals(1, files2.size());
//		assertEquals(files1.get(0), files2.get(0));
//
//		// Read the file back and confirm that it matches
//		final ApiValidationErrorEvent fileContents = (ApiValidationErrorEvent) storage.readEvent(getContext(), files1.get(0));
//		assertEquals(EVENT_4, fileContents);
//	}

	public void testReadNonExistentFile1() {

		// Try to read a file that doesn't exist
		boolean exceptionThrown = false;
		try {
			storage.readEvent(getContext(), NON_EXISTENT_FILE_1);
		} catch (Exception e) {
			exceptionThrown = true;
		}
		assertTrue(exceptionThrown);
	}

//	public void testReadNonExistentFile2() {
//
//		// Try to read a file that doesn't exist
//		boolean exceptionThrown = false;
//		try {
//			storage.readEvent(getContext(), NON_EXISTENT_FILE_2);
//		} catch (Exception e) {
//			exceptionThrown = true;
//		}
//		assertTrue(exceptionThrown);
//	}

	public void testSaveUnhandledExceptionAndDelete() {

		// Save a file into the fake filesystem
		storage.saveEvent(getContext(), EVENT_1, EventsStorage.EventType.MESSAGE_RECEIPTS);
		final List<Uri> files = storage.getEventUris(getContext(), EventsStorage.EventType.MESSAGE_RECEIPTS);
		assertEquals(1, storage.getNumberOfEvents(getContext(), EventsStorage.EventType.MESSAGE_RECEIPTS));
		assertEquals(1, storage.getNumberOfEvents(getContext(), EventsStorage.EventType.ALL));

		// Try to delete the file from the fake filesystem
		storage.deleteEvents(getContext(), files, EventsStorage.EventType.MESSAGE_RECEIPTS);
		assertEquals(0, storage.getNumberOfEvents(getContext(), EventsStorage.EventType.MESSAGE_RECEIPTS));
		assertEquals(0, storage.getNumberOfEvents(getContext(), EventsStorage.EventType.ALL));
	}

//	public void testSaveApiValidationErrorAndDelete() {
//
//		// Save a file into the fake filesystem
//		storage.saveEvent(getContext(), EVENT_4, EventType.API_VALIDATION_ERROR);
//		final List<Uri> files = storage.getEventUris(getContext(), EventType.API_VALIDATION_ERROR);
//		assertEquals(1, storage.getNumberOfEvents(getContext(), EventType.API_VALIDATION_ERROR));
//		assertEquals(1, storage.getNumberOfEvents(getContext(), EventType.ALL));
//
//		// Try to delete the file from the fake filesystem
//		storage.deleteEvents(getContext(), files, EventType.API_VALIDATION_ERROR);
//		assertEquals(0, storage.getNumberOfEvents(getContext(), EventType.API_VALIDATION_ERROR));
//		assertEquals(0, storage.getNumberOfEvents(getContext(), EventType.ALL));
//	}

	public void testDeleteNonExistentFile() {

		// Save a file into the fake filesystem
		storage.saveEvent(getContext(), EVENT_1, EventsStorage.EventType.MESSAGE_RECEIPTS);
		assertEquals(1, storage.getNumberOfEvents(getContext(), EventsStorage.EventType.MESSAGE_RECEIPTS));
		assertEquals(1, storage.getNumberOfEvents(getContext(), EventsStorage.EventType.ALL));
		// Try to delete a file that doesn't exist
		final List<Uri> bogusList = new LinkedList<Uri>();
		bogusList.add(NON_EXISTENT_FILE_1);
		storage.deleteEvents(getContext(), bogusList, EventsStorage.EventType.MESSAGE_RECEIPTS);
		assertEquals(1, storage.getNumberOfEvents(getContext(), EventsStorage.EventType.MESSAGE_RECEIPTS));
		assertEquals(1, storage.getNumberOfEvents(getContext(), EventsStorage.EventType.ALL));

//		storage.saveEvent(getContext(), EVENT_4, EventType.API_VALIDATION_ERROR);
//		assertEquals(1, storage.getNumberOfEvents(getContext(), EventType.API_VALIDATION_ERROR));
//		assertEquals(2, storage.getNumberOfEvents(getContext(), EventType.ALL));
//		final List<Uri> blahList = new LinkedList<Uri>();
//		blahList.add(NON_EXISTENT_FILE_2);
//		storage.deleteEvents(getContext(), blahList, EventType.API_VALIDATION_ERROR);
//		assertEquals(1, storage.getNumberOfEvents(getContext(), EventType.API_VALIDATION_ERROR));
//		assertEquals(2, storage.getNumberOfEvents(getContext(), EventType.ALL));
	}

	public void testDeleteOneOfTwoUnhandledExceptionFiles() {

		// Save a couple of files into the fake filesystem
		storage.saveEvent(getContext(), EVENT_1, EventsStorage.EventType.MESSAGE_RECEIPTS);
		assertEquals(1, storage.getNumberOfEvents(getContext(), EventsStorage.EventType.MESSAGE_RECEIPTS));
		storage.saveEvent(getContext(), EVENT_2, EventsStorage.EventType.MESSAGE_RECEIPTS);
		assertEquals(2, storage.getNumberOfEvents(getContext(), EventsStorage.EventType.MESSAGE_RECEIPTS));
		assertEquals(2, storage.getNumberOfEvents(getContext(), EventsStorage.EventType.ALL));

		// Delete one of the two files
		final List<Uri> files1 = storage.getEventUris(getContext(), EventsStorage.EventType.MESSAGE_RECEIPTS);
		final List<Uri> filesRemaining = new LinkedList<Uri>();
		filesRemaining.add(files1.get(1));
		files1.remove(1);
		storage.deleteEvents(getContext(), files1, EventsStorage.EventType.MESSAGE_RECEIPTS);
		assertEquals(1, storage.getNumberOfEvents(getContext(), EventsStorage.EventType.MESSAGE_RECEIPTS));
		assertEquals(1, storage.getNumberOfEvents(getContext(), EventsStorage.EventType.ALL));

		// Ensure the other file is still in the fake filesystem
		final List<Uri> files2 = storage.getEventUris(getContext(), EventsStorage.EventType.MESSAGE_RECEIPTS);
		assertEquals(files1.size(), files2.size());
		assertEquals(1, files2.size());
		assertEquals(filesRemaining.get(0), files2.get(0));
	}

//	public void testDeleteOneOfTwoApiValidationErrorFiles() {
//
//		// Save a couple of files into the fake filesystem
//		storage.saveEvent(getContext(), EVENT_4, EventType.API_VALIDATION_ERROR);
//		assertEquals(1, storage.getNumberOfEvents(getContext(), EventType.API_VALIDATION_ERROR));
//		storage.saveEvent(getContext(), EVENT_5, EventType.API_VALIDATION_ERROR);
//		assertEquals(2, storage.getNumberOfEvents(getContext(), EventType.API_VALIDATION_ERROR));
//		assertEquals(2, storage.getNumberOfEvents(getContext(), EventType.ALL));
//
//		// Delete one of the two files
//		final List<Uri> files1 = storage.getEventUris(getContext(), EventType.API_VALIDATION_ERROR);
//		final List<Uri> filesRemaining = new LinkedList<Uri>();
//		filesRemaining.add(files1.get(1));
//		files1.remove(1);
//		storage.deleteEvents(getContext(), files1, EventType.API_VALIDATION_ERROR);
//		assertEquals(1, storage.getNumberOfEvents(getContext(), EventType.API_VALIDATION_ERROR));
//		assertEquals(1, storage.getNumberOfEvents(getContext(), EventType.ALL));
//
//		// Ensure the other file is still in the fake filesystem
//		final List<Uri> files2 = storage.getEventUris(getContext(), EventType.API_VALIDATION_ERROR);
//		assertEquals(files1.size(), files2.size());
//		assertEquals(1, files2.size());
//		assertEquals(filesRemaining.get(0), files2.get(0));
//	}

	public void testReset1() {

		// Tests the reset mechanism
		storage.saveEvent(getContext(), EVENT_1, EventsStorage.EventType.MESSAGE_RECEIPTS);
		assertEquals(1, storage.getNumberOfEvents(getContext(), EventsStorage.EventType.MESSAGE_RECEIPTS));
//		storage.saveEvent(getContext(), EVENT_4, EventType.API_VALIDATION_ERROR);
//		assertEquals(1, storage.getNumberOfEvents(getContext(), EventType.API_VALIDATION_ERROR));
//		assertEquals(2, storage.getNumberOfEvents(getContext(), EventType.ALL));
		storage.reset(getContext(), EventsStorage.EventType.MESSAGE_RECEIPTS);
//		assertEquals(1, storage.getNumberOfEvents(getContext(), EventsStorage.EventType.ALL));
//		storage.reset(getContext(), EventType.API_VALIDATION_ERROR);
		assertEquals(0, storage.getNumberOfEvents(getContext(), EventsStorage.EventType.ALL));
		assertEquals(0, storage.getNumberOfEvents(getContext(), EventsStorage.EventType.MESSAGE_RECEIPTS));
//		assertEquals(0, storage.getNumberOfEvents(getContext(), EventType.API_VALIDATION_ERROR));
	}

	public void testReset2() {

		// Tests the reset mechanism
		storage.saveEvent(getContext(), EVENT_1, EventsStorage.EventType.MESSAGE_RECEIPTS);
		assertEquals(1, storage.getNumberOfEvents(getContext(), EventsStorage.EventType.MESSAGE_RECEIPTS));
//		storage.saveEvent(getContext(), EVENT_4, EventType.API_VALIDATION_ERROR);
//		assertEquals(1, storage.getNumberOfEvents(getContext(), EventType.API_VALIDATION_ERROR));
		assertEquals(1, storage.getNumberOfEvents(getContext(), EventsStorage.EventType.ALL));
		storage.reset(getContext(), EventsStorage.EventType.ALL);
		assertEquals(0, storage.getNumberOfEvents(getContext(), EventsStorage.EventType.ALL));
		assertEquals(0, storage.getNumberOfEvents(getContext(), EventsStorage.EventType.MESSAGE_RECEIPTS));
//		assertEquals(0, storage.getNumberOfEvents(getContext(), EventType.API_VALIDATION_ERROR));
	}

	public void testSetStatus() {

		final Uri uri1 = storage.saveEvent(getContext(), EVENT_1, EventsStorage.EventType.MESSAGE_RECEIPTS);
		assertEquals(EventBase.Status.NOT_POSTED, EVENT_1.getStatus());
		storage.setEventStatus(getContext(), uri1, EventBase.Status.POSTING);
		assertEquals(EventBase.Status.POSTING, EVENT_1.getStatus());
		storage.setEventStatus(getContext(), uri1, EventBase.Status.POSTED);
		assertEquals(EventBase.Status.POSTED, EVENT_1.getStatus());

//		final Uri uri2 = storage.saveEvent(getContext(), EVENT_4, EventType.API_VALIDATION_ERROR);
//		assertEquals(Event.Status.NOT_POSTED, EVENT_4.getStatus());
//		storage.setEventStatus(getContext(), uri2, Event.Status.POSTING);
//		assertEquals(Event.Status.POSTING, EVENT_4.getStatus());
//		storage.setEventStatus(getContext(), uri2, Event.Status.POSTED);
//		assertEquals(Event.Status.POSTED, EVENT_4.getStatus());
	}

	public void testGetMessageReceiptEventUrisWithStatus() {
		EVENT_1.setStatus(EventBase.Status.POSTED);
		EVENT_2.setStatus(EventBase.Status.POSTING_ERROR);
		EVENT_3.setStatus(EventBase.Status.POSTING_ERROR);
		final Uri uri1 = storage.saveEvent(getContext(), EVENT_1, EventsStorage.EventType.MESSAGE_RECEIPTS);
		final Uri uri2 = storage.saveEvent(getContext(), EVENT_2, EventsStorage.EventType.MESSAGE_RECEIPTS);
		final Uri uri3 = storage.saveEvent(getContext(), EVENT_3, EventsStorage.EventType.MESSAGE_RECEIPTS);

		final List<Uri> uris1 = storage.getEventUrisWithStatus(getContext(), EventsStorage.EventType.MESSAGE_RECEIPTS, EventBase.Status.NOT_POSTED);
		assertEquals(0, uris1.size());

		final List<Uri> uris2 = storage.getEventUrisWithStatus(getContext(), EventsStorage.EventType.MESSAGE_RECEIPTS, EventBase.Status.POSTING);
		assertEquals(0, uris2.size());

		final List<Uri> uris3 = storage.getEventUrisWithStatus(getContext(), EventsStorage.EventType.MESSAGE_RECEIPTS, EventBase.Status.POSTED);
		assertEquals(1, uris3.size());
		assertEquals(uri1, uris3.get(0));

		final List<Uri> uris4 = storage.getEventUrisWithStatus(getContext(), EventsStorage.EventType.MESSAGE_RECEIPTS, EventBase.Status.POSTING_ERROR);
		assertEquals(2, uris4.size());
		assertTrue(uri2.equals(uris4.get(0)) || uri2.equals(uris4.get(1)));
		assertTrue(uri3.equals(uris4.get(0)) || uri3.equals(uris4.get(1)));
		assertFalse(uris4.get(0).equals(uris4.get(1)));
	}

//	public void testGetApiValidationErrorEventUrisWithStatus() {
//		EVENT_4.setStatus(Event.Status.POSTED);
//		EVENT_5.setStatus(Event.Status.POSTING_ERROR);
//		EVENT_6.setStatus(Event.Status.POSTING_ERROR);
//		final Uri uri1 = storage.saveEvent(getContext(), EVENT_4, EventType.API_VALIDATION_ERROR);
//		final Uri uri2 = storage.saveEvent(getContext(), EVENT_5, EventType.API_VALIDATION_ERROR);
//		final Uri uri3 = storage.saveEvent(getContext(), EVENT_6, EventType.API_VALIDATION_ERROR);
//
//		final List<Uri> uris1 = storage.getEventUrisWithStatus(getContext(), EventType.API_VALIDATION_ERROR, Event.Status.NOT_POSTED);
//		assertEquals(0, uris1.size());
//
//		final List<Uri> uris2 = storage.getEventUrisWithStatus(getContext(), EventType.API_VALIDATION_ERROR, Event.Status.POSTING);
//		assertEquals(0, uris2.size());
//
//		final List<Uri> uris3 = storage.getEventUrisWithStatus(getContext(), EventType.API_VALIDATION_ERROR, Event.Status.POSTED);
//		assertEquals(1, uris3.size());
//		assertEquals(uri1, uris3.get(0));
//
//		final List<Uri> uris4 = storage.getEventUrisWithStatus(getContext(), EventType.API_VALIDATION_ERROR, Event.Status.POSTING_ERROR);
//		assertEquals(2, uris4.size());
//		assertTrue(uri2.equals(uris4.get(0)) || uri2.equals(uris4.get(1)));
//		assertTrue(uri3.equals(uris4.get(0)) || uri3.equals(uris4.get(1)));
//		assertFalse(uris4.get(0).equals(uris4.get(1)));
//	}

	public void testGetMixedEventUrisWithStatus() {
		EVENT_1.setStatus(EventBase.Status.NOT_POSTED);
		EVENT_2.setStatus(EventBase.Status.POSTING);
		EVENT_3.setStatus(EventBase.Status.POSTING_ERROR);
//		EVENT_4.setStatus(Event.Status.POSTED);
//		EVENT_5.setStatus(Event.Status.POSTING_ERROR);
//		EVENT_6.setStatus(Event.Status.POSTING_ERROR);
		storage.saveEvent(getContext(), EVENT_1, EventsStorage.EventType.MESSAGE_RECEIPTS);
		storage.saveEvent(getContext(), EVENT_2, EventsStorage.EventType.MESSAGE_RECEIPTS);
		storage.saveEvent(getContext(), EVENT_3, EventsStorage.EventType.MESSAGE_RECEIPTS);
//		storage.saveEvent(getContext(), EVENT_4, EventType.API_VALIDATION_ERROR);
//		storage.saveEvent(getContext(), EVENT_5, EventType.API_VALIDATION_ERROR);
//		storage.saveEvent(getContext(), EVENT_6, EventType.API_VALIDATION_ERROR);

		final List<Uri> uris1 = storage.getEventUrisWithStatus(getContext(), EventsStorage.EventType.ALL, EventBase.Status.NOT_POSTED);
		assertEquals(1, uris1.size());

		final List<Uri> uris2 = storage.getEventUrisWithStatus(getContext(), EventsStorage.EventType.ALL, EventBase.Status.POSTING);
		assertEquals(1, uris2.size());

		final List<Uri> uris3 = storage.getEventUrisWithStatus(getContext(), EventsStorage.EventType.ALL, EventBase.Status.POSTED);
		assertEquals(0, uris3.size());

		final List<Uri> uris4 = storage.getEventUrisWithStatus(getContext(), EventsStorage.EventType.ALL, EventBase.Status.POSTING_ERROR);
		assertEquals(1, uris4.size());
	}
}
