package io.pivotal.android.push.model.geofence;

import com.google.gson.annotations.SerializedName;

public final class PCFPushGeofenceLocation {

    @SerializedName("id")
    private long id;

    @SerializedName("android_request_id")
    private String androidRequestId;

    @SerializedName("name")
    private String name;

    @SerializedName("lat")
    private double latitude;

    @SerializedName("long")
    private double longitude;

    @SerializedName("rad")
    private double radius;

    public long getId() {
        return id;
    }

    public String getAndroidRequestId() {
        return androidRequestId;
    }

    public void setAndroidRequestId(String androidRequestId) {
        this.androidRequestId = androidRequestId;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getRadius() {
        return radius;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PCFPushGeofenceLocation that = (PCFPushGeofenceLocation) o;

        if (id != that.id) return false;
        if (Double.compare(that.latitude, latitude) != 0) return false;
        if (Double.compare(that.longitude, longitude) != 0) return false;
        if (Double.compare(that.radius, radius) != 0) return false;
        if (androidRequestId != null ? !androidRequestId.equals(that.androidRequestId) : that.androidRequestId != null)
            return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (int) (id ^ (id >>> 32));
        result = 31 * result + (androidRequestId != null ? androidRequestId.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        temp = Double.doubleToLongBits(latitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(radius);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}