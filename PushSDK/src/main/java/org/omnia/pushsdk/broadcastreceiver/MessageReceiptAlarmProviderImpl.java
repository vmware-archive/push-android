package org.omnia.pushsdk.broadcastreceiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.SystemClock;

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
        final PendingIntent intent = MessageReceiptAlarmReceiver.getPendingIntent(context);
        final AlarmManager alarmManager = getAlarmManager();

        // TODO - uncomment the next line once the server is able to accept message receipts

//        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, getTriggerMillis(), getIntervalMillis(), intent);
    }

    private long getTriggerMillis() {
        // TODO - select a better alarm trigger time
        return SystemClock.elapsedRealtime() + 1000/*AlarmManager.INTERVAL_FIFTEEN_MINUTES*/;
    }

    private long getIntervalMillis() {
        // TODO - select a better alarm interval time
        return 10000/*AlarmManager.INTERVAL_FIFTEEN_MINUTES*/;
    }

    private AlarmManager getAlarmManager() {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    @Override
    public void disableAlarm() {
        final PendingIntent intent = MessageReceiptAlarmReceiver.getPendingIntent(context);
        final AlarmManager alarmManager = getAlarmManager();
        alarmManager.cancel(intent);
    }
}
