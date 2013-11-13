package com.gopivotal.pushlib.gcm;

import android.content.Context;
import android.os.AsyncTask;

import com.gopivotal.pushlib.prefs.PreferencesProvider;
import com.xtreme.commons.Logger;

import java.io.IOException;

public class GcmRegistrar extends AsyncTask<GcmRegistrarListener, Void, String> {

    private Context context;
    private String senderId;
    private GcmProvider gcmProvider;
    private PreferencesProvider preferencesProvider;

    public GcmRegistrar(Context context, String senderId, GcmProvider gcmProvider, PreferencesProvider preferencesProvider) {
        verifyArguments(context, senderId, gcmProvider, preferencesProvider);
        saveArguments(context, senderId, gcmProvider, preferencesProvider);
    }

    private void verifyArguments(Context context, String senderId, GcmProvider gcmProvider, PreferencesProvider preferencesProvider) {
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
        if (senderId == null) {
            throw new IllegalArgumentException("senderId may not be null");
        }
        if (gcmProvider == null) {
            throw new IllegalArgumentException("gcmProvider may not be null");
        }
        if (preferencesProvider == null) {
            throw new IllegalArgumentException("preferencesProvider may not be null");
        }
    }

    private void saveArguments(Context context, String senderId, GcmProvider gcmProvider, PreferencesProvider preferencesProvider) {
        this.context = context;
        this.senderId = senderId;
        this.gcmProvider = gcmProvider;
        this.preferencesProvider = preferencesProvider;
    }

    public void startRegistration(GcmRegistrarListener listener) {

        final String deviceRegistrationId = preferencesProvider.loadDeviceRegistrationId();

        if (deviceRegistrationId == null || deviceRegistrationId.isEmpty()) {
            registerInBackground(listener);
        } else {
            // TODO - do we need to register with Studio server on every launch, or only when a new registration ID is created (I suspect the latter).
            Logger.i("Loaded device registration ID: " + deviceRegistrationId);
            if (listener != null) {
                listener.onRegistrationComplete(deviceRegistrationId);
            }
        }
    }
    private void registerInBackground(GcmRegistrarListener listener) {
        execute(listener);
    }

    @Override
    protected String doInBackground(GcmRegistrarListener... listeners) {

        GcmRegistrarListener listener = null;
        if (listeners != null && listeners.length > 0) {
            listener = listeners[0];
        }

        try {
            final String deviceRegistrationId = gcmProvider.register(senderId);
            Logger.i("Device registered. Device Registration ID:" + deviceRegistrationId);

            // You should send the registration ID to your server over HTTP,
            // so it can use GCM/HTTP or CCS to send messages to your app.
            // The request to your server should be authenticated if your app
            // is using accounts.
            // NOTE: may need the listener
            sendRegistrationIdToBackend();

            // For this demo: we don't need to send it because the device
            // will send upstream messages to a server that echo back the
            // message using the 'from' address in the message.

            // Persist the regID - no need to register again.
            preferencesProvider.saveDeviceRegistrationId(deviceRegistrationId);

            // Inform callback of registration success
            if (listener != null) {
                listener.onRegistrationComplete(deviceRegistrationId);
            }
            return deviceRegistrationId;
        } catch (IOException ex) {
            Logger.ex("Error registering device:", ex);
            // If there is an error, don't just keep trying to register.
            // Require the user to click a button again, or perform
            // exponential back-off.
            if (listener != null) {
                listener.onRegistrationFailed(ex.getLocalizedMessage());
            }
            return null;
        }
    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app. Not needed for this demo since the
     * device sends upstream messages to a server that echoes back the message
     * using the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend() {
        // Your implementation here.
        // NOTE - should we bring the callback in here as well?
    }

}
