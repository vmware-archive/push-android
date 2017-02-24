package io.pivotal.android.push.model.geofence;

import android.test.AndroidTestCase;
import android.test.MoreAsserts;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Iterator;

import io.pivotal.android.push.util.GsonUtil;
import io.pivotal.android.push.util.ModelUtil;

public class PCFPushGeofenceDataListTest extends AndroidTestCase {

    private PCFPushGeofenceDataList model = new PCFPushGeofenceDataList();

    public void testAddAllNullList() {
        final boolean changed = model.addAll(null);
        assertFalse(changed);
        assertEquals(0, model.size());
    }

    public void testAddAllEmptyList() {
        final PCFPushGeofenceDataList emptyList = new PCFPushGeofenceDataList();
        final boolean changed = model.addAll(emptyList);
        assertFalse(changed);
        assertEquals(0, model.size());
    }

    public void testAddAllPopulatedLists() throws IOException {
        final PCFPushGeofenceDataList populatedList1 = ModelUtil.getPCFPushGeofenceDataList(getContext(),
            "geofence_three_items.json");
        final PCFPushGeofenceDataList populatedList2 = ModelUtil.getPCFPushGeofenceDataList(getContext(),
            "geofence_one_item.json");
        final boolean changed1 = model.addAll(populatedList1);
        final boolean changed2 = model.addAll(populatedList2);
        assertTrue(changed1);
        assertTrue(changed2);
        assertEquals(3, model.size());
    }

    public void testFilteredAddAllNullList() {
        final boolean changed = model.addFiltered(null, new PCFPushGeofenceDataList.Filter() {
            @Override
            public boolean filterItem(PCFPushGeofenceData item) {
                return true;
            }
        });
        assertFalse(changed);
        assertEquals(0, model.size());
    }

    public void testFilteredAddAllEmptyList() {
        final PCFPushGeofenceDataList emptyList = new PCFPushGeofenceDataList();
        final boolean changed = model.addFiltered(emptyList, new PCFPushGeofenceDataList.Filter() {
            @Override
            public boolean filterItem(PCFPushGeofenceData item) {
                return true;
            }
        });
        assertFalse(changed);
        assertEquals(0, model.size());
    }

    public void testFilteredAddAllNullFilter() throws IOException {
        final PCFPushGeofenceDataList populatedList = ModelUtil.getPCFPushGeofenceDataList(getContext(),
            "geofence_three_items.json");
        final boolean changed = model.addFiltered(populatedList, null);
        assertFalse(changed);
        assertEquals(0, model.size());
    }

    public void testFilteredAddAllWithFilterThatAddsEverything() throws IOException {
        final PCFPushGeofenceDataList populatedList = ModelUtil.getPCFPushGeofenceDataList(getContext(),
            "geofence_three_items.json");
        final boolean changed = model.addFiltered(populatedList, new PCFPushGeofenceDataList.Filter() {
            @Override
            public boolean filterItem(PCFPushGeofenceData item) {
                return true;
            }
        });
        assertTrue(changed);
        assertEquals(3, model.size());
    }

    public void testFilteredAddAllWithFilterThatAddsNothing() throws IOException {
        final PCFPushGeofenceDataList populatedList = ModelUtil.getPCFPushGeofenceDataList(getContext(),
            "geofence_three_items.json");
        final boolean changed = model.addFiltered(populatedList, new PCFPushGeofenceDataList.Filter() {
            @Override
            public boolean filterItem(PCFPushGeofenceData item) {
                return false;
            }
        });
        assertFalse(changed);
        assertEquals(0, model.size());
    }

    public void testFilteredAddAllWithFilterThatItemsWithOddIds() throws IOException {
        final PCFPushGeofenceDataList populatedList = ModelUtil.getPCFPushGeofenceDataList(getContext(),
            "geofence_three_items.json");
        final boolean changed = model.addFiltered(populatedList, new PCFPushGeofenceDataList.Filter() {
            @Override
            public boolean filterItem(PCFPushGeofenceData item) {
                return item.getId() % 2 == 1;
            }
        });
        assertTrue(changed);
        assertEquals(2, model.size());
    }

    public void testIterateEmptyList() {
        final Type type = new TypeToken<PCFPushGeofenceDataList>(){}.getType();
        final PCFPushGeofenceDataList list = GsonUtil.getGson().fromJson("[]", type);
        assertEquals(0, list.size());
        final Iterator<PCFPushGeofenceData> i = list.iterator();
        assertNotNull(i);
        assertFalse(i.hasNext());
        assertNull(list.first());
    }

    public void testIterateOneItemList() throws IOException {
        final TypeToken<PCFPushGeofenceDataList> typeToken = new TypeToken<PCFPushGeofenceDataList>(){};
        final PCFPushGeofenceDataList list = ModelUtil.getPCFPushGeofenceDataList(getContext(),
            "geofence_one_item.json");
        assertEquals(1, list.size());
        final Iterator<PCFPushGeofenceData> i = list.iterator();
        assertNotNull(i);
        assertTrue(i.hasNext());
        final PCFPushGeofenceData item = i.next();
        assertEquals(7L, item.getId());
        assertEquals(7L, list.first().getId());
        assertFalse(i.hasNext());
    }

