package io.pivotal.android.push.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Bundle;

import com.google.android.gms.location.Geofence;

import java.util.Map;

import io.pivotal.android.push.analytics.AnalyticsEventLogger;
import io.pivotal.android.push.backend.geofence.PCFPushGetGeofenceUpdatesApiRequest;
import io.pivotal.android.push.geofence.GeofenceEngine;
import io.pivotal.android.push.geofence.GeofencePersistentStore;
import io.pivotal.android.push.geofence.GeofenceRegistrar;
import io.pivotal.android.push.geofence.GeofenceUpdater;
import io.pivotal.android.push.model.geofence.PCFPushGeofenceData;
import io.pivotal.android.push.model.geofence.PCFPushGeofenceLocation;
import io.pivotal.android.push.model.geofence.PCFPushGeofenceLocationMap;
import io.pivotal.android.push.prefs.PushPreferencesProvider;
import io.pivotal.android.push.prefs.PushPreferencesProviderImpl;
import io.pivotal.android.push.receiver.GeofenceBroadcastReceiver;
import io.pivotal.android.push.util.FileHelper;
import io.pivotal.android.push.util.GeofenceHelper;
import io.pivotal.android.push.util.Logger;
import io.pivotal.android.push.util.NetworkWrapper;
import io.pivotal.android.push.util.NetworkWrapperImpl;
import io.pivotal.android.push.util.ServiceStarter;
import io.pivotal.android.push.util.ServiceStarterImpl;
import io.pivotal.android.push.util.TimeProvider;

public class GeofenceService extends IntentService {

    public static final String GEOFENCE_AVAILABLE = "pivotal.push.geofence_update_available";
    public static final String GEOFENCE_UPDATE_JSON = "pivotal.push.geofence_update_json";

    private PCFPushGetGeofenceUpdatesApiRequest apiRequest;
    private GeofenceEngine geofenceEngine;
    private PushPreferencesProvider pushPreferencesProvider;
    private GeofenceHelper helper;
    private GeofencePersistentStore store;
    private AnalyticsEventLogger eventLogger;

    public GeofenceService() {
        super("GeofenceService");
    }

    // Intended to be overridden by application
    public void onGeofenceEnter(final Bundle payload) {}

    // Intended to be overridden by application
    public void onGeofenceExit(final Bundle payload) {}

    public boolean isGeofencingEvent(Intent intent) {
        return (intent != null && helper.isGeofencingEvent());
    }

