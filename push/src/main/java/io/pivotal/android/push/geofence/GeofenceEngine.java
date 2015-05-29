package io.pivotal.android.push.geofence;

import java.util.List;

import io.pivotal.android.push.model.geofence.PCFPushGeofenceData;
import io.pivotal.android.push.model.geofence.PCFPushGeofenceDataList;
import io.pivotal.android.push.model.geofence.PCFPushGeofenceLocation;
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

        final PCFPushGeofenceDataList geofencesToStore = new PCFPushGeofenceDataList();
        final PCFPushGeofenceLocationMap geofencesToRegister = new PCFPushGeofenceLocationMap();
        addValidGeofencesFromStore(geofencesToStore, storedGeofences, responseData);
        addValidGeofencesFromUpdate(geofencesToStore, responseData.getGeofences());
        geofencesToRegister.addAll(geofencesToStore);

        Logger.i("GeofenceEngine: going to register " + geofencesToRegister.size() + " geofences.");
        registrar.registerGeofences(geofencesToRegister, geofencesToStore);
        store.saveRegisteredGeofences(geofencesToStore);
    }

    public void reregisterCurrentLocations() {
        final PCFPushGeofenceDataList geofenceDataList = store.getCurrentlyRegisteredGeofences();
        final PCFPushGeofenceLocationMap geofencesToRegister = new PCFPushGeofenceLocationMap();
        geofencesToRegister.addAll(geofenceDataList);
        registrar.registerGeofences(geofencesToRegister, geofenceDataList);
    }

    public void clearLocations(final PCFPushGeofenceLocationMap locationsToClear) {

        if (locationsToClear == null || locationsToClear.size() == 0) {
            return;
        }

        final PCFPushGeofenceDataList storedGeofences = store.getCurrentlyRegisteredGeofences();
        final PCFPushGeofenceDataList geofencesToStore = new PCFPushGeofenceDataList();
        final PCFPushGeofenceLocationMap geofencesToRegister = new PCFPushGeofenceLocationMap();

        filterClearedLocations(locationsToClear, storedGeofences, geofencesToStore, geofencesToRegister);

        Logger.i("GeofenceEngine: going to register " + geofencesToRegister.size() + " geofences.");
        registrar.registerGeofences(geofencesToRegister, geofencesToStore);
        store.saveRegisteredGeofences(geofencesToStore);
    }

    private boolean hasDataToPersist(PCFPushGeofenceResponseData responseData, PCFPushGeofenceDataList storedGeofences) {
        return (storedGeofences != null && storedGeofences.size() > 0) ||
               (responseData.getGeofences() != null && responseData.getGeofences().size() > 0);
    }

    private void addValidGeofencesFromStore(final PCFPushGeofenceDataList requiredGeofences, final PCFPushGeofenceDataList storedGeofences, final PCFPushGeofenceResponseData responseData) {
        requiredGeofences.addFiltered(storedGeofences, new PCFPushGeofenceDataList.Filter() {

            @Override
            public boolean filterItem(final PCFPushGeofenceData item) {
                return !isDeletedItem(item, responseData) &&
                       !isExpiredItem(item) &&
                       !isUpdatedItem(item, responseData) &&
                        areLocationsValid(item);
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

    private boolean isExpiredItem(PCFPushGeofenceData item) {
        if (item.getExpiryTime() == null) return true;
        if (item.getExpiryTime().getTime() <= timeProvider.currentTimeMillis()) {
            Logger.i("Geofence with ID " + item.getId() + " has expired.");
            return true;
        }
        return false;
    }

    private boolean areLocationsValid(PCFPushGeofenceData item) {
        if (item.getLocations() == null || item.getLocations().size() <= 0) {
            return false;
        }
        for (final PCFPushGeofenceLocation location : item.getLocations()) {
            if (location.getLatitude() < -90.0 || location.getLatitude() > 90.0) {
                Logger.w(String.format("Filtering out item %d with bad latitude %f", item.getId(), location.getLatitude()));
                return false;
            }
            if (location.getLongitude() < -180.0 || location.getLongitude() > 180.0) {
                Logger.w(String.format("Filtering out item %d with bad longitude %f", item.getId(), location.getLongitude()));
                return false;
            }
            if (location.getRadius() < 10.0) {
                Logger.w(String.format("Filtering out item %d with bad radius %f", item.getId(), location.getRadius()));
                return false;
            }
        }
        return true;
    }

    private void addValidGeofencesFromUpdate(PCFPushGeofenceDataList requiredGeofences, List<PCFPushGeofenceData> newGeofences) {
        requiredGeofences.addFiltered(newGeofences, new PCFPushGeofenceDataList.Filter() {

            @Override
            public boolean filterItem(PCFPushGeofenceData item) {
                return isItemValid(item);
            }

            private boolean isItemValid(PCFPushGeofenceData item) {
                if (item.getExpiryTime() == null) {
                    Logger.w(String.format("Filtering out item %d with no expiry time", item.getId()));
                    return false;
                }
                if (isExpiredItem(item)) {
                    Logger.w(String.format("Filtering out item %d with elapsed expiry time %d", item.getId(), item.getExpiryTime().getTime()));
                    return false;
                }
                if (!areLocationsValid(item)) return false;
                if (item.getPayload() == null || item.getPayload().getAndroid() == null || item.getPayload().getAndroid().size() <= 0) {
                    Logger.w(String.format("Filtering out item %d with no payload", item.getId()));
                    return false;
                }
                if (item.getTriggerType() == null) {
                    Logger.w(String.format("Filtering out item %d with no trigger type", item.getId()));
                    return false;
                }
                if (!(item.getTriggerType().equalsIgnoreCase("enter") || item.getTriggerType().equalsIgnoreCase("exit"))) {
                    Logger.w(String.format("Filtering out item %d with invalid trigger type '%s'", item.getId(), item.getTriggerType()));
                    return false;
                }
                return true;
            }
        });
    }

    private void filterClearedLocations(final PCFPushGeofenceLocationMap locationsToClear,
                                        PCFPushGeofenceDataList storedGeofences,
                                        final PCFPushGeofenceDataList geofencesToStore,
                                        final PCFPushGeofenceLocationMap geofencesToRegister) {

        geofencesToRegister.addFiltered(storedGeofences, new PCFPushGeofenceLocationMap.Filter() {

            @Override
            public boolean filterItem(PCFPushGeofenceData item, PCFPushGeofenceLocation location) {

                if (item == null || location == null) {
                    return false;
                }

                final String requestId = getRequestId(item, location);

                if (shouldKeepLocation(requestId)) {
                    keepLocation(item, location, requestId);
                    return true;
                }
                return false;
            }

            private String getRequestId(PCFPushGeofenceData item, PCFPushGeofenceLocation location) {
                return PCFPushGeofenceLocationMap.getAndroidRequestId(item.getId(), location.getId());
            }

            private boolean shouldKeepLocation(String requestId) {
                return !locationsToClear.containsKey(requestId);
            }

            private void keepLocation(PCFPushGeofenceData item, PCFPushGeofenceLocation location, String requestId) {

                if (geofencesToStore.get(item.getId()) == null) {
                    final PCFPushGeofenceData newCopyWithoutLocations = item.newCopyWithoutLocations();
                    newCopyWithoutLocations.getLocations().add(location);
                    geofencesToStore.put(item.getId(), newCopyWithoutLocations);
                } else {
                    geofencesToStore.get(item.getId()).getLocations().add(location);
                }

                geofencesToRegister.put(requestId, location);
            }
        });
    }
}
