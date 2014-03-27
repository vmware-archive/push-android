package org.omnia.pushsdk.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.ResultReceiver;

import org.omnia.pushsdk.backend.BackEndMessageReceiptApiRequest;
import org.omnia.pushsdk.backend.BackEndMessageReceiptApiRequestImpl;
import org.omnia.pushsdk.backend.BackEndMessageReceiptApiRequestProvider;
import org.omnia.pushsdk.backend.BackEndMessageReceiptListener;
import org.omnia.pushsdk.broadcastreceiver.MessageReceiptAlarmProvider;
import org.omnia.pushsdk.broadcastreceiver.MessageReceiptAlarmProviderImpl;
import org.omnia.pushsdk.broadcastreceiver.MessageReceiptAlarmReceiver;
import org.omnia.pushsdk.network.NetworkWrapper;
import org.omnia.pushsdk.network.NetworkWrapperImpl;
import org.omnia.pushsdk.prefs.MessageReceiptsProvider;
import org.omnia.pushsdk.prefs.MessageReceiptsProviderImpl;
import org.omnia.pushsdk.model.MessageReceiptData;
import org.omnia.pushsdk.util.PushLibLogger;

import java.util.List;
import java.util.concurrent.Semaphore;

public class MessageReceiptService extends IntentService {

    public static final String KEY_RESULT_RECEIVER = "result_receiver";

    public static final int NO_RESULT = -1;
    public static final int RESULT_NO_WORK_TO_DO = 100;
    public static final int RESULT_SENT_RECEIPTS_SUCCESSFULLY = 101;
    public static final int RESULT_FAILED_TO_SEND_RECEIPTS = 102;

    private ResultReceiver resultReceiver = null;

    // Used by unit tests
    /* package */ static Semaphore semaphore = null;
    /* package */ static MessageReceiptsProvider messageReceiptsProvider = null;
    /* package */ static BackEndMessageReceiptApiRequestProvider backEndMessageReceiptApiRequestProvider = null;
    /* package */ static MessageReceiptAlarmProvider messageReceiptAlarmProvider = null;

    public MessageReceiptService() {
        super("MessageReceiptService");
        if (MessageReceiptService.messageReceiptsProvider == null) {
            MessageReceiptService.messageReceiptsProvider = new MessageReceiptsProviderImpl(this);
        }
        if (MessageReceiptService.messageReceiptAlarmProvider == null) {
            MessageReceiptService.messageReceiptAlarmProvider = new MessageReceiptAlarmProviderImpl(this);
        }
        if (MessageReceiptService.backEndMessageReceiptApiRequestProvider == null) {
            final NetworkWrapper networkWrapper = new NetworkWrapperImpl();
            final BackEndMessageReceiptApiRequestImpl backEndMessageReceiptApiRequest = new BackEndMessageReceiptApiRequestImpl(networkWrapper);
            MessageReceiptService.backEndMessageReceiptApiRequestProvider = new BackEndMessageReceiptApiRequestProvider(backEndMessageReceiptApiRequest);
        }
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

            cleanupStatics();

            // Release the wake lock provided by the WakefulBroadcastReceiver.
            if (intent != null) {
                MessageReceiptAlarmReceiver.completeWakefulIntent(intent);
            }
        }
    }

    private void doHandleIntent(Intent intent) {

        if (intent == null) {
            MessageReceiptService.messageReceiptAlarmProvider.disableAlarm();
            return;
        }

        getResultReceiver(intent);

        PushLibLogger.d("MessageReceiptService: receipts available to send: " + MessageReceiptService.messageReceiptsProvider.numberOfMessageReceipts());
        if (MessageReceiptService.messageReceiptsProvider.numberOfMessageReceipts() > 0) {
            final List<MessageReceiptData> messageReceipts = MessageReceiptService.messageReceiptsProvider.loadMessageReceipts();
            sendMessageReceipts(messageReceipts);
        } else {
            MessageReceiptService.messageReceiptAlarmProvider.disableAlarm();
            sendResult(RESULT_NO_WORK_TO_DO);
        }
    }

    private void getResultReceiver(Intent intent) {
        if (intent.hasExtra(KEY_RESULT_RECEIVER)) {
            // Used by unit tests
            resultReceiver = intent.getParcelableExtra(KEY_RESULT_RECEIVER);
            intent.removeExtra(KEY_RESULT_RECEIVER);
        }
    }

    private void sendMessageReceipts(final List<MessageReceiptData> messageReceipts) {
        final BackEndMessageReceiptApiRequest apiRequest = MessageReceiptService.backEndMessageReceiptApiRequestProvider.getRequest();
        apiRequest.startSendMessageReceipts(messageReceipts, new BackEndMessageReceiptListener() {

            @Override
            public void onBackEndMessageReceiptSuccess() {
                MessageReceiptService.messageReceiptsProvider.removeMessageReceipts(messageReceipts);
                if (MessageReceiptService.messageReceiptsProvider.numberOfMessageReceipts() <= 0) {
                    MessageReceiptService.messageReceiptAlarmProvider.disableAlarm();
                }
                sendResult(RESULT_SENT_RECEIPTS_SUCCESSFULLY);
            }

            @Override
            public void onBackEndMessageReceiptFailed(String reason) {
                sendResult(RESULT_FAILED_TO_SEND_RECEIPTS);
            }
        });
    }

    private void sendResult(int resultCode) {
        if (resultReceiver != null) {
            // Used by unit tests
            resultReceiver.send(resultCode, null);
        }
    }

    private void cleanupStatics() {
        MessageReceiptService.messageReceiptsProvider = null;
        MessageReceiptService.messageReceiptAlarmProvider = null;
        MessageReceiptService.backEndMessageReceiptApiRequestProvider = null;
    }

}
