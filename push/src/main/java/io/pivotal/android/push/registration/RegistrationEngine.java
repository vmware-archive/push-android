/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.registration;

import android.content.Context;
import android.content.pm.PackageManager;

import java.util.Set;

import io.pivotal.android.push.PushParameters;
import io.pivotal.android.push.backend.api.PCFPushRegistrationApiRequest;
import io.pivotal.android.push.backend.api.PCFPushRegistrationApiRequestProvider;
import io.pivotal.android.push.backend.api.PCFPushRegistrationListener;
import io.pivotal.android.push.gcm.GcmProvider;
import io.pivotal.android.push.gcm.GcmRegistrationApiRequest;
import io.pivotal.android.push.gcm.GcmRegistrationApiRequestProvider;
import io.pivotal.android.push.gcm.GcmRegistrationListener;
import io.pivotal.android.push.gcm.GcmUnregistrationApiRequest;
import io.pivotal.android.push.gcm.GcmUnregistrationApiRequestProvider;
import io.pivotal.android.push.gcm.GcmUnregistrationListener;
import io.pivotal.android.push.geofence.GeofenceEngine;
import io.pivotal.android.push.geofence.GeofenceUpdater;
import io.pivotal.android.push.prefs.PushPreferencesProvider;
import io.pivotal.android.push.util.Logger;
import io.pivotal.android.push.version.VersionProvider;

/**
 * This class is responsible for all the business logic behind device registration.  For a description
 * of its operation you can refer to the following diagrams:
 *
 *  1. Flow chart: https://docs.google.com/a/pivotallabs.com/drawings/d/1e1P4R5AOz486lMjhlrNO22gLyfADBPsBpYkYYKfbmYk
 *  2. Sequence diagram: https://docs.google.com/a/pivotallabs.com/drawings/d/1MoOFqKZvljEwu9M6ZNjZlEq3iOPBZKm2NMRw7TN6GKM
 *
 *  In general, though, the Registration Engine tries to do as little work as it thinks is required.
 *
 *  If the device is already successfully registered and all of the registration parameters are the same as the
 *  previous registration then the Registration Engine won't do anything.
 *
 *  On a fresh install, the Registration Engine will register with Google Cloud Messaging (GCM) and then with the
 *  Pivotal CF Mobile Services server.
 *
 *  If the GCM Sender ID is different then the previous registration, then the Registration Engine will
 *  attempt to unregister the device with GCM (Google Cloud Messaging) first.
 *
 *  If the application version code or the GCM Sender ID is updated since the previous registration, then the
 *  Registration Engine will attempt to re-register with GCM.
 *
 *  If any of the Pivotal CF Mobile Services registration parameters (platform_uuid, platform_secret, device_alias), or
 *  if a GCM registration provides a different device registration ID than a previous install, then the Registration
 *  Engine will attempt to update its registration wih the Pivotal CF Mobile Services Push server (i.e.: HTTP PUT).
 *
 *  If, however, the base_server_url parameter is different than the existing registration, then the Registration
 *  Engine will abandon its registration with the previous server and make a new one (i.e.: HTTP POST) with the new
 *  server.
 *
 *  The Registration Engine is also designed to successfully complete previous registrations that have failed. For
 *  instance, if the previous registration attempt successfully registered with GCM but failed to complete the
 *  registration with PCF Push then it will simply try to re-register with the server if called again.
 */
public class RegistrationEngine {

    private Context context;
    private GcmProvider gcmProvider;
    private PushPreferencesProvider pushPreferencesProvider;
    private GcmRegistrationApiRequestProvider gcmRegistrationApiRequestProvider;
    private GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider;
    private PCFPushRegistrationApiRequestProvider pcfPushRegistrationApiRequestProvider;
    private GeofenceUpdater geofenceUpdater;
    private GeofenceEngine geofenceEngine;
    private VersionProvider versionProvider;
    private String packageName;
    private String previousGcmDeviceRegistrationId = null;
    private String previousPCFPushDeviceRegistrationId = null;
    private String previousGcmSenderId;
    private String previousPlatformUuid;
    private String previousPlatformSecret;
    private String previousDeviceAlias;
    private String previousServiceUrl;

