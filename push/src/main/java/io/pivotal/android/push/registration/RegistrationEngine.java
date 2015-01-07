/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.registration;

import android.content.Context;

import java.util.Set;

import io.pivotal.android.push.RegistrationParameters;
import io.pivotal.android.push.backend.BackEndRegistrationApiRequest;
import io.pivotal.android.push.backend.BackEndRegistrationApiRequestProvider;
import io.pivotal.android.push.backend.BackEndRegistrationListener;
import io.pivotal.android.push.gcm.GcmProvider;
import io.pivotal.android.push.gcm.GcmRegistrationApiRequest;
import io.pivotal.android.push.gcm.GcmRegistrationApiRequestProvider;
import io.pivotal.android.push.gcm.GcmRegistrationListener;
import io.pivotal.android.push.gcm.GcmUnregistrationApiRequest;
import io.pivotal.android.push.gcm.GcmUnregistrationApiRequestProvider;
import io.pivotal.android.push.gcm.GcmUnregistrationListener;
import io.pivotal.android.push.prefs.PushPreferencesProvider;
import io.pivotal.android.push.util.Logger;
import io.pivotal.android.push.util.ServiceStarter;
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
 *  Pivotal CF Mobile Services back-end server.
 *
 *  If the GCM Sender ID is different then the previous registration, then the Registration Engine will
 *  attempt to unregister the device with GCM (Google Cloud Messaging) first.
 *
 *  If the application version code or the GCM Sender ID is updated since the previous registration, then the
 *  Registration Engine will attempt to re-register with GCM.
 *
 *  If any of the Pivotal CF Mobile Services registration parameters (variant_uuid, variant_secret, device_alias), or
 *  if a GCM registration provides a different device registration ID than a previous install, then the Registration
 *  Engine will attempt to update its registration wih the Pivotal CF Mobile Services Push server (i.e.: HTTP PUT).
 *
 *  If, however, the base_server_url parameter is different than the existing registration, then the Registration
 *  Engine will abandon its registration with the previous server and make a new one (i.e.: HTTP POST) with the new
 *  server.
 *
 *  The Registration Engine is also designed to successfully complete previous registrations that have failed. For
 *  instance, if the previous registration attempt successfully registered with GCM but failed to complete the
 *  registration with the back-end then it will simply try to re-register with the back-end if called again.
 */
public class RegistrationEngine {

    private Context context;
    private GcmProvider gcmProvider;
    private PushPreferencesProvider pushPreferencesProvider;
    private GcmRegistrationApiRequestProvider gcmRegistrationApiRequestProvider;
    private GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider;
    private BackEndRegistrationApiRequestProvider backEndRegistrationApiRequestProvider;
    private VersionProvider versionProvider;
    private ServiceStarter serviceStarter;
    private String packageName;
    private String previousGcmDeviceRegistrationId = null;
    private String previousBackEndDeviceRegistrationId = null;
    private String previousGcmSenderId;
    private String previousVariantUuid;
    private String previousVariantSecret;
    private String previousDeviceAlias;
    private String previousBackEndServerUrl;

    /**
     * Instantiate an instance of the RegistrationEngine.
     *
     * All the parameters are required.  None may be null.
     * @param context  A context
     * @param packageName
     * @param gcmProvider  Some object that can provide the GCM services.
     * @param pushPreferencesProvider  Some object that can provide persistent storage for push preferences.
     * @param gcmRegistrationApiRequestProvider  Some object that can provide GCMRegistrationApiRequest objects.
     * @param gcmUnregistrationApiRequestProvider  Some object that can provide GCMUnregistrationApiRequest objects.
     * @param backEndRegistrationApiRequestProvider  Some object that can provide BackEndRegistrationApiRequest objects.
     * @param versionProvider  Some object that can provide the application version.
     * @param serviceStarter  Some object that can be used to start services.
     */
    public RegistrationEngine(Context context,
                              String packageName,
                              GcmProvider gcmProvider,
                              PushPreferencesProvider pushPreferencesProvider,
                              GcmRegistrationApiRequestProvider gcmRegistrationApiRequestProvider,
                              GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider,
                              BackEndRegistrationApiRequestProvider backEndRegistrationApiRequestProvider,
                              VersionProvider versionProvider,
                              ServiceStarter serviceStarter) {

        verifyArguments(context,
                packageName,
                gcmProvider,
                pushPreferencesProvider,
                gcmRegistrationApiRequestProvider,
                gcmUnregistrationApiRequestProvider,
                backEndRegistrationApiRequestProvider,
                versionProvider, serviceStarter);

        saveArguments(context,
                packageName,
                gcmProvider,
                pushPreferencesProvider,
                gcmRegistrationApiRequestProvider,
                gcmUnregistrationApiRequestProvider,
                backEndRegistrationApiRequestProvider,
                versionProvider, serviceStarter);
    }

