package io.pivotal.android.push.analytics.jobs;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

import io.pivotal.android.push.backend.analytics.PCFPushCheckBackEndVersionApiRequest;
import io.pivotal.android.push.backend.analytics.PCFPushCheckBackEndVersionListener;
import io.pivotal.android.push.prefs.PushPreferencesProvider;
import io.pivotal.android.push.util.Logger;
import io.pivotal.android.push.util.TimeProvider;
import io.pivotal.android.push.version.Version;

public class CheckBackEndVersionJob extends BaseJob {

    public static final int RESULT_SERVER_VERSION_RETRIEVED_SUCCESSFULLY = 100;
    public static final int RESULT_SERVER_VERSION_OLD = 101;
    public static final int RESULT_SERVER_VERSION_FAILED = 102;

    private static final int MAX_NUMBER_OF_ATTEMPTS = 3;
    private static final long RETRY_DELAY = 30000; // 30 seconds
    private int currentAttempt = 1;

    public CheckBackEndVersionJob() {
        super();
    }

    @Override
    public void run(final JobParams jobParams) {

        final PCFPushCheckBackEndVersionApiRequest request = jobParams.checkBackEndVersionRequestProvider.getRequest();
        request.startCheckBackEndVersion(new PCFPushCheckBackEndVersionListener() {

            @Override
            public void onCheckBackEndVersionSuccess(Version version) {
                jobParams.pushPreferencesProvider.setBackEndVersion(version);
                jobParams.pushPreferencesProvider.setBackEndVersionTimePolled(new Date(jobParams.timeProvider.currentTimeMillis()));

                if (jobParams.pushPreferencesProvider.areAnalyticsEnabled()) {
                    Logger.i("The PCF Push back-end server supports analytics.  Analytics event logging is enabled.");
                } else {
                    Logger.i("The PCF Push back-end server does NOT support analytics (but still has the version endpoint!!).  Analytics event logging is disabled.");
                }

                sendJobResult(RESULT_SERVER_VERSION_RETRIEVED_SUCCESSFULLY, jobParams);
            }

            @Override
            public void onCheckBackEndVersionIsOldVersion() {
                jobParams.pushPreferencesProvider.setBackEndVersion(null);
                jobParams.pushPreferencesProvider.setBackEndVersionTimePolled(new Date(jobParams.timeProvider.currentTimeMillis()));
                Logger.i("The PCF Push back-end server does NOT support analytics.  Analytics event logging is disabled.");
                sendJobResult(RESULT_SERVER_VERSION_OLD, jobParams);
            }

            @Override
            public void onCheckBackEndVersionRetryableFailure(String reason) {
                if (currentAttempt >= MAX_NUMBER_OF_ATTEMPTS) {
                    sendJobResult(RESULT_SERVER_VERSION_FAILED, jobParams);
                } else {
                    Logger.i("Delaying before retrying to check the back-end server version...");
                    jobParams.timeProvider.sleep(RETRY_DELAY);
                    currentAttempt += 1;
                    run(jobParams);
                }
            }

            @Override
            public void onCheckBackEndVersionFatalFailure(String reason) {
                Logger.i("Not able to successfully check the back-end server version.");
                sendJobResult(RESULT_SERVER_VERSION_FAILED, jobParams);
            }
        });
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        return o instanceof CheckBackEndVersionJob;
    }

    public static boolean isPollingTime(boolean isDebug, TimeProvider timeProvider, PushPreferencesProvider preferencesProvider) {
        final long pollingTimeElapsesInterval;
        if (isDebug) {
            pollingTimeElapsesInterval = 5 * 60 * 1000L; // 5 minutes in debug mode
        } else {
            pollingTimeElapsesInterval = 24 * 60 * 60 * 1000L; // 24 hours in release mode
        }
        final Date backEndVersionTimePolled = preferencesProvider.getBackEndVersionTimePolled();
        if (backEndVersionTimePolled == null) {
            return true;
        }
        return timeProvider.currentTimeMillis() >= backEndVersionTimePolled.getTime() + pollingTimeElapsesInterval;
    }

    // Parcelable stuff

    public static final Parcelable.Creator<CheckBackEndVersionJob> CREATOR = new Parcelable.Creator<CheckBackEndVersionJob>() {

        public CheckBackEndVersionJob createFromParcel(Parcel in) {
            return new CheckBackEndVersionJob(in);
        }

        public CheckBackEndVersionJob[] newArray(int size) {
            return new CheckBackEndVersionJob[size];
        }
    };

    private CheckBackEndVersionJob(Parcel in) {
        super(in);
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
    }

}
