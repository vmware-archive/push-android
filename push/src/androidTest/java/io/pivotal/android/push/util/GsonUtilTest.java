package io.pivotal.android.push.util;

import android.test.AndroidTestCase;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import io.pivotal.android.push.model.api.PCFPushGeofenceData;
import io.pivotal.android.push.model.api.PCFPushGeofenceDataList;
import io.pivotal.android.push.model.api.PCFPushGeofenceLocation;

public class GsonUtilTest extends AndroidTestCase {

    public void testDeserializeAndSerializeEmptyLongSparseArray() throws Exception {
        // Deserialize
        final TypeToken<PCFPushGeofenceDataList> typeToken = new TypeToken<PCFPushGeofenceDataList>(){};
        final PCFPushGeofenceDataList array = GsonUtil.getGson().fromJson("[]", typeToken.getType());
        assertNotNull(array);
        assertEquals(0, array.size());

        // Serialize
        final String json = GsonUtil.getGson().toJson(array, typeToken.getType());
        assertEquals("[]", json);
    }

    public void testDeserializeAndSerializeNullLongSparseArray() throws Exception {
        // Deserialize
        final TypeToken<ArrayTestClass> typeToken = new TypeToken<ArrayTestClass>(){};
        final ArrayTestClass testClass = GsonUtil.getGson().fromJson("{\"array\":null}", typeToken.getType());
        assertNotNull(testClass);
        assertNull(testClass.array);

        // Serialize
        final String json = GsonUtil.getGsonAndSerializeNulls().toJson(testClass, typeToken.getType());
        assertEquals("{\"array\":null}", json);
    }

    public void testDeserializeAndSerializeLongSparseArrayWithOneItem() throws IOException {
        // Deserialize
        final TypeToken<PCFPushGeofenceDataList> typeToken = new TypeToken<PCFPushGeofenceDataList>(){};
        final PCFPushGeofenceDataList array = ModelUtil.getPCFPushGeofenceDataList(getContext(), "geofence_one_item.json");
        final PCFPushGeofenceData data = array.get(7L);
        final List<PCFPushGeofenceLocation> locations = data.getLocations();
        final PCFPushGeofenceLocation location = locations.get(0);
        assertNotNull(array);
        assertEquals(1, array.size());
        assertEquals(7L, data.getId());
        assertEquals(new Date(1142744274L), data.getExpiryTime());
        assertEquals("tacos", data.getData().get("message"));
        assertEquals(1, locations.size());
        assertEquals(66L, location.getId());
        assertEquals("robs_wizard_tacos", location.getName());
        assertEquals(53.5, location.getLatitude());
        assertEquals(-91.5, location.getLongitude());
        assertEquals(120.0, location.getRadius());

        // Serialize
        final String json = GsonUtil.getGson().toJson(array, typeToken.getType());
        assertTrue(json.contains("\"message\":\"tacos\""));
        assertTrue(json.contains("\"expiry_time\":1142744274"));
        assertTrue(json.contains("\"name\":\"robs_wizard_tacos\""));
        assertTrue(json.contains("\"id\":66"));
        assertTrue(json.contains("\"lat\":53.5"));
        assertTrue(json.contains("\"long\":-91.5"));
        assertTrue(json.contains("\"rad\":120.0"));
    }

    public void testDeserializeAndSerializeLongSparseArrayWithThreeItems() throws IOException {
        // Deserialize
        final TypeToken<PCFPushGeofenceDataList> typeToken = new TypeToken<PCFPushGeofenceDataList>(){};
        final PCFPushGeofenceDataList array = ModelUtil.getPCFPushGeofenceDataList(getContext(), "geofence_three_items.json");
        assertNotNull(array);
        assertEquals(3, array.size());
        assertNotNull(array.get(7L));
        assertEquals(1, array.get(7L).getLocations().size());
        assertNotNull(array.get(9L));
        assertEquals(1, array.get(9L).getLocations().size());
        assertNotNull(array.get(44L));
        assertEquals(2, array.get(44L).getLocations().size());

        // Serialize
        final String json = GsonUtil.getGson().toJson(array, typeToken.getType());
        assertTrue(json.contains("\"id\":7"));
        assertTrue(json.contains("\"id\":9"));
        assertTrue(json.contains("\"id\":44"));
    }

    public void testDeserializeDates() {
        assertEquals(null, deserializeDate("{}"));
        assertEquals(null, deserializeDate("{\"date\":null}"));
        assertEquals(0, deserializeDate("{\"date\":0}").getTime());
        assertEquals(1424309210305L, deserializeDate("{\"date\":1424309210305}").getTime());
    }

    public void testDeserializeBadDates() {
        try {
            deserializeDate("{\"date\":\"PANTS\"}");
            fail();
        } catch (Exception e) {}
    }

    public void testSerializeDates() {
        assertEquals("{}", serializeTestClass(new DateTestClass(null)));
        assertEquals("{\"date\":0}", serializeTestClass(new DateTestClass(new Date(0))));
        assertEquals("{\"date\":1424309210305}", serializeTestClass(new DateTestClass(new Date(1424309210305L))));
    }

    private String serializeTestClass(DateTestClass testClass) {
        return GsonUtil.getGson().toJson(testClass, DateTestClass.class);
    }

    private Date deserializeDate(String s) {
        return GsonUtil.getGson().fromJson(s, DateTestClass.class).date;
    }

    private static class DateTestClass {
        public Date date;

        public DateTestClass(Date date) {
            this.date = date;
        }
    }

    private static class ArrayTestClass {
        public PCFPushGeofenceDataList array;
    }
}
