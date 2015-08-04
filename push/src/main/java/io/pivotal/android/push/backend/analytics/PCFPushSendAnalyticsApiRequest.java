package io.pivotal.android.push.backend.analytics;

import android.net.Uri;

import java.util.List;

import io.pivotal.android.push.PushParameters;

public interface PCFPushSendAnalyticsApiRequest {

    void startSendEvents(List<Uri> eventUris, PCFPushSendAnalyticsListener listener);
    PCFPushSendAnalyticsApiRequest copy();
}
