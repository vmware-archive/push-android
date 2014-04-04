package org.omnia.pushsdk.model;

import com.google.gson.annotations.SerializedName;

public class MessageReceiptData {

    /* "data":{
     *    "msg_uuid" : "message-uuid-from-server", // optional field
     * }
     */

    // Optional field
    @SerializedName("msg_uuid")
    private String messageUuid;

    public MessageReceiptData() {
    }

    public String getMessageUuid() {
        return messageUuid;
    }

    public void setMessageUuid(String messageUuid) {
        this.messageUuid = messageUuid;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (!(o instanceof MessageReceiptData)) {
            return false;
        }

        MessageReceiptData other = (MessageReceiptData) o;

        if (other.messageUuid == null && messageUuid != null) {
            return false;
        }
        if (other.messageUuid != null && messageUuid == null) {
            return false;
        }
        if (other.messageUuid != null && messageUuid != null && !(other.messageUuid.equals(messageUuid))) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = (result * 31) + (messageUuid == null ? 0 : messageUuid.hashCode());
        return result;
    }
}
