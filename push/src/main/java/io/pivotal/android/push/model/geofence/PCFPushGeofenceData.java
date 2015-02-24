package io.pivotal.android.push.model.geofence;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;
import java.util.Map;

public final class PCFPushGeofenceData {

    public static enum TriggerType {
        ENTER, EXIT, ENTER_OR_EXIT
    }

    @SerializedName("id")
    private long id;

    @SerializedName("expiry_time")
    private Date expiryTime;

    @SerializedName("locations")
    private List<PCFPushGeofenceLocation> locations;

    @SerializedName("data")
    private Map<String, String> data;

    @SerializedName("tags")
    private List<String> tags;

    @SerializedName("trigger_type")
    private TriggerType triggerType;

    public long getId() {
        return id;
    }

    public Date getExpiryTime() {
        return expiryTime;
    }

    public List<PCFPushGeofenceLocation> getLocations() {
        return locations;
    }

    public List<String> getTags() {
        return tags;
    }

    public Map<String, String> getData() {
        return data;
    }

    public TriggerType getTriggerType() {
        return triggerType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PCFPushGeofenceData that = (PCFPushGeofenceData) o;

        if (id != that.id) return false;
        if (data != null ? !data.equals(that.data) : that.data != null) return false;
        if (expiryTime != null ? !expiryTime.equals(that.expiryTime) : that.expiryTime != null)
            return false;
        if (locations != null ? !locations.equals(that.locations) : that.locations != null)
            return false;
        if (tags != null ? !tags.equals(that.tags) : that.tags != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (expiryTime != null ? expiryTime.hashCode() : 0);
        result = 31 * result + (locations != null ? locations.hashCode() : 0);
        result = 31 * result + (data != null ? data.hashCode() : 0);
        result = 31 * result + (tags != null ? tags.hashCode() : 0);
        return result;
    }
}
