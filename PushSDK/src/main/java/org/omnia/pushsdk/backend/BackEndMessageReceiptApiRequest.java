package org.omnia.pushsdk.backend;

import android.net.Uri;

import java.util.List;

// TODO: generalize to other event types
public interface BackEndMessageReceiptApiRequest {

    void startSendMessageReceipts(List<Uri> messageReceiptUris, BackEndMessageReceiptListener listener);
    BackEndMessageReceiptApiRequest copy();
}
