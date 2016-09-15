package io.pivotal.android.push.fcm;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import io.pivotal.android.push.analytics.AnalyticsEventLogger;
import io.pivotal.android.push.prefs.PushPreferencesProvider;
import io.pivotal.android.push.prefs.PushPreferencesProviderImpl;
import io.pivotal.android.push.util.Logger;
import io.pivotal.android.push.util.ServiceStarter;
import io.pivotal.android.push.util.ServiceStarterImpl;

public class FcmMessagingService extends FirebaseMessagingService {

    public static final String KEY_RECEIPT_ID = "receiptId";
    public static final String KEY_HEARTBEAT = "pcf.push.heartbeat.sentToDeviceAt";
    private AnalyticsEventLogger eventLogger = null;
    private PushPreferencesProvider preferences = null;

    // Intended to be overridden by application
    public void onMessageNotificationReceived(final RemoteMessage.Notification notification) {
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
    public void onReceiveMessageSendError(String messageId, Exception exception) {}

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public final void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        initializeDependencies();

        if (remoteMessage.getNotification() != null) {
            Logger.i("FcmMessagingService has received a notification push message.");
            onMessageNotificationReceived(remoteMessage.getNotification());
            enqueueMessageNotificationReceivedEvent(remoteMessage.getData());
        }

        if (remoteMessageHasDataPayload(remoteMessage)) {
            if (remoteMessage.getData().containsKey(KEY_HEARTBEAT)) {
                Logger.i("FcmMessagingService has received a heartbeat push message.");
                onReceiveHeartbeat(remoteMessage.getData());
                enqueueHeartbeatReceivedEvent(remoteMessage.getData());
            } else {
                Logger.i("FcmMessagingService has received a data push message.");
                onMessageDataReceived(remoteMessage.getData());

                if (remoteMessage.getNotification() == null) {
                    enqueueMessageNotificationReceivedEvent(remoteMessage.getData());
                }
            }
        }

    }

    @Override
    public final void onDeletedMessages() {
        super.onDeletedMessages();
        Logger.i("FcmMessagingService has received a DELETED push message.");
        onReceiveDeletedMessages();
    }

    @Override
    public final void onSendError(String messageId, Exception exception) {
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

    private boolean remoteMessageHasDataPayload(final RemoteMessage remoteMessage) {
        return remoteMessageDataPayloadOnlyHasReceiptId(remoteMessage) && !remoteMessage.getData().isEmpty();
    }

    private boolean remoteMessageDataPayloadOnlyHasReceiptId(final RemoteMessage remoteMessage) {
        return remoteMessage.getData().size() != 1 || !remoteMessage.getData().containsKey(KEY_RECEIPT_ID);
    }

    private void enqueueMessageNotificationReceivedEvent(Map<String, String> messageData) {
        final String receiptId = messageData.get(KEY_RECEIPT_ID);
        if (receiptId != null) {
            eventLogger.logReceivedNotification(receiptId);
        } else {
            Logger.w("Note: notification has no receiptId. No analytics event will be logged for receiving this notification.");
        }
    }

    private void enqueueHeartbeatReceivedEvent(Map<String, String> heartbeat) {
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
}