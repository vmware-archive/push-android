package io.pivotal.android.push.service;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.test.AndroidTestCase;

import com.google.android.gms.gcm.GoogleCloudMessaging;

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
        service.onDestroy();
    }

    public void testHandleEmptyIntent() throws InterruptedException {
        final Intent intent = new Intent(getContext(), FakeService.class);

        final FakeService service = startService(FakeService.class);
        service.onHandleIntent(intent);
        service.assertMessageReceived(false);
        service.onDestroy();
    }

    public void testMessageReceived() throws InterruptedException {
        final Intent intent = createMessageReceivedIntent(TEST_MESSAGE);

        final FakeService service = startService(FakeService.class);
        service.onHandleIntent(intent);
        service.assertMessageContent(TEST_MESSAGE);
        service.onDestroy();
    }

    public void testMessageDeleted() throws InterruptedException {
        final Intent intent = createMessageDeletedIntent();

        final FakeService service = startService(FakeService.class);
        service.onHandleIntent(intent);
        service.assertMessageDeleted(true);
        service.onDestroy();
    }

    public void testMessageSendError() throws InterruptedException {
        final Intent intent = createMessageSendErrorIntent();

        final FakeService service = startService(FakeService.class);
        service.onHandleIntent(intent);
        service.assertMessageSendError(true);
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
    }
}
