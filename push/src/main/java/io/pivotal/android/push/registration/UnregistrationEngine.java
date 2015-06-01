/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.registration;

import android.content.Context;

import io.pivotal.android.push.PushParameters;
import io.pivotal.android.push.backend.api.PCFPushUnregisterDeviceApiRequest;
import io.pivotal.android.push.backend.api.PCFPushUnregisterDeviceApiRequestProvider;
import io.pivotal.android.push.backend.api.PCFPushUnregisterDeviceListener;
import io.pivotal.android.push.gcm.GcmProvider;
import io.pivotal.android.push.gcm.GcmUnregistrationApiRequest;
import io.pivotal.android.push.gcm.GcmUnregistrationApiRequestProvider;
import io.pivotal.android.push.gcm.GcmUnregistrationListener;
import io.pivotal.android.push.geofence.GeofenceEngine;
import io.pivotal.android.push.geofence.GeofenceUpdater;
import io.pivotal.android.push.prefs.PushPreferencesProvider;
import io.pivotal.android.push.util.Logger;

public class UnregistrationEngine {

    private Context context;
    private GcmProvider gcmProvider;
    private PushPreferencesProvider pushPreferencesProvider;
    private GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider;
    private PCFPushUnregisterDeviceApiRequestProvider PCFPushUnregisterDeviceApiRequestProvider;
    private GeofenceUpdater geofenceUpdater;
    private String previousPCFPushDeviceRegistrationId;
    private boolean isUnregistrationSuccessful = true;
    private String unregistrationFailureReason;

    /**
     * Instantiate an instance of the UnregistrationEngine.
     *
     * All the parameters are required.  None may be null.
     * @param context  A context
     * @param gcmProvider  Some object that can provide the GCM services.
     * @param pushPreferencesProvider  Some object that can provide persistent storage of push preferences.
     * @param gcmUnregistrationApiRequestProvider  Some object that can provide GCMUnregistrationApiRequest objects.
     * @param pcfPushUnregisterDeviceApiRequestProvider  Some object that can provide PCFPushUnregisterDeviceApiRequest objects.
     * @param geofenceUpdater  Some object that can unregister geofences.
     */
    public UnregistrationEngine(Context context,
                                GcmProvider gcmProvider,
                                PushPreferencesProvider pushPreferencesProvider,
                                GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider,
                                PCFPushUnregisterDeviceApiRequestProvider pcfPushUnregisterDeviceApiRequestProvider,
                                GeofenceUpdater geofenceUpdater) {

        verifyArguments(context,
                gcmProvider,
                pushPreferencesProvider,
                gcmUnregistrationApiRequestProvider,
                pcfPushUnregisterDeviceApiRequestProvider,
                geofenceUpdater);

        saveArguments(context,
                gcmProvider,
                pushPreferencesProvider,
                gcmUnregistrationApiRequestProvider,
                pcfPushUnregisterDeviceApiRequestProvider,
                geofenceUpdater);
    }

    private void verifyArguments(Context context,
                                 GcmProvider gcmProvider,
                                 PushPreferencesProvider pushPreferencesProvider,
                                 GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider,
                                 PCFPushUnregisterDeviceApiRequestProvider pcfPushUnregisterDeviceApiRequestProvider, GeofenceUpdater geofenceUpdater) {

        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
        if (gcmProvider == null) {
            throw new IllegalArgumentException("gcmProvider may not be null");
        }
        if (pushPreferencesProvider == null) {
            throw new IllegalArgumentException("pushPreferencesProvider may not be null");
        }
        if (gcmUnregistrationApiRequestProvider == null) {
            throw new IllegalArgumentException("gcmUnregistrationApiRequestProvider may not be null");
        }
        if (pcfPushUnregisterDeviceApiRequestProvider == null) {
            throw new IllegalArgumentException("pcfPushUnregisterDeviceApiRequestProvider may not be null");
        }
        if (geofenceUpdater == null) {
            throw new IllegalArgumentException("geofenceUpdater may not be null");
        }
    }

    private void saveArguments(Context context,
                               GcmProvider gcmProvider,
                               PushPreferencesProvider pushPreferencesProvider,
                               GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider,
                               PCFPushUnregisterDeviceApiRequestProvider PCFPushUnregisterDeviceApiRequestProvider, GeofenceUpdater geofenceUpdater) {

        this.context = context;
        this.gcmProvider = gcmProvider;
        this.pushPreferencesProvider = pushPreferencesProvider;
        this.gcmUnregistrationApiRequestProvider = gcmUnregistrationApiRequestProvider;
        this.PCFPushUnregisterDeviceApiRequestProvider = PCFPushUnregisterDeviceApiRequestProvider;
        this.previousPCFPushDeviceRegistrationId = pushPreferencesProvider.getPCFPushDeviceRegistrationId();
        this.geofenceUpdater = geofenceUpdater;
    }

    public void unregisterDevice(PushParameters parameters, UnregistrationListener listener) {

        verifyUnregisterDeviceArguments(parameters);

        // Clear the saved package name so that the message receiver service won't be able to send
        // the application any more broadcasts
        pushPreferencesProvider.setPackageName(null);

        if (gcmProvider.isGooglePlayServicesInstalled(context)) {
            unregisterDeviceWithGcm(parameters, listener);
        } else {
            if (listener != null) {
                listener.onUnregistrationFailed("Google Play Services is not available");
            }
        }
    }

