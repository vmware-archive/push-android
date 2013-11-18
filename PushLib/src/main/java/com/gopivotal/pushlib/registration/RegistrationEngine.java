package com.gopivotal.pushlib.registration;

import android.content.Context;

import com.gopivotal.pushlib.backend.BackEndRegistrationApiRequest;
import com.gopivotal.pushlib.backend.BackEndRegistrationApiRequestProvider;
import com.gopivotal.pushlib.backend.BackEndRegistrationListener;
import com.gopivotal.pushlib.gcm.GcmProvider;
import com.gopivotal.pushlib.gcm.GcmRegistrationApiRequest;
import com.gopivotal.pushlib.gcm.GcmRegistrationApiRequestProvider;
import com.gopivotal.pushlib.gcm.GcmRegistrationListener;
import com.gopivotal.pushlib.prefs.PreferencesProvider;
import com.gopivotal.pushlib.version.VersionProvider;
import com.xtreme.commons.Logger;

public class RegistrationEngine {

    private Context context;
    private GcmProvider gcmProvider;
    private PreferencesProvider preferencesProvider;
    private String previousGcmDeviceRegistrationId = null;
    private String previousBackEndDeviceRegistrationId = null;
    private GcmRegistrationApiRequestProvider gcmRegistrationApiRequestProvider;
    private BackEndRegistrationApiRequestProvider backEndRegistrationApiRequestProvider;
    private VersionProvider versionProvider;

    public RegistrationEngine(Context context, GcmProvider gcmProvider, PreferencesProvider preferencesProvider, GcmRegistrationApiRequestProvider gcmRegistrationApiRequestProvider, BackEndRegistrationApiRequestProvider backEndRegistrationApiRequestProvider, VersionProvider versionProvider) {
        verifyArguments(context, gcmProvider, preferencesProvider, gcmRegistrationApiRequestProvider, backEndRegistrationApiRequestProvider, versionProvider);
        saveArguments(context, gcmProvider, preferencesProvider, gcmRegistrationApiRequestProvider, backEndRegistrationApiRequestProvider, versionProvider);
    }

    private void verifyArguments(Context context, GcmProvider gcmProvider, PreferencesProvider preferencesProvider, GcmRegistrationApiRequestProvider gcmRegistrationApiRequestProvider, BackEndRegistrationApiRequestProvider backEndRegistrationApiRequestProvider, VersionProvider versionProvider) {
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
        if (versionProvider == null) {
            throw new IllegalArgumentException("versionProvider may not be null");
        }
    }

    private void saveArguments(Context context, GcmProvider gcmProvider, PreferencesProvider preferencesProvider, GcmRegistrationApiRequestProvider gcmRegistrationApiRequestProvider, BackEndRegistrationApiRequestProvider backEndRegistrationApiRequestProvider, VersionProvider versionProvider) {
        this.context = context;
        this.gcmProvider = gcmProvider;
        this.preferencesProvider = preferencesProvider;
        this.gcmRegistrationApiRequestProvider = gcmRegistrationApiRequestProvider;
        this.backEndRegistrationApiRequestProvider = backEndRegistrationApiRequestProvider;
        this.versionProvider = versionProvider;
        this.previousGcmDeviceRegistrationId = preferencesProvider.loadGcmDeviceRegistrationId();
        this.previousBackEndDeviceRegistrationId = preferencesProvider.loadBackEndDeviceRegistrationId();
    }

    // TODO - make this method asynchronous
    public void registerDevice(String senderId, final RegistrationListener listener) {

        verifyRegistrationArguments(senderId);

        if (isGcmRegistrationRequired()) {
            if (gcmProvider.isGooglePlayServicesInstalled(context)) {
                registerDeviceWithGcm(senderId, listener);
            } else {
                if (listener != null) {
                    listener.onRegistrationFailed("Google Play Services is not available");
                }
            }

        } else if (isBackEndRegistrationRequired()) {
            registerDeviceWithBackEnd(previousGcmDeviceRegistrationId, listener);

        } else {

            Logger.i("Already registered with GCM and back-end");
            if (listener != null) {
                listener.onRegistrationComplete();
            }
        }
    }

