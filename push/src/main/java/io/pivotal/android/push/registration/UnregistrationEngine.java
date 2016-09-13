/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.registration;

import android.content.Context;
import android.content.pm.PackageManager;

import io.pivotal.android.push.PushParameters;
import io.pivotal.android.push.backend.api.PCFPushUnregisterDeviceApiRequest;
import io.pivotal.android.push.backend.api.PCFPushUnregisterDeviceApiRequestProvider;
import io.pivotal.android.push.backend.api.PCFPushUnregisterDeviceListener;
import io.pivotal.android.push.geofence.GeofenceEngine;
import io.pivotal.android.push.geofence.GeofenceStatusUtil;
import io.pivotal.android.push.geofence.GeofenceUpdater;
import io.pivotal.android.push.prefs.PushPreferencesProvider;
import io.pivotal.android.push.util.Logger;
import io.pivotal.android.push.version.GeofenceStatus;

public class UnregistrationEngine {

    private Context context;
//    private GcmProvider gcmProvider;
    private PushPreferencesProvider pushPreferencesProvider;
//    private GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider;
    private PCFPushUnregisterDeviceApiRequestProvider PCFPushUnregisterDeviceApiRequestProvider;
    private GeofenceUpdater geofenceUpdater;
    private GeofenceStatusUtil geofenceStatusUtil;
    private String previousPCFPushDeviceRegistrationId;

    /**
     * Instantiate an instance of the UnregistrationEngine.
     *
     * All the parameters are required.  None may be null.
     * @param context  A context
//     * @param gcmProvider  Some object that can provide the GCM services.
     * @param pushPreferencesProvider  Some object that can provide persistent storage of push preferences.
//     * @param gcmUnregistrationApiRequestProvider  Some object that can provide GCMUnregistrationApiRequest objects.
     * @param pcfPushUnregisterDeviceApiRequestProvider  Some object that can provide PCFPushUnregisterDeviceApiRequest objects.
     * @param geofenceUpdater  Some object that can unregister geofences.
     * @param geofenceStatusUtil  Some object that can update geofence statuses.
     */
    public UnregistrationEngine(Context context,
//                                GcmProvider gcmProvider,
                                PushPreferencesProvider pushPreferencesProvider,
//                                GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider,
                                PCFPushUnregisterDeviceApiRequestProvider pcfPushUnregisterDeviceApiRequestProvider,
                                GeofenceUpdater geofenceUpdater,
                                GeofenceStatusUtil geofenceStatusUtil) {

        verifyArguments(context,
//                gcmProvider,
                pushPreferencesProvider,
//                gcmUnregistrationApiRequestProvider,
                pcfPushUnregisterDeviceApiRequestProvider,
                geofenceUpdater,
                geofenceStatusUtil);

        saveArguments(context,
//                gcmProvider,
                pushPreferencesProvider,
//                gcmUnregistrationApiRequestProvider,
                pcfPushUnregisterDeviceApiRequestProvider,
                geofenceUpdater,
                geofenceStatusUtil);
    }

    private void verifyArguments(Context context,
//                                 GcmProvider gcmProvider,
                                 PushPreferencesProvider pushPreferencesProvider,
//                                 GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider,
                                 PCFPushUnregisterDeviceApiRequestProvider pcfPushUnregisterDeviceApiRequestProvider,
                                 GeofenceUpdater geofenceUpdater,
                                 GeofenceStatusUtil geofenceStatusUtil) {

        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
//        if (gcmProvider == null) {
//            throw new IllegalArgumentException("gcmProvider may not be null");
//        }
        if (pushPreferencesProvider == null) {
            throw new IllegalArgumentException("pushPreferencesProvider may not be null");
        }
//        if (gcmUnregistrationApiRequestProvider == null) {
//            throw new IllegalArgumentException("gcmUnregistrationApiRequestProvider may not be null");
//        }
        if (pcfPushUnregisterDeviceApiRequestProvider == null) {
            throw new IllegalArgumentException("pcfPushUnregisterDeviceApiRequestProvider may not be null");
        }
        if (geofenceUpdater == null) {
            throw new IllegalArgumentException("geofenceUpdater may not be null");
        }
        if (geofenceStatusUtil == null) {
            throw new IllegalArgumentException("geofenceStatusUtil may not be null");
        }
    }

    private void saveArguments(Context context,
//                               GcmProvider gcmProvider,
                               PushPreferencesProvider pushPreferencesProvider,
//                               GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider,
                               PCFPushUnregisterDeviceApiRequestProvider PCFPushUnregisterDeviceApiRequestProvider,
                               GeofenceUpdater geofenceUpdater,
                               GeofenceStatusUtil geofenceStatusUtil) {

        this.context = context;
        this.pushPreferencesProvider = pushPreferencesProvider;
        this.PCFPushUnregisterDeviceApiRequestProvider = PCFPushUnregisterDeviceApiRequestProvider;
        this.previousPCFPushDeviceRegistrationId = pushPreferencesProvider.getPCFPushDeviceRegistrationId();
        this.geofenceUpdater = geofenceUpdater;
        this.geofenceStatusUtil = geofenceStatusUtil;
    }

