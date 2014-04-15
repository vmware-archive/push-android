package org.omnia.pushsdk.backend;

// TODO: generalize to other event types
public interface BackEndMessageReceiptListener {
    void onBackEndMessageReceiptSuccess();
    void onBackEndMessageReceiptFailed(String reason);
}
