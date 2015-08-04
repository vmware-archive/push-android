package io.pivotal.android.push.analytics;

import android.content.ComponentName;
import android.content.Intent;
import android.test.AndroidTestCase;

import java.util.HashMap;
import java.util.Properties;

import io.pivotal.android.push.analytics.jobs.EnqueueEventJob;
import io.pivotal.android.push.model.analytics.Event;
import io.pivotal.android.push.prefs.FakePushPreferencesProvider;
import io.pivotal.android.push.prefs.Pivotal;
import io.pivotal.android.push.prefs.PushPreferencesProvider;
import io.pivotal.android.push.service.EventService;
import io.pivotal.android.push.util.FakeServiceStarter;

public class EventLoggerTest extends AndroidTestCase {

    private static final String TEST_EVENT_TYPE = "TEST_EVENT_TYPE";
    private static final String TEST_EVENT_RECEIPT_ID = "receiptId";
    private static final String TEST_EVENT_DEVICE_UUID = "deviceUuid";
    private static final String TEST_EVENT_GEOFENCE_ID = "geofenceId";
    private static final String TEST_EVENT_LOCATION_ID = "locationId";
    private static final String TEST_EVENT_RECEIPT_ID_VALUE = "SOME_RECEIPT_ID";
    private static final String TEST_EVENT_DEVICE_UUID_VALUE = "SOME_DEVICE_UUID";
    private static final String TEST_EVENT_GEOFENCE_ID_VALUE = "SOME_GEOFENCE_ID";
    private static final String TEST_EVENT_LOCATION_ID_VALUE = "SOME_LOCATION_ID";
    private static HashMap<String, String> TEST_EVENT_FIELDS;

    private FakeServiceStarter serviceStarter;
    private PushPreferencesProvider preferencesProvider;

    static {
        TEST_EVENT_FIELDS = new HashMap<>();
        TEST_EVENT_FIELDS.put(TEST_EVENT_RECEIPT_ID, TEST_EVENT_RECEIPT_ID_VALUE);
        TEST_EVENT_FIELDS.put(TEST_EVENT_DEVICE_UUID, TEST_EVENT_DEVICE_UUID_VALUE);
        TEST_EVENT_FIELDS.put(TEST_EVENT_GEOFENCE_ID, TEST_EVENT_GEOFENCE_ID_VALUE);
        TEST_EVENT_FIELDS.put(TEST_EVENT_LOCATION_ID, TEST_EVENT_LOCATION_ID_VALUE);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        serviceStarter = new FakeServiceStarter();
        serviceStarter.setReturnedComponentName(new ComponentName(getContext(), EventService.class));
        preferencesProvider = new FakePushPreferencesProvider(null, TEST_EVENT_DEVICE_UUID_VALUE, 0, null, null, null, null, null, null, null, 0, false);

    }

    public void testRequiresServiceStarter() {
        try {
            new EventLogger(null, preferencesProvider, getContext());
            fail("should have failed");
        } catch (IllegalArgumentException e) {
            // should have thrown
        }
    }

    public void testRequiresPushParameters() {
        try {
            new EventLogger(serviceStarter, null, getContext());
            fail("should have failed");
        } catch (IllegalArgumentException e) {
            // should have thrown
        }
    }

    public void testRequiresContext() {
        try {
            new EventLogger(serviceStarter, preferencesProvider, null);
            fail("should have failed");
        } catch (IllegalArgumentException e) {
            // should have thrown
        }
    }

    public void testLogEventAnalyticsDisabled() {
        final EventLogger eventLogger = getEventLoggerWithAnalyticsDisabled();
        eventLogger.logEvent(TEST_EVENT_TYPE, TEST_EVENT_FIELDS);
        assertFalse(serviceStarter.wasStarted());
    }

    public void testLogEventAnalyticsEnabled() {
        final EventLogger eventLogger = getEventLoggerWithAnalyticsEnabled();
        eventLogger.logEvent(TEST_EVENT_TYPE, TEST_EVENT_FIELDS);
        assertTrue(serviceStarter.wasStarted());
        assertEquals(TEST_EVENT_TYPE, getLoggedEvent().getEventType());
    }

