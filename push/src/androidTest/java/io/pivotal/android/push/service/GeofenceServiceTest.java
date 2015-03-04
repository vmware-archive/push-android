/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.test.AndroidTestCase;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;

import io.pivotal.android.push.PushParameters;
import io.pivotal.android.push.backend.geofence.PCFPushGetGeofenceUpdatesApiRequest;
import io.pivotal.android.push.backend.geofence.PCFPushGetGeofenceUpdatesListener;
import io.pivotal.android.push.geofence.GeofenceEngine;
import io.pivotal.android.push.model.geofence.PCFPushGeofenceResponseData;
import io.pivotal.android.push.prefs.FakePushPreferencesProvider;
import io.pivotal.android.push.util.ModelUtil;

import static org.mockito.Mockito.*;

public class GeofenceServiceTest extends AndroidTestCase {

    private GeofenceEngine geofenceEngine;
    private PCFPushGetGeofenceUpdatesApiRequest apiRequest;
    private FakeGeofenceService service;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("dexmaker.dexcache", mContext.getCacheDir().getPath());
        geofenceEngine = mock(GeofenceEngine.class);
        apiRequest = mock(PCFPushGetGeofenceUpdatesApiRequest.class);
        service = startService();
        service.setGeofenceEngine(geofenceEngine);
        service.setGetGeofenceUpdatesApiRequest(apiRequest);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        service.onDestroy();
    }

    public static Intent createGeofenceUpdateSilentPushIntent(final Context context, final Class<? extends IntentService> serviceClass) {
        final Intent intent = new Intent(context, serviceClass);
        intent.setAction("com.google.android.c2dm.intent.RECEIVE");
        intent.putExtra("message_type", GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE);
        intent.putExtra(GeofenceService.GEOFENCE_AVAILABLE, "true");
        return intent;
    }

    public void testHandleNullIntent() throws InterruptedException {
        service.onHandleIntent(null);
        verifyZeroInteractions(geofenceEngine);
        verifyZeroInteractions(apiRequest);
    }

    public void testHandleEmptyIntent() throws InterruptedException {
        final Intent intent = new Intent(getContext(), FakeGeofenceService.class);
        service.onHandleIntent(intent);
        verifyZeroInteractions(geofenceEngine);
        verifyZeroInteractions(apiRequest);
    }

    public void testFetchesUpdateSuccessfullyWithEmptyResponse() throws IOException {
        final Intent intent = GeofenceServiceTest.createGeofenceUpdateSilentPushIntent(getContext(), FakeGeofenceService.class);
        final PCFPushGeofenceResponseData responseData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_empty.json");
        final FakePushPreferencesProvider preferences = getPreferencesForTimestamp(1337L);

        doAnswer(new Answer<Void>(){

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                final PCFPushGetGeofenceUpdatesListener listener = (PCFPushGetGeofenceUpdatesListener) invocation.getArguments()[2];
                listener.onPCFPushGetGeofenceUpdatesSuccess(responseData);
                return null;
            }

        }).when(apiRequest).getGeofenceUpdates(eq(1337L), any(PushParameters.class), any(PCFPushGetGeofenceUpdatesListener.class));

        service.setPushPreferencesProvider(preferences);
        service.onHandleIntent(intent);

        verify(geofenceEngine, times(1)).processResponseData(eq(responseData));
        verify(apiRequest, times(1)).getGeofenceUpdates(eq(1337L), any(PushParameters.class), any(PCFPushGetGeofenceUpdatesListener.class));
        verifyNoMoreInteractions(apiRequest);
        verifyNoMoreInteractions(geofenceEngine);
        assertTrue(preferences.wasLastGeofenceUpdateSaved());
        assertEquals(0, preferences.getLastGeofenceUpdate());
    }

    public void testFetchesUpdateSuccessfullyWithPopulatedResponse() throws IOException {
        final Intent intent = GeofenceServiceTest.createGeofenceUpdateSilentPushIntent(getContext(), FakeGeofenceService.class);
        final PCFPushGeofenceResponseData responseData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_complex.json");
        final FakePushPreferencesProvider preferences = getPreferencesForTimestamp(1337L);

        doAnswer(new Answer<Void>(){

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                final PCFPushGetGeofenceUpdatesListener listener = (PCFPushGetGeofenceUpdatesListener) invocation.getArguments()[2];
                listener.onPCFPushGetGeofenceUpdatesSuccess(responseData);
                return null;
            }

        }).when(apiRequest).getGeofenceUpdates(eq(1337L), any(PushParameters.class), any(PCFPushGetGeofenceUpdatesListener.class));

        service.setPushPreferencesProvider(preferences);
        service.onHandleIntent(intent);

        verify(geofenceEngine, times(1)).processResponseData(eq(responseData));
        verify(apiRequest, times(1)).getGeofenceUpdates(eq(1337L), any(PushParameters.class), any(PCFPushGetGeofenceUpdatesListener.class));
        verifyNoMoreInteractions(apiRequest);
        verifyNoMoreInteractions(geofenceEngine);
        assertTrue(preferences.wasLastGeofenceUpdateSaved());
        assertEquals(1424309210305L, preferences.getLastGeofenceUpdate());
    }

    public void testFetchFails() throws IOException {
        final Intent intent = GeofenceServiceTest.createGeofenceUpdateSilentPushIntent(getContext(), FakeGeofenceService.class);
        final FakePushPreferencesProvider preferences = getPreferencesForTimestamp(1337L);

        doAnswer(new Answer<Void>(){

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                final PCFPushGetGeofenceUpdatesListener listener = (PCFPushGetGeofenceUpdatesListener) invocation.getArguments()[2];
                listener.onPCFPushGetGeofenceUpdatesFailed("Fake request failed fakely.");
                return null;
            }

        }).when(apiRequest).getGeofenceUpdates(eq(1337L), any(PushParameters.class), any(PCFPushGetGeofenceUpdatesListener.class));

        service.setPushPreferencesProvider(preferences);
        service.onHandleIntent(intent);

        verify(geofenceEngine, never()).processResponseData(any(PCFPushGeofenceResponseData.class));
        verify(apiRequest, times(1)).getGeofenceUpdates(eq(1337L), any(PushParameters.class), any(PCFPushGetGeofenceUpdatesListener.class));
        verifyNoMoreInteractions(apiRequest);
        verifyNoMoreInteractions(geofenceEngine);
        assertFalse(preferences.wasLastGeofenceUpdateSaved());
        assertEquals(1337L, preferences.getLastGeofenceUpdate());
    }

    private FakeGeofenceService startService() {
        try {
            final FakeGeofenceService service = new FakeGeofenceService();
            service.attachBaseContext(getContext());
            service.onCreate();
            return service;
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
            return null;
        }
    }

    private static final class FakeGeofenceService extends GeofenceService {

        public FakeGeofenceService() {
            super();
        }

        @Override
        public void attachBaseContext(final Context base) {
            super.attachBaseContext(base);
        }
    }

    private FakePushPreferencesProvider getPreferencesForTimestamp(long timestamp) {
        return new FakePushPreferencesProvider("", "", 0, "", "", "", "", "", "", null, timestamp);
    }

}
