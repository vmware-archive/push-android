/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.registration;

/**
 * An interface for callbacks reporting when unregistration succeeds or fails.
 */
public interface UnregistrationListener {

    /**
     * Called when unregistration completes successfully.  Note: may be called
     * on a background thread.
     */
    void onUnregistrationComplete();

    /**
     * Called when unregistration fails.  Note: may be called on a background thread.
     *
     * @param reason  The reason that unregistration failed.
     */
    void onUnregistrationFailed(String reason);
}
