package org.omnia.pushsdk.jobs;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import org.omnia.pushsdk.backend.BackEndMessageReceiptApiRequest;
import org.omnia.pushsdk.backend.BackEndMessageReceiptListener;
import org.omnia.pushsdk.database.EventsStorage;
import org.omnia.pushsdk.model.BaseEvent;
import org.omnia.pushsdk.util.PushLibLogger;

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

        final List<Uri> uris = getUnpostedMessageReceipts(jobParams);
        PushLibLogger.fd("SendEventsJob: package %s: events available to send: %d", getPackageName(jobParams), uris.size());

        if (uris.size() > 0) {
            setStatusForEvents(jobParams, uris, BaseEvent.Status.POSTING);
            sendMessageReceipts(jobParams, uris);
        } else {
            sendJobResult(RESULT_NO_WORK_TO_DO, jobParams);
        }
    }

    private void setStatusForEvents(JobParams jobParams, List<Uri> uris, int status) {
        for (final Uri uri : uris) {
            jobParams.eventsStorage.setEventStatus(uri, status);
        }
    }

    // TODO - generalize to all event types
    private void sendMessageReceipts(final JobParams jobParams, final List<Uri> uris) {

        final BackEndMessageReceiptApiRequest apiRequest = jobParams.backEndMessageReceiptApiRequestProvider.getRequest();
        apiRequest.startSendMessageReceipts(uris, new BackEndMessageReceiptListener() {

            @Override
            public void onBackEndMessageReceiptSuccess() {
                if (uris != null) {
                    jobParams.eventsStorage.deleteEvents(uris, EventsStorage.EventType.MESSAGE_RECEIPT);
                }
                sendJobResult(JobResultListener.RESULT_SUCCESS, jobParams);
            }

            @Override
            public void onBackEndMessageReceiptFailed(String reason) {
                setStatusForEvents(jobParams, uris, BaseEvent.Status.POSTING_ERROR);
                sendJobResult(RESULT_FAILED_TO_SEND_RECEIPTS, jobParams);
            }
        });
    }

    private String getPackageName(JobParams jobParams) {
        final String packageName = jobParams.context.getPackageName();
        return packageName;
    }

    // TODO - generalize to all event types
    private List<Uri> getUnpostedMessageReceipts(JobParams jobParams) {
        final List<Uri> uris1 = jobParams.eventsStorage.getEventUrisWithStatus(EventsStorage.EventType.MESSAGE_RECEIPT, BaseEvent.Status.NOT_POSTED);
        final List<Uri> uris2 = jobParams.eventsStorage.getEventUrisWithStatus(EventsStorage.EventType.MESSAGE_RECEIPT, BaseEvent.Status.POSTING_ERROR);
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
