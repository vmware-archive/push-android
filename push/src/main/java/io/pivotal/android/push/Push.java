/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push;

import android.app.Application;
import android.content.Context;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.pivotal.android.push.backend.api.PCFPushRegistrationApiRequest;
import io.pivotal.android.push.backend.api.PCFPushRegistrationApiRequestImpl;
import io.pivotal.android.push.backend.api.PCFPushRegistrationApiRequestProvider;
import io.pivotal.android.push.backend.api.PCFPushUnregisterDeviceApiRequest;
import io.pivotal.android.push.backend.api.PCFPushUnregisterDeviceApiRequestImpl;
import io.pivotal.android.push.backend.api.PCFPushUnregisterDeviceApiRequestProvider;
import io.pivotal.android.push.gcm.GcmProvider;
import io.pivotal.android.push.gcm.GcmRegistrationApiRequest;
import io.pivotal.android.push.gcm.GcmRegistrationApiRequestImpl;
import io.pivotal.android.push.gcm.GcmRegistrationApiRequestProvider;
import io.pivotal.android.push.gcm.GcmUnregistrationApiRequest;
import io.pivotal.android.push.gcm.GcmUnregistrationApiRequestImpl;
import io.pivotal.android.push.gcm.GcmUnregistrationApiRequestProvider;
import io.pivotal.android.push.gcm.RealGcmProvider;
import io.pivotal.android.push.prefs.Pivotal;
import io.pivotal.android.push.prefs.PushPreferencesProvider;
import io.pivotal.android.push.prefs.PushPreferencesProviderImpl;
import io.pivotal.android.push.registration.RegistrationEngine;
import io.pivotal.android.push.registration.RegistrationListener;
import io.pivotal.android.push.registration.SubscribeToTagsListener;
import io.pivotal.android.push.registration.UnregistrationEngine;
import io.pivotal.android.push.registration.UnregistrationListener;
import io.pivotal.android.push.util.Logger;
import io.pivotal.android.push.util.NetworkWrapper;
import io.pivotal.android.push.util.NetworkWrapperImpl;
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
public class Push {

    private static Push instance;

    // TODO - consider creating an IntentService (instead of a thread pool) in order to process registration and unregistration requests.
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(1);

    /**
     * Retrieves an instance of the Pivotal CF Mobile Services Push SDK singleton object.
     *
     * @param context       A context object.  May not be null.
     * @return  A reference to the singleton Push object.
     */
    public static Push getInstance(Context context) {
        if (instance == null) {
            instance = new Push(context);
        }
        return instance;
    }

    private Context context;

