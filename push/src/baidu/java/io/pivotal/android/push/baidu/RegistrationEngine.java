/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.baidu;

import android.content.Context;


import io.pivotal.android.push.prefs.PushPreferences;
import io.pivotal.android.push.prefs.PushPreferencesBaidu;
import java.util.Set;

import io.pivotal.android.push.PushParameters;
import io.pivotal.android.push.backend.api.PCFPushRegistrationApiRequest;
import io.pivotal.android.push.backend.api.PCFPushRegistrationApiRequestImpl;
import io.pivotal.android.push.backend.api.PCFPushRegistrationApiRequestProvider;
import io.pivotal.android.push.backend.api.PCFPushRegistrationListener;

import io.pivotal.android.push.prefs.PushRequestHeaders;
import io.pivotal.android.push.registration.RegistrationListener;
import io.pivotal.android.push.util.Logger;
import io.pivotal.android.push.util.NetworkWrapper;
import io.pivotal.android.push.util.NetworkWrapperImpl;
import io.pivotal.android.push.util.Util;

/**
 * This class is responsible for all the business logic behind device registration.  For a description
 * of its operation you can refer to the following diagrams:
 *
 *  1. Flow chart: https://docs.google.com/a/pivotallabs.com/drawings/d/1e1P4R5AOz486lMjhlrNO22gLyfADBPsBpYkYYKfbmYk
 *  2. Sequence diagram: https://docs.google.com/a/pivotallabs.com/drawings/d/1MoOFqKZvljEwu9M6ZNjZlEq3iOPBZKm2NMRw7TN6GKM
 *
 *  In general, though, the Registration Engine tries to do as little work as it thinks is required.
 *
 *  If the device is already successfully registered and all of the registration parameters are the same as the
 *  previous registration then the Registration Engine won't do anything.
 *
 *  On a fresh install, the Registration Engine will register with Baidu and then with the
 *  Pivotal CF Mobile Services server.
 *
 *  If any of the Pivotal CF Mobile Services registration parameters (platform_uuid, platform_secret, device_alias), or
 *  if Baidu provides a different channel ID than a previous install, then the Registration
 *  Engine will attempt to update its registration wih the Pivotal CF Mobile Services Push server (i.e.: HTTP PUT).
 *
 *  If, however, the base_server_url parameter is different than the existing registration, then the Registration
 *  Engine will abandon its registration with the previous server and make a new one (i.e.: HTTP POST) with the new
 *  server.
 *
 *  The Registration Engine is also designed to successfully complete previous registrations that have failed. For
 *  instance, if the previous registration attempt failed to complete the registration with PCF Push then it will
 *  try to re-register with the server if called again.
 */
public class RegistrationEngine {

    public static final int MAXIMUM_CUSTOM_USER_ID_LENGTH = 255;
    private Context context;
    private PushPreferencesBaidu pushPreferences;
    private PushRequestHeaders pushRequestHeaders;
    private PCFPushRegistrationApiRequestProvider pcfPushRegistrationApiRequestProvider;

    private String packageName;
    private String previousBaiduChannelId = null;
    private String previousPCFPushDeviceRegistrationId = null;
    private String previousPlatformUuid;
    private String previousPlatformSecret;
    private String previousDeviceAlias;
    private String previousCustomUserId;
    private String previousServiceUrl;
    private String baiduChannelId;

    public static RegistrationEngine getRegistrationEngine(Context context) {
        final PushPreferencesBaidu pushPreferences = new PushPreferencesBaidu(context);
        final PushRequestHeaders pushRequestHeaders = PushRequestHeaders.getInstance(context);
        final NetworkWrapper networkWrapper = new NetworkWrapperImpl();
        final PCFPushRegistrationApiRequest pushRegistrationApiRequest = new PCFPushRegistrationApiRequestImpl(context, networkWrapper);
        final PCFPushRegistrationApiRequestProvider PCFPushRegistrationApiRequestProvider = new PCFPushRegistrationApiRequestProvider(pushRegistrationApiRequest);


        return new RegistrationEngine(context,
                context.getPackageName(),
                pushPreferences,
                pushRequestHeaders,
                PCFPushRegistrationApiRequestProvider
                );
    }

