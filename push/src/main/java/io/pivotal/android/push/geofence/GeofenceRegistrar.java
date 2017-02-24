package io.pivotal.android.push.geofence;

import io.pivotal.android.push.model.geofence.PCFPushGeofenceDataList;
import io.pivotal.android.push.model.geofence.PCFPushGeofenceLocationMap;

public interface GeofenceRegistrar {
    void reset();
    void registerGeofences(PCFPushGeofenceLocationMap geofencesToRegister, PCFPushGeofenceDataList geofenceDataList);
}
