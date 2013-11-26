package com.gopivotal.pushlib.gcm;

import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.gopivotal.pushlib.util.PushLibLogger;

import java.io.IOException;

public class RealGcmProvider implements GcmProvider {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private GoogleCloudMessaging gcm;

    public RealGcmProvider(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
        gcm = GoogleCloudMessaging.getInstance(context);
    }

    @Override
    public String register(String... senderIds) throws IOException {
        return gcm.register(senderIds);
    }

    @Override
    public boolean isGooglePlayServicesInstalled(Context context) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            final String errorString = GooglePlayServicesUtil.getErrorString(resultCode);
            PushLibLogger.e("Google Play Services is not available: " + errorString);
            return false;
        }
        return true;
    }
}
