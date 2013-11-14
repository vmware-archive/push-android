package com.gopivotal.pushlib;

import android.app.Application;
import android.content.Context;

import com.gopivotal.pushlib.gcm.GcmRegistrationApiRequest;
import com.gopivotal.pushlib.gcm.GcmRegistrationListener;
import com.gopivotal.pushlib.gcm.RealGcmProvider;
import com.gopivotal.pushlib.prefs.RealPreferencesProvider;
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

    public void startRegistration(GcmRegistrationListener listener) {
        final RealGcmProvider gcmProvider = new RealGcmProvider(context);
        final RealPreferencesProvider preferencesProvider = new RealPreferencesProvider(context);
        final GcmRegistrationApiRequest registrar = new GcmRegistrationApiRequest(context, senderId, gcmProvider, preferencesProvider);
        registrar.startRegistration(listener);
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
