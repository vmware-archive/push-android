package io.pivotal.android.push.analytics;

import android.content.ComponentName;
import android.content.Intent;
import android.test.AndroidTestCase;

import java.util.HashMap;
import java.util.Properties;

import io.pivotal.android.push.BuildConfig;
import io.pivotal.android.push.analytics.jobs.BaseJob;
import io.pivotal.android.push.analytics.jobs.EnqueueAnalyticsEventJob;
import io.pivotal.android.push.analytics.jobs.SendAnalyticsEventsJob;
import io.pivotal.android.push.model.analytics.AnalyticsEvent;
import io.pivotal.android.push.prefs.FakePushPreferencesProvider;
import io.pivotal.android.push.prefs.Pivotal;
import io.pivotal.android.push.service.AnalyticsEventService;
import io.pivotal.android.push.util.FakeServiceStarter;

public class AnalyticsEventLoggerTest extends AndroidTestCase {

    private static final String TEST_EVENT_TYPE = "TEST_EVENT_TYPE";
    private static final String TEST_EVENT_RECEIPT_ID = "receiptId";
    private static final String TEST_EVENT_DEVICE_UUID = "deviceUuid";
    private static final String TEST_EVENT_GEOFENCE_ID = "geofenceId";
    private static final String TEST_EVENT_LOCATION_ID = "locationId";
    private static final String TEST_EVENT_RECEIPT_ID_VALUE = "SOME_RECEIPT_ID";
    private static final String TEST_EVENT_DEVICE_UUID_VALUE = "SOME_DEVICE_UUID";
    private static final String TEST_EVENT_GEOFENCE_ID_VALUE = "SOME_GEOFENCE_ID";
    private static final String TEST_EVENT_LOCATION_ID_VALUE = "SOME_LOCATION_ID";
    private static final String TEST_EVENT_PLATFORM_UUID_VALUE = "SOME-PLATFORM-UUID-VALUE";
    private static HashMap<String, String> TEST_EVENT_FIELDS;

