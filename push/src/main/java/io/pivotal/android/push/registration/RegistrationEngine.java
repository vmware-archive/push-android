/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.registration;

import android.content.Context;
import android.content.pm.PackageManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Set;

import io.pivotal.android.push.PushParameters;
import io.pivotal.android.push.backend.api.PCFPushRegistrationApiRequest;
import io.pivotal.android.push.backend.api.PCFPushRegistrationApiRequestImpl;
import io.pivotal.android.push.backend.api.PCFPushRegistrationApiRequestProvider;
import io.pivotal.android.push.backend.api.PCFPushRegistrationListener;
import io.pivotal.android.push.backend.geofence.PCFPushGetGeofenceUpdatesApiRequest;
import io.pivotal.android.push.geofence.GeofenceEngine;
import io.pivotal.android.push.geofence.GeofencePersistentStore;
import io.pivotal.android.push.geofence.GeofenceRegistrar;
import io.pivotal.android.push.geofence.GeofenceStatusUtil;
import io.pivotal.android.push.geofence.GeofenceUpdater;
import io.pivotal.android.push.prefs.Pivotal;
import io.pivotal.android.push.prefs.PushPreferencesProvider;
import io.pivotal.android.push.prefs.PushPreferencesProviderImpl;
import io.pivotal.android.push.prefs.PushRequestHeaders;
import io.pivotal.android.push.util.FileHelper;
import io.pivotal.android.push.util.Logger;
import io.pivotal.android.push.util.NetworkWrapper;
import io.pivotal.android.push.util.NetworkWrapperImpl;
import io.pivotal.android.push.util.TimeProvider;
import io.pivotal.android.push.util.Util;
import io.pivotal.android.push.version.GeofenceStatus;

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
 *  On a fresh install, the Registration Engine will register with Google Firebase Cloud Messaging (FCM) and then with the
 *  Pivotal CF Mobile Services server.
 *
 *  If any of the Pivotal CF Mobile Services registration parameters (platform_uuid, platform_secret, device_alias), or
 *  if FCM provides a different token ID than a previous install, then the Registration
 *  Engine will attempt to update its registration wih the Pivotal CF Mobile Services Push server (i.e.: HTTP PUT).
 *
 *  If, however, the base_server_url parameter is different than the existing registration, then the Registration
 *  Engine will abandon its registration with the previous server and make a new one (i.e.: HTTP POST) with the new
 *  server.
 *
 *  The Registration Engine is also designed to successfully complete previous registrations that have failed. For
 *  instance, if the previous registration attempt failed to complete the registration with PCF Push then it will
 *  try to re-register with the server if called again.
 */
public class RegistrationEngine {

    public static final int MAXIMUM_CUSTOM_USER_ID_LENGTH = 255;
    private Context context;
    private FirebaseInstanceId firebaseInstanceId;
    private GoogleApiAvailability googleApiAvailability;
    private PushPreferencesProvider pushPreferencesProvider;
    private PushRequestHeaders pushRequestHeaders;
    private PCFPushRegistrationApiRequestProvider pcfPushRegistrationApiRequestProvider;
    private GeofenceUpdater geofenceUpdater;
    private GeofenceEngine geofenceEngine;
    private GeofenceStatusUtil geofenceStatusUtil;
    private String packageName;
    private String previousFcmTokenId = null;
    private String previousPCFPushDeviceRegistrationId = null;
    private String previousPlatformUuid;
    private String previousPlatformSecret;
    private String previousDeviceAlias;
    private String previousCustomUserId;
    private String previousServiceUrl;

