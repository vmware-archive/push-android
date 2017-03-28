package io.pivotal.android.push.analytics.jobs;

import android.content.Context;

import io.pivotal.android.push.backend.analytics.PCFPushSendAnalyticsApiRequestProvider;
import io.pivotal.android.push.database.AnalyticsEventsStorage;
import io.pivotal.android.push.prefs.PushPreferencesProvider;
import io.pivotal.android.push.receiver.AnalyticsEventsSenderAlarmProvider;
import io.pivotal.android.push.util.NetworkWrapper;
import io.pivotal.android.push.util.ServiceStarter;
import io.pivotal.android.push.util.TimeProvider;

public class JobParams {

    public final Context context;
    public final JobResultListener listener;
    public final TimeProvider timeProvider;
    public final NetworkWrapper networkWrapper;
    public final ServiceStarter serviceStarter;
    public final PushPreferencesProvider pushPreferencesProvider;
    public final AnalyticsEventsStorage eventsStorage;
    public final AnalyticsEventsSenderAlarmProvider alarmProvider;
    public final PCFPushSendAnalyticsApiRequestProvider sendAnalyticsRequestProvider;

    public JobParams(Context context,
                     JobResultListener listener,
                     TimeProvider timeProvider,
                     NetworkWrapper networkWrapper,
                     ServiceStarter serviceStarter,
                     AnalyticsEventsStorage eventsStorage,
                     PushPreferencesProvider pushPreferencesProvider,
                     AnalyticsEventsSenderAlarmProvider alarmProvider,
                     PCFPushSendAnalyticsApiRequestProvider sendAnalyticsRequestProvider) {

        verifyArguments(context, listener, timeProvider, networkWrapper, serviceStarter, eventsStorage, pushPreferencesProvider, alarmProvider, sendAnalyticsRequestProvider);

        this.context = context;
        this.listener = listener;
        this.timeProvider = timeProvider;
        this.networkWrapper = networkWrapper;
        this.serviceStarter = serviceStarter;
        this.eventsStorage = eventsStorage;
        this.pushPreferencesProvider = pushPreferencesProvider;
        this.alarmProvider = alarmProvider;
        this.sendAnalyticsRequestProvider = sendAnalyticsRequestProvider;
    }

    public JobParams(JobParams otherJobParams, JobResultListener listener) {

        verifyArguments(otherJobParams.context,
                listener,
                otherJobParams.timeProvider,
                otherJobParams.networkWrapper,
                otherJobParams.serviceStarter,
                otherJobParams.eventsStorage,
                otherJobParams.pushPreferencesProvider,
                otherJobParams.alarmProvider,
                otherJobParams.sendAnalyticsRequestProvider);

        this.context = otherJobParams.context;
        this.listener = listener;
        this.timeProvider = otherJobParams.timeProvider;
        this.networkWrapper = otherJobParams.networkWrapper;
        this.serviceStarter = otherJobParams.serviceStarter;
        this.eventsStorage = otherJobParams.eventsStorage;
        this.pushPreferencesProvider = otherJobParams.pushPreferencesProvider;
        this.alarmProvider = otherJobParams.alarmProvider;
        this.sendAnalyticsRequestProvider = otherJobParams.sendAnalyticsRequestProvider;
    }

    private void verifyArguments(Context context,
                                 JobResultListener listener,
                                 TimeProvider timeProvider,
                                 NetworkWrapper networkWrapper,
                                 ServiceStarter serviceStarter,
                                 AnalyticsEventsStorage eventsStorage,
                                 PushPreferencesProvider pushPreferencesProvider,
                                 AnalyticsEventsSenderAlarmProvider alarmProvider,
                                 PCFPushSendAnalyticsApiRequestProvider sendAnalyticsRequestProvider) {
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
        if (serviceStarter == null) {
            throw new IllegalArgumentException("serviceStarter may not be null");
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
    }
}
