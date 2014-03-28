package org.omnia.pushsdk.broadcastreceiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.SystemClock;

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

        // TODO - uncomment the next line once the server is able to accept message receipts
//        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, getTriggerMillis(), getIntervalMillis(), intent);
    }

    private long getTriggerMillis() {
        return SystemClock.elapsedRealtime() + 2 * AlarmManager.INTERVAL_HOUR;
        // TODO - add a jitter to the trigger time
    }

    private long getIntervalMillis() {
        return AlarmManager.INTERVAL_HOUR;
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
