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

package org.omnia.pushsdk.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import org.omnia.pushsdk.broadcastreceiver.GcmBroadcastReceiver;
import org.omnia.pushsdk.broadcastreceiver.MessageReceiptAlarmProvider;
import org.omnia.pushsdk.broadcastreceiver.MessageReceiptAlarmProviderImpl;
import org.omnia.pushsdk.prefs.PreferencesProviderImpl;
import org.omnia.pushsdk.model.MessageReceiptData;
import org.omnia.pushsdk.prefs.MessageReceiptsProvider;
import org.omnia.pushsdk.prefs.PreferencesProvider;
import org.omnia.pushsdk.prefs.MessageReceiptsProviderImpl;
import org.omnia.pushsdk.util.PushLibLogger;

import java.util.Date;
import java.util.concurrent.Semaphore;

public class GcmService extends IntentService {

    public static final String BROADCAST_NAME_SUFFIX = ".omniapushsdk.RECEIVE_PUSH";
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
    /* package */ static MessageReceiptsProvider messageReceiptsProvider = null;
    /* package */ static MessageReceiptAlarmProvider messageReceiptAlarmProvider = null;

    private ResultReceiver resultReceiver = null;

    // TODO - write unit tests to cover this class

    public GcmService() {
        super("GcmService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        try {

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

    private void setupStatics() {
        if (GcmService.preferencesProvider == null) {
            GcmService.preferencesProvider = new PreferencesProviderImpl(this);
        }
        if (GcmService.messageReceiptsProvider == null) {
            GcmService.messageReceiptsProvider = new MessageReceiptsProviderImpl(this);
        }
        if (GcmService.messageReceiptAlarmProvider == null) {
            GcmService.messageReceiptAlarmProvider = new MessageReceiptAlarmProviderImpl(this);
        }
    }

    private void cleanupStatics() {
        GcmService.preferencesProvider = null;
        GcmService.messageReceiptsProvider = null;
        GcmService.messageReceiptAlarmProvider = null;
    }

    private void doHandleIntent(Intent intent) {

        if (intent == null) {
            return;
        }

        if (hasMessageUuid(intent)) {
            enqueueReturnReceipt(intent);
        }

        getResultReceiver(intent);

        if (isBundleEmpty(intent)) {
            sendResult(RESULT_EMPTY_INTENT);
        } else {
            notifyApplication(intent);
        }
    }

    private boolean hasMessageUuid(Intent intent) {
        return intent.hasExtra(KEY_MESSAGE_UUID);
    }

    private void enqueueReturnReceipt(Intent intent) {
        final String messageUuid = intent.getStringExtra(KEY_MESSAGE_UUID);
        final MessageReceiptData messageReceipt = new MessageReceiptData();
        messageReceipt.setMessageUuid(messageUuid);
        messageReceipt.setTimestamp(new Date());
        GcmService.messageReceiptsProvider.addMessageReceipt(messageReceipt);
        GcmService.messageReceiptAlarmProvider.enableAlarmIfDisabled();
        PushLibLogger.d("There are now " + GcmService.messageReceiptsProvider.numberOfMessageReceipts() + " message receipts queued to send to the server.");
    }

    private void getResultReceiver(Intent intent) {
        if (intent.hasExtra(KEY_RESULT_RECEIVER)) {
            // Used by unit tests
            resultReceiver = intent.getParcelableExtra(KEY_RESULT_RECEIVER);
            intent.removeExtra(KEY_RESULT_RECEIVER);
        }
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
        final String packageName = preferencesProvider.loadPackageName();
        if (packageName == null) {
            return null;
        } else {
            final String broadcastName = packageName + BROADCAST_NAME_SUFFIX;
            return broadcastName;
        }
    }

    private void sendResult(int resultCode) {
        if (resultReceiver != null) {
            // Used by unit tests
            resultReceiver.send(resultCode, null);
        }
    }
}
