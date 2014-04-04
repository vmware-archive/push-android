package org.omnia.pushsdk.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class MessageReceiptEvent extends EventBase {

    private static final String TYPE = "event_push_received";

    @SerializedName("data")
    private MessageReceiptData data;
    
    public MessageReceiptEvent() {
        super();
    }

    @Override
    protected String getEventType() {
        return TYPE;
    }

    public MessageReceiptData getData() {
        return data;
    }

    public void setData(MessageReceiptData data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {

        if (!super.equals(o))
            return false;

        final MessageReceiptEvent other = (MessageReceiptEvent) o;

        if (other.data == null && data == null)
            return false;
        if (other.data == null && data != null)
            return false;
        if (other.data != null && data == null)
            return false;

        if (!data.equals(other.data))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = (result * 31) + (data == null ? 0 : data.hashCode());
        result = (result * 31) + super.hashCode();
        return result;
    }

    public static List<MessageReceiptEvent> jsonStringToList(String str) {
        final Gson gson = new Gson();
        final Type type = getTypeToken();
        final List list = gson.fromJson(str, type);
        return list;
    }


    public static String listToJsonString(List<MessageReceiptEvent> list) {
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
        return new TypeToken<List<MessageReceiptEvent>>(){}.getType();
    }
}
