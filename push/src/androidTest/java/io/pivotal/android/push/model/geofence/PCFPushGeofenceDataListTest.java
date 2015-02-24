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
        final PCFPushGeofenceDataList populatedList1 = ModelUtil.getPCFPushGeofenceDataList(getContext(), "geofence_three_items.json");
        final PCFPushGeofenceDataList populatedList2 = ModelUtil.getPCFPushGeofenceDataList(getContext(), "geofence_one_item.json");
        final boolean changed1 = model.addAll(populatedList1);
        final boolean changed2 = model.addAll(populatedList2);
        assertTrue(changed1);
        assertTrue(changed2);
        assertEquals(3, model.size());
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
        final PCFPushGeofenceDataList list = ModelUtil.getPCFPushGeofenceDataList(getContext(), "geofence_one_item.json");
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
        final PCFPushGeofenceDataList list = ModelUtil.getPCFPushGeofenceDataList(getContext(), "geofence_three_items.json");
        assertEquals(3, list.size());

        final Iterator<PCFPushGeofenceData> i = list.iterator();
        assertNotNull(i);
        assertTrue(i.hasNext());

        final PCFPushGeofenceData item1 = i.next();
        assertEquals(7L, item1.getId());
        assertEquals(7L, list.first().getId());
        assertEquals(PCFPushGeofenceData.TriggerType.ENTER, item1.getTriggerType());
        assertTrue(i.hasNext());

        final PCFPushGeofenceData item2 = i.next();
        assertEquals(9L, item2.getId());
        assertEquals(PCFPushGeofenceData.TriggerType.ENTER_OR_EXIT, item2.getTriggerType());
        assertTrue(i.hasNext());

        final PCFPushGeofenceData item3 = i.next();
        assertEquals(44L, item3.getId());
        assertEquals(PCFPushGeofenceData.TriggerType.EXIT, item3.getTriggerType());
        assertFalse(i.hasNext());
    }

    public void testAssertEquals() throws IOException {
        final PCFPushGeofenceDataList populatedList1 = ModelUtil.getPCFPushGeofenceDataList(getContext(), "geofence_three_items.json");
        final PCFPushGeofenceDataList populatedList2 = ModelUtil.getPCFPushGeofenceDataList(getContext(), "geofence_one_item.json");
        final PCFPushGeofenceDataList populatedList3 = ModelUtil.getPCFPushGeofenceDataList(getContext(), "geofence_three_items.json");
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
