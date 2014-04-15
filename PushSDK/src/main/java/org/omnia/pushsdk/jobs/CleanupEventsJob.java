package org.omnia.pushsdk.jobs;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import org.omnia.pushsdk.database.EventsStorage;
import org.omnia.pushsdk.model.BaseEvent;
import org.omnia.pushsdk.util.PushLibLogger;

import java.util.List;

public class CleanupEventsJob extends BaseJob {

    public CleanupEventsJob() {
        super();
    }

    @Override
    public void run(JobParams jobParams) {
        int numberOfFixedEvents = 0;
        numberOfFixedEvents += fixEventsWithStatus(BaseEvent.Status.POSTING, jobParams);
        numberOfFixedEvents += fixEventsWithStatus(BaseEvent.Status.POSTING_ERROR, jobParams);
        if (numberOfFixedEvents > 0) {
            PushLibLogger.fd("CleanupEventsJob: reset the status for %d POSTING or POSTING_ERROR events in the database.", numberOfFixedEvents);
        }
        jobParams.listener.onJobComplete(JobResultListener.RESULT_SUCCESS);
    }

    private int fixEventsWithStatus(int status, JobParams jobParams) {
        final List<Uri> uris = jobParams.eventsStorage.getEventUrisWithStatus(EventsStorage.EventType.MESSAGE_RECEIPT, status);
        fixEvents(jobParams, uris);
        return uris.size();
    }

    private void fixEvents(JobParams jobParams, List<Uri> uris) {
        for (final Uri uri : uris) {
            jobParams.eventsStorage.setEventStatus(uri, BaseEvent.Status.NOT_POSTED);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof CleanupEventsJob)) {
            return false;
        }
        return true;
    }

    // Parcelable stuff

    public static final Parcelable.Creator<CleanupEventsJob> CREATOR = new Parcelable.Creator<CleanupEventsJob>() {

        public CleanupEventsJob createFromParcel(Parcel in) {
            return new CleanupEventsJob(in);
        }

        public CleanupEventsJob[] newArray(int size) {
            return new CleanupEventsJob[size];
        }
    };

    private CleanupEventsJob(Parcel in) {
        super(in);
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
    }
}
