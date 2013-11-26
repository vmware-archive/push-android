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
import com.gopivotal.pushlib.prefs.PreferencesProvider;
import com.gopivotal.pushlib.util.PushLibLogger;
import com.gopivotal.pushlib.version.VersionProvider;

public class RegistrationEngine {

    private Context context;
    private GcmProvider gcmProvider;
    private PreferencesProvider preferencesProvider;
    private GcmRegistrationApiRequestProvider gcmRegistrationApiRequestProvider;
    private BackEndRegistrationApiRequestProvider backEndRegistrationApiRequestProvider;
    private BackEndUnregisterDeviceApiRequestProvider backEndUnregisterDeviceApiRequestProvider;
    private VersionProvider versionProvider;
    private String previousGcmDeviceRegistrationId = null;
    private String previousBackEndDeviceRegistrationId = null;
    private String previousReleaseUuid;
    private String previousReleaseSecret;
    private String previousDeviceAlias;

    public RegistrationEngine(Context context, GcmProvider gcmProvider, PreferencesProvider preferencesProvider, GcmRegistrationApiRequestProvider gcmRegistrationApiRequestProvider, BackEndRegistrationApiRequestProvider backEndRegistrationApiRequestProvider, BackEndUnregisterDeviceApiRequestProvider backEndUnregisterDeviceApiRequestProvider, VersionProvider versionProvider) {
        verifyArguments(context, gcmProvider, preferencesProvider, gcmRegistrationApiRequestProvider, backEndRegistrationApiRequestProvider, versionProvider, backEndUnregisterDeviceApiRequestProvider);
        saveArguments(context, gcmProvider, preferencesProvider, gcmRegistrationApiRequestProvider, backEndRegistrationApiRequestProvider, versionProvider, backEndUnregisterDeviceApiRequestProvider);
    }

    private void verifyArguments(Context context, GcmProvider gcmProvider, PreferencesProvider preferencesProvider, GcmRegistrationApiRequestProvider gcmRegistrationApiRequestProvider, BackEndRegistrationApiRequestProvider backEndRegistrationApiRequestProvider, VersionProvider versionProvider, BackEndUnregisterDeviceApiRequestProvider backEndUnregisterDeviceApiRequestProvider) {
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

    private void saveArguments(Context context, GcmProvider gcmProvider, PreferencesProvider preferencesProvider, GcmRegistrationApiRequestProvider gcmRegistrationApiRequestProvider, BackEndRegistrationApiRequestProvider backEndRegistrationApiRequestProvider, VersionProvider versionProvider, BackEndUnregisterDeviceApiRequestProvider backEndUnregisterDeviceApiRequestProvider) {
        this.context = context;
        this.gcmProvider = gcmProvider;
        this.preferencesProvider = preferencesProvider;
        this.gcmRegistrationApiRequestProvider = gcmRegistrationApiRequestProvider;
        this.backEndRegistrationApiRequestProvider = backEndRegistrationApiRequestProvider;
        this.backEndUnregisterDeviceApiRequestProvider = backEndUnregisterDeviceApiRequestProvider;
        this.versionProvider = versionProvider;
        this.previousGcmDeviceRegistrationId = preferencesProvider.loadGcmDeviceRegistrationId();
        this.previousBackEndDeviceRegistrationId = preferencesProvider.loadBackEndDeviceRegistrationId();
        this.previousReleaseUuid = preferencesProvider.loadReleaseUuid();
        this.previousReleaseSecret = preferencesProvider.loadReleaseSecret();
        this.previousDeviceAlias = preferencesProvider.loadDeviceAlias();
    }

    public void registerDevice(RegistrationParameters parameters, final RegistrationListener listener) {

        verifyRegistrationArguments(parameters);

        if (isGcmRegistrationRequired()) {
            if (gcmProvider.isGooglePlayServicesInstalled(context)) {
                registerDeviceWithGcm(parameters.getGcmSenderId(), parameters, listener);
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

    private boolean isGcmRegistrationRequired() {
        if (previousGcmDeviceRegistrationId == null || previousGcmDeviceRegistrationId.isEmpty()) {
            PushLibLogger.v("previousGcmDeviceRegistrationId is empty. Device registration with GCM will be required");
            return true;
        }
        if (hasAppBeenUpdated()) {
            PushLibLogger.v("App version changed. Device registration with GCM will be required.");
            return true;
        }
        return false;
    }

    private boolean hasAppBeenUpdated() {
        final int currentAppVersion = versionProvider.getAppVersion();
        final int savedAppVersion = preferencesProvider.loadAppVersion();
        return currentAppVersion != savedAppVersion;
    }

    private boolean isBackEndRegistrationRequired(RegistrationParameters parameters) {
        final boolean isPreviousGcmDeviceRegistrationIdEmpty = previousGcmDeviceRegistrationId == null || previousGcmDeviceRegistrationId.isEmpty();
        final boolean isPreviousReleaseUuidEmpty = previousReleaseUuid == null || previousReleaseUuid.isEmpty();
        if (isPreviousGcmDeviceRegistrationIdEmpty) {
            PushLibLogger.v("previousGcmDeviceRegistrationId is empty. Device registration with the back-end will be required.");
        }
        if (isPreviousReleaseUuidEmpty) {
            PushLibLogger.v("previousReleaseUuid is empty. Device registration with the back-end will be required.");
        }
        return isPreviousGcmDeviceRegistrationIdEmpty || isPreviousReleaseUuidEmpty || areRegistrationParametersUpdated(parameters);
    }

    private boolean isUnregisterDeviceWithBackEndRequired(String newGcmDeviceRegistrationId, RegistrationParameters parameters) {
        final boolean isPreviousGcmDeviceRegistrationIdEmpty = previousGcmDeviceRegistrationId == null || previousGcmDeviceRegistrationId.isEmpty();
        final boolean isGcmDeviceRegistrationIdDifferent = isPreviousGcmDeviceRegistrationIdEmpty || !previousGcmDeviceRegistrationId.equals(newGcmDeviceRegistrationId);
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

    private void registerDeviceWithGcm(String senderId, final RegistrationParameters parameters, final RegistrationListener listener) {
        PushLibLogger.i("Initiating device registration with GCM.");
        final GcmRegistrationApiRequest gcmRegistrationApiRequest = gcmRegistrationApiRequestProvider.getRequest();
        gcmRegistrationApiRequest.startRegistration(senderId, new GcmRegistrationListener() {

            @Override
            public void onGcmRegistrationComplete(String gcmDeviceRegistrationId) {

                if (gcmDeviceRegistrationId == null) {
                    PushLibLogger.e("GCM returned null gcmDeviceRegistrationId");
                    if (listener != null) {
                        listener.onRegistrationFailed("GCM returned null gcmDeviceRegistrationId");
                    }
                    return;
                }

                final boolean isNewGcmDeviceRegistrationId;
                if (previousGcmDeviceRegistrationId != null && previousGcmDeviceRegistrationId.equals(gcmDeviceRegistrationId)) {
                    PushLibLogger.v("New gcmDeviceRegistrationId from GCM is the same as the previous one.");
                    isNewGcmDeviceRegistrationId = false;
                } else {
                    preferencesProvider.saveGcmDeviceRegistrationId(gcmDeviceRegistrationId);
                    isNewGcmDeviceRegistrationId = true;
                }

                if (previousGcmDeviceRegistrationId == null || hasAppBeenUpdated()) {
                    preferencesProvider.saveAppVersion(versionProvider.getAppVersion());
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
