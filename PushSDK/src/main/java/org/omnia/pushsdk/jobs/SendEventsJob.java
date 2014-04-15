package org.omnia.pushsdk.jobs;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import org.omnia.pushsdk.backend.BackEndMessageReceiptApiRequest;
import org.omnia.pushsdk.backend.BackEndMessageReceiptListener;
import org.omnia.pushsdk.database.EventsStorage;
import org.omnia.pushsdk.model.EventBase;
import org.omnia.pushsdk.service.EventService;
import org.omnia.pushsdk.util.PushLibLogger;

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
            sendMessageReceipts(jobParams, uris);
        } else {
            sendJobResult(RESULT_NO_WORK_TO_DO, jobParams);
        }
    }

    // TODO - generalize to all event types
    private void sendMessageReceipts(final JobParams jobParams, final List<Uri> uris) {

        final BackEndMessageReceiptApiRequest apiRequest = jobParams.backEndMessageReceiptApiRequestProvider.getRequest();
        apiRequest.startSendMessageReceipts(uris, new BackEndMessageReceiptListener() {

            @Override
            public void onBackEndMessageReceiptSuccess() {
                postProcessAfterRequest(uris, jobParams);
            }

            @Override
            public void onBackEndMessageReceiptFailed(String reason) {
                // TODO - log reason? send somewhere?
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
        final List<Uri> uris = jobParams.eventsStorage.getEventUrisWithStatus(EventsStorage.EventType.MESSAGE_RECEIPT, EventBase.Status.NOT_POSTED);
        return uris;
    }

    private void postProcessAfterRequest(List<Uri> messageReceiptUris, JobParams jobParams) {
        if (messageReceiptUris != null) {
            jobParams.eventsStorage.deleteEvents(messageReceiptUris, EventsStorage.EventType.MESSAGE_RECEIPT);
        }
        sendJobResult(JobResultListener.RESULT_SUCCESS, jobParams);
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
