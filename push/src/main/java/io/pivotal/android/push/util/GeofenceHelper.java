package io.pivotal.android.push.util;

import android.content.Intent;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

import io.pivotal.android.push.service.GcmService;

public class GeofenceHelper {

    private final Intent intent;
    private final GeofencingEvent event;

    public GeofenceHelper(Intent intent) {
        this.intent = intent;
        this.event = GeofencingEvent.fromIntent(intent);
    }

    public boolean isGeofencingEvent() {
        return (intent != null && intent.hasExtra(GcmService.GEOFENCE_TRANSITION_KEY));
    }

    public int getGeofenceTransition() {
        return event.getGeofenceTransition();
    }

    public List<Geofence> getGeofences() {
        return event.getTriggeringGeofences();
    }
}
