package org.omnia.pushsdk.jobs;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.test.AndroidTestCase;

import org.omnia.pushsdk.backend.BackEndMessageReceiptApiRequestProvider;
import org.omnia.pushsdk.backend.FakeBackEndMessageReceiptApiRequest;
import org.omnia.pushsdk.broadcastreceiver.FakeEventsSenderAlarmProvider;
import org.omnia.pushsdk.database.EventsStorage;
import org.omnia.pushsdk.database.FakeEventsStorage;
import org.omnia.pushsdk.model.BaseEvent;
import org.omnia.pushsdk.model.MessageReceiptEvent;
import org.omnia.pushsdk.model.MessageReceiptEventTest;
import org.omnia.pushsdk.network.FakeNetworkWrapper;
import org.omnia.pushsdk.prefs.FakePreferencesProvider;

import java.lang.reflect.Field;
import java.util.concurrent.Semaphore;

public abstract class JobTest extends AndroidTestCase {

    protected MessageReceiptEvent event1;
    protected MessageReceiptEvent event2;
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
        event1 = MessageReceiptEventTest.getMessageReceiptEvent1();
        event2 = MessageReceiptEventTest.getMessageReceiptEvent2();
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
        return eventsStorage.saveEvent(event1, EventsStorage.EventType.MESSAGE_RECEIPT);
    }

    protected void assertEventHasStatus(Uri uri, int expectedStatus) {
        final BaseEvent event = eventsStorage.readEvent(uri);
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
