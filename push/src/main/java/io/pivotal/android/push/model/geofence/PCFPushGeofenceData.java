package io.pivotal.android.push.model.geofence;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class PCFPushGeofenceData {

    @SerializedName("id")
    private long id;

    @SerializedName("expiry_time")
    private Date expiryTime;

    @SerializedName("locations")
    private List<PCFPushGeofenceLocation> locations;

    @SerializedName("data")
    private PCFPushGeofencePayload payload;

    @SerializedName("tags")
    private List<String> tags;

    @SerializedName("trigger_type")
    private String triggerType;

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

    public PCFPushGeofencePayload getPayload() {
        return payload;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public PCFPushGeofenceLocation getLocationWithId(long locationId) {
        if (locations == null) {
            return null;
        }

        for (PCFPushGeofenceLocation location : locations) {
            if (location.getId() == locationId) {
                return location;
            }
        }
        return null;
    }

    public PCFPushGeofenceData newCopyWithoutLocations() {
        final PCFPushGeofenceData newItem = new PCFPushGeofenceData();
        newItem.id = id;
        newItem.triggerType = triggerType;
        if (expiryTime != null) {
            newItem.expiryTime = new Date(expiryTime.getTime());
        }
        if (payload != null) {
            newItem.payload = new PCFPushGeofencePayload(payload);
        }
        if (tags != null) {
            newItem.tags = new ArrayList<>(tags);
        }
        newItem.locations = new ArrayList<>();
        return newItem;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PCFPushGeofenceData other = (PCFPushGeofenceData) o;

        if (id != other.id) return false;
        if (payload != null ? !payload.equals(other.payload) : other.payload != null) return false;
        if (expiryTime != null ? !expiryTime.equals(other.expiryTime) : other.expiryTime != null) return false;
        if (locations != null ? !locations.equals(other.locations) : other.locations != null) return false;
        if (tags != null ? !tags.equals(other.tags) : other.tags != null) return false;
        if (triggerType != other.triggerType) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (expiryTime != null ? expiryTime.hashCode() : 0);
        result = 31 * result + (locations != null ? locations.hashCode() : 0);
        result = 31 * result + (payload != null ? payload.hashCode() : 0);
        result = 31 * result + (tags != null ? tags.hashCode() : 0);
        result = 31 * result + (triggerType != null ? triggerType.hashCode() : 0);
        return result;
    }
}
