package org.omnia.pushsdk.broadcastreceiver;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import org.omnia.pushsdk.jobs.SendEventsJob;
import org.omnia.pushsdk.service.EventService;

public class EventsSenderAlarmReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final SendEventsJob job = new SendEventsJob();
        final Intent sendEventsJobIntent = EventService.getIntentToRunJob(context, job);
        WakefulBroadcastReceiver.startWakefulService(context, sendEventsJobIntent);
    }

    public static PendingIntent getPendingIntent(Context context, int pendingIntentFlags) {
        final Intent alarmReceiverIntent = new Intent(context, EventsSenderAlarmReceiver.class);
        return PendingIntent.getBroadcast(context, 1, alarmReceiverIntent, pendingIntentFlags);
    }
}