    /**
     * Instantiate an instance of the RegistrationEngine.
     *
     * All the parameters are required.  None may be null.
     * @param context  A context
     * @param packageName  The currenly application package name.
     * @param gcmProvider  Some object that can provide the GCM services.
     * @param pushPreferencesProvider  Some object that can provide persistent storage for push preferences.
     * @param gcmRegistrationApiRequestProvider  Some object that can provide GCMRegistrationApiRequest objects.
     * @param gcmUnregistrationApiRequestProvider  Some object that can provide GCMUnregistrationApiRequest objects.
     * @param pcfPushRegistrationApiRequestProvider  Some object that can provide PCFPushRegistrationApiRequest objects.
     * @param versionProvider  Some object that can provide the application version.
     * @param geofenceUpdater  Some object that can be used to download geofence updates from the server.
     * @param geofenceEngine  Some object that can be used to register geofences.
     */
    public RegistrationEngine(Context context,
                              String packageName,
                              GcmProvider gcmProvider,
                              PushPreferencesProvider pushPreferencesProvider,
                              GcmRegistrationApiRequestProvider gcmRegistrationApiRequestProvider,
                              GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider,
                              PCFPushRegistrationApiRequestProvider pcfPushRegistrationApiRequestProvider,
                              VersionProvider versionProvider,
                              GeofenceUpdater geofenceUpdater,
                              GeofenceEngine geofenceEngine) {

        verifyArguments(context,
                packageName,
                gcmProvider,
                pushPreferencesProvider,
                gcmRegistrationApiRequestProvider,
                gcmUnregistrationApiRequestProvider,
                pcfPushRegistrationApiRequestProvider,
                versionProvider,
                geofenceUpdater,
                geofenceEngine);

        saveArguments(context,
                packageName,
                gcmProvider,
                pushPreferencesProvider,
                gcmRegistrationApiRequestProvider,
                gcmUnregistrationApiRequestProvider,
                pcfPushRegistrationApiRequestProvider,
                versionProvider,
                geofenceUpdater,
                geofenceEngine);
    }

    private void verifyArguments(Context context,
                                 String packageName,
                                 GcmProvider gcmProvider,
                                 PushPreferencesProvider pushPreferencesProvider,
                                 GcmRegistrationApiRequestProvider gcmRegistrationApiRequestProvider,
                                 GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider,
                                 PCFPushRegistrationApiRequestProvider pcfPushRegistrationApiRequestProvider,
                                 VersionProvider versionProvider,
                                 GeofenceUpdater geofenceUpdater,
                                 GeofenceEngine geofenceEngine) {

        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
        if (packageName == null) {
            throw new IllegalArgumentException("packageName may not be null");
        }
        if (gcmProvider == null) {
            throw new IllegalArgumentException("gcmProvider may not be null");
        }
        if (pushPreferencesProvider == null) {
            throw new IllegalArgumentException("pushPreferencesProvider may not be null");
        }
        if (gcmRegistrationApiRequestProvider == null) {
            throw new IllegalArgumentException("gcmRegistrationApiRequestProvider may not be null");
        }
        if (gcmUnregistrationApiRequestProvider == null) {
            throw new IllegalArgumentException("gcmUnregistrationApiRequestProvider may not be null");
        }
        if (pcfPushRegistrationApiRequestProvider == null) {
            throw new IllegalArgumentException("pcfPushRegistrationApiRequestProvider may not be null");
        }
        if (versionProvider == null) {
            throw new IllegalArgumentException("versionProvider may not be null");
        }
        if (geofenceUpdater == null) {
            throw new IllegalArgumentException("geofenceUpdater may not be null");
        }
        if (geofenceEngine == null) {
            throw new IllegalArgumentException("geofenceEngine may not be null");
        }
    }

