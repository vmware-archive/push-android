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
import io.pivotal.android.push.geofence.GeofenceConstants;
import io.pivotal.android.push.geofence.GeofenceEngine;
import io.pivotal.android.push.geofence.GeofenceStatusUtil;
import io.pivotal.android.push.geofence.GeofenceUpdater;
import io.pivotal.android.push.prefs.PushPreferencesFCM;
import io.pivotal.android.push.util.Logger;
import io.pivotal.android.push.version.GeofenceStatus;

public class UnregistrationEngine {

    private Context context;
    private PushPreferencesFCM pushPreferences;
    private PCFPushUnregisterDeviceApiRequestProvider PCFPushUnregisterDeviceApiRequestProvider;
    private GeofenceUpdater geofenceUpdater;
    private GeofenceStatusUtil geofenceStatusUtil;
    private String previousPCFPushDeviceRegistrationId;

    /**
     * Instantiate an instance of the UnregistrationEngine.
     *
     * All the parameters are required.  None may be null.
     * @param context  A context
     * @param pushPreferences  Some object that can provide persistent storage of push preferences.
     * @param pcfPushUnregisterDeviceApiRequestProvider  Some object that can provide PCFPushUnregisterDeviceApiRequest objects.
     * @param geofenceUpdater  Some object that can unregister geofences.
     * @param geofenceStatusUtil  Some object that can update geofence statuses.
     */
    public UnregistrationEngine(Context context,
                                PushPreferencesFCM pushPreferences,
                                PCFPushUnregisterDeviceApiRequestProvider pcfPushUnregisterDeviceApiRequestProvider,
                                GeofenceUpdater geofenceUpdater,
                                GeofenceStatusUtil geofenceStatusUtil) {

        verifyArguments(context,
                pushPreferences,
                pcfPushUnregisterDeviceApiRequestProvider,
                geofenceUpdater,
                geofenceStatusUtil);

        saveArguments(context,
                pushPreferences,
                pcfPushUnregisterDeviceApiRequestProvider,
                geofenceUpdater,
                geofenceStatusUtil);
    }

    private void verifyArguments(Context context,
                                 PushPreferencesFCM pushPreferences,
                                 PCFPushUnregisterDeviceApiRequestProvider pcfPushUnregisterDeviceApiRequestProvider,
                                 GeofenceUpdater geofenceUpdater,
                                 GeofenceStatusUtil geofenceStatusUtil) {

        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
        if (pushPreferences == null) {
            throw new IllegalArgumentException("pushPreferences may not be null");
        }
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
                               PushPreferencesFCM pushPreferences,
                               PCFPushUnregisterDeviceApiRequestProvider PCFPushUnregisterDeviceApiRequestProvider,
                               GeofenceUpdater geofenceUpdater,
                               GeofenceStatusUtil geofenceStatusUtil) {

        this.context = context;
        this.pushPreferences = pushPreferences;
        this.PCFPushUnregisterDeviceApiRequestProvider = PCFPushUnregisterDeviceApiRequestProvider;
        this.previousPCFPushDeviceRegistrationId = pushPreferences.getPCFPushDeviceRegistrationId();
        this.geofenceUpdater = geofenceUpdater;
        this.geofenceStatusUtil = geofenceStatusUtil;
    }

    public void unregisterDevice(PushParameters parameters, UnregistrationListener listener) {

        verifyUnregisterDeviceArguments(parameters);

        // Clear the saved package name so that the message receiver service won't be able to send
        // the application any more broadcasts
        pushPreferences.setPackageName(null);

        pushPreferences.setFcmTokenId(null);

        unregisterDeviceWithPCFPush(previousPCFPushDeviceRegistrationId, parameters, listener);
    }

    private void verifyUnregisterDeviceArguments(PushParameters parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("parameters may not be null");
        }
        if (parameters.getServiceUrl() == null) {
            throw new IllegalArgumentException("parameters.serviceUrl may not be null");
        }
    }

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
        pushPreferences.setPCFPushDeviceRegistrationId(null);
        pushPreferences.setPlatformUuid(null);
        pushPreferences.setPlatformSecret(null);
        pushPreferences.setDeviceAlias(null);
        pushPreferences.setCustomUserId(null);
        pushPreferences.setServiceUrl(null);
        pushPreferences.setTags(null);
    }

    private boolean shouldClearGeofences() {
        return pushPreferences.getLastGeofenceUpdate() != GeofenceConstants.NEVER_UPDATED_GEOFENCES;
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
        pushPreferences.setLastGeofenceUpdate(GeofenceConstants.NEVER_UPDATED_GEOFENCES);
        pushPreferences.setAreGeofencesEnabled(false);
    }
}
