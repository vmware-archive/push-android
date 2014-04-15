package org.omnia.pushsdk.jobs;

import android.os.Parcel;
import android.os.Parcelable;

public abstract class BaseJob implements Job, Parcelable {

    public BaseJob() {
    }

    public void sendJobResult(int resultCode, JobParams jobParams) {
        jobParams.listener.onJobComplete(resultCode);
    }

    // Parcelable stuff

    protected BaseJob(Parcel in) {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
    }

    @Override
    public String toString() {
        return super.getClass().getSimpleName();
    }
}
