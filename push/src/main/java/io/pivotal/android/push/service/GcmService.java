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

import io.pivotal.android.push.geofence.GeofencePersistentStore;
import io.pivotal.android.push.model.geofence.PCFPushGeofenceData;
import io.pivotal.android.push.model.geofence.PCFPushGeofenceLocationMap;
import io.pivotal.android.push.receiver.GcmBroadcastReceiver;
import io.pivotal.android.push.util.FileHelper;
import io.pivotal.android.push.util.GeofenceHelper;
import io.pivotal.android.push.util.Logger;

public class GcmService extends IntentService {

    public static final String GEOFENCE_TRANSITION_KEY = "com.google.android.location.intent.extra.transition";
    public static final String KEY_MESSAGE = "message";

    private GeofenceHelper helper;
    private GeofencePersistentStore store;

    public GcmService() {
        super("GcmService");
    }

    /* package */ void setGeofenceHelper(GeofenceHelper helper) {
        this.helper = helper;
    }

    /* package */ void setGeofencePersistentStore(GeofencePersistentStore store) {
        this.store = store;
    }

    @Override
    protected final void onHandleIntent(Intent intent) {
        Logger.fd("GcmService has received a push message from GCM.");

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
        if (GeofenceService.isGeofenceUpdate(this, intent)) {
            handleGeofenceUpdate(intent);

        } else if (isGeofencingEvent(intent)) {
            handleGeofencingEvent(intent);

        } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
            onReceiveMessage(extras);

        } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
            onReceiveMessageDeleted(extras);

        } else if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
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
        for (final Geofence geofence : helper.getGeofences()) {

            if (helper.getGeofenceTransition() == Geofence.GEOFENCE_TRANSITION_ENTER) {

                Logger.i("Entered geofence: " + geofence);
                final Bundle data = getGeofenceBundle(geofence);
                if (data != null) {
                    onGeofenceEnter(data);
                }

            } else if (helper.getGeofenceTransition() == Geofence.GEOFENCE_TRANSITION_EXIT) {

                Logger.i("Exited geofence: " + geofence);
                final Bundle data = getGeofenceBundle(geofence);
                if (data != null) {
                    onGeofenceExit(data);
                }
            }
        }
    }

    private Bundle getGeofenceBundle(Geofence geofence) {
        final String requestId = geofence.getRequestId();
        if (requestId == null) {
            Logger.e("Triggered geofence is missing a request ID: " + geofence);
            return null;
        }

        final long geofenceId = PCFPushGeofenceLocationMap.getGeofenceId(requestId);
        final PCFPushGeofenceData geofenceData = store.getGeofenceData(geofenceId);
        if (geofenceData == null) {
            Logger.e("Triggered geofence with ID " + geofenceId + " has no matching data in our persistent store.");
            return null;
        }

        final Map<String, String> data = geofenceData.getData();
        if (data == null) {
            Logger.e("Triggered geofence with ID " + geofenceId + " has no message data.");
            return null;
        }

        final Bundle result = new Bundle();
        for (final Map.Entry<String, String> entry : data.entrySet()) {
            result.putString(entry.getKey(), entry.getValue());
        }
        return result;
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
