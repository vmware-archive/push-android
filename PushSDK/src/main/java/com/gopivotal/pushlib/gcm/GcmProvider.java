package com.gopivotal.pushlib.gcm;

import android.content.Context;

import java.io.IOException;

public interface GcmProvider {
    String register(String... senderIds) throws IOException;
    void unregister() throws IOException;
    boolean isGooglePlayServicesInstalled(Context context);
}
