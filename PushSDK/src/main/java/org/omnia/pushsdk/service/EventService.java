package org.omnia.pushsdk.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.ResultReceiver;

import org.omnia.pushsdk.backend.BackEndMessageReceiptApiRequestImpl;
import org.omnia.pushsdk.backend.BackEndMessageReceiptApiRequestProvider;
import org.omnia.pushsdk.broadcastreceiver.MessageReceiptAlarmProvider;
import org.omnia.pushsdk.broadcastreceiver.MessageReceiptAlarmProviderImpl;
import org.omnia.pushsdk.broadcastreceiver.MessageReceiptAlarmReceiver;
import org.omnia.pushsdk.database.DatabaseEventsStorage;
import org.omnia.pushsdk.database.EventsDatabaseHelper;
import org.omnia.pushsdk.database.EventsDatabaseWrapper;
import org.omnia.pushsdk.database.EventsStorage;
import org.omnia.pushsdk.jobs.BaseJob;
import org.omnia.pushsdk.jobs.JobParams;
import org.omnia.pushsdk.jobs.JobResultListener;
import org.omnia.pushsdk.network.NetworkWrapper;
import org.omnia.pushsdk.network.NetworkWrapperImpl;
import org.omnia.pushsdk.prefs.PreferencesProvider;
import org.omnia.pushsdk.prefs.PreferencesProviderImpl;
import org.omnia.pushsdk.util.Const;
import org.omnia.pushsdk.util.PushLibLogger;

import java.util.concurrent.Semaphore;

public class EventService extends IntentService {

    public static final String KEY_RESULT_RECEIVER = "result_receiver";
    public static final String KEY_JOB = "job";

    public static final int NO_RESULT = -1;
    public static final int JOB_INTERRUPTED = 1;

    private ResultReceiver resultReceiver = null;

    // Used by unit tests
    /* package */ static Semaphore semaphore = null;
    /* package */ static NetworkWrapper networkWrapper = null;
    /* package */ static EventsStorage eventsStorage = null;
    /* package */ static PreferencesProvider preferencesProvider = null;
    /* package */ static MessageReceiptAlarmProvider alarmProvider = null;
    /* package */ static BackEndMessageReceiptApiRequestProvider backEndMessageReceiptApiRequestProvider = null;

    public EventService() {
        super("EventService");
    }

    private void setupStatics() {

        setupLogger();

        if (EventService.networkWrapper == null) {
            EventService.networkWrapper = new NetworkWrapperImpl();
        }
        if (EventService.eventsStorage == null) {
            setupDatabase();
        }
        if (EventService.preferencesProvider == null) {
            EventService.preferencesProvider = new PreferencesProviderImpl(this);
        }
        if (EventService.alarmProvider == null) {
            EventService.alarmProvider = new MessageReceiptAlarmProviderImpl(this);
        }
        if (EventService.backEndMessageReceiptApiRequestProvider == null) {
            final NetworkWrapper networkWrapper = new NetworkWrapperImpl();
            final BackEndMessageReceiptApiRequestImpl backEndMessageReceiptApiRequest = new BackEndMessageReceiptApiRequestImpl(this, eventsStorage, networkWrapper);
            EventService.backEndMessageReceiptApiRequestProvider = new BackEndMessageReceiptApiRequestProvider(backEndMessageReceiptApiRequest);
        }
    }

    // If the service gets started in the background without the rest of the application running, then it will
    // have to kick off the logger itself.
    private void setupLogger() {
        if (!PushLibLogger.isSetup()) {
            PushLibLogger.setup(this, Const.TAG_NAME);
        }
    }

    private void setupDatabase() {
        EventsDatabaseHelper.init();
        EventsDatabaseWrapper.createDatabaseInstance(this);
        EventService.eventsStorage = new DatabaseEventsStorage();
    }

    private void cleanupStatics() {
        EventService.networkWrapper = null;
        EventService.eventsStorage = null;
        EventService.preferencesProvider = null;
        EventService.alarmProvider = null;
        EventService.backEndMessageReceiptApiRequestProvider = null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        setupStatics();

        if (intent == null) {
            postProcessAfterService(intent);
            return;
        }

        getResultReceiver(intent);

        if (hasJob(intent)) {
            runJob(intent);
        }

        postProcessAfterService(intent);
    }

    private void getResultReceiver(Intent intent) {
        if (intent.hasExtra(KEY_RESULT_RECEIVER)) {
            // Used by unit tests
            resultReceiver = intent.getParcelableExtra(KEY_RESULT_RECEIVER);
            intent.removeExtra(KEY_RESULT_RECEIVER);
        }
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

    private void runJob(Intent intent) {
        final BaseJob job = getJobFromIntent(intent);
        final Semaphore runJobSemaphore = new Semaphore(0);

        job.run(getJobParams(new JobResultListener() {

            @Override
            public void onJobComplete(int resultCode) {
                sendResult(resultCode);
                runJobSemaphore.release();
            }
        }));

        try {
            runJobSemaphore.acquire();
        } catch (InterruptedException e) {
            sendResult(JOB_INTERRUPTED);
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

    private void sendResult(int resultCode) {
        if (resultReceiver != null) {
            // Used by unit tests
            resultReceiver.send(resultCode, null);
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
                MessageReceiptAlarmReceiver.completeWakefulIntent(intent);
            }
        }
    }

    public static Intent getIntentToRunJob(Context context, BaseJob job) {
        final Intent intent = new Intent(context, EventService.class);
        if (job != null) {
            intent.putExtra(KEY_JOB, job);
        }
        return intent;
    }
}
