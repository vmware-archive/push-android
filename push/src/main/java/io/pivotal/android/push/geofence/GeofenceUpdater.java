package io.pivotal.android.push.geofence;

import android.content.Context;
import android.content.Intent;

import io.pivotal.android.push.PushParameters;
import io.pivotal.android.push.backend.geofence.PCFPushGetGeofenceUpdatesApiRequest;
import io.pivotal.android.push.backend.geofence.PCFPushGetGeofenceUpdatesListener;
import io.pivotal.android.push.model.geofence.PCFPushGeofenceResponseData;
import io.pivotal.android.push.prefs.Pivotal;
import io.pivotal.android.push.prefs.PushPreferencesProvider;
import io.pivotal.android.push.service.GeofenceService;
import io.pivotal.android.push.util.DebugUtil;
import io.pivotal.android.push.util.GsonUtil;
import io.pivotal.android.push.util.Logger;
import io.pivotal.android.push.version.GeofenceStatus;

public class GeofenceUpdater {

    private final Context context;
    private final PCFPushGetGeofenceUpdatesApiRequest apiRequest;
    private final GeofenceEngine geofenceEngine;
    private final PushPreferencesProvider pushPreferencesProvider;

    public interface GeofenceUpdaterListener {
        public void onSuccess();
        public void onFailure(String reason);
    }

    public GeofenceUpdater(Context context, PCFPushGetGeofenceUpdatesApiRequest apiRequest, GeofenceEngine geofenceEngine, PushPreferencesProvider pushPreferencesProvider) {
        this.context = context;
        this.apiRequest = apiRequest;
        this.geofenceEngine = geofenceEngine;
        this.pushPreferencesProvider = pushPreferencesProvider;
    }

    public void startGeofenceUpdate(final Intent intent, final long timestamp, final GeofenceUpdaterListener listener) {

        if (intent != null) {
            Logger.d("Attempting to update geofences: " + intent + " timestamp: " + timestamp);
        } else {
            Logger.d("Attempting to update geofences with no intent, timestamp: " + timestamp);
        }

        if (intent != null && doesIntentProvideJson(intent) && DebugUtil.getInstance(context).isDebuggable()) {

            Logger.d("This update provides the list of geofences.");
            final String updateJson = intent.getStringExtra(GeofenceService.GEOFENCE_UPDATE_JSON);
            if (updateJson != null && !updateJson.isEmpty()) {
                try {
                    final PCFPushGeofenceResponseData responseData = GsonUtil.getGson().fromJson(updateJson, PCFPushGeofenceResponseData.class);
                    onSuccessfullyFetchedUpdates(timestamp, responseData, listener);
                } catch (Exception e) {
                    Logger.ex("Error parsing geofence update in push message", e);
                    onFailedToFetchUpdates("Error parsing geofence update in push message: " + e.getLocalizedMessage(), listener);
                }
            } else {
                onFailedToFetchUpdates("Expected intent to provide geofence update JSON but none was provided.", listener);
            }

        } else {

            // TODO - consider scheduling this request a short random time in the future in order to stagger the demand on the server.
            apiRequest.getGeofenceUpdates(timestamp, getParameters(), new PCFPushGetGeofenceUpdatesListener() {

                @Override
                public void onPCFPushGetGeofenceUpdatesSuccess(final PCFPushGeofenceResponseData responseData) {
                    onSuccessfullyFetchedUpdates(timestamp, responseData, listener);
                }

                @Override
                public void onPCFPushGetGeofenceUpdatesFailed(final String reason) {
                    // TODO - consider a retry mechanism for failed requests.
                    final String message = "Error fetching geofence updates: " + reason;
                    Logger.w(message);
                    onFailedToFetchUpdates(reason, listener);
                }
            });
        }
    }

    public void clearGeofencesFromMonitorAndStore(GeofenceUpdaterListener listener) {
        geofenceEngine.processResponseData(0L, null);
        pushPreferencesProvider.setLastGeofenceUpdate(GeofenceEngine.NEVER_UPDATED_GEOFENCES);
        if (listener != null) {
            listener.onSuccess();
        }
    }

    public void clearGeofencesFromStoreOnly(GeofenceUpdaterListener listener) {
        geofenceEngine.resetStore();
        if (listener != null) {
            listener.onSuccess();
        }
    }

    private void onSuccessfullyFetchedUpdates(final long timestamp, final PCFPushGeofenceResponseData responseData, final GeofenceUpdaterListener listener) {
        if (responseData != null && responseData.getGeofences() != null) {
            Logger.i("Successfully fetched geofence updates. Received " + responseData.getGeofences().size() + " items.");
        } else {
            Logger.i("Successfully fetched geofence updates. Received 0 items.");
        }
        if (responseData != null) {
            geofenceEngine.processResponseData(timestamp, responseData);
            pushPreferencesProvider.setLastGeofenceUpdate(responseData.getLastModified() == null ? 0 : responseData.getLastModified().getTime());
            if (listener != null) {
                listener.onSuccess();
            }
        }
    }

    private void onFailedToFetchUpdates(final String reason, final GeofenceUpdaterListener listener) {
        final GeofenceStatus previousStatus = GeofenceStatusUtil.loadGeofenceStatus(context);
        final GeofenceStatus newStatus = new GeofenceStatus(true, reason, previousStatus.getNumberCurrentlyMonitoringGeofences());
        GeofenceStatusUtil.saveGeofenceStatusAndSendBroadcast(context, newStatus);
        if (listener != null) {
            listener.onFailure(reason);
        }
    }

    private boolean doesIntentProvideJson(Intent intent) {
        return intent.hasExtra(GeofenceService.GEOFENCE_UPDATE_JSON);
    }

    private PushParameters getParameters() {
        final String gcmSenderId = Pivotal.getGcmSenderId(context);
        final String platformUuid = Pivotal.getPlatformUuid(context);
        final String platformSecret = Pivotal.getPlatformSecret(context);
        final String serviceUrl = Pivotal.getServiceUrl(context);
        final boolean areGeofencesEnabled = Pivotal.getGeofencesEnabled(context);
        return new PushParameters(gcmSenderId, platformUuid, platformSecret, serviceUrl, null, null, areGeofencesEnabled);
    }
}
