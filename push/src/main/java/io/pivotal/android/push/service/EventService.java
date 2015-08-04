package io.pivotal.android.push.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.ResultReceiver;

import java.util.List;
import java.util.concurrent.Semaphore;

import io.pivotal.android.push.analytics.jobs.BaseJob;
import io.pivotal.android.push.analytics.jobs.JobParams;
import io.pivotal.android.push.analytics.jobs.JobResultListener;
import io.pivotal.android.push.analytics.jobs.PrepareDatabaseJob;
import io.pivotal.android.push.backend.analytics.PCFPushSendAnalyticsApiRequestImpl;
import io.pivotal.android.push.backend.analytics.PCFPushSendAnalyticsApiRequestProvider;
import io.pivotal.android.push.database.DatabaseEventsStorage;
import io.pivotal.android.push.database.DatabaseWrapper;
import io.pivotal.android.push.database.EventsStorage;
import io.pivotal.android.push.prefs.Pivotal;
import io.pivotal.android.push.prefs.PushPreferencesProvider;
import io.pivotal.android.push.prefs.PushPreferencesProviderImpl;
import io.pivotal.android.push.receiver.EventsSenderAlarmProvider;
import io.pivotal.android.push.receiver.EventsSenderAlarmProviderImpl;
import io.pivotal.android.push.receiver.EventsSenderAlarmReceiver;
import io.pivotal.android.push.util.Logger;
import io.pivotal.android.push.util.NetworkWrapper;
import io.pivotal.android.push.util.NetworkWrapperImpl;

public class EventService extends IntentService {

    public static final String KEY_RESULT_RECEIVER = "result_receiver";
    public static final String KEY_JOB = "job";

    public static final int NO_RESULT = -1;
    public static final int JOB_INTERRUPTED = 1;
    public static final int ANALYTICS_DISABLED = 2;

    // Used by unit tests
    /* package */ static Semaphore semaphore = null;
    /* package */ static EventsStorage eventsStorage = null;
    /* package */ static NetworkWrapper networkWrapper = null;
    /* package */ static EventsSenderAlarmProvider alarmProvider = null;
    /* package */ static PCFPushSendAnalyticsApiRequestProvider requestProvider = null;
    /* package */ static List<String> listOfCompletedJobs = null;
    /* package */ static PushPreferencesProvider pushPreferencesProvider;


    // Used by unit tests
    /* package */ static void setPushPreferencesProvider(PushPreferencesProvider preferences) {
        EventService.pushPreferencesProvider = preferences;
    }

    public static Intent getIntentToRunJob(Context context, BaseJob job) {
        final Intent intent = new Intent(context, EventService.class);
        if (job != null) {
            intent.putExtra(KEY_JOB, job);
        }
        return intent;
    }

    public EventService() {
        super("EventService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        setupLogger();

        try {

            if (intent != null) {

                final ResultReceiver resultReceiver = getResultReceiver(intent);

                if (Pivotal.getAreAnalyticsEnabled(this)) {
                    if (hasJob(intent)) {
                        final BaseJob job = getJobFromIntent(intent);
                        setupStatics(intent);
                        runJob(job, resultReceiver);
                    }
                } else {
                    sendResult(ANALYTICS_DISABLED, resultReceiver);
                }
            }

        } finally {
            postProcessAfterService(intent);
        }
    }

    private void setupStatics(Intent intent) {

        boolean needToCleanDatabase = false;

        if (EventService.pushPreferencesProvider == null) {
            EventService.pushPreferencesProvider = new PushPreferencesProviderImpl(this);
        }
        if (EventService.eventsStorage == null) {
            needToCleanDatabase = setupDatabase();
        }
        if (EventService.alarmProvider == null) {
            EventService.alarmProvider = new EventsSenderAlarmProviderImpl(this);
        }
        if (EventService.networkWrapper == null) {
            EventService.networkWrapper = new NetworkWrapperImpl();
        }
        if (EventService.requestProvider == null) {
            final PCFPushSendAnalyticsApiRequestImpl request = new PCFPushSendAnalyticsApiRequestImpl(this, EventService.eventsStorage, EventService.pushPreferencesProvider, EventService.networkWrapper);
            EventService.requestProvider = new PCFPushSendAnalyticsApiRequestProvider(request);
        }

        if (!isIntentForCleanup(intent) && needToCleanDatabase) {
            cleanDatabase();
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
        EventService.eventsStorage = new DatabaseEventsStorage();
        return wasDatabaseInstanceCreated;
    }

    private void cleanDatabase() {
        final PrepareDatabaseJob job = new PrepareDatabaseJob();
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

    private boolean isIntentForCleanup(Intent intent) {
        if (intent == null || !hasJob(intent)) {
            return false;
        }

        BaseJob job = getJobFromIntent(intent);
        if (job == null) {
            return false;
        }

        return (job instanceof PrepareDatabaseJob);
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
                EventService.networkWrapper,
                EventService.eventsStorage,
                EventService.pushPreferencesProvider,
                EventService.alarmProvider,
                EventService.requestProvider);
    }

    // Used by unit tests
    private void sendResult(int resultCode, ResultReceiver resultReceiver) {
        if (resultReceiver != null) {
            resultReceiver.send(resultCode, null);
        }
    }

    // Used by unit tests
    private void recordCompletedJob(BaseJob job) {
        if (EventService.listOfCompletedJobs != null) {
            EventService.listOfCompletedJobs.add(job.toString());
        }
    }

    private void postProcessAfterService(Intent intent) {

        try {

            cleanupStatics();

            // If unit tests are running then release them so that they can continue
            if (EventService.semaphore != null) {
                EventService.semaphore.release();
            }

        } finally {

            // Release the wake lock provided by the WakefulBroadcastReceiver.
            // SUPER IMPORTANT! Make sure that this gets called EVERY time this service is invoked, but not until AFTER
            // any requests are completed -- otherwise the device might return to sleep before the request is complete.
            if (intent != null) {
                EventsSenderAlarmReceiver.completeWakefulIntent(intent);
            }
        }
    }

    private void cleanupStatics() {
        EventService.eventsStorage = null;
        EventService.alarmProvider = null;
        EventService.networkWrapper = null;
        EventService.pushPreferencesProvider = null;
        EventService.requestProvider = null;
        EventService.listOfCompletedJobs = null;
    }
}
