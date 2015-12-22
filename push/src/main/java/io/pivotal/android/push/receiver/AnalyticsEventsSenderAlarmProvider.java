package io.pivotal.android.push.receiver;

public interface AnalyticsEventsSenderAlarmProvider {

    void enableAlarm();
    void enableAlarmImmediately();
    void disableAlarm();
    boolean isAlarmEnabled();
    void enableAlarmIfDisabled(); // Thread safe way to ensure alarm gets enabled
    void enableAlarmImmediatelyIfDisabled(); // Thread safe way to ensure alarm gets enabled
}
