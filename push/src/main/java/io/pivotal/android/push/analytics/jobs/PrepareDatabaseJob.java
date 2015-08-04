package io.pivotal.android.push.analytics.jobs;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import io.pivotal.android.push.model.analytics.Event;
import io.pivotal.android.push.util.Logger;

public class PrepareDatabaseJob extends BaseJob {

    public PrepareDatabaseJob() {
        super();
    }

    @Override
    public void run(JobParams jobParams) {
        cleanupDatabase(jobParams);
        enableAlarmIfRequired(jobParams);
        jobParams.listener.onJobComplete(JobResultListener.RESULT_SUCCESS);
    }

    private void cleanupDatabase(JobParams jobParams) {
        int numberOfFixedEvents = 0;
        numberOfFixedEvents += fixEventsWithStatus(Event.Status.POSTING, jobParams);
        numberOfFixedEvents += deleteEventsWithStatus(Event.Status.POSTED, jobParams);
        if (numberOfFixedEvents <= 0) {
            Logger.fd("PrepareDatabaseJob: no events in the database that need to be cleaned.", numberOfFixedEvents);
        }
    }

    private int fixEventsWithStatus(int status, JobParams jobParams) {
        final List<Uri> uris = jobParams.eventsStorage.getEventUrisWithStatus(status);
        if (uris.size() > 0) {
            for (final Uri uri : uris) {
                jobParams.eventsStorage.setEventStatus(uri, Event.Status.NOT_POSTED);
            }
            Logger.fd("PrepareDatabaseJob: set %d '%s' events to status '%s'", uris.size(), Event.statusString(status), Event.statusString(Event.Status.NOT_POSTED));
        }
        return uris.size();
    }

    private int deleteEventsWithStatus(int status, JobParams jobParams) {
        final List<Uri> uris = jobParams.eventsStorage.getEventUrisWithStatus(status);
        jobParams.eventsStorage.deleteEvents(uris);
        if (uris.size() > 0) {
            Logger.fd("PrepareDatabaseJob: deleted %d events with status '%s'", uris.size(), Event.statusString(status));
        }
        return uris.size();
    }

    private void enableAlarmIfRequired(JobParams jobParams) {
        int numberOfPendingMessageReceipts = 0;
        numberOfPendingMessageReceipts += jobParams.eventsStorage.getEventUrisWithStatus(Event.Status.NOT_POSTED).size();
        numberOfPendingMessageReceipts += jobParams.eventsStorage.getEventUrisWithStatus(Event.Status.POSTING_ERROR).size();
        if (numberOfPendingMessageReceipts > 0) {
            Logger.fd("PrepareDatabaseJob: There are %d events(s) queued for sending. Enabling alarm.", numberOfPendingMessageReceipts);
            jobParams.alarmProvider.enableAlarmIfDisabled();
        } else {
            Logger.d("PrepareDatabaseJob: There are no events queued for sending. Disabling alarm.");
            jobParams.alarmProvider.disableAlarm();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof PrepareDatabaseJob)) {
            return false;
        }
        return true;
    }

    // Parcelable stuff

    public static final Parcelable.Creator<PrepareDatabaseJob> CREATOR = new Parcelable.Creator<PrepareDatabaseJob>() {

        public PrepareDatabaseJob createFromParcel(Parcel in) {
            return new PrepareDatabaseJob(in);
        }

        public PrepareDatabaseJob[] newArray(int size) {
            return new PrepareDatabaseJob[size];
        }
    };

    private PrepareDatabaseJob(Parcel in) {
        super(in);
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
    }
}
