package com.gopivotal.pushlib.registration;

import android.content.Context;

import com.gopivotal.pushlib.gcm.GcmProvider;
import com.gopivotal.pushlib.gcm.GcmRegistrationApiRequest;
import com.gopivotal.pushlib.gcm.GcmRegistrationApiRequestImpl;
import com.gopivotal.pushlib.gcm.GcmRegistrationListener;
import com.gopivotal.pushlib.gcm.RealGcmProvider;
import com.gopivotal.pushlib.prefs.PreferencesProvider;
import com.gopivotal.pushlib.prefs.RealPreferencesProvider;
import com.xtreme.commons.Logger;

public class RegistrationEngine {

    private Context context;
    private GcmProvider gcmProvider;
    private PreferencesProvider preferencesProvider;

    public RegistrationEngine(Context context, RealGcmProvider gcmProvider, RealPreferencesProvider preferencesProvider) {
        verifyArguments(context, gcmProvider, preferencesProvider);
        saveArguments(context, gcmProvider, preferencesProvider);
    }

    private void verifyArguments(Context context, RealGcmProvider gcmProvider, RealPreferencesProvider preferencesProvider) {
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
        if (gcmProvider == null) {
            throw new IllegalArgumentException("gcmProvider may not be null");
        }
        if (preferencesProvider == null) {
            throw new IllegalArgumentException("preferencesProvider may not be null");
        }
    }

    private void saveArguments(Context context, RealGcmProvider gcmProvider, RealPreferencesProvider preferencesProvider) {
        this.context = context;
        this.gcmProvider = gcmProvider;
        this.preferencesProvider = preferencesProvider;
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
        final String gcmDeviceRegistrationId = preferencesProvider.loadGcmDeviceRegistrationId();
        return gcmDeviceRegistrationId == null || gcmDeviceRegistrationId.isEmpty();
    }

    private void registerDeviceWithGcm(String senderId, final RegistrationListener listener) {
        final GcmRegistrationApiRequest gcmRegistrationApiRequest = new GcmRegistrationApiRequestImpl(context, senderId, gcmProvider);
        gcmRegistrationApiRequest.startRegistration(new GcmRegistrationListener() {

            @Override
            public void onGcmRegistrationComplete(String gcmDeviceRegistrationId) {
                preferencesProvider.saveGcmDeviceRegistrationId(gcmDeviceRegistrationId);

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