    private void saveArguments(Context context,
                               String packageName,
                               GcmProvider gcmProvider,
                               PushPreferencesProvider pushPreferencesProvider,
                               GcmRegistrationApiRequestProvider gcmRegistrationApiRequestProvider,
                               GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider,
                               PCFPushRegistrationApiRequestProvider pcfPushRegistrationApiRequestProvider,
                               VersionProvider versionProvider,
                               GeofenceUpdater geofenceUpdater,
                               GeofenceEngine geofenceEngine) {

        this.context = context;
        this.packageName = packageName;
        this.gcmProvider = gcmProvider;
        this.pushPreferencesProvider = pushPreferencesProvider;
        this.gcmRegistrationApiRequestProvider = gcmRegistrationApiRequestProvider;
        this.gcmUnregistrationApiRequestProvider = gcmUnregistrationApiRequestProvider;
        this.pcfPushRegistrationApiRequestProvider = pcfPushRegistrationApiRequestProvider;
        this.versionProvider = versionProvider;
        this.geofenceUpdater = geofenceUpdater;
        this.geofenceEngine = geofenceEngine;
        this.previousGcmDeviceRegistrationId = pushPreferencesProvider.getGcmDeviceRegistrationId();
        this.previousPCFPushDeviceRegistrationId = pushPreferencesProvider.getPCFPushDeviceRegistrationId();
        this.previousGcmSenderId = pushPreferencesProvider.getGcmSenderId();
        this.previousPlatformUuid = pushPreferencesProvider.getPlatformUuid();
        this.previousPlatformSecret = pushPreferencesProvider.getPlatformSecret();
        this.previousDeviceAlias = pushPreferencesProvider.getDeviceAlias();
        this.previousServiceUrl = pushPreferencesProvider.getServiceUrl();
    }

    /**
     * Start a registration attempt.  This method is asynchronous and will return before registration is complete.
     * If you need to know when registration completes (successfully or not), then provide a listener.
     *
     * This class is NOT reentrant.  Do not try to call registerDevice again while some registration is
     * already in progress.  It is best to create a new RegistrationEngine object entirely if you need to
     * register again (though I don't know why you would want to register more than ONCE during the lifetime
     * of a process - unless registration fails and you want to retry).
     *
     *
     * @param parameters  The registration parameters.  May not be null.
     * @param listener  An optional listener if you care to know when registration completes or fails.
     */
    public void registerDevice(PushParameters parameters, final RegistrationListener listener) {

        verifyRegistrationArguments(parameters);

        // Save the given package name so that the message receiver service can see it
        pushPreferencesProvider.setPackageName(packageName);

        if (!isEmptyPreviousGcmSenderId() && isUpdatedGcmSenderId(parameters)) {
          unregisterDeviceWithGcm(parameters, listener);

        } else if (isGcmRegistrationRequired(parameters)) {
            if (gcmProvider.isGooglePlayServicesInstalled(context)) {
                registerDeviceWithGcm(parameters, listener);
            } else {
                if (listener != null) {
                    listener.onRegistrationFailed("Google Play Services is not available");
                }
            }

        } else if (isPCFPushUpdateRegistrationRequired(previousGcmDeviceRegistrationId, parameters)) {
            registerUpdateDeviceWithPCFPush(previousGcmDeviceRegistrationId, previousPCFPushDeviceRegistrationId, pushPreferencesProvider.getTags(), parameters, listener);

        } else if (isPCFPushNewRegistrationRequired(parameters)) {
            registerNewDeviceWithPCFPush(previousGcmDeviceRegistrationId, pushPreferencesProvider.getTags(), parameters, listener);

        } else if (isGeofenceUpdateRequired(parameters)) {
            updateGeofences(listener);

        } else if (isClearGeofencesRequired(parameters)) {
            clearGeofences(listener);

        } else {
            Logger.v("Already registered with GCM and PCF Push");
            if (listener != null) {
                listener.onRegistrationComplete();
            }
        }
    }

