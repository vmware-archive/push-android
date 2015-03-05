package io.pivotal.android.push.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import io.pivotal.android.push.PushParameters;
import io.pivotal.android.push.backend.geofence.PCFPushGetGeofenceUpdatesApiRequest;
import io.pivotal.android.push.backend.geofence.PCFPushGetGeofenceUpdatesListener;
import io.pivotal.android.push.geofence.GeofenceEngine;
import io.pivotal.android.push.geofence.GeofencePersistentStore;
import io.pivotal.android.push.geofence.GeofenceRegistrar;
import io.pivotal.android.push.model.geofence.PCFPushGeofenceResponseData;
import io.pivotal.android.push.prefs.Pivotal;
import io.pivotal.android.push.prefs.PushPreferencesProvider;
import io.pivotal.android.push.prefs.PushPreferencesProviderImpl;
import io.pivotal.android.push.receiver.GcmBroadcastReceiver;
import io.pivotal.android.push.util.DebugUtil;
import io.pivotal.android.push.util.FileHelper;
import io.pivotal.android.push.util.GsonUtil;
import io.pivotal.android.push.util.Logger;
import io.pivotal.android.push.util.NetworkWrapper;
import io.pivotal.android.push.util.NetworkWrapperImpl;

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

        try {
            if (intent != null) {
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
            handleGeofenceUpdate(intent);
        }
    }

    private void handleGeofenceUpdate(Intent intent) {
        Logger.d("handleGeofenceUpdate: " + intent);

        instantiateDependencies();

        if (doesIntentProvideJson(intent) && DebugUtil.getInstance(this).isDebuggable()) {

            Logger.d("This update provides the list of geofences.");
            final String updateJson = intent.getStringExtra(GEOFENCE_UPDATE_JSON);
            if (updateJson != null && !updateJson.isEmpty()) {
                final PCFPushGeofenceResponseData responseData = GsonUtil.getGson().fromJson(updateJson, PCFPushGeofenceResponseData.class);
                onSuccessfullyFetchedUpdates(responseData);
            }

        } else {

            final long timestamp = pushPreferencesProvider.getLastGeofenceUpdate();

            Logger.d("The geofence update is available on the server.");

            // TODO - consider scheduling this request a short random time in the future in order to stagger the demand on the server.
            apiRequest.getGeofenceUpdates(timestamp, getParameters(), new PCFPushGetGeofenceUpdatesListener() {

                @Override
                public void onPCFPushGetGeofenceUpdatesSuccess(PCFPushGeofenceResponseData responseData) {
                    onSuccessfullyFetchedUpdates(responseData);
                }

                @Override
                public void onPCFPushGetGeofenceUpdatesFailed(String reason) {
                    // TODO - consider a retry mechanism for failed requests.
                    Logger.w("Error fetching geofence updates: " + reason);
                }
            });
        }
    }

    private boolean doesIntentProvideJson(Intent intent) {
        return intent.hasExtra(GEOFENCE_UPDATE_JSON);
    }

    private void onSuccessfullyFetchedUpdates(PCFPushGeofenceResponseData responseData) {
        if (responseData != null && responseData.getGeofences() != null) {
            Logger.i("Successfully fetched geofence updates. Received " + responseData.getGeofences().size() + " items.");
        } else {
            Logger.i("Successfully fetched geofence updates. Received 0 items.");
        }
        if (responseData != null) {
            geofenceEngine.processResponseData(responseData);
            pushPreferencesProvider.setLastGeofenceUpdate(responseData.getLastModified() == null ? 0 : responseData.getLastModified().getTime());
        }
    }

    private void instantiateDependencies() {
        if (pushPreferencesProvider == null) {
            pushPreferencesProvider = new PushPreferencesProviderImpl(this);
        }
        if (apiRequest == null) {
            final NetworkWrapper networkWrapper = new NetworkWrapperImpl();
            apiRequest = new PCFPushGetGeofenceUpdatesApiRequest(networkWrapper);
        }
        if (geofenceEngine == null) {
            final FileHelper fileHelper = new FileHelper(getApplicationContext());
            final GeofenceRegistrar registrar = new GeofenceRegistrar(this);
            final GeofencePersistentStore store = new GeofencePersistentStore(this, fileHelper);
            geofenceEngine = new GeofenceEngine(registrar, store);
        }
    }

    private PushParameters getParameters() {
        final String gcmSenderId = Pivotal.getGcmSenderId();
        final String platformUuid = Pivotal.getPlatformUuid();
        final String platformSecret = Pivotal.getPlatformSecret();
        final String serviceUrl = Pivotal.getServiceUrl();
        return new PushParameters(gcmSenderId, platformUuid, platformSecret, serviceUrl, null, null);
    }
}
