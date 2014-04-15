package org.omnia.pushsdk.jobs;

import android.os.Parcel;
import android.os.Parcelable;

import org.omnia.pushsdk.database.EventsStorage;
import org.omnia.pushsdk.model.EventBase;
import org.omnia.pushsdk.model.utilities.EventHelper;
import org.omnia.pushsdk.util.PushLibLogger;

public class EnqueueEventJob extends BaseJob {

    private EventBase event;
    private EventsStorage.EventType eventType;

    public EnqueueEventJob(EventBase event, EventsStorage.EventType eventType) {
        super();
        verifyArguments(event, eventType);
        saveArguments(event, eventType);
    }

    private void verifyArguments(EventBase event, EventsStorage.EventType eventType) {
        if (event == null) {
            throw new IllegalArgumentException("event may not be null");
        }
        if (eventType == EventsStorage.EventType.ALL) {
            throw new IllegalArgumentException("eventType may not be ALL");
        }
    }

    private void saveArguments(EventBase event, EventsStorage.EventType eventType) {
        this.event = event;
        this.eventType = eventType;
    }

    @Override
    public void run(JobParams jobParams) {
        saveEvent(jobParams);
        enableAlarm(jobParams);
        sendJobResult(JobResultListener.RESULT_SUCCESS, jobParams);
    }

    private void saveEvent(JobParams jobParams) {
        jobParams.eventsStorage.saveEvent(event, eventType);
        PushLibLogger.d("EnqueueEventJob: There are now " + jobParams.eventsStorage.getNumberOfEvents(EventsStorage.EventType.MESSAGE_RECEIPT) + " message receipts queued to send to the server.");
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

        if (eventType != otherJob.eventType) {
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
        eventType = (EventsStorage.EventType) in.readSerializable();
        event = EventHelper.readEventFromParcel(in, eventType);
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeSerializable(eventType);
        out.writeParcelable(event, flags);
    }
}
