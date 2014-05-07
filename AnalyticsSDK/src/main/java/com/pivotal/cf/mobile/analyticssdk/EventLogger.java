package com.pivotal.cf.mobile.analyticssdk;

import android.content.Context;
import android.content.Intent;

import com.pivotal.cf.mobile.analyticssdk.jobs.EnqueueEventJob;
import com.pivotal.cf.mobile.analyticssdk.model.events.Event;
import com.pivotal.cf.mobile.analyticssdk.service.EventService;
import com.pivotal.cf.mobile.common.prefs.AnalyticsPreferencesProvider;
import com.pivotal.cf.mobile.common.util.Logger;
import com.pivotal.cf.mobile.common.util.ServiceStarter;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class EventLogger {

    private Context context;
    private ServiceStarter serviceStarter;
    private AnalyticsPreferencesProvider preferencesProvider;

    // The Event Logger can only be instantiated from the AnalyticsSDK class or unit tests.

    /* package */ EventLogger(ServiceStarter serviceStarter, AnalyticsPreferencesProvider preferencesProvider, Context context) {
        verifyArguments(serviceStarter, preferencesProvider, context);
        saveArguments(serviceStarter, preferencesProvider, context);
    }

    private void verifyArguments(ServiceStarter serviceStarter, AnalyticsPreferencesProvider preferencesProvider, Context context) {
        if (serviceStarter == null) {
            throw new IllegalArgumentException("serviceStarter may not be null");
        }
        if (preferencesProvider == null) {
            throw new IllegalArgumentException("preferencesProvider may not be null");
        }
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
    }

    private void saveArguments(ServiceStarter serviceStarter, AnalyticsPreferencesProvider preferencesProvider, Context context) {
        this.serviceStarter = serviceStarter;
        this.preferencesProvider = preferencesProvider;
        this.context = context;
    }

    /**
     * Logs an event into the analytics database with an empty data field.  This event will be posted to the analytics
     * server sometime in the near future.
     *
     * Does nothing if analytics is disabled.  You can enable analytics by calling the `AnalyticsSDK.setParameters`
     * method with the `isAnalyticsEnabled` field set to `true`.
     *
     * @param eventType  The type of the event being logged.
     */
    public void logEvent(String eventType) {
        logEvent(eventType, null);
    }

    /**
     * Logs an event into the analytics database, including optional data.  This event will be posted to the
     * analytics server sometime in the near future.
     *
     * Does nothing if analytics is disabled.  You can enable analytics by calling the `AnalyticsSDK.setParameters`
     * method with the `isAnalyticsEnabled` field set to `true`.
     *
     * @param eventType  The type of the event being logged.
     * @param data       An optional data dictionary to post with the event.  This data dictionary must be
     *                   serializable to JSON.  The Google GSON library is used to serialize data dictionaries.
     */
    public void logEvent(String eventType, HashMap<String, Object> data) {
        if (isAnalyticsEnabled()) {
            final Event event = getEvent(eventType, data);
            final EnqueueEventJob job = new EnqueueEventJob(event);
            final Intent intent = EventService.getIntentToRunJob(context, job);
            serviceStarter.startService(context, intent);
        } else {
            Logger.w("Event not logged. Analytics is either not set up or disabled.");
        }
    }

    private Event getEvent(String eventType, HashMap<String, Object> data) {
        final Event event = new Event();
        event.setEventType(eventType);
        event.setEventId(getEventId());
        event.setTime(new Date());
        event.setStatus(Event.Status.NOT_POSTED);
        event.setData(data);
        return event;
    }

    private String getEventId() {
        final String eventId = UUID.randomUUID().toString();
        return eventId;
    }

    private boolean isAnalyticsEnabled() {
        return preferencesProvider.isAnalyticsEnabled();
    }
}
