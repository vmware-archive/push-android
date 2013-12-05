package com.gopivotal.pushlib.registration;

import android.content.Context;

import com.gopivotal.pushlib.RegistrationParameters;
import com.gopivotal.pushlib.backend.BackEndRegistrationApiRequest;
import com.gopivotal.pushlib.backend.BackEndRegistrationApiRequestProvider;
import com.gopivotal.pushlib.backend.BackEndRegistrationListener;
import com.gopivotal.pushlib.backend.BackEndUnregisterDeviceApiRequest;
import com.gopivotal.pushlib.backend.BackEndUnregisterDeviceApiRequestProvider;
import com.gopivotal.pushlib.backend.BackEndUnregisterDeviceListener;
import com.gopivotal.pushlib.gcm.GcmProvider;
import com.gopivotal.pushlib.gcm.GcmRegistrationApiRequest;
import com.gopivotal.pushlib.gcm.GcmRegistrationApiRequestProvider;
import com.gopivotal.pushlib.gcm.GcmRegistrationListener;
import com.gopivotal.pushlib.gcm.GcmUnregistrationApiRequest;
import com.gopivotal.pushlib.gcm.GcmUnregistrationApiRequestProvider;
import com.gopivotal.pushlib.gcm.GcmUnregistrationListener;
import com.gopivotal.pushlib.prefs.PreferencesProvider;
import com.gopivotal.pushlib.util.PushLibLogger;
import com.gopivotal.pushlib.version.VersionProvider;

/**
 * This class is responsible for all the business logic behind device registration.  For a description
 * of its operation you can refer to the following diagrams:
 *
 *  1. Flow chart: https://docs.google.com/a/pivotallabs.com/drawings/d/1LFzm3BmQWBnHFnt5R4HGF4ki0qNF0FpXg_HYJgEvxvE
 *  2. Sequence diagram: https://docs.google.com/a/pivotallabs.com/drawings/d/1hJYWt_bh8Vf2CPElDnxD1qcnpWRdEdIcGZWa6OYipz8/edit
 *
 *  In general, though, the Registration Engine tries to do as little work as it thinks is required.
 *
 *  If the device is already successfully registered and all of the registration parameters are the same as the
 *  previous registration then the Registration Engine won't do anything.
 *
 *  On a fresh install, the Registration Engine will register with Google Cloud Messaging (GCM) and then with the
 *  Omnia Mobile Services back-end server.
 *
 *  If the GCM Sender ID is different then the previous registration, then the Registration Engine will
 *  attempt to unregister the device with GCM (Google Cloud Messaging) first.
 *
 *  If the application version code or the GCM Sender ID is updated since the previous registration, then the
 *  Registration Engine will attempt to re-register with GCM.
 *
 *  If any of the Omnia registration parameters (release_uuid, release_secret, device_alias), or if a GCM registration
 *  provides a different device registration ID than a previous install, then the Registration Engine will attempt
 *  to unregister with the Omnia back-end server prior to re-registering.
 *
 *  The Registration Engine is also designed to successfully complete previous registrations that have failed. For
 *  instance, if the previous registration attempt successfully registered with GCM but failed to complete the
 *  registration with the back-end then it will simply try to re-register with the back-end if called again.
 */
public class RegistrationEngine {

    private Context context;
    private GcmProvider gcmProvider;
    private PreferencesProvider preferencesProvider;
    private GcmRegistrationApiRequestProvider gcmRegistrationApiRequestProvider;
    private GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider;
    private BackEndRegistrationApiRequestProvider backEndRegistrationApiRequestProvider;
    private BackEndUnregisterDeviceApiRequestProvider backEndUnregisterDeviceApiRequestProvider;
    private VersionProvider versionProvider;
    private String previousGcmDeviceRegistrationId = null;
    private String previousBackEndDeviceRegistrationId = null;
    private String previousGcmSenderId;
    private String previousReleaseUuid;
    private String previousReleaseSecret;
    private String previousDeviceAlias;

    /**
     * Instantiate an instance of the RegistrationEngine.
     *
     * All the parameters are required.  None may be null.
     *
     * @param context  A context
     * @param gcmProvider  Some object that can provide the GCM services.
     * @param preferencesProvider  Some object that can provide persistent storage of preferences.
     * @param gcmRegistrationApiRequestProvider  Some object that can provide GCMRegistrationApiRequest objects.
     * @param gcmUnregistrationApiRequestProvider  Some object that can provide GCMUnregistrationApiRequest objects.
     * @param backEndRegistrationApiRequestProvider  Some object that can provide BackEndRegistrationApiRequest objects.
     * @param backEndUnregisterDeviceApiRequestProvider  Some object that can provide BackEndUnregisterDeviceApiRequest objects.
     * @param versionProvider  Some object that can provide the application version.
     */
    public RegistrationEngine(Context context, GcmProvider gcmProvider, PreferencesProvider preferencesProvider, GcmRegistrationApiRequestProvider gcmRegistrationApiRequestProvider, GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider, BackEndRegistrationApiRequestProvider backEndRegistrationApiRequestProvider, BackEndUnregisterDeviceApiRequestProvider backEndUnregisterDeviceApiRequestProvider, VersionProvider versionProvider) {
        verifyArguments(context, gcmProvider, preferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, versionProvider, backEndUnregisterDeviceApiRequestProvider);
        saveArguments(context, gcmProvider, preferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, versionProvider, backEndUnregisterDeviceApiRequestProvider);
    }