    /**
     * Instantiate an instance of the RegistrationEngine.
     *
     * All the parameters are required.  None may be null.
     * @param context  A context
     * @param packageName  The currenly application package name.
     * @param pushPreferences  Some object that can provide persistent storage for push preferences.
     * @param pushRequestHeaders Some object that can provide storage for push request headers
     * @param pcfPushRegistrationApiRequestProvider  Some object that can provide PCFPushRegistrationApiRequest objects.
     */
    public RegistrationEngine(Context context,
                              String packageName,
                              PushPreferencesBaidu pushPreferences,
                              PushRequestHeaders pushRequestHeaders,
                              PCFPushRegistrationApiRequestProvider pcfPushRegistrationApiRequestProvider) {

        verifyArguments(context,
                packageName,
                pushPreferences,
                pushRequestHeaders,
                pcfPushRegistrationApiRequestProvider);

        saveArguments(context,
                packageName,
                pushPreferences,
                pushRequestHeaders,
                pcfPushRegistrationApiRequestProvider);
    }

    private void verifyArguments(Context context,
                                 String packageName,
                                 PushPreferences pushPreferences,
                                 PushRequestHeaders pushRequestHeaders,
                                 PCFPushRegistrationApiRequestProvider pcfPushRegistrationApiRequestProvider) {

        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
        if (packageName == null) {
            throw new IllegalArgumentException("packageName may not be null");
        }
        if (pushPreferences == null) {
            throw new IllegalArgumentException("pushPreferences may not be null");
        }
        if (pushRequestHeaders == null) {
            throw new IllegalArgumentException("pushRequestHeaders may not be null");
        }
        if (pcfPushRegistrationApiRequestProvider == null) {
            throw new IllegalArgumentException("pcfPushRegistrationApiRequestProvider may not be null");
        }
    }

    private void saveArguments(Context context,
                               String packageName,
                               PushPreferencesBaidu pushPreferences,
                               PushRequestHeaders pushRequestHeaders,
                               PCFPushRegistrationApiRequestProvider pcfPushRegistrationApiRequestProvider) {

        this.context = context;
        this.packageName = packageName;
        this.pushPreferences = pushPreferences;
        this.pushRequestHeaders = pushRequestHeaders;
        this.pcfPushRegistrationApiRequestProvider = pcfPushRegistrationApiRequestProvider;
        this.previousBaiduChannelId = pushPreferences.getBaiduChannelId();
        this.previousPCFPushDeviceRegistrationId = pushPreferences.getPCFPushDeviceRegistrationId();
        this.previousPlatformUuid = pushPreferences.getPlatformUuid();
        this.previousPlatformSecret = pushPreferences.getPlatformSecret();
        this.previousDeviceAlias = pushPreferences.getDeviceAlias();
        this.previousCustomUserId = pushPreferences.getCustomUserId();
        this.previousServiceUrl = pushPreferences.getServiceUrl();
    }

