package io.pivotal.android.push.service;

import android.app.IntentService;
import android.content.Intent;

import io.pivotal.android.push.receiver.GcmBroadcastReceiver;
import io.pivotal.android.push.util.Logger;

public class GeofenceService extends IntentService {

    public static final String GEOFENCE_AVAILABLE = "pivotal.push.geofence_update_available";

    public GeofenceService() {
        super("GeofenceService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Logger.fd("GeofenceService has received a silent push message from GCM.");

        try {
            if (intent != null) {
                onReceive(intent);
            }
        } finally {
            if (intent != null) {
                GcmBroadcastReceiver.completeWakefulIntent(intent);
            }
        }

    }

    private void onReceive(Intent intent) {

    }
}
