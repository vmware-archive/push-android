package com.pivotal.cf.mobile.pushsdk.jobs;

import android.os.Parcel;
import android.os.Parcelable;

import com.pivotal.cf.mobile.pushsdk.model.BaseEvent;
import com.pivotal.cf.mobile.pushsdk.model.utilities.EventHelper;
import com.pivotal.cf.mobile.pushsdk.util.PushLibLogger;

public class EnqueueEventJob extends BaseJob {

    public static final int RESULT_COULD_NOT_SAVE_EVENT_TO_STORAGE = 200;

    private BaseEvent event;

    public EnqueueEventJob(BaseEvent event) {
        super();
        verifyArguments(event);
        saveArguments(event);
    }

    private void verifyArguments(BaseEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event may not be null");
        }
    }

    private void saveArguments(BaseEvent event) {
        this.event = event;
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

    // TODO generalize to other kinds of events
    private boolean saveEvent(JobParams jobParams) {
        if (jobParams.eventsStorage.saveEvent(event) != null) {
            PushLibLogger.d("EnqueueEventJob: There are now " + jobParams.eventsStorage.getNumberOfEvents() + " message receipts queued to send to the server.");
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
        event = EventHelper.readEventFromParcel(in);
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeParcelable(event, flags);
    }
}