    private void verifyRegistrationArguments(String senderId) {
        if (senderId == null) {
            throw new IllegalArgumentException("senderId may not be null");
        }
    }

    private boolean isGcmRegistrationRequired() {
        if (previousGcmDeviceRegistrationId == null || previousGcmDeviceRegistrationId.isEmpty()) {
            return true;
        }
        if (hasAppBeenUpdated()) {
            Logger.i("App version changed. Device registration with GCM will be required.");
            return true;
        };
        return false;
    }

    private boolean hasAppBeenUpdated() {
        final int currentAppVersion = versionProvider.getAppVersion();
        final int savedAppVersion = preferencesProvider.loadAppVersion();
        return currentAppVersion != savedAppVersion;
    }

    private boolean isBackEndRegistrationRequired() {
        return (previousBackEndDeviceRegistrationId == null || previousBackEndDeviceRegistrationId.isEmpty());
    }

    private boolean isNewGcmDeviceRegistrationId(String gcmDeviceRegistrationId) {
        if (previousGcmDeviceRegistrationId == null && gcmDeviceRegistrationId != null) {
            return true;
        }
        return !previousGcmDeviceRegistrationId.equals(gcmDeviceRegistrationId);
    }

    private void registerDeviceWithGcm(String senderId, final RegistrationListener listener) {
        final GcmRegistrationApiRequest gcmRegistrationApiRequest = gcmRegistrationApiRequestProvider.getRequest();
        gcmRegistrationApiRequest.startRegistration(senderId, new GcmRegistrationListener() {

            @Override
            public void onGcmRegistrationComplete(String gcmDeviceRegistrationId) {

                if (gcmDeviceRegistrationId == null) {
                    Logger.e("GCM returned null gcmDeviceRegistrationId");
                    if (listener != null) {
                        listener.onRegistrationFailed("GCM returned null gcmDeviceRegistrationId");
                    }
                    return;
                }

                final boolean isNewGcmDeviceRegistrationId;
                if (previousGcmDeviceRegistrationId != null && previousGcmDeviceRegistrationId.equals(gcmDeviceRegistrationId)) {
                    Logger.i("New gcmDeviceRegistrationId from GCM is the same as the previous one.");
                    isNewGcmDeviceRegistrationId = false;
                } else {
                    preferencesProvider.saveGcmDeviceRegistrationId(gcmDeviceRegistrationId);
                    isNewGcmDeviceRegistrationId = true;
                }

                if (previousGcmDeviceRegistrationId == null || hasAppBeenUpdated()) {
                    preferencesProvider.saveAppVersion(versionProvider.getAppVersion());
                }

                if (isNewGcmDeviceRegistrationId) {
                    registerDeviceWithBackEnd(gcmDeviceRegistrationId, listener);
                } else {
                    if (listener != null) {
                        listener.onRegistrationComplete();
                    }
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

    private void registerDeviceWithBackEnd(final String gcmDeviceRegistrationId, final RegistrationListener listener) {
        final BackEndRegistrationApiRequest backEndRegistrationApiRequest = backEndRegistrationApiRequestProvider.getRequest();
        backEndRegistrationApiRequest.startDeviceRegistration(gcmDeviceRegistrationId, getBackEndRegistrationListener(listener));
    }

    private BackEndRegistrationListener getBackEndRegistrationListener(final RegistrationListener listener) {
        return new BackEndRegistrationListener() {

            @Override
            public void onBackEndRegistrationSuccess(String backEndDeviceRegistrationId) {

                if (backEndDeviceRegistrationId == null) {
                    Logger.e("Back-end server return null backEndDeviceRegistrationId");
                    if (listener != null) {
                        listener.onRegistrationFailed("Back-end server return null backEndDeviceRegistrationId");
                    }
                    return;
                }

                if (previousBackEndDeviceRegistrationId != null && previousBackEndDeviceRegistrationId.equals(backEndDeviceRegistrationId)) {
                    Logger.i("New backEndDeviceRegistrationId from server is the same as the previous one");
                } else {
                    preferencesProvider.saveBackEndDeviceRegistrationId(backEndDeviceRegistrationId);
                }

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
