package org.omnia.pushsdk.jobs;

public interface JobResultListener {

    public static int RESULT_SUCCESS = 0;
    public static int RESULT_FAIL = 1;

    public void onJobComplete(int resultCode);
}
