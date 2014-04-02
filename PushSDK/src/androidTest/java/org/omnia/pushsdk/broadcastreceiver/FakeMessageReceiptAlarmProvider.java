package org.omnia.pushsdk.broadcastreceiver;

public class FakeMessageReceiptAlarmProvider implements MessageReceiptAlarmProvider {

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