    /**
     * Start a registration attempt.  This method is asynchronous and will return before registration is complete.
     * If you need to know when registration completes (successfully or not), then provide a listener.
     *
     * This class is NOT reentrant.  Do not try to call registerDevice again while some registration is
     * already in progress.  It is best to create a new RegistrationEngine object entirely if you need to
     * register again (though I don't know why you would want to register more than ONCE during the lifetime
     * of a process - unless registration fails and you want to retry).
     *
     *  @param parameters  The registration parameters.  May not be null.
     * @param channelId
     * @param listener  An optional listener if you care to know when registration completes or fails.
     */
    public void registerDevice(PushParameters parameters, String channelId, final RegistrationListener listener) {
        verifyRegistrationArguments(parameters);

        // Save the given package name so that the message receiver service can see it
        pushPreferences.setPackageName(packageName);
        pushPreferences.setSslCertValidationMode(parameters.getSslCertValidationMode());
        pushPreferences.setPinnedCertificateNames(parameters.getPinnedSslCertificateNames());

        if (channelId == null) {
            Logger.e("Baidu baiduChannelId is not available. Registration failed.");
            if (listener != null) {
                listener.onRegistrationFailed("Baidu baiduChannelId not available.");
            }
            return;
        }

        this.baiduChannelId = channelId;
        final boolean isBaiduChannelIdUpdated = isBaiduChannelIdUpdated(channelId);

        final boolean isServiceUrlUpdated = isServiceUrlUpdated(parameters);
        if (isServiceUrlUpdated) {
            Logger.v("The PCF Push serviceUrl has been updated. A new registration with the PCF Push server is required.");
        }

        final boolean isPlatformUpdated = isPlatformUpdated(parameters);
        if (isPlatformUpdated) {
            Logger.v("The PCF Push platform has been updated. A new registration with the PCF Push server is required.");
        }

        if (isPCFPushUpdateRegistrationRequired(channelId, parameters) && !isServiceUrlUpdated && !isPlatformUpdated) {
            registerUpdateDeviceWithPCFPush(channelId, previousPCFPushDeviceRegistrationId, pushPreferences.getTags(), parameters, listener);

        } else if (isBaiduChannelIdUpdated || isServiceUrlUpdated || isPlatformUpdated) {
            registerNewDeviceWithPCFPush(channelId, pushPreferences.getTags(), parameters, listener);

        } else {
            Logger.v("Already registered");
            if (listener != null) {
                listener.onRegistrationComplete();
            }
        }
    }

    private void verifyRegistrationArguments(PushParameters parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("parameters may not be null");
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
        if (parameters.getCustomUserId() != null && parameters.getCustomUserId().length() > MAXIMUM_CUSTOM_USER_ID_LENGTH) {
            throw new IllegalArgumentException("customUserId must be fewer than or equal to "+ MAXIMUM_CUSTOM_USER_ID_LENGTH + " characters");
        }
    }

    private boolean isPreviousBaiduChannelIdEmpty() {
        return previousBaiduChannelId == null || previousBaiduChannelId.isEmpty();
    }

    private boolean haveTagsBeenUpdated(PushParameters parameters) {
        final Set<String> savedTags = Util.lowercaseTags(pushPreferences.getTags());
        final Set<String> requestedTags = parameters.getTags();

        if (isNullOrEmpty(savedTags) && isNullOrEmpty(requestedTags)) {
            return false;
        } else if (isNullOrEmpty(savedTags) && !isNullOrEmpty(requestedTags)) {
            return true;
        } else if (!isNullOrEmpty(savedTags) && isNullOrEmpty(requestedTags)) {
            return true;
        } else {
            return !requestedTags.equals(savedTags);
        }
    }

    private boolean isNullOrEmpty(Set<String> s) {
        return (s == null || s.isEmpty());
    }

    private boolean isPCFPushUpdateRegistrationRequired(String newBaiduChannelId, PushParameters parameters) {
        final boolean isBaiduChannelIdDifferent = !isPreviousBaiduChannelIdEmpty() && !previousBaiduChannelId.equals(newBaiduChannelId);
        final boolean isPreviousPCFPushDeviceRegistrationIdEmpty = previousPCFPushDeviceRegistrationId == null || previousPCFPushDeviceRegistrationId.isEmpty();
        if (isPreviousPCFPushDeviceRegistrationIdEmpty) {
            Logger.v("previousPCFPushDeviceRegistrationId is empty. Device will NOT require an update-registration with PCF Push.");
            return false;
        }
        if (isBaiduChannelIdDifferent) {
            Logger.v("The Baidu baiduChannelId is different. Device will need to update its registration with PCF Push.");
            return true;
        }
        if (areRegistrationParametersUpdated(parameters)) {
            Logger.v("The registration parameters have been updated. Device will need to update its registration with PCF Push.");
            return true;
        }
        if (haveTagsBeenUpdated(parameters)) {
            Logger.v("App tags changed. Device will need to update its registration with PCF Push.");
            return true;
        }
        Logger.v("It does not seem that the device needs to update its registration with PCF Push.");
        return false;
    }

