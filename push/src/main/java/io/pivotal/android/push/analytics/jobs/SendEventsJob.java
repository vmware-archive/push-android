package io.pivotal.android.push.analytics.jobs;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import io.pivotal.android.push.PushParameters;
import io.pivotal.android.push.backend.analytics.PCFPushSendAnalyticsApiRequest;
import io.pivotal.android.push.backend.analytics.PCFPushSendAnalyticsListener;
import io.pivotal.android.push.model.analytics.AnalyticsEvent;
import io.pivotal.android.push.util.Logger;

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
            setStatusForEvents(jobParams, uris, AnalyticsEvent.Status.POSTING);
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

        final PCFPushSendAnalyticsApiRequest request = jobParams.requestProvider.getRequest();
        final PushParameters pushParameters = getParameters();
        request.startSendEvents(uris, new PCFPushSendAnalyticsListener() {

            public void onBackEndSendEventsSuccess() {
                if (uris != null) {
                    jobParams.eventsStorage.deleteEvents(uris);
                }
                sendJobResult(JobResultListener.RESULT_SUCCESS, jobParams);
            }

            @Override
            public void onBackEndSendEventsFailed(String reason) {
                setStatusForEvents(jobParams, uris, AnalyticsEvent.Status.POSTING_ERROR);
                sendJobResult(RESULT_FAILED_TO_SEND_RECEIPTS, jobParams);
            }
        });
    }

    private PushParameters getParameters() {
        // TODO - provide a push parameters object
        return null;
    }

    private String getPackageName(JobParams jobParams) {
        final String packageName = jobParams.context.getPackageName();
        return packageName;
    }

    private List<Uri> getUnpostedEvents(JobParams jobParams) {
        final List<Uri> uris1 = jobParams.eventsStorage.getEventUrisWithStatus(AnalyticsEvent.Status.NOT_POSTED);
        final List<Uri> uris2 = jobParams.eventsStorage.getEventUrisWithStatus(AnalyticsEvent.Status.POSTING_ERROR);
        final List<Uri> uris = new ArrayList<>(uris1);
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
