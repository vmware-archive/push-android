/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.registration;

import android.content.Context;

import io.pivotal.android.push.RegistrationParameters;
import io.pivotal.android.push.backend.BackEndUnregisterDeviceApiRequest;
import io.pivotal.android.push.backend.BackEndUnregisterDeviceApiRequestProvider;
import io.pivotal.android.push.backend.BackEndUnregisterDeviceListener;
import io.pivotal.android.push.gcm.GcmProvider;
import io.pivotal.android.push.gcm.GcmUnregistrationApiRequest;
import io.pivotal.android.push.gcm.GcmUnregistrationApiRequestProvider;
import io.pivotal.android.push.gcm.GcmUnregistrationListener;
import io.pivotal.android.push.prefs.PushPreferencesProvider;
import io.pivotal.android.push.util.Logger;
import io.pivotal.android.push.util.ServiceStarter;

public class UnregistrationEngine {

    private Context context;
    private GcmProvider gcmProvider;
    private ServiceStarter serviceStarter;
    private PushPreferencesProvider pushPreferencesProvider;
    private GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider;
    private BackEndUnregisterDeviceApiRequestProvider backEndUnregisterDeviceApiRequestProvider;
    private String previousBackEndDeviceRegistrationId;
    private String previousVariantUuid;

    /**
     * Instantiate an instance of the UnregistrationEngine.
     *
     * All the parameters are required.  None may be null.
     * @param context  A context
     * @param gcmProvider  Some object that can provide the GCM services.
     * @param serviceStarter  Some object that can be used to start services.
     * @param pushPreferencesProvider  Some object that can provide persistent storage of push preferences.
     * @param gcmUnregistrationApiRequestProvider  Some object that can provide GCMUnregistrationApiRequest objects.
     * @param backEndUnregisterDeviceApiRequestProvider  Some object that can provide BackEndUnregisterDeviceApiRequest objects.
     */
    public UnregistrationEngine(Context context,
                                GcmProvider gcmProvider,
                                ServiceStarter serviceStarter,
                                PushPreferencesProvider pushPreferencesProvider,
                                GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider,
                                BackEndUnregisterDeviceApiRequestProvider backEndUnregisterDeviceApiRequestProvider) {

        verifyArguments(context,
                gcmProvider,
                serviceStarter,
                pushPreferencesProvider,
                gcmUnregistrationApiRequestProvider,
                backEndUnregisterDeviceApiRequestProvider
        );

        saveArguments(context,
                gcmProvider,
                serviceStarter,
                pushPreferencesProvider,
                gcmUnregistrationApiRequestProvider,
                backEndUnregisterDeviceApiRequestProvider
        );
    }

    private void verifyArguments(Context context,
                                 GcmProvider gcmProvider,
                                 ServiceStarter serviceStarter,
                                 PushPreferencesProvider pushPreferencesProvider,
                                 GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider,
                                 BackEndUnregisterDeviceApiRequestProvider backEndUnregisterDeviceApiRequestProvider) {

        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
        if (gcmProvider == null) {
            throw new IllegalArgumentException("gcmProvider may not be null");
        }
        if (serviceStarter == null) {
            throw new IllegalArgumentException("serviceStarter may not be null");
        }
        if (pushPreferencesProvider == null) {
            throw new IllegalArgumentException("pushPreferencesProvider may not be null");
        }
        if (gcmUnregistrationApiRequestProvider == null) {
            throw new IllegalArgumentException("gcmUnregistrationApiRequestProvider may not be null");
        }
        if (backEndUnregisterDeviceApiRequestProvider == null) {
            throw new IllegalArgumentException("backEndUnregisterDeviceApiRequestProvider may not be null");
        }
    }

    private void saveArguments(Context context,
                               GcmProvider gcmProvider,
                               ServiceStarter serviceStarter,
                               PushPreferencesProvider pushPreferencesProvider,
                               GcmUnregistrationApiRequestProvider gcmUnregistrationApiRequestProvider,
                               BackEndUnregisterDeviceApiRequestProvider backEndUnregisterDeviceApiRequestProvider) {

        this.context = context;
        this.gcmProvider = gcmProvider;
        this.serviceStarter = serviceStarter;
        this.pushPreferencesProvider = pushPreferencesProvider;
        this.gcmUnregistrationApiRequestProvider = gcmUnregistrationApiRequestProvider;
        this.backEndUnregisterDeviceApiRequestProvider = backEndUnregisterDeviceApiRequestProvider;
        this.previousBackEndDeviceRegistrationId = pushPreferencesProvider.getBackEndDeviceRegistrationId();
        this.previousVariantUuid = pushPreferencesProvider.getVariantUuid();
    }

