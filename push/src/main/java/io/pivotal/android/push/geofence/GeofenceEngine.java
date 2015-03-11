package io.pivotal.android.push.geofence;

import java.util.List;

import io.pivotal.android.push.model.geofence.PCFPushGeofenceData;
import io.pivotal.android.push.model.geofence.PCFPushGeofenceDataList;
import io.pivotal.android.push.model.geofence.PCFPushGeofenceLocationMap;
import io.pivotal.android.push.model.geofence.PCFPushGeofenceResponseData;
import io.pivotal.android.push.util.Logger;
import io.pivotal.android.push.util.TimeProvider;

public class GeofenceEngine {

    public static final long NEVER_UPDATED_GEOFENCES = -1L;

    private GeofenceRegistrar registrar;
    private GeofencePersistentStore store;
    private TimeProvider timeProvider;

    public GeofenceEngine(GeofenceRegistrar registrar, GeofencePersistentStore store, TimeProvider timeProvider) {
        verifyArguments(registrar, store, timeProvider);
        saveArguments(registrar, store, timeProvider);
    }

    private void verifyArguments(GeofenceRegistrar geofenceRegistrar, GeofencePersistentStore store, TimeProvider timeProvider) {
        if (geofenceRegistrar == null) {
            throw new IllegalArgumentException("registrar may not be null");
        }
        if (store == null) {
            throw new IllegalArgumentException("store may not be null");
        }
        if (timeProvider == null) {
            throw new IllegalArgumentException("timeProvider may not be null");
        }
    }

    private void saveArguments(GeofenceRegistrar registrar, GeofencePersistentStore store, TimeProvider timeProvider) {
        this.registrar = registrar;
        this.store = store;
        this.timeProvider = timeProvider;
    }

    public void processResponseData(final long lastUpdatedTimestamp, final PCFPushGeofenceResponseData responseData) {

        // If the last updated lastUpdatedTimestamp is zero then we need to reset our stored data.
        if (lastUpdatedTimestamp == 0L) {
            store.reset();
            registrar.reset();
        }

        if (responseData == null) {
            return;
        }

        final PCFPushGeofenceDataList storedGeofences;

        if (lastUpdatedTimestamp != 0L) {
            storedGeofences = store.getCurrentlyRegisteredGeofences();
        } else {
            storedGeofences = new PCFPushGeofenceDataList();
        }

        if (!hasDataToPersist(responseData, storedGeofences)) {
            return;
        }

        final PCFPushGeofenceDataList requiredGeofences = new PCFPushGeofenceDataList();

        addValidGeofencesFromStore(requiredGeofences, storedGeofences, responseData);

        addValidGeofencesFromUpdate(requiredGeofences, responseData.getGeofences());

        final PCFPushGeofenceLocationMap geofencesToRegister = new PCFPushGeofenceLocationMap();
        geofencesToRegister.addAll(requiredGeofences);

        registrar.registerGeofences(geofencesToRegister, requiredGeofences);
        store.saveRegisteredGeofences(requiredGeofences);
    }

    private boolean hasDataToPersist(PCFPushGeofenceResponseData responseData, PCFPushGeofenceDataList storedGeofences) {
        return (storedGeofences != null && storedGeofences.size() > 0) ||
               (responseData.getGeofences() != null && responseData.getGeofences().size() > 0);
    }

    private void addValidGeofencesFromStore(final PCFPushGeofenceDataList requiredGeofences, final PCFPushGeofenceDataList storedGeofences, final PCFPushGeofenceResponseData responseData) {
        requiredGeofences.addFiltered(storedGeofences, new PCFPushGeofenceDataList.Filter() {

            @Override
            public boolean filterItem(final PCFPushGeofenceData item) {
                return !isDeletedItem(item, responseData) && !isExpiredItem(item) && !isUpdatedItem(item, responseData);
            }
        });
    }

    private boolean isUpdatedItem(PCFPushGeofenceData item, PCFPushGeofenceResponseData responseData) {
        if (item == null || responseData == null || responseData.getGeofences() == null) {
            return false;
        }
        for (PCFPushGeofenceData data : responseData.getGeofences()) {
            if (data.getId() == item.getId()) return true;
        }
        return false;
    }

    private boolean isDeletedItem(PCFPushGeofenceData item, PCFPushGeofenceResponseData responseData) {
        return responseData.getDeletedGeofenceIds() != null && responseData.getDeletedGeofenceIds().contains(item.getId());
    }

    private void addValidGeofencesFromUpdate(PCFPushGeofenceDataList requiredGeofences, List<PCFPushGeofenceData> newGeofences) {
        requiredGeofences.addFiltered(newGeofences, new PCFPushGeofenceDataList.Filter() {

            @Override
            public boolean filterItem(PCFPushGeofenceData item) {
                return isItemValid(item);
            }

            private boolean isItemValid(PCFPushGeofenceData item) {
                if (isExpiredItem(item)) return false;
                if (item.getLocations() == null || item.getLocations().size() <= 0) return false;
                if (item.getData() == null || item.getData().size() <= 0) return false;
                if (item.getTriggerType() == null) return false;
                return true;
            }
        });
    }

    private boolean isExpiredItem(PCFPushGeofenceData item) {
        if (item.getExpiryTime() == null) return true;
        if (item.getExpiryTime().getTime() <= timeProvider.currentTimeMillis()) {
            Logger.i("Geofence with ID " + item.getId() + " has expired.");
            return true;
        }
        return false;
    }
}
