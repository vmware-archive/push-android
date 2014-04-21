package com.pivotal.cf.mobile.pushsdk.database;

import android.net.Uri;
import android.test.AndroidTestCase;

import com.pivotal.cf.mobile.pushsdk.model.BaseEvent;
import com.pivotal.cf.mobile.pushsdk.model.MessageReceiptEvent;

import junit.framework.Assert;

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
    private DatabaseEventsStorage eventsStorage;
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
        EventsDatabaseWrapper.createDatabaseInstance(getContext());
        EVENT_1 = MessageReceiptEvent.getMessageReceiptEvent(TEST_VARIANT_UUID_1, TEST_MESSAGE_UUID_1);
        EVENT_2 = MessageReceiptEvent.getMessageReceiptEvent(TEST_VARIANT_UUID_2, TEST_MESSAGE_UUID_2);
//		EVENT_3 = ApiValidationErrorEvent.getApiValidationErrorEvent("URL", "POST", REQUEST_HEADERS, 44, "content/silly", "Ministry of Silly Walks", "ERROR_MESSAGE OF DOOM", metadata);
//		EVENT_4 = ApiValidationErrorEvent.getApiValidationErrorEvent("valid URL", "POST", REQUEST_HEADERS, 44, "content/silly", "Ministry of Silly Walks", "DOUBLE ERROR", metadata);
        eventsStorage = new DatabaseEventsStorage();
        eventsStorage.reset(EventsStorage.EventType.ALL);
        // eventsStorage.reset(EventsStorage.EventType.MESSAGE_RECEIPT);
        // eventsStorage.reset(EventsStorage.EventType.API_VALIDATION_ERROR);
    }

    public void testStartState() {
        assertNotNull(eventsStorage);
        final List<Uri> files1 = eventsStorage.getEventUris(EventsStorage.EventType.MESSAGE_RECEIPT);
        assertTrue(files1 == null || files1.size() == 0);
//		final List<Uri> files2 = eventsStorage.getEventUris(EventsStorage.EventType.API_VALIDATION_ERROR);
//		assertTrue(files2 == null || files2.size() == 0);
        final List<Uri> files3 = eventsStorage.getEventUris(EventsStorage.EventType.ALL);
        assertTrue(files3 == null || files3.size() == 0);
        assertEquals(0, eventsStorage.getNumberOfEvents(EventsStorage.EventType.MESSAGE_RECEIPT));
//		assertEquals(0, eventsStorage.getNumberOfEvents(EventsStorage.EventType.API_VALIDATION_ERROR));
        assertEquals(0, eventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
        assertFalse(EVENT_1.equals(EVENT_2));
//		assertFalse(EVENT_3.equals(EVENT_4));
    }

    public void testSaveMessageReceiptOnce() {
        final Uri saveResult = eventsStorage.saveEvent(EVENT_1, EventsStorage.EventType.MESSAGE_RECEIPT);
        assertNotNull(saveResult);
        assertEquals(1, eventsStorage.getNumberOfEvents(EventsStorage.EventType.MESSAGE_RECEIPT));
//		assertEquals(0, eventsStorage.getNumberOfEvents(EventsStorage.EventType.API_VALIDATION_ERROR));
        assertEquals(1, eventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
    }

//	public void testSaveApiValidationErrorOnce() {
//		final Uri saveResult = eventsStorage.saveEvent(EVENT_3, EventsStorage.EventType.API_VALIDATION_ERROR);
//		assertNotNull(saveResult);
//		assertEquals(1, eventsStorage.getNumberOfEvents(EventsStorage.EventType.API_VALIDATION_ERROR));
//		assertEquals(0, eventsStorage.getNumberOfEvents(EventsStorage.EventType.MESSAGE_RECEIPT));
//		assertEquals(1, eventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
//	}

    public void testSaveMessageReceiptTwice() {
        eventsStorage.saveEvent(EVENT_1, EventsStorage.EventType.MESSAGE_RECEIPT);
        assertEquals(1, eventsStorage.getNumberOfEvents(EventsStorage.EventType.MESSAGE_RECEIPT));
        assertEquals(1, eventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
        final Uri saveResult = eventsStorage.saveEvent(EVENT_2, EventsStorage.EventType.MESSAGE_RECEIPT);
        assertNotNull(saveResult);
        assertEquals(2, eventsStorage.getNumberOfEvents(EventsStorage.EventType.MESSAGE_RECEIPT));
        assertEquals(2, eventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
    }

//	public void testSaveApiValidationErrorTwice() {
//		eventsStorage.saveEvent(EVENT_3, EventsStorage.EventType.API_VALIDATION_ERROR);
//		assertEquals(1, eventsStorage.getNumberOfEvents(EventsStorage.EventType.API_VALIDATION_ERROR));
//		assertEquals(1, eventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
//		final Uri saveResult = eventsStorage.saveEvent(EVENT_4, EventsStorage.EventType.API_VALIDATION_ERROR);
//		assertNotNull(saveResult);
//		assertEquals(2, eventsStorage.getNumberOfEvents(EventsStorage.EventType.API_VALIDATION_ERROR));
//		assertEquals(2, eventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
//	}

    public void testSaveMessageReceiptAndRead() {
        eventsStorage.saveEvent(EVENT_1, EventsStorage.EventType.MESSAGE_RECEIPT);
        final List<Uri> files1 = eventsStorage.getEventUris(EventsStorage.EventType.MESSAGE_RECEIPT);
        assertEquals(1, files1.size());
        final List<Uri> files2 = eventsStorage.getEventUris(EventsStorage.EventType.ALL);
        assertEquals(1, files2.size());
        final MessageReceiptEvent fileContents = (MessageReceiptEvent) eventsStorage.readEvent(files1.get(0));
        assertEquals(EVENT_1, fileContents);
    }

//	public void testSaveApiValidationErrorAndRead() {
//		eventsStorage.saveEvent(EVENT_3, EventsStorage.EventType.API_VALIDATION_ERROR);
//		final List<Uri> files1 = eventsStorage.getEventUris(EventsStorage.EventType.API_VALIDATION_ERROR);
//		assertEquals(1, files1.size());
//		final List<Uri> files2 = eventsStorage.getEventUris(EventsStorage.EventType.ALL);
//		assertEquals(1, files2.size());
//		final ApiValidationErrorEvent fileContents = (ApiValidationErrorEvent) eventsStorage.readEvent(files2.get(0));
//		assertEquals(EVENT_3, fileContents);
//	}

    public void testReadNonExistentFile() {
        boolean exceptionThrown = false;
        try {
            final MessageReceiptEvent fileContents1 = (MessageReceiptEvent) eventsStorage.readEvent(NON_EXISTENT_FILE_1);
            assertEquals(null, fileContents1);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

//		exceptionThrown = false;
//		try {
//			final ApiValidationErrorEvent fileContents2 = (ApiValidationErrorEvent) eventsStorage.readEvent(NON_EXISTENT_FILE_2);
//			assertEquals(null, fileContents2);
//		} catch (Exception e) {
//			exceptionThrown = true;
//		}
//		assertTrue(exceptionThrown);
    }

    public void testSetStatusNonExistentFile() {
        boolean exceptionThrown = false;
        try {
            eventsStorage.setEventStatus(NON_EXISTENT_FILE_1, BaseEvent.Status.POSTED);
        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

//		exceptionThrown = false;
//		try {
//			eventsStorage.setEventStatus(NON_EXISTENT_FILE_2, BaseEvent.Status.POSTED);
//		} catch (IllegalArgumentException e) {
//			exceptionThrown = true;
//		}
//		assertTrue(exceptionThrown);
    }

    public void testSaveMessageReceiptAndDelete() {
        eventsStorage.saveEvent(EVENT_1, EventsStorage.EventType.MESSAGE_RECEIPT);
        final List<Uri> files = eventsStorage.getEventUris(EventsStorage.EventType.MESSAGE_RECEIPT);
        assertEquals(1, eventsStorage.getNumberOfEvents(EventsStorage.EventType.MESSAGE_RECEIPT));
        assertEquals(1, eventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
        eventsStorage.deleteEvents(files, EventsStorage.EventType.MESSAGE_RECEIPT);
        assertEquals(0, eventsStorage.getNumberOfEvents(EventsStorage.EventType.MESSAGE_RECEIPT));
        assertEquals(0, eventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
    }

//	public void testSaveApiValidationErrorAndDelete() {
//		eventsStorage.saveEvent(EVENT_3, EventsStorage.EventType.API_VALIDATION_ERROR);
//		final List<Uri> files = eventsStorage.getEventUris(EventsStorage.EventType.API_VALIDATION_ERROR);
//		assertEquals(1, eventsStorage.getNumberOfEvents(EventsStorage.EventType.API_VALIDATION_ERROR));
//		assertEquals(1, eventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
//		eventsStorage.deleteEvents(files, EventsStorage.EventType.API_VALIDATION_ERROR);
//		assertEquals(0, eventsStorage.getNumberOfEvents(EventsStorage.EventType.API_VALIDATION_ERROR));
//		assertEquals(0, eventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
//	}

    public void testDeleteNonExistingFile() {
        eventsStorage.saveEvent(EVENT_1, EventsStorage.EventType.MESSAGE_RECEIPT);
        assertEquals(1, eventsStorage.getNumberOfEvents(EventsStorage.EventType.MESSAGE_RECEIPT));
        assertEquals(1, eventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
        final List<Uri> bogusList = new LinkedList<Uri>();
        bogusList.add(NON_EXISTENT_FILE_1);
        eventsStorage.deleteEvents(bogusList, EventsStorage.EventType.MESSAGE_RECEIPT);
        assertEquals(1, eventsStorage.getNumberOfEvents(EventsStorage.EventType.MESSAGE_RECEIPT));
        assertEquals(1, eventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));

//		eventsStorage.saveEvent(EVENT_3, EventsStorage.EventType.API_VALIDATION_ERROR);
//		assertEquals(1, eventsStorage.getNumberOfEvents(EventsStorage.EventType.API_VALIDATION_ERROR));
//		assertEquals(2, eventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
//		final List<Uri> blahList = new LinkedList<Uri>();
//		blahList.add(NON_EXISTENT_FILE_2);
//		eventsStorage.deleteEvents(blahList, EventsStorage.EventType.API_VALIDATION_ERROR);
//		assertEquals(1, eventsStorage.getNumberOfEvents(EventsStorage.EventType.API_VALIDATION_ERROR));
//		assertEquals(2, eventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
    }

    public void testDeleteOneOfTwoMessageReceiptFiles() {

        eventsStorage.saveEvent(EVENT_1, EventsStorage.EventType.MESSAGE_RECEIPT);
        assertEquals(1, eventsStorage.getNumberOfEvents(EventsStorage.EventType.MESSAGE_RECEIPT));

        eventsStorage.saveEvent(EVENT_2, EventsStorage.EventType.MESSAGE_RECEIPT);
        assertEquals(2, eventsStorage.getNumberOfEvents(EventsStorage.EventType.MESSAGE_RECEIPT));
        assertEquals(2, eventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));

        final List<Uri> files1 = eventsStorage.getEventUris(EventsStorage.EventType.MESSAGE_RECEIPT);
        final List<Uri> filesRemaining = new LinkedList<Uri>();
        filesRemaining.add(files1.get(1));
        files1.remove(1);

        eventsStorage.deleteEvents(files1, EventsStorage.EventType.MESSAGE_RECEIPT);
        assertEquals(1, eventsStorage.getNumberOfEvents(EventsStorage.EventType.MESSAGE_RECEIPT));
        assertEquals(1, eventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));

        final List<Uri> files2 = eventsStorage.getEventUris(EventsStorage.EventType.MESSAGE_RECEIPT);
        assertEquals(files1.size(), files2.size());
        assertEquals(1, files2.size());
        assertEquals(filesRemaining.get(0), files2.get(0));
    }

//	public void testDeleteOneOfTwoApiValidationErrorFiles() {
//
//		eventsStorage.saveEvent(EVENT_3, EventsStorage.EventType.API_VALIDATION_ERROR);
//		assertEquals(1, eventsStorage.getNumberOfEvents(EventsStorage.EventType.API_VALIDATION_ERROR));
//
//		eventsStorage.saveEvent(EVENT_4, EventsStorage.EventType.API_VALIDATION_ERROR);
//		assertEquals(2, eventsStorage.getNumberOfEvents(EventsStorage.EventType.API_VALIDATION_ERROR));
//		assertEquals(2, eventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
//
//		final List<Uri> files1 = eventsStorage.getEventUris(EventsStorage.EventType.API_VALIDATION_ERROR);
//		assertEquals(2, files1.size());
//		final List<Uri> filesRemaining = new LinkedList<Uri>();
//		filesRemaining.add(files1.get(1));
//		files1.remove(1);
//		assertEquals(1, files1.size());
//
//		eventsStorage.deleteEvents(files1, EventsStorage.EventType.API_VALIDATION_ERROR);
//		assertEquals(1, eventsStorage.getNumberOfEvents(EventsStorage.EventType.API_VALIDATION_ERROR));
//		assertEquals(1, eventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
//
//		final List<Uri> files2 = eventsStorage.getEventUris(EventsStorage.EventType.API_VALIDATION_ERROR);
//		assertEquals(1, files2.size());
//		assertEquals(filesRemaining.get(0), files2.get(0));
//	}

    public void testReset1() {

        eventsStorage.saveEvent(EVENT_1, EventsStorage.EventType.MESSAGE_RECEIPT);
//		eventsStorage.saveEvent(EVENT_3, EventsStorage.EventType.API_VALIDATION_ERROR);
        assertEquals(1, eventsStorage.getNumberOfEvents(EventsStorage.EventType.MESSAGE_RECEIPT));
//		assertEquals(1, eventsStorage.getNumberOfEvents(EventsStorage.EventType.API_VALIDATION_ERROR));
        assertEquals(1, eventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));

        eventsStorage.reset(EventsStorage.EventType.MESSAGE_RECEIPT);
//		eventsStorage.reset(EventsStorage.EventType.API_VALIDATION_ERROR);
        assertEquals(0, eventsStorage.getNumberOfEvents(EventsStorage.EventType.MESSAGE_RECEIPT));
//		assertEquals(0, eventsStorage.getNumberOfEvents(EventsStorage.EventType.API_VALIDATION_ERROR));
        assertEquals(0, eventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
    }

    public void testReset2() {

        eventsStorage.saveEvent(EVENT_1, EventsStorage.EventType.MESSAGE_RECEIPT);
//		eventsStorage.saveEvent(EVENT_3, EventsStorage.EventType.API_VALIDATION_ERROR);
        assertEquals(1, eventsStorage.getNumberOfEvents(EventsStorage.EventType.MESSAGE_RECEIPT));
//		assertEquals(1, eventsStorage.getNumberOfEvents(EventsStorage.EventType.API_VALIDATION_ERROR));
        assertEquals(1, eventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));

        eventsStorage.reset(EventsStorage.EventType.ALL);
        assertEquals(0, eventsStorage.getNumberOfEvents(EventsStorage.EventType.MESSAGE_RECEIPT));
//		assertEquals(0, eventsStorage.getNumberOfEvents(EventsStorage.EventType.API_VALIDATION_ERROR));
        assertEquals(0, eventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
    }

    public void testSetStatus() {

        final Uri uri1 = eventsStorage.saveEvent(EVENT_1, EventsStorage.EventType.MESSAGE_RECEIPT);
        assertEquals(BaseEvent.Status.NOT_POSTED, EVENT_1.getStatus());

        eventsStorage.setEventStatus(uri1, BaseEvent.Status.POSTING);
        Assert.assertEquals(BaseEvent.Status.POSTING, eventsStorage.readEvent(uri1).getStatus());

        eventsStorage.setEventStatus(uri1, BaseEvent.Status.POSTED);
        Assert.assertEquals(BaseEvent.Status.POSTED, eventsStorage.readEvent(uri1).getStatus());

//		final Uri uri2 = eventsStorage.saveEvent(EVENT_3, EventsStorage.EventType.API_VALIDATION_ERROR);
//		assertEquals(BaseEvent.Status.NOT_POSTED, EVENT_3.getStatus());
//
//		eventsStorage.setEventStatus(uri2, BaseEvent.Status.POSTING);
//		assertEquals(BaseEvent.Status.POSTING, eventsStorage.readEvent(uri2).getStatus());
//
//		eventsStorage.setEventStatus(uri2, BaseEvent.Status.POSTED);
//		assertEquals(BaseEvent.Status.POSTED, eventsStorage.readEvent(uri2).getStatus());
    }

    public void testGetMessageReceiptEventUrisWithStatus() {
        EVENT_1.setStatus(BaseEvent.Status.POSTED);
        EVENT_2.setStatus(BaseEvent.Status.POSTING_ERROR);
        eventsStorage.saveEvent(EVENT_1, EventsStorage.EventType.MESSAGE_RECEIPT);
        eventsStorage.saveEvent(EVENT_2, EventsStorage.EventType.MESSAGE_RECEIPT);

        final List<Uri> uris1 = eventsStorage.getEventUrisWithStatus(EventsStorage.EventType.MESSAGE_RECEIPT, BaseEvent.Status.NOT_POSTED);
        assertEquals(0, uris1.size());

        final List<Uri> uris2 = eventsStorage.getEventUrisWithStatus(EventsStorage.EventType.MESSAGE_RECEIPT, BaseEvent.Status.POSTING);
        assertEquals(0, uris2.size());

        final List<Uri> uris3 = eventsStorage.getEventUrisWithStatus(EventsStorage.EventType.MESSAGE_RECEIPT, BaseEvent.Status.POSTING_ERROR);
        assertEquals(1, uris3.size());
    }

//	public void testGetApiValidationErrorEventUrisWithStatus() {
//		EVENT_3.setStatus(BaseEvent.Status.POSTING);
//		EVENT_4.setStatus(BaseEvent.Status.NOT_POSTED);
//		eventsStorage.saveEvent(EVENT_3, EventsStorage.EventType.API_VALIDATION_ERROR);
//		eventsStorage.saveEvent(EVENT_4, EventsStorage.EventType.API_VALIDATION_ERROR);
//
//		final List<Uri> uris1 = eventsStorage.getEventUrisWithStatus(EventsStorage.EventType.API_VALIDATION_ERROR, BaseEvent.Status.NOT_POSTED);
//		assertEquals(1, uris1.size());
//
//		final List<Uri> uris2 = eventsStorage.getEventUrisWithStatus(EventsStorage.EventType.API_VALIDATION_ERROR, BaseEvent.Status.POSTING);
//		assertEquals(1, uris2.size());
//
//		final List<Uri> uris3 = eventsStorage.getEventUrisWithStatus(EventsStorage.EventType.API_VALIDATION_ERROR, BaseEvent.Status.POSTING_ERROR);
//		assertEquals(0, uris3.size());
//	}

    public void testGetMixedEventUrisWithStatus() {
        EVENT_1.setStatus(BaseEvent.Status.NOT_POSTED);
        EVENT_2.setStatus(BaseEvent.Status.POSTING);
//		EVENT_3.setStatus(BaseEvent.Status.NOT_POSTED);
//		EVENT_4.setStatus(BaseEvent.Status.POSTED);
        eventsStorage.saveEvent(EVENT_1, EventsStorage.EventType.MESSAGE_RECEIPT);
        eventsStorage.saveEvent(EVENT_2, EventsStorage.EventType.MESSAGE_RECEIPT);
//		eventsStorage.saveEvent(EVENT_3, EventsStorage.EventType.API_VALIDATION_ERROR);
//		eventsStorage.saveEvent(EVENT_4, EventsStorage.EventType.API_VALIDATION_ERROR);

        final List<Uri> uris1 = eventsStorage.getEventUrisWithStatus(EventsStorage.EventType.ALL, BaseEvent.Status.NOT_POSTED);
        assertEquals(1, uris1.size());

        final List<Uri> uris2 = eventsStorage.getEventUrisWithStatus(EventsStorage.EventType.ALL, BaseEvent.Status.POSTING);
        assertEquals(1, uris2.size());

        final List<Uri> uris3 = eventsStorage.getEventUrisWithStatus(EventsStorage.EventType.ALL, BaseEvent.Status.POSTED);
        assertEquals(0, uris3.size());

        final List<Uri> uris4 = eventsStorage.getEventUrisWithStatus(EventsStorage.EventType.ALL, BaseEvent.Status.POSTING_ERROR);
        assertEquals(0, uris4.size());
    }

    public void testNotAllowedToSaveWithTypeAll() {
        try {
            eventsStorage.saveEvent(EVENT_1, EventsStorage.EventType.ALL); // should throw
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
        assertEquals(0, eventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
    }

    public void testNotAllowedToDeleteWithTypeAll() {
        try {
            final Uri uri1 = eventsStorage.saveEvent(EVENT_1, EventsStorage.EventType.MESSAGE_RECEIPT);
            final Uri uri2 = eventsStorage.saveEvent(EVENT_2, EventsStorage.EventType.MESSAGE_RECEIPT);
            final List<Uri> list = new LinkedList<Uri>();
            list.add(uri1);
            list.add(uri2);
            assertEquals(2, eventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
            eventsStorage.deleteEvents(list, EventsStorage.EventType.ALL); // should throw
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
        assertEquals(2, eventsStorage.getNumberOfEvents(EventsStorage.EventType.ALL));
    }

}