    private boolean areRegistrationParametersUpdated(PushParameters parameters) {
        final boolean isPreviousDeviceAliasEmpty = previousDeviceAlias == null || previousDeviceAlias.isEmpty();
        final boolean isNewDeviceAliasEmpty = parameters.getDeviceAlias() == null || parameters.getDeviceAlias().isEmpty();
        final boolean isPreviousCustomUserIdEmpty = previousCustomUserId == null || previousCustomUserId.isEmpty();
        final boolean isNewCustomUserIdEmpty = parameters.getCustomUserId() == null || parameters.getCustomUserId().isEmpty();
        final boolean isDeviceAliasUpdated = (isPreviousDeviceAliasEmpty && !isNewDeviceAliasEmpty) || (!isPreviousDeviceAliasEmpty && isNewDeviceAliasEmpty) || (!isNewDeviceAliasEmpty && !parameters.getDeviceAlias().equals(previousDeviceAlias));
        final boolean isCustomUserIdUpdated = (isPreviousCustomUserIdEmpty && !isNewCustomUserIdEmpty) || (!isPreviousCustomUserIdEmpty && isNewCustomUserIdEmpty) || (!isNewCustomUserIdEmpty && !parameters.getCustomUserId().equals(previousCustomUserId));
        return isDeviceAliasUpdated || isCustomUserIdUpdated;
    }

    private boolean isPlatformUpdated(PushParameters parameters) {
        final boolean isPreviousPlatformUuidEmpty = previousPlatformUuid == null || previousPlatformUuid.isEmpty();
        final boolean isPlatformUuidUpdated = (isPreviousPlatformUuidEmpty && !parameters.getPlatformUuid().isEmpty()) || !parameters.getPlatformUuid().equals(previousPlatformUuid);
        final boolean isPreviousPlatformSecretEmpty = previousPlatformSecret == null || previousPlatformSecret.isEmpty();
        final boolean isPlatformSecretUpdated = (isPreviousPlatformSecretEmpty && !parameters.getPlatformSecret().isEmpty()) || !parameters.getPlatformSecret().equals(previousPlatformSecret);
        return isPlatformSecretUpdated || isPlatformUuidUpdated;
    }

    private boolean isServiceUrlUpdated(PushParameters parameters) {
        final boolean isPreviousServiceUrlEmpty = previousServiceUrl == null;
        final boolean isServiceUrlUpdated = (isPreviousServiceUrlEmpty && parameters.getServiceUrl() != null) || !parameters.getServiceUrl().equals(previousServiceUrl);
        return isServiceUrlUpdated;
    }

    private boolean isBaiduChannelIdUpdated(String channelId) {
        return !channelId.equals(pushPreferences.getBaiduChannelId());
    }

    private void registerUpdateDeviceWithPCFPush(String baiduChannelId,
                                                 String pcfPushDeviceRegistrationId,
                                                 Set<String> savedTags,
                                                 PushParameters parameters,
                                                 RegistrationListener listener) {

        Logger.i("Initiating update device registration with PCF Push.");
        final PCFPushRegistrationApiRequest PCFPushRegistrationApiRequest = pcfPushRegistrationApiRequestProvider.getRequest();
        PCFPushRegistrationApiRequest.startUpdateDeviceRegistration(baiduChannelId,
                pcfPushDeviceRegistrationId,
                savedTags,
                parameters,
                getPCFPushUpdateRegistrationListener(parameters, listener));
    }

