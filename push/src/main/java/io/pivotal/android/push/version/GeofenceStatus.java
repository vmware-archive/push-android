package io.pivotal.android.push.version;

import android.support.annotation.Nullable;

public class GeofenceStatus {

    private final boolean isError;
    private final String errorReason;
    private final int numberCurrentlyMonitoringGeofences;

    public GeofenceStatus(boolean isError, @Nullable String errorReason, int numberCurrentlyMonitoringGeofences) {
        this.isError = isError;
        this.errorReason = errorReason;
        this.numberCurrentlyMonitoringGeofences = numberCurrentlyMonitoringGeofences;
    }

    public static GeofenceStatus emptyStatus() {
        return new GeofenceStatus(false, null, 0);
    }

    /**
     * Indicates if an error happened while PCF Push tried to update or monitor geofences.
     * @return Returns true if an error occurred, else returns false.
     */
    public boolean isError() {
        return isError;
    }

    /**
     * The error reason (if there is one).
     * @return The reason for the error
     */
    public String getErrorReason() {
        return errorReason;
    }

    /**
     * The number of geofences currently being monitored.
     * @return number of geofences currently being monitored.
     */
    public int getNumberCurrentlyMonitoringGeofences() {
        return numberCurrentlyMonitoringGeofences;
    }
}
