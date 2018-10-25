package io.pivotal.android.push;

import static com.google.gson.internal.$Gson$Preconditions.checkArgument;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;

import io.pivotal.android.push.analytics.jobs.PrepareDatabaseJob;
import io.pivotal.android.push.backend.api.PCFPushUnregisterDeviceApiRequest;
import io.pivotal.android.push.backend.api.PCFPushUnregisterDeviceApiRequestImpl;
import io.pivotal.android.push.backend.api.PCFPushUnregisterDeviceApiRequestProvider;
import io.pivotal.android.push.baidu.UnregistrationEngine;
import io.pivotal.android.push.prefs.PushPreferencesBaidu;
import io.pivotal.android.push.receiver.AnalyticsEventsSenderAlarmProvider;
import io.pivotal.android.push.receiver.AnalyticsEventsSenderAlarmProviderImpl;
import io.pivotal.android.push.registration.SubscribeToTagsListener;
import io.pivotal.android.push.registration.UnregistrationListener;
import io.pivotal.android.push.service.AnalyticsEventService;
import io.pivotal.android.push.util.NetworkWrapper;
import io.pivotal.android.push.util.NetworkWrapperImpl;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.pivotal.android.push.analytics.AnalyticsEventLogger;
import io.pivotal.android.push.prefs.Pivotal;
import io.pivotal.android.push.prefs.PushRequestHeaders;
import io.pivotal.android.push.baidu.RegistrationEngine;
import io.pivotal.android.push.registration.RegistrationListener;
import io.pivotal.android.push.util.Logger;
import io.pivotal.android.push.util.ServiceStarter;
import io.pivotal.android.push.util.ServiceStarterImpl;

public class Push {
    private static Push instance;
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(1);
    private String baiduAPIKey;
    private boolean wasDatabaseCleanupJobRun = false;

    public synchronized static Push getInstance(Context context) {
        if (instance == null) {
            instance = new Push(context);
        }
        return instance;
    }

    private Context context;
    private PushParameters parameters = null;
    private PushServiceInfo pushServiceInfo = null;

    private RegistrationListener registrationListener;
    private UnregistrationListener unregistrationListener;

    private Push(@NonNull Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }

        if (context instanceof Application) {
            this.context = context;
        } else {
            this.context = context.getApplicationContext();
        }

