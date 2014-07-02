package io.pivotal.android.push.service;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.test.AndroidTestCase;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import junit.framework.Assert;

import java.util.HashMap;

import io.pivotal.android.analytics.jobs.EnqueueEventJob;
import io.pivotal.android.analytics.model.events.Event;
import io.pivotal.android.analytics.service.EventService;
import io.pivotal.android.common.prefs.AnalyticsPreferencesProvider;
import io.pivotal.android.common.test.prefs.FakeAnalyticsPreferencesProvider;
import io.pivotal.android.common.test.util.FakeServiceStarter;
import io.pivotal.android.common.util.ServiceStarter;
import io.pivotal.android.push.model.events.EventPushReceived;
import io.pivotal.android.push.model.events.PushEventHelper;
import io.pivotal.android.push.prefs.FakePushPreferencesProvider;
import io.pivotal.android.push.prefs.PushPreferencesProvider;

public class GcmServiceTest extends AndroidTestCase {

    private static final String TEST_MESSAGE_UUID = "some-message-uuid";
    private static final String TEST_VARIANT_UUID = "some-variant-uuid";
    private static final String TEST_MESSAGE = "some fancy message";
    private static final String TEST_DEVICE_ID = "some_device_id";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testHandleNullIntent() throws InterruptedException {
        final FakeService service = startService(FakeService.class);
        service.onHandleIntent(null);
        service.assertMessageReceived(false);
        service.assertAnalyticsServiceStarted(false);
        service.onDestroy();
    }

    public void testHandleEmptyIntent() throws InterruptedException {
        final Intent intent = new Intent(getContext(), FakeService.class);

        final FakeService service = startService(FakeService.class);
        service.onHandleIntent(intent);
        service.assertMessageReceived(false);
        service.assertAnalyticsServiceStarted(false);
        service.onDestroy();
    }

    public void testMessageReceivedWithAnalyticsEnabled() throws InterruptedException {
        final Intent intent = createMessageReceivedIntent(TEST_MESSAGE);

        final FakeService service = startService(FakeService.class);
        service.getAnalyticsPreferencesProvider().setIsAnalyticsEnabled(true);
        service.onHandleIntent(intent);
        service.assertMessageContent(TEST_MESSAGE);
        service.assertAnalyticsServiceStarted(true);
        service.onDestroy();
    }

    public void testMessageReceivedWithAnalyticsDisabled() throws InterruptedException {
        final Intent intent = createMessageReceivedIntent(TEST_MESSAGE);

        final FakeService service = startService(FakeService.class);
        service.getAnalyticsPreferencesProvider().setIsAnalyticsEnabled(false);
        service.onHandleIntent(intent);
        service.assertMessageContent(TEST_MESSAGE);
        service.assertAnalyticsServiceStarted(false);
        service.onDestroy();
    }

    public void testMessageDeletedWithAnalyticsEnabled() throws InterruptedException {
        final Intent intent = createMessageDeletedIntent();

        final FakeService service = startService(FakeService.class);
        service.getAnalyticsPreferencesProvider().setIsAnalyticsEnabled(true);
        service.onHandleIntent(intent);
        service.assertMessageDeleted(true);
        service.assertAnalyticsServiceStarted(true);
        service.onDestroy();
    }

    public void testMessageDeletedWithAnalyticsDisabled() throws InterruptedException {
        final Intent intent = createMessageDeletedIntent();

        final FakeService service = startService(FakeService.class);
        service.getAnalyticsPreferencesProvider().setIsAnalyticsEnabled(false);
        service.onHandleIntent(intent);
        service.assertMessageDeleted(true);
        service.assertAnalyticsServiceStarted(false);
        service.onDestroy();
    }

    public void testMessageSendErrorWithAnalyticsEnabled() throws InterruptedException {
        final Intent intent = createMessageSendErrorIntent();

        final FakeService service = startService(FakeService.class);
        service.getAnalyticsPreferencesProvider().setIsAnalyticsEnabled(true);
        service.onHandleIntent(intent);
        service.assertMessageSendError(true);
        service.assertAnalyticsServiceStarted(true);
        service.onDestroy();
    }

    public void testMessageSendErrorWithAnalyticsDisabled() throws InterruptedException {
        final Intent intent = createMessageSendErrorIntent();

        final FakeService service = startService(FakeService.class);
        service.getAnalyticsPreferencesProvider().setIsAnalyticsEnabled(false);
        service.onHandleIntent(intent);
        service.assertMessageSendError(true);
        service.assertAnalyticsServiceStarted(false);
        service.onDestroy();
    }

    public void testQueuesReceiptNotificationWithMessageUuidWithAnalyticsDisabled() throws InterruptedException {
        final Intent intent = new Intent(getContext(), FakeService.class);
        intent.putExtra(GcmService.KEY_MESSAGE_UUID, TEST_MESSAGE_UUID);

        final FakeService service = startService(FakeService.class);
        service.getAnalyticsPreferencesProvider().setIsAnalyticsEnabled(false);
        service.getPushPreferencesProvider().setVariantUuid(TEST_VARIANT_UUID);
        service.onHandleIntent(intent);
        service.assertAnalyticsServiceStarted(false);
        service.onDestroy();
    }

