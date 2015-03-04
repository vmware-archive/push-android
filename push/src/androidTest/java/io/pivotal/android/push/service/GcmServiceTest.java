/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.test.AndroidTestCase;
import android.test.mock.MockContext;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmServiceTest extends AndroidTestCase {

    private static final String TEST_MESSAGE = "some fancy message";

    public void testHandleNullIntent() throws InterruptedException {
        final FakeGcmService service = startService(FakeGcmService.class);
        service.onHandleIntent(null);
        service.assertMessageReceived(false);
        service.assertMessageSendError(false);
        service.assertMessageDeleted(false);
        service.onDestroy();
    }

    public void testHandleEmptyIntent() throws InterruptedException {
        final Intent intent = new Intent(getContext(), FakeGcmService.class);
        final FakeGcmService service = startService(FakeGcmService.class);
        service.onHandleIntent(intent);
        service.assertMessageReceived(false);
        service.assertMessageSendError(false);
        service.assertMessageDeleted(false);
        service.onDestroy();
    }

    public void testMessageReceived() throws InterruptedException {
        final Intent intent = createMessageReceivedIntent(TEST_MESSAGE);
        final FakeGcmService service = startService(FakeGcmService.class);
        service.onHandleIntent(intent);
        service.assertMessageContent(TEST_MESSAGE);
        service.assertMessageSendError(false);
        service.assertMessageDeleted(false);
        service.onDestroy();
    }

    public void testMessageDeleted() throws InterruptedException {
        final Intent intent = createMessageDeletedIntent();
        final FakeGcmService service = startService(FakeGcmService.class);
        service.onHandleIntent(intent);
        service.assertMessageSendError(false);
        service.assertMessageDeleted(true);
        service.onDestroy();
    }

    public void testMessageSendError() throws InterruptedException {
        final Intent intent = createMessageSendErrorIntent();
        final FakeGcmService service = startService(FakeGcmService.class);
        service.onHandleIntent(intent);
        service.assertMessageSendError(true);
        service.assertMessageDeleted(false);
        service.onDestroy();
    }

    public void testReceivesGeofenceUpdateSilentPush() throws InterruptedException {
        final FakeContext context = new FakeContext(getContext());
        final Intent intent = GeofenceServiceTest.createGeofenceUpdateSilentPushIntent(context, FakeGcmService.class);
        final FakeGcmService service = startService(FakeGcmService.class, context);
        service.onHandleIntent(intent);
        service.assertMessageSendError(false);
        service.assertMessageDeleted(false);
        service.onDestroy();
        assertEquals(GeofenceService.class.getCanonicalName(), context.getStartedServiceIntent().getComponent().getClassName());
        assertEquals(getContext().getPackageName(), context.getStartedServiceIntent().getComponent().getPackageName());
        assertTrue(context.getStartedServiceIntent().getExtras().getString(GeofenceService.GEOFENCE_AVAILABLE).equals("true"));
        assertTrue(context.getStartedServiceIntent().getAction().equals(intent.getAction()));
    }

    private Intent createMessageReceivedIntent(final String message) {
        final Intent intent = new Intent(getContext(), FakeGcmService.class);
        intent.setAction("com.google.android.c2dm.intent.RECEIVE");
        intent.putExtra("message_type", GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE);
        intent.putExtra(GcmService.KEY_MESSAGE, message);
        return intent;
    }

    private Intent createMessageDeletedIntent() {
        final Intent intent = new Intent(getContext(), FakeGcmService.class);
        intent.setAction("com.google.android.c2dm.intent.RECEIVE");
        intent.putExtra("message_type", GoogleCloudMessaging.MESSAGE_TYPE_DELETED);
        return intent;
    }

    private Intent createMessageSendErrorIntent() {
        final Intent intent = new Intent(getContext(), FakeGcmService.class);
        intent.setAction("com.google.android.c2dm.intent.RECEIVE");
        intent.putExtra("message_type", GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR);
        return intent;
    }

    private <T extends FakeGcmService> T startService(final Class<T> klass) {
        return startService(klass, getContext());
    }

    private <T extends FakeGcmService> T startService(final Class<T> klass, final Context context) {
        try {
            final Object object = klass.newInstance();
            final T service = klass.cast(object);
            service.attachBaseContext(context);
            service.onCreate();
            return service;
        } catch (Exception e) {
            return null;
        }
    }

    private final class FakeContext extends MockContext {

        private final Context context;
        private Intent startedServiceIntent;

        private FakeContext(Context context) {
            this.context = context;
        }

        @Override
        public Context getApplicationContext() {
            return this;
        }

        @Override
        public String getPackageName() {
            return context.getPackageName();
        }

        @Override
        public ComponentName startService(Intent service) {
            startedServiceIntent = service;
            final ComponentName componentName = service.getComponent();
            return componentName;
        }

        public Intent getStartedServiceIntent() {
            return startedServiceIntent;
        }
    }

    private static final class FakeGcmService extends GcmService {

        private boolean messageDeleted = false;
        private boolean messageSendError = false;
        private boolean messageReceived = false;

        private Bundle bundle;

        public FakeGcmService() {
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
