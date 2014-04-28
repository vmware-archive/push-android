package com.pivotal.cf.mobile.pushsdk.model.events;

import android.content.ContentValues;
import android.os.Parcel;
import android.provider.BaseColumns;
import android.test.AndroidTestCase;
import android.test.MoreAsserts;

import com.google.gson.Gson;
import com.pivotal.cf.mobile.pushsdk.database.Database;
import com.pivotal.cf.mobile.pushsdk.database.FakeCursor;
import com.pivotal.cf.mobile.pushsdk.util.PushLibLogger;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

public class EventTest extends AndroidTestCase {

    private static final String TEST_EVENT_ID_1 = "EVENT-ID-1";
    private static final String TEST_MESSAGE_UUID_1 = "SOME-MESSAGE-UUID-1";
    private static final String TEST_EVENT_VARIANT_UUID_1 = "VARIANT-UUID-1";
    private static final String TEST_DEVICE_ID_1 = "DEVICE-ID-1";
    private static final String TEST_TIME_1 = "SOME BOGUS TIME";
    private static final int TEST_YEAR_1 = 1969;
    private static final int TEST_MONTH_1 = 6;
    private static final int TEST_DAY_1 = 20;
    private static final int TEST_HOUR_1 = 20;
    private static final int TEST_MINUTE_1 = 17;
    private static final int TEST_SECOND_1 = 40;
    private static final int TEST_ROW_ID_1 = 67;

    private static final String TEST_EVENT_ID_2 = "EVENT-ID-2";
    private static final String TEST_MESSAGE_UUID_2 = "ANOTHER-MESSAGE-UUID";
    private static final String TEST_EVENT_VARIANT_UUID_2 = "VARIANT-UUID-2";
    private static final String TEST_DEVICE_ID_2 = "DEVICE-ID-2";
    private static final int TEST_YEAR_2 = 1957;
    private static final int TEST_MONTH_2 = 9;
    private static final int TEST_DAY_2 = 4;
    private static final int TEST_HOUR_2 = 19;
    private static final int TEST_MINUTE_2 = 28;
    private static final int TEST_SECOND_2 = 34;
    private static final String SOME_OTHER_EVENT_TYPE = "SOME_OTHER_EVENT_TYPE";

    public void testEquals1() {
        final Event model1 = getEvent1();
        final Event model2 = getEvent1();
        assertEquals(model1, model2);
        assertEquals(model2, model1);
    }

    public void testNotEquals() {
        final Event model1 = getEvent1();
        final Event model2 = getEvent2();
        MoreAsserts.assertNotEqual(model1, model2);
        MoreAsserts.assertNotEqual(model2, model1);
    }

    public void testCopyConstructor1() {
        final Event model1 = new Event();
        final Event model2 = new Event(model1);
        assertEquals(model1, model2);
        assertFalse(model1 == model2);
    }

    public void testCopyConstructor2() {
        final Event model1 = getEvent1();
        final Event model2 = new Event(model1);
        assertEquals(model1, model2);
        assertFalse(model1 == model2);
    }

    public void testNotEqualsWithDates() {

        final Event model1 = new Event();
        model1.setTime(getTestDate1());

        final Event model2 = new Event();
        model2.setTime(getTestDate2());

        MoreAsserts.assertNotEqual(model1, model2);
        MoreAsserts.assertNotEqual(model2, model1);

        model1.setTime((String) null);
        MoreAsserts.assertNotEqual(model1, model2);
        MoreAsserts.assertNotEqual(model2, model1);

        model1.setTime((Date) null);
        MoreAsserts.assertNotEqual(model1, model2);
        MoreAsserts.assertNotEqual(model2, model1);

        model2.setTime((String) null);
        assertEquals(model1, model2);
    }

    public void testNotEqualsWithIds() {

        final Event model1 = new Event();
        model1.setEventId(TEST_EVENT_ID_1);

        final Event model2 = new Event();
        model2.setEventId(TEST_EVENT_ID_2);

        MoreAsserts.assertNotEqual(model1, model2);
        MoreAsserts.assertNotEqual(model2, model1);

        model1.setEventId(null);
        MoreAsserts.assertNotEqual(model1, model2);
        MoreAsserts.assertNotEqual(model2, model1);

        model2.setEventId(null);
        assertEquals(model1, model2);
    }