    public void testQueuesReceiptNotificationWithMessageUuidWithAnalyticsEnabled() throws InterruptedException {
        final Intent intent = new Intent(getContext(), FakeService.class);
        intent.putExtra(GcmService.KEY_MESSAGE_UUID, TEST_MESSAGE_UUID);

        final FakeService service = startService(FakeService.class);
        service.getAnalyticsPreferencesProvider().setIsAnalyticsEnabled(true);
        service.getPushPreferencesProvider().setVariantUuid(TEST_VARIANT_UUID);
        service.getPushPreferencesProvider().setBackEndDeviceRegistrationId(TEST_DEVICE_ID);
        service.onHandleIntent(intent);
        service.assertAnalyticsEventSent(TEST_MESSAGE_UUID, TEST_VARIANT_UUID, TEST_DEVICE_ID);
        service.onDestroy();
    }

    private Intent createMessageReceivedIntent(final String message) {
        final Intent intent = new Intent(getContext(), FakeService.class);
        intent.setAction("com.google.android.c2dm.intent.RECEIVE");
        intent.putExtra("message_type", GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE);
        intent.putExtra(GcmService.KEY_MESSAGE, message);
        return intent;
    }

    private Intent createMessageDeletedIntent() {
        final Intent intent = new Intent(getContext(), FakeService.class);
        intent.setAction("com.google.android.c2dm.intent.RECEIVE");
        intent.putExtra("message_type", GoogleCloudMessaging.MESSAGE_TYPE_DELETED);
        return intent;
    }

    private Intent createMessageSendErrorIntent() {
        final Intent intent = new Intent(getContext(), FakeService.class);
        intent.setAction("com.google.android.c2dm.intent.RECEIVE");
        intent.putExtra("message_type", GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR);
        return intent;
    }

    private <T extends FakeService> T startService(final Class<T> klass) {
        try {
            final Object object = klass.newInstance();
            final T service = klass.cast(object);
            service.attachBaseContext(getContext());
            service.onCreate();
            return service;
        } catch (Exception e) {
            return null;
        }
    }

    private static final class FakeService extends GcmService {

        private boolean messageDeleted = false;
        private boolean messageSendError = false;
        private boolean messageReceived = false;

        private Bundle bundle;

        public FakeService() {
            super();
        }

        @Override
        public void attachBaseContext(final Context base) {
            super.attachBaseContext(base);
        }

        @Override
        public ServiceStarter onCreateServiceStarter() {
            return new FakeServiceStarter();
        }

        @Override
        public PushPreferencesProvider onCreatePushPreferencesProvider() {
            return new FakePushPreferencesProvider();
        }

        @Override
        public AnalyticsPreferencesProvider onCreateAnalyticsPreferencesProvider() {
            return new FakeAnalyticsPreferencesProvider(false, null);
        }

        @Override
        public void onReceiveMessage(final Bundle payload) {
            super.onReceiveMessage(payload);
            messageReceived = true;
            bundle = payload;
        }

        @Override
        public void onReceiveMessageSendError(final Bundle payload) {
            super.onReceiveMessageSendError(payload);
            messageSendError = true;
        }

        @Override
        public void onReceiveMessageDeleted(final Bundle payload) {
            super.onReceiveMessageDeleted(payload);
            messageDeleted = true;
        }

        public void assertMessageContent(final String expected) {
            assertTrue(messageReceived);
            assertEquals(expected, bundle.getString(GcmService.KEY_MESSAGE));
        }

        public void assertMessageReceived(final boolean expected) {
            assertEquals(expected, messageReceived);
        }

        public void assertMessageDeleted(final boolean expected) {
            assertEquals(expected, messageDeleted);
        }

        public void assertMessageSendError(final boolean expected) {
            assertEquals(expected, messageSendError);
        }

        public void assertAnalyticsServiceStarted(final boolean expected) {
            final FakeServiceStarter starter = (FakeServiceStarter) getServiceStarter();
            assertEquals(expected, starter.wasStarted());
        }

        public void assertAnalyticsEventSent(final String messageUUID, final String variantUUID, final String deviceID) {
            final FakeServiceStarter starter = (FakeServiceStarter) getServiceStarter();
            assertTrue(starter.wasStarted());

            final Intent intent = starter.getStartedIntent();
            assertEquals(EventService.class.getCanonicalName(), intent.getComponent().getClassName());

            final EnqueueEventJob job = intent.getParcelableExtra(EventService.KEY_JOB);
            final Event event = job.getEvent();
            Assert.assertEquals(EventPushReceived.EVENT_TYPE, event.getEventType());

            final HashMap<String, Object> map = event.getData();
            assertEquals(messageUUID, map.get(EventPushReceived.MESSAGE_UUID));
            assertEquals(variantUUID, map.get(PushEventHelper.VARIANT_UUID));
            assertEquals(deviceID, map.get(PushEventHelper.DEVICE_ID));
        }
    }
}
