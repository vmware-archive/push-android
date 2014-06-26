package io.pivotal.android.push.model.events;

import android.test.AndroidTestCase;

import java.util.HashMap;

import io.pivotal.android.analytics.model.events.Event;

public class EventPushRegisteredTest extends AndroidTestCase {

    public static final String TEST_VARIANT_UUID = "TEST_VARIANT_UUID";
    public static final String TEST_DEVICE_ID = "TEST_DEVICE_ID";

    public void testGetEvent() {
        final Event event = EventPushRegistered.getEvent(TEST_VARIANT_UUID, TEST_DEVICE_ID);
        assertEquals(EventPushRegistered.EVENT_TYPE, event.getEventType());
        final HashMap<String, Object> data = event.getData();
        assertEquals(TEST_VARIANT_UUID, data.get(PushEventHelper.VARIANT_UUID));
        assertEquals(TEST_DEVICE_ID, data.get(PushEventHelper.DEVICE_ID));
    }
}