    public static RegistrationEngine getRegistrationEngine(Context context) {
        final PushPreferencesProvider pushPreferencesProvider = new PushPreferencesProviderImpl(context);
        final PushRequestHeaders pushRequestHeaders = PushRequestHeaders.getInstance(context);
        final NetworkWrapper networkWrapper = new NetworkWrapperImpl();
        final PCFPushRegistrationApiRequest dummyPCFPushRegistrationApiRequest = new PCFPushRegistrationApiRequestImpl(context, networkWrapper);
        final PCFPushRegistrationApiRequestProvider PCFPushRegistrationApiRequestProvider = new PCFPushRegistrationApiRequestProvider(dummyPCFPushRegistrationApiRequest);
        final PCFPushGetGeofenceUpdatesApiRequest geofenceUpdatesApiRequest = new PCFPushGetGeofenceUpdatesApiRequest(context, networkWrapper);
        final GeofenceRegistrar geofenceRegistrar = new GeofenceRegistrar(context);
        final FileHelper fileHelper = new FileHelper(context);
        final TimeProvider timeProvider = new TimeProvider();
        final GeofencePersistentStore geofencePersistentStore = new GeofencePersistentStore(context, fileHelper);
        final GeofenceEngine geofenceEngine = new GeofenceEngine(geofenceRegistrar, geofencePersistentStore, timeProvider, pushPreferencesProvider);
        final GeofenceUpdater geofenceUpdater = new GeofenceUpdater(context, geofenceUpdatesApiRequest, geofenceEngine, pushPreferencesProvider, pushRequestHeaders);
        final GeofenceStatusUtil geofenceStatusUtil = new GeofenceStatusUtil(context);

        return new RegistrationEngine(context,
                context.getPackageName(),
                FirebaseInstanceId.getInstance(),
                GoogleApiAvailability.getInstance(),
                pushPreferencesProvider,
                pushRequestHeaders,
                PCFPushRegistrationApiRequestProvider,
                geofenceUpdater,
                geofenceEngine, geofenceStatusUtil);
    }

    /**
     * Instantiate an instance of the RegistrationEngine.
     *
     * All the parameters are required.  None may be null.
     * @param context  A context
     * @param packageName  The currenly application package name.
     * @param firebaseInstanceId Some object that can provide Firebase Token ID
     * @param googleApiAvailability Some object that can used to determine if Google Play is available.
     * @param pushPreferencesProvider  Some object that can provide persistent storage for push preferences.
     * @param pushRequestHeaders Some object that can provide storage for push request headers
     * @param pcfPushRegistrationApiRequestProvider  Some object that can provide PCFPushRegistrationApiRequest objects.
     * @param geofenceUpdater  Some object that can be used to download geofence updates from the server.
     * @param geofenceEngine  Some object that can be used to register geofences.
     * @param geofenceStatusUtil  Some object that can be used to change the geofence status
     */
    public RegistrationEngine(Context context,
                              String packageName,
                              FirebaseInstanceId firebaseInstanceId,
                              GoogleApiAvailability googleApiAvailability,
                              PushPreferencesProvider pushPreferencesProvider,
                              PushRequestHeaders pushRequestHeaders,
                              PCFPushRegistrationApiRequestProvider pcfPushRegistrationApiRequestProvider,
                              GeofenceUpdater geofenceUpdater,
                              GeofenceEngine geofenceEngine,
                              GeofenceStatusUtil geofenceStatusUtil) {

        verifyArguments(context,
                packageName,
                firebaseInstanceId,
                googleApiAvailability,
                pushPreferencesProvider,
                pushRequestHeaders,
                pcfPushRegistrationApiRequestProvider,
                geofenceUpdater,
                geofenceEngine, geofenceStatusUtil);

        saveArguments(context,
                packageName,
                firebaseInstanceId,
                googleApiAvailability,
                pushPreferencesProvider,
                pushRequestHeaders,
                pcfPushRegistrationApiRequestProvider,
                geofenceUpdater,
                geofenceEngine, geofenceStatusUtil);
    }

    private void verifyArguments(Context context,
                                 String packageName,
                                 FirebaseInstanceId firebaseInstanceId,
                                 GoogleApiAvailability googleApiAvailability,
                                 PushPreferencesProvider pushPreferencesProvider,
                                 PushRequestHeaders pushRequestHeaders,
                                 PCFPushRegistrationApiRequestProvider pcfPushRegistrationApiRequestProvider,
                                 GeofenceUpdater geofenceUpdater,
                                 GeofenceEngine geofenceEngine,
                                 GeofenceStatusUtil geofenceStatusUtil) {

        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
        if (packageName == null) {
            throw new IllegalArgumentException("packageName may not be null");
        }
        if (firebaseInstanceId == null) {
            throw new IllegalArgumentException("firebaseInstanceId may not be null");
        }
        if (googleApiAvailability == null) {
            throw new IllegalArgumentException("googleApiAvailability may not be null");
        }
        if (pushPreferencesProvider == null) {
            throw new IllegalArgumentException("pushPreferencesProvider may not be null");
        }
        if (pushRequestHeaders == null) {
            throw new IllegalArgumentException("pushRequestHeaders may not be null");
        }
        if (pcfPushRegistrationApiRequestProvider == null) {
            throw new IllegalArgumentException("pcfPushRegistrationApiRequestProvider may not be null");
        }
        if (geofenceUpdater == null) {
            throw new IllegalArgumentException("geofenceUpdater may not be null");
        }
        if (geofenceEngine == null) {
            throw new IllegalArgumentException("geofenceEngine may not be null");
        }
        if (geofenceStatusUtil == null) {
            throw new IllegalArgumentException("geofenceStatusUtil may not be null");
        }
    }

