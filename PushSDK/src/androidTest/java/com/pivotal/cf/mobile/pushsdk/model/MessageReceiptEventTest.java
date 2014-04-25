package com.pivotal.cf.mobile.pushsdk.model;

import android.content.ContentValues;
import android.os.Parcel;
import android.provider.BaseColumns;
import android.test.AndroidTestCase;
import android.test.MoreAsserts;

import com.google.gson.Gson;

import com.pivotal.cf.mobile.pushsdk.database.DatabaseConstants;
import com.pivotal.cf.mobile.pushsdk.database.FakeCursor;
import com.pivotal.cf.mobile.pushsdk.util.PushLibLogger;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

public class MessageReceiptEventTest extends AndroidTestCase {

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

    public void testEquals1() {
        final MessageReceiptEvent model1 = getMessageReceiptEvent1();
        final MessageReceiptEvent model2 = getMessageReceiptEvent1();
        assertEquals(model1, model2);
        assertEquals(model2, model1);
    }

    public void testNotEquals() {
        final MessageReceiptEvent model1 = getMessageReceiptEvent1();
        final MessageReceiptEvent model2 = getMessageReceiptEvent2();
        MoreAsserts.assertNotEqual(model1, model2);
        MoreAsserts.assertNotEqual(model2, model1);
    }

    public void testCopyConstructor1() {
        final MessageReceiptEvent model1 = new MessageReceiptEvent();
        final MessageReceiptEvent model2 = new MessageReceiptEvent(model1);
        assertEquals(model1, model2);
        assertFalse(model1 == model2);
    }

    public void testCopyConstructor2() {
        final MessageReceiptEvent model1 = getMessageReceiptEvent1();
        final MessageReceiptEvent model2 = new MessageReceiptEvent(model1);
        assertEquals(model1, model2);
        assertFalse(model1 == model2);
    }

    public void testNotEqualsWithDates() {

        final MessageReceiptEvent model1 = new MessageReceiptEvent();
        model1.setTime(getTestDate1());

        final MessageReceiptEvent model2 = new MessageReceiptEvent();
        model2.setTime(getTestDate2());

        MoreAsserts.assertNotEqual(model1, model2);
        MoreAsserts.assertNotEqual(model2, model1);

        model1.setTime((String)null);
        MoreAsserts.assertNotEqual(model1, model2);
        MoreAsserts.assertNotEqual(model2, model1);

        model1.setTime((Date)null);
        MoreAsserts.assertNotEqual(model1, model2);
        MoreAsserts.assertNotEqual(model2, model1);

        model2.setTime((String)null);
        assertEquals(model1, model2);
    }

    public void testNotEqualsWithIds() {

        final MessageReceiptEvent model1 = new MessageReceiptEvent();
        model1.setEventId(TEST_EVENT_ID_1);

        final MessageReceiptEvent model2 = new MessageReceiptEvent();
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

        final MessageReceiptEvent model1 = new MessageReceiptEvent();
        model1.setVariantUuid(TEST_EVENT_VARIANT_UUID_1);

        final MessageReceiptEvent model2 = new MessageReceiptEvent();
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

        final MessageReceiptEvent model1 = new MessageReceiptEvent();
        model1.setDeviceId(TEST_DEVICE_ID_1);

        final MessageReceiptEvent model2 = new MessageReceiptEvent();
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

        final MessageReceiptEvent model1 = new MessageReceiptEvent();
        model1.setData(new MessageReceiptData());
        model1.getData().setMessageUuid(TEST_MESSAGE_UUID_1);

        final MessageReceiptEvent model2 = new MessageReceiptEvent();
        model2.setData(new MessageReceiptData());
        model2.getData().setMessageUuid(TEST_MESSAGE_UUID_2);

        MoreAsserts.assertNotEqual(model1, model2);
        MoreAsserts.assertNotEqual(model2, model1);

        model1.getData().setMessageUuid(null);
        MoreAsserts.assertNotEqual(model1, model2);
        MoreAsserts.assertNotEqual(model2, model1);

        model2.getData().setMessageUuid(null);
        assertEquals(model1, model2);
    }

