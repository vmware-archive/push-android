package com.pivotal.cf.mobile.analyticssdk.jobs;

import android.os.Parcel;
import android.os.Parcelable;

import com.pivotal.cf.mobile.analyticssdk.model.events.Event;
import com.pivotal.cf.mobile.common.util.Logger;

public class EnqueueEventJob extends BaseJob {

    public static final int RESULT_COULD_NOT_SAVE_EVENT_TO_STORAGE = 200;

    private Event event;

    public EnqueueEventJob(Event event) {
        super();
        verifyArguments(event);
        saveArguments(event);
    }

    private void verifyArguments(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("event may not be null");
        }
    }

    private void saveArguments(Event event) {
        this.event = event;
    }

    public Event getEvent() {
        return this.event;
    }

    @Override
    public void run(JobParams jobParams) {
        if (saveEvent(jobParams)) {
            enableAlarm(jobParams);
            sendJobResult(JobResultListener.RESULT_SUCCESS, jobParams);
        } else {
            sendJobResult(EnqueueEventJob.RESULT_COULD_NOT_SAVE_EVENT_TO_STORAGE, jobParams);
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

    private void enableAlarm(JobParams jobParams) {
        jobParams.alarmProvider.enableAlarmIfDisabled();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (!(o instanceof EnqueueEventJob)) {
            return false;
        }

        final EnqueueEventJob otherJob = (EnqueueEventJob) o;

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

    public static final Parcelable.Creator<EnqueueEventJob> CREATOR = new Parcelable.Creator<EnqueueEventJob>() {

        public EnqueueEventJob createFromParcel(Parcel in) {
            return new EnqueueEventJob(in);
        }

        public EnqueueEventJob[] newArray(int size) {
            return new EnqueueEventJob[size];
        }
    };

    private EnqueueEventJob(Parcel in) {
        super(in);
        event = readEventFromParcel(in);
    }

    private Event readEventFromParcel(Parcel parcel) {
        return parcel.readParcelable(Event.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeParcelable(event, flags);
    }
}