    private PCFPushRegistrationListener getPCFPushUpdateRegistrationListener(final PushParameters parameters, final RegistrationListener listener) {
        return new PCFPushRegistrationListener() {

            @Override
            public void onPCFPushRegistrationSuccess(String pcfPushDeviceRegistrationId) {

                if (pcfPushDeviceRegistrationId == null) {
                    Logger.e("PCF Push server return null pcfPushDeviceRegistrationId upon registration update.");

                    // The server didn't return a valid registration response.  We should clear our local
                    // registration data so that we can attempt to reregister next time.
                    clearPCFPushRegistrationPreferences();

                    if (listener != null) {
                        listener.onRegistrationFailed("PCF Push server return null pcfPushDeviceRegistrationId upon registration update.");
                    }
                    return;
                }
                pushPreferences.setBaiduChannelId(baiduChannelId);

                Logger.i("Saving PCF Push device registration ID: " + pcfPushDeviceRegistrationId);
                pushPreferences.setPCFPushDeviceRegistrationId(pcfPushDeviceRegistrationId);

                Logger.v("Saving updated platformUuid, platformSecret, deviceAlias, and serviceUrl");
                pushPreferences.setPlatformUuid(parameters.getPlatformUuid());
                pushPreferences.setPlatformSecret(parameters.getPlatformSecret());
                pushPreferences.setDeviceAlias(parameters.getDeviceAlias());
                pushPreferences.setCustomUserId(parameters.getCustomUserId());
                pushPreferences.setServiceUrl(parameters.getServiceUrl());
                pushPreferences.setTags(parameters.getTags());

                Logger.v("Saving tags: " + parameters.getTags());

                if (listener != null) {
                    listener.onRegistrationComplete();
                }
            }

            @Override
            public void onPCFPushRegistrationFailed(String reason) {
                clearPCFPushRegistrationPreferences();

                if (listener != null) {
                    listener.onRegistrationFailed(reason);
                }
            }
        };
    }

    private void registerNewDeviceWithPCFPush(final String baiduChannelId,
                                              Set<String> savedTags,
                                              PushParameters parameters,
                                              RegistrationListener listener) {

        Logger.i("Initiating new device registration with PCF Push.");
        final PCFPushRegistrationApiRequest PCFPushRegistrationApiRequest = pcfPushRegistrationApiRequestProvider.getRequest();
        PCFPushRegistrationApiRequest.startNewDeviceRegistration(baiduChannelId, savedTags, parameters, getPCFPushNewRegistrationListener(parameters, listener));
    }

    private PCFPushRegistrationListener getPCFPushNewRegistrationListener(final PushParameters parameters, final RegistrationListener listener) {
        return new PCFPushRegistrationListener() {

            @Override
            public void onPCFPushRegistrationSuccess(String pcfPushDeviceRegistrationId) {

                if (pcfPushDeviceRegistrationId == null) {

                    Logger.e("PCF Push returned null pcfPushDeviceRegistrationId");

                    // The server didn't return a valid registration response.  We should clear our local
                    // registration data so that we can attempt to reregister next time.
                    clearPCFPushRegistrationPreferences();

                    if (listener != null) {
                        listener.onRegistrationFailed("PCF Push returned null pcfPushDeviceRegistrationId");
                    }
                    return;
                }
                pushPreferences.setBaiduChannelId(baiduChannelId);

                Logger.i("Saving PCF Push device registration ID: " + pcfPushDeviceRegistrationId);
                pushPreferences.setPCFPushDeviceRegistrationId(pcfPushDeviceRegistrationId);

                Logger.v("Saving updated platformUuid, platformSecret, deviceAlias, and serviceUrl");
                pushPreferences.setPlatformUuid(parameters.getPlatformUuid());
                pushPreferences.setPlatformSecret(parameters.getPlatformSecret());
                pushPreferences.setDeviceAlias(parameters.getDeviceAlias());
                pushPreferences.setCustomUserId(parameters.getCustomUserId());
                pushPreferences.setServiceUrl(parameters.getServiceUrl());
                pushPreferences.setTags(parameters.getTags());
                Logger.v("Saving tags: " + parameters.getTags());

                if (listener != null) {
                    listener.onRegistrationComplete();
                }

            }

            @Override
            public void onPCFPushRegistrationFailed(String reason) {
                clearPCFPushRegistrationPreferences();

                if (listener != null) {
                    listener.onRegistrationFailed(reason);
                }
            }
        };
    }

    private void clearPCFPushRegistrationPreferences() {
        pushPreferences.setPCFPushDeviceRegistrationId(null);
        pushPreferences.setPlatformUuid(null);
        pushPreferences.setPlatformSecret(null);
        pushPreferences.setDeviceAlias(null);
        pushPreferences.setCustomUserId(null);
        pushPreferences.setServiceUrl(null);
        pushPreferences.setTags(null);
    }
}
