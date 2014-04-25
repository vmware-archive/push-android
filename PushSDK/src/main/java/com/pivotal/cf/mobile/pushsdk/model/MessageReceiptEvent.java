package com.pivotal.cf.mobile.pushsdk.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import com.pivotal.cf.mobile.pushsdk.database.DatabaseConstants;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MessageReceiptEvent extends BaseEvent implements Parcelable {

    public static class Columns {
        public static final String DATA = "data";
    }

    public static final String TYPE = "event_push_received";

    @SerializedName(Columns.DATA)
    private MessageReceiptData data;
    
    public MessageReceiptEvent() {
        super();
    }

    // Copy constructor
    public MessageReceiptEvent(MessageReceiptEvent source) {
        super(source);
        if (source.data != null) {
            this.data = new MessageReceiptData(source.data);
        }
    }

    public MessageReceiptEvent(Cursor cursor) {
        int columnIndex;

        columnIndex = cursor.getColumnIndex(BaseColumns._ID);
        if (columnIndex >= 0) {
            setId(cursor.getInt(columnIndex));
        } else {
            setId(0);
        }

        columnIndex = cursor.getColumnIndex(BaseEvent.Columns.STATUS);
        if (columnIndex >= 0) {
            setStatus(cursor.getInt(columnIndex));
        } else {
            setStatus(Status.NOT_POSTED);
        }

        columnIndex = cursor.getColumnIndex(BaseEvent.Columns.EVENT_UUID);
        if (columnIndex >= 0) {
            setEventId(cursor.getString(columnIndex));
        }

        columnIndex = cursor.getColumnIndex(BaseEvent.Columns.VARIANT_UUID);
        if (columnIndex >= 0) {
            setVariantUuid(cursor.getString(columnIndex));
        }

        columnIndex = cursor.getColumnIndex(BaseEvent.Columns.TIME);
        if (columnIndex >= 0) {
            setTime(cursor.getString(columnIndex));
        }

        columnIndex = cursor.getColumnIndex(BaseEvent.Columns.DEVICE_ID);
        if (columnIndex >= 0) {
            setDeviceId(cursor.getString(columnIndex));
        }

        columnIndex = cursor.getColumnIndex(MessageReceiptData.Columns.MESSAGE_UUID);
        if (columnIndex >= 0) {
            setData(new MessageReceiptData());
            getData().setMessageUuid(cursor.getString(columnIndex));
        }
    }

    @Override
    protected String getEventType() {
        return TYPE;
    }

    @Override
    public ContentValues getContentValues() {
        // NOTE - do not save the 'id' field to the ContentValues. Let the database
        // figure out the 'id' itself.
        final ContentValues cv = new ContentValues();
        cv.put(BaseEvent.Columns.EVENT_UUID, getEventId());
        cv.put(BaseEvent.Columns.VARIANT_UUID, getVariantUuid());
        cv.put(BaseEvent.Columns.TIME, getTime());
        cv.put(BaseEvent.Columns.STATUS, getStatus());
        cv.put(BaseEvent.Columns.DEVICE_ID, getDeviceId());
        if (data != null) {
            cv.put(MessageReceiptData.Columns.MESSAGE_UUID, data.getMessageUuid());
        } else {
            cv.put(MessageReceiptData.Columns.MESSAGE_UUID, (String) null);
        }
        return cv;
    }

    public MessageReceiptData getData() {
        return data;
    }

    public void setData(MessageReceiptData data) {
        this.data = data;
    }

    public static MessageReceiptEvent getMessageReceiptEvent(String variantUuid, String messageUuid, String deviceId) {
        final String eventId = UUID.randomUUID().toString();
        final Date time = new Date();
        return getMessageReceiptEvent(eventId, variantUuid, messageUuid, deviceId, time);
    }

    public static MessageReceiptEvent getMessageReceiptEvent(String variantUuid, String messageUuid, String deviceId, Date time) {
        final String eventId = UUID.randomUUID().toString();
        return getMessageReceiptEvent(eventId, variantUuid, messageUuid, deviceId, time);
    }

    public static MessageReceiptEvent getMessageReceiptEvent(String eventId, String variantUuid, String messageUuid, String deviceId, Date time) {
        final MessageReceiptEvent event = new MessageReceiptEvent();
        event.setEventId(eventId);
        event.setVariantUuid(variantUuid);
        event.setTime(time);
        event.setDeviceId(deviceId);
        event.setData(new MessageReceiptData());
        event.getData().setMessageUuid(messageUuid);
        event.setStatus(Status.NOT_POSTED);
        return event;
    }


    public static String getCreateTableSqlStatement() {
        final StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS ");
        sb.append('\'');
        sb.append(DatabaseConstants.MESSAGE_RECEIPTS_TABLE_NAME);
        sb.append("\' ('");
        sb.append(BaseColumns._ID);
        sb.append("' INTEGER PRIMARY KEY AUTOINCREMENT, '");
        sb.append(BaseEvent.Columns.EVENT_UUID);
        sb.append("' TEXT, '");
        sb.append(BaseEvent.Columns.VARIANT_UUID);
        sb.append("' TEXT, '");
        sb.append(BaseEvent.Columns.DEVICE_ID);
        sb.append("' TEXT, '");
        sb.append(BaseEvent.Columns.TIME);
        sb.append("' INT, '");
        sb.append(BaseEvent.Columns.STATUS);
        sb.append("' INT, '");
        sb.append(MessageReceiptData.Columns.MESSAGE_UUID);
        sb.append("' TEXT);");
        return sb.toString();
    }

    public static String getDropTableSqlStatement() {
        final StringBuilder sb = new StringBuilder();
        sb.append("DROP TABLE IF EXISTS '");
        sb.append(DatabaseConstants.MESSAGE_RECEIPTS_TABLE_NAME);
        sb.append("';");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {

        if (!super.equals(o))
            return false;

        final MessageReceiptEvent other = (MessageReceiptEvent) o;

        if (other.data == null && data != null) {
            return false;
        }
        if (other.data != null && data == null) {
            return false;
        }
        if (other.data != null && data != null && !data.equals(other.data)) {
            return false;
        }

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

    // Parcelable stuff

    public static final Parcelable.Creator<MessageReceiptEvent> CREATOR = new Parcelable.Creator<MessageReceiptEvent>() {

        public MessageReceiptEvent createFromParcel(Parcel in) {
            return new MessageReceiptEvent(in);
        }

        public MessageReceiptEvent[] newArray(int size) {
            return new MessageReceiptEvent[size];
        }
    };

    private MessageReceiptEvent(Parcel in) {
        super(in);
        data = in.readParcelable(MessageReceiptData.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeParcelable(data, flags);
    }
}
