package io.pivotal.android.push.receiver;

public interface AnalyticsEventsSenderAlarmProvider {

    void enableAlarm();
    void disableAlarm();
    boolean isAlarmEnabled();
    void enableAlarmIfDisabled(); // Thread safe way to ensure alarm gets enabled
}
