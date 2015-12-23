package io.pivotal.android.push.database;

import android.net.Uri;
import android.test.AndroidTestCase;

import junit.framework.Assert;

import java.util.LinkedList;
import java.util.List;

import io.pivotal.android.push.model.analytics.DummyEvent;
import io.pivotal.android.push.model.analytics.AnalyticsEvent;

public class DatabaseAnalyticsEventsStorageTest extends AndroidTestCase {

    private static final String TEST_DEVICE_UUID_1 = "TEST-DEVICE-UUID-1";
    private static final String TEST_DEVICE_UUID_2 = "TEST-DEVICE-UUID-2";
    private static final String TEST_DEVICE_UUID_3 = "TEST-DEVICE-UUID-3";
    private static final Uri NON_EXISTENT_FILE_1 = Uri.withAppendedPath(Database.EVENTS_CONTENT_URI, "/999999");
    private DatabaseAnalyticsEventsStorage storage;
    private AnalyticsEvent EVENT_1;
    private AnalyticsEvent EVENT_2;
    private AnalyticsEvent EVENT_3;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        DatabaseWrapper.createDatabaseInstance(getContext());
        EVENT_1 = DummyEvent.getEvent(TEST_DEVICE_UUID_1);
        EVENT_2 = DummyEvent.getEvent(TEST_DEVICE_UUID_2);
        EVENT_3 = DummyEvent.getEvent(TEST_DEVICE_UUID_3);
        storage = new DatabaseAnalyticsEventsStorage();
        storage.reset();
    }

    public void testStartState() {
        assertNotNull(storage);
        final List<Uri> files1 = storage.getEventUris();
        assertTrue(files1 == null || files1.size() == 0);
        Assert.assertEquals(0, storage.getNumberOfEvents());
        assertFalse(EVENT_1.equals(EVENT_2));
    }

    public void testSaveMessageReceiptOnce() {
        final Uri saveResult = storage.saveEvent(EVENT_1);
        assertNotNull(saveResult);
        Assert.assertEquals(1, storage.getNumberOfEvents());
    }

    public void testSaveMessageReceiptTwice() {
        storage.saveEvent(EVENT_1);
        Assert.assertEquals(1, storage.getNumberOfEvents());
        final Uri saveResult = storage.saveEvent(EVENT_2);
        assertNotNull(saveResult);
        Assert.assertEquals(2, storage.getNumberOfEvents());
    }

    public void testSaveMessageReceiptAndRead() {
        storage.saveEvent(EVENT_1);
        final List<Uri> files1 = storage.getEventUris();
        assertEquals(1, files1.size());
        final AnalyticsEvent fileContents = storage.readEvent(files1.get(0));
        assertEquals(EVENT_1, fileContents);
    }

    public void testReadNonExistentFile() {
        boolean exceptionThrown = false;
        try {
            final AnalyticsEvent fileContents1 = storage.readEvent(NON_EXISTENT_FILE_1);
            assertEquals(null, fileContents1);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    public void testSetStatusNonExistentFile() {
        boolean exceptionThrown = false;
        try {
            storage.setEventStatus(NON_EXISTENT_FILE_1, AnalyticsEvent.Status.POSTED);
        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    public void testSaveMessageReceiptAndDelete() {
        storage.saveEvent(EVENT_1);
        final List<Uri> files = storage.getEventUris();
        Assert.assertEquals(1, storage.getNumberOfEvents());
        storage.deleteEvents(files);
        Assert.assertEquals(0, storage.getNumberOfEvents());
    }

    public void testDeleteNonExistingFile() {
        storage.saveEvent(EVENT_1);
        Assert.assertEquals(1, storage.getNumberOfEvents());
        final List<Uri> bogusList = new LinkedList<Uri>();
        bogusList.add(NON_EXISTENT_FILE_1);
        storage.deleteEvents(bogusList);
        Assert.assertEquals(1, storage.getNumberOfEvents());
    }

    public void testDeleteOneOfTwoMessageReceiptFiles() {

        storage.saveEvent(EVENT_1);
        Assert.assertEquals(1, storage.getNumberOfEvents());

        storage.saveEvent(EVENT_2);
        Assert.assertEquals(2, storage.getNumberOfEvents());

        final List<Uri> files1 = storage.getEventUris();
        final List<Uri> filesRemaining = new LinkedList<Uri>();
        filesRemaining.add(files1.get(1));
        files1.remove(1);

        storage.deleteEvents(files1);
        Assert.assertEquals(1, storage.getNumberOfEvents());

        final List<Uri> files2 = storage.getEventUris();
        assertEquals(files1.size(), files2.size());
        assertEquals(1, files2.size());
        assertEquals(filesRemaining.get(0), files2.get(0));
    }

    public void testReset() {

        storage.saveEvent(EVENT_1);
        Assert.assertEquals(1, storage.getNumberOfEvents());

        storage.reset();
        Assert.assertEquals(0, storage.getNumberOfEvents());
    }

    public void testSetStatus() {

        final Uri uri1 = storage.saveEvent(EVENT_1);
        assertNotNull(uri1);
        assertEquals(AnalyticsEvent.Status.NOT_POSTED, EVENT_1.getStatus());

        storage.setEventStatus(uri1, AnalyticsEvent.Status.POSTING);
        Assert.assertEquals(AnalyticsEvent.Status.POSTING, storage.readEvent(uri1).getStatus());

        storage.setEventStatus(uri1, AnalyticsEvent.Status.POSTED);
        Assert.assertEquals(AnalyticsEvent.Status.POSTED, storage.readEvent(uri1).getStatus());
    }

    public void testGetMessageReceiptEventUrisWithStatus() {
        EVENT_1.setStatus(AnalyticsEvent.Status.POSTED);
        EVENT_2.setStatus(AnalyticsEvent.Status.POSTING_ERROR);
        storage.saveEvent(EVENT_1);
        storage.saveEvent(EVENT_2);

        final List<Uri> uris1 = storage.getEventUrisWithStatus(AnalyticsEvent.Status.NOT_POSTED);
        assertEquals(0, uris1.size());

        final List<Uri> uris2 = storage.getEventUrisWithStatus(AnalyticsEvent.Status.POSTING);
        assertEquals(0, uris2.size());

        final List<Uri> uris3 = storage.getEventUrisWithStatus(AnalyticsEvent.Status.POSTING_ERROR);
        assertEquals(1, uris3.size());
    }

    public void testGetEventsWithType() {
        EVENT_1.setEventType("A");
        EVENT_2.setEventType("B");
        EVENT_3.setEventType("A");
        final Uri uri1 = storage.saveEvent(EVENT_1);
        final Uri uri2 = storage.saveEvent(EVENT_2);
        final Uri uri3 = storage.saveEvent(EVENT_3);

        final List<Uri> uris1 = storage.getEventUrisWithType("Blerg");
        assertEquals(0, uris1.size());

        final List<Uri> uris2 = storage.getEventUrisWithType("A");
        assertEquals(2, uris2.size());
        assertTrue(uri1.equals(uris2.get(0)) || uri1.equals(uris2.get(1)));
        assertTrue(uri3.equals(uris2.get(0)) || uri3.equals(uris2.get(1)));
        assertFalse(uris2.get(0).equals(uris2.get(1)));

        final List<Uri> uris3 = storage.getEventUrisWithType("B");
        assertEquals(1, uris3.size());
        assertEquals(uri2, uris3.get(0));
    }
}