    public void testNotEqualsWithVariantUuids() {

        final Event model1 = new Event();
        model1.setVariantUuid(TEST_EVENT_VARIANT_UUID_1);

        final Event model2 = new Event();
        model2.setVariantUuid(TEST_EVENT_VARIANT_UUID_2);

        MoreAsserts.assertNotEqual(model1, model2);
        MoreAsserts.assertNotEqual(model2, model1);

        model1.setVariantUuid(null);
        MoreAsserts.assertNotEqual(model1, model2);
        MoreAsserts.assertNotEqual(model2, model1);

        model2.setVariantUuid(null);
        assertEquals(model1, model2);
    }

    public void testNotEqualsWithDeviceIds() {

        final Event model1 = new Event();
        model1.setDeviceId(TEST_DEVICE_ID_1);

        final Event model2 = new Event();
        model2.setDeviceId(TEST_DEVICE_ID_2);

        MoreAsserts.assertNotEqual(model1, model2);
        MoreAsserts.assertNotEqual(model2, model1);

        model1.setDeviceId(null);
        MoreAsserts.assertNotEqual(model1, model2);
        MoreAsserts.assertNotEqual(model2, model1);

        model2.setDeviceId(null);
        assertEquals(model1, model2);
    }

    public void testNotEqualsWithMessageUuids() {

        final Event model1 = new Event();
        model1.setData(getEventData1());

        final Event model2 = new Event();
        model2.setData(getEventData2());

        MoreAsserts.assertNotEqual(model1, model2);
        MoreAsserts.assertNotEqual(model2, model1);

        model1.setData(null);
        MoreAsserts.assertNotEqual(model1, model2);
        MoreAsserts.assertNotEqual(model2, model1);

        model2.setData(null);
        assertEquals(model1, model2);
    }

    public void testNotEqualsWithStatuses() {

        final Event model1 = new Event();
        model1.setStatus(Event.Status.POSTING_ERROR);

        final Event model2 = new Event();
        model1.setStatus(Event.Status.POSTING);

        MoreAsserts.assertNotEqual(model1, model2);
        MoreAsserts.assertNotEqual(model2, model1);
    }

    public void testNotEqualsNull() {
        final Event model1 = getEvent1();
        assertFalse(model1.equals(null));
    }

    public void testNotEqualsOtherObject() {
        final Event model1 = getEvent1();
        assertFalse(model1.equals("INTERLOPER STRING"));
    }

    public void testHashCode() {
        final Event model1 = getEvent1();
        final Event model2 = getEvent1();
        assertEquals(model1.hashCode(), model2.hashCode());
    }

    public void testToJson1() {
        final Event event = getEvent1();

        final Gson gson = new Gson();
        final String json = gson.toJson(event);
        assertTrue(json.contains("\"id\":"));
        assertTrue(json.contains("\"" + TEST_EVENT_ID_1 + "\""));
        assertTrue(json.contains("\"variant_uuid\":"));
        assertTrue(json.contains("\"" + TEST_EVENT_VARIANT_UUID_1 + "\""));
        assertTrue(json.contains("\"type\":\"event_push_received\""));
        assertTrue(json.contains("\"data\":"));
        assertTrue(json.contains("\"msg_uuid\":"));
        assertTrue(json.contains("\"" + TEST_MESSAGE_UUID_1 + "\""));
        assertTrue(json.contains("\"time\":"));
        assertTrue(json.contains("\"" + (getTestDate1().getTime() / 1000L) + "\""));
    }

    public void testToJson2() {
        final Event event = new Event();
        event.setData(getEventData3());
        final Gson gson = new Gson();
        final String json = gson.toJson(event);
        assertTrue(json.startsWith("{\"data\":{\"A LIST\":["));
        assertTrue(json.contains("\"ZEBRAS\""));
        assertTrue(json.contains("\"LEMURS\""));
        assertTrue(json.contains("\"SLOTHS\""));
        assertTrue(json.endsWith("]}}"));
    }

