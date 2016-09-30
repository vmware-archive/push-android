package io.pivotal.android.push.fcm;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.test.AndroidTestCase;

import com.google.firebase.messaging.RemoteMessage;

import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.HashMap;
import java.util.Map;

import io.pivotal.android.push.analytics.AnalyticsEventLogger;
import io.pivotal.android.push.prefs.PushPreferencesProvider;
import io.pivotal.android.push.service.GeofenceService;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FcmMessagingServiceTest extends AndroidTestCase {

    private final HashMap<String, String> NO_DATA = new HashMap<>();
    private final RemoteMessage.Notification NO_NOTIFICATION = null;
    private final HashMap<String, String> RECEIPT_DATA = new HashMap<>();

    private TestFcmMessagingService fcmMessagingService;
    private AnalyticsEventLogger eventLogger;
    private ArgumentCaptor<Intent> intentCaptor;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("dexmaker.dexcache", mContext.getCacheDir().getPath());
        eventLogger = mock(AnalyticsEventLogger.class);
        final PushPreferencesProvider pushPreferencesProvider = mock(PushPreferencesProvider.class);

        fcmMessagingService = new TestFcmMessagingService();
        fcmMessagingService.setEventLogger(eventLogger);
        fcmMessagingService.setPushPreferencesProvider(pushPreferencesProvider);

        RECEIPT_DATA.put(FcmMessagingService.KEY_RECEIPT_ID, "some receipt id");

        // Context setup for Geofences
        final Context context = mock(Context.class);
        fcmMessagingService.attachBaseContext(context);

        intentCaptor = ArgumentCaptor.forClass(Intent.class);

        doAnswer(new Answer<ComponentName>() {
            @Override
            public ComponentName answer(InvocationOnMock invocation) throws Throwable {
                Intent service = (Intent) invocation.getArguments()[0];
                return service.getComponent();
            }
        }).when(context).startService(intentCaptor.capture());
        when(context.getPackageName()).thenReturn("io.pivotal.android.push.fcm");
    }

    public void testMessageTypeNotification() {
        final RemoteMessage.Notification notification = mock(RemoteMessage.Notification.class);

        fcmMessagingService.handleReceivedMessage(notification, RECEIPT_DATA);

        fcmMessagingService.assertDeletedMessagesCalled(false);
        fcmMessagingService.assertErrorMessageReceived(null, null);
        fcmMessagingService.assertMessageDataReceived(NO_DATA);
        fcmMessagingService.assertMessageNotificationReceived(notification, RECEIPT_DATA);

        verify(eventLogger).logReceivedNotification(anyString());
        verify(eventLogger, never()).logReceivedHeartbeat(anyString());
    }

    public void testMessageTypeData() {
        final HashMap<String, String> messageData = new HashMap<>();
        messageData.put("SomePayloadKey", "SomePayloadValue");
        messageData.put(FcmMessagingService.KEY_RECEIPT_ID, "some receipt id");

        fcmMessagingService.handleReceivedMessage(NO_NOTIFICATION, messageData);

        fcmMessagingService.assertDeletedMessagesCalled(false);
        fcmMessagingService.assertErrorMessageReceived(null, null);
        fcmMessagingService.assertMessageDataReceived(messageData);
        fcmMessagingService.assertMessageNotificationReceived(NO_NOTIFICATION, NO_DATA);

        verify(eventLogger).logReceivedNotification(anyString());
        verify(eventLogger, never()).logReceivedHeartbeat(anyString());
    }

    public void testMessageTypeNotificationAndData() {
        final RemoteMessage.Notification notification = mock(RemoteMessage.Notification.class);
        final HashMap<String, String> messageData = new HashMap<>();
        messageData.put("SomePayloadKey", "SomePayloadValue");
        messageData.put(FcmMessagingService.KEY_RECEIPT_ID, "some receipt id");

        fcmMessagingService.handleReceivedMessage(notification, messageData);

        fcmMessagingService.assertDeletedMessagesCalled(false);
        fcmMessagingService.assertErrorMessageReceived(null, null);
        fcmMessagingService.assertMessageDataReceived(NO_DATA);
        fcmMessagingService.assertMessageNotificationReceived(notification, messageData);

        verify(eventLogger).logReceivedNotification(anyString());
        verify(eventLogger, never()).logReceivedHeartbeat(anyString());
    }

    public void testMessageTypeHeartbeat() {
        final HashMap<String, String> messageData = new HashMap<>();
        messageData.put(FcmMessagingService.KEY_RECEIPT_ID, "some receipt id");
        messageData.put(FcmMessagingService.KEY_HEARTBEAT, "heartbeat key");

        fcmMessagingService.handleReceivedMessage(NO_NOTIFICATION, messageData);

        fcmMessagingService.assertDeletedMessagesCalled(false);
        fcmMessagingService.assertErrorMessageReceived(null, null);
        fcmMessagingService.assertMessageDataReceived(NO_DATA);
        fcmMessagingService.assertMessageNotificationReceived(NO_NOTIFICATION, NO_DATA);
        fcmMessagingService.assertHeartbeatReceived(messageData);

        verify(eventLogger, never()).logReceivedNotification(anyString());
        verify(eventLogger).logReceivedHeartbeat(anyString());
    }

    public void testReceiveDeleteMessage() {
        fcmMessagingService.onReceiveDeletedMessages();

        fcmMessagingService.assertDeletedMessagesCalled(true);
        fcmMessagingService.assertErrorMessageReceived(null, null);
        fcmMessagingService.assertMessageDataReceived(NO_DATA);
        fcmMessagingService.assertMessageNotificationReceived(NO_NOTIFICATION, NO_DATA);
        fcmMessagingService.assertHeartbeatReceived(NO_DATA);

        verify(eventLogger, never()).logReceivedNotification(anyString());
        verify(eventLogger, never()).logReceivedHeartbeat(anyString());
    }

    public void testReceiveMessageSendError() {
        final String messageId = "123";
        final Exception messageException = new Exception("Exception");

        fcmMessagingService.onReceiveMessageSendError(messageId, messageException);

        fcmMessagingService.assertErrorMessageReceived(messageId, messageException);

        fcmMessagingService.assertDeletedMessagesCalled(false);
        fcmMessagingService.assertMessageDataReceived(NO_DATA);
        fcmMessagingService.assertMessageNotificationReceived(NO_NOTIFICATION, NO_DATA);
        fcmMessagingService.assertHeartbeatReceived(NO_DATA);

        verify(eventLogger, never()).logReceivedNotification(anyString());
        verify(eventLogger, never()).logReceivedHeartbeat(anyString());
    }

    public void testGeofenceAvailableKeySetToTrue() {
        final HashMap<String, String> messageData = new HashMap<>();
        messageData.put(GeofenceService.GEOFENCE_AVAILABLE, "true");

        fcmMessagingService.handleReceivedMessage(NO_NOTIFICATION, messageData);

        final Intent startServiceIntent = intentCaptor.getValue();

        assertEquals(startServiceIntent.getComponent().getClassName(), GeofenceService.class.getName());
        assertTrue(startServiceIntent.getExtras().containsKey(GeofenceService.GEOFENCE_AVAILABLE));
        assertEquals("true", startServiceIntent.getExtras().getString(GeofenceService.GEOFENCE_AVAILABLE));

        fcmMessagingService.assertDeletedMessagesCalled(false);
        fcmMessagingService.assertMessageDataReceived(NO_DATA);
        fcmMessagingService.assertMessageNotificationReceived(NO_NOTIFICATION, NO_DATA);
        fcmMessagingService.assertHeartbeatReceived(NO_DATA);
        fcmMessagingService.assertErrorMessageReceived(null, null);
    }

    public void testGeofenceAvailableKeySetToFalse() {
        final HashMap<String, String> messageData = new HashMap<>();
        messageData.put(GeofenceService.GEOFENCE_AVAILABLE, "false");

        fcmMessagingService.handleReceivedMessage(NO_NOTIFICATION, messageData);

        final Intent startServiceIntent = intentCaptor.getValue();

        assertEquals(startServiceIntent.getComponent().getClassName(), GeofenceService.class.getName());
        assertTrue(startServiceIntent.getExtras().containsKey(GeofenceService.GEOFENCE_AVAILABLE));
        assertEquals("false", startServiceIntent.getExtras().getString(GeofenceService.GEOFENCE_AVAILABLE));

        fcmMessagingService.assertDeletedMessagesCalled(false);
        fcmMessagingService.assertMessageDataReceived(NO_DATA);
        fcmMessagingService.assertMessageNotificationReceived(NO_NOTIFICATION, NO_DATA);
        fcmMessagingService.assertHeartbeatReceived(NO_DATA);
        fcmMessagingService.assertErrorMessageReceived(null, null);
    }

    public void testGeofenceWithExtraData() {
        final HashMap<String, String> messageData = new HashMap<>();
        messageData.put(GeofenceService.GEOFENCE_AVAILABLE, "false");
        messageData.put(GeofenceService.GEOFENCE_UPDATE_JSON, "{key: value}");
        messageData.put("other data key", "some string");

        fcmMessagingService.handleReceivedMessage(NO_NOTIFICATION, messageData);

        final Intent startServiceIntent = intentCaptor.getValue();

        assertEquals(startServiceIntent.getComponent().getClassName(), GeofenceService.class.getName());
        assertTrue(startServiceIntent.getExtras().containsKey(GeofenceService.GEOFENCE_AVAILABLE));
        assertEquals("false", startServiceIntent.getExtras().getString(GeofenceService.GEOFENCE_AVAILABLE));

        assertTrue(startServiceIntent.getExtras().containsKey(GeofenceService.GEOFENCE_UPDATE_JSON));
        assertEquals("{key: value}", startServiceIntent.getExtras().getString(GeofenceService.GEOFENCE_UPDATE_JSON));

        assertTrue(startServiceIntent.getExtras().containsKey("other data key"));
        assertEquals("some string", startServiceIntent.getExtras().getString("other data key"));

        fcmMessagingService.assertDeletedMessagesCalled(false);
        fcmMessagingService.assertMessageDataReceived(NO_DATA);
        fcmMessagingService.assertMessageNotificationReceived(NO_NOTIFICATION, NO_DATA);
        fcmMessagingService.assertHeartbeatReceived(NO_DATA);
        fcmMessagingService.assertErrorMessageReceived(null, null);
    }

    public void testGeofenceAvailableKeySetToEmpty() {
        final HashMap<String, String> messageData = new HashMap<>();
        messageData.put(GeofenceService.GEOFENCE_AVAILABLE, "");

        fcmMessagingService.handleReceivedMessage(NO_NOTIFICATION, messageData);

        final Intent startServiceIntent = intentCaptor.getValue();

        assertEquals(startServiceIntent.getComponent().getClassName(), GeofenceService.class.getName());
        assertTrue(startServiceIntent.getExtras().containsKey(GeofenceService.GEOFENCE_AVAILABLE));
        assertEquals("", startServiceIntent.getExtras().getString(GeofenceService.GEOFENCE_AVAILABLE));

        fcmMessagingService.assertDeletedMessagesCalled(false);
        fcmMessagingService.assertMessageDataReceived(NO_DATA);
        fcmMessagingService.assertMessageNotificationReceived(NO_NOTIFICATION, NO_DATA);
        fcmMessagingService.assertHeartbeatReceived(NO_DATA);
        fcmMessagingService.assertErrorMessageReceived(null, null);
    }

    private final class TestFcmMessagingService extends FcmMessagingService {

        private Map<String, String> data = NO_DATA;
        private Map<String, String> notificationData = NO_DATA;
        private RemoteMessage.Notification notification = NO_NOTIFICATION;
        private Map<String, String> heartbeat = NO_DATA;
        private boolean onReceiveDeletedMessagesCalled = false;
        private String errorMessageId = null;
        private Exception errorMessageException = null;

        @Override
        public void onMessageDataReceived(final Map<String, String> data) {
            this.data = data;
        }

        @Override
        public void onMessageNotificationReceived(final RemoteMessage.Notification notification, final Map<String, String> data) {
            this.notification = notification;
            this.notificationData = data;
        }

        @Override
        public void onReceiveHeartbeat(final Map<String, String> heartbeat) {
            this.heartbeat = heartbeat;
        }

        @Override
        public void onReceiveDeletedMessages() {
            onReceiveDeletedMessagesCalled = true;
        }

        @Override
        public void onReceiveMessageSendError(final String messageId, final Exception exception) {
            this.errorMessageId = messageId;
            this.errorMessageException = exception;
        }

        public void assertMessageDataReceived(final Map<String, String> expected) {
            assertEquals(expected, data);
        }

        public void assertMessageNotificationReceived(final RemoteMessage.Notification expectedNotification, final Map<String, String> expectedData) {
            assertEquals(expectedNotification, notification);
            assertEquals(expectedData, notificationData);
        }

        public void assertHeartbeatReceived(final Map<String, String> expected) {
            assertEquals(expected, heartbeat);
        }

        public void assertDeletedMessagesCalled(final boolean expected) {
            assertEquals(expected, onReceiveDeletedMessagesCalled);
        }

        public void assertErrorMessageReceived(final String messageId, final Exception exception) {
            assertEquals(messageId, errorMessageId);
            assertEquals(exception, errorMessageException);
        }

        @Override
        public void attachBaseContext(final Context base) {
            super.attachBaseContext(base);
        }
    }
}