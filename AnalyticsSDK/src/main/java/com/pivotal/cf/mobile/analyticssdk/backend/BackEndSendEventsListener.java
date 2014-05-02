package com.pivotal.cf.mobile.analyticssdk.backend;

public interface BackEndSendEventsListener {
    void onBackEndSendEventsSuccess();
    void onBackEndSendEventsFailed(String reason);
}