    public void testConvertStringToList() {
        final List<Event> list = Event.jsonStringToList(getTestListOfMessageReceipts());
        assertEquals(2, list.size());
        assertEquals(TEST_MESSAGE_UUID_1, list.get(0).getData().get(EventPushReceived.MESSAGE_UUID));
        assertEquals(TEST_MESSAGE_UUID_2, list.get(1).getData().get(EventPushReceived.MESSAGE_UUID));
    }

    public void testConvertStringToListEmpty1() {
        final List<Event> list = Event.jsonStringToList("");
        assertNull(list);
    }

    public void testConvertStringToListEmpty2() {
        final List<Event> list = Event.jsonStringToList("[]");
        assertEquals(0, list.size());
    }

    public void testConvertStringToListNull() {
        final List<Event> list = Event.jsonStringToList(null);
        assertNull(list);
    }

    public void testConvertListToString() {
        List<Event> list = new LinkedList<Event>();
        list.add(getEvent1());
        list.add(getEvent2());
        final String str = Event.listToJsonString(list);
        assertTrue(str.contains("\"data\":{\"msg_uuid\":"));
        assertTrue(str.contains("\"type\":\"" + EventPushReceived.EVENT_TYPE + "\""));
        assertTrue(str.contains("\"type\":\"" + SOME_OTHER_EVENT_TYPE + "\""));
        assertTrue(str.contains("\"" + TEST_MESSAGE_UUID_1 + "\""));
        assertTrue(str.contains("\"" + TEST_MESSAGE_UUID_2 + "\""));
    }

    public void testConvertListToStringEmpty() {
        List<Event> list = new LinkedList<Event>();
        final String str = Event.listToJsonString(list);
        assertEquals("[]", str);
    }

    public void testConvertListToStringNull() {
        final String str = Event.listToJsonString(null);
        assertNull(str);
    }

    public void testGetCreateTableSqlStatement() {
        final String sql = Event.getCreateTableSqlStatement();
        assertNotNull(sql);
        PushLibLogger.i("Create table statement: " + sql);
        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS"));
        assertTrue(sql.contains(Database.EVENTS_TABLE_NAME));
        assertTrue(sql.contains("'" + BaseColumns._ID + "'"));
        assertTrue(sql.contains("'" + Event.Columns.TYPE + "'"));
        assertTrue(sql.contains("'" + Event.Columns.EVENT_UUID + "'"));
        assertTrue(sql.contains("'" + Event.Columns.VARIANT_UUID + "'"));
        assertTrue(sql.contains("'" + Event.Columns.DEVICE_ID + "'"));
        assertTrue(sql.contains("'" + Event.Columns.TIME + "'"));
        assertTrue(sql.contains("'" + Event.Columns.DATA + "'"));
    }

    public void testGetDropTableSqlStatement() {
        final String sql = Event.getDropTableSqlStatement();
        assertNotNull(sql);
        PushLibLogger.i("Drop table statement: " + sql);
        assertTrue(sql.contains("DROP TABLE IF EXISTS"));
        assertTrue(sql.contains(Database.EVENTS_TABLE_NAME));
    }

    public void testGetContentValues1() {
        final Event event = getEvent1();
        final ContentValues cv = event.getContentValues();
        assertFalse(cv.containsKey(BaseColumns._ID));
        assertTrue(cv.containsKey(Event.Columns.TIME));
        assertTrue(cv.containsKey(Event.Columns.STATUS));
        assertTrue(cv.containsKey(Event.Columns.EVENT_UUID));
        assertTrue(cv.containsKey(Event.Columns.VARIANT_UUID));
        assertTrue(cv.containsKey(Event.Columns.DEVICE_ID));
        assertTrue(cv.containsKey(Event.Columns.TYPE));
        assertTrue(cv.containsKey(Event.Columns.DATA));
        assertEquals(event.getTime(), cv.getAsString(Event.Columns.TIME));
        assertEquals(event.getStatus(), cv.getAsInteger(Event.Columns.STATUS).intValue());
        assertEquals(event.getEventId(), cv.getAsString(Event.Columns.EVENT_UUID));
        assertEquals(event.getDeviceId(), cv.getAsString(Event.Columns.DEVICE_ID));
        assertEquals(event.getVariantUuid(), cv.getAsString(Event.Columns.VARIANT_UUID));
        assertEquals(event.getEventType(), cv.getAsString(Event.Columns.TYPE));
        assertNotNull(cv.getAsByteArray(Event.Columns.DATA));
    }

