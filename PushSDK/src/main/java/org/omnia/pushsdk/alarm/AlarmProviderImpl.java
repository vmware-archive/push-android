package org.omnia.pushsdk.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.SystemClock;

import org.omnia.pushsdk.sample.util.PushLibLogger;

public class AlarmProviderImpl implements AlarmProvider {

    private final Context context;

    public AlarmProviderImpl(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
        this.context = context;
    }

    @Override
    public void enableAlarm() {
        final PendingIntent intent = AlarmReceiver.getPendingIntent(context);
        final AlarmManager alarmManager = getAlarmManager();
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, getTriggerMillis(), getIntervalMillis(), intent);
    }

    private long getTriggerMillis() {
        return SystemClock.elapsedRealtime() + 1000/*AlarmManager.INTERVAL_FIFTEEN_MINUTES*/;
    }

    private long getIntervalMillis() {
        return 10000/*AlarmManager.INTERVAL_FIFTEEN_MINUTES*/;
    }

    private AlarmManager getAlarmManager() {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    @Override
    public void disableAlarm() {
        final PendingIntent intent = AlarmReceiver.getPendingIntent(context);
        final AlarmManager alarmManager = getAlarmManager();
        alarmManager.cancel(intent);
    }
}
