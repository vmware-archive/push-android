package io.pivotal.android.push.model.analytics;

import android.test.AndroidTestCase;
import android.test.MoreAsserts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AnalyticsEventListTest extends AndroidTestCase {

    private static AnalyticsEvent EVENT1;
    private static AnalyticsEvent EVENT2;
    private static AnalyticsEvent EVENT3;

    static {
        EVENT1 = AnalyticsEventTest.getEvent1();
        EVENT2 = AnalyticsEventTest.getEvent2();
        EVENT3 = AnalyticsEventTest.getEvent3();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testHashCodeNulls() {
        final AnalyticsEventList list1 = new AnalyticsEventList();
        final AnalyticsEventList list2 = new AnalyticsEventList();
        assertEquals(list1.hashCode(), list2.hashCode());
    }

    public void testHashCodeNotNulls() {

        final AnalyticsEventList list1 = new AnalyticsEventList();
        list1.setEvents(getEventList(EVENT1, EVENT2, EVENT3));

        final AnalyticsEventList list2 = new AnalyticsEventList();
        list2.setEvents(getEventList(EVENT1, EVENT2, EVENT3));
        assertEquals(list1.hashCode(), list2.hashCode());
    }

    public void testEquals1() {
        final AnalyticsEventList list1 = new AnalyticsEventList();
        final AnalyticsEventList list2 = new AnalyticsEventList();
        assertEquals(list1, list2);
    }

    public void testEqualsWithEventLists() {
        final AnalyticsEventList list1 = new AnalyticsEventList();
        final AnalyticsEventList list2 = new AnalyticsEventList();
        assertEquals(list1, list2);
        list1.setEvents(getEventList(EVENT1, EVENT2, EVENT3));
        MoreAsserts.assertNotEqual(list1, list2);
        list2.setEvents(getEventList(EVENT1, EVENT2, EVENT3));
        assertEquals(list1, list2);
        list1.setEvents(null);
        MoreAsserts.assertNotEqual(list1, list2);
    }

    private static List<AnalyticsEvent> getEventList(AnalyticsEvent... events) {
        return new ArrayList<>(Arrays.asList(events));
    }
}
