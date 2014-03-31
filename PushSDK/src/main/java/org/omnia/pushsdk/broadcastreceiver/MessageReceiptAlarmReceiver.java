package org.omnia.pushsdk.broadcastreceiver;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import org.omnia.pushsdk.service.MessageReceiptService;

public class MessageReceiptAlarmReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ComponentName comp = new ComponentName(context.getPackageName(), MessageReceiptService.class.getName());
        WakefulBroadcastReceiver.startWakefulService(context, (intent.setComponent(comp)));
    }

    public static PendingIntent getPendingIntent(Context context) {
        final Intent alarmReceiverIntent = new Intent(context, MessageReceiptAlarmReceiver.class);
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmReceiverIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        return pendingIntent;
    }
}