    private void saveArguments(Context context,
                               String packageName,
                               FirebaseInstanceId firebaseInstanceId,
                               GoogleApiAvailability googleApiAvailability,
                               PushPreferencesProvider pushPreferencesProvider,
                               PushRequestHeaders pushRequestHeaders,
                               PCFPushRegistrationApiRequestProvider pcfPushRegistrationApiRequestProvider,
                               GeofenceUpdater geofenceUpdater,
                               GeofenceEngine geofenceEngine,
                               GeofenceStatusUtil geofenceStatusUtil) {

        this.context = context;
        this.packageName = packageName;
        this.firebaseInstanceId = firebaseInstanceId;
        this.googleApiAvailability = googleApiAvailability;
        this.pushPreferencesProvider = pushPreferencesProvider;
        this.pushRequestHeaders = pushRequestHeaders;
        this.pcfPushRegistrationApiRequestProvider = pcfPushRegistrationApiRequestProvider;
        this.geofenceUpdater = geofenceUpdater;
        this.geofenceEngine = geofenceEngine;
        this.geofenceStatusUtil = geofenceStatusUtil;
        this.previousFcmTokenId = pushPreferencesProvider.getFcmTokenId();
        this.previousPCFPushDeviceRegistrationId = pushPreferencesProvider.getPCFPushDeviceRegistrationId();
        this.previousPlatformUuid = pushPreferencesProvider.getPlatformUuid();
        this.previousPlatformSecret = pushPreferencesProvider.getPlatformSecret();
        this.previousDeviceAlias = pushPreferencesProvider.getDeviceAlias();
        this.previousCustomUserId = pushPreferencesProvider.getCustomUserId();
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

        if (!isGooglePlayServicesInstalled(context)) {
            if (listener != null) {
                listener.onRegistrationFailed("Google Play Services is not available");
            }
        }

        verifyRegistrationArguments(parameters);

        // Save the given package name so that the message receiver service can see it
        pushPreferencesProvider.setPackageName(packageName);

        String fcmTokenId = firebaseInstanceId.getToken();
        if (fcmTokenId == null) {
            Logger.e("FCM returned null fcmTokenId");
            if (listener != null) {
                listener.onRegistrationFailed("FCM returned null fcmTokenId");
            }
            return;
        }

        final boolean isNewFcmTokenId;
        if (!isPreviousFcmTokenIdEmpty() && previousFcmTokenId.equals(fcmTokenId)) {
            Logger.v("New fcmTokenId from FCM is the same as the previous one.");
            isNewFcmTokenId = false;
        } else {
            isNewFcmTokenId = true;
        }

        if (isPreviousFcmTokenIdEmpty() || isNewFcmTokenId) {
            pushPreferencesProvider.setFcmTokenId(fcmTokenId);
        }

        final boolean isServiceUrlUpdated = isServiceUrlUpdated(parameters);
        if (isServiceUrlUpdated) {
            Logger.v("The PCF Push serviceUrl has been updated. A new registration with the PCF Push server is required.");
        }

        final boolean isPlatformUpdated = isPlatformUpdated(parameters);
        if (isPlatformUpdated) {
            Logger.v("The PCF Push platform has been updated. A new registration with the PCF Push server is required.");
        }

        if (isPCFPushUpdateRegistrationRequired(fcmTokenId, parameters) && !isServiceUrlUpdated && !isPlatformUpdated) {
            registerUpdateDeviceWithPCFPush(fcmTokenId, previousPCFPushDeviceRegistrationId, pushPreferencesProvider.getTags(), parameters, listener);

        } else if (isNewFcmTokenId || isServiceUrlUpdated || isPlatformUpdated) {
                registerNewDeviceWithPCFPush(fcmTokenId, pushPreferencesProvider.getTags(), parameters, listener);

        } else if (isGeofenceUpdateRequired(parameters)) {
            updateGeofences(listener);

        } else if (isClearGeofencesRequired(parameters)) {
            clearGeofences(listener);

        } else {
            Logger.v("Already registered");
            if (listener != null) {
                listener.onRegistrationComplete();
            }
        }
    }

