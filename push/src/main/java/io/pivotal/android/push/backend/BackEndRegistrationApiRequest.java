/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.backend;

import java.util.Set;

import io.pivotal.android.push.RegistrationParameters;

public interface BackEndRegistrationApiRequest {

    void startNewDeviceRegistration(String gcmDeviceRegistrationId,
                                    Set<String> savedTags,
                                    RegistrationParameters parameters,
                                    BackEndRegistrationListener listener);

    void startUpdateDeviceRegistration(String gcmDeviceRegistrationId,
                                       String backEndDeviceRegistrationId,
                                       Set<String> savedTags,
                                       RegistrationParameters parameters,
                                       BackEndRegistrationListener listener);

    BackEndRegistrationApiRequest copy();

}
