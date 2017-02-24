package io.pivotal.android.push.analytics.jobs;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.test.InstrumentationRegistry;

import io.pivotal.android.push.prefs.PushPreferences;

import java.lang.reflect.Field;
import java.util.concurrent.Semaphore;

import io.pivotal.android.push.backend.analytics.FakePCFPushSendAnalyticsApiRequest;
import io.pivotal.android.push.backend.analytics.PCFPushSendAnalyticsApiRequestProvider;
import io.pivotal.android.push.database.FakeAnalyticsEventsStorage;
import io.pivotal.android.push.model.analytics.AnalyticsEvent;
import io.pivotal.android.push.model.analytics.AnalyticsEventTest;
import io.pivotal.android.push.receiver.FakeAnalyticsEventsSenderAlarmProvider;
import io.pivotal.android.push.util.FakeNetworkWrapper;
import io.pivotal.android.push.util.FakeServiceStarter;
import io.pivotal.android.push.util.TimeProvider;
import org.junit.Before;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class JobTest {

    private static final String TEST_SERVICE_URL = "http://test.service.url";

    protected AnalyticsEvent event1;
    protected AnalyticsEvent event2;
    protected AnalyticsEvent heartbeatEvent;
    protected FakeAnalyticsEventsStorage eventsStorage;
    protected TimeProvider timeProvider;
    protected FakeNetworkWrapper networkWrapper;
    protected FakeServiceStarter serviceStarter;
    protected PushPreferences pushPreferences;
    protected FakeAnalyticsEventsSenderAlarmProvider alarmProvider;
    protected FakePCFPushSendAnalyticsApiRequest sendAnalyticsApiRequest;
    protected PCFPushSendAnalyticsApiRequestProvider sendAnalyticsApiRequestProvider;
    protected Semaphore semaphore = new Semaphore(0);

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", InstrumentationRegistry.getContext().getCacheDir().getPath());
        event1 = AnalyticsEventTest.getEvent1();
        event2 = AnalyticsEventTest.getEvent2();
        heartbeatEvent = AnalyticsEventTest.getHeartbeatEvent();
        eventsStorage = new FakeAnalyticsEventsStorage();
        timeProvider = mock(TimeProvider.class);
        networkWrapper = new FakeNetworkWrapper();
        serviceStarter = new FakeServiceStarter();
        alarmProvider = new FakeAnalyticsEventsSenderAlarmProvider();
        sendAnalyticsApiRequest = new FakePCFPushSendAnalyticsApiRequest();
        sendAnalyticsApiRequestProvider = new PCFPushSendAnalyticsApiRequestProvider(sendAnalyticsApiRequest);

        pushPreferences = mock(PushPreferences.class);
        when(pushPreferences.getServiceUrl()).thenReturn(TEST_SERVICE_URL);
    }

    protected JobParams getJobParams(JobResultListener listener) {
        return new JobParams(InstrumentationRegistry.getContext(), listener, timeProvider, networkWrapper, serviceStarter, eventsStorage,
            pushPreferences, alarmProvider, sendAnalyticsApiRequestProvider);
    }

    protected Uri saveEventWithStatus(int status) {
        event1.setStatus(status);
        return eventsStorage.saveEvent(event1);
    }

    protected void assertDatabaseEventCount(int expectedEventCount) {
        assertEquals(expectedEventCount, eventsStorage.getNumberOfEvents());
    }

    protected void assertEventHasStatus(Uri uri, int expectedStatus) {
        final AnalyticsEvent event = eventsStorage.readEvent(uri);
        assertNotNull(event);
        assertEquals(expectedStatus, event.getStatus());
    }

    protected void assertEventNotInStorage(Uri uri) {
        try {
            eventsStorage.readEvent(uri);
            fail("expected event to be removed from storage");
        } catch(IllegalArgumentException e) {
            // It is expected that the readEvent call throws an exception since
            // the event is not supposed to be in the database
        }
    }

    // Parcelable stuff

    protected <T extends Parcelable> T getJobViaParcel(T inputJob) {
        final Parcel inputParcel = Parcel.obtain();
        inputJob.writeToParcel(inputParcel, 0);
        final byte[] bytes = inputParcel.marshall();
        assertNotNull(bytes);
        final Parcel outputParcel = Parcel.obtain();
        outputParcel.unmarshall(bytes, 0, bytes.length);
        outputParcel.setDataPosition(0);
        final T outputJob = getCreator(inputJob).createFromParcel(outputParcel);
        inputParcel.recycle();
        outputParcel.recycle();
        return outputJob;
    }

    private <T extends Parcelable> Parcelable.Creator<T> getCreator(T inputJob) {
        final Class<? extends Parcelable> clazz = inputJob.getClass();
        try {
            final Field field = clazz.getField("CREATOR");
            final Parcelable.Creator<T> creator = (Parcelable.Creator<T>) field.get(null);
            return creator;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
}
