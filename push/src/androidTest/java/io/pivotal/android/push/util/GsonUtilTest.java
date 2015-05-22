package io.pivotal.android.push.util;

import android.test.AndroidTestCase;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

import io.pivotal.android.push.model.geofence.PCFPushGeofenceData;
import io.pivotal.android.push.model.geofence.PCFPushGeofenceDataList;
import io.pivotal.android.push.model.geofence.PCFPushGeofenceLocation;

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
        assertEquals(120.0f, location.getRadius());

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

        final PCFPushGeofenceData item1 = array.get(7L);
        assertNotNull(item1);
        assertEquals(PCFPushGeofenceData.TriggerType.ENTER, item1.getTriggerType());
        assertEquals(1, item1.getLocations().size());

        final PCFPushGeofenceData item2 = array.get(9L);
        assertNotNull(item2);
        assertEquals(PCFPushGeofenceData.TriggerType.EXIT, item2.getTriggerType());
        assertEquals(1, item2.getLocations().size());

        final PCFPushGeofenceData item3 = array.get(44L);
        assertNotNull(item3);
        assertEquals(PCFPushGeofenceData.TriggerType.EXIT, item3.getTriggerType());
        assertEquals(2, item3.getLocations().size());

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

    public void testDeserializeTriggerTypes() {
        assertEquals(null, deserializeTriggerType(null));
        assertEquals(PCFPushGeofenceData.TriggerType.ENTER, deserializeTriggerType("enter"));
        assertEquals(PCFPushGeofenceData.TriggerType.EXIT, deserializeTriggerType("exit"));
    }

    public void testDeserializeBadTriggerTypes() {
        try {
            GsonUtil.getGsonAndSerializeNulls().fromJson("{\"trigger_type\":\"PANTS\"}", TriggerTypeTestClass.class);
            fail();
        } catch (Exception e) {}
        try {
            GsonUtil.getGsonAndSerializeNulls().fromJson("{\"trigger_type\":1337}", TriggerTypeTestClass.class);
            fail();
        } catch (Exception e) {}
    }

    public void testSerializeTriggerTypes() {
        assertEquals("{\"trigger_type\":null}", serializeTriggerType(null));
        assertEquals("{\"trigger_type\":\"enter\"}", serializeTriggerType(PCFPushGeofenceData.TriggerType.ENTER));
        assertEquals("{\"trigger_type\":\"exit\"}", serializeTriggerType(PCFPushGeofenceData.TriggerType.EXIT));
    }

    private String serializeTestClass(DateTestClass testClass) {
        return GsonUtil.getGson().toJson(testClass, DateTestClass.class);
    }

    private Date deserializeDate(String s) {
        return GsonUtil.getGson().fromJson(s, DateTestClass.class).date;
    }

    private PCFPushGeofenceData.TriggerType deserializeTriggerType(String triggerType) {
        final String json;
        if (triggerType == null) {
            json = "{\"trigger_type\":null}";
        } else {
            json = "{\"trigger_type\":\"" + triggerType + "\"}";
        }
        return GsonUtil.getGsonAndSerializeNulls().fromJson(json, TriggerTypeTestClass.class).triggerType;
    }

    private String serializeTriggerType(PCFPushGeofenceData.TriggerType triggerType) {
        final Type type = new TypeToken<TriggerTypeTestClass>(){}.getType();
        final TriggerTypeTestClass testClass = new TriggerTypeTestClass();
        testClass.triggerType = triggerType;
        return GsonUtil.getGsonAndSerializeNulls().toJson(testClass, type);
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

    private static class TriggerTypeTestClass {

        @SerializedName("trigger_type")
        public PCFPushGeofenceData.TriggerType triggerType;
    }
}
