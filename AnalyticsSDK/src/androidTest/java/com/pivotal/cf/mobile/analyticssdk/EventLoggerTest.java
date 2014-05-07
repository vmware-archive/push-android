package com.pivotal.cf.mobile.analyticssdk;

import android.content.ComponentName;
import android.content.Intent;
import android.test.AndroidTestCase;

import com.pivotal.cf.mobile.analyticssdk.jobs.EnqueueEventJob;
import com.pivotal.cf.mobile.analyticssdk.model.events.Event;
import com.pivotal.cf.mobile.analyticssdk.service.EventService;
import com.pivotal.cf.mobile.common.test.prefs.FakeAnalyticsPreferencesProvider;
import com.pivotal.cf.mobile.common.test.util.FakeServiceStarter;

import java.util.HashMap;

public class EventLoggerTest extends AndroidTestCase {

    private static final String TEST_EVENT_TYPE = "TEST_EVENT_TYPE";
    private static final String TEST_EVENT_DATA_KEY = "SOME_KEY";
    private static final String TEST_EVENT_DATA_VALUE = "SOME_VALUE";
    private static HashMap<String, Object> TEST_EVENT_DATA;

    private FakeServiceStarter serviceStarter;
    private FakeAnalyticsPreferencesProvider preferencesProvider;

    static {
        TEST_EVENT_DATA = new HashMap<String, Object>();
        TEST_EVENT_DATA.put(TEST_EVENT_DATA_KEY, TEST_EVENT_DATA_VALUE);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        serviceStarter = new FakeServiceStarter();
        serviceStarter.setReturnedComponentName(new ComponentName(getContext(), EventService.class));
        preferencesProvider = new FakeAnalyticsPreferencesProvider(false, null);
    }

    public void testRequiresServiceStarter() {
        try {
            new EventLogger(null, preferencesProvider, getContext());
            fail("should have failed");
        } catch (IllegalArgumentException e) {
            // should have thrown
        }
    }

    public void testRequiresPreferencesProvider() {
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
        eventLogger.logEvent(TEST_EVENT_TYPE);
        assertFalse(serviceStarter.wasStarted());
    }

    public void testLogEventAnalyticsEnabled() {
        final EventLogger eventLogger = getEventLoggerWithAnalyticsEnabled();
        eventLogger.logEvent(TEST_EVENT_TYPE);
        assertTrue(serviceStarter.wasStarted());
        assertEquals(TEST_EVENT_TYPE, getLoggedEvent().getEventType());
        assertNull(getLoggedEvent().getData());
    }

    public void testLogEventDataAnalyticsDisabled() {
        final EventLogger eventLogger = getEventLoggerWithAnalyticsDisabled();
        eventLogger.logEvent(TEST_EVENT_TYPE, TEST_EVENT_DATA);
        assertFalse(serviceStarter.wasStarted());
    }

    public void testLogEventDataAnalyticsEnabled() {
        final EventLogger eventLogger = getEventLoggerWithAnalyticsEnabled();
        eventLogger.logEvent(TEST_EVENT_TYPE, TEST_EVENT_DATA);
        assertTrue(serviceStarter.wasStarted());
        assertEquals(TEST_EVENT_TYPE, getLoggedEvent().getEventType());
        assertTrue(getLoggedEvent().getData().containsKey(TEST_EVENT_DATA_KEY));
        assertEquals(TEST_EVENT_DATA_VALUE, getLoggedEvent().getData().get(TEST_EVENT_DATA_KEY));
    }

    private EventLogger getEventLoggerWithAnalyticsDisabled() {
        preferencesProvider.setIsAnalyticsEnabled(false);
        return new EventLogger(serviceStarter, preferencesProvider, getContext());
    }

    private EventLogger getEventLoggerWithAnalyticsEnabled() {
        preferencesProvider.setIsAnalyticsEnabled(true);
        return new EventLogger(serviceStarter, preferencesProvider, getContext());
    }

    private Event getLoggedEvent() {
        final Intent intent = serviceStarter.getStartedIntent();
        final EnqueueEventJob job = intent.getParcelableExtra(EventService.KEY_JOB);
        return job.getEvent();
    }
}
