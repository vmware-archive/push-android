/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.service;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import io.pivotal.android.push.PushParameters;
import io.pivotal.android.push.backend.geofence.PCFPushGetGeofenceUpdatesApiRequest;
import io.pivotal.android.push.backend.geofence.PCFPushGetGeofenceUpdatesListener;
import io.pivotal.android.push.geofence.GeofenceEngine;
import io.pivotal.android.push.model.geofence.PCFPushGeofenceResponseData;
import io.pivotal.android.push.prefs.Pivotal;
import io.pivotal.android.push.prefs.PushPreferencesFCM;
import io.pivotal.android.push.util.ModelUtil;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

@RunWith(AndroidJUnit4.class)
public class GeofenceServiceTest {

    private static final String TEST_DEVICE_UUID = "TEST_DEVICE_UUID";

    private GeofenceEngine geofenceEngine;
    private PCFPushGetGeofenceUpdatesApiRequest apiRequest;
    private FakeGeofenceService service;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", InstrumentationRegistry.getContext().getCacheDir().getPath());

        geofenceEngine = mock(GeofenceEngine.class);
        apiRequest = mock(PCFPushGetGeofenceUpdatesApiRequest.class);
        service = startService();
        service.setGeofenceEngine(geofenceEngine);
        service.setGetGeofenceUpdatesApiRequest(apiRequest);
    }

    @After
    public void tearDown() throws Exception {
        service.onDestroy();
    }

    public static Intent createGeofenceUpdateSilentPushIntent(final Context context,
        final Class<? extends IntentService> serviceClass) {
        final Intent intent = new Intent(context, serviceClass);
        intent.putExtra(GeofenceService.GEOFENCE_AVAILABLE, "true");
        return intent;
    }

    @Test
    public void testHandleNullIntent() throws InterruptedException {
        service.onHandleIntent(null);
        verifyZeroInteractions(geofenceEngine);
        verifyZeroInteractions(apiRequest);
    }

    @Test
    public void testHandleEmptyIntent() throws InterruptedException {
        final Intent intent = new Intent(InstrumentationRegistry.getContext(), FakeGeofenceService.class);
        final PushPreferencesFCM preferences = getPreferences(1337L, false);
        service.setPushPreferences(preferences);
        service.onHandleIntent(intent);
        verifyZeroInteractions(geofenceEngine);
        verifyZeroInteractions(apiRequest);
    }

    @Test
    public void testGeofencesDisabled() throws IOException {
        final Intent intent = GeofenceServiceTest
            .createGeofenceUpdateSilentPushIntent(InstrumentationRegistry.getContext(), FakeGeofenceService.class);
        final PushPreferencesFCM preferences = getPreferences(1337L, false);

        service.setPushPreferences(preferences);
        service.onHandleIntent(intent);

        verifyZeroInteractions(apiRequest);
        verifyZeroInteractions(geofenceEngine);
        verify(preferences, never()).setLastGeofenceUpdate(anyLong());
    }

    @Test
    public void testFetchesUpdateSuccessfullyWithEmptyResponse() throws IOException {
        final Context context = InstrumentationRegistry.getContext();
        final Intent intent = GeofenceServiceTest
            .createGeofenceUpdateSilentPushIntent(context, FakeGeofenceService.class);
        final PCFPushGeofenceResponseData responseData = ModelUtil.getPCFPushGeofenceResponseData(
            context,
            "geofence_response_data_empty.json");
        final PushPreferencesFCM preferences = getPreferences(1337L, true);

        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                final PCFPushGetGeofenceUpdatesListener listener = (PCFPushGetGeofenceUpdatesListener) invocation
                    .getArguments()[3];
                listener.onPCFPushGetGeofenceUpdatesSuccess(responseData);
                return null;
            }

        }).when(apiRequest).getGeofenceUpdates(eq(1337L), eq(TEST_DEVICE_UUID), any(PushParameters.class),
            any(PCFPushGetGeofenceUpdatesListener.class));

        service.setPushPreferences(preferences);
        service.onHandleIntent(intent);

        verify(geofenceEngine, times(1)).processResponseData(eq(1337L), eq(responseData), any(Set.class));
        verify(apiRequest, times(1)).getGeofenceUpdates(eq(1337L), eq(TEST_DEVICE_UUID), any(PushParameters.class),
            any(PCFPushGetGeofenceUpdatesListener.class));
        verifyNoMoreInteractions(apiRequest);
        verifyNoMoreInteractions(geofenceEngine);
        verify(preferences).setLastGeofenceUpdate(eq(0L));
    }

    @Test
    public void testFetchesUpdateSuccessfullyWithPopulatedResponse() throws IOException {
        final Context context = InstrumentationRegistry.getContext();
        final Intent intent = GeofenceServiceTest
            .createGeofenceUpdateSilentPushIntent(context, FakeGeofenceService.class);
        final PCFPushGeofenceResponseData responseData = ModelUtil.getPCFPushGeofenceResponseData(context,
            "geofence_response_data_complex.json");
        final PushPreferencesFCM preferences = getPreferences(1337L, true);

        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                final PCFPushGetGeofenceUpdatesListener listener = (PCFPushGetGeofenceUpdatesListener) invocation
                    .getArguments()[3];
                listener.onPCFPushGetGeofenceUpdatesSuccess(responseData);
                return null;
            }

        }).when(apiRequest).getGeofenceUpdates(eq(1337L), eq(TEST_DEVICE_UUID), any(PushParameters.class),
            any(PCFPushGetGeofenceUpdatesListener.class));

        service.setPushPreferences(preferences);
        service.onHandleIntent(intent);

        verify(geofenceEngine, times(1)).processResponseData(eq(1337L), eq(responseData), any(Set.class));
        verify(apiRequest, times(1)).getGeofenceUpdates(eq(1337L), eq(TEST_DEVICE_UUID), any(PushParameters.class),
            any(PCFPushGetGeofenceUpdatesListener.class));
        verifyNoMoreInteractions(apiRequest);
        verifyNoMoreInteractions(geofenceEngine);
        verify(preferences).setLastGeofenceUpdate(eq(1424309210305L));
    }

    @Test
    public void testFetchFails() throws IOException {
        final Intent intent = GeofenceServiceTest
            .createGeofenceUpdateSilentPushIntent(InstrumentationRegistry.getContext(), FakeGeofenceService.class);
        final PushPreferencesFCM preferences = getPreferences(1337L, true);

        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                final PCFPushGetGeofenceUpdatesListener listener = (PCFPushGetGeofenceUpdatesListener) invocation
                    .getArguments()[3];
                listener.onPCFPushGetGeofenceUpdatesFailed("Fake request failed fakely.");
                return null;
            }

        }).when(apiRequest).getGeofenceUpdates(eq(1337L), eq(TEST_DEVICE_UUID), any(PushParameters.class),
            any(PCFPushGetGeofenceUpdatesListener.class));

        service.setPushPreferences(preferences);
        service.onHandleIntent(intent);

        verify(geofenceEngine, never())
            .processResponseData(eq(1337L), any(PCFPushGeofenceResponseData.class), any(Set.class));
        verify(apiRequest, times(1)).getGeofenceUpdates(eq(1337L), eq(TEST_DEVICE_UUID), any(PushParameters.class),
            any(PCFPushGetGeofenceUpdatesListener.class));
        verifyNoMoreInteractions(apiRequest);
        verifyNoMoreInteractions(geofenceEngine);
        verify(preferences, never()).setLastGeofenceUpdate(anyLong());
    }

    private PushPreferencesFCM getPreferences(long timestamp, boolean areGeofencesEnabled) {
        final PushPreferencesFCM pushPreferences = mock(PushPreferencesFCM.class);
        when(pushPreferences.getPCFPushDeviceRegistrationId()).thenReturn(TEST_DEVICE_UUID);
        when(pushPreferences.getTags()).thenReturn(null);
        when(pushPreferences.areGeofencesEnabled()).thenReturn(areGeofencesEnabled);
        when(pushPreferences.getLastGeofenceUpdate()).thenReturn(timestamp);
        return pushPreferences;
    }

    private FakeGeofenceService startService() {
        try {
            final Context context = InstrumentationRegistry.getContext();
            final FakeGeofenceService service = new FakeGeofenceService(context);
            service.attachBaseContext(context);
            service.onCreate();
            return service;
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
            return null;
        }
    }

    private static final class FakeGeofenceService extends GeofenceService {

        private final Context context;

        public FakeGeofenceService(Context context) {
            super();
            this.context = context;
        }

        @Override
        public void attachBaseContext(final Context base) {
            super.attachBaseContext(base);
        }

        @Override
        public ClassLoader getClassLoader() {
            return context.getClassLoader();
        }
    }
}
