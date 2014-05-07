package com.pivotal.cf.mobile.analyticssdk.broadcastreceiver;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.pivotal.cf.mobile.analyticssdk.jobs.SendEventsJob;
import com.pivotal.cf.mobile.analyticssdk.service.EventService;
import com.pivotal.cf.mobile.common.prefs.AnalyticsPreferencesProvider;
import com.pivotal.cf.mobile.common.prefs.AnalyticsPreferencesProviderImpl;
import com.pivotal.cf.mobile.common.util.Logger;

public class EventsSenderAlarmReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (isAnalyticsEnabled(context)) {
            final SendEventsJob job = new SendEventsJob();
            final Intent sendEventsJobIntent = EventService.getIntentToRunJob(context, job);
            WakefulBroadcastReceiver.startWakefulService(context, sendEventsJobIntent);
        } else {
            Logger.i("Ignoring EventsSenderAlarm since Analytics have been disabled.");
            EventsSenderAlarmReceiver.completeWakefulIntent(intent);
        }
    }

    private boolean isAnalyticsEnabled(Context context) {
        final AnalyticsPreferencesProvider preferencesProvider = new AnalyticsPreferencesProviderImpl(context);
        return preferencesProvider.isAnalyticsEnabled();
    }

    public static PendingIntent getPendingIntent(Context context, int pendingIntentFlags) {
        final Intent alarmReceiverIntent = new Intent(context, EventsSenderAlarmReceiver.class);
        return PendingIntent.getBroadcast(context, 1, alarmReceiverIntent, pendingIntentFlags);
    }
}