    private void verifyUnregisterDeviceArguments(PushParameters parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("parameters may not be null");
        }
        if (parameters.getServiceUrl() == null) {
            throw new IllegalArgumentException("parameters.serviceUrl may not be null");
        }
    }

    private void unregisterDeviceWithGcm(PushParameters parameters, final UnregistrationListener listener) {
        Logger.i("Unregistering sender ID with GCM.");
        final GcmUnregistrationApiRequest gcmUnregistrationApiRequest = gcmUnregistrationApiRequestProvider.getRequest();
        gcmUnregistrationApiRequest.startUnregistration(getGcmUnregistrationListener(parameters, listener));
    }

    private GcmUnregistrationListener getGcmUnregistrationListener(final PushParameters parameters, final UnregistrationListener listener) {

        return new GcmUnregistrationListener() {
            @Override
            public void onGcmUnregistrationComplete() {
                clearGcmRegistrationPreferences();
                unregisterDeviceWithPCFPush(previousPCFPushDeviceRegistrationId, parameters, listener);
            }

            @Override
            public void onGcmUnregistrationFailed(String reason) {
                // Even if we couldn't unregister from GCM we need to continue and unregister the device from PCF Push
                unregisterDeviceWithPCFPush(previousPCFPushDeviceRegistrationId, parameters, listener);
            }
        };
    }

    private void clearGcmRegistrationPreferences() {
        pushPreferencesProvider.setGcmDeviceRegistrationId(null);
        pushPreferencesProvider.setGcmSenderId(null);
        pushPreferencesProvider.setAppVersion(-1);
    }

    private void unregisterDeviceWithPCFPush(final String pcfPushDeviceRegistrationId, PushParameters parameters, final UnregistrationListener listener) {
        if (pcfPushDeviceRegistrationId != null) {
            Logger.i("Initiating device unregistration with PCF Push.");
            final PCFPushUnregisterDeviceApiRequest PCFPushUnregisterDeviceApiRequest = PCFPushUnregisterDeviceApiRequestProvider.getRequest();
            PCFPushUnregisterDeviceApiRequest.startUnregisterDevice(pcfPushDeviceRegistrationId, parameters, getPCFPushUnregisterDeviceListener(listener));
        } else {
            if (shouldClearGeofences()) {
                geofenceUpdater.clearGeofencesFromMonitorAndStore(getClearGeofencesListener(listener));
            } else {
                Logger.i("Not currently registered with PCF Push.  Unregistration is not required.");
                if (listener != null) {
                    listener.onUnregistrationComplete();
                }
            }
        }
    }

    private PCFPushUnregisterDeviceListener getPCFPushUnregisterDeviceListener(final UnregistrationListener listener) {
        return new PCFPushUnregisterDeviceListener() {

            @Override
            public void onPCFPushUnregisterDeviceSuccess() {

                clearPCFPushRegistrationPreferences();

                if (shouldClearGeofences()) {
                    geofenceUpdater.clearGeofencesFromMonitorAndStore(getClearGeofencesListener(listener));
                } else if (listener != null) {
                    listener.onUnregistrationComplete();
                }
            }

            @Override
            public void onPCFPushUnregisterDeviceFailed(String reason) {
                if (shouldClearGeofences()) {
                    isUnregistrationSuccessful = false;
                    unregistrationFailureReason = reason;
                    geofenceUpdater.clearGeofencesFromMonitorAndStore(getClearGeofencesListener(listener));
                } else if (listener != null) {
                    listener.onUnregistrationFailed(reason);
                }
            }
        };
    }

    private void clearPCFPushRegistrationPreferences() {
        pushPreferencesProvider.setPCFPushDeviceRegistrationId(null);
        pushPreferencesProvider.setPlatformUuid(null);
        pushPreferencesProvider.setPlatformSecret(null);
        pushPreferencesProvider.setDeviceAlias(null);
        pushPreferencesProvider.setServiceUrl(null);
        pushPreferencesProvider.setTags(null);
    }

    private boolean shouldClearGeofences() {
        return pushPreferencesProvider.getLastGeofenceUpdate() != GeofenceEngine.NEVER_UPDATED_GEOFENCES;
    }

    private GeofenceUpdater.GeofenceUpdaterListener getClearGeofencesListener(final UnregistrationListener listener) {
        return new GeofenceUpdater.GeofenceUpdaterListener() {

            @Override
            public void onSuccess() {

                clearGeofencePreferences();

                if (listener != null) {
                    if (isUnregistrationSuccessful) {
                        listener.onUnregistrationComplete();
                    } else {
                        listener.onUnregistrationFailed(unregistrationFailureReason);
                    }
                }

            }

            @Override
            public void onFailure(String reason) {

                if (listener != null) {
                    listener.onUnregistrationFailed(reason);
                }
            }
        };
    }

    private void clearGeofencePreferences() {
        pushPreferencesProvider.setLastGeofenceUpdate(GeofenceEngine.NEVER_UPDATED_GEOFENCES);
        pushPreferencesProvider.setAreGeofencesEnabled(false);
    }
}
