package io.pivotal.android.push.receiver;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import io.pivotal.android.push.analytics.jobs.SendAnalyticsEventsJob;
import io.pivotal.android.push.prefs.Pivotal;
import io.pivotal.android.push.service.AnalyticsEventService;
import io.pivotal.android.push.util.Logger;

public class AnalyticsEventsSenderAlarmReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Pivotal.getAreAnalyticsEnabled(context)) {
            final SendAnalyticsEventsJob job = new SendAnalyticsEventsJob();
            final Intent sendEventsJobIntent = AnalyticsEventService.getIntentToRunJob(context, job);
            WakefulBroadcastReceiver.startWakefulService(context, sendEventsJobIntent);
        } else {
            Logger.i("Ignoring EventsSenderAlarm since Analytics have been disabled.");
            AnalyticsEventsSenderAlarmReceiver.completeWakefulIntent(intent);
        }
    }

    public static PendingIntent getPendingIntent(Context context, int pendingIntentFlags) {
        final Intent alarmReceiverIntent = new Intent(context, AnalyticsEventsSenderAlarmReceiver.class);
        return PendingIntent.getBroadcast(context, 1, alarmReceiverIntent, pendingIntentFlags);
    }
}
