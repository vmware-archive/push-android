/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Bundle;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.Geofence;

import java.util.Map;

import io.pivotal.android.push.analytics.AnalyticsEventLogger;
import io.pivotal.android.push.geofence.GeofenceEngine;
import io.pivotal.android.push.geofence.GeofencePersistentStore;
import io.pivotal.android.push.geofence.GeofenceRegistrar;
import io.pivotal.android.push.model.geofence.PCFPushGeofenceData;
import io.pivotal.android.push.model.geofence.PCFPushGeofenceLocation;
import io.pivotal.android.push.model.geofence.PCFPushGeofenceLocationMap;
import io.pivotal.android.push.prefs.PushPreferencesProvider;
import io.pivotal.android.push.prefs.PushPreferencesProviderImpl;
import io.pivotal.android.push.receiver.GcmBroadcastReceiver;
import io.pivotal.android.push.util.FileHelper;
import io.pivotal.android.push.util.GeofenceHelper;
import io.pivotal.android.push.util.Logger;
import io.pivotal.android.push.util.ServiceStarter;
import io.pivotal.android.push.util.ServiceStarterImpl;
import io.pivotal.android.push.util.TimeProvider;

public class GcmService extends IntentService {

    public static final String GEOFENCE_TRANSITION_KEY = "com.google.android.location.intent.extra.transition";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_RECEIPT_ID = "receiptId";
    public static final String KEY_HEARTBEAT = "pcf.push.heartbeat.sentAt";

    private GeofenceHelper helper;
    private GeofenceEngine engine;
    private GeofencePersistentStore store;
    private PushPreferencesProvider preferences;
    private AnalyticsEventLogger eventLogger;

    public GcmService() {
        super("GcmService");
    }

    /* package */ void setGeofenceHelper(GeofenceHelper helper) {
        this.helper = helper;
    }

    /* package */ void setGeofenceEngine(GeofenceEngine engine) {
        this.engine = engine;
    }

    /* package */ void setGeofencePersistentStore(GeofencePersistentStore store) {
        this.store = store;
    }

    /* package */ void setPushPreferencesProvider(PushPreferencesProvider preferences) {
        this.preferences = preferences;
    }

    /* package */ void setEventLogger(AnalyticsEventLogger eventLogger) {
        this.eventLogger = eventLogger;
    }

    @Override
    protected final void onHandleIntent(Intent intent) {
        Logger.setup(this);
        Logger.fd("GcmService has received an event.");

        try {

            if (intent != null) {
                initializeDependencies(intent);
                onReceive(intent);
            }
        } finally {
            if (intent != null && !GeofenceService.isGeofenceUpdate(this, intent)) {
                GcmBroadcastReceiver.completeWakefulIntent(intent);
            }
        }
    }

    private void initializeDependencies(Intent intent) {
        if (helper == null) {
            helper = new GeofenceHelper(intent);
        }
        if (store == null) {
            final FileHelper fileHelper = new FileHelper(this);
            store = new GeofencePersistentStore(this, fileHelper);
        }
        if (preferences == null) {
            preferences = new PushPreferencesProviderImpl(this);
        }
        if (engine == null) {
            final GeofenceRegistrar registrar = new GeofenceRegistrar(this);
            final TimeProvider timeProvider = new TimeProvider();
            engine = new GeofenceEngine(registrar, store, timeProvider, preferences);
        }
        if (eventLogger == null) {
            final ServiceStarter serviceStarter = new ServiceStarterImpl();
            eventLogger = new AnalyticsEventLogger(serviceStarter, preferences, this);
        }
    }

    private void onReceive(Intent intent) {
        final GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        final String messageType = gcm.getMessageType(intent);
        final Bundle extras = intent.getExtras();

        if (extras != null && !extras.isEmpty()) {
            handleMessage(intent, extras, messageType);
        }
    }

