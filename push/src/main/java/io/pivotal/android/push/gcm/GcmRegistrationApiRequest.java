/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.gcm;

public interface GcmRegistrationApiRequest {
    void startRegistration(String senderId, GcmRegistrationListener listener);
    GcmRegistrationApiRequest copy();
}
