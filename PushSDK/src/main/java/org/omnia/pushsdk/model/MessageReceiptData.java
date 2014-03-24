package org.omnia.pushsdk.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class MessageReceiptData {

    /* {
     *    "msg_uuid" : "message-uuid-from-server",
     *    "timestamp" : "2014-03-24T12:15:41+0000" (time message received - UTC - ISO 8601)
     * }
     */

    @SerializedName("msg_uuid")
    private String messageUuid;

    @SerializedName("timestamp")
    private String timestamp;

    private static SimpleDateFormat dateFormatter;

    public MessageReceiptData() {
    }

    public String getMessageUuid() {
        return messageUuid;
    }

    public void setMessageUuid(String messageUuid) {
        this.messageUuid = messageUuid;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setTimestamp(Date timestamp) {
        final SimpleDateFormat dateFormatter = getDateFormatter();
        this.timestamp = dateFormatter.format(timestamp);
    }

    public static SimpleDateFormat getDateFormatter() {
        if (dateFormatter == null) {
            dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        }
        return dateFormatter;
    }

    public static List<MessageReceiptData> jsonStringToList(String str) {
        final Gson gson = new Gson();
        final Type type = getTypeToken();
        final List list = gson.fromJson(str, type);
        return list;
    }


    public static String listToJsonString(List<MessageReceiptData> list) {
        if (list == null) {
            return null;
        } else {
            final Gson gson = new Gson();
            final Type type = getTypeToken();
            final String str = gson.toJson(list, type);
            return str;
        }
    }

    private static Type getTypeToken() {
        return new TypeToken<List<MessageReceiptData>>(){}.getType();
    }
}
