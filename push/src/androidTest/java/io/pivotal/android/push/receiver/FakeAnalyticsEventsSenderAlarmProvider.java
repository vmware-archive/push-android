package io.pivotal.android.push.receiver;

public class FakeAnalyticsEventsSenderAlarmProvider implements AnalyticsEventsSenderAlarmProvider {

    private boolean isAlarmEnabled = false;
    private boolean isAlarmEnabledImmediately = false;

    @Override
    public synchronized void enableAlarm() {
        isAlarmEnabled = true;
    }

    @Override
    public void enableAlarmImmediately() {
        isAlarmEnabledImmediately = true;
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

    public synchronized boolean isAlarmEnabledImmediately() {
        return isAlarmEnabledImmediately;
    }

    @Override
    public synchronized void enableAlarmIfDisabled() {
        if (!isAlarmEnabled()) {
            enableAlarm();
        }
    }

    @Override
    public void enableAlarmImmediatelyIfDisabled() {
        if (!isAlarmEnabled()) {
            enableAlarmImmediately();
        }
    }
}
