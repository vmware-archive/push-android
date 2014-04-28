package com.pivotal.cf.mobile.pushsdk.jobs;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.test.AndroidTestCase;

import com.pivotal.cf.mobile.pushsdk.backend.BackEndMessageReceiptApiRequestProvider;
import com.pivotal.cf.mobile.pushsdk.backend.FakeBackEndMessageReceiptApiRequest;
import com.pivotal.cf.mobile.pushsdk.broadcastreceiver.FakeEventsSenderAlarmProvider;
import com.pivotal.cf.mobile.pushsdk.database.FakeEventsStorage;
import com.pivotal.cf.mobile.pushsdk.model.events.Event;
import com.pivotal.cf.mobile.pushsdk.model.events.EventTest;
import com.pivotal.cf.mobile.pushsdk.network.FakeNetworkWrapper;
import com.pivotal.cf.mobile.pushsdk.prefs.FakePreferencesProvider;

import java.lang.reflect.Field;
import java.util.concurrent.Semaphore;

public abstract class JobTest extends AndroidTestCase {

    protected Event event1;
    protected Event event2;
    protected FakeEventsStorage eventsStorage;
    protected FakeNetworkWrapper networkWrapper;
    protected FakePreferencesProvider preferencesProvider;
    protected FakeEventsSenderAlarmProvider alarmProvider;
    protected FakeBackEndMessageReceiptApiRequest backEndMessageReceiptApiRequest;
    protected BackEndMessageReceiptApiRequestProvider backEndMessageReceiptApiRequestProvider;
    protected Semaphore semaphore = new Semaphore(0);

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        event1 = EventTest.getBaseEvent1();
        event2 = EventTest.getBaseEvent2();
        eventsStorage = new FakeEventsStorage();
        networkWrapper = new FakeNetworkWrapper();
        alarmProvider = new FakeEventsSenderAlarmProvider();
        preferencesProvider = new FakePreferencesProvider(null, null, 0, null, null, null, null, null);
        backEndMessageReceiptApiRequest = new FakeBackEndMessageReceiptApiRequest();
        backEndMessageReceiptApiRequestProvider = new BackEndMessageReceiptApiRequestProvider(backEndMessageReceiptApiRequest);
    }

    protected JobParams getJobParams(JobResultListener listener) {
        return new JobParams(getContext(), listener, networkWrapper, eventsStorage, preferencesProvider, alarmProvider, backEndMessageReceiptApiRequestProvider);
    }

    protected Uri saveEventWithStatus(int status) {
        event1.setStatus(status);
        return eventsStorage.saveEvent(event1);
    }

    protected void assertDatabaseEventCount(int expectedEventCount) {
        assertEquals(expectedEventCount, eventsStorage.getNumberOfEvents());
    }

    protected void assertEventHasStatus(Uri uri, int expectedStatus) {
        final Event event = eventsStorage.readEvent(uri);
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
