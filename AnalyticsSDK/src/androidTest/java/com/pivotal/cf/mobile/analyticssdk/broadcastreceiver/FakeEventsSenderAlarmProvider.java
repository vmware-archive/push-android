package com.pivotal.cf.mobile.analyticssdk.broadcastreceiver;

import com.pivotal.cf.mobile.analyticssdk.broadcastreceiver.EventsSenderAlarmProvider;

public class FakeEventsSenderAlarmProvider implements EventsSenderAlarmProvider {

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
