package com.gopivotal.pushlib.registration;

import android.content.Context;

import com.gopivotal.pushlib.gcm.GcmProvider;
import com.gopivotal.pushlib.gcm.GcmRegistrationApiRequest;
import com.gopivotal.pushlib.gcm.GcmRegistrationApiRequestImpl;
import com.gopivotal.pushlib.gcm.GcmRegistrationListener;
import com.gopivotal.pushlib.gcm.RealGcmProvider;
import com.gopivotal.pushlib.prefs.PreferencesProvider;
import com.gopivotal.pushlib.prefs.RealPreferencesProvider;

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

        // TODO - make this method asynchronous

        if (senderId == null) {
            throw new IllegalArgumentException("senderId may not be null");
        }

        final GcmRegistrationApiRequest gcmRegistrationApiRequest = new GcmRegistrationApiRequestImpl(context, senderId, gcmProvider, preferencesProvider);
        gcmRegistrationApiRequest.startRegistration(new GcmRegistrationListener() {

            @Override
            public void onGcmRegistrationComplete(String gcmDeviceRegistrationId) {
                listener.onRegistrationComplete();
            }

            @Override
            public void onGcmRegistrationFailed(String reason) {
                listener.onRegistrationFailed(reason);
            }
        });

    }
}
