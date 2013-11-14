package com.gopivotal.pushlib.gcm;

import android.content.Context;
import android.os.AsyncTask;

import com.xtreme.commons.Logger;

import java.io.IOException;

public class GcmRegistrationApiRequestImpl extends AsyncTask<Void, Void, String> implements GcmRegistrationApiRequest {

    private Context context;
    private String senderId;
    private GcmProvider gcmProvider;
    private GcmRegistrationListener listener;

    public GcmRegistrationApiRequestImpl(Context context, GcmProvider gcmProvider) {
        verifyArguments(context, gcmProvider);
        saveArguments(context, gcmProvider);
    }

    private void verifyArguments(Context context, GcmProvider gcmProvider) {
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
        if (gcmProvider == null) {
            throw new IllegalArgumentException("gcmProvider may not be null");
        }
    }

    private void saveArguments(Context context, GcmProvider gcmProvider) {
        this.context = context;
        this.gcmProvider = gcmProvider;
    }

    public void startRegistration(String senderId, GcmRegistrationListener listener) {
        verifyRegistrationArguments(senderId, listener);
        saveRegistrationArguments(senderId, listener);
        // TODO - stop using a AsyncTask to implement this class since the calling mechanism will already be on its own worker thread
        execute(null);
    }

    private void verifyRegistrationArguments(String senderId, GcmRegistrationListener listener) {
        if (senderId == null) {
            throw new IllegalArgumentException("senderId may not be null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("listener may not be null");
        }
    }

    private void saveRegistrationArguments(String senderId, GcmRegistrationListener listener) {
        this.senderId = senderId;
        this.listener = listener;
    }

    @Override
    protected String doInBackground(Void... v) {

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

    @Override
    public GcmRegistrationApiRequest copy() {
        return new GcmRegistrationApiRequestImpl(context, gcmProvider);
    }
}