    public void testGetContentValues2() {
        final Event event = new Event();
        final ContentValues cv = event.getContentValues();
        assertFalse(cv.containsKey(BaseColumns._ID));
        assertTrue(cv.containsKey(Event.Columns.TIME));
        assertTrue(cv.containsKey(Event.Columns.STATUS));
        assertTrue(cv.containsKey(Event.Columns.EVENT_UUID));
        assertTrue(cv.containsKey(Event.Columns.VARIANT_UUID));
        assertTrue(cv.containsKey(Event.Columns.DEVICE_ID));
        assertTrue(cv.containsKey(Event.Columns.TYPE));
        assertTrue(cv.containsKey(Event.Columns.DATA));
        MoreAsserts.assertNotEqual(0, cv.getAsString(Event.Columns.TIME));
        assertEquals(Event.Status.NOT_POSTED, cv.getAsInteger(Event.Columns.STATUS).intValue());
        assertNull(cv.getAsString(Event.Columns.EVENT_UUID));
        assertNull(cv.getAsString(Event.Columns.VARIANT_UUID));
        assertNull(cv.getAsString(Event.Columns.DEVICE_ID));
        assertNull(cv.getAsByteArray(Event.Columns.DATA));
    }

    public void testConstructFromCursor1() {
        final FakeCursor cursor = new FakeCursor();
        cursor.addField(BaseColumns._ID, TEST_ROW_ID_1);
        cursor.addField(Event.Columns.TIME, TEST_TIME_1);
        cursor.addField(Event.Columns.STATUS, Event.Status.POSTED);
        cursor.addField(Event.Columns.EVENT_UUID, TEST_EVENT_ID_1);
        cursor.addField(Event.Columns.DEVICE_ID, TEST_DEVICE_ID_1);
        cursor.addField(Event.Columns.VARIANT_UUID, TEST_EVENT_VARIANT_UUID_1);
        cursor.addField(Event.Columns.TYPE, EventPushReceived.EVENT_TYPE);
        cursor.addField(Event.Columns.DATA, Event.serialize(getEventData1()));
        cursor.addField(EventPushReceived.MESSAGE_UUID, TEST_MESSAGE_UUID_1);
        final Event event = new Event(cursor);
        assertEquals(TEST_ROW_ID_1, event.getId());
        assertEquals(TEST_TIME_1, event.getTime());
        assertEquals(Event.Status.POSTED, event.getStatus());
        assertEquals(TEST_EVENT_ID_1, event.getEventId());
        assertEquals(TEST_EVENT_VARIANT_UUID_1, event.getVariantUuid());
        assertEquals(TEST_DEVICE_ID_1, event.getDeviceId());
        assertEquals(EventPushReceived.EVENT_TYPE, event.getEventType());
        assertNotNull(event.getData());
        assertEquals(TEST_MESSAGE_UUID_1, event.getData().get(EventPushReceived.MESSAGE_UUID));
    }

    public void testConstructFromCursor2() {
        final FakeCursor cursor = new FakeCursor();
        final Event event = new Event(cursor);
        assertEquals(0, event.getId());
        assertNull(event.getTime());
        assertEquals(Event.Status.NOT_POSTED, event.getStatus());
        assertNull(event.getEventId());
        assertNull(event.getVariantUuid());
        assertNull(event.getDeviceId());
        assertNull(event.getEventType());
        assertNull(event.getData());
    }

    public void testIsParcelable1() {
        final Event inputEvent = getEvent1();
        final Event outputEvent = getObjectViaParcel(inputEvent);
        assertNotNull(outputEvent);
        assertEquals(inputEvent, outputEvent);
    }

    public void testIsParcelable2() {
        final Event inputEvent = new Event();
        final Event outputEvent = getObjectViaParcel(inputEvent);
        assertNotNull(outputEvent);
        assertEquals(inputEvent, outputEvent);
    }

