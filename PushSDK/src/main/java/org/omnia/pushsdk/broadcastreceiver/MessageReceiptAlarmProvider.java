package org.omnia.pushsdk.broadcastreceiver;

public interface MessageReceiptAlarmProvider {

    void enableAlarm();
    void disableAlarm();
    boolean isAlarmEnabled();
    void enableAlarmIfDisabled(); // Thread safe way to ensure alarm gets enabled

}
