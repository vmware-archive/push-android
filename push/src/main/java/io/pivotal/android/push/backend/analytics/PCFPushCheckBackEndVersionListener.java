package io.pivotal.android.push.backend.analytics;

import io.pivotal.android.push.version.Version;

public interface PCFPushCheckBackEndVersionListener {
    void onCheckBackEndVersionSuccess(Version version);
    void onCheckBackEndVersionIsOldVersion();
    void onCheckBackEndVersionRetryableFailure(String reason);
    void onCheckBackEndVersionFatalFailure(String reason);
}
