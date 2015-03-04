package io.pivotal.android.push.geofence;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

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
import io.pivotal.android.push.util.Logger;
import io.pivotal.android.push.util.Util;

public class GeofenceRegistrar {

    public static final String GEOFENCE_UPDATE_BROADCAST = "io.pivotal.android.push.geofence.GeofenceRegistrar.Update";
    public static final String GEOFENCES_KEY = "geofences";

    private final Context context;
    private GoogleApiClient googleApiClient;

    public GeofenceRegistrar(Context context) {
        this.context = context;
    }

    public void registerGeofences(PCFPushGeofenceLocationMap geofencesToRegister, PCFPushGeofenceDataList geofenceDataList) {
        if (geofencesToRegister == null) {
            return;
        }

        final List<Map<String, String>> serializableList = new LinkedList<>();
        final List<Geofence> list = new ArrayList<>(geofencesToRegister.size());
        for (final PCFPushGeofenceLocationMap.LocationEntry entry : geofencesToRegister.locationEntrySet()) {
            final Map<String, String> serializableItem = new TreeMap<>();
            final Geofence item = makeGeofence(entry, geofenceDataList, serializableItem);
            list.add(item);
            serializableList.add(serializableItem);
        }

        monitorGeofences(list);
        Util.saveJsonMapToFilesystem(context, serializableList);
        sendBroadcast(serializableList);
    }

    private Geofence makeGeofence(PCFPushGeofenceLocationMap.LocationEntry entry, PCFPushGeofenceDataList geofenceDataList, Map<String, String> serializableItem) {
        final PCFPushGeofenceData geofenceData = entry.getGeofenceData(geofenceDataList);
        final PCFPushGeofenceLocation geofenceLocation = entry.getLocation();

        // Check expiry?  Expired geofences shouldn't be passed to us.

        final Geofence geofence = new Geofence.Builder()
                .setCircularRegion(geofenceLocation.getLatitude(), geofenceLocation.getLongitude(), geofenceLocation.getRadius())
                .setRequestId(PCFPushGeofenceLocationMap.getAndroidRequestId(entry))
                .setTransitionTypes(getTransitionTypes(geofenceData.getTriggerType()))
                .setExpirationDuration(getExpiryDuration(geofenceData.getExpiryTime()))
                .build();

        serializableItem.put("lat", String.valueOf(geofenceLocation.getLatitude()));
        serializableItem.put("long", String.valueOf(geofenceLocation.getLongitude()));
        serializableItem.put("rad", String.valueOf(geofenceLocation.getRadius()));
        serializableItem.put("name", String.valueOf(geofenceLocation.getName()));

        return geofence;
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

    private void monitorGeofences(final List<Geofence> geofences) {
        if (geofences.isEmpty()) {
            return;
        }

        // TODO - remove currently monitored geofences before registering more.

        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Toast.makeText(context, "GoogleApiClient Connected", Toast.LENGTH_SHORT).show();

                        final Class<?> gcmServiceClass = GcmService.getGcmServiceClass(context);
                        final Intent intent = new Intent(context, gcmServiceClass);
                        final PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                        PendingResult<Status> result = LocationServices.GeofencingApi.addGeofences(googleApiClient, geofences, pendingIntent);
                        result.setResultCallback(new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status status) {

                                String toastMessage;
                                if (status.isSuccess()) {
                                    toastMessage = "Success: We Are Monitoring Our Fences";
                                    Logger.i("Now monitoring for geofences");
                                } else {
                                    toastMessage = "Error: We Are NOT Monitoring Our Fences";
                                    Logger.e("Error trying to monitor geofences. Status code: " + status.getStatusCode());
                                }
                                // TODO - remove this toast
                                Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        // TODO - remove this toast
                        Toast.makeText(context, "GoogleApiClient Connection Suspended", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        // TODO - remove this toast
                        Toast.makeText(context, "GoogleApiClient Connection Failed", Toast.LENGTH_SHORT).show();

                    }
                })
                .build();

        googleApiClient.connect();
    }

    private void sendBroadcast(List<Map<String, String>> list) {
        // TODO - consider adding a permission to this broadcast
        final Intent intent = new Intent(GEOFENCE_UPDATE_BROADCAST);
        context.sendBroadcast(intent);
    }
}
