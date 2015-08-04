package io.pivotal.android.push.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.SystemClock;

import java.util.Random;

import io.pivotal.android.push.util.DebugUtil;
import io.pivotal.android.push.util.Logger;

public class EventsSenderAlarmProviderImpl implements EventsSenderAlarmProvider {

    private final Context context;

    public EventsSenderAlarmProviderImpl(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
        this.context = context;
    }

    @Override
    public synchronized void enableAlarm() {
        Logger.d("Events sender alarm enabled.");
        final PendingIntent intent = EventsSenderAlarmReceiver.getPendingIntent(context, PendingIntent.FLAG_UPDATE_CURRENT);
        final AlarmManager alarmManager = getAlarmManager();
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, getTriggerMillis(), getIntervalMillis(), intent);
    }

    private long getTriggerMillis() {
        if (DebugUtil.getInstance(context).isDebuggable()) {
            return SystemClock.elapsedRealtime() + 2 * 60 * 1000; // 2 minutes
        } else {
            return SystemClock.elapsedRealtime() + EventsSenderAlarmProviderImpl.getTriggerOffsetInMillis();
        }
    }

    private long getIntervalMillis() {
        if (DebugUtil.getInstance(context).isDebuggable()) {
            return 60 * 1000; // 1 minute
        } else {
            return AlarmManager.INTERVAL_HOUR;
        }
    }

    private AlarmManager getAlarmManager() {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    @Override
    public synchronized void disableAlarm() {
        Logger.d("Events sender alarm disabled.");
        final PendingIntent intent = EventsSenderAlarmReceiver.getPendingIntent(context, PendingIntent.FLAG_UPDATE_CURRENT);
        final AlarmManager alarmManager = getAlarmManager();
        alarmManager.cancel(intent);
        intent.cancel();
    }

    @Override
    public synchronized boolean isAlarmEnabled() {
        final PendingIntent intent = EventsSenderAlarmReceiver.getPendingIntent(context, PendingIntent.FLAG_NO_CREATE);
        final boolean isAlarmEnabled = (intent != null);
        Logger.d("Events sender alarm enabled: " + (isAlarmEnabled ? "yes." : "no."));
        return isAlarmEnabled;
    }

    @Override
    public synchronized void enableAlarmIfDisabled() {
        if (!isAlarmEnabled()) {
            enableAlarm();
        }
    }

    // Returns a random number in the range [1 hour, 3 hours)
    public static long getTriggerOffsetInMillis() {
        final Random r = new Random();
        long jitter = (long)(r.nextDouble() * 2.0d * (double) AlarmManager.INTERVAL_HOUR) - AlarmManager.INTERVAL_HOUR;
        long offset = 2 * AlarmManager.INTERVAL_HOUR + jitter;
        return offset;
    }
}
