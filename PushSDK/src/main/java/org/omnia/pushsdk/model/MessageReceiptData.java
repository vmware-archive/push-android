package org.omnia.pushsdk.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class MessageReceiptData implements Parcelable {

    public static class Columns {
        public static final String MESSAGE_UUID = "msg_uuid";
    }

    // Optional field
    @SerializedName(Columns.MESSAGE_UUID)
    private String messageUuid;

    public MessageReceiptData() {
    }

    // Copy constructor
    public MessageReceiptData(MessageReceiptData source) {
        this.messageUuid = source.messageUuid;
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

    // Parcelable stuff

    public static final Parcelable.Creator<MessageReceiptData> CREATOR = new Parcelable.Creator<MessageReceiptData>() {

        public MessageReceiptData createFromParcel(Parcel in) {
            return new MessageReceiptData(in);
        }

        public MessageReceiptData[] newArray(int size) {
            return new MessageReceiptData[size];
        }
    };

    private MessageReceiptData(Parcel in) {
        messageUuid = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(messageUuid);
    }
}
