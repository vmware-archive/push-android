package org.omnia.pushsdk.jobs;

import android.test.AndroidTestCase;

import org.omnia.pushsdk.backend.BackEndMessageReceiptApiRequestProvider;
import org.omnia.pushsdk.backend.FakeBackEndMessageReceiptApiRequest;
import org.omnia.pushsdk.broadcastreceiver.FakeMessageReceiptAlarmProvider;
import org.omnia.pushsdk.database.FakeEventsStorage;
import org.omnia.pushsdk.model.MessageReceiptEventTest;
import org.omnia.pushsdk.network.FakeNetworkWrapper;
import org.omnia.pushsdk.prefs.FakePreferencesProvider;

public class JobParamsTest extends AndroidTestCase {

    private FakeEventsStorage eventsStorage;
    private FakeNetworkWrapper networkWrapper;
    private FakePreferencesProvider preferencesProvider;
    private FakeMessageReceiptAlarmProvider alarmProvider;
    private FakeBackEndMessageReceiptApiRequest backEndMessageReceiptApiRequest;
    private BackEndMessageReceiptApiRequestProvider backEndMessageReceiptApiRequestProvider;
    private JobResultListener listener;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        eventsStorage = new FakeEventsStorage();
        networkWrapper = new FakeNetworkWrapper();
        alarmProvider = new FakeMessageReceiptAlarmProvider();
        preferencesProvider = new FakePreferencesProvider(null, null, 0, null, null, null, null, null);
        backEndMessageReceiptApiRequest = new FakeBackEndMessageReceiptApiRequest();
        backEndMessageReceiptApiRequestProvider = new BackEndMessageReceiptApiRequestProvider(backEndMessageReceiptApiRequest);
        listener = new JobResultListener() {
            @Override
            public void onJobComplete(int resultCode) {
                fail();
            }
        };
    }

    public void testRequiresContext() {
        try {
            new JobParams(null, listener, networkWrapper, eventsStorage, preferencesProvider, alarmProvider, backEndMessageReceiptApiRequestProvider);
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testRequiresListener() {
        try {
            new JobParams(getContext(), null, networkWrapper, eventsStorage, preferencesProvider, alarmProvider, backEndMessageReceiptApiRequestProvider);
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testRequiresNetworkWrapper() {
        try {
            new JobParams(getContext(), listener, null, eventsStorage, preferencesProvider, alarmProvider, backEndMessageReceiptApiRequestProvider);
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testRequiresEventsStorage() {
        try {
            new JobParams(getContext(), listener, networkWrapper, null, preferencesProvider, alarmProvider, backEndMessageReceiptApiRequestProvider);
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testRequiresPreferencesProvider() {
        try {
            new JobParams(getContext(), listener, networkWrapper, eventsStorage, null, alarmProvider, backEndMessageReceiptApiRequestProvider);
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testRequiresAlarmProvider() {
        try {
            new JobParams(getContext(), listener, networkWrapper, eventsStorage, preferencesProvider, null, backEndMessageReceiptApiRequestProvider);
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testRequiresBackEndMessageReceiptApiRequestProvider() {
        try {
            new JobParams(getContext(), listener, networkWrapper, eventsStorage, preferencesProvider, alarmProvider, null);
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }
}
