package io.pivotal.android.push.analytics.jobs;

import android.content.Intent;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import io.pivotal.android.push.model.analytics.AnalyticsEvent;
import io.pivotal.android.push.service.AnalyticsEventService;
import io.pivotal.android.push.util.Logger;

public class PrepareDatabaseJob extends BaseJob {

    private boolean canSendEvents;

    public PrepareDatabaseJob(boolean canSendEvents) {
        super();
        this.canSendEvents = canSendEvents;
    }

    @Override
    public void run(JobParams jobParams) {
        cleanupDatabase(jobParams);
        if (canSendEvents) {
            sendEventsIfRequired(jobParams);
        }
        jobParams.listener.onJobComplete(JobResultListener.RESULT_SUCCESS);
    }

    private void cleanupDatabase(JobParams jobParams) {
        int numberOfFixedEvents = 0;
        numberOfFixedEvents += fixEventsWithStatus(AnalyticsEvent.Status.POSTING, jobParams);
        numberOfFixedEvents += deleteEventsWithStatus(AnalyticsEvent.Status.POSTED, jobParams);
        if (numberOfFixedEvents <= 0) {
            Logger.fd("PrepareDatabaseJob: no events in the database that need to be cleaned.", numberOfFixedEvents);
        }
    }

    private int fixEventsWithStatus(int status, JobParams jobParams) {
        final List<Uri> uris = jobParams.eventsStorage.getEventUrisWithStatus(status);
        if (uris.size() > 0) {
            for (final Uri uri : uris) {
                jobParams.eventsStorage.setEventStatus(uri, AnalyticsEvent.Status.NOT_POSTED);
            }
            Logger.fd("PrepareDatabaseJob: set %d '%s' events to status '%s'", uris.size(), AnalyticsEvent.statusString(status), AnalyticsEvent.statusString(AnalyticsEvent.Status.NOT_POSTED));
        }
        return uris.size();
    }

    private int deleteEventsWithStatus(int status, JobParams jobParams) {
        final List<Uri> uris = jobParams.eventsStorage.getEventUrisWithStatus(status);
        jobParams.eventsStorage.deleteEvents(uris);
        if (uris.size() > 0) {
            Logger.fd("PrepareDatabaseJob: deleted %d events with status '%s'", uris.size(), AnalyticsEvent.statusString(status));
        }
        return uris.size();
    }

    private void sendEventsIfRequired(JobParams jobParams) {
        int numberOfPendingMessageReceipts = 0;
        numberOfPendingMessageReceipts += jobParams.eventsStorage.getEventUrisWithStatus(AnalyticsEvent.Status.NOT_POSTED).size();
        numberOfPendingMessageReceipts += jobParams.eventsStorage.getEventUrisWithStatus(AnalyticsEvent.Status.POSTING_ERROR).size();
        if (numberOfPendingMessageReceipts > 0) {
            Logger.fd("PrepareDatabaseJob: There are %d events(s) queued for sending. Enqueueing SendAnalyticsEventsJob.", numberOfPendingMessageReceipts);
            enqueueSendEventsJob(jobParams);
        } else {
            Logger.d("PrepareDatabaseJob: There are no events queued for sending. Disabling alarm.");
            jobParams.alarmProvider.disableAlarm();
        }
    }

    private void enqueueSendEventsJob(JobParams jobParams) {
        final SendAnalyticsEventsJob job = new SendAnalyticsEventsJob();
        final Intent intent = AnalyticsEventService.getIntentToRunJob(jobParams.context, job);
        jobParams.serviceStarter.startService(jobParams.context, intent);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof PrepareDatabaseJob)) {
            return false;
        }
        return canSendEvents == ((PrepareDatabaseJob) o).canSendEvents;
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
        canSendEvents = in.readInt() != 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeInt(canSendEvents ? 1 : 0);
    }
}
