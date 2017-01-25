package io.pivotal.android.push.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.ResultReceiver;

import java.util.List;
import java.util.concurrent.Semaphore;

import io.pivotal.android.push.analytics.jobs.BaseJob;
import io.pivotal.android.push.analytics.jobs.CheckBackEndVersionJob;
import io.pivotal.android.push.analytics.jobs.EnqueueAnalyticsEventJob;
import io.pivotal.android.push.analytics.jobs.JobParams;
import io.pivotal.android.push.analytics.jobs.JobResultListener;
import io.pivotal.android.push.analytics.jobs.PrepareDatabaseJob;
import io.pivotal.android.push.analytics.jobs.SendAnalyticsEventsJob;
import io.pivotal.android.push.backend.analytics.PCFPushCheckBackEndVersionApiRequestImpl;
import io.pivotal.android.push.backend.analytics.PCFPushCheckBackEndVersionApiRequestProvider;
import io.pivotal.android.push.backend.analytics.PCFPushSendAnalyticsApiRequestImpl;
import io.pivotal.android.push.backend.analytics.PCFPushSendAnalyticsApiRequestProvider;
import io.pivotal.android.push.database.AnalyticsEventsStorage;
import io.pivotal.android.push.database.DatabaseAnalyticsEventsStorage;
import io.pivotal.android.push.database.DatabaseWrapper;
import io.pivotal.android.push.prefs.Pivotal;
import io.pivotal.android.push.prefs.PushPreferencesProvider;
import io.pivotal.android.push.prefs.PushPreferencesProviderImpl;
import io.pivotal.android.push.prefs.PushRequestHeaders;
import io.pivotal.android.push.receiver.AnalyticsEventsSenderAlarmProvider;
import io.pivotal.android.push.receiver.AnalyticsEventsSenderAlarmProviderImpl;
import io.pivotal.android.push.receiver.AnalyticsEventsSenderAlarmReceiver;
import io.pivotal.android.push.util.Logger;
import io.pivotal.android.push.util.NetworkWrapper;
import io.pivotal.android.push.util.NetworkWrapperImpl;
import io.pivotal.android.push.util.ServiceStarter;
import io.pivotal.android.push.util.ServiceStarterImpl;
import io.pivotal.android.push.util.TimeProvider;

public class AnalyticsEventService extends IntentService {

    public static final String KEY_RESULT_RECEIVER = "result_receiver";
    public static final String KEY_JOB = "job";

    public static final int NO_RESULT = -1;
    public static final int JOB_INTERRUPTED = 1;
    public static final int ANALYTICS_DISABLED = 2;

    // Used by unit tests
    /* package */ static Semaphore semaphore = null;
    /* package */ static AnalyticsEventsStorage eventsStorage = null;
    /* package */ static TimeProvider timeProvider;
    /* package */ static NetworkWrapper networkWrapper = null;
    /* package */ static ServiceStarter serviceStarter = null;
    /* package */ static AnalyticsEventsSenderAlarmProvider alarmProvider = null;
    /* package */ static PCFPushSendAnalyticsApiRequestProvider sendAnalyticsRequestProvider = null;
    /* package */ static PCFPushCheckBackEndVersionApiRequestProvider checkBackEndVersionRequestProvider = null;
    /* package */ static List<String> listOfCompletedJobs = null;
    /* package */ static PushPreferencesProvider pushPreferencesProvider;
    /* package */ static PushRequestHeaders pushRequestHeaders;


    // Used by unit tests
    /* package */ static void setPushPreferencesProvider(PushPreferencesProvider preferences) {
        AnalyticsEventService.pushPreferencesProvider = preferences;
    }

    public static Intent getIntentToRunJob(Context context, BaseJob job) {
        final Intent intent = new Intent(context, AnalyticsEventService.class);
        if (job != null) {
            intent.putExtra(KEY_JOB, job);
        }
        return intent;
    }