    public void testNotEqualsWithStatuses() {

        final MessageReceiptEvent model1 = new MessageReceiptEvent();
        model1.setStatus(BaseEvent.Status.POSTING_ERROR);

        final MessageReceiptEvent model2 = new MessageReceiptEvent();
        model1.setStatus(BaseEvent.Status.POSTING);

        MoreAsserts.assertNotEqual(model1, model2);
        MoreAsserts.assertNotEqual(model2, model1);
    }

    public void testNotEqualsNull() {
        final MessageReceiptEvent model1 = getMessageReceiptEvent1();
        assertFalse(model1.equals(null));
    }

    public void testNotEqualsOtherObject() {
        final MessageReceiptEvent model1 = getMessageReceiptEvent1();
        assertFalse(model1.equals("INTERLOPER STRING"));
    }

    public void testHashCode() {
        final MessageReceiptEvent model1 = getMessageReceiptEvent1();
        final MessageReceiptEvent model2 = getMessageReceiptEvent1();
        assertEquals(model1.hashCode(), model2.hashCode());
    }

    public void testToJson() {
        final MessageReceiptEvent model = getMessageReceiptEvent1();

        final Gson gson = new Gson();
        final String json = gson.toJson(model);
        assertTrue(json.contains("\"id\":"));
        assertTrue(json.contains("\"" + TEST_EVENT_ID_1 + "\""));
        assertTrue(json.contains("\"variant_uuid\":"));
        assertTrue(json.contains("\"" + TEST_EVENT_VARIANT_UUID_1 + "\""));
        assertTrue(json.contains("\"type\":\"event_push_received\""));
        assertTrue(json.contains("\"data\":"));
        assertTrue(json.contains("\"msg_uuid\":"));
        assertTrue(json.contains("\"" + TEST_MESSAGE_UUID_1 + "\""));
        assertTrue(json.contains("\"time\":"));
        assertTrue(json.contains("\"" +(getTestDate1().getTime() / 1000L) + "\""));
    }

    public void testConvertStringToList() {
        final List<MessageReceiptEvent> list = MessageReceiptEvent.jsonStringToList(getTestListOfMessageReceipts());
        assertEquals(2, list.size());
        assertEquals(TEST_MESSAGE_UUID_1, list.get(0).getData().getMessageUuid());
        assertEquals(TEST_MESSAGE_UUID_2, list.get(1).getData().getMessageUuid());
    }

    public void testConvertStringToListEmpty1() {
        final List<MessageReceiptEvent> list = MessageReceiptEvent.jsonStringToList("");
        assertNull(list);
    }

    public void testConvertStringToListEmpty2() {
        final List<MessageReceiptEvent> list = MessageReceiptEvent.jsonStringToList("[]");
        assertEquals(0, list.size());
    }

    public void testConvertStringToListNull() {
        final List<MessageReceiptEvent> list = MessageReceiptEvent.jsonStringToList(null);
        assertNull(list);
    }

    public void testConvertListToString() {
        List<MessageReceiptEvent> list = new LinkedList<MessageReceiptEvent>();
        list.add(getMessageReceiptEvent1());
        list.add(getMessageReceiptEvent2());
        final String str = MessageReceiptEvent.listToJsonString(list);
        assertTrue(str.contains("\"data\":{\"msg_uuid\":"));
        assertTrue(str.contains("\"type\":\"event_push_received\""));
        assertTrue(str.contains("\"" + TEST_MESSAGE_UUID_1 + "\"}"));
        assertTrue(str.contains("\"" + TEST_MESSAGE_UUID_2 + "\"}"));
    }

    public void testConvertListToStringEmpty() {
        List<MessageReceiptEvent> list = new LinkedList<MessageReceiptEvent>();
        final String str = MessageReceiptEvent.listToJsonString(list);
        assertEquals("[]", str);
    }

    public void testConvertListToStringNull() {
        final String str = MessageReceiptEvent.listToJsonString(null);
        assertNull(str);
    }

