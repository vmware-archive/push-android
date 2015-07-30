package io.pivotal.android.push.database;

import android.net.Uri;
import android.test.AndroidTestCase;

import junit.framework.Assert;

import java.util.LinkedList;
import java.util.List;

import io.pivotal.android.push.model.analytics.DummyEvent;
import io.pivotal.android.push.model.analytics.Event;

public class DatabaseEventsStorageTest extends AndroidTestCase {

    private static final String TEST_DEVICE_UUID_1 = "TEST-DEVICE-UUID-1";
    private static final String TEST_DEVICE_UUID_2 = "TEST-DEVICE-UUID-2";
    private static final Uri NON_EXISTENT_FILE_1 = Uri.withAppendedPath(Database.EVENTS_CONTENT_URI, "/999999");
    private DatabaseEventsStorage eventsStorage;
    private Event EVENT_1;
    private Event EVENT_2;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        DatabaseWrapper.createDatabaseInstance(getContext());
        EVENT_1 = DummyEvent.getEvent(TEST_DEVICE_UUID_1);
        EVENT_2 = DummyEvent.getEvent(TEST_DEVICE_UUID_2);
        eventsStorage = new DatabaseEventsStorage();
        eventsStorage.reset();
    }

    public void testStartState() {
        assertNotNull(eventsStorage);
        final List<Uri> files1 = eventsStorage.getEventUris();
        assertTrue(files1 == null || files1.size() == 0);
        Assert.assertEquals(0, eventsStorage.getNumberOfEvents());
        assertFalse(EVENT_1.equals(EVENT_2));
    }

    public void testSaveMessageReceiptOnce() {
        final Uri saveResult = eventsStorage.saveEvent(EVENT_1);
        assertNotNull(saveResult);
        Assert.assertEquals(1, eventsStorage.getNumberOfEvents());
    }

    public void testSaveMessageReceiptTwice() {
        eventsStorage.saveEvent(EVENT_1);
        Assert.assertEquals(1, eventsStorage.getNumberOfEvents());
        final Uri saveResult = eventsStorage.saveEvent(EVENT_2);
        assertNotNull(saveResult);
        Assert.assertEquals(2, eventsStorage.getNumberOfEvents());
    }

    public void testSaveMessageReceiptAndRead() {
        eventsStorage.saveEvent(EVENT_1);
        final List<Uri> files1 = eventsStorage.getEventUris();
        assertEquals(1, files1.size());
        final Event fileContents = eventsStorage.readEvent(files1.get(0));
        assertEquals(EVENT_1, fileContents);
    }

    public void testReadNonExistentFile() {
        boolean exceptionThrown = false;
        try {
            final Event fileContents1 = eventsStorage.readEvent(NON_EXISTENT_FILE_1);
            assertEquals(null, fileContents1);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    public void testSetStatusNonExistentFile() {
        boolean exceptionThrown = false;
        try {
            eventsStorage.setEventStatus(NON_EXISTENT_FILE_1, Event.Status.POSTED);
        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    public void testSaveMessageReceiptAndDelete() {
        eventsStorage.saveEvent(EVENT_1);
        final List<Uri> files = eventsStorage.getEventUris();
        Assert.assertEquals(1, eventsStorage.getNumberOfEvents());
        eventsStorage.deleteEvents(files);
        Assert.assertEquals(0, eventsStorage.getNumberOfEvents());
    }

    public void testDeleteNonExistingFile() {
        eventsStorage.saveEvent(EVENT_1);
        Assert.assertEquals(1, eventsStorage.getNumberOfEvents());
        final List<Uri> bogusList = new LinkedList<Uri>();
        bogusList.add(NON_EXISTENT_FILE_1);
        eventsStorage.deleteEvents(bogusList);
        Assert.assertEquals(1, eventsStorage.getNumberOfEvents());
    }

    public void testDeleteOneOfTwoMessageReceiptFiles() {

        eventsStorage.saveEvent(EVENT_1);
        Assert.assertEquals(1, eventsStorage.getNumberOfEvents());

        eventsStorage.saveEvent(EVENT_2);
        Assert.assertEquals(2, eventsStorage.getNumberOfEvents());

        final List<Uri> files1 = eventsStorage.getEventUris();
        final List<Uri> filesRemaining = new LinkedList<Uri>();
        filesRemaining.add(files1.get(1));
        files1.remove(1);

        eventsStorage.deleteEvents(files1);
        Assert.assertEquals(1, eventsStorage.getNumberOfEvents());

        final List<Uri> files2 = eventsStorage.getEventUris();
        assertEquals(files1.size(), files2.size());
        assertEquals(1, files2.size());
        assertEquals(filesRemaining.get(0), files2.get(0));
    }

    public void testReset() {

        eventsStorage.saveEvent(EVENT_1);
        Assert.assertEquals(1, eventsStorage.getNumberOfEvents());

        eventsStorage.reset();
        Assert.assertEquals(0, eventsStorage.getNumberOfEvents());
    }

    public void testSetStatus() {

        final Uri uri1 = eventsStorage.saveEvent(EVENT_1);
        assertNotNull(uri1);
        assertEquals(Event.Status.NOT_POSTED, EVENT_1.getStatus());

        eventsStorage.setEventStatus(uri1, Event.Status.POSTING);
        Assert.assertEquals(Event.Status.POSTING, eventsStorage.readEvent(uri1).getStatus());

        eventsStorage.setEventStatus(uri1, Event.Status.POSTED);
        Assert.assertEquals(Event.Status.POSTED, eventsStorage.readEvent(uri1).getStatus());
    }

    public void testGetMessageReceiptEventUrisWithStatus() {
        EVENT_1.setStatus(Event.Status.POSTED);
        EVENT_2.setStatus(Event.Status.POSTING_ERROR);
        eventsStorage.saveEvent(EVENT_1);
        eventsStorage.saveEvent(EVENT_2);

        final List<Uri> uris1 = eventsStorage.getEventUrisWithStatus(Event.Status.NOT_POSTED);
        assertEquals(0, uris1.size());

        final List<Uri> uris2 = eventsStorage.getEventUrisWithStatus(Event.Status.POSTING);
        assertEquals(0, uris2.size());

        final List<Uri> uris3 = eventsStorage.getEventUrisWithStatus(Event.Status.POSTING_ERROR);
        assertEquals(1, uris3.size());
    }
}
