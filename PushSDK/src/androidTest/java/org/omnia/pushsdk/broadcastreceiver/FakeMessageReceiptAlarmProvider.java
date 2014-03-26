package org.omnia.pushsdk.broadcastreceiver;

public class FakeMessageReceiptAlarmProvider implements MessageReceiptAlarmProvider {

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