    public void testGetCreateTableSqlStatement() {
        final String sql = MessageReceiptEvent.getCreateTableSqlStatement();
        assertNotNull(sql);
        PushLibLogger.i("Create table statement: " + sql);
        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS"));
        assertTrue(sql.contains(DatabaseConstants.MESSAGE_RECEIPTS_TABLE_NAME));
        assertTrue(sql.contains("'" + BaseColumns._ID + "'"));
        assertTrue(sql.contains("'" + BaseEvent.Columns.EVENT_UUID + "'"));
        assertTrue(sql.contains("'" + BaseEvent.Columns.VARIANT_UUID + "'"));
        assertTrue(sql.contains("'" + BaseEvent.Columns.DEVICE_ID + "'"));
        assertTrue(sql.contains("'" + BaseEvent.Columns.TIME + "'"));
        assertTrue(sql.contains("'" + MessageReceiptData.Columns.MESSAGE_UUID + "'"));
    }

    public void testGetDropTableSqlStatement() {
        final String sql = MessageReceiptEvent.getDropTableSqlStatement();
        assertNotNull(sql);
        PushLibLogger.i("Drop table statement: " + sql);
        assertTrue(sql.contains("DROP TABLE IF EXISTS"));
        assertTrue(sql.contains(DatabaseConstants.MESSAGE_RECEIPTS_TABLE_NAME));
    }

    public void testGetContentValues1() {
        final MessageReceiptEvent event = getMessageReceiptEvent1();
        final ContentValues cv = event.getContentValues();
        assertFalse(cv.containsKey(BaseColumns._ID));
        assertTrue(cv.containsKey(BaseEvent.Columns.TIME));
        assertTrue(cv.containsKey(BaseEvent.Columns.STATUS));
        assertTrue(cv.containsKey(BaseEvent.Columns.EVENT_UUID));
        assertTrue(cv.containsKey(BaseEvent.Columns.VARIANT_UUID));
        assertTrue(cv.containsKey(BaseEvent.Columns.DEVICE_ID));
        assertTrue(cv.containsKey(MessageReceiptData.Columns.MESSAGE_UUID));
        assertEquals(event.getTime(), cv.getAsString(BaseEvent.Columns.TIME));
        assertEquals(event.getStatus(), cv.getAsInteger(BaseEvent.Columns.STATUS).intValue());
        assertEquals(event.getEventId(), cv.getAsString(BaseEvent.Columns.EVENT_UUID));
        assertEquals(event.getDeviceId(), cv.getAsString(BaseEvent.Columns.DEVICE_ID));
        assertEquals(event.getVariantUuid(), cv.getAsString(BaseEvent.Columns.VARIANT_UUID));
        assertEquals(event.getData().getMessageUuid(), cv.getAsString(MessageReceiptData.Columns.MESSAGE_UUID));
    }

    public void testGetContentValues2() {
        final MessageReceiptEvent event = new MessageReceiptEvent();
        final ContentValues cv = event.getContentValues();
        assertFalse(cv.containsKey(BaseColumns._ID));
        assertTrue(cv.containsKey(BaseEvent.Columns.TIME));
        assertTrue(cv.containsKey(BaseEvent.Columns.STATUS));
        assertTrue(cv.containsKey(BaseEvent.Columns.EVENT_UUID));
        assertTrue(cv.containsKey(BaseEvent.Columns.VARIANT_UUID));
        assertTrue(cv.containsKey(BaseEvent.Columns.DEVICE_ID));
        assertTrue(cv.containsKey(MessageReceiptData.Columns.MESSAGE_UUID));
        MoreAsserts.assertNotEqual(0, cv.getAsString(BaseEvent.Columns.TIME));
        assertEquals(BaseEvent.Status.NOT_POSTED, cv.getAsInteger(BaseEvent.Columns.STATUS).intValue());
        assertNull(cv.getAsString(BaseEvent.Columns.EVENT_UUID));
        assertNull(cv.getAsString(BaseEvent.Columns.VARIANT_UUID));
        assertNull(cv.getAsString(BaseEvent.Columns.DEVICE_ID));
        assertNull(cv.getAsString(MessageReceiptData.Columns.MESSAGE_UUID));
    }

