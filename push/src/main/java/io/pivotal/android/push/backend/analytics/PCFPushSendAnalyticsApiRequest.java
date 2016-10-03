package io.pivotal.android.push.backend.analytics;

import android.net.Uri;

import java.util.List;

public interface PCFPushSendAnalyticsApiRequest {

    void startSendEvents(List<Uri> eventUris, PCFPushSendAnalyticsListener listener);
    PCFPushSendAnalyticsApiRequest copy();
}
