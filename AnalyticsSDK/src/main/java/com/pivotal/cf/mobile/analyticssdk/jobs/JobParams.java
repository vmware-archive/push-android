package com.pivotal.cf.mobile.analyticssdk.jobs;

import android.content.Context;

import com.pivotal.cf.mobile.analyticssdk.backend.BackEndSendEventsApiRequestProvider;
import com.pivotal.cf.mobile.analyticssdk.broadcastreceiver.EventsSenderAlarmProvider;
import com.pivotal.cf.mobile.analyticssdk.database.EventsStorage;
import com.pivotal.cf.mobile.common.network.NetworkWrapper;
import com.pivotal.cf.mobile.common.prefs.PreferencesProvider;

public class JobParams {

    public final Context context;
    public final JobResultListener listener;
    public final NetworkWrapper networkWrapper;
    public final PreferencesProvider preferencesProvider;
    public final EventsStorage eventsStorage;
    public final EventsSenderAlarmProvider alarmProvider;
    public final BackEndSendEventsApiRequestProvider backEndSendEventsApiRequestProvider;

    public JobParams(Context context,
                     JobResultListener listener,
                     NetworkWrapper networkWrapper,
                     EventsStorage eventsStorage,
                     PreferencesProvider preferencesProvider,
                     EventsSenderAlarmProvider alarmProvider,
                     BackEndSendEventsApiRequestProvider backEndSendEventsApiRequestProvider) {

        verifyArguments(context, listener, networkWrapper, eventsStorage, preferencesProvider, alarmProvider, backEndSendEventsApiRequestProvider);

        this.context = context;
        this.listener = listener;
        this.networkWrapper = networkWrapper;
        this.eventsStorage = eventsStorage;
        this.preferencesProvider = preferencesProvider;
        this.alarmProvider = alarmProvider;
        this.backEndSendEventsApiRequestProvider = backEndSendEventsApiRequestProvider;
    }

    private void verifyArguments(Context context,
                                 JobResultListener listener,
                                 NetworkWrapper networkWrapper,
                                 EventsStorage eventsStorage,
                                 PreferencesProvider preferencesProvider,
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
        if (preferencesProvider == null) {
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
