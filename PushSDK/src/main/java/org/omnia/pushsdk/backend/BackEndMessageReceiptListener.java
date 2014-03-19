package org.omnia.pushsdk.backend;

public interface BackEndMessageReceiptListener {
    void onBackEndMessageReceiptSuccess();
    void onBackEndMessageReceiptFailed(String reason);
}
