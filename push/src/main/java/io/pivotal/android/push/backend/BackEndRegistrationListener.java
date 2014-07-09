/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.backend;

public interface BackEndRegistrationListener {
    void onBackEndRegistrationSuccess(String backEndDeviceRegistrationId);
    void onBackEndRegistrationFailed(String reason);
}
