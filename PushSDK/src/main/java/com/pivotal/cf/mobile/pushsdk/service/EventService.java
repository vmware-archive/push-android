package com.pivotal.cf.mobile.pushsdk.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.ResultReceiver;

import com.pivotal.cf.mobile.pushsdk.backend.BackEndMessageReceiptApiRequestImpl;
import com.pivotal.cf.mobile.pushsdk.backend.BackEndMessageReceiptApiRequestProvider;
import com.pivotal.cf.mobile.pushsdk.broadcastreceiver.EventsSenderAlarmProvider;
import com.pivotal.cf.mobile.pushsdk.broadcastreceiver.EventsSenderAlarmProviderImpl;
import com.pivotal.cf.mobile.pushsdk.broadcastreceiver.EventsSenderAlarmReceiver;
import com.pivotal.cf.mobile.pushsdk.database.DatabaseEventsStorage;
import com.pivotal.cf.mobile.pushsdk.database.EventsDatabaseHelper;
import com.pivotal.cf.mobile.pushsdk.database.EventsDatabaseWrapper;
import com.pivotal.cf.mobile.pushsdk.database.EventsStorage;
import com.pivotal.cf.mobile.pushsdk.jobs.BaseJob;
import com.pivotal.cf.mobile.pushsdk.jobs.JobParams;
import com.pivotal.cf.mobile.pushsdk.jobs.JobResultListener;
import com.pivotal.cf.mobile.pushsdk.jobs.PrepareDatabaseJob;
import com.pivotal.cf.mobile.pushsdk.network.NetworkWrapper;
import com.pivotal.cf.mobile.pushsdk.network.NetworkWrapperImpl;
import com.pivotal.cf.mobile.pushsdk.prefs.PreferencesProvider;
import com.pivotal.cf.mobile.pushsdk.prefs.PreferencesProviderImpl;
import com.pivotal.cf.mobile.pushsdk.util.Const;
import com.pivotal.cf.mobile.pushsdk.util.PushLibLogger;

import java.util.List;
import java.util.concurrent.Semaphore;

public class EventService extends IntentService {

    public static final String KEY_RESULT_RECEIVER = "result_receiver";
    public static final String KEY_JOB = "job";

    public static final int NO_RESULT = -1;
    public static final int JOB_INTERRUPTED = 1;

    // Used by unit tests
    /* package */ static Semaphore semaphore = null;
    /* package */ static NetworkWrapper networkWrapper = null;
    /* package */ static EventsStorage eventsStorage = null;
    /* package */ static PreferencesProvider preferencesProvider = null;
    /* package */ static EventsSenderAlarmProvider alarmProvider = null;
    /* package */ static BackEndMessageReceiptApiRequestProvider backEndMessageReceiptApiRequestProvider = null;
    /* package */ static List<String> listOfCompletedJobs = null;

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

        setupStatics(intent);

        if (intent != null) {
            if (hasJob(intent)) {
                final BaseJob job = getJobFromIntent(intent);
                final ResultReceiver resultReceiver = getResultReceiver(intent);
                runJob(job, resultReceiver);
            }
        }

        postProcessAfterService(intent);
    }

    private void setupStatics(Intent intent) {

        setupLogger();

        boolean needToCleanDatabase = false;

        if (EventService.networkWrapper == null) {
            EventService.networkWrapper = new NetworkWrapperImpl();
        }
        if (EventService.preferencesProvider == null) {
            EventService.preferencesProvider = new PreferencesProviderImpl(this);
        }
        if (EventService.alarmProvider == null) {
            EventService.alarmProvider = new EventsSenderAlarmProviderImpl(this);
        }
        if (EventService.eventsStorage == null) {
            needToCleanDatabase = setupDatabase();
        }
        if (EventService.backEndMessageReceiptApiRequestProvider == null) {
            final BackEndMessageReceiptApiRequestImpl backEndMessageReceiptApiRequest = new BackEndMessageReceiptApiRequestImpl(this, EventService.eventsStorage, EventService.networkWrapper);
            EventService.backEndMessageReceiptApiRequestProvider = new BackEndMessageReceiptApiRequestProvider(backEndMessageReceiptApiRequest);
        }

        if (!isIntentForCleanup(intent) && needToCleanDatabase) {
            cleanDatabase();
        }
    }

    // If the service gets started in the background without the rest of the application running, then it will
    // have to kick off the logger itself.
    private void setupLogger() {
        if (!PushLibLogger.isSetup()) {
            PushLibLogger.setup(this, Const.TAG_NAME);
        }
    }

    private boolean setupDatabase() {
        EventsDatabaseHelper.init();
        final boolean wasDatabaseInstanceCreated = EventsDatabaseWrapper.createDatabaseInstance(this);
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
            PushLibLogger.ex("Got interrupted while trying to run job '" + job.toString() + "'.", e);
            sendResult(JOB_INTERRUPTED, resultReceiver);
        }
    }

    private JobParams getJobParams(JobResultListener listener) {
        return new JobParams(this,
                listener,
                EventService.networkWrapper,
                EventService.eventsStorage,
                EventService.preferencesProvider,
                EventService.alarmProvider,
                EventService.backEndMessageReceiptApiRequestProvider);
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
        EventService.networkWrapper = null;
        EventService.eventsStorage = null;
        EventService.preferencesProvider = null;
        EventService.alarmProvider = null;
        EventService.backEndMessageReceiptApiRequestProvider = null;
        EventService.listOfCompletedJobs = null;
    }
}
