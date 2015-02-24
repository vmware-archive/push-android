/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.backend.geofence;

import io.pivotal.android.push.model.geofence.PCFPushGeofenceResponseData;

public interface PCFPushGetGeofenceUpdatesListener {
    void onPCFPushGetGeofenceUpdatesSuccess(PCFPushGeofenceResponseData responseData);
    void onPCFPushGetGeofenceUpdatesFailed(String reason);
}
