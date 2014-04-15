package org.omnia.pushsdk.jobs;

public interface JobResultListener {

    public static int RESULT_SUCCESS = 0;

    // Other success and failure codes will be declared in their respective job classes.

    public void onJobComplete(int resultCode);
}
