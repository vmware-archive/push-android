package io.pivotal.android.push.geofence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.pivotal.android.push.model.api.PCFPushGeofenceData;
import io.pivotal.android.push.model.api.PCFPushGeofenceDataList;
import io.pivotal.android.push.model.api.PCFPushGeofenceLocation;
import io.pivotal.android.push.model.api.PCFPushGeofenceResponseData;

public class GeofenceEngine {

    private GeofenceRegistrar registrar;
    private GeofencePersistentStore persistentStore;

    public GeofenceEngine(GeofenceRegistrar registrar, GeofencePersistentStore persistentStore) {
        verifyArguments(registrar, persistentStore);
        saveArguments(registrar, persistentStore);
    }

    private void verifyArguments(GeofenceRegistrar geofenceRegistrar, GeofencePersistentStore persistentStore) {
        if (geofenceRegistrar == null) {
            throw new IllegalArgumentException("registrar may not be null");
        }
        if (persistentStore == null) {
            throw new IllegalArgumentException("persistentStore may not be null");
        }
    }

    private void saveArguments(GeofenceRegistrar geofenceRegistrar, GeofencePersistentStore geofencePersistentStore) {
        this.registrar = geofenceRegistrar;
        this.persistentStore = geofencePersistentStore;
    }

    public void processResponseData(PCFPushGeofenceResponseData responseData) {

        if (responseData == null) {
            return;
        }

        final PCFPushGeofenceDataList storedGeofences = persistentStore.getCurrentlyRegisteredGeofences();

        if (storedGeofences != null && storedGeofences.size() == 0 && (responseData.getGeofences() == null || responseData.getGeofences().size() == 0)) {
            return;
        }

        final PCFPushGeofenceDataList requiredGeofences = new PCFPushGeofenceDataList();
        final List<PCFPushGeofenceData> newGeofences = responseData.getGeofences();

        if (storedGeofences != null) {

            if (responseData.getDeletedGeofenceIds() != null && responseData.getDeletedGeofenceIds().size() > 0) {
                for(final long id : responseData.getDeletedGeofenceIds()) {
                    if (storedGeofences.indexOfKey(id) >= 0) {
                        storedGeofences.remove(id);
                    }
                }
            }

            requiredGeofences.addAll(storedGeofences);
        }
        if (newGeofences != null) {
            requiredGeofences.addAll(newGeofences);
        }

        final Map<String, PCFPushGeofenceLocation> requiredGeofencesMap = new HashMap<>();
        if (requiredGeofences.size() > 0) {
            for (final PCFPushGeofenceData geofence : requiredGeofences) {
                for (final PCFPushGeofenceLocation location : geofence.getLocations()) {
                    requiredGeofencesMap.put(getAndroidRequestId(geofence.getId(), location.getId()), location);
                }
            }
        }
        registrar.registerGeofences(requiredGeofencesMap);
    }

    public static String getAndroidRequestId(long geofenceId, long locationId) {
        return String.format("%d_%d", geofenceId, locationId);
    }
}
