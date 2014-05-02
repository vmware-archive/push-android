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

package com.pivotal.cf.mobile.pushsdk.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import com.pivotal.cf.mobile.analyticssdk.jobs.EnqueueEventJob;
import com.pivotal.cf.mobile.analyticssdk.model.events.Event;
import com.pivotal.cf.mobile.analyticssdk.service.EventService;
import com.pivotal.cf.mobile.common.prefs.PreferencesProvider;
import com.pivotal.cf.mobile.common.prefs.PreferencesProviderImpl;
import com.pivotal.cf.mobile.common.util.Logger;
import com.pivotal.cf.mobile.pushsdk.broadcastreceiver.GcmBroadcastReceiver;
import com.pivotal.cf.mobile.pushsdk.model.events.EventPushReceived;
import com.pivotal.cf.mobile.pushsdk.util.ServiceStarter;
import com.pivotal.cf.mobile.pushsdk.util.ServiceStarterImpl;

import java.util.concurrent.Semaphore;

public class GcmService extends IntentService {

    public static final String BROADCAST_NAME_SUFFIX = ".pcfmspushsdk.RECEIVE_PUSH";
    public static final String KEY_RESULT_RECEIVER = "result_receiver";
    public static final String KEY_GCM_INTENT = "gcm_intent";
    public static final String KEY_MESSAGE_UUID = "msg_uuid";

    public static final int NO_RESULT = -1;
    public static final int RESULT_EMPTY_INTENT = 100;
    public static final int RESULT_EMPTY_PACKAGE_NAME = 101;
    public static final int RESULT_NOTIFIED_APPLICATION = 102;

    // Used by unit tests
    /* package */ static Semaphore semaphore = null;
    /* package */ static PreferencesProvider preferencesProvider = null;
    /* package */ static ServiceStarter serviceStarter = null;

    private ResultReceiver resultReceiver = null;

    public GcmService() {
        super("GcmService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        try {
            setupLogger();
            setupStatics();
            doHandleIntent(intent);

        } finally {

            cleanupStatics();

            // If unit tests are running then release them so that they can continue
            if (GcmService.semaphore != null) {
                GcmService.semaphore.release();
            }

            // Release the wake lock provided by the WakefulBroadcastReceiver.
            if (intent != null) {
                GcmBroadcastReceiver.completeWakefulIntent(intent);
            }
        }
    }

    private void setupLogger() {
        if (!Logger.isSetup()) {
            Logger.setup(this);
        }
    }

    private void setupStatics() {
        if (GcmService.preferencesProvider == null) {
            GcmService.preferencesProvider = new PreferencesProviderImpl(this);
        }
        if (GcmService.serviceStarter == null) {
            GcmService.serviceStarter = new ServiceStarterImpl();
        }
    }

    private void cleanupStatics() {
        GcmService.preferencesProvider = null;
        GcmService.serviceStarter = null;
    }

    private void doHandleIntent(Intent intent) {

        Logger.fd("GcmService: Package '%s' has received a push message from GCM.", getPackageName());

        if (intent == null) {
            return;
        }

        getResultReceiver(intent);

        if (isBundleEmpty(intent)) {
            sendResult(RESULT_EMPTY_INTENT);
        } else {

            if (getBroadcastName() == null) {
                sendResult(RESULT_EMPTY_PACKAGE_NAME);
                return;
            }

            enqueueMessageReceivedEvent(intent);
            notifyApplication(intent);
        }
    }

    private void enqueueMessageReceivedEvent(Intent intent) {
        final Event event = getMessageReceivedEvent(intent);
        final EnqueueEventJob enqueueEventJob = new EnqueueEventJob(event);
        final Intent enqueueEventJobIntent = EventService.getIntentToRunJob(this, enqueueEventJob);
        if (GcmService.serviceStarter.startService(this, enqueueEventJobIntent) == null) {
            Logger.e("ERROR: could not start service '" + enqueueEventJobIntent + ". A 'message received' event for this message will not be sent.");
        }
    }

    private Event getMessageReceivedEvent(Intent intent) {
        final String messageUuid = intent.getStringExtra(KEY_MESSAGE_UUID);
        final String variantUuid = GcmService.preferencesProvider.getVariantUuid();
        final String deviceId = GcmService.preferencesProvider.getBackEndDeviceRegistrationId();
        final Event event = EventPushReceived.getEvent(variantUuid, messageUuid, deviceId);
        return event;
    }

    private boolean isBundleEmpty(Intent intent) {
        final Bundle extras = intent.getExtras();
        return (extras == null || extras.size() <= 0);
    }

    private void notifyApplication(Intent gcmIntent) {
        final String broadcastName = getBroadcastName();
        if (broadcastName != null) {
            final Intent intent = new Intent(broadcastName);
            intent.putExtra(KEY_GCM_INTENT, gcmIntent);
            sendBroadcast(intent);
            sendResult(RESULT_NOTIFIED_APPLICATION);
        } else {
            sendResult(RESULT_EMPTY_PACKAGE_NAME);
        }
    }

    private String getBroadcastName() {
        final String packageName = preferencesProvider.getPackageName();
        if (packageName == null) {
            return null;
        } else {
            final String broadcastName = packageName + BROADCAST_NAME_SUFFIX;
            return broadcastName;
        }
    }

    private void getResultReceiver(Intent intent) {
        if (intent.hasExtra(KEY_RESULT_RECEIVER)) {
            // Used by unit tests
            resultReceiver = intent.getParcelableExtra(KEY_RESULT_RECEIVER);
            intent.removeExtra(KEY_RESULT_RECEIVER);
        }
    }

    private void sendResult(int resultCode) {
        if (resultReceiver != null) {
            // Used by unit tests
            resultReceiver.send(resultCode, null);
        }
    }
}
