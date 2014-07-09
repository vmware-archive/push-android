/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import io.pivotal.android.push.receiver.GcmBroadcastReceiver;
import io.pivotal.android.push.util.Logger;

public class GcmService extends IntentService {
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_MESSAGE_UUID = "msg_uuid";

    public GcmService() {
        super("GcmService");
    }

    @Override
    protected final void onHandleIntent(Intent intent) {
        Logger.fd("GcmService has received a push message from GCM.");

        if (intent != null) {
            onReceive(intent);
            GcmBroadcastReceiver.completeWakefulIntent(intent);
        }
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