    /**
     * Start a FCM token update attempt. It informs the Push backend of the device's new FCM token id.
     * This method is asynchronous and will return before update is complete.
     *
     * This function is not intended to be used directly. It will be called automatically by the Push instance
     * when it receives a token update notification from FcmTokenIDService.
     */
    public void updateDeviceTokenId() {
        PushParameters parameters = new PushParameters(pushPreferencesProvider.getPlatformUuid(),
                pushPreferencesProvider.getPlatformSecret(),
                pushPreferencesProvider.getServiceUrl(),
                pushPreferencesProvider.getDeviceAlias(),
                pushPreferencesProvider.getCustomUserId(),
                pushPreferencesProvider.getTags(),
                pushPreferencesProvider.areGeofencesEnabled(),
                Pivotal.getSslCertValidationMode(context),
                Pivotal.getPinnedSslCertificateNames(context),
                pushRequestHeaders.getRequestHeaders()
        );

        registerDevice(parameters, null);
    }

    private void verifyRegistrationArguments(PushParameters parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("parameters may not be null");
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
        if (parameters.getCustomUserId() != null && parameters.getCustomUserId().length() > MAXIMUM_CUSTOM_USER_ID_LENGTH) {
            throw new IllegalArgumentException("customUserId must be fewer than or equal to "+ MAXIMUM_CUSTOM_USER_ID_LENGTH + " characters");
        }
    }

    private boolean isPreviousFcmTokenIdEmpty() {
        return previousFcmTokenId == null || previousFcmTokenId.isEmpty();
    }

