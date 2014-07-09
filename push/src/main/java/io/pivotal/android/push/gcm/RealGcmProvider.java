/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.gcm;

import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import io.pivotal.android.push.util.Logger;

public class RealGcmProvider implements GcmProvider {

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
    public void unregister() throws IOException {
        gcm.unregister();
    }

    @Override
    public boolean isGooglePlayServicesInstalled(Context context) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            final String errorString = GooglePlayServicesUtil.getErrorString(resultCode);
            Logger.e("Google Play Services is not available: " + errorString);
            return false;
        }
        return true;
    }
}
