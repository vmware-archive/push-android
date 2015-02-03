/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.registration;

/**
 * An interface for callbacks reporting when subscribing to tags succeeds or fails
 */
public interface SubscribeToTagsListener {

    /**
     * Called when subscribing completes successfully.  Note: may be called
     * on a background thread.
     */
    void onSubscribeToTagsComplete();

    /**
     * Called when subscribing fails.  Note: may be called on a background thread.
     *
     * @param reason  The reason that registration failed.
     */
    void onSubscribeToTagsFailed(String reason);
}
