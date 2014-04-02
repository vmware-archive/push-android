package org.omnia.pushsdk.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.omnia.pushsdk.prefs.MessageReceiptsProvider;
import org.omnia.pushsdk.prefs.MessageReceiptsProviderImpl;
import org.omnia.pushsdk.util.Const;
import org.omnia.pushsdk.util.DebugUtil;
import org.omnia.pushsdk.util.PushLibLogger;

import java.util.concurrent.Semaphore;

// Received whenever the phone boots up
public class BootCompletedBroadcastReceiver extends BroadcastReceiver {

    public MessageReceiptAlarmProvider messageReceiptAlarmProvider;
    public MessageReceiptsProvider messageReceiptsProvider;

    public BootCompletedBroadcastReceiver() {
        // Does nothing
    }

    public BootCompletedBroadcastReceiver(MessageReceiptAlarmProvider messageReceiptAlarmProvider, MessageReceiptsProvider messageReceiptsProvider) {
        this.messageReceiptAlarmProvider = messageReceiptAlarmProvider;
        this.messageReceiptsProvider = messageReceiptsProvider;
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
        if (messageReceiptAlarmProvider == null) {
            messageReceiptAlarmProvider = new MessageReceiptAlarmProviderImpl(context);
        }
        if (messageReceiptsProvider == null) {
            messageReceiptsProvider = new MessageReceiptsProviderImpl(context);
        }
    }

    private void startAlarm() {
        final int numberOfMessageReceipts = messageReceiptsProvider.numberOfMessageReceipts();
        if (numberOfMessageReceipts > 0) {
            PushLibLogger.fd("There are %d message receipt(s) queued for sending. Enabling alarm.", numberOfMessageReceipts);
            messageReceiptAlarmProvider.enableAlarmIfDisabled();
        } else {
            PushLibLogger.d("There are no message receipts queued for sending.");
        }
    }
}
