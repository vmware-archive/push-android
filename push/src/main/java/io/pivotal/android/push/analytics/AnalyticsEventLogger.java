package io.pivotal.android.push.analytics;

import android.content.Context;
import android.content.Intent;

import io.pivotal.android.push.prefs.PushPreferences;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.pivotal.android.push.BuildConfig;
import io.pivotal.android.push.analytics.jobs.EnqueueAnalyticsEventJob;
import io.pivotal.android.push.analytics.jobs.SendAnalyticsEventsJob;
import io.pivotal.android.push.model.analytics.AnalyticsEvent;
import io.pivotal.android.push.service.AnalyticsEventService;
import io.pivotal.android.push.util.Logger;
import io.pivotal.android.push.util.ServiceStarter;

public class AnalyticsEventLogger {

    public static final String PCF_PUSH_EVENT_TYPE_PUSH_NOTIFICATION_RECEIVED = "pcf_push_event_type_push_notification_received";
    public static final String PCF_PUSH_EVENT_TYPE_PUSH_NOTIFICATION_OPENED = "pcf_push_event_type_push_notification_opened";
    public static final String PCF_PUSH_EVENT_TYPE_GEOFENCE_LOCATION_TRIGGERED = "pcf_push_event_type_geofence_location_triggered";
    public static final String PCF_PUSH_EVENT_TYPE_HEARTBEAT = "pcf_push_event_type_heartbeat";

    private Context context;
    private ServiceStarter serviceStarter;
    private PushPreferences preferencesProvider;

    public AnalyticsEventLogger(ServiceStarter serviceStarter, PushPreferences preferencesProvider, Context context) {
        verifyArguments(serviceStarter, preferencesProvider, context);
        saveArguments(serviceStarter, preferencesProvider, context);
    }

    private void verifyArguments(ServiceStarter serviceStarter, PushPreferences preferencesProvider, Context context) {
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

    private void saveArguments(ServiceStarter serviceStarter, PushPreferences preferencesProvider, Context context) {
        this.serviceStarter = serviceStarter;
        this.preferencesProvider = preferencesProvider;
        this.context = context;
    }

    public void logReceivedNotification(String receiptId) {
        Map<String, String> fields = new HashMap<>();
        fields.put("receiptId", receiptId);
        fields.put("deviceUuid", preferencesProvider.getPCFPushDeviceRegistrationId());
        logEvent(PCF_PUSH_EVENT_TYPE_PUSH_NOTIFICATION_RECEIVED, fields);
    }

    public void logOpenedNotification(String receiptId) {
        Map<String, String> fields = new HashMap<>();
        fields.put("receiptId", receiptId);
        fields.put("deviceUuid", preferencesProvider.getPCFPushDeviceRegistrationId());
        logEvent(PCF_PUSH_EVENT_TYPE_PUSH_NOTIFICATION_OPENED, fields);
    }

    public void logGeofenceTriggered(String geofenceId, String locationId) {
        Map<String, String> fields = new HashMap<>();
        fields.put("geofenceId", geofenceId);
        fields.put("locationId", locationId);
        fields.put("deviceUuid", preferencesProvider.getPCFPushDeviceRegistrationId());
        logEvent(PCF_PUSH_EVENT_TYPE_GEOFENCE_LOCATION_TRIGGERED, fields);
    }

    public void logReceivedHeartbeat(String receiptId) {
        final Map<String, String> fields = new HashMap<>();
        fields.put("receiptId", receiptId);
        fields.put("deviceUuid", preferencesProvider.getPCFPushDeviceRegistrationId());
        logEvent(PCF_PUSH_EVENT_TYPE_HEARTBEAT, fields);
    }

    public void logEvent(String eventType, Map<String, String> fields) {
        if (preferencesProvider.areAnalyticsEnabled()) {
            final AnalyticsEvent event = getEvent(eventType, fields);
            enqueueAnalyticsEventJob(event);
            enqueueSendEventsJob();
        } else {
            Logger.w("Event not logged. Analytics is either not set up or disabled.");
        }
    }

    private AnalyticsEvent getEvent(String eventType, Map<String, String> fields) {
        final AnalyticsEvent event = new AnalyticsEvent();
        event.setEventType(eventType);
        event.setReceiptId(fields.get("receiptId"));
        event.setEventTime(new Date());
        event.setDeviceUuid(fields.get("deviceUuid"));
        event.setGeofenceId(fields.get("geofenceId"));
        event.setLocationId(fields.get("locationId"));
        event.setSdkVersion(BuildConfig.VERSION_NAME);
        event.setPlatformType("android");
        event.setPlatformUuid(preferencesProvider.getPlatformUuid());
        event.setStatus(AnalyticsEvent.Status.NOT_POSTED);
        return event;
    }

    private void enqueueAnalyticsEventJob(AnalyticsEvent event) {
        Logger.i("Logging analytics event: " + event);
        final EnqueueAnalyticsEventJob job = new EnqueueAnalyticsEventJob(event);
        final Intent intent = AnalyticsEventService.getIntentToRunJob(context, job);
        serviceStarter.startService(context, intent);
    }

    private void enqueueSendEventsJob() {
        Logger.i("Enqueueing SendAnalyticsEventsJob.");
        final SendAnalyticsEventsJob job = new SendAnalyticsEventsJob();
        final Intent intent = AnalyticsEventService.getIntentToRunJob(context, job);
        serviceStarter.startService(context, intent);
    }
}