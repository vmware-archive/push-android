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
import com.google.android.gms.location.Geofence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.pivotal.android.push.geofence.GeofencePersistentStore;
import io.pivotal.android.push.util.GeofenceHelper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GcmServiceTest extends AndroidTestCase {

    private static final String TEST_MESSAGE = "some fancy message";
    private static final Geofence GEOFENCE_1 = makeGeofence(-43.5,   61.5, 150.0f, "PCF_3_5",   Geofence.GEOFENCE_TRANSITION_ENTER, Geofence.NEVER_EXPIRE);
    private static final Geofence GEOFENCE_2 = makeGeofence( 53.5,  -91.5, 120.0f, "PCF_10_66", Geofence.GEOFENCE_TRANSITION_EXIT,  Geofence.NEVER_EXPIRE);
    private static final Geofence GEOFENCE_3 = makeGeofence( 53.5,  -91.5, 120.0f, "PCF_44_66", Geofence.GEOFENCE_TRANSITION_EXIT,  Geofence.NEVER_EXPIRE);
    private static final Geofence GEOFENCE_4 = makeGeofence( 55.5,  -94.5, 100.0f, "PCF_44_82", Geofence.GEOFENCE_TRANSITION_EXIT,  Geofence.NEVER_EXPIRE);


    private final List<Geofence> GEOFENCE_LIST_ENTER = new ArrayList<>();
    private final List<Geofence> GEOFENCE_LIST_EXIT = new ArrayList<>();
    private final List<Geofence> GEOFENCE_LIST_2_EXIT = new ArrayList<>();

    private GeofenceHelper helper;
    private GeofencePersistentStore store;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("dexmaker.dexcache", mContext.getCacheDir().getPath());
        helper = mock(GeofenceHelper.class);
        store = mock(GeofencePersistentStore.class);

        GEOFENCE_LIST_ENTER.add(GEOFENCE_1);

        GEOFENCE_LIST_EXIT.add(GEOFENCE_2);

        GEOFENCE_LIST_2_EXIT.add(GEOFENCE_2);
        GEOFENCE_LIST_2_EXIT.add(GEOFENCE_3);
        GEOFENCE_LIST_2_EXIT.add(GEOFENCE_4);
    }

    public void testHandleNullIntent() throws InterruptedException {
        final FakeGcmService service = startService(FakeGcmService.class);
        when(helper.isGeofencingEvent()).thenReturn(false);
        service.onHandleIntent(null);
        service.assertMessageReceived(false);
        service.assertMessageSendError(false);
        service.assertMessageDeleted(false);
        service.assertGeofenceEntered(false);
        service.assertGeofenceExited(false);
        service.onDestroy();
    }

    public void testHandleEmptyIntent() throws InterruptedException {
        final Intent intent = new Intent(getContext(), FakeGcmService.class);
        final FakeGcmService service = startService(FakeGcmService.class);
        when(helper.isGeofencingEvent()).thenReturn(false);
        service.onHandleIntent(intent);
        service.assertMessageReceived(false);
        service.assertMessageSendError(false);
        service.assertMessageDeleted(false);
        service.assertGeofenceEntered(false);
        service.assertGeofenceExited(false);
        service.onDestroy();
    }

    public void testMessageReceived() throws InterruptedException {
        final Intent intent = createMessageReceivedIntent(TEST_MESSAGE);
        final FakeGcmService service = startService(FakeGcmService.class);
        when(helper.isGeofencingEvent()).thenReturn(false);
        service.onHandleIntent(intent);
        service.assertMessageContent(TEST_MESSAGE);
        service.assertMessageSendError(false);
        service.assertMessageDeleted(false);
        service.assertGeofenceEntered(false);
        service.assertGeofenceExited(false);
        service.onDestroy();
    }

    public void testMessageDeleted() throws InterruptedException {
        final Intent intent = createMessageDeletedIntent();
        final FakeGcmService service = startService(FakeGcmService.class);
        when(helper.isGeofencingEvent()).thenReturn(false);
        service.onHandleIntent(intent);
        service.assertMessageSendError(false);
        service.assertMessageDeleted(true);
        service.assertGeofenceEntered(false);
        service.assertGeofenceExited(false);
        service.onDestroy();
    }

    public void testMessageSendError() throws InterruptedException {
        final Intent intent = createMessageSendErrorIntent();
        final FakeGcmService service = startService(FakeGcmService.class);
        when(helper.isGeofencingEvent()).thenReturn(false);
        service.onHandleIntent(intent);
        service.assertMessageSendError(true);
        service.assertMessageDeleted(false);
        service.assertGeofenceEntered(false);
        service.assertGeofenceExited(false);
        service.onDestroy();
    }

    public void testReceivesGeofenceUpdateSilentPush() throws InterruptedException {
        final FakeContext context = new FakeContext(getContext());
        final Intent intent = GeofenceServiceTest.createGeofenceUpdateSilentPushIntent(context, FakeGcmService.class);
        final FakeGcmService service = startService(FakeGcmService.class, context);
        when(helper.isGeofencingEvent()).thenReturn(false);
        service.onHandleIntent(intent);
        service.assertMessageSendError(false);
        service.assertMessageDeleted(false);
        service.assertGeofenceEntered(false);
        service.assertGeofenceExited(false);
        service.onDestroy();
        assertEquals(GeofenceService.class.getCanonicalName(), context.getStartedServiceIntent().getComponent().getClassName());
        assertEquals(getContext().getPackageName(), context.getStartedServiceIntent().getComponent().getPackageName());
        assertTrue(context.getStartedServiceIntent().getExtras().getString(GeofenceService.GEOFENCE_AVAILABLE).equals("true"));
        assertTrue(context.getStartedServiceIntent().getAction().equals(intent.getAction()));
    }

    public void testReceivesGeofenceEnterEvent() throws Exception {
        final Intent intent = createGeofenceTransitionEventIntent(getContext(), Geofence.GEOFENCE_TRANSITION_ENTER);
        final FakeGcmService service = startService(FakeGcmService.class);
        when(helper.isGeofencingEvent()).thenReturn(true);
        when(helper.getGeofenceTransition()).thenReturn(Geofence.GEOFENCE_TRANSITION_ENTER);
        setupMockGeofenceList(GEOFENCE_LIST_ENTER);
        service.onHandleIntent(intent);
        service.assertMessageSendError(false);
        service.assertMessageDeleted(false);
        service.assertGeofenceEntered(true);
        service.assertGeofenceExited(false);
        service.assertGeofenceEnteredContainsMessage("pizza");
        service.onDestroy();
    }

    public void testReceivesGeofenceExitEvent() throws Exception {
        final Intent intent = createGeofenceTransitionEventIntent(getContext(), Geofence.GEOFENCE_TRANSITION_EXIT);
        final FakeGcmService service = startService(FakeGcmService.class);
        when(helper.isGeofencingEvent()).thenReturn(true);
        when(helper.getGeofenceTransition()).thenReturn(Geofence.GEOFENCE_TRANSITION_EXIT);
        setupMockGeofenceList(GEOFENCE_LIST_EXIT);
        service.onHandleIntent(intent);
        service.assertMessageSendError(false);
        service.assertMessageDeleted(false);
        service.assertGeofenceEntered(false);
        service.assertGeofenceExited(true);
        service.assertGeofenceExitedContainsMessage("tacos");
        service.onDestroy();
    }

    public void testReceivesGeofenceExitEventMultiple() throws Exception {
        final Intent intent = createGeofenceTransitionEventIntent(getContext(), Geofence.GEOFENCE_TRANSITION_EXIT);
        final FakeGcmService service = startService(FakeGcmService.class);
        when(helper.isGeofencingEvent()).thenReturn(true);
        when(helper.getGeofenceTransition()).thenReturn(Geofence.GEOFENCE_TRANSITION_EXIT);
        setupMockGeofenceList(GEOFENCE_LIST_2_EXIT);
        service.onHandleIntent(intent);
        service.assertMessageSendError(false);
        service.assertMessageDeleted(false);
        service.assertGeofenceEntered(false);
        service.assertTimesGeofenceExited(3);
        service.assertGeofenceExitedContainsMessage("tacos");
        service.assertGeofenceExitedContainsMessage("eat all this great stuff");
        service.onDestroy();
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

    private Intent createGeofenceTransitionEventIntent(Context context, int transition) throws IOException {
        final Intent intent = new Intent(context, FakeGcmService.class);
        intent.putExtra("com.google.android.location.intent.extra.transition", transition);
        return intent;
    }

    private void setupMockGeofenceList(List<Geofence> list) {
        final List<Geofence> geofences = new ArrayList<>();
        geofences.addAll(list);
        when(helper.getGeofences()).thenReturn(geofences);
    }

    private static Geofence makeGeofence(double latitude, double longitude, float radius, String requestId, int transition, long duration) {
        return new Geofence.Builder()
                .setCircularRegion(latitude, longitude, radius)
                .setRequestId(requestId)
                .setTransitionTypes(transition)
                .setExpirationDuration(duration)
                .build();
    }

    private <T extends FakeGcmService> T startService(final Class<T> klass) {
        return startService(klass, getContext());
    }

    private <T extends FakeGcmService> T startService(final Class<T> klass, final Context context) {
        try {
            final Object object = klass.newInstance();
            final T service = klass.cast(object);
            service.setGeofenceHelper(helper);
            service.setGeofencePersistentStore(store);
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
        private List<String> enteredGeofencesMessages = new ArrayList<>();
        private List<String> exitedGeofencesMessages = new ArrayList<>();
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

        @Override
        public void onGeofenceEnter(Map<String, String> payload) {
            super.onGeofenceEnter(payload);
            if (payload != null) {
                enteredGeofencesMessages.add(payload.get("message"));
            }
        }

        @Override
        public void onGeofenceExit(Map<String, String> payload) {
            super.onGeofenceExit(payload);
            if (payload != null) {
                exitedGeofencesMessages.add(payload.get("message"));
            }
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

        public void assertGeofenceEntered(final boolean expected) {
            assertEquals(expected, enteredGeofencesMessages.size() == 1);
        }

        public void assertGeofenceExited(final boolean expected) {
            assertEquals(expected, exitedGeofencesMessages.size() == 1);
        }

        public void assertTimesGeofenceExited(final int count) {
            assertEquals(count, exitedGeofencesMessages.size());
        }

        public void assertGeofenceEnteredContainsMessage(final String expectedMessage) {
            assertTrue(enteredGeofencesMessages.contains(expectedMessage));
        }

        public void assertGeofenceExitedContainsMessage(final String expectedMessage) {
            assertTrue(exitedGeofencesMessages.contains(expectedMessage));
        }
    }
}
