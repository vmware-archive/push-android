package io.pivotal.android.push.geofence;

import android.content.Context;

public class GeofenceFactoryImpl implements GeofenceFactory {
    @Override
    public GeofenceRegistrar getGeofenceRegistrar(Context context) {
        return new GeofenceFactoryImpl().getGeofenceRegistrar(context);
    }
}
