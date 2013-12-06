package org.omnia.pushsdk.registration;

/**
 * An interface for callbacks reporting when registration succeeds or fails.
 */
public interface RegistrationListener {

    /**
     * Called when registration completes successfully.  Note: may be called
     * on a background thread.
     */
    void onRegistrationComplete();

    /**
     * Called when registration fails.  Note: may be called on a background thread.
     *
     * @param reason  The reason that registration failed.
     */
    void onRegistrationFailed(String reason);
}