    private void verifyRegistrationArguments(PushParameters parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("parameters may not be null");
        }
        if (parameters.getGcmSenderId() == null || parameters.getGcmSenderId().isEmpty()) {
            throw new IllegalArgumentException("parameters.senderId may not be null or empty");
        }
        if (parameters.getPlatformUuid() == null || parameters.getPlatformUuid().isEmpty()) {
            throw new IllegalArgumentException("parameters.platformUuid may not be null or empty");
        }
        if (parameters.getPlatformSecret() == null || parameters.getPlatformSecret().isEmpty()) {
            throw new IllegalArgumentException("parameters.platformSecret may not be null or empty");
        }
        if (parameters.getServiceUrl() == null) {
            throw new IllegalArgumentException("parameters.serviceUrl may not be null");
        }
    }

    private boolean isGcmRegistrationRequired(PushParameters parameters) {
        if (isEmptyPreviousGcmDeviceRegistrationId()) {
            Logger.v("previousGcmDeviceRegistrationId is empty. Device registration with GCM will be required");
            return true;
        }
        if (isEmptyPreviousGcmSenderId()) {
            Logger.v("previousGcmSenderId is empty. Device registration with GCM will be required");
            return true;
        }
        if (isUpdatedGcmSenderId(parameters)) {
            Logger.v("gcmSenderId has been updated. Device unregistration and re-registration with GCM will be required");
            return true;
        }
        if (hasAppBeenUpdated()) {
            Logger.v("App version changed. Device registration with GCM will be required.");
            return true;
        }
        return false;
    }

    private boolean isEmptyPreviousGcmDeviceRegistrationId() {
        return previousGcmDeviceRegistrationId == null || previousGcmDeviceRegistrationId.isEmpty();
    }

    private boolean isEmptyPreviousGcmSenderId() {
        final boolean isPreviousGcmSenderIdEmpty = previousGcmSenderId == null || previousGcmSenderId.isEmpty();
        return isPreviousGcmSenderIdEmpty;
    }

    private boolean isUpdatedGcmSenderId(PushParameters parameters) {
        return !parameters.getGcmSenderId().equals(previousGcmSenderId);
    }

    private boolean haveTagsBeenUpdated(PushParameters parameters) {
        final Set<String> savedTags = pushPreferencesProvider.getTags();
        final Set<String> requestedTags = parameters.getTags();

        if (isNullOrEmpty(savedTags) && isNullOrEmpty(requestedTags)) {
            return false;
        } else if (isNullOrEmpty(savedTags) && !isNullOrEmpty(requestedTags)) {
            return true;
        } else if (!isNullOrEmpty(savedTags) && isNullOrEmpty(requestedTags)) {
            return true;
        } else {
            return !requestedTags.equals(savedTags);
        }
    }

    private boolean isNullOrEmpty(Set<String> s) {
        return (s == null || s.isEmpty());
    }

    private boolean hasAppBeenUpdated() {
        final int currentAppVersion = versionProvider.getAppVersion();
        final int savedAppVersion = pushPreferencesProvider.getAppVersion();
        return currentAppVersion != savedAppVersion;
    }

    private boolean isPCFPushNewRegistrationRequired(PushParameters parameters) {
        final boolean isPreviousPlatformUuidEmpty = previousPlatformUuid == null || previousPlatformUuid.isEmpty();
        final boolean isServiceUrlUpdated = isServiceUrlUpdated(parameters);
        if (isEmptyPreviousGcmDeviceRegistrationId()) {
            Logger.v("previousGcmDeviceRegistrationId is empty. Device registration with PCF Push will be required.");
        }
        if (isPreviousPlatformUuidEmpty) {
            Logger.v("previousPlatformUuid is empty. Device registration with PCF Push will be required.");
        }
        if (isServiceUrlUpdated) {
            Logger.v("The serviceUrl has been updated. Device registration with PCF Push will be required.");
        }
        return isEmptyPreviousGcmDeviceRegistrationId() || isPreviousPlatformUuidEmpty || areRegistrationParametersUpdated(parameters) || isServiceUrlUpdated;
    }

    private boolean isPCFPushUpdateRegistrationRequired(String newGcmDeviceRegistrationId, PushParameters parameters) {
        final boolean isGcmDeviceRegistrationIdDifferent = isEmptyPreviousGcmDeviceRegistrationId() || !previousGcmDeviceRegistrationId.equals(newGcmDeviceRegistrationId);
        final boolean isPreviousPCFPushDeviceRegistrationIdEmpty = previousPCFPushDeviceRegistrationId == null || previousPCFPushDeviceRegistrationId.isEmpty();
        if (isPreviousPCFPushDeviceRegistrationIdEmpty) {
            Logger.v("previousPCFPushDeviceRegistrationId is empty. Device will NOT require an update-registration with PCF Push.");
            return false;
        }
        if (isGcmDeviceRegistrationIdDifferent) {
            Logger.v("The gcmDeviceRegistrationId is different. Device will need to update its registration with PCF Push.");
            return true;
        }
        if (areRegistrationParametersUpdated(parameters)) {
            Logger.v("The registration parameters have been updated. Device will need to update its registration with PCF Push.");
            return true;
        }
        if (haveTagsBeenUpdated(parameters)) {
            Logger.v("App tags changed. Device will need to update its registration with PCF Push.");
            return true;
        }
        Logger.v("It does not seem that the device needs to update its registration with PCF Push.");
        return false;
    }

    private boolean areRegistrationParametersUpdated(PushParameters parameters) {
        final boolean isPreviousPlatformUuidEmpty = previousPlatformUuid == null || previousPlatformUuid.isEmpty();
        final boolean isPlatformUuidUpdated = (isPreviousPlatformUuidEmpty && !parameters.getPlatformUuid().isEmpty()) || !parameters.getPlatformUuid().equals(previousPlatformUuid);
        final boolean isPreviousPlatformSecretEmpty = previousPlatformSecret == null || previousPlatformSecret.isEmpty();
        final boolean isPlatformSecretUpdated = (isPreviousPlatformSecretEmpty && !parameters.getPlatformSecret().isEmpty()) || !parameters.getPlatformSecret().equals(previousPlatformSecret);
        final boolean isPreviousDeviceAliasEmpty = previousDeviceAlias == null || previousDeviceAlias.isEmpty();
        final boolean isNewDeviceAliasEmpty = parameters.getDeviceAlias() == null || parameters.getDeviceAlias().isEmpty();
        final boolean isDeviceAliasUpdated = (isPreviousDeviceAliasEmpty && !isNewDeviceAliasEmpty) || (!isPreviousDeviceAliasEmpty && isNewDeviceAliasEmpty) || (!isNewDeviceAliasEmpty && !parameters.getDeviceAlias().equals(previousDeviceAlias));
        return isDeviceAliasUpdated || isPlatformSecretUpdated || isPlatformUuidUpdated;
    }

    private boolean isServiceUrlUpdated(PushParameters parameters) {
        final boolean isPreviousServiceUrlEmpty = previousServiceUrl == null;
        final boolean isServiceUrlUpdated = (isPreviousServiceUrlEmpty && parameters.getServiceUrl() != null) || !parameters.getServiceUrl().equals(previousServiceUrl);
        return isServiceUrlUpdated;
    }

    private boolean isGeofenceUpdateRequired(PushParameters parameters) {
        if (isPermissionForGeofences() && parameters.areGeofencesEnabled() && pushPreferencesProvider.getLastGeofenceUpdate() == GeofenceEngine.NEVER_UPDATED_GEOFENCES) {
            Logger.i("A geofence update is required in order to download the current geofence configuration.");
            return true;
        }
        return false;
    }

    private boolean areGeofencesAvailable(PushParameters parameters) {
        if (isPermissionForGeofences() && parameters.areGeofencesEnabled() && pushPreferencesProvider.getLastGeofenceUpdate() != GeofenceEngine.NEVER_UPDATED_GEOFENCES) {
            Logger.v("Geofences are available.");
            return true;
        }
        return false;
    }

    private boolean isClearGeofencesRequired(PushParameters parameters) {
        if ((!isPermissionForGeofences() && parameters.areGeofencesEnabled()) || (!parameters.areGeofencesEnabled() && pushPreferencesProvider.getLastGeofenceUpdate() != GeofenceEngine.NEVER_UPDATED_GEOFENCES)) {
            Logger.v("Geofences are now disabled and the current configuration needs to be cleared.");
            return true;
        }
        return false;
    }

    private boolean isPermissionForGeofences() {
        final int accessGpsPermission = context.checkCallingOrSelfPermission("android.permission.ACCESS_FINE_LOCATION");
        final int receiveBootPermission = context.checkCallingOrSelfPermission("android.permission.RECEIVE_BOOT_COMPLETED");
        return (accessGpsPermission == PackageManager.PERMISSION_GRANTED) && (receiveBootPermission == PackageManager.PERMISSION_GRANTED);
    }

    private void unregisterDeviceWithGcm(final PushParameters parameters, final RegistrationListener listener) {
        Logger.i("GCM Sender ID has been changed. Unregistering sender ID with GCM.");
        final GcmUnregistrationApiRequest gcmUnregistrationApiRequest = gcmUnregistrationApiRequestProvider.getRequest();
        gcmUnregistrationApiRequest.startUnregistration(new GcmUnregistrationListener() {

            @Override
            public void onGcmUnregistrationComplete() {
                pushPreferencesProvider.setGcmDeviceRegistrationId(null);
                pushPreferencesProvider.setGcmSenderId(null);
                pushPreferencesProvider.setAppVersion(PushPreferencesProvider.NO_SAVED_VERSION);

                registerDeviceWithGcm(parameters, listener);
            }

            @Override
            public void onGcmUnregistrationFailed(String reason) {
                // Even if we couldn't unregister from GCM we should try to register with GCM.
                registerDeviceWithGcm(parameters, listener);
            }
        });
    }

    private void registerDeviceWithGcm(final PushParameters parameters, final RegistrationListener listener) {
        Logger.i("Initiating device registration with GCM.");
        final GcmRegistrationApiRequest gcmRegistrationApiRequest = gcmRegistrationApiRequestProvider.getRequest();
        gcmRegistrationApiRequest.startRegistration(parameters.getGcmSenderId(), new GcmRegistrationListener() {

            @Override
            public void onGcmRegistrationComplete(String gcmDeviceRegistrationId) {

                if (gcmDeviceRegistrationId == null) {
                    Logger.e("GCM returned null gcmDeviceRegistrationId");
                    if (listener != null) {
                        listener.onRegistrationFailed("GCM returned null gcmDeviceRegistrationId");
                    }
                    return;
                }

                pushPreferencesProvider.setGcmDeviceRegistrationId(gcmDeviceRegistrationId);
                pushPreferencesProvider.setGcmSenderId(parameters.getGcmSenderId());
                pushPreferencesProvider.setAppVersion(versionProvider.getAppVersion());

                final boolean isNewGcmDeviceRegistrationId;
                if (previousGcmDeviceRegistrationId != null && previousGcmDeviceRegistrationId.equals(gcmDeviceRegistrationId)) {
                    Logger.v("New gcmDeviceRegistrationId from GCM is the same as the previous one.");
                    isNewGcmDeviceRegistrationId = false;
                } else {
                    isNewGcmDeviceRegistrationId = true;
                }

                final boolean isServiceUrlUpdated = isServiceUrlUpdated(parameters);
                if (isServiceUrlUpdated) {
                    Logger.v("The PCF Push serviceUrl has been updated. A new registration with the PCF Push server is required.");
                }

                if (isPCFPushUpdateRegistrationRequired(gcmDeviceRegistrationId, parameters) && !isServiceUrlUpdated) {
                    registerUpdateDeviceWithPCFPush(gcmDeviceRegistrationId, previousPCFPushDeviceRegistrationId, pushPreferencesProvider.getTags(), parameters, listener);

                } else if (isNewGcmDeviceRegistrationId || isServiceUrlUpdated) {
                    registerNewDeviceWithPCFPush(gcmDeviceRegistrationId, pushPreferencesProvider.getTags(), parameters, listener);

                } else if (isGeofenceUpdateRequired(parameters)) {
                    updateGeofences(listener);

                } else if (isClearGeofencesRequired(parameters)) {
                    clearGeofences(listener);

                } else if (listener != null) {
                    listener.onRegistrationComplete();
                }
            }

            @Override
            public void onGcmRegistrationFailed(String reason) {

                if (isClearGeofencesRequired(parameters)) {
                    clearGeofences(null); // Note - skipping the callback since we want to report the failure below
                }

                if (listener != null) {
                    listener.onRegistrationFailed(reason);
                }
            }
        });
    }

    private void registerUpdateDeviceWithPCFPush(String gcmDeviceRegistrationId,
                                                 String pcfPushDeviceRegistrationId,
                                                 Set<String> savedTags,
                                                 PushParameters parameters,
                                                 RegistrationListener listener) {

        Logger.i("Initiating update device registration with PCF Push.");
        final PCFPushRegistrationApiRequest PCFPushRegistrationApiRequest = pcfPushRegistrationApiRequestProvider.getRequest();
        PCFPushRegistrationApiRequest.startUpdateDeviceRegistration(gcmDeviceRegistrationId,
                pcfPushDeviceRegistrationId,
                savedTags,
                parameters,
                getPCFPushUpdateRegistrationListener(parameters, listener));
    }

    private PCFPushRegistrationListener getPCFPushUpdateRegistrationListener(final PushParameters parameters, final RegistrationListener listener) {
        return new PCFPushRegistrationListener() {

            @Override
            public void onPCFPushRegistrationSuccess(String pcfPushDeviceRegistrationId) {

                if (pcfPushDeviceRegistrationId == null) {
                    Logger.e("PCF Push server return null pcfPushDeviceRegistrationId upon registration update.");

                    // The server didn't return a valid registration response.  We should clear our local
                    // registration data so that we can attempt to reregister next time.
                    clearPCFPushRegistrationPreferences();

                    if (listener != null) {
                        listener.onRegistrationFailed("PCF Push server return null pcfPushDeviceRegistrationId upon registration update.");
                    }
                    return;
                }

                if (haveTagsBeenUpdated(parameters) && areGeofencesAvailable(parameters)) {
                    Logger.i("Tags have been updated - reregistering current geofences.");
                    geofenceEngine.reregisterCurrentLocations();
                }

                Logger.i("Saving PCF Push device registration ID: " + pcfPushDeviceRegistrationId);
                pushPreferencesProvider.setPCFPushDeviceRegistrationId(pcfPushDeviceRegistrationId);

                Logger.v("Saving updated platformUuid, platformSecret, deviceAlias, and serviceUrl");
                pushPreferencesProvider.setPlatformUuid(parameters.getPlatformUuid());
                pushPreferencesProvider.setPlatformSecret(parameters.getPlatformSecret());
                pushPreferencesProvider.setDeviceAlias(parameters.getDeviceAlias());
                pushPreferencesProvider.setServiceUrl(parameters.getServiceUrl());
                pushPreferencesProvider.setTags(parameters.getTags());
                Logger.v("Saving tags: " + parameters.getTags());

                if (isGeofenceUpdateRequired(parameters)) {
                    updateGeofences(listener);

                } else if (isClearGeofencesRequired(parameters)) {
                    clearGeofences(listener);

                } else if (listener != null) {
                    listener.onRegistrationComplete();
                }
            }

            @Override
            public void onPCFPushRegistrationFailed(String reason) {

                if (isClearGeofencesRequired(parameters)) {
                    clearGeofences(null); // Note - skipping the callback since we want to report the failure below

                } else if (haveTagsBeenUpdated(parameters) && areGeofencesAvailable(parameters)) {
                    Logger.i("Tags have been updated - reregistering current geofences.");
                    geofenceEngine.reregisterCurrentLocations();
                }

                clearPCFPushRegistrationPreferences();

                if (listener != null) {
                    listener.onRegistrationFailed(reason);
                }
            }
        };
    }

    private void registerNewDeviceWithPCFPush(final String gcmDeviceRegistrationId,
                                              Set<String> savedTags,
                                              PushParameters parameters,
                                              RegistrationListener listener) {

        Logger.i("Initiating new device registration with PCF Push.");
        final PCFPushRegistrationApiRequest PCFPushRegistrationApiRequest = pcfPushRegistrationApiRequestProvider.getRequest();
        PCFPushRegistrationApiRequest.startNewDeviceRegistration(gcmDeviceRegistrationId, savedTags, parameters, getPCFPushNewRegistrationListener(parameters, listener));
    }

    private PCFPushRegistrationListener getPCFPushNewRegistrationListener(final PushParameters parameters, final RegistrationListener listener) {
        return new PCFPushRegistrationListener() {

            @Override
            public void onPCFPushRegistrationSuccess(String pcfPushDeviceRegistrationId) {

                if (pcfPushDeviceRegistrationId == null) {

                    Logger.e("PCF Push returned null pcfPushDeviceRegistrationId");

                    // The server didn't return a valid registration response.  We should clear our local
                    // registration data so that we can attempt to reregister next time.
                    clearPCFPushRegistrationPreferences();

                    if (listener != null) {
                        listener.onRegistrationFailed("PCF Push returned null pcfPushDeviceRegistrationId");
                    }
                    return;
                }

                Logger.i("Saving PCF Push device registration ID: " + pcfPushDeviceRegistrationId);
                pushPreferencesProvider.setPCFPushDeviceRegistrationId(pcfPushDeviceRegistrationId);

                Logger.v("Saving updated platformUuid, platformSecret, deviceAlias, and serviceUrl");
                pushPreferencesProvider.setPlatformUuid(parameters.getPlatformUuid());
                pushPreferencesProvider.setPlatformSecret(parameters.getPlatformSecret());
                pushPreferencesProvider.setDeviceAlias(parameters.getDeviceAlias());
                pushPreferencesProvider.setServiceUrl(parameters.getServiceUrl());
                pushPreferencesProvider.setTags(parameters.getTags());
                Logger.v("Saving tags: " + parameters.getTags());

                if (isPermissionForGeofences() && parameters.areGeofencesEnabled()) {
                    updateGeofences(listener);

                } else if (isClearGeofencesRequired(parameters)) {
                    clearGeofences(listener);

                } else {
                    pushPreferencesProvider.setAreGeofencesEnabled(false);
                    pushPreferencesProvider.setLastGeofenceUpdate(GeofenceEngine.NEVER_UPDATED_GEOFENCES);

                    if (listener != null) {
                        listener.onRegistrationComplete();
                    }
                }
            }

            @Override
            public void onPCFPushRegistrationFailed(String reason) {

                clearPCFPushRegistrationPreferences();

                if (isClearGeofencesRequired(parameters)) {
                    clearGeofences(null); // Note - skipping the callback since we want to report the failure below
                }

                if (listener != null) {
                    listener.onRegistrationFailed(reason);
                }
            }
        };
    }

    private void updateGeofences(final RegistrationListener listener) {

        geofenceUpdater.startGeofenceUpdate(null, 0L, new GeofenceUpdater.GeofenceUpdaterListener() {

            @Override
            public void onSuccess() {
                pushPreferencesProvider.setAreGeofencesEnabled(true);
                if (listener != null) {
                    listener.onRegistrationComplete();
                }
            }

            @Override
            public void onFailure(String reason) {
                if (listener != null) {
                    listener.onRegistrationFailed(reason);
                }
            }
        });
    }

    private void clearGeofences(final RegistrationListener listener) {

        if (isPermissionForGeofences()) {

            geofenceUpdater.clearGeofencesFromMonitorAndStore(new GeofenceUpdater.GeofenceUpdaterListener() {

                @Override
                public void onSuccess() {
                    pushPreferencesProvider.setAreGeofencesEnabled(false);
                    pushPreferencesProvider.setLastGeofenceUpdate(GeofenceEngine.NEVER_UPDATED_GEOFENCES);
                    if (listener != null) {
                        listener.onRegistrationComplete();
                    }
                }

                @Override
                public void onFailure(String reason) {
                    if (listener != null) {
                        listener.onRegistrationFailed(reason);
                    }
                }
            });

        } else {

            geofenceUpdater.clearGeofencesFromStoreOnly(new GeofenceUpdater.GeofenceUpdaterListener() {

                // TODO - update GeofenceStatus?

                @Override
                public void onSuccess() {
                    pushPreferencesProvider.setLastGeofenceUpdate(GeofenceEngine.NEVER_UPDATED_GEOFENCES);
                    pushPreferencesProvider.setAreGeofencesEnabled(false);
                    if (listener != null) {
                        listener.onRegistrationComplete();
                    }
                }

                @Override
                public void onFailure(String reason) {
                    if (listener != null) {
                        listener.onRegistrationFailed(reason);
                    }
                }
            });
        }
    }

    private void clearPCFPushRegistrationPreferences() {
        pushPreferencesProvider.setPCFPushDeviceRegistrationId(null);
        pushPreferencesProvider.setPlatformUuid(null);
        pushPreferencesProvider.setPlatformSecret(null);
        pushPreferencesProvider.setDeviceAlias(null);
        pushPreferencesProvider.setServiceUrl(null);
        pushPreferencesProvider.setTags(null);
    }
}
