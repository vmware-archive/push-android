package com.pivotal.cf.mobile.analyticssdk.jobs;

import android.content.Context;

import com.pivotal.cf.mobile.analyticssdk.backend.BackEndSendEventsApiRequestProvider;
import com.pivotal.cf.mobile.analyticssdk.broadcastreceiver.EventsSenderAlarmProvider;
import com.pivotal.cf.mobile.analyticssdk.database.EventsStorage;
import com.pivotal.cf.mobile.common.prefs.AnalyticsPreferencesProvider;
import com.pivotal.cf.mobile.common.network.NetworkWrapper;

public class JobParams {

    public final Context context;
    public final JobResultListener listener;
    public final NetworkWrapper networkWrapper;
    public final AnalyticsPreferencesProvider analyticsPreferencesProvider;
    public final EventsStorage eventsStorage;
    public final EventsSenderAlarmProvider alarmProvider;
    public final BackEndSendEventsApiRequestProvider backEndSendEventsApiRequestProvider;

    public JobParams(Context context,
                     JobResultListener listener,
                     NetworkWrapper networkWrapper,
                     EventsStorage eventsStorage,
                     AnalyticsPreferencesProvider analyticsPreferencesProvider,
                     EventsSenderAlarmProvider alarmProvider,
                     BackEndSendEventsApiRequestProvider backEndSendEventsApiRequestProvider) {

        verifyArguments(context, listener, networkWrapper, eventsStorage, analyticsPreferencesProvider, alarmProvider, backEndSendEventsApiRequestProvider);

        this.context = context;
        this.listener = listener;
        this.networkWrapper = networkWrapper;
        this.eventsStorage = eventsStorage;
        this.analyticsPreferencesProvider = analyticsPreferencesProvider;
        this.alarmProvider = alarmProvider;
        this.backEndSendEventsApiRequestProvider = backEndSendEventsApiRequestProvider;
    }

    private void verifyArguments(Context context,
                                 JobResultListener listener,
                                 NetworkWrapper networkWrapper,
                                 EventsStorage eventsStorage,
                                 AnalyticsPreferencesProvider analyticsPreferencesProvider,
                                 EventsSenderAlarmProvider alarmProvider,
                                 BackEndSendEventsApiRequestProvider backEndSendEventsApiRequestProvider) {
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("listener may not be null");
        }
        if (networkWrapper == null) {
            throw new IllegalArgumentException("networkWrapper may not be null");
        }
        if (eventsStorage == null) {
            throw new IllegalArgumentException("eventsStorage may not be null");
        }
        if (analyticsPreferencesProvider == null) {
            throw new IllegalArgumentException("preferencesProvider may not be null");
        }
        if (alarmProvider == null) {
            throw new IllegalArgumentException("alarmProvider may not be null");
        }
        if (backEndSendEventsApiRequestProvider == null) {
            throw new IllegalArgumentException("backEndMessageReceiptApiRequestProvider may not be null");
        }
    }
}
