package com.pivotal.cf.mobile.analyticssdk.backend;

import android.net.Uri;

import java.util.Collections;
import java.util.List;

public class FakeBackEndSendEventsApiRequest implements BackEndSendEventsApiRequest {

    private final FakeBackEndSendEventsApiRequest originatingRequest;
    private boolean willBeSuccessfulRequest = false;
    private boolean wasRequestAttempted = false;
    private RequestHook requestHook = null;
    private List<Uri> receivedUris = null;

    public interface RequestHook {
        public void onRequestMade(FakeBackEndSendEventsApiRequest request, List<Uri> uris);
    }

    public FakeBackEndSendEventsApiRequest(FakeBackEndSendEventsApiRequest originatingRequest) {
        this.originatingRequest = originatingRequest;
    }

    public FakeBackEndSendEventsApiRequest() {
        this.originatingRequest = null;
        this.willBeSuccessfulRequest = false;
    }

    @Override
    public void startSendEvents(List<Uri> eventUris, BackEndSendEventsListener listener) {

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
    public BackEndSendEventsApiRequest copy() {
        final FakeBackEndSendEventsApiRequest newRequest = new FakeBackEndSendEventsApiRequest(this);
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
