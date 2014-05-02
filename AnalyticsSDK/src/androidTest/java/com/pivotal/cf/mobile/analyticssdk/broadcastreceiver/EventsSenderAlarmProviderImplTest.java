package com.pivotal.cf.mobile.analyticssdk.broadcastreceiver;

import android.app.AlarmManager;
import android.test.AndroidTestCase;

import com.pivotal.cf.mobile.analyticssdk.broadcastreceiver.EventsSenderAlarmProviderImpl;

public class EventsSenderAlarmProviderImplTest extends AndroidTestCase {

    private static final int NUMBER_OF_TESTS = 1000;
    private static final long TRIGGER_LOWER_BOUND = AlarmManager.INTERVAL_HOUR;
    private static final long TRIGGER_UPPER_BOUND = 3 * AlarmManager.INTERVAL_HOUR;

    public void testGetTriggerOffset() {
        for (int i = 0; i < NUMBER_OF_TESTS; i += 1) {
            long offset = EventsSenderAlarmProviderImpl.getTriggerOffsetInMillis();
            assertTrue("Bad trigger offset " + offset, offset >= TRIGGER_LOWER_BOUND);
            assertTrue("Bad trigger offset " + offset, offset <=  TRIGGER_UPPER_BOUND);
        }
    }
}
