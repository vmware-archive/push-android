package io.pivotal.android.push.model.geofence;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public final class PCFPushGeofencePayload {

    @SerializedName("androidFcm")
    private Map<String, String> androidFcm = new HashMap<>();

    public Map<String, String> getAndroidFcm() {
        return androidFcm;
    }

    public PCFPushGeofencePayload(PCFPushGeofencePayload payload) {
        this.androidFcm = new HashMap<>(payload.androidFcm);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PCFPushGeofencePayload that = (PCFPushGeofencePayload) o;

        return !(androidFcm != null ? !androidFcm.equals(that.androidFcm) : that.androidFcm != null);
    }

    @Override
    public int hashCode() {
        return androidFcm != null ? androidFcm.hashCode() : 0;
    }
}
