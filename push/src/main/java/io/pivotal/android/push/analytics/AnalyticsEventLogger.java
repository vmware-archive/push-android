package io.pivotal.android.push.analytics;

import android.content.Context;
import android.content.Intent;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.pivotal.android.push.analytics.jobs.EnqueueEventJob;
import io.pivotal.android.push.model.analytics.AnalyticsEvent;
import io.pivotal.android.push.prefs.Pivotal;
import io.pivotal.android.push.prefs.PushPreferencesProvider;
import io.pivotal.android.push.service.AnalyticsEventService;
import io.pivotal.android.push.util.Logger;
import io.pivotal.android.push.util.ServiceStarter;

public class AnalyticsEventLogger {

    public static final String PCF_PUSH_EVENT_TYPE_PUSH_NOTIFICATION_RECEIVED = "pcf_push_event_type_push_notification_received";
    public static final String PCF_PUSH_EVENT_TYPE_PUSH_NOTIFICATION_OPENED = "pcf_push_event_type_push_notification_opened";
    public static final String PCF_PUSH_EVENT_TYPE_GEOFENCE_LOCATION_TRIGGERED = "pcf_push_event_type_geofence_location_triggered";

    private Context context;
    private ServiceStarter serviceStarter;
    private PushPreferencesProvider preferencesProvider;

    public AnalyticsEventLogger(ServiceStarter serviceStarter, PushPreferencesProvider preferencesProvider, Context context) {
        verifyArguments(serviceStarter, preferencesProvider, context);
        saveArguments(serviceStarter, preferencesProvider, context);
    }

    private void verifyArguments(ServiceStarter serviceStarter, PushPreferencesProvider preferencesProvider, Context context) {
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

    private void saveArguments(ServiceStarter serviceStarter, PushPreferencesProvider preferencesProvider, Context context) {
        this.serviceStarter = serviceStarter;
        this.preferencesProvider = preferencesProvider;
        this.context = context;
    }

    public void logEvent(String eventType) {
        logEvent(eventType, null);
    }

    public void logEvent(String eventType, Map<String, String> fields) {
        if (Pivotal.getAreAnalyticsEnabled(context)) {
            final AnalyticsEvent event = getEvent(eventType, fields);
            final EnqueueEventJob job = new EnqueueEventJob(event);
            final Intent intent = AnalyticsEventService.getIntentToRunJob(context, job);
            serviceStarter.startService(context, intent);
        } else {
            Logger.w("Event not logged. Analytics is either not set up or disabled.");
        }
    }

    public void logReceivedNotification(String receiptId) {
        Map<String, String> fields = new HashMap<>();
        fields.put("receiptId", receiptId);
        fields.put("deviceUuid", preferencesProvider.getPCFPushDeviceRegistrationId());
        logEvent(PCF_PUSH_EVENT_TYPE_PUSH_NOTIFICATION_RECEIVED, fields);
        Logger.i("Logging received remote notification for receiptId: " + receiptId);
    }

    public void logOpenedNotification(String receiptId) {
        Map<String, String> fields = new HashMap<>();
        fields.put("receiptId", receiptId);
        fields.put("deviceUuid", preferencesProvider.getPCFPushDeviceRegistrationId());
        logEvent(PCF_PUSH_EVENT_TYPE_PUSH_NOTIFICATION_OPENED, fields);
        Logger.i("Logging opened remote notification for receiptId: " + receiptId);
    }

    public void logGeofenceTriggered(String geofenceId, String locationId) {
        Map<String, String> fields = new HashMap<>();
        fields.put("geofenceId", geofenceId);
        fields.put("locationId", locationId);
        fields.put("deviceUuid", preferencesProvider.getPCFPushDeviceRegistrationId());
        logEvent(PCF_PUSH_EVENT_TYPE_GEOFENCE_LOCATION_TRIGGERED, fields);
        Logger.i("Logging triggered geofenceId " + geofenceId + " and locationId " + locationId);
    }

    private AnalyticsEvent getEvent(String eventType, Map<String, String> fields) {
        final AnalyticsEvent event = new AnalyticsEvent();
        event.setEventType(eventType);
        event.setReceiptId(fields.get("receiptId"));
        event.setEventTime(new Date());
        event.setDeviceUuid(fields.get("deviceUuid"));
        event.setGeofenceId(fields.get("geofenceId"));
        event.setLocationId(fields.get("locationId"));
        event.setStatus(AnalyticsEvent.Status.NOT_POSTED);
        return event;
    }
}