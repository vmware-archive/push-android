/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.pivotal.android.push.analytics.AnalyticsEventLogger;
import io.pivotal.android.push.analytics.jobs.CheckBackEndVersionJob;
import io.pivotal.android.push.analytics.jobs.PrepareDatabaseJob;
import io.pivotal.android.push.backend.api.PCFPushRegistrationApiRequest;
import io.pivotal.android.push.backend.api.PCFPushRegistrationApiRequestImpl;
import io.pivotal.android.push.backend.api.PCFPushRegistrationApiRequestProvider;
import io.pivotal.android.push.backend.api.PCFPushUnregisterDeviceApiRequest;
import io.pivotal.android.push.backend.api.PCFPushUnregisterDeviceApiRequestImpl;
import io.pivotal.android.push.backend.api.PCFPushUnregisterDeviceApiRequestProvider;
import io.pivotal.android.push.backend.geofence.PCFPushGetGeofenceUpdatesApiRequest;
import io.pivotal.android.push.gcm.GcmProvider;
import io.pivotal.android.push.gcm.GcmRegistrationApiRequest;
import io.pivotal.android.push.gcm.GcmRegistrationApiRequestImpl;
import io.pivotal.android.push.gcm.GcmRegistrationApiRequestProvider;
import io.pivotal.android.push.gcm.GcmUnregistrationApiRequest;
import io.pivotal.android.push.gcm.GcmUnregistrationApiRequestImpl;
import io.pivotal.android.push.gcm.GcmUnregistrationApiRequestProvider;
import io.pivotal.android.push.gcm.RealGcmProvider;
import io.pivotal.android.push.geofence.GeofenceEngine;
import io.pivotal.android.push.geofence.GeofencePersistentStore;
import io.pivotal.android.push.geofence.GeofenceRegistrar;
import io.pivotal.android.push.geofence.GeofenceStatusUtil;
import io.pivotal.android.push.geofence.GeofenceUpdater;
import io.pivotal.android.push.prefs.Pivotal;
import io.pivotal.android.push.prefs.PushPreferencesProvider;
import io.pivotal.android.push.prefs.PushPreferencesProviderImpl;
import io.pivotal.android.push.receiver.AnalyticsEventsSenderAlarmProvider;
import io.pivotal.android.push.receiver.AnalyticsEventsSenderAlarmProviderImpl;
import io.pivotal.android.push.registration.RegistrationEngine;
import io.pivotal.android.push.registration.RegistrationListener;
import io.pivotal.android.push.registration.SubscribeToTagsListener;
import io.pivotal.android.push.registration.UnregistrationEngine;
import io.pivotal.android.push.registration.UnregistrationListener;
import io.pivotal.android.push.service.AnalyticsEventService;
import io.pivotal.android.push.util.DebugUtil;
import io.pivotal.android.push.util.FileHelper;
import io.pivotal.android.push.util.Logger;
import io.pivotal.android.push.util.NetworkWrapper;
import io.pivotal.android.push.util.NetworkWrapperImpl;
import io.pivotal.android.push.util.ServiceStarter;
import io.pivotal.android.push.util.ServiceStarterImpl;
import io.pivotal.android.push.util.TimeProvider;
import io.pivotal.android.push.version.GeofenceStatus;
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

    public static final String GEOFENCE_UPDATE_BROADCAST = "io.pivotal.android.push.geofence.UPDATE";

    private static Push instance;

    // TODO - consider creating an IntentService (instead of a thread pool) in order to process registration and unregistration requests.
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(1);

    /**
     * Retrieves an instance of the Pivotal CF Mobile Services Push SDK singleton object.
     *
     * @param context       A context object.  May not be null.
     * @return  A reference to the singleton Push object.
     */
    public static Push getInstance(@NonNull Context context) {
        if (instance == null) {
            instance = new Push(context);
        }
        return instance;
    }

    private Context context;
    private boolean wasDatabaseCleanupJobRun = false;

    private Push(@NonNull Context context) {
        verifyArguments(context);
        saveArguments(context);

        Logger.i("Push SDK initialized.");
    }

    private void verifyArguments(@NonNull Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
    }

    private void saveArguments(@NonNull Context context) {
        if (context instanceof Application) {
            this.context = context;
        } else {
            this.context = context.getApplicationContext();
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
    public void startRegistration(@Nullable final String deviceAlias,
                                  @Nullable final Set<String> tags,
                                  final boolean areGeofencesEnabled) {
        startRegistration(deviceAlias, tags, areGeofencesEnabled, null);
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
    public void startRegistration(@Nullable final String deviceAlias,
                                  @Nullable final Set<String> tags,
                                  final boolean areGeofencesEnabled,
                                  @Nullable final RegistrationListener listener) {

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
        final PushParameters parameters = getPushParameters(deviceAlias, tags, areGeofencesEnabled, pushPreferencesProvider.getRequestHeaders());
        final PCFPushGetGeofenceUpdatesApiRequest geofenceUpdatesApiRequest = new PCFPushGetGeofenceUpdatesApiRequest(context, networkWrapper);
        final GeofenceRegistrar geofenceRegistrar = new GeofenceRegistrar(context);
        final FileHelper fileHelper = new FileHelper(context);
        final TimeProvider timeProvider = new TimeProvider();
        final GeofencePersistentStore geofencePersistentStore = new GeofencePersistentStore(context, fileHelper);
        final GeofenceEngine geofenceEngine = new GeofenceEngine(geofenceRegistrar, geofencePersistentStore, timeProvider, pushPreferencesProvider);
        final GeofenceUpdater geofenceUpdater = new GeofenceUpdater(context, geofenceUpdatesApiRequest, geofenceEngine, pushPreferencesProvider);
        final GeofenceStatusUtil geofenceStatusUtil = new GeofenceStatusUtil(context);

        checkAnalytics();

        verifyRegistrationArguments(parameters);

        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    final RegistrationEngine registrationEngine = new RegistrationEngine(context,
                            context.getPackageName(),
                            gcmProvider,
                            pushPreferencesProvider,
                            gcmRegistrationApiRequestProvider,
                            gcmUnregistrationApiRequestProvider,
                            PCFPushRegistrationApiRequestProvider,
                            versionProvider,
                            geofenceUpdater,
                            geofenceEngine,
                            geofenceStatusUtil);

                    registrationEngine.registerDevice(parameters, listener);
                } catch (Exception e) {
                    Logger.ex("Push SDK registration failed", e);
                }
            }
        };
        threadPool.execute(runnable);
    }

    private PushParameters getPushParameters(@Nullable String deviceAlias,
                                             @Nullable Set<String> tags,
                                             boolean areGeofencesEnabled,
                                             @Nullable Map<String, String> requestHeaders) {

        final String gcmSenderId = Pivotal.getGcmSenderId(context);
        final String platformUuid = Pivotal.getPlatformUuid(context);
        final String platformSecret = Pivotal.getPlatformSecret(context);
        final String serviceUrl = Pivotal.getServiceUrl(context);
        final Pivotal.SslCertValidationMode sslCertValidationMode = Pivotal.getSslCertValidationMode(context);
        final List<String> pinnedCertificateNames = Pivotal.getPinnedSslCertificateNames(context);
        return new PushParameters(gcmSenderId, platformUuid, platformSecret, serviceUrl, deviceAlias, tags, areGeofencesEnabled, sslCertValidationMode, pinnedCertificateNames, requestHeaders);
    }

    private void verifyRegistrationArguments(@NonNull PushParameters parameters) {
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

    private void checkAnalytics() {
        if (Pivotal.getAreAnalyticsEnabled(context)) {

            final PushPreferencesProviderImpl preferencesProvider = new PushPreferencesProviderImpl(context);
            final TimeProvider timeProvider = new TimeProvider();
            final boolean isDebug = DebugUtil.getInstance(context).isDebuggable();

            if (CheckBackEndVersionJob.isPollingTime(isDebug, timeProvider, preferencesProvider)) {
                final CheckBackEndVersionJob job = new CheckBackEndVersionJob();
                final Intent intent = AnalyticsEventService.getIntentToRunJob(context, job);
                context.startService(intent);
            }

            cleanupDatabase();

        } else {
            Logger.i("Pivotal PushSDK analytics is disabled.");
            final AnalyticsEventsSenderAlarmProvider alarmProvider = new AnalyticsEventsSenderAlarmProviderImpl(context);
            alarmProvider.disableAlarm();
        }
    }

    private void cleanupDatabase() {
        if (!wasDatabaseCleanupJobRun) {

            // If the process has just been initialized, then run the PrepareDatabaseJob in order to prepare the database
            final PrepareDatabaseJob job = new PrepareDatabaseJob();
            final Intent intent = AnalyticsEventService.getIntentToRunJob(context, job);
            context.startService(intent);

        } else {

            // Otherwise, simply make sure that the timer for posting events to the server is enabled.
            final AnalyticsEventsSenderAlarmProvider alarmProvider = new AnalyticsEventsSenderAlarmProviderImpl(context);
            alarmProvider.enableAlarmIfDisabled();
        }
        wasDatabaseCleanupJobRun = true;
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
    public void subscribeToTags(@Nullable final Set<String> tags) {
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
    public void subscribeToTags(@Nullable final Set<String> tags,
                                @Nullable final SubscribeToTagsListener subscribeToTagsListener) {

        final PushPreferencesProvider pushPreferencesProvider = new PushPreferencesProviderImpl(context);
        final String deviceAlias = pushPreferencesProvider.getDeviceAlias();
        final boolean areGeofencesEnabled = pushPreferencesProvider.areGeofencesEnabled();

        startRegistration(deviceAlias, tags, areGeofencesEnabled, new RegistrationListener() {
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
    public void startUnregistration(@Nullable final UnregistrationListener listener) {
        final PushPreferencesProvider pushPreferencesProvider = new PushPreferencesProviderImpl(context);
        final boolean areGeofencesEnabled = pushPreferencesProvider.areGeofencesEnabled();

        final PushParameters parameters = getPushParameters(null, null, areGeofencesEnabled, pushPreferencesProvider.getRequestHeaders());
        verifyUnregistrationArguments(parameters);

        final GcmProvider gcmProvider = new RealGcmProvider(context);
        final GcmUnregistrationApiRequest dummyGcmUnregistrationApiRequest = new GcmUnregistrationApiRequestImpl(context, gcmProvider);
        final GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider = new GcmUnregistrationApiRequestProvider(dummyGcmUnregistrationApiRequest);
        final NetworkWrapper networkWrapper = new NetworkWrapperImpl();
        final PCFPushUnregisterDeviceApiRequest dummyPCFPushUnregisterDeviceApiRequest = new PCFPushUnregisterDeviceApiRequestImpl(context, networkWrapper);
        final PCFPushUnregisterDeviceApiRequestProvider pcfPushUnregisterDeviceApiRequestProvider = new PCFPushUnregisterDeviceApiRequestProvider(dummyPCFPushUnregisterDeviceApiRequest);
        final PCFPushGetGeofenceUpdatesApiRequest geofenceUpdatesApiRequest = new PCFPushGetGeofenceUpdatesApiRequest(context, networkWrapper);
        final GeofenceRegistrar geofenceRegistrar = new GeofenceRegistrar(context);
        final FileHelper fileHelper = new FileHelper(context);
        final TimeProvider timeProvider = new TimeProvider();
        final GeofencePersistentStore geofencePersistentStore = new GeofencePersistentStore(context, fileHelper);
        final GeofenceEngine geofenceEngine = new GeofenceEngine(geofenceRegistrar, geofencePersistentStore, timeProvider, pushPreferencesProvider);
        final GeofenceUpdater geofenceUpdater = new GeofenceUpdater(context, geofenceUpdatesApiRequest, geofenceEngine, pushPreferencesProvider);
        final GeofenceStatusUtil geofenceStatusUtil = new GeofenceStatusUtil(context);

        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    final UnregistrationEngine unregistrationEngine = new UnregistrationEngine(
                            context,
                            gcmProvider,
                            pushPreferencesProvider,
                            gcmUnregistrationApiRequestProvider,
                            pcfPushUnregisterDeviceApiRequestProvider,
                            geofenceUpdater,
                            geofenceStatusUtil);
                    unregistrationEngine.unregisterDevice(parameters, listener);
                } catch (Exception e) {
                    Logger.ex("Push SDK unregistration failed", e);
                }
            }
        };
        threadPool.execute(runnable);
    }

    private void verifyUnregistrationArguments(@NonNull PushParameters parameters) {
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

    /**
     * Call this method to read the current geofence monitoring status.  If an error occurs while geofences are being updated in the background
     * then this status object is the only way to check the status at runtime.  A BroadcastReceiver is triggered whenever this geofence status
     * is changed.
     *
     * @return the current geofence monitoring status
     */
    public GeofenceStatus getGeofenceStatus() {
        final GeofenceStatusUtil geofenceStatusUtil = new GeofenceStatusUtil(context);
        return geofenceStatusUtil.loadGeofenceStatus();
    }

    /**
     * Call this method to read the current PCF Push Notification Service device UUID.  You'll need device UUID if you want to target this
     * specific device with remote notification using the PCF Push Notification Service.  This method will return `null` if the device is
     * not currently registered with PCF Push.
     *
     * @return the current device UUID if the device is registered
     */
    public String getDeviceUuid() {
        final PushPreferencesProvider pushPreferencesProvider = new PushPreferencesProviderImpl(context);
        return pushPreferencesProvider.getPCFPushDeviceRegistrationId();
    }

    /**
     * Call this method in order to inject custom headers into any HTTP requests made by the Push SDK.
     * Note that you can not provide any 'Authorization' or 'Content-Type' headers via this method; they will
     * be ignored by the Push SDK.
     *
     * In order for this method to take effect you will need to call it *before* `startRegistration`,
     * `subscribeToTags`, or any other methods that will make network requests.
     *
     * @param requestHeaders  A Map object with pairs of String headers and String values that will be injected into
     *                        any network requests made by the Push SDK.
     */
    public void setRequestHeaders(@Nullable Map<String, String> requestHeaders) {
        final PushPreferencesProviderImpl preferences = new PushPreferencesProviderImpl(context);
        preferences.setRequestHeaders(requestHeaders);
    }

    /**
     * Call this method to log an analytics event each time a notification has been opened.  You will need to pass the bundle containing the
     * receiptId of the event.
     *
     * @param bundle  a mapping of values for the event instance.
     */
    public void logOpenedNotification(Bundle bundle) {
        final ServiceStarter serviceStarter = new ServiceStarterImpl();
        final PushPreferencesProvider preferences = new PushPreferencesProviderImpl(context);
        final AnalyticsEventLogger eventLogger = new AnalyticsEventLogger(serviceStarter, preferences, context);
        if (bundle != null && bundle.containsKey("receiptId")) {
            final String receiptId = bundle.getString("receiptId");
            if (receiptId != null) {
                eventLogger.logOpenedNotification(receiptId);
            }
        } else {
            Logger.w("Note: notification has no receiptId. No analytics event will be logged for opening this notification.");
        }
    }
}