    public void testLogEventDataAnalyticsDisabled() {
        final EventLogger eventLogger = getEventLoggerWithAnalyticsDisabled();
        eventLogger.logEvent(TEST_EVENT_TYPE, TEST_EVENT_FIELDS);
        assertFalse(serviceStarter.wasStarted());
    }
    
    public void testLogEventNotificationReceived() {
        final EventLogger eventLogger = getEventLoggerWithAnalyticsEnabled();
        eventLogger.logReceivedNotification(TEST_EVENT_RECEIPT_ID_VALUE);
        assertTrue(serviceStarter.wasStarted());
        assertEquals(EventLogger.PCF_PUSH_EVENT_TYPE_PUSH_NOTIFICATION_RECEIVED, getLoggedEvent().getEventType());
        assertEquals(TEST_EVENT_RECEIPT_ID_VALUE, getLoggedEvent().getReceiptId());
        assertEquals(TEST_EVENT_DEVICE_UUID_VALUE, getLoggedEvent().getDeviceUuid());
        assertNull(getLoggedEvent().getGeofenceId());
        assertNull(getLoggedEvent().getLocationId());
        assertNotNull(getLoggedEvent().getEventTime());
    }

    public void testLogEventNotificationOpened() {
        final EventLogger eventLogger = getEventLoggerWithAnalyticsEnabled();
        eventLogger.logOpenedNotification(TEST_EVENT_RECEIPT_ID_VALUE);
        assertTrue(serviceStarter.wasStarted());
        assertEquals(EventLogger.PCF_PUSH_EVENT_TYPE_PUSH_NOTIFICATION_OPENED, getLoggedEvent().getEventType());
        assertEquals(TEST_EVENT_RECEIPT_ID_VALUE, getLoggedEvent().getReceiptId());
        assertEquals(TEST_EVENT_DEVICE_UUID_VALUE, getLoggedEvent().getDeviceUuid());
        assertNull(getLoggedEvent().getGeofenceId());
        assertNull(getLoggedEvent().getLocationId());
        assertNotNull(getLoggedEvent().getEventTime());
    }

    public void testLogEventGeofenceTriggered() {
        final EventLogger eventLogger = getEventLoggerWithAnalyticsEnabled();
        eventLogger.logGeofenceTriggered("57", "1337");
        assertTrue(serviceStarter.wasStarted());
        assertEquals(EventLogger.PCF_PUSH_EVENT_TYPE_GEOFENCE_LOCATION_TRIGGERED, getLoggedEvent().getEventType());
        assertEquals(TEST_EVENT_DEVICE_UUID_VALUE, getLoggedEvent().getDeviceUuid());
        assertEquals("57", getLoggedEvent().getGeofenceId());
        assertEquals("1337", getLoggedEvent().getLocationId());
        assertNull(getLoggedEvent().getReceiptId());
        assertNotNull(getLoggedEvent().getEventTime());
    }

    private EventLogger getEventLoggerWithAnalyticsDisabled() {
        Pivotal.setProperties(getProperties(false));
        return new EventLogger(serviceStarter, preferencesProvider, getContext());
    }

    private EventLogger getEventLoggerWithAnalyticsEnabled() {
        Pivotal.setProperties(getProperties(true));
        return new EventLogger(serviceStarter, preferencesProvider, getContext());
    }

    private Event getLoggedEvent() {
        final Intent intent = serviceStarter.getStartedIntent();
        final EnqueueEventJob job = intent.getParcelableExtra(EventService.KEY_JOB);
        return job.getEvent();
    }

    private Properties getProperties(boolean areAnalyticsEnabled) {
        final Properties properties = new Properties();
        properties.setProperty(Pivotal.Keys.SERVICE_URL, "http://some.url");
        properties.setProperty(Pivotal.Keys.GCM_SENDER_ID, "fake_sender_id");
        properties.setProperty(Pivotal.Keys.PLATFORM_UUID, "fake_platform_uuid");
        properties.setProperty(Pivotal.Keys.PLATFORM_SECRET, "fake_platform_secret");
        properties.setProperty(Pivotal.Keys.ARE_ANALYTICS_ENABLED, Boolean.toString(areAnalyticsEnabled));
        return properties;
    }
}