    public static Class<?> getGeofenceServiceClass(final Context context) {
        try {
            final Class<?> klass = GeofenceService.findServiceClassName(context);
            if (klass != null) return klass;
        } catch (Exception e) {
            Logger.ex(e);
        }

        return GeofenceService.class;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Logger.setup(this);

        try {
            if (pushPreferencesProvider == null) {
                pushPreferencesProvider = new PushPreferencesProviderImpl(this);
            }
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
                GeofenceBroadcastReceiver.completeWakefulIntent(intent);
            }
        }
    }

    // Used by unit tests
    void setGetGeofenceUpdatesApiRequest(PCFPushGetGeofenceUpdatesApiRequest apiRequest) {
        this.apiRequest = apiRequest;
    }

    // Used by unit tests
    void setGeofenceEngine(GeofenceEngine geofenceEngine) {
        this.geofenceEngine = geofenceEngine;
    }

    // Used by unit tests
    void setPushPreferencesProvider(PushPreferencesProvider preferences) {
        this.pushPreferencesProvider = preferences;
    }

    // Used by unit tests
    void setGeofenceHelper(GeofenceHelper helper) {
        this.helper = helper;
    }

    // Used by unit tests
    void setGeofencePersistentStore(GeofencePersistentStore store) {
        this.store = store;
    }

    // Used by unit tests
    void setEventLogger(AnalyticsEventLogger eventLogger) {
        this.eventLogger = eventLogger;
    }

    private void onReceive(Intent intent) {
        final Bundle extras = intent.getExtras();

        if (extras != null && !extras.isEmpty()) {
            initializeDependencies(intent);
            if (isGeofencingEvent(intent)) {
                handleGeofenceMessage(intent);
            } else if (isPushGeofenceUpdate(intent)) {
                final GeofenceUpdater updater = new GeofenceUpdater(this, apiRequest, geofenceEngine, pushPreferencesProvider);
                final long timestamp = pushPreferencesProvider.getLastGeofenceUpdate();
                updater.startGeofenceUpdate(intent, timestamp, null);
            }
        }
    }

    private void initializeDependencies(Intent intent) {
        if (apiRequest == null) {
            final NetworkWrapper networkWrapper = new NetworkWrapperImpl();
            apiRequest = new PCFPushGetGeofenceUpdatesApiRequest(this, networkWrapper);
        }
        if (helper == null) {
            helper = new GeofenceHelper(intent);
        }
        if (store == null) {
            final FileHelper fileHelper = new FileHelper(this);
            store = new GeofencePersistentStore(this, fileHelper);
        }
        if (pushPreferencesProvider == null) {
            pushPreferencesProvider = new PushPreferencesProviderImpl(this);
        }
        if (geofenceEngine == null) {
            final GeofenceRegistrar registrar = new GeofenceRegistrar(this);
            final TimeProvider timeProvider = new TimeProvider();
            geofenceEngine = new GeofenceEngine(registrar, store, timeProvider, pushPreferencesProvider);
        }
        if (eventLogger == null) {
            final ServiceStarter serviceStarter = new ServiceStarterImpl();
            eventLogger = new AnalyticsEventLogger(serviceStarter, pushPreferencesProvider, this);
        }
    }

    private void handleGeofenceMessage(Intent intent) {
        final boolean areGeofencesEnabled = pushPreferencesProvider.areGeofencesEnabled();

        if (areGeofencesEnabled) {
            handleGeofencingEvent(intent);
        } else {
            Logger.i("Ignoring message. Geofences are disabled.");
        }
    }

    private void handleGeofencingEvent(Intent intent) {
        Logger.d("handleGeofencingEvent: " + intent);
        final PCFPushGeofenceLocationMap locationsToClear = new PCFPushGeofenceLocationMap();

        for (final Geofence geofence : helper.getGeofences()) {
            processGeofence(locationsToClear, geofence);
        }

        if (!locationsToClear.isEmpty()) {
            geofenceEngine.clearLocations(locationsToClear);
        }
    }

    private void processGeofence(PCFPushGeofenceLocationMap locationsToClear, Geofence geofence) {
        final String requestId = geofence.getRequestId();
        if (requestId == null) {
            Logger.e("Triggered geofence is missing a request ID: " + geofence);
            return;
        }

        final long geofenceId = PCFPushGeofenceLocationMap.getGeofenceId(requestId);
        final long locationId = PCFPushGeofenceLocationMap.getLocationId(requestId);
        final PCFPushGeofenceData geofenceData = store.getGeofenceData(geofenceId);
        if (geofenceData == null) {
            Logger.e("Triggered geofence with ID " + geofenceId + " has no matching data in our persistent store.");
            return;
        }

        final Bundle bundleData = getGeofenceBundle(geofenceId, requestId, geofenceData);
        if (bundleData == null) {
            return;
        }

        if (helper.getGeofenceTransition() == Geofence.GEOFENCE_TRANSITION_ENTER) {

            try {
                Logger.i("Entered geofence: " + geofence);
                eventLogger.logGeofenceTriggered(String.valueOf(geofenceId), String.valueOf(locationId));
                onGeofenceEnter(bundleData);
            } catch (Exception e) {
                Logger.ex("Caught exception in user code", e);
            }

        } else if (helper.getGeofenceTransition() == Geofence.GEOFENCE_TRANSITION_EXIT) {

            try {
                Logger.i("Exited geofence: " + geofence);
                eventLogger.logGeofenceTriggered(String.valueOf(geofenceId), String.valueOf(locationId));
                onGeofenceExit(bundleData);
            } catch (Exception e) {
                Logger.ex("Caught exception in user code", e);
            }
        }

        addLocationToClear(locationsToClear, requestId, geofenceData);
    }

    private Bundle getGeofenceBundle(long geofenceId, String requestId, PCFPushGeofenceData geofenceData) {

        if (geofenceData.getPayload() == null) {
            Logger.e("Triggered geofence with ID " + geofenceId + " has no message payload.");
            return null;
        }

        final Map<String, String> data = geofenceData.getPayload().getAndroidFcm();
        if (data == null) {
            Logger.e("Triggered geofence with ID " + geofenceId + " has no Android message payload.");
            return null;
        }

        final Bundle bundle = new Bundle();
        for (final Map.Entry<String, String> entry : data.entrySet()) {
            bundle.putString(entry.getKey(), entry.getValue());
        }
        bundle.putString("PCF_GEOFENCE_ID", requestId);

        return bundle;
    }

    private void addLocationToClear(PCFPushGeofenceLocationMap locationsToClear, String requestId, PCFPushGeofenceData geofenceData) {
        final long locationId = PCFPushGeofenceLocationMap.getLocationId(requestId);
        final PCFPushGeofenceLocation location = geofenceData.getLocationWithId(locationId);
        if (location != null) {
            locationsToClear.putLocation(geofenceData, location);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private boolean isPushGeofenceUpdate(Intent intent) {
        final Bundle extras = intent.getExtras();
        return extras != null &&
                extras.containsKey(GeofenceService.GEOFENCE_AVAILABLE) &&
                extras.getString(GeofenceService.GEOFENCE_AVAILABLE) != null &&
                extras.getString(GeofenceService.GEOFENCE_AVAILABLE).equals("true");
    }

    private static Class<?> findServiceClassName(final Context context) throws PackageManager.NameNotFoundException, ClassNotFoundException {
        final PackageManager manager = context.getPackageManager();
        final PackageInfo info = manager.getPackageInfo(context.getPackageName(), PackageManager.GET_SERVICES);
        final ServiceInfo[] services = info.services;

        if (services != null) {
            for (int i =0; i < services.length; i++) {
                final Class<?> klass = Class.forName(services[i].name);
                if (GeofenceService.class.isAssignableFrom(klass)) {
                    return klass;
                }
            }
        }
        return null;
    }

}
