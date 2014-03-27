package org.omnia.pushsdk.model;

import android.test.AndroidTestCase;
import android.test.MoreAsserts;

import com.google.gson.Gson;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

public class MessageReceiptDataTest extends AndroidTestCase {

    private static final String TEST_MESSAGE_UUID_1 = "SOME-MESSAGE-UUID";
    private static final int TEST_YEAR_1 = 1969;
    private static final int TEST_MONTH_1 = 6;
    private static final int TEST_DAY_1 = 20;
    private static final int TEST_HOUR_1 = 20;
    private static final int TEST_MINUTE_1 = 17;
    private static final int TEST_SECOND_1 = 40;

    private static final String TEST_MESSAGE_UUID_2 = "ANOTHER-MESSAGE-UUID";
    private static final int TEST_YEAR_2 = 1957;
    private static final int TEST_MONTH_2 = 9;
    private static final int TEST_DAY_2 = 4;
    private static final int TEST_HOUR_2 = 19;
    private static final int TEST_MINUTE_2 = 28;
    private static final int TEST_SECOND_2 = 34;

    public void testEquals1() {
        final MessageReceiptData model1 = getMessageReceiptData1();
        final MessageReceiptData model2 = getMessageReceiptData1();
        assertEquals(model1, model2);
        assertEquals(model2, model1);
    }

    public void testNotEquals() {
        final MessageReceiptData model1 = getMessageReceiptData1();
        final MessageReceiptData model2 = getMessageReceiptData2();
        MoreAsserts.assertNotEqual(model1, model2);
        MoreAsserts.assertNotEqual(model2, model1);
    }

    public void testNotEquals2() {
        final MessageReceiptData model1 = new MessageReceiptData();
        model1.setTimestamp(getTestDate1());
        model1.setMessageUuid(TEST_MESSAGE_UUID_1);
        final MessageReceiptData model2 = new MessageReceiptData();
        model1.setTimestamp(getTestDate2());
        model1.setMessageUuid(TEST_MESSAGE_UUID_1);
        MoreAsserts.assertNotEqual(model1, model2);
        MoreAsserts.assertNotEqual(model2, model1);
    }

    public void testNotEquals3() {
        final MessageReceiptData model1 = new MessageReceiptData();
        model1.setTimestamp(getTestDate1());
        model1.setMessageUuid(TEST_MESSAGE_UUID_1);
        final MessageReceiptData model2 = new MessageReceiptData();
        model1.setTimestamp(getTestDate1());
        model1.setMessageUuid(TEST_MESSAGE_UUID_2);
        MoreAsserts.assertNotEqual(model1, model2);
        MoreAsserts.assertNotEqual(model2, model1);
    }

    public void testNotEquals4() {
        final MessageReceiptData model1 = new MessageReceiptData();
        model1.setTimestamp(getTestDate1());
        model1.setMessageUuid(TEST_MESSAGE_UUID_1);
        final MessageReceiptData model2 = new MessageReceiptData();
        model1.setTimestamp((String)null);
        model1.setMessageUuid(TEST_MESSAGE_UUID_1);
        MoreAsserts.assertNotEqual(model1, model2);
        MoreAsserts.assertNotEqual(model2, model1);
    }

    public void testNotEquals5() {
        final MessageReceiptData model1 = new MessageReceiptData();
        model1.setTimestamp(getTestDate1());
        model1.setMessageUuid(TEST_MESSAGE_UUID_1);
        final MessageReceiptData model2 = new MessageReceiptData();
        model1.setTimestamp(TEST_MESSAGE_UUID_1);
        model1.setMessageUuid(null);
        MoreAsserts.assertNotEqual(model1, model2);
        MoreAsserts.assertNotEqual(model2, model1);
    }

    public void testNotEqualsNull() {
        final MessageReceiptData model1 = getMessageReceiptData1();
        assertFalse(model1.equals(null));
    }

