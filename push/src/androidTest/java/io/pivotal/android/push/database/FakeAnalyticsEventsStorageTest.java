package io.pivotal.android.push.database;

import android.net.Uri;
import android.test.AndroidTestCase;

import java.util.LinkedList;
import java.util.List;

import io.pivotal.android.push.model.analytics.DummyEvent;
import io.pivotal.android.push.model.analytics.AnalyticsEvent;

public class FakeAnalyticsEventsStorageTest extends AndroidTestCase {

	private static final Uri NON_EXISTENT_FILE_1 = Uri.parse("file://events/this_name_does_not_exist_come_on!");
    private static final String TEST_VARIANT_ID_1 = "TEST_VARIANT_ID_1";
    private static final String TEST_VARIANT_ID_2 = "TEST_VARIANT_ID_2";
    private static final String TEST_VARIANT_ID_3 = "TEST_VARIANT_ID_3";
    private static final String TEST_MESSAGE_UUID_1 = "TEST_MESSAGE_UUID_1";
    private static final String TEST_MESSAGE_UUID_2 = "TEST_MESSAGE_UUID_2";
    private static final String TEST_MESSAGE_UUID_3 = "TEST_MESSAGE_UUID_3";
    private static final String TEST_DEVICE_ID_1 = "TEST-DEVICE-ID-1";
    private static final String TEST_DEVICE_ID_2 = "TEST-DEVICE-ID-2";
    private static final String TEST_DEVICE_ID_3 = "TEST-DEVICE-ID-3";
	private AnalyticsEvent EVENT_1;
	private AnalyticsEvent EVENT_2;
	private AnalyticsEvent EVENT_3;
	private FakeAnalyticsEventsStorage storage;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		EVENT_1 = DummyEvent.getEvent(TEST_DEVICE_ID_1);
		EVENT_2 = DummyEvent.getEvent(TEST_DEVICE_ID_2);
		EVENT_3 = DummyEvent.getEvent(TEST_DEVICE_ID_3);
		storage = new FakeAnalyticsEventsStorage();
	}

	public void testStartState() {

		// Tests the initial state of the fake filesystem
		assertEquals(0, storage.getNumberOfEvents());
	}

	public void testSaveMessageReceiptEventOnce() {

		// Tests saving one file into the fake filesystem
		final Uri saveResult = storage.saveEvent(EVENT_1);
		assertNotNull(saveResult);
		assertEquals(1, storage.getNumberOfEvents());
	}

    public void testSaveFails() {
        storage.setWillSaveFail(true);
        assertNull(storage.saveEvent(EVENT_1));
    }

	public void testSaveMessageReceiptEventTwice() {

		// Save one file into the fake filesystem
		final Uri saveResult1 = storage.saveEvent(EVENT_1);
		assertNotNull(saveResult1);
		assertEquals(1, storage.getNumberOfEvents());

		// Save another file into the fake filesystem
		final Uri saveResult2 = storage.saveEvent(EVENT_2);
		assertNotNull(saveResult2);
		assertEquals(2, storage.getNumberOfEvents());
	}

	public void testSaveMessageReceiptAndRead() {

		// Save a file into the fake filesystem
		storage.saveEvent(EVENT_1);
		final List<Uri> uris1 = storage.getEventUris();
		assertEquals(1, uris1.size());
		final List<Uri> uris2 = storage.getEventUris();
		assertEquals(1, uris2.size());
		assertEquals(uris1.get(0), uris2.get(0));

		// Read the file back and confirm that it matches
		final AnalyticsEvent fileContents = (AnalyticsEvent) storage.readEvent(uris2.get(0));
		assertEquals(EVENT_1, fileContents);
	}

	public void testReadNonExistentFile1() {

		// Try to read a file that doesn't exist
		boolean exceptionThrown = false;
		try {
			storage.readEvent(NON_EXISTENT_FILE_1);
		} catch (Exception e) {
			exceptionThrown = true;
		}
		assertTrue(exceptionThrown);
	}

	public void testSaveMessageReceiptAndDelete() {

		// Save a file into the fake filesystem
		storage.saveEvent(EVENT_1);
		final List<Uri> files = storage.getEventUris();
		assertEquals(1, storage.getNumberOfEvents());

		// Try to delete the file from the fake filesystem
		storage.deleteEvents(files);
		assertEquals(0, storage.getNumberOfEvents());
	}

	public void testDeleteNonExistentFile() {

		// Save a file into the fake filesystem
		storage.saveEvent(EVENT_1);
		assertEquals(1, storage.getNumberOfEvents());

        // Try to delete a file that doesn't exist
		final List<Uri> bogusList = new LinkedList<Uri>();
		bogusList.add(NON_EXISTENT_FILE_1);
		storage.deleteEvents(bogusList);
		assertEquals(1, storage.getNumberOfEvents());
	}

	public void testDeleteOneOfTwoMessageReceiptFiles() {

		// Save a couple of files into the fake filesystem
		storage.saveEvent(EVENT_1);
		assertEquals(1, storage.getNumberOfEvents());
		storage.saveEvent(EVENT_2);
		assertEquals(2, storage.getNumberOfEvents());

		// Delete one of the two files
		final List<Uri> files1 = storage.getEventUris();
		final List<Uri> filesRemaining = new LinkedList<Uri>();
		filesRemaining.add(files1.get(1));
		files1.remove(1);
		storage.deleteEvents(files1);
		assertEquals(1, storage.getNumberOfEvents());

		// Ensure the other file is still in the fake filesystem
		final List<Uri> files2 = storage.getEventUris();
		assertEquals(files1.size(), files2.size());
		assertEquals(1, files2.size());
		assertEquals(filesRemaining.get(0), files2.get(0));
	}

	public void testReset() {

		// Tests the reset mechanism
		storage.saveEvent(EVENT_1);
		assertEquals(1, storage.getNumberOfEvents());
		storage.reset();
		assertEquals(0, storage.getNumberOfEvents());
	}

	public void testSetStatus() {

		final Uri uri1 = storage.saveEvent(EVENT_1);
        assertEventStatus(uri1, AnalyticsEvent.Status.NOT_POSTED);

		storage.setEventStatus(uri1, AnalyticsEvent.Status.POSTING);
        assertEventStatus(uri1, AnalyticsEvent.Status.POSTING);

		storage.setEventStatus(uri1, AnalyticsEvent.Status.POSTED);
        assertEventStatus(uri1, AnalyticsEvent.Status.POSTED);
	}

    private void assertEventStatus(Uri uri, int expectedStatus) {
        final AnalyticsEvent event = storage.readEvent(uri);
        assertEquals(expectedStatus, event.getStatus());
    }

	public void testGetMessageReceiptEventUrisWithStatus() {
		EVENT_1.setStatus(AnalyticsEvent.Status.POSTED);
		EVENT_2.setStatus(AnalyticsEvent.Status.POSTING_ERROR);
		EVENT_3.setStatus(AnalyticsEvent.Status.POSTING_ERROR);
		final Uri uri1 = storage.saveEvent(EVENT_1);
		final Uri uri2 = storage.saveEvent(EVENT_2);
		final Uri uri3 = storage.saveEvent(EVENT_3);

		final List<Uri> uris1 = storage.getEventUrisWithStatus(AnalyticsEvent.Status.NOT_POSTED);
		assertEquals(0, uris1.size());

		final List<Uri> uris2 = storage.getEventUrisWithStatus(AnalyticsEvent.Status.POSTING);
		assertEquals(0, uris2.size());

		final List<Uri> uris3 = storage.getEventUrisWithStatus(AnalyticsEvent.Status.POSTED);
		assertEquals(1, uris3.size());
		assertEquals(uri1, uris3.get(0));

		final List<Uri> uris4 = storage.getEventUrisWithStatus(AnalyticsEvent.Status.POSTING_ERROR);
		assertEquals(2, uris4.size());
		assertTrue(uri2.equals(uris4.get(0)) || uri2.equals(uris4.get(1)));
		assertTrue(uri3.equals(uris4.get(0)) || uri3.equals(uris4.get(1)));
		assertFalse(uris4.get(0).equals(uris4.get(1)));
	}
}
