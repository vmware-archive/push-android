package org.omnia.pushsdk.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.ResultReceiver;

import org.omnia.pushsdk.backend.BackEndMessageReceiptApiRequestProvider;
import org.omnia.pushsdk.broadcastreceiver.MessageReceiptAlarmReceiver;
import org.omnia.pushsdk.prefs.MessageReceiptsProvider;
import org.omnia.pushsdk.prefs.RealMessageReceiptsProvider;
import org.omnia.pushsdk.sample.util.PushLibLogger;

import java.util.concurrent.Semaphore;

public class MessageReceiptService extends IntentService {

    public static final String KEY_RESULT_RECEIVER = "result_receiver";

    public static final int NO_RESULT = -1;
    public static final int RESULT_EMPTY_INTENT = 100;

    private ResultReceiver resultReceiver = null;

    // Used by unit tests
    /* package */ static Semaphore semaphore = null;
    /* package */ static MessageReceiptsProvider messageReceiptsProvider = null;
    /* package */ static BackEndMessageReceiptApiRequestProvider backEndMessageReceiptApiRequestProvider = null;

    public MessageReceiptService() {
        super("MessageReceiptService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {

            doHandleIntent(intent);

        } finally {

            // If unit tests are running then release them so that they can continue
            if (MessageReceiptService.semaphore != null) {
                MessageReceiptService.semaphore.release();
            }

            // Release the wake lock provided by the WakefulBroadcastReceiver.
            if (intent != null) {
                MessageReceiptAlarmReceiver.completeWakefulIntent(intent);
            }
        }
    }

    private void doHandleIntent(Intent intent) {

        if (intent == null) {
            return;
        }

        getResultReceiver(intent);

        MessageReceiptsProvider messageReceiptsProvider = new RealMessageReceiptsProvider(this);
        PushLibLogger.d("MessageReceiptService: receipts available to send: " + messageReceiptsProvider.numberOfMessageReceipts());
    }

    private void getResultReceiver(Intent intent) {
        if (intent.hasExtra(KEY_RESULT_RECEIVER)) {
            // Used by unit tests
            resultReceiver = intent.getParcelableExtra(KEY_RESULT_RECEIVER);
            intent.removeExtra(KEY_RESULT_RECEIVER);
        }
    }
}
