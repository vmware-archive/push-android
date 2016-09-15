package io.pivotal.android.push.fcm;

import com.google.firebase.messaging.RemoteMessage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

import io.pivotal.android.push.analytics.AnalyticsEventLogger;
import io.pivotal.android.push.prefs.PushPreferencesProvider;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RemoteMessage.class)
public class FcmMessagingServiceTest {

    private final Map<String, String> NO_DATA = new HashMap<>();
    private final RemoteMessage.Notification NO_NOTIFICATION = null;
    private final Map<String, String> RECEIPT_DATA = new HashMap<>();


    private TestFcmMessagingService fcmMessagingService;
    private AnalyticsEventLogger eventLogger;
    private PushPreferencesProvider pushPreferencesProvider;

    @Before
    public void setup() {
        eventLogger = mock(AnalyticsEventLogger.class);
        pushPreferencesProvider = mock(PushPreferencesProvider.class);

        fcmMessagingService = new TestFcmMessagingService();
        fcmMessagingService.setEventLogger(eventLogger);
        fcmMessagingService.setPushPreferencesProvider(pushPreferencesProvider);

        RECEIPT_DATA.put(FcmMessagingService.KEY_RECEIPT_ID, "some receipt id");
    }

    @Test
    public void testMessageTypeNotification() {

        RemoteMessage.Notification notification = mock(RemoteMessage.Notification.class);

        RemoteMessage remoteMessage = mock(RemoteMessage.class);
        when(remoteMessage.getNotification()).thenReturn(notification);
        when(remoteMessage.getData()).thenReturn(RECEIPT_DATA);

        fcmMessagingService.onMessageReceived(remoteMessage);

        fcmMessagingService.assertDeletedMessagesCalled(false);
        fcmMessagingService.assertErrorMessageReceived(null, null);
        fcmMessagingService.assertMessageDataReceived(NO_DATA);
        fcmMessagingService.assertMessageNotificationReceived(notification);

        verify(eventLogger).logReceivedNotification(anyString());
        verify(eventLogger, never()).logReceivedHeartbeat(anyString());
    }

    @Test
    public void testMessageTypeData() {
        Map<String, String> messageData = new HashMap<>();
        messageData.put("SomePayloadKey", "SomePayloadValue");
        messageData.put(FcmMessagingService.KEY_RECEIPT_ID, "some receipt id");

        RemoteMessage remoteMessage = mock(RemoteMessage.class);
        when(remoteMessage.getData()).thenReturn(messageData);

        fcmMessagingService.onMessageReceived(remoteMessage);

        fcmMessagingService.assertDeletedMessagesCalled(false);
        fcmMessagingService.assertErrorMessageReceived(null, null);
        fcmMessagingService.assertMessageDataReceived(messageData);
        fcmMessagingService.assertMessageNotificationReceived(NO_NOTIFICATION);

        verify(eventLogger).logReceivedNotification(anyString());
        verify(eventLogger, never()).logReceivedHeartbeat(anyString());
    }

    @Test
    public void testMessageTypeNotificationAndData() {
        RemoteMessage.Notification notification = mock(RemoteMessage.Notification.class);
        Map<String, String> messageData = new HashMap<>();
        messageData.put("SomePayloadKey", "SomePayloadValue");
        messageData.put(FcmMessagingService.KEY_RECEIPT_ID, "some receipt id");

        RemoteMessage remoteMessage = mock(RemoteMessage.class);
        when(remoteMessage.getNotification()).thenReturn(notification);
        when(remoteMessage.getData()).thenReturn(messageData);

        fcmMessagingService.onMessageReceived(remoteMessage);

        fcmMessagingService.assertDeletedMessagesCalled(false);
        fcmMessagingService.assertErrorMessageReceived(null, null);
        fcmMessagingService.assertMessageDataReceived(messageData);
        fcmMessagingService.assertMessageNotificationReceived(notification);

        verify(eventLogger).logReceivedNotification(anyString());
        verify(eventLogger, never()).logReceivedHeartbeat(anyString());
    }

