package org.omnia.pushsdk.broadcastreceiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.SystemClock;

import org.omnia.pushsdk.util.DebugUtil;
import org.omnia.pushsdk.util.PushLibLogger;

public class MessageReceiptAlarmProviderImpl implements MessageReceiptAlarmProvider {

    private final Context context;

    public MessageReceiptAlarmProviderImpl(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
        this.context = context;
    }

    @Override
    public void enableAlarm() {
        PushLibLogger.d("Message receipt sender alarm enabled.");
        final PendingIntent intent = MessageReceiptAlarmReceiver.getPendingIntent(context);
        final AlarmManager alarmManager = getAlarmManager();
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, getTriggerMillis(), getIntervalMillis(), intent);
    }

    private long getTriggerMillis() {
        if (DebugUtil.getInstance(context).isDebuggable()) {
            return SystemClock.elapsedRealtime() + 2 * 60 * 1000; // 2 minutes
        } else {
            return SystemClock.elapsedRealtime() + 2 * AlarmManager.INTERVAL_HOUR;
        }
        // TODO - add a jitter to the trigger time
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
    public void disableAlarm() {
        PushLibLogger.d("Message receipt sender alarm disabled.");
        final PendingIntent intent = MessageReceiptAlarmReceiver.getPendingIntent(context);
        final AlarmManager alarmManager = getAlarmManager();
        alarmManager.cancel(intent);
    }
}
