package com.pivotal.cf.mobile.analyticssdk;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.pivotal.cf.mobile.analyticssdk.broadcastreceiver.EventsSenderAlarmProvider;
import com.pivotal.cf.mobile.analyticssdk.broadcastreceiver.EventsSenderAlarmProviderImpl;
import com.pivotal.cf.mobile.analyticssdk.jobs.PrepareDatabaseJob;
import com.pivotal.cf.mobile.analyticssdk.service.EventService;
import com.pivotal.cf.mobile.common.prefs.AnalyticsPreferencesProvider;
import com.pivotal.cf.mobile.common.prefs.AnalyticsPreferencesProviderImpl;
import com.pivotal.cf.mobile.common.util.Logger;
import com.pivotal.cf.mobile.common.util.ServiceStarter;
import com.pivotal.cf.mobile.common.util.ServiceStarterImpl;

public class AnalyticsSDK {

    private static AnalyticsSDK instance;

    private Context context;
    private boolean wasDatabaseCleanupJobRun = false;

    /**
     * Gets an instance of the Analytics SDK singleton object.
     *
     * @param context some context
     * @return an instance of the Analytics SDK
     */
    public synchronized static AnalyticsSDK getInstance(Context context) {
        if (instance == null) {
            instance = new AnalyticsSDK(context);
        }
        return instance;
    }

    private AnalyticsSDK(Context context) {
        verifyArguments(context);
        saveArguments(context);

        if (!Logger.isSetup()) {
            Logger.setup(context);
        }
    }

    private void verifyArguments(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
    }

    private void saveArguments(Context context) {
        if (!(context instanceof Application)) {
            this.context = context.getApplicationContext();
        } else {
            this.context = context;
        }
    }

    /**
     * Update the Analytics engine parameters.  You MUST set the Analytics Parameters before any analytics data
     * can be captured.
     *
     * @param parameters  The new parameter settings.
     */
    public void setParameters(AnalyticsParameters parameters) {
        verifyParameters(parameters);
        saveParameters(parameters);

        if (parameters.isAnalyticsEnabled()) {
            cleanupDatabase();
            Logger.i("AnalyticsSDK parameters: baseServerUrl:" + parameters.getBaseServerUrl());
        } else {
            Logger.i("AnalyticsSDK is disabled.");
            final EventsSenderAlarmProvider alarmProvider = new EventsSenderAlarmProviderImpl(context);
            alarmProvider.disableAlarm();
        }
    }

    private void verifyParameters(AnalyticsParameters parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("parameters may not be null");
        }
        if (parameters.isAnalyticsEnabled() && parameters.getBaseServerUrl() == null) {
            throw new IllegalArgumentException("parameters.baseServerUrl may not be null if parameters.isAnalyticsIsEnabled is true");
        }
    }

    private void saveParameters(AnalyticsParameters parameters) {
        final AnalyticsPreferencesProvider prefs = new AnalyticsPreferencesProviderImpl(context);
        prefs.setBaseServerUrl(parameters.getBaseServerUrl());
        prefs.setIsAnalyticsEnabled(parameters.isAnalyticsEnabled());
    }

    private void cleanupDatabase() {
        if (!wasDatabaseCleanupJobRun) {

            // If the process has just been initialized, then run the PrepareDatabaseJob in order to prepare the database
            final PrepareDatabaseJob job = new PrepareDatabaseJob();
            final Intent intent = EventService.getIntentToRunJob(context, job);
            context.startService(intent);

        } else {

            // Otherwise, simply make sure that the timer for posting events to the server is enabled.
            final EventsSenderAlarmProvider alarmProvider = new EventsSenderAlarmProviderImpl(context);
            alarmProvider.enableAlarmIfDisabled();
        }
        wasDatabaseCleanupJobRun = true;
    }

    /**
     * Gets an instance of the EventLogger - which can be used to log analytics events.
     *
     * @return
     */
    public EventLogger getEventLogger() {
        final ServiceStarter serviceStarter = new ServiceStarterImpl();
        final AnalyticsPreferencesProvider preferencesProvider = new AnalyticsPreferencesProviderImpl(context);
        return new EventLogger(serviceStarter, preferencesProvider, context);
    }
}