    public void unregisterDevice(RegistrationParameters parameters, UnregistrationListener listener) {

        verifyUnregisterDeviceArguments(parameters);

        // Clear the saved package name so that the message receiver service won't be able to send
        // the application any more broadcasts
        pushPreferencesProvider.setPackageName(null);

        if (gcmProvider.isGooglePlayServicesInstalled(context)) {
            unregisterDeviceWithGcm(parameters, listener);
        } else {
            if (listener != null) {
                listener.onUnregistrationFailed("Google Play Services is not available");
            }
        }
    }

    private void verifyUnregisterDeviceArguments(RegistrationParameters parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("parameters may not be null");
        }
        if (parameters.getBaseServerUrl() == null) {
            throw new IllegalArgumentException("parameters.baseServerUrl may not be null");
        }
    }

    private void unregisterDeviceWithGcm(RegistrationParameters parameters, final UnregistrationListener listener) {
        Logger.i("Unregistering sender ID with GCM.");
        final GcmUnregistrationApiRequest gcmUnregistrationApiRequest = gcmUnregistrationApiRequestProvider.getRequest();
        gcmUnregistrationApiRequest.startUnregistration(getGcmUnregistrationListener(parameters, listener));
    }

    private GcmUnregistrationListener getGcmUnregistrationListener(final RegistrationParameters parameters, final UnregistrationListener listener) {

        return new GcmUnregistrationListener() {
            @Override
            public void onGcmUnregistrationComplete() {
                clearGcmRegistrationPreferences();
                unregisterDeviceWithBackEnd(previousBackEndDeviceRegistrationId, parameters, listener);
            }

            @Override
            public void onGcmUnregistrationFailed(String reason) {
                // Even if we couldn't unregister from GCM we need to continue and unregister the device from the back-end
                unregisterDeviceWithBackEnd(previousBackEndDeviceRegistrationId, parameters, listener);
            }
        };
    }

    private void clearGcmRegistrationPreferences() {
        pushPreferencesProvider.setGcmDeviceRegistrationId(null);
        pushPreferencesProvider.setGcmSenderId(null);
        pushPreferencesProvider.setAppVersion(-1);
    }

    private void unregisterDeviceWithBackEnd(final String backEndDeviceRegistrationId, RegistrationParameters parameters, final UnregistrationListener listener) {
        if (backEndDeviceRegistrationId == null) {
            Logger.i("Not currently registered with the back-end.  Unregistration is not required.");
            listener.onUnregistrationComplete();
        } else {
            Logger.i("Initiating device unregistration with the back-end.");
            final BackEndUnregisterDeviceApiRequest backEndUnregisterDeviceApiRequest = backEndUnregisterDeviceApiRequestProvider.getRequest();
            backEndUnregisterDeviceApiRequest.startUnregisterDevice(backEndDeviceRegistrationId, parameters, getBackEndUnregisterDeviceListener(listener));
        }
    }

    private BackEndUnregisterDeviceListener getBackEndUnregisterDeviceListener(final UnregistrationListener listener) {
        return new BackEndUnregisterDeviceListener() {

            @Override
            public void onBackEndUnregisterDeviceSuccess() {
                logPushUnregisteredEvent(previousVariantUuid, previousBackEndDeviceRegistrationId);
                clearBackEndRegistrationPreferences();
                if (listener != null) {
                    listener.onUnregistrationComplete();
                }
            }

            @Override
            public void onBackEndUnregisterDeviceFailed(String reason) {
                if (listener != null) {
                    listener.onUnregistrationFailed(reason);
                }
            }
        };
    }

    private void clearBackEndRegistrationPreferences() {
        pushPreferencesProvider.setBackEndDeviceRegistrationId(null);
        pushPreferencesProvider.setVariantUuid(null);
        pushPreferencesProvider.setVariantSecret(null);
        pushPreferencesProvider.setDeviceAlias(null);
        pushPreferencesProvider.setBaseServerUrl(null);
        pushPreferencesProvider.setTags(null);
    }

    private void logPushUnregisteredEvent(String variantUuid, String deviceId) {
        // add analytics lib
    }
}
