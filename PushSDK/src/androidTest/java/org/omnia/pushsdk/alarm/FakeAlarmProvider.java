package org.omnia.pushsdk.alarm;

public class FakeAlarmProvider implements AlarmProvider {

    private boolean isAlarmEnabled = false;

    @Override
    public void enableAlarm() {
        isAlarmEnabled = true;
    }

    @Override
    public void disableAlarm() {
        isAlarmEnabled = false;
    }

    public boolean isAlarmEnabled() {
        return isAlarmEnabled;
    }
}
