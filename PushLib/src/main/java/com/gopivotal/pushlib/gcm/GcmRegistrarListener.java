package com.gopivotal.pushlib.gcm;

public interface GcmRegistrarListener {
    /**
     * Returns when registation is complete.  May return on a background thread.
     *
     * @param registrationId The registration ID, as provided by Google Cloud Messaging.
     */
    void onRegistrationComplete(String registrationId);

    /**
     * Returns if registration has failed.
     *
     * @param reason Contains the reason that registration has failed. More information
     *               may be printed to the Android device log.
     */
    void onRegistrationFailed(String reason);
}
