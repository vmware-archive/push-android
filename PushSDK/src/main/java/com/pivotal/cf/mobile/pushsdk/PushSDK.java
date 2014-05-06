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

package com.pivotal.cf.mobile.pushsdk;

import android.app.Application;
import android.content.Context;

import com.pivotal.cf.mobile.analyticssdk.AnalyticsSDK;
import com.pivotal.cf.mobile.common.network.NetworkWrapper;
import com.pivotal.cf.mobile.common.network.NetworkWrapperImpl;
import com.pivotal.cf.mobile.pushsdk.prefs.PreferencesProvider;
import com.pivotal.cf.mobile.pushsdk.prefs.PreferencesProviderImpl;
import com.pivotal.cf.mobile.common.util.Logger;
import com.pivotal.cf.mobile.pushsdk.backend.BackEndRegistrationApiRequest;
import com.pivotal.cf.mobile.pushsdk.backend.BackEndRegistrationApiRequestImpl;
import com.pivotal.cf.mobile.pushsdk.backend.BackEndRegistrationApiRequestProvider;
import com.pivotal.cf.mobile.pushsdk.backend.BackEndUnregisterDeviceApiRequest;
import com.pivotal.cf.mobile.pushsdk.backend.BackEndUnregisterDeviceApiRequestImpl;
import com.pivotal.cf.mobile.pushsdk.backend.BackEndUnregisterDeviceApiRequestProvider;
import com.pivotal.cf.mobile.pushsdk.gcm.GcmProvider;
import com.pivotal.cf.mobile.pushsdk.gcm.GcmRegistrationApiRequest;
import com.pivotal.cf.mobile.pushsdk.gcm.GcmRegistrationApiRequestImpl;
import com.pivotal.cf.mobile.pushsdk.gcm.GcmRegistrationApiRequestProvider;
import com.pivotal.cf.mobile.pushsdk.gcm.GcmUnregistrationApiRequest;
import com.pivotal.cf.mobile.pushsdk.gcm.GcmUnregistrationApiRequestImpl;
import com.pivotal.cf.mobile.pushsdk.gcm.GcmUnregistrationApiRequestProvider;
import com.pivotal.cf.mobile.pushsdk.gcm.RealGcmProvider;
import com.pivotal.cf.mobile.pushsdk.registration.RegistrationEngine;
import com.pivotal.cf.mobile.pushsdk.registration.RegistrationListener;
import com.pivotal.cf.mobile.pushsdk.registration.UnregistrationEngine;
import com.pivotal.cf.mobile.pushsdk.registration.UnregistrationListener;
import com.pivotal.cf.mobile.pushsdk.version.VersionProvider;
import com.pivotal.cf.mobile.pushsdk.version.VersionProviderImpl;

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
public class PushSDK {

    private static PushSDK instance;

    // TODO - consider creating an IntentService (instead of a thread pool) in order to process
    // registration and unregistration requests.
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(1);

    // TODO - decide if it is really appropriate to pass an instance of the Analytics SDK to the Push SDK.
    // The Push SDK can generate analytics events without access to the Analytics engine front end, of course.
    /**
     * Retrieves an instance of the Pivotal CF Mobile Services Push SDK singleton object.
     *
     * @param analyticsSDK  An instance of the Pivotal CF Mobile Services Analytics SDK. Use `null` if you don't
     *                      want analytics.
     * @param context       A context object.  May not be null.
     * @return  A reference to the singleton PushSDK object.
     */
    public static PushSDK getInstance(AnalyticsSDK analyticsSDK, Context context) {
        if (instance == null) {
            instance = new PushSDK(analyticsSDK, context);
        }
        return instance;
    }

    private Context context;
    private AnalyticsSDK analyticsSDK;

    private PushSDK(AnalyticsSDK analyticsSDK, Context context) {
        verifyArguments(context);
        saveArguments(analyticsSDK, context);

        if (!Logger.isSetup()) {
            Logger.setup(context);
        }
        Logger.i("PushSDK initialized.");
    }

    private void verifyArguments(Context context) {
        // NOTE - analyticsSDK is considered optional
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
    }

    private void saveArguments(AnalyticsSDK analyticsSDK, Context context) {
        if (!(context instanceof Application)) {
            this.context = context.getApplicationContext();
        } else {
            this.context = context;
        }
        this.analyticsSDK = analyticsSDK;
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
        final VersionProvider versionProvider = new VersionProviderImpl(context);
        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    final RegistrationEngine registrationEngine = new RegistrationEngine(context, context.getPackageName(), gcmProvider, preferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, backEndRegistrationApiRequestProvider, versionProvider);
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
