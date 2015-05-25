package io.pivotal.android.push.model.geofence;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PCFPushGeofencePayload {

    @SerializedName("android")
    private Map<String, String> android = new HashMap<>();

    public Map<String, String> getAndroid() {
        return android;
    }

    public PCFPushGeofencePayload(PCFPushGeofencePayload payload) {
        this.android = new HashMap<>(payload.android);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PCFPushGeofencePayload that = (PCFPushGeofencePayload) o;

        return !(android != null ? !android.equals(that.android) : that.android != null);
    }

    @Override
    public int hashCode() {
        return android != null ? android.hashCode() : 0;
    }
}
