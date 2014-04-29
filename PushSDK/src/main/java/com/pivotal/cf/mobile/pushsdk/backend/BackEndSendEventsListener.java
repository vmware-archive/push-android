package com.pivotal.cf.mobile.pushsdk.backend;

public interface BackEndSendEventsListener {
    void onBackEndSendEventsSuccess();
    void onBackEndSendEventsFailed(String reason);
}
