package io.pivotal.android.push.model.geofence;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

public final class PCFPushGeofenceResponseData {

    @SerializedName("num")
    private long number;

    @SerializedName("last_modified")
    private Date lastModified;

    @SerializedName("geofences")
    private List<PCFPushGeofenceData> geofences;

    @SerializedName("deleted_geofence_ids")
    private List<Long> deletedGeofenceIds;

    public long getNumber() {
        return number;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public List<PCFPushGeofenceData> getGeofences() {
        return geofences;
    }

    public List<Long> getDeletedGeofenceIds() {
        return deletedGeofenceIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PCFPushGeofenceResponseData that = (PCFPushGeofenceResponseData) o;

        if (number != that.number) return false;
        if (deletedGeofenceIds != null ? !deletedGeofenceIds.equals(that.deletedGeofenceIds) : that.deletedGeofenceIds != null)
            return false;
        if (geofences != null ? !geofences.equals(that.geofences) : that.geofences != null)
            return false;
        if (lastModified != null ? !lastModified.equals(that.lastModified) : that.lastModified != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (number ^ (number >>> 32));
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0);
        result = 31 * result + (geofences != null ? geofences.hashCode() : 0);
        result = 31 * result + (deletedGeofenceIds != null ? deletedGeofenceIds.hashCode() : 0);
        return result;
    }
}