    private void handleMessage(Intent intent, Bundle extras, String messageType) {
        final boolean areGeofencesEnabled = preferences.areGeofencesEnabled();
        if (GeofenceService.isGeofenceUpdate(this, intent)) {
            if (areGeofencesEnabled) {
                handleGeofenceUpdate(intent);
            } else {
                Logger.i("Ignoring message. Geofences are disabled.");
            }

        } else if (isGeofencingEvent(intent)) {
            if (areGeofencesEnabled) {
                handleGeofencingEvent(intent);
            } else {
                Logger.i("Ignoring message. Geofences are disabled.");
            }

        } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
            if (extras.containsKey(KEY_HEARTBEAT)) {
                Logger.i("GcmService has received a heartbeat push message.");
                enqueueHeartbeatReceivedEvent(intent);

            } else {
                Logger.i("GcmService has received a push message.");
                enqueueMessageReceivedEvent(intent);
                onReceiveMessage(extras);
            }

        } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
            Logger.i("GcmService has received a DELETED push message.");
            onReceiveMessageDeleted(extras);

        } else if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
            Logger.e("GcmService has received an ERROR push message.");
            onReceiveMessageSendError(extras);
        }
    }

    private void handleGeofenceUpdate(Intent intent) {
        final Intent geofenceServiceIntent = new Intent(getBaseContext(), GeofenceService.class);
        geofenceServiceIntent.setAction(intent.getAction());
        geofenceServiceIntent.replaceExtras(intent);
        getBaseContext().startService(geofenceServiceIntent);
    }

    private void handleGeofencingEvent(Intent intent) {
        Logger.d("handleGeofencingEvent: " + intent);
        final PCFPushGeofenceLocationMap locationsToClear = new PCFPushGeofenceLocationMap();

        for (final Geofence geofence : helper.getGeofences()) {
            processGeofence(locationsToClear, geofence);
        }

        if (!locationsToClear.isEmpty()) {
            engine.clearLocations(locationsToClear);
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

        final Map<String, String> data = geofenceData.getPayload().getAndroid();
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

    private void enqueueMessageReceivedEvent(Intent intent) {
        final String receiptId = intent.getStringExtra(KEY_RECEIPT_ID);
        if (receiptId != null) {
            eventLogger.logReceivedNotification(receiptId);
        } else {
            Logger.w("Note: notification has no receiptId. No analytics event will be logged for receiving this notification.");
        }
    }

    private void enqueueHeartbeatReceivedEvent(Intent intent) {
        final String receiptId = intent.getStringExtra(KEY_RECEIPT_ID);
        if (receiptId != null) {
            eventLogger.logReceivedHeartbeat(receiptId);
        } else {
            Logger.w("Note: heartbeat has no receiptId. No analytics event will be logged for receiving this notification.");
        }

    }

    // Intended to be overridden by application
    public void onReceiveMessage(final Bundle payload) {}

    // Intended to be overridden by application
    public void onReceiveMessageDeleted(final Bundle payload) {}

    // Intended to be overridden by application
    public void onReceiveMessageSendError(final Bundle payload) {}

    // Intended to be overridden by application
    public void onGeofenceEnter(final Bundle payload) {}

    // Intended to be overridden by application
    public void onGeofenceExit(final Bundle payload) {}

    public boolean isGeofencingEvent(Intent intent) {
        return (intent != null && helper.isGeofencingEvent());
    }

    public static Class<?> getGcmServiceClass(final Context context) {
        try {
            final Class<?> klass = GcmService.findServiceClassName(context);
            if (klass != null) return klass;
        } catch (Exception e) {
            Logger.ex(e);
        }

        return GcmService.class;
    }

    private static Class<?> findServiceClassName(final Context context) throws PackageManager.NameNotFoundException, ClassNotFoundException {
        final PackageManager manager = context.getPackageManager();
        final PackageInfo info = manager.getPackageInfo(context.getPackageName(), PackageManager.GET_SERVICES);
        final ServiceInfo[] services = info.services;

        if (services != null) {
            for (int i =0; i < services.length; i++) {
                final Class<?> klass = Class.forName(services[i].name);
                if (GcmService.class.isAssignableFrom(klass)) {
                    return klass;
                }
            }
        }
        return null;
    }
}
