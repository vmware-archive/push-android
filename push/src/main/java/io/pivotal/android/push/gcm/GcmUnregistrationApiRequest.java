/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.gcm;

public interface GcmUnregistrationApiRequest {
    void startUnregistration(GcmUnregistrationListener listener);
    GcmUnregistrationApiRequest copy();
}
