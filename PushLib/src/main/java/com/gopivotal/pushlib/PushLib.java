package com.gopivotal.pushlib;

import android.app.Application;
import android.content.Context;

import com.gopivotal.pushlib.gcm.GcmRegistrationApiRequestImpl;
import com.gopivotal.pushlib.gcm.GcmRegistrationListener;
import com.gopivotal.pushlib.gcm.RealGcmProvider;
import com.gopivotal.pushlib.prefs.RealPreferencesProvider;
import com.gopivotal.pushlib.registration.RegistrationEngine;
import com.gopivotal.pushlib.registration.RegistrationListener;
import com.xtreme.commons.Logger;

public class PushLib {

    private static PushLib instance;

    public static PushLib init(Context context, String senderId) {
        if (instance == null) {
            instance = new PushLib(context, senderId);
        }
        return instance;
    }

    private Context context;
    private String senderId;

    private PushLib(Context context, String senderId) {
        verifyArguments(context, senderId);
        saveArguments(context, senderId);

        if (!Logger.isSetup()) {
            Logger.setup(context, Const.TAG_NAME);
        }
    }

    /**
     * Registers the device and application for receiving push notifications.  If the application
     * is already registered then will do nothing.
     *
     * @param listener
     */
    public void startRegistration(RegistrationListener listener) {
        final RealGcmProvider gcmProvider = new RealGcmProvider(context);
        final RealPreferencesProvider preferencesProvider = new RealPreferencesProvider(context);
        final RegistrationEngine registrationEngine = new RegistrationEngine(context, gcmProvider, preferencesProvider);
        registrationEngine.registerDevice(senderId, listener);
    }

    private void saveArguments(Context context, String senderId) {
        if (!(context instanceof Application)) {
            this.context = context.getApplicationContext();
        } else {
            this.context = context;
        }
        this.senderId = senderId;
    }

    private void verifyArguments(Context context, String senderId) {
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
        if (senderId == null) {
            throw new IllegalArgumentException("senderId may not be null");
        }

    }

}
