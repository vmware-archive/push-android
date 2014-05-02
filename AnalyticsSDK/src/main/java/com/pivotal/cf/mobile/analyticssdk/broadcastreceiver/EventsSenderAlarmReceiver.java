package com.pivotal.cf.mobile.analyticssdk.broadcastreceiver;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.pivotal.cf.mobile.analyticssdk.jobs.SendEventsJob;
import com.pivotal.cf.mobile.analyticssdk.service.EventService;

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
