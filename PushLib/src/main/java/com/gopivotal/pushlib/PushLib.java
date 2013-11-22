package com.gopivotal.pushlib;

import android.app.Application;
import android.content.Context;

import com.gopivotal.pushlib.backend.BackEndRegistrationApiRequest;
import com.gopivotal.pushlib.backend.BackEndRegistrationApiRequestImpl;
import com.gopivotal.pushlib.backend.BackEndRegistrationApiRequestProvider;
import com.gopivotal.pushlib.backend.BackEndUnregisterDeviceApiRequest;
import com.gopivotal.pushlib.backend.BackEndUnregisterDeviceApiRequestImpl;
import com.gopivotal.pushlib.backend.BackEndUnregisterDeviceApiRequestProvider;
import com.gopivotal.pushlib.gcm.GcmProvider;
import com.gopivotal.pushlib.gcm.GcmRegistrationApiRequest;
import com.gopivotal.pushlib.gcm.GcmRegistrationApiRequestImpl;
import com.gopivotal.pushlib.gcm.GcmRegistrationApiRequestProvider;
import com.gopivotal.pushlib.gcm.RealGcmProvider;
import com.gopivotal.pushlib.network.NetworkWrapper;
import com.gopivotal.pushlib.network.NetworkWrapperImpl;
import com.gopivotal.pushlib.prefs.PreferencesProvider;
import com.gopivotal.pushlib.prefs.RealPreferencesProvider;
import com.gopivotal.pushlib.registration.RegistrationEngine;
import com.gopivotal.pushlib.registration.RegistrationListener;
import com.gopivotal.pushlib.util.Const;
import com.gopivotal.pushlib.version.RealVersionProvider;
import com.gopivotal.pushlib.version.VersionProvider;
import com.xtreme.commons.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class PushLib {

    private static PushLib instance;
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(1);

    public static PushLib init(Context context, PushLibParameters parameters) {
        if (instance == null) {
            instance = new PushLib(context, parameters);
        }
        return instance;
    }

    private Context context;
    private PushLibParameters parameters;

    private PushLib(Context context, PushLibParameters parameters) {
        verifyArguments(context, parameters);
        saveArguments(context, parameters);

        if (!Logger.isSetup()) {
            Logger.setup(context, Const.TAG_NAME);
        }
    }

    private void saveArguments(Context context, PushLibParameters parameters) {
        if (!(context instanceof Application)) {
            this.context = context.getApplicationContext();
        } else {
            this.context = context;
        }
        this.parameters = parameters;
    }

    private void verifyArguments(Context context, PushLibParameters parameters) {
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
        if (parameters == null) {
            throw new IllegalArgumentException("parameters may not be null");
        }
        if (parameters.getGcmSenderId() == null || parameters.getGcmSenderId().isEmpty()) {
            throw new IllegalArgumentException("parameters.senderId may not be null or empty");
        }
        if (parameters.getReleaseUuid() == null || parameters.getReleaseUuid().isEmpty()) {
            throw new IllegalArgumentException("parameters.releaseUuid may not be null or empty");
        }
        if (parameters.getReleaseSecret() == null || parameters.getReleaseSecret().isEmpty()) {
            throw new IllegalArgumentException("parameters.releaseSecret may not be null or empty");
        }
    }

    /**
     * Asynchronously registers the device and application for receiving push notifications.  If the application
     * is already registered then will do nothing.
     *
     * @param listener Optional listener for receiving a callback after registration finishes. This callback may
     *                 be called on a background thread.
     */
    public void startRegistration(final RegistrationListener listener) {
        final GcmProvider gcmProvider = new RealGcmProvider(context);
        final PreferencesProvider preferencesProvider = new RealPreferencesProvider(context);
        final GcmRegistrationApiRequest dummyGcmRegistrationApiRequest = new GcmRegistrationApiRequestImpl(context, gcmProvider);
        final GcmRegistrationApiRequestProvider gcmRegistrationApiRequestProvider = new GcmRegistrationApiRequestProvider(dummyGcmRegistrationApiRequest);
        final NetworkWrapper networkWrapper = new NetworkWrapperImpl();
        final BackEndRegistrationApiRequest dummyBackEndRegistrationApiRequest = new BackEndRegistrationApiRequestImpl(networkWrapper);
        final BackEndRegistrationApiRequestProvider backEndRegistrationApiRequestProvider = new BackEndRegistrationApiRequestProvider(dummyBackEndRegistrationApiRequest);
        final BackEndUnregisterDeviceApiRequest dummyBackEndUnregisterDeviceApiRequest = new BackEndUnregisterDeviceApiRequestImpl(networkWrapper);
        final BackEndUnregisterDeviceApiRequestProvider backEndUnregisterDeviceApiRequestProvider = new BackEndUnregisterDeviceApiRequestProvider(dummyBackEndUnregisterDeviceApiRequest);
        final VersionProvider versionProvider = new RealVersionProvider(context);
        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    final RegistrationEngine registrationEngine = new RegistrationEngine(context, gcmProvider, preferencesProvider, gcmRegistrationApiRequestProvider, backEndRegistrationApiRequestProvider, backEndUnregisterDeviceApiRequestProvider, versionProvider);
                    registrationEngine.registerDevice(parameters, listener);
                } catch (Exception e) {
                    Logger.ex("PushLib registration failed", e);
                }
            }
        };
        threadPool.execute(runnable);
    }
}
