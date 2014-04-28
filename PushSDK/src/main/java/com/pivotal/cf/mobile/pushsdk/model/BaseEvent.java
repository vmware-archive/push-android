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
import com.pivotal.cf.mobile.pushsdk.util.PushLibLogger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class BaseEvent implements Parcelable {

    public static class Columns {
        public static final String EVENT_UUID = "id";
        public static final String TYPE = "type";
        public static final String TIME = "time";
        public static final String VARIANT_UUID = "variant_uuid";
        public static final String STATUS = "status";
        public static final String DEVICE_ID = "device_id";
        public static final String DATA = "data";
    }

    public static class Status {
        public static final int NOT_POSTED = 0;
        public static final int POSTING = 1;
        public static final int POSTED = 2;
        public static final int POSTING_ERROR = 3;
    }

    public static String statusString(int status) {
        switch (status) {
            case Status.NOT_POSTED:
                return "Not posted";
            case Status.POSTING:
                return "Posting";
            case Status.POSTED:
                return "Posted";
            case Status.POSTING_ERROR:
                return "Error";
        }
        return "?";
    }

    private transient int status;
    private transient int id;

    @SerializedName(Columns.EVENT_UUID)
    private String eventId;
    
    @SerializedName(Columns.TYPE)
    private String eventType;

    @SerializedName(Columns.TIME)
    private String time;

    @SerializedName(Columns.VARIANT_UUID)
    private String variantUuid;

    @SerializedName(Columns.DEVICE_ID)
    private String deviceId;

    // TODO - can we generalize to "Map<String, Object>"?
    @SerializedName(Columns.DATA)
    private HashMap<String, String> data;

    public BaseEvent() {
    }

    // Construct from cursor
    public BaseEvent(Cursor cursor) {
        int columnIndex;

        columnIndex = cursor.getColumnIndex(BaseColumns._ID);
        if (columnIndex >= 0) {
            setId(cursor.getInt(columnIndex));
        } else {
            setId(0);
        }

        columnIndex = cursor.getColumnIndex(Columns.STATUS);
        if (columnIndex >= 0) {
            setStatus(cursor.getInt(columnIndex));
        } else {
            setStatus(Status.NOT_POSTED);
        }

        columnIndex = cursor.getColumnIndex(Columns.TYPE);
        if (columnIndex >= 0) {
            setEventType(cursor.getString(columnIndex));
        }

        columnIndex = cursor.getColumnIndex(Columns.EVENT_UUID);
        if (columnIndex >= 0) {
            setEventId(cursor.getString(columnIndex));
        }

        columnIndex = cursor.getColumnIndex(Columns.VARIANT_UUID);
        if (columnIndex >= 0) {
            setVariantUuid(cursor.getString(columnIndex));
        }

        columnIndex = cursor.getColumnIndex(Columns.TIME);
        if (columnIndex >= 0) {
            setTime(cursor.getString(columnIndex));
        }

        columnIndex = cursor.getColumnIndex(Columns.DEVICE_ID);
        if (columnIndex >= 0) {
            setDeviceId(cursor.getString(columnIndex));
        }

        columnIndex = cursor.getColumnIndex(Columns.DATA);
        if (columnIndex >= 0) {
            if (cursor.isNull(columnIndex)) {
                setData(null);
            } else {
                final byte[] bytes = cursor.getBlob(columnIndex);
                final Serializable deserializedBytes = deserialize(bytes);
                setData(deserializedBytes);
            }
        }
    }

    // Copy constructor
    public BaseEvent(BaseEvent source) {
        // TODO - do a deep copy (i.e.: new copies of all of the individual fields)
        this.status = source.status;
        this.id = source.id;
        this.eventId = source.eventId;
        this.eventType = source.eventType;
        this.time = source.time;
        this.variantUuid = source.variantUuid;
        this.deviceId = source.deviceId;
        this.data = source.data;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        if (status != Status.NOT_POSTED && status != Status.POSTING && status != Status.POSTED && status != Status.POSTING_ERROR) {
            throw new IllegalArgumentException("Illegal event status: " + status);
        }
        this.status = status;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String id) {
        this.eventId = id;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setTime(Date time) {
        if (time != null) {
            this.time = String.format("%d", time.getTime() / 1000L);
        } else {
            this.time = null;
        }
    }

    public String getVariantUuid() {
        return variantUuid;
    }

    public void setVariantUuid(String variantUuid) {
        this.variantUuid = variantUuid;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public HashMap<String, String> getData() {
        return data;
    }

    public void setData(HashMap<String, String> data) {
        this.data = data;
    }

    private void setData(Serializable serializedData) {
        if (serializedData == null) {
            setData(null);
        } else if (HashMap.class.isAssignableFrom(serializedData.getClass())) {
            this.data = (HashMap<String, String>) serializedData;
        } else {
            PushLibLogger.w("Warning: attempted to deserialize invalid event data field");
            setData(null);
        }
    }

    @Override
    public boolean equals(Object o) {

        if (o == null) {
            return false;
        }

        if (!(o instanceof BaseEvent)) {
            return false;
        }

        final BaseEvent other = (BaseEvent) o;

        if (other.status != status) {
            return false;
        }

        if (other.eventId == null && eventId != null) {
            return false;
        }
        if (other.eventId != null && eventId == null) {
            return false;
        }
        if (other.eventId != null && eventId != null && !(other.eventId.equals(eventId))) {
            return false;
        }

        if (other.eventType == null && eventType != null) {
            return false;
        }
        if (other.eventType != null && eventType == null) {
            return false;
        }
        if (other.eventType != null && eventType != null && !(other.eventType.equals(eventType))) {
            return false;
        }

        if (other.time == null && time != null) {
            return false;
        }
        if (other.time != null && time == null) {
            return false;
        }
        if (other.time != null && time != null && !(other.time.equals(time))) {
            return false;
        }

        if (other.variantUuid == null && variantUuid != null) {
            return false;
        }
        if (other.variantUuid != null && variantUuid == null) {
            return false;
        }
        if (other.variantUuid != null && variantUuid != null && !(other.variantUuid.equals(variantUuid))) {
            return false;
        }

        if (other.deviceId == null && deviceId != null) {
            return false;
        }
        if (other.deviceId != null && deviceId == null) {
            return false;
        }
        if (other.deviceId != null && deviceId != null && !(other.deviceId.equals(deviceId))) {
            return false;
        }

        if (other.data == null && data != null) {
            return false;
        }
        if (other.data != null && data == null) {
            return false;
        }
        if (other.data != null && data != null && !(other.data.equals(data))) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = (result * 31) + (eventId == null ? 0 : eventId.hashCode());
        result = (result * 31) + (eventType == null ? 0 : eventType.hashCode());
        result = (result * 31) + (time == null ? 0 : time.hashCode());
        result = (result * 31) + (variantUuid == null ? 0 : variantUuid.hashCode());
        result = (result * 31) + (deviceId == null ? 0 : deviceId.hashCode());
        result = (result * 31) + (data == null ? 0 : data.hashCode());
        return result;
    }

    // JSON helpers

    public static List<BaseEvent> jsonStringToList(String str) {
        final Gson gson = new Gson();
        final Type type = getTypeToken();
        final List list = gson.fromJson(str, type);
        return list;
    }

    public static String listToJsonString(List<BaseEvent> list) {
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
        return new TypeToken<List<BaseEvent>>(){}.getType();
    }

    // Database helpers

    public ContentValues getContentValues() {
        // NOTE - do not save the 'id' field to the ContentValues. Let the database
        // figure out the 'id' itself.
        final ContentValues cv = new ContentValues();
        cv.put(Columns.EVENT_UUID, getEventId());
        cv.put(Columns.VARIANT_UUID, getVariantUuid());
        cv.put(Columns.TIME, getTime());
        cv.put(Columns.STATUS, getStatus());
        cv.put(Columns.DEVICE_ID, getDeviceId());
        cv.put(Columns.TYPE, getEventType());

        if (data != null) {
            final byte[] bytes = serialize(data);
            cv.put(Columns.DATA, bytes);
        } else {
            cv.putNull(Columns.DATA);
        }

        return cv;
    }

    public static String getCreateTableSqlStatement() {
        final StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS ");
        sb.append('\'');
        sb.append(DatabaseConstants.EVENTS_TABLE_NAME);
        sb.append("\' ('");
        sb.append(BaseColumns._ID);
        sb.append("' INTEGER PRIMARY KEY AUTOINCREMENT, '");
        sb.append(Columns.TYPE);
        sb.append("' TEXT, '");
        sb.append(Columns.EVENT_UUID);
        sb.append("' TEXT, '");
        sb.append(Columns.VARIANT_UUID);
        sb.append("' TEXT, '");
        sb.append(Columns.DEVICE_ID);
        sb.append("' TEXT, '");
        sb.append(Columns.TIME);
        sb.append("' INT, '");
        sb.append(Columns.STATUS);
        sb.append("' INT, '");
        sb.append(Columns.DATA);
        sb.append("' BLOB);");
        return sb.toString();
    }

    public static String getDropTableSqlStatement() {
        final StringBuilder sb = new StringBuilder();
        sb.append("DROP TABLE IF EXISTS '");
        sb.append(DatabaseConstants.EVENTS_TABLE_NAME);
        sb.append("';");
        return sb.toString();
    }

    public static int getRowIdFromCursor(final Cursor cursor) {
        final int idColumn = cursor.getColumnIndex(BaseColumns._ID);
        if (idColumn < 0) {
            throw new IllegalArgumentException("No " + BaseColumns._ID + " in cursor");
        }
        final int id = cursor.getInt(idColumn);
        return id;
    }

    // Serializable helpers

    public static Serializable deserialize(byte[] bytes) {

        ByteArrayInputStream byteStream = null;
        ObjectInputStream in = null;

        try {
            byteStream = new ByteArrayInputStream(bytes);
            in = new ObjectInputStream(byteStream);
            return (Serializable) in.readObject();

        } catch (IOException i) {
            PushLibLogger.ex("Error deserializing data: ", i);
        } catch (ClassNotFoundException c) {
            PushLibLogger.ex("Error deserializing data: ", c);
        }

        finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {}
            }
            if (byteStream != null) {
                try {
                    byteStream.close();
                } catch (IOException e) {}
            }
        }

        return null;
    }

    public static byte[] serialize(Serializable data) {

        ByteArrayOutputStream byteStream = null;
        ObjectOutputStream out = null;

        try {
            byteStream = new ByteArrayOutputStream();
            out = new ObjectOutputStream(byteStream);
            out.writeObject(data);
            return byteStream.toByteArray();

        } catch (IOException i) {
            PushLibLogger.w("Warning: Serializable object didn't serialize.");
        }

        finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {}
            }
            if (byteStream != null) {
                try {
                    byteStream.close();
                } catch (IOException e) {}
            }
        }

        return null;
    }

    // Parcelable stuff

    public static final Parcelable.Creator<BaseEvent> CREATOR = new Parcelable.Creator<BaseEvent>() {

        public BaseEvent createFromParcel(Parcel in) {
            return new BaseEvent(in);
        }

        public BaseEvent[] newArray(int size) {
            return new BaseEvent[size];
        }
    };

    private BaseEvent(Parcel in) {
        id = in.readInt();
        status = in.readInt();
        eventType = in.readString();
        eventId = in.readString();
        variantUuid = in.readString();
        time = in.readString();
        deviceId = in.readString();
        setData(in.readSerializable());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeInt(status);
        out.writeString(eventType);
        out.writeString(eventId);
        out.writeString(variantUuid);
        out.writeString(time);
        out.writeString(deviceId);
        out.writeSerializable(data);
    }

}
