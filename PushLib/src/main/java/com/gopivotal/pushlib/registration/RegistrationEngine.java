package com.gopivotal.pushlib.registration;

import android.content.Context;

import com.gopivotal.pushlib.gcm.GcmProvider;
import com.gopivotal.pushlib.gcm.GcmRegistrationApiRequest;
import com.gopivotal.pushlib.gcm.GcmRegistrationApiRequestProvider;
import com.gopivotal.pushlib.gcm.GcmRegistrationListener;
import com.gopivotal.pushlib.prefs.PreferencesProvider;
import com.gopivotal.pushlib.util.Util;
import com.gopivotal.pushlib.version.VersionProvider;
import com.xtreme.commons.Logger;

public class RegistrationEngine {

    private Context context;
    private GcmProvider gcmProvider;
    private PreferencesProvider preferencesProvider;
    private String previousGcmDeviceRegistrationId = null;
    private GcmRegistrationApiRequestProvider gcmRegistrationApiRequestProvider;
    private VersionProvider versionProvider;

    public RegistrationEngine(Context context, GcmProvider gcmProvider, PreferencesProvider preferencesProvider, GcmRegistrationApiRequestProvider gcmRegistrationApiRequestProvider, VersionProvider versionProvider) {
        verifyArguments(context, gcmProvider, preferencesProvider, gcmRegistrationApiRequestProvider, versionProvider);
        saveArguments(context, gcmProvider, preferencesProvider, gcmRegistrationApiRequestProvider, versionProvider);
    }

    private void verifyArguments(Context context, GcmProvider gcmProvider, PreferencesProvider preferencesProvider, GcmRegistrationApiRequestProvider gcmRegistrationApiRequestProvider, VersionProvider versionProvider) {
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
        if (versionProvider == null) {
            throw new IllegalArgumentException("versionProvider may not be null");
        }
    }

    private void saveArguments(Context context, GcmProvider gcmProvider, PreferencesProvider preferencesProvider, GcmRegistrationApiRequestProvider gcmRegistrationApiRequestProvider, VersionProvider versionProvider) {
        this.context = context;
        this.gcmProvider = gcmProvider;
        this.preferencesProvider = preferencesProvider;
        this.gcmRegistrationApiRequestProvider = gcmRegistrationApiRequestProvider;
        this.versionProvider = versionProvider;
        this.previousGcmDeviceRegistrationId = preferencesProvider.loadGcmDeviceRegistrationId();
    }

    public void registerDevice(String senderId, final RegistrationListener listener) {

        verifyRegistrationArguments(senderId);

        // TODO - if there was a previous registration, compare the new GCM device ID with the previous one.  If
        // they are the same, then we shouldn't have to re-register with the back-end.
        if (isRegistrationRequired()) {
            // TODO - make this method asynchronous
            registerDeviceWithGcm(senderId, listener);
        } else {
            // TODO - do we need to register with Studio server on every launch, or only when a new registration ID is created (I suspect the latter)?
            Logger.i("Already registered with GCM");
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

    private boolean isRegistrationRequired() {
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
                    Logger.e("Server returned null gcmDeviceRegistrationId");
                    if (listener != null) {
                        listener.onRegistrationFailed("Server returned null gcmDeviceRegistrationId");
                    }
                    return;
                }

                if (previousGcmDeviceRegistrationId != null && previousGcmDeviceRegistrationId.equals(gcmDeviceRegistrationId)) {
                    Logger.i("New gcmDeviceRegistrationId from server is the same as the old one.");
                } else {
                    preferencesProvider.saveGcmDeviceRegistrationId(gcmDeviceRegistrationId);
                }

                if (previousGcmDeviceRegistrationId == null || hasAppBeenUpdated()) {
                    preferencesProvider.saveAppVersion(versionProvider.getAppVersion());
                }

                // TODO - register with backend here

                if (listener != null) {
                    listener.onRegistrationComplete();
                }
            }

            @Override
            public void onGcmRegistrationFailed(String reason) {
                // TODO - should I unregister the backend here?
                // TODO - should I clear the gcmDeviceRegistrationId from the preferences?
                if (listener != null) {
                    listener.onRegistrationFailed(reason);
                }
            }
        });
    }
}
