package com.pivotal.cf.mobile.pushsdk.backend;

import android.net.Uri;

import java.util.List;

public interface BackEndSendEventsApiRequest {

    void startSendEvents(List<Uri> eventUris, BackEndSendEventsListener listener);
    BackEndSendEventsApiRequest copy();
}
