package org.omnia.pushsdk.jobs;

import android.content.Context;

import org.omnia.pushsdk.backend.BackEndMessageReceiptApiRequestProvider;
import org.omnia.pushsdk.broadcastreceiver.MessageReceiptAlarmProvider;
import org.omnia.pushsdk.database.EventsStorage;
import org.omnia.pushsdk.network.NetworkWrapper;
import org.omnia.pushsdk.prefs.PreferencesProvider;

public class JobParams {

    public final Context context;
    public final JobResultListener listener;
    public final NetworkWrapper networkWrapper;
    public final PreferencesProvider preferencesProvider;
    public final EventsStorage eventsStorage;
    public final MessageReceiptAlarmProvider alarmProvider;
    public final BackEndMessageReceiptApiRequestProvider backEndMessageReceiptApiRequestProvider;

    public JobParams(Context context,
                     JobResultListener listener,
                     NetworkWrapper networkWrapper,
                     EventsStorage eventsStorage,
                     PreferencesProvider preferencesProvider,
                     MessageReceiptAlarmProvider alarmProvider,
                     BackEndMessageReceiptApiRequestProvider backEndMessageReceiptApiRequestProvider) {

        verifyArguments(context, listener, networkWrapper, eventsStorage, preferencesProvider, alarmProvider, backEndMessageReceiptApiRequestProvider);

        this.context = context;
        this.listener = listener;
        this.networkWrapper = networkWrapper;
        this.eventsStorage = eventsStorage;
        this.preferencesProvider = preferencesProvider;
        this.alarmProvider = alarmProvider;
        this.backEndMessageReceiptApiRequestProvider = backEndMessageReceiptApiRequestProvider;
    }

    private void verifyArguments(Context context,
                                 JobResultListener listener,
                                 NetworkWrapper networkWrapper,
                                 EventsStorage eventsStorage,
                                 PreferencesProvider preferencesProvider,
                                 MessageReceiptAlarmProvider alarmProvider,
                                 BackEndMessageReceiptApiRequestProvider backEndMessageReceiptApiRequestProvider) {
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
        if (backEndMessageReceiptApiRequestProvider == null) {
            throw new IllegalArgumentException("backEndMessageReceiptApiRequestProvider may not be null");
        }
    }
}
