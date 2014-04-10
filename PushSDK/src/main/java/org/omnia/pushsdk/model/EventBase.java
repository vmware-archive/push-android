package org.omnia.pushsdk.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public abstract class EventBase implements Parcelable {

    public static class Columns {
        public static final String EVENT_UUID = "id";
        public static final String TYPE = "type";
        public static final String TIME = "time";
        public static final String VARIANT_UUID = "variant_uuid";
        public static final String STATUS = "status";
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
    private String type;

    @SerializedName(Columns.TIME)
    private String time;

    @SerializedName(Columns.VARIANT_UUID)
    private String variantUuid;

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

    public String getType() {
        return type;
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

    public EventBase() {
        this.type = getEventType();
    }

    protected abstract String getEventType();

    public abstract ContentValues getContentValues();

    @Override
    public boolean equals(Object o) {

        if (o == null) {
            return false;
        }

        if (!(o instanceof EventBase)) {
            return false;
        }

        final EventBase other = (EventBase) o;

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

        if (other.type == null && type != null) {
            return false;
        }
        if (other.type != null && type == null) {
            return false;
        }
        if (other.type != null && type != null && !(other.type.equals(type))) {
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

        return true;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = (result * 31) + (eventId == null ? 0 : eventId.hashCode());
        result = (result * 31) + (type == null ? 0 : type.hashCode());
        result = (result * 31) + (time == null ? 0 : time.hashCode());
        result = (result * 31) + (variantUuid == null ? 0 : variantUuid.hashCode());
        return result;
    }

    public static int getRowIdFromCursor(final Cursor cursor) {
        final int idColumn = cursor.getColumnIndex(BaseColumns._ID);
        if (idColumn < 0) {
            throw new IllegalArgumentException("No " + BaseColumns._ID + " in cursor");
        }
        final int id = cursor.getInt(idColumn);
        return id;
    }

    // Parcelable stuff

    protected EventBase(Parcel in) {
        id = in.readInt();
        status = in.readInt();
        eventId = in.readString();
        variantUuid = in.readString();
        time = in.readString();
        type = getEventType();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeInt(status);
        out.writeString(eventId);
        out.writeString(variantUuid);
        out.writeString(time);
    }
}
