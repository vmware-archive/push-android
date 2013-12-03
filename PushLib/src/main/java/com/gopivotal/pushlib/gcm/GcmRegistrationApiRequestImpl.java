package com.gopivotal.pushlib.gcm;

import android.content.Context;
import android.content.pm.PackageManager;

import com.gopivotal.pushlib.util.PushLibLogger;
import com.gopivotal.pushlib.util.Util;
import com.xtreme.commons.DebugUtil;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class GcmRegistrationApiRequestImpl implements GcmRegistrationApiRequest {

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
        executeRegistration();
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

    private void executeRegistration() {
        try {
            final String deviceRegistrationId = gcmProvider.register(senderId);
            PushLibLogger.i("Device registered with GCM. Device registration ID:" + deviceRegistrationId);

            Util.saveIdToFilesystem(context, deviceRegistrationId, "gcm_registration_id");

            // Inform callback of registration success
            if (listener != null) {
                listener.onGcmRegistrationComplete(deviceRegistrationId);
            }

        } catch (IOException ex) {
            PushLibLogger.ex("Error registering device with GCM:", ex);
            // If there is an error, don't just keep trying to register.
            // Require the user to click a button again, or perform
            // exponential back-off.
            if (listener != null) {
                listener.onGcmRegistrationFailed(ex.getLocalizedMessage());
            }
        }
    }

    @Override
    public GcmRegistrationApiRequest copy() {
        return new GcmRegistrationApiRequestImpl(context, gcmProvider);
    }
}