    public void testIsParcelable3() {
        final Event inputEvent = getEvent1();
        inputEvent.setData(getEventData3());
        final Event outputEvent = getObjectViaParcel(inputEvent);
        assertNotNull(outputEvent);
        assertEquals(inputEvent, outputEvent);
        final HashMap<String, Object> outputMap = outputEvent.getData();
        assertTrue(outputMap.containsKey("A LIST"));
        final List<String> outputList = (List<String>) outputMap.get("A LIST");
        assertEquals(3, outputList.size());
    }

    private Event getObjectViaParcel(Event inputEvent) {
        final Parcel inputParcel = Parcel.obtain();
        inputEvent.writeToParcel(inputParcel, 0);
        final byte[] bytes = inputParcel.marshall();
        assertNotNull(bytes);
        final Parcel outputParcel = Parcel.obtain();
        outputParcel.unmarshall(bytes, 0, bytes.length);
        outputParcel.setDataPosition(0);
        final Event outputEvent = Event.CREATOR.createFromParcel(outputParcel);
        inputParcel.recycle();
        outputParcel.recycle();
        return outputEvent;
    }

    public static String getTestListOfMessageReceipts() {
        final StringBuilder sb = new StringBuilder();
        sb.append("[{\"data\":{\"msg_uuid\":\"");
        sb.append(TEST_MESSAGE_UUID_1);
        sb.append("\"},\"time\":\"");
        sb.append(getTestDate1().getTime() / 1000L);
        sb.append("\"},{\"data\":{\"msg_uuid\":\"");
        sb.append(TEST_MESSAGE_UUID_2);
        sb.append("\"},\"time\":\"");
        sb.append(getTestDate2().getTime() / 1000L);
        sb.append("\"}]");
        return sb.toString();
    }

    public static Event getEvent1() {
        final Event event = new Event();
        event.setEventId(TEST_EVENT_ID_1);
        event.setVariantUuid(TEST_EVENT_VARIANT_UUID_1);
        event.setDeviceId(TEST_DEVICE_ID_1);
        event.setTime(getTestDate1());
        event.setData(getEventData1());
        event.setEventType(EventPushReceived.EVENT_TYPE);
        return event;
    }

    public static Event getEvent2() {
        final Event event = new Event();
        event.setEventId(TEST_EVENT_ID_2);
        event.setVariantUuid(TEST_EVENT_VARIANT_UUID_2);
        event.setDeviceId(TEST_DEVICE_ID_2);
        event.setTime(getTestDate1());
        event.setData(getEventData2());
        event.setEventType(SOME_OTHER_EVENT_TYPE);
        return event;
    }

    private static HashMap<String, Object> getEventData1() {
        final HashMap<String, Object> map = new HashMap<String, Object>();
        map.put(EventPushReceived.MESSAGE_UUID, TEST_MESSAGE_UUID_1);
        return map;
    }

    private static HashMap<String, Object> getEventData2() {
        final HashMap<String, Object> map = new HashMap<String, Object>();
        map.put(EventPushReceived.MESSAGE_UUID, TEST_MESSAGE_UUID_2);
        map.put("AND NOW", "FOR SOMETHING");
        map.put("COMPLETELY", "DIFFERENT");
        return map;
    }

    private static HashMap<String, Object> getEventData3() {
        final HashMap<String, Object> map = new HashMap<String, Object>();
        final List<String> list = new LinkedList<String>();
        list.add("ZEBRAS");
        list.add("LEMURS");
        list.add("SLOTHS");
        map.put("A LIST", list);
        return map;
    }

    private static Date getTestDate1() {
        final Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(TEST_YEAR_1, TEST_MONTH_1, TEST_DAY_1, TEST_HOUR_1, TEST_MINUTE_1, TEST_SECOND_1);
        return cal.getTime();
    }

    private static Date getTestDate2() {
        final Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(TEST_YEAR_2, TEST_MONTH_2, TEST_DAY_2, TEST_HOUR_2, TEST_MINUTE_2, TEST_SECOND_2);
        return cal.getTime();
    }
}
