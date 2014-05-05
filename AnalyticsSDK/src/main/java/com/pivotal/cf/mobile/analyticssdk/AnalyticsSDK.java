package com.pivotal.cf.mobile.analyticssdk;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.pivotal.cf.mobile.analyticssdk.jobs.EnqueueEventJob;
import com.pivotal.cf.mobile.analyticssdk.jobs.PrepareDatabaseJob;
import com.pivotal.cf.mobile.analyticssdk.model.events.Event;
import com.pivotal.cf.mobile.analyticssdk.service.EventService;
import com.pivotal.cf.mobile.common.util.Logger;

import java.util.Date;
import java.util.UUID;

public class AnalyticsSDK {

    private static AnalyticsSDK instance;
    private String eventId;

    /**
     * Initializes the Analytics SDK.
     *
     * @param context some context
     * @return an instance of the Analytics SDK
     */
    public synchronized static AnalyticsSDK init(Context context) {
        if (instance == null) {
            instance = new AnalyticsSDK(context);
        }
        return instance;
    }

    private Context context;

    private AnalyticsSDK(Context context) {
        verifyArguments(context);
        saveArguments(context);

        if (!Logger.isSetup()) {
            Logger.setup(context);
        }

        cleanupDatabase();
    }

    private void verifyArguments(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
    }

    private void saveArguments(Context context) {
        if (!(context instanceof Application)) {
            this.context = context.getApplicationContext();
        } else {
            this.context = context;
        }
    }

    private void cleanupDatabase() {
        final PrepareDatabaseJob job = new PrepareDatabaseJob();
        final Intent intent = EventService.getIntentToRunJob(context, job);
        context.startService(intent);
    }

    /**
     * Logs an event into the analytics database.  This event will be posted to the analytics
     * server sometime in the near future.
     *
     * @param eventType The type of the event being logged.
     */
    public void logEvent(String eventType) {
        final Event event = getEvent(eventType);
        final EnqueueEventJob job = new EnqueueEventJob(event);
        final Intent intent = EventService.getIntentToRunJob(context, job);
        context.startService(intent);
    }

    private Event getEvent(String eventType) {
        final Event event = new Event();
        event.setEventType(eventType);
        event.setEventId(getEventId());
        event.setTime(new Date());
        event.setStatus(Event.Status.NOT_POSTED);
        return event;
    }

    public String getEventId() {
        final String eventId = UUID.randomUUID().toString();
        return eventId;
    }
}
