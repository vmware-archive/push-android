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

package io.pivotal.android.push;

import android.app.Application;
import android.content.Context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.pivotal.android.analytics.AnalyticsParameters;
import io.pivotal.android.analytics.AnalyticsSDK;
import io.pivotal.android.common.network.NetworkWrapper;
import io.pivotal.android.common.network.NetworkWrapperImpl;
import io.pivotal.android.common.prefs.AnalyticsPreferencesProvider;
import io.pivotal.android.common.prefs.AnalyticsPreferencesProviderImpl;
import io.pivotal.android.common.util.Logger;
import io.pivotal.android.common.util.ServiceStarter;
import io.pivotal.android.common.util.ServiceStarterImpl;
import io.pivotal.android.push.backend.BackEndRegistrationApiRequest;
import io.pivotal.android.push.backend.BackEndRegistrationApiRequestImpl;
import io.pivotal.android.push.backend.BackEndRegistrationApiRequestProvider;
import io.pivotal.android.push.backend.BackEndUnregisterDeviceApiRequest;
import io.pivotal.android.push.backend.BackEndUnregisterDeviceApiRequestImpl;
import io.pivotal.android.push.backend.BackEndUnregisterDeviceApiRequestProvider;
import io.pivotal.android.push.gcm.GcmProvider;
import io.pivotal.android.push.gcm.GcmRegistrationApiRequest;
import io.pivotal.android.push.gcm.GcmRegistrationApiRequestImpl;
import io.pivotal.android.push.gcm.GcmRegistrationApiRequestProvider;
import io.pivotal.android.push.gcm.GcmUnregistrationApiRequest;
import io.pivotal.android.push.gcm.GcmUnregistrationApiRequestImpl;
import io.pivotal.android.push.gcm.GcmUnregistrationApiRequestProvider;
import io.pivotal.android.push.gcm.RealGcmProvider;
import io.pivotal.android.push.prefs.PushPreferencesProvider;
import io.pivotal.android.push.prefs.PushPreferencesProviderImpl;
import io.pivotal.android.push.registration.RegistrationEngine;
import io.pivotal.android.push.registration.RegistrationListener;
import io.pivotal.android.push.registration.UnregistrationEngine;
import io.pivotal.android.push.registration.UnregistrationListener;
import io.pivotal.android.push.version.VersionProvider;
import io.pivotal.android.push.version.VersionProviderImpl;

/**
 * Entry-point for the Push Library functionality.  Requires Google Play Services,
 * INTERNET and GET_ACCOUNT permissions in order to operate.  Requires SDK level >= 10.
 *
 * The current registration parameters are stored in the application shared preferences.
 * If the user clears the cache then the registration will be "forgotten" and it will
 * be attempted again the next time the registration method is called.
 */
public class PushSDK {

    private static PushSDK instance;

    // TODO - consider creating an IntentService (instead of a thread pool) in order to process
    // registration and unregistration requests.
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(1);

    /**
     * Retrieves an instance of the Pivotal Mobile Services Suite Push SDK singleton object.
     *
     * @param context       A context object.  May not be null.
     * @return  A reference to the singleton PushSDK object.
     */
    public static PushSDK getInstance(Context context) {
        if (instance == null) {
            instance = new PushSDK(context);
        }
        return instance;
    }

    private Context context;

    private PushSDK(Context context) {
        verifyArguments(context);
        saveArguments(context);

        if (!Logger.isSetup()) {
            Logger.setup(context);
        }
        Logger.i("PushSDK initialized.");
    }

    private void verifyArguments(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
    }

    private void saveArguments(Context context) {
        if (!(context instanceof Application)) {
            this.context = context.getApplicationContext();
        } else {
            this.context = context;
        }
    }

