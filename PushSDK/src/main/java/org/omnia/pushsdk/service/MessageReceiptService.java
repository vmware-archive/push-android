package org.omnia.pushsdk.service;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.ResultReceiver;

import org.omnia.pushsdk.backend.BackEndMessageReceiptApiRequest;
import org.omnia.pushsdk.backend.BackEndMessageReceiptApiRequestImpl;
import org.omnia.pushsdk.backend.BackEndMessageReceiptApiRequestProvider;
import org.omnia.pushsdk.backend.BackEndMessageReceiptListener;
import org.omnia.pushsdk.broadcastreceiver.MessageReceiptAlarmProvider;
import org.omnia.pushsdk.broadcastreceiver.MessageReceiptAlarmProviderImpl;
import org.omnia.pushsdk.broadcastreceiver.MessageReceiptAlarmReceiver;
import org.omnia.pushsdk.database.DatabaseEventsStorage;
import org.omnia.pushsdk.database.EventsDatabaseHelper;
import org.omnia.pushsdk.database.EventsDatabaseWrapper;
import org.omnia.pushsdk.database.EventsStorage;
import org.omnia.pushsdk.model.EventBase;
import org.omnia.pushsdk.network.NetworkWrapper;
import org.omnia.pushsdk.network.NetworkWrapperImpl;
import org.omnia.pushsdk.util.Const;
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
    /* package */ static EventsStorage eventsStorage = null;
    /* package */ static BackEndMessageReceiptApiRequestProvider backEndMessageReceiptApiRequestProvider = null;
    /* package */ static MessageReceiptAlarmProvider messageReceiptAlarmProvider = null;

    public MessageReceiptService() {
        super("MessageReceiptService");
    }

    private void setupStatics() {

        EventsDatabaseHelper.init();
        EventsDatabaseWrapper.createDatabaseInstance(this);

        if (MessageReceiptService.eventsStorage == null) {
            MessageReceiptService.eventsStorage = new DatabaseEventsStorage();
        }
        if (MessageReceiptService.messageReceiptAlarmProvider == null) {
            MessageReceiptService.messageReceiptAlarmProvider = new MessageReceiptAlarmProviderImpl(this);
        }
        if (MessageReceiptService.backEndMessageReceiptApiRequestProvider == null) {
            final NetworkWrapper networkWrapper = new NetworkWrapperImpl();
            final BackEndMessageReceiptApiRequestImpl backEndMessageReceiptApiRequest = new BackEndMessageReceiptApiRequestImpl(this, eventsStorage, networkWrapper);
            MessageReceiptService.backEndMessageReceiptApiRequestProvider = new BackEndMessageReceiptApiRequestProvider(backEndMessageReceiptApiRequest);
        }
    }

    private void cleanupStatics() {
        MessageReceiptService.eventsStorage = null;
        MessageReceiptService.messageReceiptAlarmProvider = null;
        MessageReceiptService.backEndMessageReceiptApiRequestProvider = null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        setupLogger();

        if (intent == null) {
            MessageReceiptService.messageReceiptAlarmProvider.disableAlarm();
            postProcessAfterService(intent);
            return;
        }

        setupStatics();

        getResultReceiver(intent);

        final List<Uri> uris = getUnpostedMessageReceipts();
        PushLibLogger.fd("MessageReceiptService: package %s: receipts available to send: %d", getPackageName(), uris.size());

        if (uris.size() > 0) {

            sendMessageReceipts(uris, intent);

        } else {

            PushLibLogger.d("MessageReceiptService: found no receipts. Disabling alarm.");
            MessageReceiptService.messageReceiptAlarmProvider.disableAlarm();
            sendResult(RESULT_NO_WORK_TO_DO);
            postProcessAfterService(intent);
        }
    }

    // If the service gets started in the background without the rest of the application running, then it will
    // have to kick off the logger itself.
    private void setupLogger() {
        if (!PushLibLogger.isSetup()) {
            PushLibLogger.setup(this, Const.TAG_NAME);
        }
    }

    private void getResultReceiver(Intent intent) {
        if (intent.hasExtra(KEY_RESULT_RECEIVER)) {
            // Used by unit tests
            resultReceiver = intent.getParcelableExtra(KEY_RESULT_RECEIVER);
            intent.removeExtra(KEY_RESULT_RECEIVER);
        }
    }

    private void sendMessageReceipts(final List<Uri> messageReceiptUris, final Intent intent) {
        final BackEndMessageReceiptApiRequest apiRequest = MessageReceiptService.backEndMessageReceiptApiRequestProvider.getRequest();
        apiRequest.startSendMessageReceipts(messageReceiptUris, new BackEndMessageReceiptListener() {

            @Override
            public void onBackEndMessageReceiptSuccess() {
                try {
                    sendResult(RESULT_SENT_RECEIPTS_SUCCESSFULLY);
                    postProcessAfterRequest(messageReceiptUris);
                } finally {
                    postProcessAfterService(intent);
                }
            }

            @Override
            public void onBackEndMessageReceiptFailed(String reason) {
                try {
                    sendResult(RESULT_FAILED_TO_SEND_RECEIPTS);
                } finally {
                    postProcessAfterService(intent);
                }
            }
        });
    }

    private void sendResult(int resultCode) {
        if (resultReceiver != null) {
            // Used by unit tests
            resultReceiver.send(resultCode, null);
        }
    }

    private void postProcessAfterRequest(final List<Uri> messageReceiptUris) {
        if (messageReceiptUris != null) {
            MessageReceiptService.eventsStorage.deleteEvents(messageReceiptUris, EventsStorage.EventType.MESSAGE_RECEIPT);
            final List<Uri> remainingUnpostedUris = getUnpostedMessageReceipts();
            if (remainingUnpostedUris.size() <= 0) {
                PushLibLogger.d("MessageReceiptService: no more messages left in queue. Disabling alarm.");
                MessageReceiptService.messageReceiptAlarmProvider.disableAlarm();
            } else {
                PushLibLogger.fd("MessageReceiptService: there are still %d more message(s) left in queue. Leaving alarm enabled.", remainingUnpostedUris.size());
            }
        }
    }

    private List<Uri> getUnpostedMessageReceipts() {
        final List<Uri> uris = MessageReceiptService.eventsStorage.getEventUrisWithStatus(EventsStorage.EventType.MESSAGE_RECEIPT, EventBase.Status.NOT_POSTED);
        return uris;
    }

    private void postProcessAfterService(Intent intent) {

        try {

            cleanupStatics();

            // If unit tests are running then release them so that they can continue
            if (MessageReceiptService.semaphore != null) {
                MessageReceiptService.semaphore.release();
            }

        } finally {

            // Release the wake lock provided by the WakefulBroadcastReceiver.
            // SUPER IMPORTANT! Make sure that this gets called EVERY time this service is invoked, but not until AFTER
            // any requests are completed -- otherwise the device might return to sleep before the request is complete.
            if (intent != null) {
                MessageReceiptAlarmReceiver.completeWakefulIntent(intent);
            }
        }
    }

}