    private void verifyArguments(Context context,
                                 String packageName,
                                 GcmProvider gcmProvider,
                                 PushPreferencesProvider pushPreferencesProvider,
                                 GcmRegistrationApiRequestProvider gcmRegistrationApiRequestProvider,
                                 GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider,
                                 BackEndRegistrationApiRequestProvider backEndRegistrationApiRequestProvider,
                                 VersionProvider versionProvider,
                                 ServiceStarter serviceStarter) {

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
        if (backEndRegistrationApiRequestProvider == null) {
            throw new IllegalArgumentException("backEndRegistrationApiRequestProvider may not be null");
        }
        if (versionProvider == null) {
            throw new IllegalArgumentException("versionProvider may not be null");
        }
        if (serviceStarter == null) {
            throw new IllegalArgumentException("serviceStarter may not be null");
        }
    }

    private void saveArguments(Context context,
                               String packageName,
                               GcmProvider gcmProvider,
                               PushPreferencesProvider pushPreferencesProvider,
                               GcmRegistrationApiRequestProvider gcmRegistrationApiRequestProvider,
                               GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider,
                               BackEndRegistrationApiRequestProvider backEndRegistrationApiRequestProvider,
                               VersionProvider versionProvider,
                               ServiceStarter serviceStarter) {

        this.context = context;
        this.packageName = packageName;
        this.gcmProvider = gcmProvider;
        this.pushPreferencesProvider = pushPreferencesProvider;
        this.gcmRegistrationApiRequestProvider = gcmRegistrationApiRequestProvider;
        this.gcmUnregistrationApiRequestProvider = gcmUnregistrationApiRequestProvider;
        this.backEndRegistrationApiRequestProvider = backEndRegistrationApiRequestProvider;
        this.versionProvider = versionProvider;
        this.serviceStarter = serviceStarter;
        this.previousGcmDeviceRegistrationId = pushPreferencesProvider.getGcmDeviceRegistrationId();
        this.previousBackEndDeviceRegistrationId = pushPreferencesProvider.getBackEndDeviceRegistrationId();
        this.previousGcmSenderId = pushPreferencesProvider.getGcmSenderId();
        this.previousVariantUuid = pushPreferencesProvider.getVariantUuid();
        this.previousVariantSecret = pushPreferencesProvider.getVariantSecret();
        this.previousDeviceAlias = pushPreferencesProvider.getDeviceAlias();
        this.previousBackEndServerUrl = pushPreferencesProvider.getBaseServerUrl();
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
    public void registerDevice(RegistrationParameters parameters, final RegistrationListener listener) {

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

        } else if (isBackEndUpdateRegistrationRequired(previousGcmDeviceRegistrationId, parameters)) {
            registerUpdateDeviceWithBackEnd(previousGcmDeviceRegistrationId, previousBackEndDeviceRegistrationId, pushPreferencesProvider.getTags(), parameters, listener);

        } else if (isBackEndNewRegistrationRequired(parameters)) {
            registerNewDeviceWithBackEnd(previousGcmDeviceRegistrationId, pushPreferencesProvider.getTags(), parameters, listener);

        } else {
            Logger.v("Already registered with GCM and back-end");
            if (listener != null) {
                listener.onRegistrationComplete();
            }
        }
    }