        Logger.i("Push SDK initialized.");
    }

    /**
     * Sets the Baidu API key to be used with Baidu device registration.
     *
     * @param baiduAPIKey
     */
    public void setBaiduAPIKey(@NonNull final String baiduAPIKey) {
        this.baiduAPIKey = baiduAPIKey;
    }

    /**
     * Asynchronously registers the device and application for receiving push notifications.  If the application
     * is already registered then will do nothing.  If some of the registration parameters are different then
     * the last successful registration then the device will be re-registered with the new parameters.  Only
     * one registration attempt will run at a time: if some attempt is currently in progress, then this request
     * will only start after the first attempt completes.
     * @param deviceAlias Provides the device alias for registration.  This is optional and may be null
     * @param tags Provides the list of tags for registration.  This is optional and may be null.
     */
    public void startRegistration(@Nullable final String deviceAlias,
                                  @Nullable final Set<String> tags) {
        startRegistration(deviceAlias, tags, null);
    }

    /**
     * Asynchronously registers the device and application for receiving push notifications.  If the application
     * is already registered then will do nothing.  If some of the registration parameters are different then
     * the last successful registration then the device will be re-registered with the new parameters.  Only
     * one registration attempt will run at a time: if some attempt is currently in progress, then this request
     * will only start after the first attempt completes.
     * @param deviceAlias Provides the device alias for registration.  This is optional and may be null.
     * @param tags Provides the list of tags for registration.  This is optional and may be null.
     * @param listener Optional listener for receiving a callback after registration finishes. This callback may
     */
    public void startRegistration(@Nullable final String deviceAlias,
                                  @Nullable final Set<String> tags,
                                  @Nullable final RegistrationListener listener) {
        final String customUserId = null;
        startRegistration(deviceAlias, customUserId, tags, listener);
    }

    /**
     * Asynchronously registers the device and application for receiving push notifications.  If the application
     * is already registered then will do nothing.  If some of the registration parameters are different then
     * the last successful registration then the device will be re-registered with the new parameters.  Only
     * one registration attempt will run at a time: if some attempt is currently in progress, then this request
     * will only start after the first attempt completes.
     * @param deviceAlias Provides the device alias for registration.  This is optional and may be null.
     * @param customUserId Provides a custom user ID that can be used to identify the user using the device. This field is optional and may be null.
     * @param tags Provides the list of tags for registration.  This is optional and may be null.
     * @param listener Optional listener for receiving a callback after registration finishes. This callback may
     */
    public synchronized void startRegistration(@Nullable final String deviceAlias,
                                               @Nullable final String customUserId,
                                               @Nullable final Set<String> tags,
                                               @Nullable final RegistrationListener listener) {
        checkArgument(pushServiceInfo != null);

        final PushRequestHeaders pushRequestHeaders = PushRequestHeaders.getInstance(context);

        parameters = getPushParameters(deviceAlias, customUserId, tags, pushServiceInfo.areAnalyticsEnabled(), pushRequestHeaders.getRequestHeaders());

        registrationListener = listener;

        checkAnalytics();

        verifyRegistrationArguments(parameters);

        initiateRegistration();
    }

    private PushParameters getPushParameters(@Nullable String deviceAlias,
        @Nullable String customUserId,
        @Nullable Set<String> tags,
        boolean areAnalyticsEnabled,
        @Nullable Map<String, String> requestHeaders) {

        final String platformUuid = pushServiceInfo.getPlatformUuid();
        final String platformSecret = pushServiceInfo.getPlatformSecret();
        final String serviceUrl = pushServiceInfo.getServiceUrl();
        final Pivotal.SslCertValidationMode sslCertValidationMode = pushServiceInfo.getSslCertValidationMode();
        final List<String> pinnedCertificateNames = pushServiceInfo.getPinnedSslCertificateNames();
        final boolean areGeofencesEnabled = false;

        return new PushParameters(platformUuid, platformSecret, serviceUrl, "android-baidu", deviceAlias, customUserId, tags, areGeofencesEnabled, areAnalyticsEnabled, sslCertValidationMode, pinnedCertificateNames, requestHeaders);
    }

    private void checkAnalytics() {
        if (pushServiceInfo.areAnalyticsEnabled()) {

            AnalyticsEventService.setPushParameters(parameters);
            final Intent intent = AnalyticsEventService.getIntentToRunJob(context, null);
            context.startService(intent);

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
            final PrepareDatabaseJob job = new PrepareDatabaseJob(true);
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
     * Call this method to read the current PCF Push Notification Service device UUID.  You'll need device UUID if you want to target this
     * specific device with remote notification using the PCF Push Notification Service.  This method will return `null` if the device is
     * not currently registered with PCF Push.
     *
     * @return the current device UUID if the device is registered
     */
    public String getDeviceUuid() {
        final PushPreferencesBaidu PushPreferencesBaidu = new PushPreferencesBaidu(context);
        return PushPreferencesBaidu.getPCFPushDeviceRegistrationId();
    }

    private void verifyRegistrationArguments(@NonNull PushParameters parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("parameters may not be null");
        }
        if (parameters.getPlatformUuid() == null || parameters.getPlatformUuid().isEmpty()) {
            throw new IllegalArgumentException("parameters.platformUuid may not be null or empty");
        }
        if (parameters.getPlatformSecret() == null || parameters.getPlatformSecret().isEmpty()) {
            throw new IllegalArgumentException("parameters.platformSecret may not be null or empty");
        }
        if (parameters.getServiceUrl() == null || parameters.getServiceUrl().isEmpty()) {
            throw new IllegalArgumentException("parameters.serviceUrl may not be null or empty");
        }
        if (baiduAPIKey == null || baiduAPIKey.isEmpty()) {
            throw new IllegalArgumentException("Baidu api key may not be null or empty");
        }
    }

    private void initiateRegistration() {
        PushManager.startWork(context, PushConstants.LOGIN_TYPE_API_KEY, baiduAPIKey);
    }

    private void executeRegistration(final String channelId) {
        final PushParameters pushParameters = parameters;
        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    final RegistrationEngine registrationEngine = RegistrationEngine.getRegistrationEngine(context);

                    registrationEngine.registerDevice(pushParameters, channelId, registrationListener);
                } catch (Exception e) {
                    registrationListener.onRegistrationFailed(e.getMessage());
                    Logger.ex("Push SDK registration failed", e);
                }
            }
        };
        threadPool.execute(runnable);
    }

    public synchronized void onBaiduServiceBound(int errorCode, String channelId) {
        if (errorCode == PushConstants.ERROR_SUCCESS) {
            executeRegistration(channelId);
        } else {
            if (registrationListener != null) {
                registrationListener.onRegistrationFailed(String.format("Registration failed due to baidu error: %s", errorCode));
            }
        }
    }

    public synchronized void onBaiduServiceUnbound(int errorCode) {
        if (errorCode == PushConstants.ERROR_SUCCESS) {
            executeUnregistration();
        } else {
            unregistrationListener.onUnregistrationFailed(String.format("Unregistration failed due to a baidu error: %s", errorCode));
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

        final PushPreferencesBaidu pushPreferences = new PushPreferencesBaidu(context);
        final String deviceAlias = pushPreferences.getDeviceAlias();
        final String customUserId = pushPreferences.getCustomUserId();

        startRegistration(deviceAlias, customUserId, tags, new RegistrationListener() {
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
        checkArgument(pushServiceInfo != null);

        unregistrationListener = listener;

        verifyUnregistrationArguments(parameters);

        executeUnregistration();
    }

    private void executeUnregistration() {
        PushManager.stopWork(context);

        final PushPreferencesBaidu pushPreferences = new PushPreferencesBaidu(context);

        verifyUnregistrationArguments(this.parameters);

        final NetworkWrapper networkWrapper = new NetworkWrapperImpl();
        final PCFPushUnregisterDeviceApiRequest unregisterDeviceApiRequest = new PCFPushUnregisterDeviceApiRequestImpl(context, networkWrapper);
        final PCFPushUnregisterDeviceApiRequestProvider pcfPushUnregisterDeviceApiRequestProvider = new PCFPushUnregisterDeviceApiRequestProvider(unregisterDeviceApiRequest);

        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    final UnregistrationEngine unregistrationEngine = new UnregistrationEngine(
                        context,
                        pushPreferences,
                        pcfPushUnregisterDeviceApiRequestProvider);
                    unregistrationEngine.unregisterDevice(parameters, unregistrationListener);
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
        final PushRequestHeaders pushRequestHeaders = PushRequestHeaders.getInstance(context);
        pushRequestHeaders.setRequestHeaders(requestHeaders);
    }

    /**
     * Call this method to set or change the target Push platform information for network request.
     *
     * This method must be called *before* {@link #startRegistration}, {@link #subscribeToTags}, or any other methods that
     * will make a network request.
     *
     * @param pushServiceInfo A {@link PushServiceInfo} object containing the new/updated Push platform information.
     */
    public void setPushServiceInfo(@Nullable final PushServiceInfo pushServiceInfo) {
        this.pushServiceInfo = pushServiceInfo;
    }

    /**
     * Call this method to log an analytics event each time a notification has been opened.  You will need to pass the bundle containing the
     * receiptId of the event.
     *
     * @param bundle  a mapping of values for the event instance.
     */
    public void logOpenedNotification(Bundle bundle) {
        final ServiceStarter serviceStarter = new ServiceStarterImpl();
        final PushPreferencesBaidu preferences = new PushPreferencesBaidu(context);
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

    /**
     * @return the current version of the Pivotal CF Push Client SDK.
     */
    public static String getVersion() {
        return BuildConfig.VERSION_NAME;
    }

}
