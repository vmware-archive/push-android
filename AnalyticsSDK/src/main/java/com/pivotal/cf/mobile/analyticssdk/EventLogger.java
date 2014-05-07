package com.pivotal.cf.mobile.analyticssdk;

import android.content.Context;
import android.content.Intent;

import com.pivotal.cf.mobile.analyticssdk.jobs.EnqueueEventJob;
import com.pivotal.cf.mobile.analyticssdk.model.events.Event;
import com.pivotal.cf.mobile.analyticssdk.service.EventService;
import com.pivotal.cf.mobile.common.prefs.AnalyticsPreferencesProvider;
import com.pivotal.cf.mobile.common.util.Logger;
import com.pivotal.cf.mobile.common.util.ServiceStarter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class EventLogger {

    public static final String EVENT_TYPE_ERROR = "event_error";
    public static final String ERROR_ID = "id";
    public static final String ERROR_MESSAGE = "message";
    public static final String EXCEPTION_DATA = "exception";
    public static final String EXCEPTION_NAME = "name";
    public static final String EXCEPTION_REASON = "reason";
    public static final String EXCEPTION_STACK_TRACE = "stack_trace";

    private static final int MAX_STACK_TRACE_SIZE = 2048;

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

    /**
     * Logs an error into the analytics database.
     *
     * @param errorId       An application-defined error ID that can be used to collate these exceptions in the
     *                      error database.
     * @param errorMessage  An application-define error message that can be used to provide context or meaning to
     *                      this exception in the error database.
     */
    public void logError(String errorId, String errorMessage) {
        final HashMap<String, Object> data = getErrorData(errorId, errorMessage);
        logEvent(EVENT_TYPE_ERROR, data);
    }

    /**
     * Logs an exception into the analytics database.
     *
     * @param errorId       An application-defined error ID that can be used to collate these exceptions in the
     *                      error database.
     * @param errorMessage  An application-define error message that can be used to provide context or meaning to
     *                      this exception in the error database.
     * @param throwable     The exception object to be logged.
     */
    public void logException(String errorId, String errorMessage, Throwable throwable) {
        final HashMap<String, Object> data = getErrorData(errorId, errorMessage);
        final HashMap<String, Object> exceptionData = new HashMap<String, Object>();
        data.put(EXCEPTION_DATA, exceptionData);
        exceptionData.put(EXCEPTION_NAME, throwable.getClass().getCanonicalName());
        exceptionData.put(EXCEPTION_REASON, throwable.getMessage());
        exceptionData.put(EXCEPTION_STACK_TRACE, getStackTrace(throwable));
        logEvent(EVENT_TYPE_ERROR, data);
    }

    private HashMap<String, Object> getErrorData(String errorId, String errorMessage) {
        final HashMap<String, Object> data = new HashMap<String, Object>();
        if (errorId != null) {
            data.put(ERROR_ID, errorId);
        }
        if (errorMessage != null) {
            data.put(ERROR_MESSAGE, errorMessage);
        }
        return data;
    }

    private Event getEvent(String eventType, HashMap<String, Object> data) {
        final Event event = new Event();
        event.setEventType(eventType);
        event.setEventId(getEventId());
        event.setTime(new Date());
        event.setData(data);
        event.setStatus(Event.Status.NOT_POSTED);
        return event;
    }

    private String getEventId() {
        return UUID.randomUUID().toString();
    }

    private boolean isAnalyticsEnabled() {
        return preferencesProvider.isAnalyticsEnabled();
    }

    private String getStackTrace(Throwable throwable) {
        final StringWriter writer = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(writer);
        throwable.printStackTrace(printWriter);
        printWriter.close();
        final String stackTrace = writer.toString();
        if (stackTrace.length() > MAX_STACK_TRACE_SIZE) {
            return stackTrace.substring(0, MAX_STACK_TRACE_SIZE);
        }
        return stackTrace;
    }
}
