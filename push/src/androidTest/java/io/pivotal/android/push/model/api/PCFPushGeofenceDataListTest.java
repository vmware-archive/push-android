package io.pivotal.android.push.model.api;

import android.test.AndroidTestCase;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Iterator;

import io.pivotal.android.push.util.GsonUtil;
import io.pivotal.android.push.util.ModelUtil;

public class PCFPushGeofenceDataListTest extends AndroidTestCase {

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
        final TypeToken<PCFPushGeofenceDataList> typeToken = new TypeToken<PCFPushGeofenceDataList>(){};
        final PCFPushGeofenceDataList list = ModelUtil.getPCFPushGeofenceDataList(getContext(), "geofence_three_items.json");
        assertEquals(3, list.size());
        final Iterator<PCFPushGeofenceData> i = list.iterator();
        assertNotNull(i);
        assertTrue(i.hasNext());
        final PCFPushGeofenceData item1 = i.next();
        assertEquals(7L, item1.getId());
        assertEquals(7L, list.first().getId());
        assertTrue(i.hasNext());
        final PCFPushGeofenceData item2 = i.next();
        assertEquals(9L, item2.getId());
        assertTrue(i.hasNext());
        final PCFPushGeofenceData item3 = i.next();
        assertEquals(44L, item3.getId());
        assertFalse(i.hasNext());
    }
}
