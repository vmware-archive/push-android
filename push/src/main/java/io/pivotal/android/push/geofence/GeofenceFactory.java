package io.pivotal.android.push.geofence;

import android.content.Context;

public interface GeofenceFactory {
    GeofenceRegistrar getGeofenceRegistrar(Context context);
}