    private boolean haveTagsBeenUpdated(PushParameters parameters) {
        final Set<String> savedTags = Util.lowercaseTags(pushPreferencesProvider.getTags());
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

    private boolean isPCFPushUpdateRegistrationRequired(String newFcmTokenId, PushParameters parameters) {
        final boolean isFcmTokenIdDifferent = !isPreviousFcmTokenIdEmpty() && !previousFcmTokenId.equals(newFcmTokenId); // If previous token is empty, this means a new registration, not an registration update
        final boolean isPreviousPCFPushDeviceRegistrationIdEmpty = previousPCFPushDeviceRegistrationId == null || previousPCFPushDeviceRegistrationId.isEmpty();
        if (isPreviousPCFPushDeviceRegistrationIdEmpty) {
            Logger.v("previousPCFPushDeviceRegistrationId is empty. Device will NOT require an update-registration with PCF Push.");
            return false;
        }
        if (isFcmTokenIdDifferent) {
            Logger.v("The fcmTokenId is different. Device will need to update its registration with PCF Push.");
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
        final boolean isPreviousDeviceAliasEmpty = previousDeviceAlias == null || previousDeviceAlias.isEmpty();
        final boolean isNewDeviceAliasEmpty = parameters.getDeviceAlias() == null || parameters.getDeviceAlias().isEmpty();
        final boolean isPreviousCustomUserIdEmpty = previousCustomUserId == null || previousCustomUserId.isEmpty();
        final boolean isNewCustomUserIdEmpty = parameters.getCustomUserId() == null || parameters.getCustomUserId().isEmpty();
        final boolean isDeviceAliasUpdated = (isPreviousDeviceAliasEmpty && !isNewDeviceAliasEmpty) || (!isPreviousDeviceAliasEmpty && isNewDeviceAliasEmpty) || (!isNewDeviceAliasEmpty && !parameters.getDeviceAlias().equals(previousDeviceAlias));
        final boolean isCustomUserIdUpdated = (isPreviousCustomUserIdEmpty && !isNewCustomUserIdEmpty) || (!isPreviousCustomUserIdEmpty && isNewCustomUserIdEmpty) || (!isNewCustomUserIdEmpty && !parameters.getCustomUserId().equals(previousCustomUserId));
        return isDeviceAliasUpdated || isCustomUserIdUpdated;
    }

    private boolean isPlatformUpdated(PushParameters parameters) {
        final boolean isPreviousPlatformUuidEmpty = previousPlatformUuid == null || previousPlatformUuid.isEmpty();
        final boolean isPlatformUuidUpdated = (isPreviousPlatformUuidEmpty && !parameters.getPlatformUuid().isEmpty()) || !parameters.getPlatformUuid().equals(previousPlatformUuid);
        final boolean isPreviousPlatformSecretEmpty = previousPlatformSecret == null || previousPlatformSecret.isEmpty();
        final boolean isPlatformSecretUpdated = (isPreviousPlatformSecretEmpty && !parameters.getPlatformSecret().isEmpty()) || !parameters.getPlatformSecret().equals(previousPlatformSecret);
        return isPlatformSecretUpdated || isPlatformUuidUpdated;
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

    private void registerUpdateDeviceWithPCFPush(String fcmTokenId,
                                                 String pcfPushDeviceRegistrationId,
                                                 Set<String> savedTags,
                                                 PushParameters parameters,
                                                 RegistrationListener listener) {

        Logger.i("Initiating update device registration with PCF Push.");
        final PCFPushRegistrationApiRequest PCFPushRegistrationApiRequest = pcfPushRegistrationApiRequestProvider.getRequest();
        PCFPushRegistrationApiRequest.startUpdateDeviceRegistration(fcmTokenId,
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
                    geofenceEngine.reregisterCurrentLocations(parameters.getTags());
                }

                Logger.i("Saving PCF Push device registration ID: " + pcfPushDeviceRegistrationId);
                pushPreferencesProvider.setPCFPushDeviceRegistrationId(pcfPushDeviceRegistrationId);

                Logger.v("Saving updated platformUuid, platformSecret, deviceAlias, and serviceUrl");
                pushPreferencesProvider.setPlatformUuid(parameters.getPlatformUuid());
                pushPreferencesProvider.setPlatformSecret(parameters.getPlatformSecret());
                pushPreferencesProvider.setDeviceAlias(parameters.getDeviceAlias());
                pushPreferencesProvider.setCustomUserId(parameters.getCustomUserId());
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
                    geofenceEngine.reregisterCurrentLocations(parameters.getTags());
                }

                clearPCFPushRegistrationPreferences();

                if (listener != null) {
                    listener.onRegistrationFailed(reason);
                }
            }
        };
    }

    private void registerNewDeviceWithPCFPush(final String fcmTokenId,
                                              Set<String> savedTags,
                                              PushParameters parameters,
                                              RegistrationListener listener) {

        Logger.i("Initiating new device registration with PCF Push.");
        final PCFPushRegistrationApiRequest PCFPushRegistrationApiRequest = pcfPushRegistrationApiRequestProvider.getRequest();
        PCFPushRegistrationApiRequest.startNewDeviceRegistration(fcmTokenId, savedTags, parameters, getPCFPushNewRegistrationListener(parameters, listener));
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
                pushPreferencesProvider.setCustomUserId(parameters.getCustomUserId());
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

                @Override
                public void onSuccess() {

                    pushPreferencesProvider.setLastGeofenceUpdate(GeofenceEngine.NEVER_UPDATED_GEOFENCES);
                    pushPreferencesProvider.setAreGeofencesEnabled(false);

                    setGeofenceStatus();

                    if (listener != null) {
                        listener.onRegistrationComplete();
                    }
                }

                @Override
                public void onFailure(String reason) {

                    setGeofenceStatus();

                    if (listener != null) {
                        listener.onRegistrationFailed(reason);
                    }
                }

                private void setGeofenceStatus() {
                    final GeofenceStatus status = new GeofenceStatus(true, "Permission for geofences is not available.", 0);
                    geofenceStatusUtil.saveGeofenceStatusAndSendBroadcast(status);
                }
            });
        }
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

    private boolean isGooglePlayServicesInstalled(Context context) {
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            final String errorString = googleApiAvailability.getErrorString(resultCode);
            Logger.e("Google Play Services is not available: " + errorString);
            return false;
        }
        return true;
    }
}
