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

        if (isEmptyUpdate(responseData, storedGeofences)) {
            return;
        }

        final PCFPushGeofenceDataList requiredGeofences = new PCFPushGeofenceDataList();

        if (areDeletedGeofences(responseData)) {
            addStoredGeofencesThatWereNotDeleted(requiredGeofences, storedGeofences, responseData);
        } else {
            requiredGeofences.addAll(storedGeofences);
        }

        addValidGeofencesFromUpdate(requiredGeofences, responseData.getGeofences());

        final PCFPushGeofenceLocationMap requiredGeofencesMap = new PCFPushGeofenceLocationMap();
        requiredGeofencesMap.addAll(requiredGeofences);

        registrar.registerGeofences(requiredGeofencesMap);
        store.saveRegisteredGeofences(requiredGeofences);
    }

    private boolean isEmptyUpdate(PCFPushGeofenceResponseData responseData, PCFPushGeofenceDataList storedGeofences) {
        return storedGeofences != null && storedGeofences.size() == 0 && (responseData.getGeofences() == null || responseData.getGeofences().size() == 0);
    }

    private boolean areDeletedGeofences(PCFPushGeofenceResponseData responseData) {
        return responseData.getDeletedGeofenceIds() != null && responseData.getDeletedGeofenceIds().size() > 0;
    }

    private void addStoredGeofencesThatWereNotDeleted(PCFPushGeofenceDataList requiredGeofences, PCFPushGeofenceDataList storedGeofences, final PCFPushGeofenceResponseData responseData) {
        requiredGeofences.filteredAddAll(storedGeofences, new PCFPushGeofenceDataList.Filter() {
            @Override
            public boolean filterItem(PCFPushGeofenceData item) {
                return isItemNotDeleted(item);
            }

            private boolean isItemNotDeleted(PCFPushGeofenceData item) {
                return !responseData.getDeletedGeofenceIds().contains(item.getId());
            }
        });
    }

    private void addValidGeofencesFromUpdate(PCFPushGeofenceDataList requiredGeofences, List<PCFPushGeofenceData> newGeofences) {
        requiredGeofences.filteredAddAll(newGeofences, new PCFPushGeofenceDataList.Filter() {

            @Override
            public boolean filterItem(PCFPushGeofenceData item) {
                return isItemValid(item);
            }

            private boolean isItemValid(PCFPushGeofenceData item) {
                if (item.getLocations() == null || item.getLocations().size() <= 0) return false;
                if (item.getData() == null || item.getData().size() <= 0) return false;
                if (item.getTriggerType() == null) return false;
                return true;
            }
        });
    }
}
