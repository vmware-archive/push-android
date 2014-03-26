package org.omnia.pushsdk.alarm;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
    }

    public static PendingIntent getPendingIntent(Context context) {
        final Intent alarmReceiverIntent = new Intent(context, AlarmReceiver.class);
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmReceiverIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        return pendingIntent;
    }
}
