/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.backend;

import io.pivotal.android.push.RegistrationParameters;

public interface BackEndRegistrationApiRequest {

    void startNewDeviceRegistration(String gcmDeviceRegistrationId, RegistrationParameters parameters, BackEndRegistrationListener listener);
    void startUpdateDeviceRegistration(String gcmDeviceRegistrationId, String backEndDeviceRegistrationId, RegistrationParameters parameters, BackEndRegistrationListener listener);
    BackEndRegistrationApiRequest copy();

}
