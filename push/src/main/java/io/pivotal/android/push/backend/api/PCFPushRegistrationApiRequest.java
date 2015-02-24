/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.backend.api;

import java.util.Set;

import io.pivotal.android.push.RegistrationParameters;

public interface PCFPushRegistrationApiRequest {

    void startNewDeviceRegistration(String gcmDeviceRegistrationId,
                                    Set<String> savedTags,
                                    RegistrationParameters parameters,
                                    PCFPushRegistrationListener listener);

    void startUpdateDeviceRegistration(String gcmDeviceRegistrationId,
                                       String pcfPushDeviceRegistrationId,
                                       Set<String> savedTags,
                                       RegistrationParameters parameters,
                                       PCFPushRegistrationListener listener);

    PCFPushRegistrationApiRequest copy();

}
