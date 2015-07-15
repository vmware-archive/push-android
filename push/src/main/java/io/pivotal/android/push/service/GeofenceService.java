package io.pivotal.android.push.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import io.pivotal.android.push.backend.geofence.PCFPushGetGeofenceUpdatesApiRequest;
import io.pivotal.android.push.geofence.GeofenceEngine;
import io.pivotal.android.push.geofence.GeofencePersistentStore;
import io.pivotal.android.push.geofence.GeofenceRegistrar;
import io.pivotal.android.push.geofence.GeofenceUpdater;
import io.pivotal.android.push.prefs.PushPreferencesProvider;
import io.pivotal.android.push.prefs.PushPreferencesProviderImpl;
import io.pivotal.android.push.receiver.GcmBroadcastReceiver;
import io.pivotal.android.push.util.FileHelper;
import io.pivotal.android.push.util.Logger;
import io.pivotal.android.push.util.NetworkWrapper;
import io.pivotal.android.push.util.NetworkWrapperImpl;
import io.pivotal.android.push.util.TimeProvider;

public class GeofenceService extends IntentService {

    public static final String GEOFENCE_AVAILABLE = "pivotal.push.geofence_update_available";
    public static final String GEOFENCE_UPDATE_JSON = "pivotal.push.geofence_update_json";

    private PCFPushGetGeofenceUpdatesApiRequest apiRequest;
    private GeofenceEngine geofenceEngine;
    private PushPreferencesProvider pushPreferencesProvider;

    public GeofenceService() {
        super("GeofenceService");
    }

    // Used by unit tests
    /* package */ void setGetGeofenceUpdatesApiRequest(PCFPushGetGeofenceUpdatesApiRequest apiRequest) {
        this.apiRequest = apiRequest;
    }

    // Used by unit tests
    /* package */ void setGeofenceEngine(GeofenceEngine geofenceEngine) {
        this.geofenceEngine = geofenceEngine;
    }

    // Used by unit tests
    /* package */ void setPushPreferencesProvider(PushPreferencesProvider preferences) {
        this.pushPreferencesProvider = preferences;
    }

    public static boolean isGeofenceUpdate(Context context, Intent intent) {
        final GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
        final String messageType = gcm.getMessageType(intent);
        final Bundle extras = intent.getExtras();
        return extras != null &&
                extras.containsKey(GeofenceService.GEOFENCE_AVAILABLE) &&
                extras.getString(GeofenceService.GEOFENCE_AVAILABLE) != null &&
                extras.getString(GeofenceService.GEOFENCE_AVAILABLE).equals("true") &
                GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Logger.setup(this);

        try {
            if (intent != null && pushPreferencesProvider.areGeofencesEnabled())  {
                if (intent.getAction() != null) {
                    Logger.d("GeofenceService has received an intent: " + intent.getAction());
                } else {
                    Logger.d("GeofenceService has received an intent with no action");
                }
                onReceive(intent);
            }
        } finally {
            if (intent != null) {
                GcmBroadcastReceiver.completeWakefulIntent(intent);
            }
        }
    }

    private void onReceive(Intent intent) {

        if (isGeofenceUpdate(this, intent)) {
            instantiateDependencies();
            final GeofenceUpdater updater = new GeofenceUpdater(this, apiRequest, geofenceEngine, pushPreferencesProvider);
            final long timestamp = pushPreferencesProvider.getLastGeofenceUpdate();
            updater.startGeofenceUpdate(intent, timestamp, null);
        }
    }

    private void instantiateDependencies() {
        if (pushPreferencesProvider == null) {
            pushPreferencesProvider = new PushPreferencesProviderImpl(this);
        }
        if (apiRequest == null) {
            final NetworkWrapper networkWrapper = new NetworkWrapperImpl();
            apiRequest = new PCFPushGetGeofenceUpdatesApiRequest(this, networkWrapper);
        }
        if (geofenceEngine == null) {
            final FileHelper fileHelper = new FileHelper(getApplicationContext());
            final GeofenceRegistrar registrar = new GeofenceRegistrar(this);
            final GeofencePersistentStore store = new GeofencePersistentStore(this, fileHelper);
            final TimeProvider timeProvider = new TimeProvider();
            geofenceEngine = new GeofenceEngine(registrar, store, timeProvider, pushPreferencesProvider);
        }
    }

}
