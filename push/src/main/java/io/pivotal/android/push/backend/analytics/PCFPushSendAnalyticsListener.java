package io.pivotal.android.push.backend.analytics;

public interface PCFPushSendAnalyticsListener {
    void onBackEndSendEventsSuccess();
    void onBackEndSendEventsFailed(String reason);
}
