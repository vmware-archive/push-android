package com.gopivotal.pushlib.gcm;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import com.xtreme.commons.DebugUtil;
import com.xtreme.commons.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

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

            saveDeviceRegistrationIdToFilesystem(deviceRegistrationId);

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

    private void saveDeviceRegistrationIdToFilesystem(String deviceRegistrationId) {
        // Saves the registration ID to the file system.  Useful for debugging
        // only since the registration ID is not loaded anywhere.
        if (DebugUtil.getInstance(context).isDebuggable()) {
            int res = context.checkCallingOrSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE");
            if (res == PackageManager.PERMISSION_GRANTED) {
                final PrintWriter pw;
                try {
                    final File externalFilesDir = context.getExternalFilesDir(null);
                    final File dir = new File(externalFilesDir.getAbsolutePath() + File.separator + "pushlib");
                    if (!dir.exists()) {
                        dir.mkdir();
                    }
                    final File regIdFile = new File(dir, "regid.txt");
                    pw = new PrintWriter(regIdFile);
                    pw.println(deviceRegistrationId);
                    pw.close();
                    Logger.i("Saved registration ID to file: " + regIdFile.getAbsolutePath());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public GcmRegistrationApiRequest copy() {
        return new GcmRegistrationApiRequestImpl(context, gcmProvider);
    }
}
