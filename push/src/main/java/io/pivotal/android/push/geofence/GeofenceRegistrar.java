package io.pivotal.android.push.geofence;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
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

public class GeofenceRegistrar {

    public static final String GEOFENCE_UPDATE_BROADCAST = "io.pivotal.android.push.geofence.GeofenceRegistrar.Update";

    private final Context context;
    private GoogleApiClient googleApiClient;

    public GeofenceRegistrar(Context context) {
        this.context = context;
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
                serializableList.add(makeSerializableGeofence(entry));
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

    private Map<String, String> makeSerializableGeofence(PCFPushGeofenceLocationMap.LocationEntry entry) {
        final PCFPushGeofenceLocation geofenceLocation = entry.getLocation();
        final Map<String, String> serializableItem = new TreeMap<>();

        serializableItem.put("lat", String.valueOf(geofenceLocation.getLatitude()));
        serializableItem.put("long", String.valueOf(geofenceLocation.getLongitude()));
        serializableItem.put("rad", String.valueOf(geofenceLocation.getRadius()));
        serializableItem.put("name", String.valueOf(geofenceLocation.getName()));

        return serializableItem;
    }

    private int getTransitionTypes(PCFPushGeofenceData.TriggerType triggerType) {
        switch (triggerType) {
            case ENTER:
                return Geofence.GEOFENCE_TRANSITION_ENTER;
            case EXIT:
                return Geofence.GEOFENCE_TRANSITION_EXIT;
            case ENTER_OR_EXIT:
                return Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT;
            default:
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

        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {

                    @Override
                    public void onConnected(Bundle bundle) {
                        Logger.i("GoogleApiClient connected.");

                        final Class<?> gcmServiceClass = GcmService.getGcmServiceClass(context);
                        Logger.i("Da Clazz: " + gcmServiceClass.getCanonicalName());
                        Logger.i("Da Clazz: " + gcmServiceClass.toString());
                        final Intent intent = new Intent(context, gcmServiceClass);
                        final PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                        final PendingResult<Status> removeGeofencesResult = LocationServices.GeofencingApi.removeGeofences(googleApiClient, pendingIntent);
                        removeGeofencesResult.setResultCallback(new ResultCallback<Status>() {

                            @Override
                            public void onResult(Status status) {

                                if (status.isSuccess()) {
                                    Logger.i("Success: removed currently monitored geofences.");
                                } else {
                                    Logger.w("Was not able to remove currently monitored geofences: Status code: " + status.getStatusCode());
                                }

                                if (!geofences.isEmpty()) {
                                    final PendingResult<Status> addGeofencesResult = LocationServices.GeofencingApi.addGeofences(googleApiClient, geofences, pendingIntent);
                                    addGeofencesResult.setResultCallback(new ResultCallback<Status>() {

                                        @Override
                                        public void onResult(Status status) {

                                            if (status.isSuccess()) {
                                                Logger.i("Success: Now monitoring for " + geofences.size() + " geofences.");
                                            } else {
                                                Logger.e("Error trying to monitor geofences. Status code: " + status.getStatusCode());
                                            }

                                            updateDebugGeofencesFile(serializableGeofences);

                                            googleApiClient.disconnect();
                                            googleApiClient = null;
                                        }
                                    });
                                } else {

                                    updateDebugGeofencesFile(serializableGeofences);

                                    googleApiClient.disconnect();
                                    googleApiClient = null;
                                }
                            }
                        });

                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                        Logger.w("GoogleApiClient Connection Suspended: cause:" + cause);

                        // TODO - what to do if the connection is suspended?
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Logger.e("GoogleApiClient Connection Failed: errorCode:" + connectionResult.getErrorCode());
                        if (googleApiClient.isConnected()) {
                            googleApiClient.disconnect();
                        }
                        googleApiClient = null;
                    }
                })
                .build();

        googleApiClient.blockingConnect();
    }

    private void updateDebugGeofencesFile(List<Map<String, String>> serializableGeofences) {
        if (DebugUtil.getInstance(context).isDebuggable()) {
            // If in debug mode then save the geofences to a file on the filesystem and send
            // a broadcast so a test app is able to see the geofences.
            Util.saveJsonMapToFilesystem(context, serializableGeofences);

            // TODO - consider adding a permission to this broadcast
            final Intent intent = new Intent(GEOFENCE_UPDATE_BROADCAST);
            context.sendBroadcast(intent);
        }
    }
}
