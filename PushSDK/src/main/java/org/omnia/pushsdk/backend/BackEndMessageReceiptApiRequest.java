package org.omnia.pushsdk.backend;

import android.net.Uri;

import org.omnia.pushsdk.model.MessageReceiptEvent;

import java.util.List;

public interface BackEndMessageReceiptApiRequest {

    void startSendMessageReceipts(List<Uri> messageReceiptUris, BackEndMessageReceiptListener listener);
    BackEndMessageReceiptApiRequest copy();
}
