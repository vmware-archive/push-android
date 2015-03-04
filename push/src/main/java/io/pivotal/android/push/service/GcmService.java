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
import com.google.android.gms.location.GeofencingEvent;

import io.pivotal.android.push.receiver.GcmBroadcastReceiver;
import io.pivotal.android.push.util.Logger;

public class GcmService extends IntentService {

    public static final String KEY_MESSAGE = "message";

    public GcmService() {
        super("GcmService");
    }

    @Override
    protected final void onHandleIntent(Intent intent) {
        Logger.fd("GcmService has received a push message from GCM.");

        try {
            if (intent != null) {
                onReceive(intent);
            }
        } finally {
            if (intent != null && !GeofenceService.isGeofenceUpdate(this, intent)) {
                GcmBroadcastReceiver.completeWakefulIntent(intent);
            }
        }
    }

    public void onReceive(Intent intent) {
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
        final GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        for (final Geofence geofence : event.getTriggeringGeofences()) {

            if (event.getGeofenceTransition() == Geofence.GEOFENCE_TRANSITION_ENTER) {
                Logger.i("Entered geofence " + geofence);
                onGeofenceEnter(intent.getExtras());

            } else if (event.getGeofenceTransition() == Geofence.GEOFENCE_TRANSITION_EXIT) {

                Logger.i("Exited geofence " + geofence);
                onGeofenceExit(intent.getExtras());
            }
        }
    }

    public void onReceiveMessage(final Bundle payload) {}

    public void onReceiveMessageDeleted(final Bundle payload) {}

    public void onReceiveMessageSendError(final Bundle payload) {}

    public void onGeofenceEnter(final Bundle payload) {}

    public void onGeofenceExit(final Bundle payload) {}

    public static boolean isGeofencingEvent(Intent intent) {
        final GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        final boolean hasError = geofencingEvent.hasError();
        final boolean isEmpty = geofencingEvent.getTriggeringGeofences() == null || geofencingEvent.getTriggeringGeofences().isEmpty();
        return hasError || !isEmpty;
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