    /**
     * Sets up the Analytics SDK.  There can be no analytics events generated until this method
     * is called at least once with the `analyticsEnabled` parameter set to `true`.
     *
     * @param analyticsParameters  the parameterization for the Analytics SDK.
     */
    public void setupAnalytics(AnalyticsParameters analyticsParameters) {
        final AnalyticsSDK analyticsSDK = AnalyticsSDK.getInstance(context);
        analyticsSDK.setParameters(analyticsParameters);
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
        final PushPreferencesProvider pushPreferencesProvider = new PushPreferencesProviderImpl(context);
        final AnalyticsPreferencesProvider analyticsPreferencesProvider = new AnalyticsPreferencesProviderImpl(context);
        final GcmRegistrationApiRequest dummyGcmRegistrationApiRequest = new GcmRegistrationApiRequestImpl(context, gcmProvider);
        final GcmRegistrationApiRequestProvider gcmRegistrationApiRequestProvider = new GcmRegistrationApiRequestProvider(dummyGcmRegistrationApiRequest);
        final GcmUnregistrationApiRequest dummyGcmUnregistrationApiRequest = new GcmUnregistrationApiRequestImpl(context, gcmProvider);
        final GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider = new GcmUnregistrationApiRequestProvider(dummyGcmUnregistrationApiRequest);
        final NetworkWrapper networkWrapper = new NetworkWrapperImpl();
        final BackEndRegistrationApiRequest dummyBackEndRegistrationApiRequest = new BackEndRegistrationApiRequestImpl(context, networkWrapper);
        final BackEndRegistrationApiRequestProvider backEndRegistrationApiRequestProvider = new BackEndRegistrationApiRequestProvider(dummyBackEndRegistrationApiRequest);
        final VersionProvider versionProvider = new VersionProviderImpl(context);
        final ServiceStarter serviceStarter = new ServiceStarterImpl();
        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    final RegistrationEngine registrationEngine = new RegistrationEngine(context, context.getPackageName(), gcmProvider, pushPreferencesProvider, analyticsPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, versionProvider, serviceStarter);
                    registrationEngine.registerDevice(parameters, listener);
                } catch (Exception e) {
                    Logger.ex("PushSDK registration failed", e);
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
        if (parameters.getBaseServerUrl() == null) {
            throw new IllegalArgumentException("parameters.baseServerUrl may not be null");
        }
    }

    /**
     * Asynchronously unregisters the device and application from receiving push notifications.  If the application
     *
     * @param parameters Provides the parameters required for unregistration.  May not be null.
     * @param listener Optional listener for receiving a callback after un`registration finishes. This callback may
     */
    public void startUnregistration(final RegistrationParameters parameters, final UnregistrationListener listener) {
        verifyUnregistrationArguments(parameters);
        final GcmProvider gcmProvider = new RealGcmProvider(context);
        final PushPreferencesProvider pushPreferencesProvider = new PushPreferencesProviderImpl(context);
        final AnalyticsPreferencesProvider analyticsPreferencesProvider = new AnalyticsPreferencesProviderImpl(context);
        final GcmUnregistrationApiRequest dummyGcmUnregistrationApiRequest = new GcmUnregistrationApiRequestImpl(context, gcmProvider);
        final GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider = new GcmUnregistrationApiRequestProvider(dummyGcmUnregistrationApiRequest);
        final NetworkWrapper networkWrapper = new NetworkWrapperImpl();
        final BackEndUnregisterDeviceApiRequest dummyBackEndUnregisterDeviceApiRequest = new BackEndUnregisterDeviceApiRequestImpl(networkWrapper);
        final BackEndUnregisterDeviceApiRequestProvider backEndUnregisterDeviceApiRequestProvider = new BackEndUnregisterDeviceApiRequestProvider(dummyBackEndUnregisterDeviceApiRequest);
        final ServiceStarter serviceStarter = new ServiceStarterImpl();
        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    final UnregistrationEngine unregistrationEngine = new UnregistrationEngine(context, gcmProvider, serviceStarter, pushPreferencesProvider, analyticsPreferencesProvider, gcmUnregistrationApiRequestProvider, backEndUnregisterDeviceApiRequestProvider);
                    unregistrationEngine.unregisterDevice(parameters, listener);
                } catch (Exception e) {
                    Logger.ex("PushSDK unregistration failed", e);
                }
            }
        };
        threadPool.execute(runnable);
    }

    private void verifyUnregistrationArguments(RegistrationParameters parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("parameters may not be null");
        }
        if (parameters.getBaseServerUrl() == null) {
            throw new IllegalArgumentException("parameters.baseServerUrl may not be null");
        }
    }
}
