package io.pivotal.android.push.analytics.jobs;

import android.content.Context;

import io.pivotal.android.push.backend.analytics.PCFPushCheckBackEndVersionApiRequestProvider;
import io.pivotal.android.push.backend.analytics.PCFPushSendAnalyticsApiRequestProvider;
import io.pivotal.android.push.database.AnalyticsEventsStorage;
import io.pivotal.android.push.prefs.PushPreferencesProvider;
import io.pivotal.android.push.receiver.AnalyticsEventsSenderAlarmProvider;
import io.pivotal.android.push.util.NetworkWrapper;
import io.pivotal.android.push.util.TimeProvider;

public class JobParams {

    public final Context context;
    public final JobResultListener listener;
    public final TimeProvider timeProvider;
    public final NetworkWrapper networkWrapper;
    public final PushPreferencesProvider pushPreferencesProvider;
    public final AnalyticsEventsStorage eventsStorage;
    public final AnalyticsEventsSenderAlarmProvider alarmProvider;
    public final PCFPushSendAnalyticsApiRequestProvider sendAnalyticsRequestProvider;
    public final PCFPushCheckBackEndVersionApiRequestProvider checkBackEndVersionRequestProvider;

    public JobParams(Context context,
                     JobResultListener listener,
                     TimeProvider timeProvider,
                     NetworkWrapper networkWrapper,
                     AnalyticsEventsStorage eventsStorage,
                     PushPreferencesProvider pushPreferencesProvider,
                     AnalyticsEventsSenderAlarmProvider alarmProvider,
                     PCFPushSendAnalyticsApiRequestProvider sendAnalyticsRequestProvider,
                     PCFPushCheckBackEndVersionApiRequestProvider checkBackEndVersionRequestProvider) {

        verifyArguments(context, listener, timeProvider, networkWrapper, eventsStorage, pushPreferencesProvider, alarmProvider, sendAnalyticsRequestProvider, checkBackEndVersionRequestProvider);

        this.context = context;
        this.listener = listener;
        this.timeProvider = timeProvider;
        this.networkWrapper = networkWrapper;
        this.eventsStorage = eventsStorage;
        this.pushPreferencesProvider = pushPreferencesProvider;
        this.alarmProvider = alarmProvider;
        this.sendAnalyticsRequestProvider = sendAnalyticsRequestProvider;
        this.checkBackEndVersionRequestProvider = checkBackEndVersionRequestProvider;
    }

    private void verifyArguments(Context context,
                                 JobResultListener listener,
                                 TimeProvider timeProvider,
                                 NetworkWrapper networkWrapper,
                                 AnalyticsEventsStorage eventsStorage,
                                 PushPreferencesProvider pushPreferencesProvider,
                                 AnalyticsEventsSenderAlarmProvider alarmProvider,
                                 PCFPushSendAnalyticsApiRequestProvider sendAnalyticsRequestProvider,
                                 PCFPushCheckBackEndVersionApiRequestProvider checkBackEndVersionRequestProvider) {
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("listener may not be null");
        }
        if (timeProvider == null) {
            throw new IllegalArgumentException("timeProvider may not be null");
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
        if (sendAnalyticsRequestProvider == null) {
            throw new IllegalArgumentException("sendAnalyticsRequestProvider may not be null");
        }
        if (checkBackEndVersionRequestProvider == null) {
            throw new IllegalArgumentException("checkBackEndVersionRequestProvider may not be null");
        }
    }
}
