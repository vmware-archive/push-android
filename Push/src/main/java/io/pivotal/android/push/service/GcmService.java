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

package io.pivotal.android.push.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import io.pivotal.android.analytics.jobs.EnqueueEventJob;
import io.pivotal.android.analytics.model.events.Event;
import io.pivotal.android.analytics.service.EventService;
import io.pivotal.android.common.prefs.AnalyticsPreferencesProvider;
import io.pivotal.android.common.prefs.AnalyticsPreferencesProviderImpl;
import io.pivotal.android.common.util.Logger;
import io.pivotal.android.common.util.ServiceStarter;
import io.pivotal.android.common.util.ServiceStarterImpl;
import io.pivotal.android.push.model.events.EventPushReceived;
import io.pivotal.android.push.prefs.PushPreferencesProvider;
import io.pivotal.android.push.prefs.PushPreferencesProviderImpl;
import io.pivotal.android.push.receiver.GcmBroadcastReceiver;

public class GcmService extends IntentService {
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_MESSAGE_UUID = "msg_uuid";

    private ServiceStarter serviceStarter;
    private PushPreferencesProvider pushPreferencesProvider;
    private AnalyticsPreferencesProvider analyticsPreferencesProvider;

    public GcmService() {
        super("GcmService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        serviceStarter = onCreateServiceStarter();
        pushPreferencesProvider = onCreatePushPreferencesProvider();
        analyticsPreferencesProvider = onCreateAnalyticsPreferencesProvider();
    }

    /* package */ ServiceStarter onCreateServiceStarter() {
        return new ServiceStarterImpl();
    }

    /* package */ PushPreferencesProvider onCreatePushPreferencesProvider() {
        return new PushPreferencesProviderImpl(this);
    }

    /* package */ AnalyticsPreferencesProvider onCreateAnalyticsPreferencesProvider() {
        return new AnalyticsPreferencesProviderImpl(this);
    }

    /* package */ ServiceStarter getServiceStarter() {
        return serviceStarter;
    }

    /* package */ PushPreferencesProvider getPushPreferencesProvider() {
        return pushPreferencesProvider;
    }

    /* package */ AnalyticsPreferencesProvider getAnalyticsPreferencesProvider() {
        return analyticsPreferencesProvider;
    }

    @Override
    protected final void onHandleIntent(Intent intent) {
        Logger.fd("GcmService has received a push message from GCM.");

        if (intent == null) {
            return;
        }

        onReceive(intent);

        if (isAnalyticsEnabled()) {
            enqueueMessageReceivedEvent(intent);
        }

        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    public void onReceive(Intent intent) {
        final GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        final String messageType = gcm.getMessageType(intent);
        final Bundle extras = intent.getExtras();

        if (extras != null && !extras.isEmpty()) {
            handleMessage(extras, messageType);
        }
    }

    private void handleMessage(Bundle extras, String messageType) {
        if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
            onReceiveMessage(extras);

        } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
            onReceiveMessageDeleted(extras);

        } else if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
            onReceiveMessageSendError(extras);
        }
    }

    public void onReceiveMessage(final Bundle payload) {}

    public void onReceiveMessageDeleted(final Bundle payload) {}

    public void onReceiveMessageSendError(final Bundle payload) {}

    private boolean isAnalyticsEnabled() {
        return analyticsPreferencesProvider.isAnalyticsEnabled();
    }

    private void enqueueMessageReceivedEvent(Intent intent) {
        final Event event = getMessageReceivedEvent(intent);
        final EnqueueEventJob enqueueEventJob = new EnqueueEventJob(event);
        final Intent enqueueEventJobIntent = EventService.getIntentToRunJob(this, enqueueEventJob);
        if (serviceStarter.startService(this, enqueueEventJobIntent) == null) {
            Logger.e("ERROR: could not start service '" + enqueueEventJobIntent + ". A 'message received' event for this message will not be sent.");
        }
    }

    private Event getMessageReceivedEvent(Intent intent) {
        final String messageUuid = intent.getStringExtra(KEY_MESSAGE_UUID);
        final String variantUuid = pushPreferencesProvider.getVariantUuid();
        final String deviceId = pushPreferencesProvider.getBackEndDeviceRegistrationId();
        final Event event = EventPushReceived.getEvent(messageUuid, variantUuid, deviceId);
        return event;
    }
}