    public void testConstructFromCursor1() {
        final FakeCursor cursor = new FakeCursor();
        cursor.addField(BaseColumns._ID, TEST_ROW_ID_1);
        cursor.addField(BaseEvent.Columns.TIME, TEST_TIME_1);
        cursor.addField(BaseEvent.Columns.STATUS, BaseEvent.Status.POSTED);
        cursor.addField(BaseEvent.Columns.EVENT_UUID, TEST_EVENT_ID_1);
        cursor.addField(BaseEvent.Columns.DEVICE_ID, TEST_DEVICE_ID_1);
        cursor.addField(BaseEvent.Columns.VARIANT_UUID, TEST_EVENT_VARIANT_UUID_1);
        cursor.addField(MessageReceiptData.Columns.MESSAGE_UUID, TEST_MESSAGE_UUID_1);
        final MessageReceiptEvent event = new MessageReceiptEvent(cursor);
        assertEquals(TEST_ROW_ID_1, event.getId());
        assertEquals(TEST_TIME_1, event.getTime());
        assertEquals(BaseEvent.Status.POSTED, event.getStatus());
        assertEquals(TEST_EVENT_ID_1, event.getEventId());
        assertEquals(TEST_EVENT_VARIANT_UUID_1, event.getVariantUuid());
        assertEquals(TEST_DEVICE_ID_1, event.getDeviceId());
        assertNotNull(event.getData());
        assertEquals(TEST_MESSAGE_UUID_1, event.getData().getMessageUuid());
    }

    public void testConstructFromCursor2() {
        final FakeCursor cursor = new FakeCursor();
        final MessageReceiptEvent event = new MessageReceiptEvent(cursor);
        assertEquals(0, event.getId());
        assertNull(event.getTime());
        assertEquals(BaseEvent.Status.NOT_POSTED, event.getStatus());
        assertNull(event.getEventId());
        assertNull(event.getVariantUuid());
        assertNull(event.getDeviceId());
        assertNull(event.getData());
    }

    public void testIsParcelable1() {
        final MessageReceiptEvent inputEvent = getMessageReceiptEvent1();
        final MessageReceiptEvent outputEvent = getObjectViaParcel(inputEvent);
        assertNotNull(outputEvent);
        assertEquals(inputEvent, outputEvent);
    }

    public void testIsParcelable2() {
        final MessageReceiptEvent inputEvent = new MessageReceiptEvent();
        final MessageReceiptEvent outputEvent = getObjectViaParcel(inputEvent);
        assertNotNull(outputEvent);
        assertEquals(inputEvent, outputEvent);
    }

    private MessageReceiptEvent getObjectViaParcel(MessageReceiptEvent inputEvent) {
        final Parcel inputParcel = Parcel.obtain();
        inputEvent.writeToParcel(inputParcel, 0);
        final byte[] bytes = inputParcel.marshall();
        assertNotNull(bytes);
        final Parcel outputParcel = Parcel.obtain();
        outputParcel.unmarshall(bytes, 0, bytes.length);
        outputParcel.setDataPosition(0);
        final MessageReceiptEvent outputEvent = MessageReceiptEvent.CREATOR.createFromParcel(outputParcel);
        inputParcel.recycle();
        outputParcel.recycle();
        return outputEvent;
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

    public static MessageReceiptEvent getMessageReceiptEvent1() {
        final MessageReceiptEvent event = new MessageReceiptEvent();
        final MessageReceiptData data = new MessageReceiptData();
        event.setEventId(TEST_EVENT_ID_1);
        event.setVariantUuid(TEST_EVENT_VARIANT_UUID_1);
        event.setDeviceId(TEST_DEVICE_ID_1);
        event.setTime(getTestDate1());
        event.setData(data);
        data.setMessageUuid(TEST_MESSAGE_UUID_1);
        return event;
    }

    public static MessageReceiptEvent getMessageReceiptEvent2() {
        final MessageReceiptEvent event = new MessageReceiptEvent();
        final MessageReceiptData data = new MessageReceiptData();
        event.setEventId(TEST_EVENT_ID_2);
        event.setVariantUuid(TEST_EVENT_VARIANT_UUID_2);
        event.setDeviceId(TEST_DEVICE_ID_2);
        event.setTime(getTestDate1());
        event.setData(data);
        data.setMessageUuid(TEST_MESSAGE_UUID_2);
        return event;
    }
}
