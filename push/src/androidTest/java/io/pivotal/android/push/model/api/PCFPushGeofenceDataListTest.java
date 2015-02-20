package io.pivotal.android.push.model.api;

import android.test.AndroidTestCase;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Iterator;

import io.pivotal.android.push.util.GsonUtil;

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
        final PCFPushGeofenceDataList list = getJson("geofence_one_item.json", typeToken);
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
        final PCFPushGeofenceDataList list = getJson("geofence_three_items.json", typeToken);
        assertEquals(3, list.size());
        final Iterator<PCFPushGeofenceData> i = list.iterator();
        assertNotNull(i);
        assertTrue(i.hasNext());
        final PCFPushGeofenceData item1 = i.next();
        assertEquals(7L, item1.getId());
        assertEquals(7L, list.first().getId());
        assertTrue(i.hasNext());
        final PCFPushGeofenceData item2 = i.next();
        assertEquals(10L, item2.getId());
        assertTrue(i.hasNext());
        final PCFPushGeofenceData item3 = i.next();
        assertEquals(44L, item3.getId());
        assertFalse(i.hasNext());
    }

    private <T> T getJson(String filename, TypeToken<T> typeToken) throws IOException {
        InputStream is = null;
        try {
            is = getContext().getAssets().open(filename);
            final InputStreamReader reader = new InputStreamReader(is);
            final Gson gson = GsonUtil.getGson();
            return gson.fromJson(reader, typeToken.getType());
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
}
