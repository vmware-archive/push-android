package com.gopivotal.pushlib.gcm;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import com.gopivotal.pushlib.Const;
import com.xtreme.commons.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

public class GcmRegistrar extends AsyncTask<GcmRegistrarListener, Void, String> {

    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";

    private Context context;
    private String senderId;
    private GcmProvider gcmProvider;

    public GcmRegistrar(Context context, String senderId, GcmProvider gcmProvider) {
        verifyArguments(context, senderId, gcmProvider);
        saveArguments(context, senderId, gcmProvider);
    }

    private void saveArguments(Context context, String senderId, GcmProvider gcmProvider) {
        this.context = context;
        this.senderId = senderId;
        this.gcmProvider = gcmProvider;
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

    public void startRegistration(GcmRegistrarListener listener) {

        final String regId = getRegistrationId(context);

        if (regId.isEmpty()) {
            registerInBackground(listener);
        } else {
            // TODO - do we need to register with Studio server on every launch, or only when a new registration ID is created (I suspect the latter).
            Logger.i("Loaded registration ID: " + regId);
            if (listener != null) {
                listener.onRegistrationComplete(regId);
            }
        }
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p/>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     * registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Logger.i("Registration not found. Registration will be required.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Logger.i("App version changed. Registration will be required.");
            return "";
        }
        return registrationId;
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

        String msg = "";
        try {
            final String regId = gcmProvider.register(senderId);
            Logger.i("Device registered. Registration ID:" + regId);

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
            storeRegistrationId(context, regId);

            // Inform callback of registration success
            if (listener != null) {
                listener.onRegistrationComplete(regId);
            }
            return regId;
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

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId   registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Logger.i("Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
        //PrintWriter p = null;
        //try {
        //    // TODO - save regid to app cache directory
        //    p = new PrintWriter("/mnt/sdcard/regid.txt");
        //    p.print(regId);
        //    p.close();
        //} catch (FileNotFoundException e) {
        //    e.printStackTrace();
        //}
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return context.getSharedPreferences(Const.TAG_NAME, Context.MODE_PRIVATE);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            final PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
}
