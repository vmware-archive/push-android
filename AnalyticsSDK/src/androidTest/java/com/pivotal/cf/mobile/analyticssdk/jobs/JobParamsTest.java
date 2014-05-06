package com.pivotal.cf.mobile.analyticssdk.jobs;

import android.test.AndroidTestCase;

import com.pivotal.cf.mobile.analyticssdk.backend.BackEndSendEventsApiRequestProvider;
import com.pivotal.cf.mobile.analyticssdk.backend.FakeBackEndSendEventsApiRequest;
import com.pivotal.cf.mobile.analyticssdk.broadcastreceiver.FakeEventsSenderAlarmProvider;
import com.pivotal.cf.mobile.analyticssdk.database.FakeEventsStorage;
import com.pivotal.cf.mobile.analyticssdk.prefs.FakePreferencesProvider;
import com.pivotal.cf.mobile.common.test.network.FakeNetworkWrapper;

public class JobParamsTest extends AndroidTestCase {

    private FakeEventsStorage eventsStorage;
    private FakeNetworkWrapper networkWrapper;
    private FakePreferencesProvider preferencesProvider;
    private FakeEventsSenderAlarmProvider alarmProvider;
    private FakeBackEndSendEventsApiRequest backEndMessageReceiptApiRequest;
    private BackEndSendEventsApiRequestProvider backEndSendEventsApiRequestProvider;
    private JobResultListener listener;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        eventsStorage = new FakeEventsStorage();
        networkWrapper = new FakeNetworkWrapper();
        alarmProvider = new FakeEventsSenderAlarmProvider();
        preferencesProvider = new FakePreferencesProvider(null);
        backEndMessageReceiptApiRequest = new FakeBackEndSendEventsApiRequest();
        backEndSendEventsApiRequestProvider = new BackEndSendEventsApiRequestProvider(backEndMessageReceiptApiRequest);
        listener = new JobResultListener() {
            @Override
            public void onJobComplete(int resultCode) {
                fail();
            }
        };
    }

    public void testRequiresContext() {
        try {
            new JobParams(null, listener, networkWrapper, eventsStorage, preferencesProvider, alarmProvider, backEndSendEventsApiRequestProvider);
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testRequiresListener() {
        try {
            new JobParams(getContext(), null, networkWrapper, eventsStorage, preferencesProvider, alarmProvider, backEndSendEventsApiRequestProvider);
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testRequiresNetworkWrapper() {
        try {
            new JobParams(getContext(), listener, null, eventsStorage, preferencesProvider, alarmProvider, backEndSendEventsApiRequestProvider);
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testRequiresEventsStorage() {
        try {
            new JobParams(getContext(), listener, networkWrapper, null, preferencesProvider, alarmProvider, backEndSendEventsApiRequestProvider);
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testRequiresPreferencesProvider() {
        try {
            new JobParams(getContext(), listener, networkWrapper, eventsStorage, null, alarmProvider, backEndSendEventsApiRequestProvider);
            fail();
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    public void testRequiresAlarmProvider() {
        try {
            new JobParams(getContext(), listener, networkWrapper, eventsStorage, preferencesProvider, null, backEndSendEventsApiRequestProvider);
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
