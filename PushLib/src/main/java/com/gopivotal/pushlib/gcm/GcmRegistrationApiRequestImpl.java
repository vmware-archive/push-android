package com.gopivotal.pushlib.gcm;

import android.content.Context;
import android.content.pm.PackageManager;

import com.gopivotal.pushlib.util.PushLibLogger;
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

            saveDeviceRegistrationIdToFilesystem(deviceRegistrationId);

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

    private void saveDeviceRegistrationIdToFilesystem(String deviceRegistrationId) {
        // Saves the registration ID to the file system.  Useful for debugging
        // only since the registration ID is not loaded anywhere.
        if (DebugUtil.getInstance(context).isDebuggable()) {
            int res = context.checkCallingOrSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE");
            if (res == PackageManager.PERMISSION_GRANTED) {
                final PrintWriter pw;
                try {
                    final File externalFilesDir = context.getExternalFilesDir(null);
                    if (externalFilesDir == null) {
                        PushLibLogger.d("Was not able to get the externalFilesDir");
                        return;
                    }
                    final File dir = new File(externalFilesDir.getAbsolutePath() + File.separator + "pushlib");
                    if (!dir.exists()) {
                        dir.mkdir();
                    }
                    final File regIdFile = new File(dir, "regid.txt");
                    pw = new PrintWriter(regIdFile);
                    pw.println(deviceRegistrationId);
                    pw.close();
                    PushLibLogger.d("Saved registration ID to file: " + regIdFile.getAbsolutePath());
                } catch (Exception e) {
                    PushLibLogger.w("Was not able to save registration ID to filesystem. This error is not-fatal. " + e.getLocalizedMessage());
                }
            }
        }
    }

    @Override
    public GcmRegistrationApiRequest copy() {
        return new GcmRegistrationApiRequestImpl(context, gcmProvider);
    }
}
