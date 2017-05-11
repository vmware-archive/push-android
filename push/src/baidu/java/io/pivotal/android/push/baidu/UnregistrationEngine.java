/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.baidu;

import android.content.Context;
import io.pivotal.android.push.PushParameters;
import io.pivotal.android.push.backend.api.PCFPushUnregisterDeviceApiRequest;
import io.pivotal.android.push.backend.api.PCFPushUnregisterDeviceApiRequestProvider;
import io.pivotal.android.push.backend.api.PCFPushUnregisterDeviceListener;
import io.pivotal.android.push.prefs.PushPreferencesBaidu;
import io.pivotal.android.push.registration.UnregistrationListener;
import io.pivotal.android.push.util.Logger;

public class UnregistrationEngine {

    private Context context;
    private PushPreferencesBaidu pushPreferences;
    private PCFPushUnregisterDeviceApiRequestProvider PCFPushUnregisterDeviceApiRequestProvider;
    private String previousPCFPushDeviceRegistrationId;

    /**
     * Instantiate an instance of the UnregistrationEngine.
     *
     * All the parameters are required.  None may be null.
     * @param context  A context
     * @param pushPreferences  Some object that can provide persistent storage of push preferences.
     * @param pcfPushUnregisterDeviceApiRequestProvider  Some object that can provide PCFPushUnregisterDeviceApiRequest objects.
     */
    public UnregistrationEngine(Context context,
                                PushPreferencesBaidu pushPreferences,
                                PCFPushUnregisterDeviceApiRequestProvider pcfPushUnregisterDeviceApiRequestProvider) {

        verifyArguments(context,
                pushPreferences,
                pcfPushUnregisterDeviceApiRequestProvider);

        saveArguments(context,
                pushPreferences,
                pcfPushUnregisterDeviceApiRequestProvider);
    }

    private void verifyArguments(Context context,
                                 PushPreferencesBaidu pushPreferences,
                                 PCFPushUnregisterDeviceApiRequestProvider pcfPushUnregisterDeviceApiRequestProvider) {

        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
        if (pushPreferences == null) {
            throw new IllegalArgumentException("pushPreferences may not be null");
        }
        if (pcfPushUnregisterDeviceApiRequestProvider == null) {
            throw new IllegalArgumentException("pcfPushUnregisterDeviceApiRequestProvider may not be null");
        }
    }

    private void saveArguments(Context context,
                               PushPreferencesBaidu pushPreferences,
                               PCFPushUnregisterDeviceApiRequestProvider PCFPushUnregisterDeviceApiRequestProvider) {

        this.context = context;
        this.pushPreferences = pushPreferences;
        this.PCFPushUnregisterDeviceApiRequestProvider = PCFPushUnregisterDeviceApiRequestProvider;
        this.previousPCFPushDeviceRegistrationId = pushPreferences.getPCFPushDeviceRegistrationId();
    }

    public void unregisterDevice(PushParameters parameters, UnregistrationListener listener) {

        verifyUnregisterDeviceArguments(parameters);

        // Clear the saved package name so that the message receiver service won't be able to send
        // the application any more broadcasts
        pushPreferences.setPackageName(null);
        pushPreferences.setBaiduChannelId(null);

        unregisterDeviceWithPCFPush(previousPCFPushDeviceRegistrationId, parameters, listener);
    }

    private void verifyUnregisterDeviceArguments(PushParameters parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("parameters may not be null");
        }
        if (parameters.getServiceUrl() == null) {
            throw new IllegalArgumentException("parameters.serviceUrl may not be null");
        }
    }

    private void unregisterDeviceWithPCFPush(final String registrationId, PushParameters parameters,
        final UnregistrationListener listener) {
        if (registrationId != null) {
            Logger.i("Initiating device unregistration with PCF Push.");
            final PCFPushUnregisterDeviceApiRequest request = PCFPushUnregisterDeviceApiRequestProvider.getRequest();
            request.startUnregisterDevice(registrationId, parameters, getPCFPushUnregisterDeviceListener(listener));
        } else {
            Logger.i("Not currently registered with PCF Push.  Unregistration is not required.");
            if (listener != null) {
                listener.onUnregistrationComplete();
            }
        }
    }

    private PCFPushUnregisterDeviceListener getPCFPushUnregisterDeviceListener(final UnregistrationListener listener) {
        return new PCFPushUnregisterDeviceListener() {

            @Override
            public void onPCFPushUnregisterDeviceSuccess() {

                clearPCFPushRegistrationPreferences();

                if (listener != null) {
                    listener.onUnregistrationComplete();
                }
            }

            @Override
            public void onPCFPushUnregisterDeviceFailed(String reason) {

                if (listener != null) {
                    listener.onUnregistrationFailed(reason);
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
