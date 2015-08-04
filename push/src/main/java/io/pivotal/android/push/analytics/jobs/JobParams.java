package io.pivotal.android.push.analytics.jobs;

import android.content.Context;

import io.pivotal.android.push.backend.analytics.PCFPushSendAnalyticsApiRequestProvider;
import io.pivotal.android.push.database.EventsStorage;
import io.pivotal.android.push.prefs.PushPreferencesProvider;
import io.pivotal.android.push.receiver.EventsSenderAlarmProvider;
import io.pivotal.android.push.util.NetworkWrapper;

public class JobParams {

    public final Context context;
    public final JobResultListener listener;
    public final NetworkWrapper networkWrapper;
    public final PushPreferencesProvider pushPreferencesProvider;
    public final EventsStorage eventsStorage;
    public final EventsSenderAlarmProvider alarmProvider;
    public final PCFPushSendAnalyticsApiRequestProvider requestProvider;

    public JobParams(Context context,
                     JobResultListener listener,
                     NetworkWrapper networkWrapper,
                     EventsStorage eventsStorage,
                     PushPreferencesProvider pushPreferencesProvider,
                     EventsSenderAlarmProvider alarmProvider,
                     PCFPushSendAnalyticsApiRequestProvider requestProvider) {

        verifyArguments(context, listener, networkWrapper, eventsStorage, pushPreferencesProvider, alarmProvider, requestProvider);

        this.context = context;
        this.listener = listener;
        this.networkWrapper = networkWrapper;
        this.eventsStorage = eventsStorage;
        this.pushPreferencesProvider = pushPreferencesProvider;
        this.alarmProvider = alarmProvider;
        this.requestProvider = requestProvider;
    }

    private void verifyArguments(Context context,
                                 JobResultListener listener,
                                 NetworkWrapper networkWrapper,
                                 EventsStorage eventsStorage,
                                 PushPreferencesProvider pushPreferencesProvider,
                                 EventsSenderAlarmProvider alarmProvider,
                                 PCFPushSendAnalyticsApiRequestProvider requestProvider) {
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
        if (pushPreferencesProvider == null) {
            throw new IllegalArgumentException("pushPreferencesProvider may not be null");
        }
        if (alarmProvider == null) {
            throw new IllegalArgumentException("alarmProvider may not be null");
        }
        if (requestProvider == null) {
            throw new IllegalArgumentException("requestProvider may not be null");
        }
    }
}