    public void testIterateThreeItemList() throws IOException {
        final PCFPushGeofenceDataList list = ModelUtil.getPCFPushGeofenceDataList(getContext(),
            "geofence_three_items.json");
        assertEquals(3, list.size());

        final Iterator<PCFPushGeofenceData> i = list.iterator();
        assertNotNull(i);
        assertTrue(i.hasNext());

        final PCFPushGeofenceData item1 = i.next();
        assertEquals(7L, item1.getId());
        assertEquals(7L, list.first().getId());
        assertEquals("enter", item1.getTriggerType());
        assertTrue(i.hasNext());

        final PCFPushGeofenceData item2 = i.next();
        assertEquals(9L, item2.getId());
        assertEquals("exit", item2.getTriggerType());
        assertTrue(i.hasNext());

        final PCFPushGeofenceData item3 = i.next();
        assertEquals(44L, item3.getId());
        assertEquals("exit", item3.getTriggerType());
        assertFalse(i.hasNext());
    }

    public void testRemoveNullLocation() throws IOException {
        final PCFPushGeofenceDataList list = ModelUtil.getPCFPushGeofenceDataList(getContext(),
            "geofence_three_items.json");
        assertEquals(3, list.size());
        list.removeLocation(null, null);
        assertEquals(3, list.size());
    }

    public void testRemoveLocationFromItemWithTwoLocations() throws IOException {
        final PCFPushGeofenceDataList list = ModelUtil.getPCFPushGeofenceDataList(getContext(),
            "geofence_three_items.json");

        final PCFPushGeofenceData item1 = list.get(44L);
        final PCFPushGeofenceLocation location1 = item1.getLocations().get(0);
        assertEquals(3, list.size());
        assertNotNull(location1);
        list.removeLocation(item1, location1);
        assertEquals(3, list.size());
        assertNotNull(list.get(7L));
        assertNotNull(list.get(9L));
        assertNotNull(list.get(44L));
        assertEquals(1, list.get(44L).getLocations().size());
        assertEquals(1, list.get(7L).getLocations().size());
        assertEquals(1, list.get(9L).getLocations().size());
        assertEquals(1, list.get(44L).getLocations().size());

        final PCFPushGeofenceLocation location2 = item1.getLocations().get(0);
        assertNotNull(location2);
        list.removeLocation(item1, location2);
        assertEquals(2, list.size());
        assertNotNull(list.get(7L));
        assertNotNull(list.get(9L));
        assertNull(list.get(44L));
        assertEquals(1, list.get(7L).getLocations().size());
        assertEquals(1, list.get(9L).getLocations().size());
    }

    public void testRemoveLocationFromItemWithOnlyOneLocation() throws IOException {
        final PCFPushGeofenceDataList list = ModelUtil.getPCFPushGeofenceDataList(getContext(),
            "geofence_three_items.json");
        final PCFPushGeofenceData item = list.get(7L);
        final PCFPushGeofenceLocation location = item.getLocations().get(0);
        assertEquals(3, list.size());
        assertNotNull(location);
        list.removeLocation(item, location);
        assertEquals(2, list.size());
        assertNull(list.get(7L));
        assertNotNull(list.get(9L));
        assertNotNull(list.get(44L));
        assertEquals(1, list.get(9L).getLocations().size());
        assertEquals(2, list.get(44L).getLocations().size());
    }

    public void testRemoveInvalidLocation() throws IOException {
        final PCFPushGeofenceDataList list = ModelUtil.getPCFPushGeofenceDataList(getContext(),
            "geofence_three_items.json");
        final PCFPushGeofenceData item = list.get(7L);
        final PCFPushGeofenceLocation location = list.get(9L).getLocations().get(0);
        assertEquals(3, list.size());
        assertNotNull(location);
        list.removeLocation(item, location);
        assertEquals(3, list.size());
        assertNotNull(list.get(7L));
        assertNotNull(list.get(9L));
        assertNotNull(list.get(44L));
        assertEquals(1, list.get(7L).getLocations().size());
        assertEquals(1, list.get(9L).getLocations().size());
        assertEquals(2, list.get(44L).getLocations().size());
    }

    public void testAssertEquals() throws IOException {
        final PCFPushGeofenceDataList populatedList1 = ModelUtil.getPCFPushGeofenceDataList(getContext(),
            "geofence_three_items.json");
        final PCFPushGeofenceDataList populatedList2 = ModelUtil.getPCFPushGeofenceDataList(getContext(),
            "geofence_one_item.json");
        final PCFPushGeofenceDataList populatedList3 = ModelUtil.getPCFPushGeofenceDataList(getContext(),
            "geofence_three_items.json");
        populatedList3.get(44L).getLocations().clear();
        assertEquals(model, model);
        MoreAsserts.assertNotEqual(model, null);
        MoreAsserts.assertNotEqual(null, model);
        MoreAsserts.assertNotEqual(model, "DECOY");
        MoreAsserts.assertNotEqual("DECOY", model);
        assertEquals(populatedList1, populatedList1);
        assertEquals(populatedList2, populatedList2);
        assertEquals(populatedList3, populatedList3);
        MoreAsserts.assertNotEqual(populatedList1, populatedList2);
        MoreAsserts.assertNotEqual(populatedList2, populatedList1);
        MoreAsserts.assertNotEqual(populatedList3, populatedList1);
        MoreAsserts.assertNotEqual(populatedList1, populatedList3);
    }
}
