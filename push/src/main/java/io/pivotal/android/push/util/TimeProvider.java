package io.pivotal.android.push.util;

public class TimeProvider {

    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    public void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {}
    }
}
