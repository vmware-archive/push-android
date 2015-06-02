package io.pivotal.android.push.geofence;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import io.pivotal.android.push.model.geofence.PCFPushGeofenceData;
import io.pivotal.android.push.model.geofence.PCFPushGeofenceDataList;
import io.pivotal.android.push.model.geofence.PCFPushGeofenceLocation;
import io.pivotal.android.push.model.geofence.PCFPushGeofenceLocationMap;
import io.pivotal.android.push.service.GcmService;
import io.pivotal.android.push.util.DebugUtil;
import io.pivotal.android.push.util.Logger;
import io.pivotal.android.push.util.Util;
import io.pivotal.android.push.version.GeofenceStatus;

public class GeofenceRegistrar {


    private static final Object lock = new Object();
    private Context context;

    public GeofenceRegistrar(Context context) {
        if (context instanceof Application) {
            this.context = context;
        } else {
            this.context = context.getApplicationContext();
        }
    }

    public void reset() {
        final List<Geofence> emptyGeofencesToRegister = new LinkedList<>();
        final List<Map<String, String>> emptySerializableGeofences = new LinkedList<>();
        monitorGeofences(emptyGeofencesToRegister, emptySerializableGeofences);
    }

    public void registerGeofences(PCFPushGeofenceLocationMap geofencesToRegister, PCFPushGeofenceDataList geofenceDataList) {
        if (geofencesToRegister == null || geofenceDataList == null) {
            return;
        }

        final List<Map<String, String>> serializableList = initializeSerializableGeofencesList();

        final List<Geofence> list = new ArrayList<>(geofencesToRegister.size());
        for (final PCFPushGeofenceLocationMap.LocationEntry entry : geofencesToRegister.locationEntrySet()) {
            list.add(makeGeofence(entry, geofenceDataList));
            if (serializableList != null) {
                serializableList.add(makeSerializableGeofence(entry, entry.getGeofenceData(geofenceDataList).getExpiryTime()));
            }
        }

        monitorGeofences(list, serializableList);
    }

    private List<Map<String, String>> initializeSerializableGeofencesList() {
        if (DebugUtil.getInstance(context).isDebuggable()) {
            return new LinkedList<>();
        } else {
            return null;
        }
    }

    private Geofence makeGeofence(PCFPushGeofenceLocationMap.LocationEntry entry, PCFPushGeofenceDataList geofenceDataList) {
        final PCFPushGeofenceData geofenceData = entry.getGeofenceData(geofenceDataList);
        final PCFPushGeofenceLocation geofenceLocation = entry.getLocation();

        return new Geofence.Builder()
                .setCircularRegion(geofenceLocation.getLatitude(), geofenceLocation.getLongitude(), geofenceLocation.getRadius())
                .setRequestId(PCFPushGeofenceLocationMap.getAndroidRequestId(entry))
                .setTransitionTypes(getTransitionTypes(geofenceData.getTriggerType()))
                .setExpirationDuration(getExpiryDuration(geofenceData.getExpiryTime()))
                .build();
    }

    private Map<String, String> makeSerializableGeofence(PCFPushGeofenceLocationMap.LocationEntry entry, Date expiry) {
        final PCFPushGeofenceLocation geofenceLocation = entry.getLocation();
        final Map<String, String> serializableItem = new TreeMap<>();

        serializableItem.put("lat", String.valueOf(geofenceLocation.getLatitude()));
        serializableItem.put("long", String.valueOf(geofenceLocation.getLongitude()));
        serializableItem.put("rad", String.valueOf(geofenceLocation.getRadius()));
        serializableItem.put("name", String.valueOf(geofenceLocation.getName()));
        serializableItem.put("expiry", String.valueOf(expiry.getTime()));

        return serializableItem;
    }

    private int getTransitionTypes(String triggerType) {
        if (triggerType.equalsIgnoreCase("enter")) {
            return Geofence.GEOFENCE_TRANSITION_ENTER;
        } else if (triggerType.equalsIgnoreCase("exit")) {
            return Geofence.GEOFENCE_TRANSITION_EXIT;
        } else {
            return 0;
        }
    }

