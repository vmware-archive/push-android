package org.omnia.pushsdk;

import android.app.Application;
import android.content.Context;

import org.omnia.pushsdk.backend.BackEndRegistrationApiRequest;
import org.omnia.pushsdk.backend.BackEndRegistrationApiRequestImpl;
import org.omnia.pushsdk.backend.BackEndRegistrationApiRequestProvider;
import org.omnia.pushsdk.backend.BackEndUnregisterDeviceApiRequest;
import org.omnia.pushsdk.backend.BackEndUnregisterDeviceApiRequestImpl;
import org.omnia.pushsdk.backend.BackEndUnregisterDeviceApiRequestProvider;
import org.omnia.pushsdk.gcm.GcmProvider;
import org.omnia.pushsdk.gcm.GcmRegistrationApiRequest;
import org.omnia.pushsdk.gcm.GcmRegistrationApiRequestImpl;
import org.omnia.pushsdk.gcm.GcmRegistrationApiRequestProvider;
import org.omnia.pushsdk.gcm.GcmUnregistrationApiRequest;
import org.omnia.pushsdk.gcm.GcmUnregistrationApiRequestImpl;
import org.omnia.pushsdk.gcm.GcmUnregistrationApiRequestProvider;
import org.omnia.pushsdk.gcm.RealGcmProvider;
import org.omnia.pushsdk.network.NetworkWrapper;
import org.omnia.pushsdk.network.NetworkWrapperImpl;
import org.omnia.pushsdk.prefs.PreferencesProvider;
import org.omnia.pushsdk.prefs.RealPreferencesProvider;
import org.omnia.pushsdk.registration.RegistrationEngine;
import org.omnia.pushsdk.registration.RegistrationListener;
import org.omnia.pushsdk.util.Const;
import org.omnia.pushsdk.util.PushLibLogger;
import org.omnia.pushsdk.version.RealVersionProvider;
import org.omnia.pushsdk.version.VersionProvider;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Entry-point for the Push Library functionality.  Requires Google Play Services,
 * INTERNET and GET_ACCOUNT permissions in order to operate.  Requires SDK level >= 10.
 *
 * The current registration parameters are stored in the application shared preferences.
 * If the user clears the cache then the registration will be "forgotten" and it will
 * be attempted again the next time the registration method is called.
 */
public class PushLib {

    private static PushLib instance;
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(1);

    /**
     * Initializes the Push Library singleton object.  You will
     * need to call this before you can attempt to register with the
     * Push server.
     *
     * @param context  A context object.  May not be null.
     * @return  A reference to the singleton PushLib object.
     */
    public static PushLib init(Context context) {
        if (instance == null) {
            instance = new PushLib(context);
        }
        return instance;
    }

    private Context context;

    private PushLib(Context context) {
        verifyArguments(context);
        saveArguments(context);

        if (!PushLibLogger.isSetup()) {
            PushLibLogger.setup(context, Const.TAG_NAME);
        }
    }

    private void saveArguments(Context context) {
        if (!(context instanceof Application)) {
            this.context = context.getApplicationContext();
        } else {
            this.context = context;
        }
    }

    private void verifyArguments(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
    }

    /**
     * Asynchronously registers the device and application for receiving push notifications.  If the application
     * is already registered then will do nothing.  If some of the registration parameters are different then
     * the last successful registration then the device will be re-registered with the new parameters.  Only
     * one registration attempt will run at a time: if some attempt is currently in progress, then this request
     * will only start after the first attempt completes.
     *
     * @param parameters Provides the parameters required for registration.  May not be null.
     * @param listener Optional listener for receiving a callback after registration finishes. This callback may
     *                 be called on a background thread.  May be null.
     */
    public void startRegistration(final RegistrationParameters parameters, final RegistrationListener listener) {
        verifyRegistrationArguments(parameters);
        final GcmProvider gcmProvider = new RealGcmProvider(context);
        final PreferencesProvider preferencesProvider = new RealPreferencesProvider(context);
        final GcmRegistrationApiRequest dummyGcmRegistrationApiRequest = new GcmRegistrationApiRequestImpl(context, gcmProvider);
        final GcmRegistrationApiRequestProvider gcmRegistrationApiRequestProvider = new GcmRegistrationApiRequestProvider(dummyGcmRegistrationApiRequest);
        final GcmUnregistrationApiRequest dummyGcmUnregistrationApiRequest = new GcmUnregistrationApiRequestImpl(context, gcmProvider);
        final GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider = new GcmUnregistrationApiRequestProvider(dummyGcmUnregistrationApiRequest);
        final NetworkWrapper networkWrapper = new NetworkWrapperImpl();
        final BackEndRegistrationApiRequest dummyBackEndRegistrationApiRequest = new BackEndRegistrationApiRequestImpl(context, networkWrapper);
        final BackEndRegistrationApiRequestProvider backEndRegistrationApiRequestProvider = new BackEndRegistrationApiRequestProvider(dummyBackEndRegistrationApiRequest);
        final BackEndUnregisterDeviceApiRequest dummyBackEndUnregisterDeviceApiRequest = new BackEndUnregisterDeviceApiRequestImpl(networkWrapper);
        final BackEndUnregisterDeviceApiRequestProvider backEndUnregisterDeviceApiRequestProvider = new BackEndUnregisterDeviceApiRequestProvider(dummyBackEndUnregisterDeviceApiRequest);
        final VersionProvider versionProvider = new RealVersionProvider(context);
        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    final RegistrationEngine registrationEngine = new RegistrationEngine(context, gcmProvider, preferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, backEndUnregisterDeviceApiRequestProvider, versionProvider);
                    registrationEngine.registerDevice(parameters, listener);
                } catch (Exception e) {
                    PushLibLogger.ex("PushLib registration failed", e);
                }
            }
        };
        threadPool.execute(runnable);
    }

    private void verifyRegistrationArguments(RegistrationParameters parameters) {
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
}
