package com.pivotal.cf.mobile.analyticssdk.model.events;

import android.test.AndroidTestCase;
import android.test.MoreAsserts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventListTest extends AndroidTestCase {

    private static final String TEST_DEVICE_ID_1 = "TEST-DEVICE-ID-1";
    private static final String TEST_DEVICE_ID_2 = "TEST-DEVICE-ID-1";

    private static Event EVENT1;
    private static Event EVENT2;
    private static Event EVENT3;

    static {
        EVENT1 = EventTest.getEvent1();
        EVENT2 = EventTest.getEvent2();
        EVENT3 = EventTest.getEvent3();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testHashCodeNulls() {
        final EventList list1 = new EventList();
        final EventList list2 = new EventList();
        assertEquals(list1.hashCode(), list2.hashCode());
    }

    public void testHashCodeNotNulls() {

        final EventList list1 = new EventList();
        list1.setDeviceId(TEST_DEVICE_ID_1);
        list1.setEvents(getEventList(EVENT1, EVENT2, EVENT3));

        final EventList list2 = new EventList();
        list2.setDeviceId(TEST_DEVICE_ID_1);
        list2.setEvents(getEventList(EVENT1, EVENT2, EVENT3));
        assertEquals(list1.hashCode(), list2.hashCode());
    }

    public void testEquals1() {
        final EventList list1 = new EventList();
        final EventList list2 = new EventList();
        assertEquals(list1, list2);
    }

    public void testEqualsWithDeviceIds() {
        final EventList list1 = new EventList();
        final EventList list2 = new EventList();
        assertEquals(list1, list2);
        list1.setDeviceId(TEST_DEVICE_ID_1);
        MoreAsserts.assertNotEqual(list1, list2);
        list2.setDeviceId(TEST_DEVICE_ID_2);
        assertEquals(list1, list2);
        list1.setDeviceId(null);
        MoreAsserts.assertNotEqual(list1, list2);
    }

    public void testEqualsWithEventLists() {
        final EventList list1 = new EventList();
        final EventList list2 = new EventList();
        assertEquals(list1, list2);
        list1.setEvents(getEventList(EVENT1, EVENT2, EVENT3));
        MoreAsserts.assertNotEqual(list1, list2);
        list2.setEvents(getEventList(EVENT1, EVENT2, EVENT3));
        assertEquals(list1, list2);
        list1.setEvents(null);
        MoreAsserts.assertNotEqual(list1, list2);
    }

    private static List<Event> getEventList(Event... events) {
        return new ArrayList<Event>(Arrays.asList(events));
    }
}
