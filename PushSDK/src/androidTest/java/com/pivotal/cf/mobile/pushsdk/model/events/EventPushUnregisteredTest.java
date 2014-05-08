package com.pivotal.cf.mobile.pushsdk.model.events;

import android.test.AndroidTestCase;

import com.pivotal.cf.mobile.analyticssdk.model.events.Event;

import java.util.HashMap;

public class EventPushUnregisteredTest extends AndroidTestCase {

    public static final String TEST_VARIANT_UUID = "TEST_VARIANT_UUID";
    public static final String TEST_DEVICE_ID = "TEST_DEVICE_ID";

    public void testGetEvent() {
        final Event event = EventPushUnregistered.getEvent(TEST_VARIANT_UUID, TEST_DEVICE_ID);
        assertEquals(EventPushUnregistered.EVENT_TYPE, event.getEventType());
        final HashMap<String, Object> data = event.getData();
        assertEquals(TEST_VARIANT_UUID, data.get(PushEventHelper.VARIANT_UUID));
        assertEquals(TEST_DEVICE_ID, data.get(PushEventHelper.DEVICE_ID));
    }
}
