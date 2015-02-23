package io.pivotal.android.push.geofence;

import java.util.List;

import io.pivotal.android.push.model.geofence.PCFPushGeofenceData;
import io.pivotal.android.push.model.geofence.PCFPushGeofenceDataList;
import io.pivotal.android.push.model.geofence.PCFPushGeofenceLocationMap;
import io.pivotal.android.push.model.geofence.PCFPushGeofenceResponseData;

public class GeofenceEngine {

    private GeofenceRegistrar registrar;
    private GeofencePersistentStore store;

    public GeofenceEngine(GeofenceRegistrar registrar, GeofencePersistentStore store) {
        verifyArguments(registrar, store);
        saveArguments(registrar, store);
    }

    private void verifyArguments(GeofenceRegistrar geofenceRegistrar, GeofencePersistentStore store) {
        if (geofenceRegistrar == null) {
            throw new IllegalArgumentException("registrar may not be null");
        }
        if (store == null) {
            throw new IllegalArgumentException("store may not be null");
        }
    }

    private void saveArguments(GeofenceRegistrar registrar, GeofencePersistentStore store) {
        this.registrar = registrar;
        this.store = store;
    }

    public void processResponseData(PCFPushGeofenceResponseData responseData) {

        if (responseData == null) {
            return;
        }

        final PCFPushGeofenceDataList storedGeofences = store.getCurrentlyRegisteredGeofences();

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

        final PCFPushGeofenceLocationMap requiredGeofencesMap = new PCFPushGeofenceLocationMap();
        requiredGeofencesMap.addAll(requiredGeofences);

        registrar.registerGeofences(requiredGeofencesMap);
        store.saveRegisteredGeofences(requiredGeofences);
    }
}