    private void verifyArguments(Context context, GcmProvider gcmProvider, PreferencesProvider preferencesProvider, GcmRegistrationApiRequestProvider gcmRegistrationApiRequestProvider, GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider, BackEndRegistrationApiRequestProvider backEndRegistrationApiRequestProvider, VersionProvider versionProvider, BackEndUnregisterDeviceApiRequestProvider backEndUnregisterDeviceApiRequestProvider) {
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
        if (gcmProvider == null) {
            throw new IllegalArgumentException("gcmProvider may not be null");
        }
        if (preferencesProvider == null) {
            throw new IllegalArgumentException("preferencesProvider may not be null");
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
        if (backEndUnregisterDeviceApiRequestProvider == null) {
            throw new IllegalArgumentException("backEndUnregisterDeviceApiRequestProvider may not be null");
        }
        if (versionProvider == null) {
            throw new IllegalArgumentException("versionProvider may not be null");
        }
    }

    private void saveArguments(Context context, GcmProvider gcmProvider, PreferencesProvider preferencesProvider, GcmRegistrationApiRequestProvider gcmRegistrationApiRequestProvider, GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider, BackEndRegistrationApiRequestProvider backEndRegistrationApiRequestProvider, VersionProvider versionProvider, BackEndUnregisterDeviceApiRequestProvider backEndUnregisterDeviceApiRequestProvider) {
        this.context = context;
        this.gcmProvider = gcmProvider;
        this.preferencesProvider = preferencesProvider;
        this.gcmRegistrationApiRequestProvider = gcmRegistrationApiRequestProvider;
        this.gcmUnregistrationApiRequestProvider = gcmUnregistrationApiRequestProvider;
        this.backEndRegistrationApiRequestProvider = backEndRegistrationApiRequestProvider;
        this.backEndUnregisterDeviceApiRequestProvider = backEndUnregisterDeviceApiRequestProvider;
        this.versionProvider = versionProvider;
        this.previousGcmDeviceRegistrationId = preferencesProvider.loadGcmDeviceRegistrationId();
        this.previousBackEndDeviceRegistrationId = preferencesProvider.loadBackEndDeviceRegistrationId();
        this.previousGcmSenderId = preferencesProvider.loadGcmSenderId();
        this.previousReleaseUuid = preferencesProvider.loadReleaseUuid();
        this.previousReleaseSecret = preferencesProvider.loadReleaseSecret();
        this.previousDeviceAlias = preferencesProvider.loadDeviceAlias();
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

        } else if (isUnregisterDeviceWithBackEndRequired(previousGcmDeviceRegistrationId, parameters)) {
            unregisterDeviceWithBackEnd(previousBackEndDeviceRegistrationId, previousGcmDeviceRegistrationId, parameters, listener);

        } else if (isBackEndRegistrationRequired(parameters)) {
            registerDeviceWithBackEnd(previousGcmDeviceRegistrationId, parameters, listener);

        } else {
            PushLibLogger.v("Already registered with GCM and back-end");
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
        if (parameters.getReleaseUuid() == null || parameters.getReleaseUuid().isEmpty()) {
            throw new IllegalArgumentException("parameters.releaseUuid may not be null or empty");
        }
        if (parameters.getReleaseSecret() == null || parameters.getReleaseSecret().isEmpty()) {
            throw new IllegalArgumentException("parameters.releaseSecret may not be null or empty");
        }
    }

    private boolean isGcmRegistrationRequired(RegistrationParameters parameters) {
        if (isEmptyPreviousGcmDeviceRegistrationId()) {
            PushLibLogger.v("previousGcmDeviceRegistrationId is empty. Device registration with GCM will be required");
            return true;
        }
        if (isEmptyPreviousGcmSenderId()) {
            PushLibLogger.v("previousGcmSenderId is empty. Device registration with GCM will be required");
            return true;
        }
        if (isUpdatedGcmSenderId(parameters)) {
            PushLibLogger.v("gcmSenderId has been updated. Device unregistration and re-registration with GCM will be required");
            return true;
        }
        if (hasAppBeenUpdated()) {
            PushLibLogger.v("App version changed. Device registration with GCM will be required.");
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

    private boolean hasAppBeenUpdated() {
        final int currentAppVersion = versionProvider.getAppVersion();
        final int savedAppVersion = preferencesProvider.loadAppVersion();
        return currentAppVersion != savedAppVersion;
    }

    private boolean isBackEndRegistrationRequired(RegistrationParameters parameters) {
        final boolean isPreviousReleaseUuidEmpty = previousReleaseUuid == null || previousReleaseUuid.isEmpty();
        if (isEmptyPreviousGcmDeviceRegistrationId()) {
            PushLibLogger.v("previousGcmDeviceRegistrationId is empty. Device registration with the back-end will be required.");
        }
        if (isPreviousReleaseUuidEmpty) {
            PushLibLogger.v("previousReleaseUuid is empty. Device registration with the back-end will be required.");
        }
        return isEmptyPreviousGcmDeviceRegistrationId() || isPreviousReleaseUuidEmpty || areRegistrationParametersUpdated(parameters);
    }

    private boolean isUnregisterDeviceWithBackEndRequired(String newGcmDeviceRegistrationId, RegistrationParameters parameters) {
        final boolean isGcmDeviceRegistrationIdDifferent = isEmptyPreviousGcmDeviceRegistrationId() || !previousGcmDeviceRegistrationId.equals(newGcmDeviceRegistrationId);
        final boolean isPreviousBackEndDeviceRegistrationIdEmpty = previousBackEndDeviceRegistrationId == null || previousBackEndDeviceRegistrationId.isEmpty();
        if (isPreviousBackEndDeviceRegistrationIdEmpty) {
            PushLibLogger.v("previousBackEndDeviceRegistrationId is empty. Device will NOT have to be unregistered with the back-end.");
            return false;
        }
        if (isGcmDeviceRegistrationIdDifferent) {
            PushLibLogger.v("The gcmDeviceRegistrationId is different. Device will have to be unregistered with the back-end.");
            return true;
        }
        if (areRegistrationParametersUpdated(parameters)) {
            PushLibLogger.v("The registration parameters have been updated. Device will have to be unregistered with the back-end.");
            return true;
        }
        PushLibLogger.v("It does not seem that the device needs to be unregistered with the back-end.");
        return false;
    }

    private boolean areRegistrationParametersUpdated(RegistrationParameters parameters) {
        final boolean isPreviousReleaseUuidEmpty = previousReleaseUuid == null || previousReleaseUuid.isEmpty();
        final boolean isReleaseUuidUpdated = (isPreviousReleaseUuidEmpty && !parameters.getReleaseUuid().isEmpty()) || !parameters.getReleaseUuid().equals(previousReleaseUuid);
        final boolean isPreviousReleaseSecretEmpty = previousReleaseSecret == null || previousReleaseSecret.isEmpty();
        final boolean isReleaseSecretUpdated = (isPreviousReleaseSecretEmpty && !parameters.getReleaseSecret().isEmpty()) || !parameters.getReleaseSecret().equals(previousReleaseSecret);
        final boolean isPreviousDeviceAliasEmpty = previousDeviceAlias == null || previousDeviceAlias.isEmpty();
        final boolean isNewDeviceAliasEmpty = parameters.getDeviceAlias() == null || parameters.getDeviceAlias().isEmpty();
        final boolean isDeviceAliasUpdated = (isPreviousDeviceAliasEmpty && !isNewDeviceAliasEmpty) || (!isPreviousDeviceAliasEmpty && isNewDeviceAliasEmpty) || !parameters.getDeviceAlias().equals(previousDeviceAlias);
        return isDeviceAliasUpdated || isReleaseSecretUpdated || isReleaseUuidUpdated;
    }

    private void unregisterDeviceWithGcm(final RegistrationParameters parameters, final RegistrationListener listener) {
        PushLibLogger.i("GCM Sender ID has been changed. Unregistering sender ID with GCM.");
        final GcmUnregistrationApiRequest gcmUnregistrationApiRequest = gcmUnregistrationApiRequestProvider.getRequest();
        gcmUnregistrationApiRequest.startUnregistration(new GcmUnregistrationListener() {
            @Override
            public void onGcmUnregistrationComplete() {
                preferencesProvider.saveGcmDeviceRegistrationId(null);
                preferencesProvider.saveGcmSenderId(null);
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
        PushLibLogger.i("Initiating device registration with GCM.");
        final GcmRegistrationApiRequest gcmRegistrationApiRequest = gcmRegistrationApiRequestProvider.getRequest();
        gcmRegistrationApiRequest.startRegistration(parameters.getGcmSenderId(), new GcmRegistrationListener() {

            @Override
            public void onGcmRegistrationComplete(String gcmDeviceRegistrationId) {

                if (gcmDeviceRegistrationId == null) {
                    PushLibLogger.e("GCM returned null gcmDeviceRegistrationId");
                    if (listener != null) {
                        listener.onRegistrationFailed("GCM returned null gcmDeviceRegistrationId");
                    }
                    return;
                }

                preferencesProvider.saveGcmDeviceRegistrationId(gcmDeviceRegistrationId);
                preferencesProvider.saveGcmSenderId(parameters.getGcmSenderId());
                preferencesProvider.saveAppVersion(versionProvider.getAppVersion());

                final boolean isNewGcmDeviceRegistrationId;
                if (previousGcmDeviceRegistrationId != null && previousGcmDeviceRegistrationId.equals(gcmDeviceRegistrationId)) {
                    PushLibLogger.v("New gcmDeviceRegistrationId from GCM is the same as the previous one.");
                    isNewGcmDeviceRegistrationId = false;
                } else {
                    isNewGcmDeviceRegistrationId = true;
                }

                if (isUnregisterDeviceWithBackEndRequired(gcmDeviceRegistrationId, parameters)) {
                    unregisterDeviceWithBackEnd(previousBackEndDeviceRegistrationId, gcmDeviceRegistrationId, parameters, listener);
                } else if (isNewGcmDeviceRegistrationId) {
                    registerDeviceWithBackEnd(gcmDeviceRegistrationId, parameters, listener);
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

    private void unregisterDeviceWithBackEnd(final String backEndDeviceRegistrationId, String gcmDeviceRegistrationId, RegistrationParameters parameters, final RegistrationListener listener) {
        PushLibLogger.i("Initiating device unregistration with the back-end.");
        final BackEndUnregisterDeviceApiRequest backEndUnregisterDeviceApiRequest = backEndUnregisterDeviceApiRequestProvider.getRequest();
        backEndUnregisterDeviceApiRequest.startUnregisterDevice(backEndDeviceRegistrationId, getBackEndUnregisterDeviceListener(gcmDeviceRegistrationId, parameters, listener));
    }

    private BackEndUnregisterDeviceListener getBackEndUnregisterDeviceListener(final String gcmDeviceRegistrationId, final RegistrationParameters parameters, final RegistrationListener listener) {
        return new BackEndUnregisterDeviceListener() {

            @Override
            public void onBackEndUnregisterDeviceSuccess() {
                preferencesProvider.saveBackEndDeviceRegistrationId(null);
                preferencesProvider.saveReleaseUuid(null);
                preferencesProvider.saveReleaseSecret(null);
                preferencesProvider.saveDeviceAlias(null);
                registerDeviceWithBackEnd(gcmDeviceRegistrationId, parameters, listener);
            }

            @Override
            public void onBackEndUnregisterDeviceFailed(String reason) {
                // Even if we couldn't unregister the old device ID we should still attempt
                // to register the new one.
                registerDeviceWithBackEnd(gcmDeviceRegistrationId, parameters, listener);
            }
        };
    }

    private void registerDeviceWithBackEnd(final String gcmDeviceRegistrationId, RegistrationParameters parameters, final RegistrationListener listener) {
        PushLibLogger.i("Initiating device registration with the back-end.");
        final BackEndRegistrationApiRequest backEndRegistrationApiRequest = backEndRegistrationApiRequestProvider.getRequest();
        backEndRegistrationApiRequest.startDeviceRegistration(gcmDeviceRegistrationId, parameters, getBackEndRegistrationListener(parameters, listener));
    }

    private BackEndRegistrationListener getBackEndRegistrationListener(final RegistrationParameters parameters, final RegistrationListener listener) {
        return new BackEndRegistrationListener() {

            @Override
            public void onBackEndRegistrationSuccess(String backEndDeviceRegistrationId) {

                if (backEndDeviceRegistrationId == null) {
                    PushLibLogger.e("Back-end server return null backEndDeviceRegistrationId");
                    if (listener != null) {
                        listener.onRegistrationFailed("Back-end server return null backEndDeviceRegistrationId");
                    }
                    return;
                }

                PushLibLogger.i("Saving back-end device registration ID: " + backEndDeviceRegistrationId);
                preferencesProvider.saveBackEndDeviceRegistrationId(backEndDeviceRegistrationId);

                PushLibLogger.v("Saving updated releaseUUID, releaseSecret, and deviceAlias");
                preferencesProvider.saveReleaseUuid(parameters.getReleaseUuid());
                preferencesProvider.saveReleaseSecret(parameters.getReleaseSecret());
                preferencesProvider.saveDeviceAlias(parameters.getDeviceAlias());

                if (listener != null) {
                    listener.onRegistrationComplete();
                }
            }

            @Override
            public void onBackEndRegistrationFailed(String reason) {
                if (listener != null) {
                    listener.onRegistrationFailed(reason);
                }
            }
        };
    }
}