    public void unregisterDevice(PushParameters parameters, UnregistrationListener listener) {

        verifyUnregisterDeviceArguments(parameters);

        // Clear the saved package name so that the message receiver service won't be able to send
        // the application any more broadcasts
        pushPreferencesProvider.setPackageName(null);

//        if (gcmProvider.isGooglePlayServicesInstalled(context)) {
//            unregisterDeviceWithGcm(parameters, listener);
//        } else {
//            if (listener != null) {
//                listener.onUnregistrationFailed("Google Play Services is not available");
//            }
//        }
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
//        final GcmUnregistrationApiRequest gcmUnregistrationApiRequest = gcmUnregistrationApiRequestProvider.getRequest();
//        gcmUnregistrationApiRequest.startUnregistration(getGcmUnregistrationListener(parameters, listener));
    }

//    private GcmUnregistrationListener getGcmUnregistrationListener(final PushParameters parameters, final UnregistrationListener listener) {
//
//        return new GcmUnregistrationListener() {
//            @Override
//            public void onGcmUnregistrationComplete() {
//                clearGcmRegistrationPreferences();
//                unregisterDeviceWithPCFPush(previousPCFPushDeviceRegistrationId, parameters, listener);
//            }
//
//            @Override
//            public void onGcmUnregistrationFailed(String reason) {
//                // Even if we couldn't unregister from GCM we need to continue and unregister the device from PCF Push
//                unregisterDeviceWithPCFPush(previousPCFPushDeviceRegistrationId, parameters, listener);
//            }
//        };
//    }

//    private void clearGcmRegistrationPreferences() {
//        pushPreferencesProvider.setFcmTokenId(null);
//        pushPreferencesProvider.setGcmSenderId(null);
//        pushPreferencesProvider.setAppVersion(-1);
//    }

    private void unregisterDeviceWithPCFPush(final String registrationId, PushParameters parameters, final UnregistrationListener listener) {
        if (registrationId != null) {
            Logger.i("Initiating device unregistration with PCF Push.");
            final PCFPushUnregisterDeviceApiRequest request = PCFPushUnregisterDeviceApiRequestProvider.getRequest();
            request.startUnregisterDevice(registrationId, parameters, getPCFPushUnregisterDeviceListener(listener));
        } else {
            if (shouldClearGeofences()) {
                clearGeofences(listener);
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
                    clearGeofences(listener);
                } else if (listener != null) {
                    listener.onUnregistrationComplete();
                }
            }

            @Override
            public void onPCFPushUnregisterDeviceFailed(String reason) {

                if (shouldClearGeofences()) {
                    clearGeofences(null);
                }

                if (listener != null) {
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
        pushPreferencesProvider.setCustomUserId(null);
        pushPreferencesProvider.setServiceUrl(null);
        pushPreferencesProvider.setTags(null);
    }

    private boolean shouldClearGeofences() {
        return pushPreferencesProvider.getLastGeofenceUpdate() != GeofenceEngine.NEVER_UPDATED_GEOFENCES;
    }

    private boolean isPermissionForGeofences() {
        final int accessGpsPermission = context.checkCallingOrSelfPermission("android.permission.ACCESS_FINE_LOCATION");
        final int receiveBootPermission = context.checkCallingOrSelfPermission("android.permission.RECEIVE_BOOT_COMPLETED");
        return (accessGpsPermission == PackageManager.PERMISSION_GRANTED) && (receiveBootPermission == PackageManager.PERMISSION_GRANTED);
    }

    private void clearGeofences(final UnregistrationListener listener) {

        if (isPermissionForGeofences()) {

            geofenceUpdater.clearGeofencesFromMonitorAndStore(new GeofenceUpdater.GeofenceUpdaterListener() {

                @Override
                public void onSuccess() {

                    clearGeofencePreferences();

                    if (listener != null) {
                        listener.onUnregistrationComplete();
                    }
                }

                @Override
                public void onFailure(String reason) {
                    if (listener != null) {
                        listener.onUnregistrationFailed(reason);
                    }
                }
            });

        } else {

            geofenceUpdater.clearGeofencesFromStoreOnly(new GeofenceUpdater.GeofenceUpdaterListener() {

                @Override
                public void onSuccess() {

                    clearGeofencePreferences();

                    setGeofenceStatus();

                    if (listener != null) {
                        listener.onUnregistrationComplete();
                    }
                }

                @Override
                public void onFailure(String reason) {

                    setGeofenceStatus();

                    if (listener != null) {
                        listener.onUnregistrationFailed(reason);
                    }
                }

                private void setGeofenceStatus() {
                    final GeofenceStatus status = new GeofenceStatus(true, "Permission for geofences is not available.", 0);
                    geofenceStatusUtil.saveGeofenceStatusAndSendBroadcast(status);
                }
            });
        }
    }

    private void clearGeofencePreferences() {
        pushPreferencesProvider.setLastGeofenceUpdate(GeofenceEngine.NEVER_UPDATED_GEOFENCES);
        pushPreferencesProvider.setAreGeofencesEnabled(false);
    }
}
