package org.omnia.pushsdk.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class MessageReceiptData {

    /* {
     *    "msg_uuid" : "message-uuid-from-server", // optional field
     *    "timestamp" : "2014-03-24T12:15:41+0000" (time message received - UTC - ISO 8601)
     * }
     */

    // Optional field
    @SerializedName("msg_uuid")
    private String messageUuid;

    @SerializedName("timestamp")
    private String timestamp;

    // TODO update to new format (includes variant_uuid)

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

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (!(o instanceof MessageReceiptData)) {
            return false;
        }

        MessageReceiptData other = (MessageReceiptData) o;

        if (other.timestamp == null && timestamp != null) {
            return false;
        }
        if (other.timestamp != null && timestamp == null) {
            return false;
        }
        if (other.timestamp != null && timestamp != null && !(other.timestamp.equals(timestamp))) {
            return false;
        }

        if (other.messageUuid == null && messageUuid != null) {
            return false;
        }
        if (other.messageUuid != null && messageUuid == null) {
            return false;
        }
        if (other.messageUuid != null && messageUuid != null && !(other.messageUuid.equals(messageUuid))) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = (result * 31) + (timestamp == null ? 0 : timestamp.hashCode());
        result = (result * 31) + (messageUuid == null ? 0 : messageUuid.hashCode());
        return result;
    }
}
