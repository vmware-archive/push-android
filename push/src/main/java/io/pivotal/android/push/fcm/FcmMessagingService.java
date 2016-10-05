package io.pivotal.android.push.fcm;

import android.content.Intent;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import io.pivotal.android.push.analytics.AnalyticsEventLogger;
import io.pivotal.android.push.prefs.PushPreferencesProvider;
import io.pivotal.android.push.prefs.PushPreferencesProviderImpl;
import io.pivotal.android.push.service.GeofenceService;
import io.pivotal.android.push.util.Logger;
import io.pivotal.android.push.util.ServiceStarter;
import io.pivotal.android.push.util.ServiceStarterImpl;

public class FcmMessagingService extends FirebaseMessagingService {

    public static final String KEY_RECEIPT_ID = "receiptId";
    public static final String KEY_HEARTBEAT = "pcf.push.heartbeat.sentToDeviceAt";
    private AnalyticsEventLogger eventLogger = null;
    private PushPreferencesProvider preferences = null;

    // Intended to be overridden by application
    public void onMessageNotificationReceived(final RemoteMessage.Notification notification, final Map<String, String> data) {
    }

    // Intended to be overridden by application
    public void onMessageDataReceived(final Map<String, String> data) {
    }

    // Intended to be overridden by application
    public void onReceiveHeartbeat(final Map<String, String> heartbeat) {
    }

    // Intended to be overridden by application
    public void onReceiveDeletedMessages(){}

    // Intended to be overridden by application
    public void onReceiveMessageSendError(final String messageId, final Exception exception) {}

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public final void onMessageReceived(final RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        initializeDependencies();

        handleReceivedMessage(remoteMessage.getNotification(), new HashMap<>(remoteMessage.getData()));
    }

    @Override
    public final void onDeletedMessages() {
        super.onDeletedMessages();
        Logger.i("FcmMessagingService has received a DELETED push message.");
        onReceiveDeletedMessages();
    }

    @Override
    public final void onSendError(final String messageId, final Exception exception) {
        super.onSendError(messageId, exception);
        Logger.i("FcmMessagingService has received an ERROR push message.");
        onReceiveMessageSendError(messageId, exception);
    }

    void setPushPreferencesProvider(PushPreferencesProvider preferences) {
        this.preferences = preferences;
    }

    void setEventLogger(AnalyticsEventLogger eventLogger) {
        this.eventLogger = eventLogger;
    }

    void handleReceivedMessage(final RemoteMessage.Notification notificationMessage, final HashMap<String, String> dataMessage) {
        if (notificationMessage != null) {
            Logger.i("FcmMessagingService has received a notification push message.");
            onMessageNotificationReceived(notificationMessage, dataMessage);
            enqueueMessageNotificationReceivedEvent(dataMessage);
        } else if (remoteMessageHasDataPayload(dataMessage)) {
            if (dataMessage.containsKey(GeofenceService.GEOFENCE_AVAILABLE)) {
                Logger.i("FcmMessagingService has received a geofence push message.");
                handleGeofenceMessage(dataMessage);
            } else if (dataMessage.containsKey(KEY_HEARTBEAT)) {
                Logger.i("FcmMessagingService has received a heartbeat push message.");
                onReceiveHeartbeat(dataMessage);
                enqueueHeartbeatReceivedEvent(dataMessage);
            } else {
                Logger.i("FcmMessagingService has received a data push message.");
                onMessageDataReceived(dataMessage);

                if (notificationMessage == null) {
                    enqueueMessageNotificationReceivedEvent(dataMessage);
                }
            }
        }
    }

    private boolean remoteMessageHasDataPayload(final Map<String, String> dataMessage) {
        return remoteMessageDataPayloadOnlyHasReceiptId(dataMessage) && !dataMessage.isEmpty();
    }

    private boolean remoteMessageDataPayloadOnlyHasReceiptId(final Map<String, String> dataMessage) {
        return dataMessage.size() != 1 || !dataMessage.containsKey(KEY_RECEIPT_ID);
    }

    private void enqueueMessageNotificationReceivedEvent(final Map<String, String> messageData) {
        final String receiptId = messageData.get(KEY_RECEIPT_ID);
        if (receiptId != null) {
            eventLogger.logReceivedNotification(receiptId);
        } else {
            Logger.w("Note: notification has no receiptId. No analytics event will be logged for receiving this notification.");
        }
    }

    private void enqueueHeartbeatReceivedEvent(final Map<String, String> heartbeat) {
        final String receiptId = heartbeat.get(KEY_RECEIPT_ID);
        if (receiptId != null) {
            eventLogger.logReceivedHeartbeat(receiptId);
        } else {
            Logger.w("Note: heartbeat has no receiptId. No analytics event will be logged for receiving this notification.");
        }
    }

    private void initializeDependencies() {
        if (preferences == null) {
            preferences = new PushPreferencesProviderImpl(this);
        }

        if (eventLogger == null) {
            final ServiceStarter serviceStarter = new ServiceStarterImpl();
            eventLogger = new AnalyticsEventLogger(serviceStarter, preferences, this);
        }
    }

    private void handleGeofenceMessage(final HashMap<String, String> dataMessage) {
        final Intent geofenceServiceIntent = new Intent(getBaseContext(), GeofenceService.class);

        for (Entry<String, String> dataKeyValue : dataMessage.entrySet()) {
            geofenceServiceIntent.putExtra(dataKeyValue.getKey(), dataKeyValue.getValue());
        }
        getBaseContext().startService(geofenceServiceIntent);
    }
}