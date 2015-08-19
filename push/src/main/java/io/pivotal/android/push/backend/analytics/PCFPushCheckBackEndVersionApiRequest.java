package io.pivotal.android.push.backend.analytics;

public interface PCFPushCheckBackEndVersionApiRequest {
    void startCheckBackEndVersion(PCFPushCheckBackEndVersionListener listener);
    PCFPushCheckBackEndVersionApiRequest copy();
}
