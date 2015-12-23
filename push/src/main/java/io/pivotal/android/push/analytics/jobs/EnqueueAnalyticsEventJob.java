package io.pivotal.android.push.analytics.jobs;

import android.os.Parcel;
import android.os.Parcelable;

import io.pivotal.android.push.model.analytics.AnalyticsEvent;
import io.pivotal.android.push.util.Logger;

public class EnqueueAnalyticsEventJob extends BaseJob {

    public static final int RESULT_COULD_NOT_SAVE_EVENT_TO_STORAGE = 200;

    private AnalyticsEvent event;

    public EnqueueAnalyticsEventJob(AnalyticsEvent event) {
        super();
        verifyArguments(event);
        saveArguments(event);
    }

    private void verifyArguments(AnalyticsEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event may not be null");
        }
    }

    private void saveArguments(AnalyticsEvent event) {
        this.event = event;
    }

    public AnalyticsEvent getEvent() {
        return this.event;
    }

    @Override
    public void run(final JobParams jobParams) {
        if (saveEvent(jobParams)) {
            sendJobResult(JobResultListener.RESULT_SUCCESS, jobParams);
        } else {
            sendJobResult(EnqueueAnalyticsEventJob.RESULT_COULD_NOT_SAVE_EVENT_TO_STORAGE, jobParams);
        }
    }

    private boolean saveEvent(JobParams jobParams) {
        if (jobParams.eventsStorage.saveEvent(event) != null) {
            Logger.fd("EnqueueEventJob: Enqueuing event with type '%s'. There are now %d events queued to send to the server.",
                    event.getEventType(),
                    jobParams.eventsStorage.getNumberOfEvents());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (!(o instanceof EnqueueAnalyticsEventJob)) {
            return false;
        }

        final EnqueueAnalyticsEventJob otherJob = (EnqueueAnalyticsEventJob) o;

        if (event == null && otherJob.event != null) {
            return false;
        }
        if (event != null && otherJob.event == null) {
            return false;
        }
        if (event != null && otherJob.event != null && !event.equals(otherJob.event)) {
            return false;
        }

        return true;
    }

    // Parcelable stuff

    public static final Parcelable.Creator<EnqueueAnalyticsEventJob> CREATOR = new Parcelable.Creator<EnqueueAnalyticsEventJob>() {

        public EnqueueAnalyticsEventJob createFromParcel(Parcel in) {
            return new EnqueueAnalyticsEventJob(in);
        }

        public EnqueueAnalyticsEventJob[] newArray(int size) {
            return new EnqueueAnalyticsEventJob[size];
        }
    };

    private EnqueueAnalyticsEventJob(Parcel in) {
        super(in);
        event = readEventFromParcel(in);
    }

    private AnalyticsEvent readEventFromParcel(Parcel parcel) {
        return parcel.readParcelable(AnalyticsEvent.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeParcelable(event, flags);
    }
}
