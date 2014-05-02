package com.pivotal.cf.mobile.analyticssdk.jobs;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.pivotal.cf.mobile.analyticssdk.backend.BackEndSendEventsApiRequest;
import com.pivotal.cf.mobile.analyticssdk.backend.BackEndSendEventsListener;
import com.pivotal.cf.mobile.analyticssdk.model.events.Event;
import com.pivotal.cf.mobile.common.util.Logger;

import java.util.LinkedList;
import java.util.List;

public class SendEventsJob extends BaseJob {

    public static final int RESULT_NO_WORK_TO_DO = 100;
    public static final int RESULT_FAILED_TO_SEND_RECEIPTS = 101;

    public SendEventsJob() {
        super();
    }

    @Override
    public void run(JobParams jobParams) {

        final List<Uri> uris = getUnpostedEvents(jobParams);
        Logger.fd("SendEventsJob: package %s: events available to send: %d", getPackageName(jobParams), uris.size());

        if (uris.size() > 0) {
            setStatusForEvents(jobParams, uris, Event.Status.POSTING);
            sendEvents(jobParams, uris);
        } else {
            sendJobResult(RESULT_NO_WORK_TO_DO, jobParams);
        }
    }

    private void setStatusForEvents(JobParams jobParams, List<Uri> uris, int status) {
        for (final Uri uri : uris) {
            jobParams.eventsStorage.setEventStatus(uri, status);
        }
    }

    private void sendEvents(final JobParams jobParams, final List<Uri> uris) {

        final BackEndSendEventsApiRequest apiRequest = jobParams.backEndSendEventsApiRequestProvider.getRequest();
        apiRequest.startSendEvents(uris, new BackEndSendEventsListener() {

            @Override
            public void onBackEndSendEventsSuccess() {
                if (uris != null) {
                    jobParams.eventsStorage.deleteEvents(uris);
                }
                sendJobResult(JobResultListener.RESULT_SUCCESS, jobParams);
            }

            @Override
            public void onBackEndSendEventsFailed(String reason) {
                setStatusForEvents(jobParams, uris, Event.Status.POSTING_ERROR);
                sendJobResult(RESULT_FAILED_TO_SEND_RECEIPTS, jobParams);
            }
        });
    }

    private String getPackageName(JobParams jobParams) {
        final String packageName = jobParams.context.getPackageName();
        return packageName;
    }

    private List<Uri> getUnpostedEvents(JobParams jobParams) {
        final List<Uri> uris1 = jobParams.eventsStorage.getEventUrisWithStatus(Event.Status.NOT_POSTED);
        final List<Uri> uris2 = jobParams.eventsStorage.getEventUrisWithStatus(Event.Status.POSTING_ERROR);
        final List<Uri> uris = new LinkedList<Uri>(uris1);
        uris.addAll(uris2);
        return uris;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof SendEventsJob)) {
            return false;
        }
        return true;
    }

    // Parcelable stuff

    public static final Parcelable.Creator<SendEventsJob> CREATOR = new Parcelable.Creator<SendEventsJob>() {

        public SendEventsJob createFromParcel(Parcel in) {
            return new SendEventsJob(in);
        }

        public SendEventsJob[] newArray(int size) {
            return new SendEventsJob[size];
        }
    };

    private SendEventsJob(Parcel in) {
        super(in);
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
    }
}