    public AnalyticsEventService() {
        super("AnalyticsEventService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        setupLogger();

        try {

            if (intent != null) {

                final ResultReceiver resultReceiver = getResultReceiver(intent);

                if (hasJob(intent)) {
                    final BaseJob job = getJobFromIntent(intent);
                    if (AnalyticsEventService.pushPreferencesProvider == null) {
                        AnalyticsEventService.pushPreferencesProvider = new PushPreferencesProviderImpl(this);
                    }
                    if (AnalyticsEventService.pushRequestHeaders == null) {
                        AnalyticsEventService.pushRequestHeaders = PushRequestHeaders.getInstance(this);
                    }
                    if ((job instanceof CheckBackEndVersionJob && Pivotal.getAreAnalyticsEnabled(this)) || pushPreferencesProvider.areAnalyticsEnabled()) {
                        setupStatics(intent);
                        runJob(job, resultReceiver);
                    } else {
                        sendResult(ANALYTICS_DISABLED, resultReceiver);
                    }
                }
            }

        } finally {
            postProcessAfterService(intent);
        }
    }

    private void setupStatics(Intent intent) {

        boolean needToCleanDatabase = false;

        if (AnalyticsEventService.eventsStorage == null) {
            needToCleanDatabase = setupDatabase();
        }
        if (AnalyticsEventService.alarmProvider == null) {
            AnalyticsEventService.alarmProvider = new AnalyticsEventsSenderAlarmProviderImpl(this);
        }
        if (AnalyticsEventService.timeProvider == null) {
            AnalyticsEventService.timeProvider = new TimeProvider();
        }
        if (AnalyticsEventService.networkWrapper == null) {
            AnalyticsEventService.networkWrapper = new NetworkWrapperImpl();
        }
        if (AnalyticsEventService.serviceStarter == null) {
            AnalyticsEventService.serviceStarter = new ServiceStarterImpl();
        }
        if (AnalyticsEventService.sendAnalyticsRequestProvider == null) {
            final PCFPushSendAnalyticsApiRequestImpl request = new PCFPushSendAnalyticsApiRequestImpl(this, AnalyticsEventService.eventsStorage, AnalyticsEventService.pushPreferencesProvider, AnalyticsEventService.pushRequestHeaders, AnalyticsEventService.networkWrapper);
            AnalyticsEventService.sendAnalyticsRequestProvider = new PCFPushSendAnalyticsApiRequestProvider(request);
        }
        if (AnalyticsEventService.checkBackEndVersionRequestProvider == null) {
            final PCFPushCheckBackEndVersionApiRequestImpl request = new PCFPushCheckBackEndVersionApiRequestImpl(this, AnalyticsEventService.pushPreferencesProvider, AnalyticsEventService.pushRequestHeaders, AnalyticsEventService.networkWrapper);
            AnalyticsEventService.checkBackEndVersionRequestProvider = new PCFPushCheckBackEndVersionApiRequestProvider(request);
        }

        if (!isIntentForSetup(intent) && needToCleanDatabase) {
            Logger.i("Instantiating database.");
            final BaseJob referringJob = getJobFromIntent(intent);
            cleanDatabase(referringJob);
        }
    }

    // If the service gets started in the background without the rest of the application running, then it will
    // have to kick off the logger itself.
    private void setupLogger() {
        if (!Logger.isSetup()) {
            Logger.setup(this);
        }
    }

    private boolean setupDatabase() {
        final boolean wasDatabaseInstanceCreated = DatabaseWrapper.createDatabaseInstance(this);
        AnalyticsEventService.eventsStorage = new DatabaseAnalyticsEventsStorage();
        return wasDatabaseInstanceCreated;
    }

    private void cleanDatabase(final BaseJob referringJob) {
        final boolean canSendEvents = !(referringJob instanceof EnqueueAnalyticsEventJob) && !(referringJob instanceof SendAnalyticsEventsJob);
        final PrepareDatabaseJob job = new PrepareDatabaseJob(canSendEvents);
        runJob(job, null); // no result receiver used in this hard-coded job
    }

    private ResultReceiver getResultReceiver(Intent intent) {
        ResultReceiver resultReceiver = null;
        if (intent.hasExtra(KEY_RESULT_RECEIVER)) {
            // Used by unit tests
            resultReceiver = intent.getParcelableExtra(KEY_RESULT_RECEIVER);
            intent.removeExtra(KEY_RESULT_RECEIVER);
        }
        return resultReceiver;
    }

    private boolean isIntentForSetup(Intent intent) {
        if (intent == null || !hasJob(intent)) {
            return false;
        }

        BaseJob job = getJobFromIntent(intent);
        if (job == null) {
            return false;
        }

        return (job instanceof PrepareDatabaseJob) || (job instanceof CheckBackEndVersionJob);
    }

    private boolean hasJob(Intent intent) {
        return (getJobFromIntent(intent) != null);
    }

    private BaseJob getJobFromIntent(Intent intent) {

        if (!intent.hasExtra(KEY_JOB)) {
            return null;
        }

        final Object o = intent.getParcelableExtra(KEY_JOB);
        if (!(o instanceof BaseJob)) {
            return null;
        }
        return (BaseJob) o;
    }

    private void runJob(BaseJob job, final ResultReceiver resultReceiver) {
        final Semaphore runJobSemaphore = new Semaphore(0);

        job.run(getJobParams(new JobResultListener() {

            @Override
            public void onJobComplete(int resultCode) {
                sendResult(resultCode, resultReceiver);
                runJobSemaphore.release();
            }
        }));

        try {
            runJobSemaphore.acquire();
            recordCompletedJob(job);
        } catch (InterruptedException e) {
            Logger.ex("Got interrupted while trying to run job '" + job.toString() + "'.", e);
            sendResult(JOB_INTERRUPTED, resultReceiver);
        }
    }

    private JobParams getJobParams(JobResultListener listener) {
        return new JobParams(this,
                listener,
                AnalyticsEventService.timeProvider,
                AnalyticsEventService.networkWrapper,
                AnalyticsEventService.serviceStarter,
                AnalyticsEventService.eventsStorage,
                AnalyticsEventService.pushPreferencesProvider,
                AnalyticsEventService.alarmProvider,
                AnalyticsEventService.sendAnalyticsRequestProvider,
                AnalyticsEventService.checkBackEndVersionRequestProvider);
    }

    // Used by unit tests
    private void sendResult(int resultCode, ResultReceiver resultReceiver) {
        if (resultReceiver != null) {
            resultReceiver.send(resultCode, null);
        }
    }

    // Used by unit tests
    private void recordCompletedJob(BaseJob job) {
        if (AnalyticsEventService.listOfCompletedJobs != null) {
            AnalyticsEventService.listOfCompletedJobs.add(job.toString());
        }
    }

    private void postProcessAfterService(Intent intent) {

        try {

            cleanupStatics();

            // If unit tests are running then release them so that they can continue
            if (AnalyticsEventService.semaphore != null) {
                AnalyticsEventService.semaphore.release();
            }

        } finally {

            // Release the wake lock provided by the WakefulBroadcastReceiver.
            // SUPER IMPORTANT! Make sure that this gets called EVERY time this service is invoked, but not until AFTER
            // any requests are completed -- otherwise the device might return to sleep before the request is complete.
            if (intent != null) {
                AnalyticsEventsSenderAlarmReceiver.completeWakefulIntent(intent);
            }
        }
    }

    private void cleanupStatics() {
        AnalyticsEventService.eventsStorage = null;
        AnalyticsEventService.alarmProvider = null;
        AnalyticsEventService.networkWrapper = null;
        AnalyticsEventService.serviceStarter = null;
        AnalyticsEventService.pushPreferencesProvider = null;
        AnalyticsEventService.sendAnalyticsRequestProvider = null;
        AnalyticsEventService.checkBackEndVersionRequestProvider = null;
        AnalyticsEventService.listOfCompletedJobs = null;
    }
}