    private Push(Context context) {
        verifyArguments(context);
        saveArguments(context);

        Logger.i("Push SDK initialized.");
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
     * Asynchronously registers the device and application for receiving push notifications.  If the application
     * is already registered then will do nothing.  If some of the registration parameters are different then
     * the last successful registration then the device will be re-registered with the new parameters.  Only
     * one registration attempt will run at a time: if some attempt is currently in progress, then this request
     * will only start after the first attempt completes.
     *
     * @param deviceAlias Provides the device alias for registration.  This is optional and may be null
     * @param tags Provides the list of tags for registration.  This is optional and may be null.
     */
    public void startRegistration(final String deviceAlias, final Set<String> tags) {
        startRegistration(deviceAlias, tags, null);
    }

    /**
     * Asynchronously registers the device and application for receiving push notifications.  If the application
     * is already registered then will do nothing.  If some of the registration parameters are different then
     * the last successful registration then the device will be re-registered with the new parameters.  Only
     * one registration attempt will run at a time: if some attempt is currently in progress, then this request
     * will only start after the first attempt completes.
     *
     * @param deviceAlias Provides the device alias for registration.  This is optional and may be null.
     * @param tags Provides the list of tags for registration.  This is optional and may be null.
     * @param listener Optional listener for receiving a callback after registration finishes. This callback may
     *                 be called on a background thread.  May be null.
     */
    public void startRegistration(final String deviceAlias, final Set<String> tags, final RegistrationListener listener) {
        final GcmProvider gcmProvider = new RealGcmProvider(context);
        final PushPreferencesProvider pushPreferencesProvider = new PushPreferencesProviderImpl(context);
        final GcmRegistrationApiRequest dummyGcmRegistrationApiRequest = new GcmRegistrationApiRequestImpl(context, gcmProvider);
        final GcmRegistrationApiRequestProvider gcmRegistrationApiRequestProvider = new GcmRegistrationApiRequestProvider(dummyGcmRegistrationApiRequest);
        final GcmUnregistrationApiRequest dummyGcmUnregistrationApiRequest = new GcmUnregistrationApiRequestImpl(context, gcmProvider);
        final GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider = new GcmUnregistrationApiRequestProvider(dummyGcmUnregistrationApiRequest);
        final NetworkWrapper networkWrapper = new NetworkWrapperImpl();
        final PCFPushRegistrationApiRequest dummyPCFPushRegistrationApiRequest = new PCFPushRegistrationApiRequestImpl(context, networkWrapper);
        final PCFPushRegistrationApiRequestProvider PCFPushRegistrationApiRequestProvider = new PCFPushRegistrationApiRequestProvider(dummyPCFPushRegistrationApiRequest);
        final VersionProvider versionProvider = new VersionProviderImpl(context);
        final PushParameters parameters = getPushParameters(deviceAlias, tags);

        verifyRegistrationArguments(parameters);

        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    final RegistrationEngine registrationEngine = new RegistrationEngine(context, context.getPackageName(), gcmProvider, pushPreferencesProvider, gcmRegistrationApiRequestProvider, gcmUnregistrationApiRequestProvider, PCFPushRegistrationApiRequestProvider, versionProvider);
                    registrationEngine.registerDevice(parameters, listener);
                } catch (Exception e) {
                    Logger.ex("Push SDK registration failed", e);
                }
            }
        };
        threadPool.execute(runnable);
    }

    private PushParameters getPushParameters(String deviceAlias, Set<String> tags) {
        final String gcmSenderId = Pivotal.getGcmSenderId();
        final String platformUuid = Pivotal.getPlatformUuid();
        final String platformSecret = Pivotal.getPlatformSecret();
        final String serviceUrl = Pivotal.getServiceUrl();
        return new PushParameters(gcmSenderId, platformUuid, platformSecret, serviceUrl, deviceAlias, tags);
    }

    private void verifyRegistrationArguments(PushParameters parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("parameters may not be null");
        }
        if (parameters.getGcmSenderId() == null || parameters.getGcmSenderId().isEmpty()) {
            throw new IllegalArgumentException("parameters.senderId may not be null or empty");
        }
        if (parameters.getPlatformUuid() == null || parameters.getPlatformUuid().isEmpty()) {
            throw new IllegalArgumentException("parameters.platformUuid may not be null or empty");
        }
        if (parameters.getPlatformSecret() == null || parameters.getPlatformSecret().isEmpty()) {
            throw new IllegalArgumentException("parameters.platformSecret may not be null or empty");
        }
        if (parameters.getServiceUrl() == null) {
            throw new IllegalArgumentException("parameters.serviceUrl may not be null");
        }
    }

    /**
     * Sets the tags that the device should be subscribed to. Always provide the entire
     * list of tags that the device should be subscribed to. If the device is already subscribed to
     * some tags and those tags are not provided when calling this method again then those
     * tags will be unsubscribed.
     *
     * NOTE: Calling this method will perform a device registration, if the device has not been registered yet
     *
     * @param tags Provides the list of tags the device should subscribe to. Allowed to be `null` or empty.
     */
    public void subscribeToTags(final Set<String> tags) {
        subscribeToTags(tags, null);
    }

    /**
     * Sets the tags that the device should be subscribed to. Always provide the entire
     * list of tags that the device should be subscribed to. If the device is already subscribed to
     * some tags and those tags are not provided when calling this method again then those
     * tags will be unsubscribed.
     *
     * NOTE: Calling this method will perform a device registration, if the device has not been registered yet
     *
     * @param tags Provides the list of tags the device should subscribe to. Allowed to be `null` or empty.
     *
     * @param subscribeToTagsListener Optional listener for receiving a callback after registration finishes.
     *        This callback may be called on a background thread.  May be `null`.
     *
     *        onSubscribeToTagsComplete will be executed if subscription is successful. This method may be called on
     *                a background thread.
     *
     *        onSubscribeToTagsFailed will be executed if subscription fails. This method may be called on a
     *                background thread.
     */
    public void subscribeToTags(final Set<String> tags, final SubscribeToTagsListener subscribeToTagsListener) {

        final PushPreferencesProvider pushPreferencesProvider = new PushPreferencesProviderImpl(context);
        final String deviceAlias = pushPreferencesProvider.getDeviceAlias();

        startRegistration(deviceAlias, tags, new RegistrationListener() {
            @Override
            public void onRegistrationComplete() {
                if (subscribeToTagsListener != null) {
                    subscribeToTagsListener.onSubscribeToTagsComplete();
                }
            }

            @Override
            public void onRegistrationFailed(String reason) {
                if (subscribeToTagsListener != null) {
                    subscribeToTagsListener.onSubscribeToTagsFailed(reason);
                }
            }
        });
    }

    /**
     * Asynchronously unregisters the device and application from receiving push notifications.
     *
     */
    public void startUnregistration() {
        startUnregistration(null);
    }

    /**
     * Asynchronously unregisters the device and application from receiving push notifications.
     *
     * @param listener Optional listener for receiving a callback after un`registration finishes. This callback may
     */
    public void startUnregistration(final UnregistrationListener listener) {
        final PushParameters parameters = getPushParameters(null, null);
        verifyUnregistrationArguments(parameters);

        final GcmProvider gcmProvider = new RealGcmProvider(context);
        final PushPreferencesProvider pushPreferencesProvider = new PushPreferencesProviderImpl(context);
        final GcmUnregistrationApiRequest dummyGcmUnregistrationApiRequest = new GcmUnregistrationApiRequestImpl(context, gcmProvider);
        final GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider = new GcmUnregistrationApiRequestProvider(dummyGcmUnregistrationApiRequest);
        final NetworkWrapper networkWrapper = new NetworkWrapperImpl();
        final PCFPushUnregisterDeviceApiRequest dummyPCFPushUnregisterDeviceApiRequest = new PCFPushUnregisterDeviceApiRequestImpl(networkWrapper);
        final PCFPushUnregisterDeviceApiRequestProvider PCFPushUnregisterDeviceApiRequestProvider = new PCFPushUnregisterDeviceApiRequestProvider(dummyPCFPushUnregisterDeviceApiRequest);
        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    final UnregistrationEngine unregistrationEngine = new UnregistrationEngine(context, gcmProvider, pushPreferencesProvider, gcmUnregistrationApiRequestProvider, PCFPushUnregisterDeviceApiRequestProvider);
                    unregistrationEngine.unregisterDevice(parameters, listener);
                } catch (Exception e) {
                    Logger.ex("Push SDK unregistration failed", e);
                }
            }
        };
        threadPool.execute(runnable);
    }

    private void verifyUnregistrationArguments(PushParameters parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("parameters may not be null");
        }
        if (parameters.getServiceUrl() == null) {
            throw new IllegalArgumentException("parameters.serviceUrl may not be null");
        }
    }

    /**
     * @return the current version of the Pivotal CF Push Client SDK.
     */
    public static String getVersion() {
        return BuildConfig.VERSION_NAME;
    }
}