    private long getExpiryDuration(Date expiryTime) {
        if (expiryTime == null) {
            return Geofence.NEVER_EXPIRE;
        }
        return Math.max(0L, expiryTime.getTime() - new Date().getTime());
    }

    private void monitorGeofences(final List<Geofence> geofences, final List<Map<String, String>> serializableGeofences) {

        if (geofences == null) {
            return;
        }

        Logger.i("Connecting to GoogleApiClient.");

        final GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {

                @Override
                public void onConnected(Bundle bundle) {
                    Logger.i("GoogleApiClient connected.");
                }

                @Override
                public void onConnectionSuspended(int cause) {
                    Logger.w("GoogleApiClient Connection Suspended: cause:" + cause);

                    // TODO - what to do if the connection is suspended? - Google seems to suggest that the connection will be automatically reestablished so we might not need to do anything special at all.
                }
            })
            .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(ConnectionResult connectionResult) {
                    Logger.e("GoogleApiClient Connection Failed: errorCode:" + connectionResult.getErrorCode());
                }
            })
            .build();

        synchronized (lock) {
            final ConnectionResult connectionResult = googleApiClient.blockingConnect();
            if (connectionResult.isSuccess()) {
                handleMonitorGeofences(geofences, serializableGeofences, googleApiClient);
            } else {
                final String errorReason = "GoogleApiClient.blockingConnect returned status " + connectionResult.getErrorCode();
                Logger.e(errorReason);

                final GeofenceStatus geofenceStatus = new GeofenceStatus(true, errorReason, 0);
                final GeofenceStatusUtil geofenceStatusUtil = new GeofenceStatusUtil(context);
                geofenceStatusUtil.saveGeofenceStatusAndSendBroadcast(geofenceStatus);
            }
            googleApiClient.disconnect();
        }
    }

    private void handleMonitorGeofences(final List<Geofence> geofences, final List<Map<String, String>> serializableGeofences, final GoogleApiClient googleApiClient) {
        final Class<?> gcmServiceClass = GcmService.getGcmServiceClass(context);
        final Intent intent = new Intent(context, gcmServiceClass);
        final PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        final Status removeGeofencesStatus = LocationServices.GeofencingApi.removeGeofences(googleApiClient, pendingIntent).await();

        GeofenceStatus resultantStatus;

        if (removeGeofencesStatus.isSuccess()) {
            Logger.i("Success: removed currently monitored geofences.");
        } else {
            Logger.w("Was not able to remove currently monitored geofences: Status code: " + removeGeofencesStatus.getStatusCode());
        }

        if (!geofences.isEmpty()) {
            final Status addGeofencesStatus = LocationServices.GeofencingApi.addGeofences(googleApiClient, geofences, pendingIntent).await();

            if (addGeofencesStatus.isSuccess()) {
                Logger.i("Success: Now monitoring for " + geofences.size() + " geofences.");
                resultantStatus = new GeofenceStatus(false, null, geofences.size());
            } else {
                Logger.e("Error trying to monitor geofences. Status: " + addGeofencesStatus.getStatusCode());
                resultantStatus = new GeofenceStatus(true, "LocationServices.GeofencingApi returned status " + addGeofencesStatus.getStatusCode(), 0);
            }

        } else {
            Logger.i("Geofences to monitor is empty. Exiting.");
            resultantStatus = GeofenceStatus.emptyStatus();
        }

        updateDebugGeofencesFile(serializableGeofences);

        final GeofenceStatusUtil geofenceStatusUtil = new GeofenceStatusUtil(context);
        geofenceStatusUtil.saveGeofenceStatusAndSendBroadcast(resultantStatus);
    }

    private void updateDebugGeofencesFile(List<Map<String, String>> serializableGeofences) {
        if (DebugUtil.getInstance(context).isDebuggable()) {
            // If in debug mode then save the geofences to a file on the filesystem and send
            // a broadcast so a test app is able to see the geofences.
            Util.saveJsonMapToFilesystem(context, serializableGeofences);
        }
    }
}
