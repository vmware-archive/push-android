package org.omnia.pushsdk.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.omnia.pushsdk.database.DatabaseEventsStorage;
import org.omnia.pushsdk.database.EventsDatabaseHelper;
import org.omnia.pushsdk.database.EventsDatabaseWrapper;
import org.omnia.pushsdk.database.EventsStorage;
import org.omnia.pushsdk.util.Const;
import org.omnia.pushsdk.util.PushLibLogger;

// Received whenever the phone boots up
public class BootCompletedBroadcastReceiver extends BroadcastReceiver {

    private Context context;
    private EventsStorage eventsStorage;
    private MessageReceiptAlarmProvider messageReceiptAlarmProvider;

    public BootCompletedBroadcastReceiver() {
        // Does nothing
    }

    public BootCompletedBroadcastReceiver(Context context, EventsStorage eventsStorage, MessageReceiptAlarmProvider messageReceiptAlarmProvider) {
        this.context = context;
        this.eventsStorage = eventsStorage;
        this.messageReceiptAlarmProvider = messageReceiptAlarmProvider;
    }

    @Override
	public void onReceive(Context context, Intent intent) {
        if (!PushLibLogger.isSetup()) {
            PushLibLogger.setup(context, Const.TAG_NAME);
        }
        PushLibLogger.d("Device boot detected for package " + context.getPackageName());

        setupDependencies(context);

        startAlarm();
    }

    private void setupDependencies(Context context) {

        EventsDatabaseHelper.init();
        EventsDatabaseWrapper.createDatabaseInstance(context);

        if (messageReceiptAlarmProvider == null) {
            messageReceiptAlarmProvider = new MessageReceiptAlarmProviderImpl(context);
        }
        if (eventsStorage == null) {
            eventsStorage = new DatabaseEventsStorage();
        }
    }

    private void startAlarm() {
        final int numberOfMessageReceipts = eventsStorage.getNumberOfEvents(EventsStorage.EventType.MESSAGE_RECEIPT);
        if (numberOfMessageReceipts > 0) {
            PushLibLogger.fd("There are %d message receipt(s) queued for sending. Enabling alarm.", numberOfMessageReceipts);
            messageReceiptAlarmProvider.enableAlarmIfDisabled();
        } else {
            PushLibLogger.d("There are no message receipts queued for sending.");
        }
    }
}