    private void verifyRegistrationArguments(RegistrationParameters parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("parameters may not be null");
        }
        if (parameters.getGcmSenderId() == null || parameters.getGcmSenderId().isEmpty()) {
            throw new IllegalArgumentException("parameters.senderId may not be null or empty");
        }
        if (parameters.getVariantUuid() == null || parameters.getVariantUuid().isEmpty()) {
            throw new IllegalArgumentException("parameters.variantUuid may not be null or empty");
        }
        if (parameters.getVariantSecret() == null || parameters.getVariantSecret().isEmpty()) {
            throw new IllegalArgumentException("parameters.variantSecret may not be null or empty");
        }
        if (parameters.getDeviceAlias() == null) {
            throw new IllegalArgumentException("parameters.deviceAlias may not be null");
        }
        if (parameters.getBaseServerUrl() == null) {
            throw new IllegalArgumentException("parameters.baseServerUrl may not be null");
        }
    }

    private boolean isGcmRegistrationRequired(RegistrationParameters parameters) {
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

    private boolean isUpdatedGcmSenderId(RegistrationParameters parameters) {
        return !parameters.getGcmSenderId().equals(previousGcmSenderId);
    }

    private boolean haveTagsBeenUpdated(RegistrationParameters parameters) {
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

    private boolean isBackEndNewRegistrationRequired(RegistrationParameters parameters) {
        final boolean isPreviousVariantUuidEmpty = previousVariantUuid == null || previousVariantUuid.isEmpty();
        final boolean isBackEndServerUrlUpdated = isBackEndServerUrlUpdated(parameters);
        if (isEmptyPreviousGcmDeviceRegistrationId()) {
            Logger.v("previousGcmDeviceRegistrationId is empty. Device registration with the back-end will be required.");
        }
        if (isPreviousVariantUuidEmpty) {
            Logger.v("previousVariantUuid is empty. Device registration with the back-end will be required.");
        }
        if (isBackEndServerUrlUpdated) {
            Logger.v("The backEndServerUrl has been updated. Device registration with the back-end will be required.");
        }
        return isEmptyPreviousGcmDeviceRegistrationId() || isPreviousVariantUuidEmpty || areRegistrationParametersUpdated(parameters) || isBackEndServerUrlUpdated;
    }

    private boolean isBackEndUpdateRegistrationRequired(String newGcmDeviceRegistrationId, RegistrationParameters parameters) {
        final boolean isGcmDeviceRegistrationIdDifferent = isEmptyPreviousGcmDeviceRegistrationId() || !previousGcmDeviceRegistrationId.equals(newGcmDeviceRegistrationId);
        final boolean isPreviousBackEndDeviceRegistrationIdEmpty = previousBackEndDeviceRegistrationId == null || previousBackEndDeviceRegistrationId.isEmpty();
        if (isPreviousBackEndDeviceRegistrationIdEmpty) {
            Logger.v("previousBackEndDeviceRegistrationId is empty. Device will NOT require an update-registration with the back-end.");
            return false;
        }
        if (isGcmDeviceRegistrationIdDifferent) {
            Logger.v("The gcmDeviceRegistrationId is different. Device will need to update its registration with the back-end.");
            return true;
        }
        if (areRegistrationParametersUpdated(parameters)) {
            Logger.v("The registration parameters have been updated. Device will need to update its registration with the back-end.");
            return true;
        }
        if (haveTagsBeenUpdated(parameters)) {
            Logger.v("App tags changed. Device will need to update its registration with the back-end.");
            return true;
        }
        Logger.v("It does not seem that the device needs to update its registration with the back-end.");
        return false;
    }

    private boolean areRegistrationParametersUpdated(RegistrationParameters parameters) {
        final boolean isPreviousVariantUuidEmpty = previousVariantUuid == null || previousVariantUuid.isEmpty();
        final boolean isVariantUuidUpdated = (isPreviousVariantUuidEmpty && !parameters.getVariantUuid().isEmpty()) || !parameters.getVariantUuid().equals(previousVariantUuid);
        final boolean isPreviousVariantSecretEmpty = previousVariantSecret == null || previousVariantSecret.isEmpty();
        final boolean isVariantSecretUpdated = (isPreviousVariantSecretEmpty && !parameters.getVariantSecret().isEmpty()) || !parameters.getVariantSecret().equals(previousVariantSecret);
        final boolean isPreviousDeviceAliasEmpty = previousDeviceAlias == null || previousDeviceAlias.isEmpty();
        final boolean isNewDeviceAliasEmpty = parameters.getDeviceAlias() == null || parameters.getDeviceAlias().isEmpty();
        final boolean isDeviceAliasUpdated = (isPreviousDeviceAliasEmpty && !isNewDeviceAliasEmpty) || (!isPreviousDeviceAliasEmpty && isNewDeviceAliasEmpty) || !parameters.getDeviceAlias().equals(previousDeviceAlias);
        return isDeviceAliasUpdated || isVariantSecretUpdated || isVariantUuidUpdated;
    }

    private boolean isBackEndServerUrlUpdated(RegistrationParameters parameters) {
        final boolean isPreviousBackEndServerUrlEmpty = previousBackEndServerUrl == null;
        final boolean isBackEndServerUrlUpdated = (isPreviousBackEndServerUrlEmpty && parameters.getBaseServerUrl() != null) || !parameters.getBaseServerUrl().equals(previousBackEndServerUrl);
        return isBackEndServerUrlUpdated;
    }

    private void unregisterDeviceWithGcm(final RegistrationParameters parameters, final RegistrationListener listener) {
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

    private void registerDeviceWithGcm(final RegistrationParameters parameters, final RegistrationListener listener) {
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

                final boolean isBackEndServerUrlUpdated = isBackEndServerUrlUpdated(parameters);
                if (isBackEndServerUrlUpdated) {
                    Logger.v("The back-end server has been updated. A new registration with the back-end server is required.");
                }

                if (isBackEndUpdateRegistrationRequired(gcmDeviceRegistrationId, parameters) && !isBackEndServerUrlUpdated) {
                    registerUpdateDeviceWithBackEnd(gcmDeviceRegistrationId, previousBackEndDeviceRegistrationId, pushPreferencesProvider.getTags(), parameters, listener);

                }  else if (isNewGcmDeviceRegistrationId || isBackEndServerUrlUpdated) {
                    registerNewDeviceWithBackEnd(gcmDeviceRegistrationId, pushPreferencesProvider.getTags(), parameters, listener);

                } else if (listener != null) {
                    listener.onRegistrationComplete();
                }
            }

            @Override
            public void onGcmRegistrationFailed(String reason) {
                if (listener != null) {
                    listener.onRegistrationFailed(reason);
                }
            }
        });
    }

    private void registerUpdateDeviceWithBackEnd(String gcmDeviceRegistrationId,
                                                 String backEndDeviceRegistrationId,
                                                 Set<String> savedTags,
                                                 RegistrationParameters parameters,
                                                 RegistrationListener listener) {

        Logger.i("Initiating update device registration with the back-end.");
        final BackEndRegistrationApiRequest backEndRegistrationApiRequest = backEndRegistrationApiRequestProvider.getRequest();
        backEndRegistrationApiRequest.startUpdateDeviceRegistration(gcmDeviceRegistrationId,
                backEndDeviceRegistrationId,
                savedTags,
                parameters,
                getBackEndUpdateRegistrationListener(parameters, listener));
    }

    private BackEndRegistrationListener getBackEndUpdateRegistrationListener(final RegistrationParameters parameters, final RegistrationListener listener) {
        return new BackEndRegistrationListener() {

            @Override
            public void onBackEndRegistrationSuccess(String backEndDeviceRegistrationId) {

                if (backEndDeviceRegistrationId == null) {
                    Logger.e("Back-end server return null backEndDeviceRegistrationId upon registration update.");

                    // The server didn't return a valid registration response.  We should clear our local
                    // registration data so that we can attempt to reregister next time.
                    clearBackEndRegistrationPreferences();

                    if (listener != null) {
                        listener.onRegistrationFailed("Back-end server return null backEndDeviceRegistrationId upon registration update.");
                    }
                    return;
                }

                Logger.i("Saving back-end device registration ID: " + backEndDeviceRegistrationId);
                pushPreferencesProvider.setBackEndDeviceRegistrationId(backEndDeviceRegistrationId);

                Logger.v("Saving updated variantUuid, variantSecret, deviceAlias, and baseServerUrl");
                pushPreferencesProvider.setVariantUuid(parameters.getVariantUuid());
                pushPreferencesProvider.setVariantSecret(parameters.getVariantSecret());
                pushPreferencesProvider.setDeviceAlias(parameters.getDeviceAlias());
                pushPreferencesProvider.setBaseServerUrl(parameters.getBaseServerUrl());
                pushPreferencesProvider.setTags(parameters.getTags());
                Logger.v("Saving tags: " + parameters.getTags());

                logPushRegisteredEvent(parameters.getVariantUuid(), backEndDeviceRegistrationId);

                if (listener != null) {
                    listener.onRegistrationComplete();
                }
            }

            @Override
            public void onBackEndRegistrationFailed(String reason) {

                clearBackEndRegistrationPreferences();

                if (listener != null) {
                    listener.onRegistrationFailed(reason);
                }
            }
        };
    }

    private void registerNewDeviceWithBackEnd(final String gcmDeviceRegistrationId,
                                              Set<String> savedTags,
                                              RegistrationParameters parameters,
                                              RegistrationListener listener) {

        Logger.i("Initiating new device registration with the back-end.");
        final BackEndRegistrationApiRequest backEndRegistrationApiRequest = backEndRegistrationApiRequestProvider.getRequest();
        backEndRegistrationApiRequest.startNewDeviceRegistration(gcmDeviceRegistrationId, savedTags, parameters, getBackEndNewRegistrationListener(parameters, listener));
    }

    private BackEndRegistrationListener getBackEndNewRegistrationListener(final RegistrationParameters parameters, final RegistrationListener listener) {
        return new BackEndRegistrationListener() {

            @Override
            public void onBackEndRegistrationSuccess(String backEndDeviceRegistrationId) {

                if (backEndDeviceRegistrationId == null) {

                    Logger.e("Back-end server returned null backEndDeviceRegistrationId");

                    // The server didn't return a valid registration response.  We should clear our local
                    // registration data so that we can attempt to reregister next time.
                    clearBackEndRegistrationPreferences();

                    if (listener != null) {
                        listener.onRegistrationFailed("Back-end server return null backEndDeviceRegistrationId");
                    }
                    return;
                }

                Logger.i("Saving back-end device registration ID: " + backEndDeviceRegistrationId);
                pushPreferencesProvider.setBackEndDeviceRegistrationId(backEndDeviceRegistrationId);

                Logger.v("Saving updated variantUuid, variantSecret, deviceAlias, and baseServerUrl");
                pushPreferencesProvider.setVariantUuid(parameters.getVariantUuid());
                pushPreferencesProvider.setVariantSecret(parameters.getVariantSecret());
                pushPreferencesProvider.setDeviceAlias(parameters.getDeviceAlias());
                pushPreferencesProvider.setBaseServerUrl(parameters.getBaseServerUrl());
                pushPreferencesProvider.setTags(parameters.getTags());
                Logger.v("Saving tags: " + parameters.getTags());

                logPushRegisteredEvent(parameters.getVariantUuid(), backEndDeviceRegistrationId);

                if (listener != null) {
                    listener.onRegistrationComplete();
                }
            }

            @Override
            public void onBackEndRegistrationFailed(String reason) {

                clearBackEndRegistrationPreferences();

                if (listener != null) {
                    listener.onRegistrationFailed(reason);
                }
            }
        };
    }

    private void clearBackEndRegistrationPreferences() {
        pushPreferencesProvider.setBackEndDeviceRegistrationId(null);
        pushPreferencesProvider.setVariantUuid(null);
        pushPreferencesProvider.setVariantSecret(null);
        pushPreferencesProvider.setDeviceAlias(null);
        pushPreferencesProvider.setBaseServerUrl(null);
        pushPreferencesProvider.setTags(null);
    }

    private void logPushRegisteredEvent(String variantUuid, String deviceId) {
        // add analytics lib
    }
}
