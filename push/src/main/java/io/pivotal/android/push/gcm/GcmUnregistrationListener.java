/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.gcm;

public interface GcmUnregistrationListener {
    /**
     * Returns when unregistration is complete.  May return on a background thread.
     */
    void onGcmUnregistrationComplete();

    /**
     * Returns if registration has failed.
     *
     * @param reason Contains the reason that unregistration has failed. More information
     *               may be printed to the Android device log.
     */
    void onGcmUnregistrationFailed(String reason);
}
