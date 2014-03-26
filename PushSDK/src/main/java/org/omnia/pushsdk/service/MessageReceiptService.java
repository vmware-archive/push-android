package org.omnia.pushsdk.service;

import android.app.IntentService;
import android.content.Intent;

import org.omnia.pushsdk.broadcastreceiver.MessageReceiptAlarmReceiver;
import org.omnia.pushsdk.prefs.MessageReceiptsProvider;
import org.omnia.pushsdk.prefs.RealMessageReceiptsProvider;
import org.omnia.pushsdk.sample.util.PushLibLogger;

public class MessageReceiptService extends IntentService {

    public MessageReceiptService() {
        super("MessageReceiptService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            doHandleIntent(intent);
        } finally {
            if (intent != null) {
                MessageReceiptAlarmReceiver.completeWakefulIntent(intent);
            }
        }
    }

    private void doHandleIntent(Intent intent) {
        MessageReceiptsProvider messageReceiptsProvider = new RealMessageReceiptsProvider(this);
        PushLibLogger.d("MessageReceiptService: receipts available to send: " + messageReceiptsProvider.numberOfMessageReceipts());
    }
}
