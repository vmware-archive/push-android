package io.pivotal.android.push.model.geofence;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class PCFPushGeofenceLocationMap extends HashMap<String, PCFPushGeofenceLocation> {

    public static class LocationEntry {
        private final long geofenceId;
        private final long locationId;
        private final PCFPushGeofenceLocation location;

        public LocationEntry(long geofenceId, long locationId, PCFPushGeofenceLocation location) {
            this.geofenceId = geofenceId;
            this.locationId = locationId;
            this.location = location;
        }

        public long getGeofenceId() {
            return geofenceId;
        }

        public long getLocationId() {
            return locationId;
        }

        public PCFPushGeofenceLocation getLocation() {
            return location;
        }

        /**
         * Returns the geofence data object associated with this particular geofence location
         *
         * @param list A list of known PCFPushGeofenceData objects
         *
         * @return the PCFPusgGeofenceData object that matches the geofence ID in this location.
         */
        public PCFPushGeofenceData getGeofenceData(PCFPushGeofenceDataList list) {
            return list.get(geofenceId);
        }
    }

    public void addAll(PCFPushGeofenceDataList list) {
        if (list != null) {
            for (final PCFPushGeofenceData geofence : list) {
                if (geofence != null && geofence.getLocations() != null) {
                    for (final PCFPushGeofenceLocation location : geofence.getLocations()) {
                        final String id = getAndroidRequestId(geofence.getId(), location.getId());
                        this.put(id, location);
                    }
                }
            }
        }
    }

    public Set<LocationEntry> locationEntrySet() {
        final Set<LocationEntry> locationEntries = new HashSet<>();
        for (final Entry<String, PCFPushGeofenceLocation> entry : entrySet()) {
            final long geofenceId = getGeofenceId(entry.getKey());
            final long locationId = getLocationId(entry.getKey());
            final LocationEntry locationEntry = new LocationEntry(geofenceId, locationId, entry.getValue());
            locationEntries.add(locationEntry);
        }
        return locationEntries;
    }

    private static long getGeofenceId(String key) {
        return Long.parseLong(key.split("_")[1]);
    }

    private static long getLocationId(String key) {
        return Long.parseLong(key.split("_")[2]);
    }

    public static String getAndroidRequestId(LocationEntry entry) {
        return getAndroidRequestId(entry.getGeofenceId(), entry.getLocationId());
    }

    public static String getAndroidRequestId(long geofenceId, long locationId) {
        return String.format("PCF_%d_%d", geofenceId, locationId);
    }
}
