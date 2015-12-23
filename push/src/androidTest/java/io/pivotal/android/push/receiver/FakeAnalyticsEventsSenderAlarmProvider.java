package io.pivotal.android.push.receiver;

public class FakeAnalyticsEventsSenderAlarmProvider implements AnalyticsEventsSenderAlarmProvider {

    private boolean isAlarmEnabled = false;

    @Override
    public synchronized void enableAlarm() {
        isAlarmEnabled = true;
    }

    @Override
    public synchronized void disableAlarm() {
        isAlarmEnabled = false;
    }

    @Override
    public synchronized boolean isAlarmEnabled() {
        return isAlarmEnabled;
    }

    @Override
    public synchronized void enableAlarmIfDisabled() {
        if (!isAlarmEnabled()) {
            enableAlarm();
        }
    }
}
