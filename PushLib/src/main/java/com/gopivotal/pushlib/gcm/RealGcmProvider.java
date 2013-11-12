package com.gopivotal.pushlib.gcm;

import android.content.Context;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

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
}
