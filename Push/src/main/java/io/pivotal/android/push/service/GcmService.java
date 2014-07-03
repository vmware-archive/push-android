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

import io.pivotal.android.common.util.Logger;
import io.pivotal.android.push.receiver.GcmBroadcastReceiver;

public class GcmService extends IntentService {
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_MESSAGE_UUID = "msg_uuid";

    public GcmService() {
        super("GcmService");
    }

    @Override
    protected final void onHandleIntent(Intent intent) {
        Logger.fd("GcmService has received a push message from GCM.");

        if (intent == null) {
            return;
        }

        onReceive(intent);

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
}