    public void testNotEqualsOtherObject() {
        final MessageReceiptData model1 = getMessageReceiptData1();
        assertFalse(model1.equals("INTERLOPER STRING"));
    }

    public void testHashCode() {
        final MessageReceiptData model1 = getMessageReceiptData1();
        final MessageReceiptData model2 = getMessageReceiptData1();
        assertEquals(model1.hashCode(), model2.hashCode());
    }

    public void testToJson() {
        final MessageReceiptData model = getMessageReceiptData1();

        final Gson gson = new Gson();
        final String json = gson.toJson(model);
        assertTrue(json.contains("\"msg_uuid\""));
        assertTrue(json.contains("\"" + TEST_MESSAGE_UUID_1 + "\""));
        assertTrue(json.contains("\"timestamp\""));
        assertTrue(json.contains("\"1969-07-20T20:17:40+0000\""));
    }

    public void testConvertStringToList() {
        final List<MessageReceiptData> list = MessageReceiptData.jsonStringToList(getTestListOfMessageReceipts());
        assertEquals(2, list.size());
        assertEquals(TEST_MESSAGE_UUID_1, list.get(0).getMessageUuid());
        assertEquals(TEST_MESSAGE_UUID_2, list.get(1).getMessageUuid());
    }

    public void testConvertStringToListEmpty1() {
        final List<MessageReceiptData> list = MessageReceiptData.jsonStringToList("");
        assertNull(list);
    }

    public void testConvertStringToListEmpty2() {
        final List<MessageReceiptData> list = MessageReceiptData.jsonStringToList("[]");
        assertEquals(0, list.size());
    }

    public void testConvertStringToListNull() {
        final List<MessageReceiptData> list = MessageReceiptData.jsonStringToList(null);
        assertNull(list);
    }

    public void testConvertListToString() {
        List<MessageReceiptData> list = new LinkedList<MessageReceiptData>();
        list.add(getMessageReceiptData1());
        list.add(getMessageReceiptData2());
        final String str = MessageReceiptData.listToJsonString(list);
        assertTrue(str.contains("\"msg_uuid\""));
        assertTrue(str.contains("\"" + TEST_MESSAGE_UUID_1 + "\""));
        assertTrue(str.contains("\"timestamp\""));
        assertTrue(str.contains("\"1969-07-20T20:17:40+0000\""));
        assertTrue(str.contains("\"" + TEST_MESSAGE_UUID_2 + "\""));
        assertTrue(str.contains("\"1957-10-04T19:28:34+0000\""));
    }

    public void testConvertListToStringEmpty() {
        List<MessageReceiptData> list = new LinkedList<MessageReceiptData>();
        final String str = MessageReceiptData.listToJsonString(list);
        assertEquals("[]", str);
    }

    public void testConvertListToStringNull() {
        final String str = MessageReceiptData.listToJsonString(null);
        assertNull(str);
    }

    public static MessageReceiptData getMessageReceiptData1() {
        final MessageReceiptData model = new MessageReceiptData();
        model.setMessageUuid(TEST_MESSAGE_UUID_1);
        model.setTimestamp(getTestDate1());
        return model;
    }

    public static MessageReceiptData getMessageReceiptData2() {
        final MessageReceiptData model = new MessageReceiptData();
        model.setMessageUuid(TEST_MESSAGE_UUID_2);
        model.setTimestamp(getTestDate2());
        return model;
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
        sb.append("[{\"msg_uuid\":\"");
        sb.append(TEST_MESSAGE_UUID_1);
        sb.append("\",\"timestamp\":\"");
        sb.append(MessageReceiptData.getDateFormatter().format(getTestDate1()));
        sb.append("\"},{\"msg_uuid\":\"");
        sb.append(TEST_MESSAGE_UUID_2);
        sb.append("\",\"timestamp\":\"");
        sb.append(MessageReceiptData.getDateFormatter().format(getTestDate2()));
        sb.append("\"}]");
        return sb.toString();
    }
}
