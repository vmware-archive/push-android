package io.pivotal.android.push.version;

public class GeofenceStatus {

    private final boolean isError;
    private final String errorReason;
    private final int numberCurrentlyMonitoringGeofences;

    // TODO - add 'isEnabled'?

    public GeofenceStatus(boolean isError, String errorReason, int numberCurrentlyMonitoringGeofences) {
        this.isError = isError;
        this.errorReason = errorReason;
        this.numberCurrentlyMonitoringGeofences = numberCurrentlyMonitoringGeofences;
    }

    public static GeofenceStatus emptyStatus() {
        return new GeofenceStatus(false, null, 0);
    }

    public boolean isError() {
        return isError;
    }

    public String getErrorReason() {
        return errorReason;
    }

    public int getNumberCurrentlyMonitoringGeofences() {
        return numberCurrentlyMonitoringGeofences;
    }
}