    private FakeServiceStarter serviceStarter;
    private FakePushPreferencesProvider preferencesProvider;

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
        serviceStarter.setReturnedComponentName(new ComponentName(getContext(), AnalyticsEventService.class));
        preferencesProvider = new FakePushPreferencesProvider(null, TEST_EVENT_DEVICE_UUID_VALUE, 0, null, TEST_EVENT_PLATFORM_UUID_VALUE, null, null, null, null, null, null, 0, false);
        preferencesProvider.setAreAnalyticsEnabled(true);
    }

    public void testRequiresServiceStarter() {
        try {
            new AnalyticsEventLogger(null, preferencesProvider, getContext());
            fail("should have failed");
        } catch (IllegalArgumentException e) {
            // should have thrown
        }
    }

    public void testRequiresPushParameters() {
        try {
            new AnalyticsEventLogger(serviceStarter, null, getContext());
            fail("should have failed");
        } catch (IllegalArgumentException e) {
            // should have thrown
        }
    }

    public void testRequiresContext() {
        try {
            new AnalyticsEventLogger(serviceStarter, preferencesProvider, null);
            fail("should have failed");
        } catch (IllegalArgumentException e) {
            // should have thrown
        }
    }

    public void testLogEventAnalyticsDisabled() {
        final AnalyticsEventLogger eventLogger = getEventLoggerWithAnalyticsDisabled();
        eventLogger.logEvent(TEST_EVENT_TYPE, TEST_EVENT_FIELDS);
        assertFalse(serviceStarter.wasStarted());
    }

    public void testLogEventAnalyticsEnabled() {
        final AnalyticsEventLogger eventLogger = getEventLoggerWithAnalyticsEnabled();
        eventLogger.logEvent(TEST_EVENT_TYPE, TEST_EVENT_FIELDS);
        assertTrue(serviceStarter.wasStarted());
        assertEquals(TEST_EVENT_TYPE, getLoggedEvent(0).getEventType());
    }

    public void testLogEventDataAnalyticsDisabled() {
        final AnalyticsEventLogger eventLogger = getEventLoggerWithAnalyticsDisabled();
        eventLogger.logEvent(TEST_EVENT_TYPE, TEST_EVENT_FIELDS);
        assertFalse(serviceStarter.wasStarted());
    }

    public void testLogEventNotificationReceived() {
        final AnalyticsEventLogger eventLogger = getEventLoggerWithAnalyticsEnabled();
        eventLogger.logReceivedNotification(TEST_EVENT_RECEIPT_ID_VALUE);
        assertTrue(serviceStarter.wasStarted());
        assertNumberOfServiceIntents(2);
        assertEquals(AnalyticsEventLogger.PCF_PUSH_EVENT_TYPE_PUSH_NOTIFICATION_RECEIVED, getLoggedEvent(0).getEventType());
        assertEquals(TEST_EVENT_RECEIPT_ID_VALUE, getLoggedEvent(0).getReceiptId());
        assertEquals(TEST_EVENT_DEVICE_UUID_VALUE, getLoggedEvent(0).getDeviceUuid());
        assertEquals(BuildConfig.VERSION_NAME, getLoggedEvent(0).getSdkVersion());
        assertEquals("android", getLoggedEvent(0).getPlatformType());
        assertEquals(TEST_EVENT_PLATFORM_UUID_VALUE, getLoggedEvent(0).getPlatformUuid());
        assertNull(getLoggedEvent(0).getGeofenceId());
        assertNull(getLoggedEvent(0).getLocationId());
        assertNotNull(getLoggedEvent(0).getEventTime());
        assertTrue(getJob(0) instanceof EnqueueAnalyticsEventJob);
        assertTrue(getJob(1) instanceof SendAnalyticsEventsJob);
    }

    public void testLogEventNotificationOpened() {
        final AnalyticsEventLogger eventLogger = getEventLoggerWithAnalyticsEnabled();
        eventLogger.logOpenedNotification(TEST_EVENT_RECEIPT_ID_VALUE);
        assertTrue(serviceStarter.wasStarted());
        assertNumberOfServiceIntents(2);
        assertEquals(AnalyticsEventLogger.PCF_PUSH_EVENT_TYPE_PUSH_NOTIFICATION_OPENED, getLoggedEvent(0).getEventType());
        assertEquals(TEST_EVENT_RECEIPT_ID_VALUE, getLoggedEvent(0).getReceiptId());
        assertEquals(TEST_EVENT_DEVICE_UUID_VALUE, getLoggedEvent(0).getDeviceUuid());
        assertEquals(BuildConfig.VERSION_NAME, getLoggedEvent(0).getSdkVersion());
        assertEquals("android", getLoggedEvent(0).getPlatformType());
        assertEquals(TEST_EVENT_PLATFORM_UUID_VALUE, getLoggedEvent(0).getPlatformUuid());
        assertNull(getLoggedEvent(0).getGeofenceId());
        assertNull(getLoggedEvent(0).getLocationId());
        assertNotNull(getLoggedEvent(0).getEventTime());
        assertTrue(getJob(0) instanceof EnqueueAnalyticsEventJob);
        assertTrue(getJob(1) instanceof SendAnalyticsEventsJob);
    }

    public void testLogEventGeofenceTriggered() {
        final AnalyticsEventLogger eventLogger = getEventLoggerWithAnalyticsEnabled();
        eventLogger.logGeofenceTriggered("57", "1337");
        assertTrue(serviceStarter.wasStarted());
        assertEquals(AnalyticsEventLogger.PCF_PUSH_EVENT_TYPE_GEOFENCE_LOCATION_TRIGGERED, getLoggedEvent(0).getEventType());
        assertNumberOfServiceIntents(2);
        assertEquals(TEST_EVENT_DEVICE_UUID_VALUE, getLoggedEvent(0).getDeviceUuid());
        assertEquals("57", getLoggedEvent(0).getGeofenceId());
        assertEquals("1337", getLoggedEvent(0).getLocationId());
        assertEquals(BuildConfig.VERSION_NAME, getLoggedEvent(0).getSdkVersion());
        assertEquals("android", getLoggedEvent(0).getPlatformType());
        assertEquals(TEST_EVENT_PLATFORM_UUID_VALUE, getLoggedEvent(0).getPlatformUuid());
        assertNull(getLoggedEvent(0).getReceiptId());
        assertNotNull(getLoggedEvent(0).getEventTime());
        assertTrue(getJob(0) instanceof EnqueueAnalyticsEventJob);
        assertTrue(getJob(1) instanceof SendAnalyticsEventsJob);
    }

    public void testLogHeartbeatEvent() {
        final AnalyticsEventLogger eventLogger = getEventLoggerWithAnalyticsEnabled();
        eventLogger.logReceivedHeartbeat(TEST_EVENT_RECEIPT_ID_VALUE);
        assertTrue(serviceStarter.wasStarted());
        assertNumberOfServiceIntents(2);
        assertEquals(AnalyticsEventLogger.PCF_PUSH_EVENT_TYPE_HEARTBEAT, getLoggedEvent(0).getEventType());
        assertEquals(TEST_EVENT_RECEIPT_ID_VALUE, getLoggedEvent(0).getReceiptId());
        assertEquals(TEST_EVENT_DEVICE_UUID_VALUE, getLoggedEvent(0).getDeviceUuid());
        assertEquals(BuildConfig.VERSION_NAME, getLoggedEvent(0).getSdkVersion());
        assertEquals("android", getLoggedEvent(0).getPlatformType());
        assertEquals(TEST_EVENT_PLATFORM_UUID_VALUE, getLoggedEvent(0).getPlatformUuid());
        assertNull(getLoggedEvent(0).getGeofenceId());
        assertNull(getLoggedEvent(0).getLocationId());
        assertNotNull(getLoggedEvent(0).getEventTime());
        assertTrue(getJob(0) instanceof EnqueueAnalyticsEventJob);
        assertTrue(getJob(1) instanceof SendAnalyticsEventsJob);
    }

    private void assertNumberOfServiceIntents(int expected) {
        assertEquals(expected, serviceStarter.getStartedIntents().size());
    }

    private AnalyticsEventLogger getEventLoggerWithAnalyticsDisabled() {
        preferencesProvider.setAreAnalyticsEnabled(false);
        return new AnalyticsEventLogger(serviceStarter, preferencesProvider, getContext());
    }

    private AnalyticsEventLogger getEventLoggerWithAnalyticsEnabled() {
        Pivotal.setProperties(getProperties(true));
        return new AnalyticsEventLogger(serviceStarter, preferencesProvider, getContext());
    }

    private AnalyticsEvent getLoggedEvent(int intentNumber) {
        final EnqueueAnalyticsEventJob job = getJob(intentNumber);
        return job.getEvent();
    }

    private <T extends BaseJob> T getJob(int intentNumber) {
        final Intent intent = getServiceIntent(intentNumber);
        return intent.getParcelableExtra(AnalyticsEventService.KEY_JOB);
    }

    private Intent getServiceIntent(int intentNumber) {
        return serviceStarter.getStartedIntents().get(intentNumber);
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
