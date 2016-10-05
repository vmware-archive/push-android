/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.gcm;

import android.content.Context;

import java.io.IOException;

public interface GcmProvider {
    String register(String... senderIds) throws IOException;
    void unregister() throws IOException;
    boolean isGooglePlayServicesInstalled(Context context);
}
