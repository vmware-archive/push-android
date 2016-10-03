package io.pivotal.android.push.fcm;

import com.google.firebase.iid.FirebaseInstanceIdService;

import io.pivotal.android.push.Push;


public class FcmTokenIDService extends FirebaseInstanceIdService {

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onTokenRefresh() {
        Push.getInstance(this).onFcmTokenUpdated();
    }
}