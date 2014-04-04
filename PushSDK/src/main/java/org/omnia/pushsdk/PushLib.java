/* Copyright (c) 2013 Pivotal Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import org.omnia.pushsdk.prefs.PreferencesProviderImpl;
import org.omnia.pushsdk.registration.RegistrationEngine;
import org.omnia.pushsdk.registration.RegistrationListener;
import org.omnia.pushsdk.registration.UnregistrationEngine;
import org.omnia.pushsdk.registration.UnregistrationListener;
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
        final PreferencesProvider preferencesProvider = new PreferencesProviderImpl(context);
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
                    final RegistrationEngine registrationEngine = new RegistrationEngine(context, context.getPackageName(), gcmProvider, preferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, backEndUnregisterDeviceApiRequestProvider, versionProvider);
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
        if (parameters.getVariantUuid() == null || parameters.getVariantUuid().isEmpty()) {
            throw new IllegalArgumentException("parameters.variantUuid may not be null or empty");
        }
        if (parameters.getVariantSecret() == null || parameters.getVariantSecret().isEmpty()) {
            throw new IllegalArgumentException("parameters.variantSecret may not be null or empty");
        }
    }

    /**
     * Asynchronously unregisters the device and application from receiving push notifications.  If the application
     *
     * @param listener Optional listener for receiving a callback after un`registration finishes. This callback may
     *                 be called on a background thread.  May be null.
     */
    public void startUnregistration(final UnregistrationListener listener) {
        final GcmProvider gcmProvider = new RealGcmProvider(context);
        final PreferencesProvider preferencesProvider = new PreferencesProviderImpl(context);
        final GcmUnregistrationApiRequest dummyGcmUnregistrationApiRequest = new GcmUnregistrationApiRequestImpl(context, gcmProvider);
        final GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider = new GcmUnregistrationApiRequestProvider(dummyGcmUnregistrationApiRequest);
        final NetworkWrapper networkWrapper = new NetworkWrapperImpl();
        final BackEndUnregisterDeviceApiRequest dummyBackEndUnregisterDeviceApiRequest = new BackEndUnregisterDeviceApiRequestImpl(networkWrapper);
        final BackEndUnregisterDeviceApiRequestProvider backEndUnregisterDeviceApiRequestProvider = new BackEndUnregisterDeviceApiRequestProvider(dummyBackEndUnregisterDeviceApiRequest);
        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    final UnregistrationEngine unregistrationEngine = new UnregistrationEngine(context, gcmProvider, preferencesProvider, gcmUnregistrationApiRequestProvider, backEndUnregisterDeviceApiRequestProvider);
                    unregistrationEngine.unregisterDevice(listener);
                } catch (Exception e) {
                    PushLibLogger.ex("PushLib unregistration failed", e);
                }
            }
        };
        threadPool.execute(runnable);
    }
}
