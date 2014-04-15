package org.omnia.pushsdk.jobs;

import android.os.Parcel;
import android.os.Parcelable;

public class DummyJob extends BaseJob {

    private int resultCode;
    private boolean willInterrupt;

    public DummyJob() {
        // Does nothing
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public boolean willInterrupt() {
        return willInterrupt;
    }

    public void setWillInterrupt(boolean willInterrupt) {
        this.willInterrupt = willInterrupt;
    }

    @Override
    public void run(JobParams jobParams) {
        if (willInterrupt) {
            Thread.currentThread().interrupt();
        } else {
            jobParams.listener.onJobComplete(resultCode);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof DummyJob)) {
            return false;
        }

        final DummyJob otherJob = (DummyJob)o;
        if (otherJob.resultCode != resultCode) {
            return false;
        }
        if (otherJob.willInterrupt != willInterrupt) {
            return false;
        }
        return true;
    }

    // Parcelable stuff

    public static final Parcelable.Creator<DummyJob> CREATOR = new Parcelable.Creator<DummyJob>() {

        public DummyJob createFromParcel(Parcel in) {
            return new DummyJob(in);
        }

        public DummyJob[] newArray(int size) {
            return new DummyJob[size];
        }
    };

    private DummyJob(Parcel in) {
        super(in);
        resultCode = in.readInt();
        willInterrupt = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeInt(resultCode);
        out.writeByte((byte) (willInterrupt ? 1 : 0));
    }
}