    @Test
    public void testMessageTypeHeartbeat() {
        Map<String, String> messageData = new HashMap<>();
        messageData.put(FcmMessagingService.KEY_RECEIPT_ID, "some receipt id");
        messageData.put(FcmMessagingService.KEY_HEARTBEAT, "heartbeat key");

        RemoteMessage remoteMessage = mock(RemoteMessage.class);
        when(remoteMessage.getData()).thenReturn(messageData);

        fcmMessagingService.onMessageReceived(remoteMessage);

        fcmMessagingService.assertDeletedMessagesCalled(false);
        fcmMessagingService.assertErrorMessageReceived(null, null);
        fcmMessagingService.assertMessageDataReceived(NO_DATA);
        fcmMessagingService.assertMessageNotificationReceived(NO_NOTIFICATION);
        fcmMessagingService.assertHeartbeatReceived(messageData);

        verify(eventLogger, never()).logReceivedNotification(anyString());
        verify(eventLogger).logReceivedHeartbeat(anyString());
    }

    @Test
    public void testReceiveDeleteMessage() {
        fcmMessagingService.onReceiveDeletedMessages();

        fcmMessagingService.assertDeletedMessagesCalled(true);
        fcmMessagingService.assertErrorMessageReceived(null, null);
        fcmMessagingService.assertMessageDataReceived(NO_DATA);
        fcmMessagingService.assertMessageNotificationReceived(NO_NOTIFICATION);
        fcmMessagingService.assertHeartbeatReceived(NO_DATA);

        verify(eventLogger, never()).logReceivedNotification(anyString());
        verify(eventLogger, never()).logReceivedHeartbeat(anyString());
    }

    @Test
    public void testReceiveMessageSendError() {
        String messageId = "123";
        Exception messageException = new Exception("Exception");

        fcmMessagingService.onReceiveMessageSendError(messageId, messageException);

        fcmMessagingService.assertErrorMessageReceived(messageId, messageException);

        fcmMessagingService.assertDeletedMessagesCalled(false);
        fcmMessagingService.assertMessageDataReceived(NO_DATA);
        fcmMessagingService.assertMessageNotificationReceived(NO_NOTIFICATION);
        fcmMessagingService.assertHeartbeatReceived(NO_DATA);

        verify(eventLogger, never()).logReceivedNotification(anyString());
        verify(eventLogger, never()).logReceivedHeartbeat(anyString());
    }

    private final class TestFcmMessagingService extends FcmMessagingService {

        private Map<String, String> data = NO_DATA;
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
        public void onMessageNotificationReceived(final RemoteMessage.Notification notification) {
            this.notification = notification;
        }

        @Override
        public void onReceiveHeartbeat(Map<String, String> heartbeat) {
            this.heartbeat = heartbeat;
        }

        @Override
        public void onReceiveDeletedMessages() {
            onReceiveDeletedMessagesCalled = true;
        }

        @Override
        public void onReceiveMessageSendError(String messageId, Exception exception) {
            this.errorMessageId = messageId;
            this.errorMessageException = exception;
        }

        public void assertMessageDataReceived(final Map<String, String> expected) {
            assertEquals(expected, data);
        }

        public void assertMessageNotificationReceived(final RemoteMessage.Notification expected) {
            assertEquals(expected, notification);
        }

        public void assertHeartbeatReceived(Map<String, String> expected) {
            assertEquals(expected, heartbeat);
        }

        public void assertDeletedMessagesCalled(boolean expected) {
            assertEquals(expected, onReceiveDeletedMessagesCalled);
        }

        public void assertErrorMessageReceived(String messageId, Exception exception) {
            assertEquals(messageId, errorMessageId);
            assertEquals(exception, errorMessageException);
        }

    }
}