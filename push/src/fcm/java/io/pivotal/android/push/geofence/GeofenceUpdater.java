package io.pivotal.android.push.geofence;

import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import io.pivotal.android.push.prefs.PushPreferences;
import java.util.Set;

import io.pivotal.android.push.PushParameters;
import io.pivotal.android.push.backend.geofence.PCFPushGetGeofenceUpdatesApiRequest;
import io.pivotal.android.push.backend.geofence.PCFPushGetGeofenceUpdatesListener;
import io.pivotal.android.push.model.geofence.PCFPushGeofenceResponseData;
import io.pivotal.android.push.prefs.Pivotal;
import io.pivotal.android.push.prefs.PushRequestHeaders;
import io.pivotal.android.push.service.GeofenceService;
import io.pivotal.android.push.util.DebugUtil;
import io.pivotal.android.push.util.GsonUtil;
import io.pivotal.android.push.util.Logger;
import io.pivotal.android.push.version.GeofenceStatus;

public class GeofenceUpdater {

    private final Context context;
    private final PCFPushGetGeofenceUpdatesApiRequest apiRequest;
    private final GeofenceEngine geofenceEngine;
    private final PushPreferences pushPreferences;
    private final PushRequestHeaders pushRequestHeaders;

    public interface GeofenceUpdaterListener {
        void onSuccess();
        void onFailure(String reason);
    }

    public GeofenceUpdater(Context context, PCFPushGetGeofenceUpdatesApiRequest apiRequest, GeofenceEngine geofenceEngine, PushPreferences pushPreferences, PushRequestHeaders pushRequestHeaders) {
        this.context = context;
        this.apiRequest = apiRequest;
        this.geofenceEngine = geofenceEngine;
        this.pushPreferences = pushPreferences;
        this.pushRequestHeaders = pushRequestHeaders;
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
            final PushParameters parameters = new PushParameters(
                    pushPreferences.getPlatformUuid(),
                    pushPreferences.getPlatformSecret(),
                    pushPreferences.getServiceUrl(),
                    "android-fcm",
                    pushPreferences.getDeviceAlias(),
                    pushPreferences.getCustomUserId(),
                    pushPreferences.getTags(),
                    pushPreferences.areGeofencesEnabled(),
                    pushPreferences.areAnalyticsEnabled(),
                    Pivotal.SslCertValidationMode.DEFAULT,
                    new ArrayList<String>(),
                    pushRequestHeaders.getRequestHeaders());
            final String deviceUuid = pushPreferences.getPCFPushDeviceRegistrationId();

            apiRequest.getGeofenceUpdates(timestamp, deviceUuid, parameters, new PCFPushGetGeofenceUpdatesListener() {

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
        Logger.v("Clearing geofences from monitor and store.");
        final Set<String> subscribedTags = pushPreferences.getTags();
        geofenceEngine.processResponseData(0L, null, subscribedTags);
        pushPreferences.setLastGeofenceUpdate(GeofenceEngine.NEVER_UPDATED_GEOFENCES);
        if (listener != null) {
            listener.onSuccess();
        }
    }

    public void clearGeofencesFromStoreOnly(GeofenceUpdaterListener listener) {
        Logger.v("Clearing geofences from store only. There are no permissions for clearing geofences from the monitor.");
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
            final Set<String> subscribedTags = pushPreferences.getTags();
            geofenceEngine.processResponseData(timestamp, responseData, subscribedTags);
            pushPreferences.setLastGeofenceUpdate(responseData.getLastModified() == null ? 0 : responseData.getLastModified().getTime());
            if (listener != null) {
                listener.onSuccess();
            }
        }
    }

    private void onFailedToFetchUpdates(final String reason, final GeofenceUpdaterListener listener) {
        final GeofenceStatusUtil geofenceStatusUtil = new GeofenceStatusUtil(context);
        final GeofenceStatus previousStatus = geofenceStatusUtil.loadGeofenceStatus();
        final GeofenceStatus newStatus = new GeofenceStatus(true, reason, previousStatus.getNumberCurrentlyMonitoringGeofences());
        geofenceStatusUtil.saveGeofenceStatusAndSendBroadcast(newStatus);
        if (listener != null) {
            listener.onFailure(reason);
        }
    }

    private boolean doesIntentProvideJson(Intent intent) {
        return intent.hasExtra(GeofenceService.GEOFENCE_UPDATE_JSON);
    }
}
