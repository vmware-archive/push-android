package io.pivotal.android.push.backend.analytics;

import android.net.Uri;

import java.util.Collections;
import java.util.List;

import io.pivotal.android.push.PushParameters;

public class FakePCFPushSendAnalyticsApiRequest implements PCFPushSendAnalyticsApiRequest {

    private final FakePCFPushSendAnalyticsApiRequest originatingRequest;
    private boolean willBeSuccessfulRequest = false;
    private boolean wasRequestAttempted = false;
    private RequestHook requestHook = null;
    private List<Uri> receivedUris = null;

    public interface RequestHook {
        public void onRequestMade(FakePCFPushSendAnalyticsApiRequest request, List<Uri> uris);
    }

    public FakePCFPushSendAnalyticsApiRequest(FakePCFPushSendAnalyticsApiRequest originatingRequest) {
        this.originatingRequest = originatingRequest;
    }

    public FakePCFPushSendAnalyticsApiRequest() {
        this.originatingRequest = null;
        this.willBeSuccessfulRequest = false;
    }

    @Override
    public void startSendEvents(List<Uri> eventUris, PushParameters parameters, PCFPushSendAnalyticsListener listener) {

        wasRequestAttempted = true;
        if (originatingRequest != null) {
            originatingRequest.wasRequestAttempted = true;
        }

        if (requestHook != null) {
            requestHook.onRequestMade(this, eventUris);
        }

        if (willBeSuccessfulRequest) {
            receivedUris = eventUris;
            if (originatingRequest != null) {
                originatingRequest.receivedUris = eventUris;
            }
            listener.onBackEndSendEventsSuccess();
        } else {
            listener.onBackEndSendEventsFailed("The fake request failed fakely.");
        }
    }

    @Override
    public PCFPushSendAnalyticsApiRequest copy() {
        final FakePCFPushSendAnalyticsApiRequest newRequest = new FakePCFPushSendAnalyticsApiRequest(this);
        newRequest.willBeSuccessfulRequest = willBeSuccessfulRequest;
        return newRequest;
    }

    public boolean wasRequestAttempted() {
        return wasRequestAttempted;
    }

    public void setWillBeSuccessfulRequest(boolean b) {
        this.willBeSuccessfulRequest = b;
    }

    public void setRequestHook(RequestHook requestHook) {
        this.requestHook = requestHook;
    }

    public int numberOfEventsSent() {
        if (receivedUris == null) {
            return 0;
        } else {
            return receivedUris.size();
        }
    };

    public List<Uri> getListOfReceivedUris() {
        return Collections.unmodifiableList(receivedUris);
    }
}
