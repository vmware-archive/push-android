package com.gopivotal.pushlib.gcm;

import android.content.Context;
import android.os.AsyncTask;

import com.gopivotal.pushlib.prefs.PreferencesProvider;
import com.xtreme.commons.Logger;

import java.io.IOException;

public class GcmRegistrationApiRequestImpl extends AsyncTask<GcmRegistrationListener, Void, String> implements GcmRegistrationApiRequest {

    private Context context;
    private String senderId;
    private GcmProvider gcmProvider;

    public GcmRegistrationApiRequestImpl(Context context, String senderId, GcmProvider gcmProvider) {
        verifyArguments(context, senderId, gcmProvider);
        saveArguments(context, senderId, gcmProvider);
    }

    private void verifyArguments(Context context, String senderId, GcmProvider gcmProvider) {
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
        if (senderId == null) {
            throw new IllegalArgumentException("senderId may not be null");
        }
        if (gcmProvider == null) {
            throw new IllegalArgumentException("gcmProvider may not be null");
        }
    }

    private void saveArguments(Context context, String senderId, GcmProvider gcmProvider) {
        this.context = context;
        this.senderId = senderId;
        this.gcmProvider = gcmProvider;
    }

    public void startRegistration(GcmRegistrationListener listener) {
        execute(listener);
    }

    @Override
    protected String doInBackground(GcmRegistrationListener... listeners) {

        GcmRegistrationListener listener = null;
        if (listeners != null && listeners.length > 0) {
            listener = listeners[0];
        }

        try {
            final String deviceRegistrationId = gcmProvider.register(senderId);
            Logger.i("Device registered with GCM. Device registration ID:" + deviceRegistrationId);

            // Inform callback of registration success
            if (listener != null) {
                listener.onGcmRegistrationComplete(deviceRegistrationId);
            }
            return deviceRegistrationId;

        } catch (IOException ex) {
            Logger.ex("Error registering device with GCM:", ex);
            // If there is an error, don't just keep trying to register.
            // Require the user to click a button again, or perform
            // exponential back-off.
            if (listener != null) {
                listener.onGcmRegistrationFailed(ex.getLocalizedMessage());
            }
            return null;
        }
    }

}
