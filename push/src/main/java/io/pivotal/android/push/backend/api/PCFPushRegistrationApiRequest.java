/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.backend.api;

import java.util.Set;

import io.pivotal.android.push.PushParameters;

public interface PCFPushRegistrationApiRequest {

    void startNewDeviceRegistration(String fcmDeviceRegistrationId,
                                    Set<String> savedTags,
                                    PushParameters parameters,
                                    PCFPushRegistrationListener listener);

    void startUpdateDeviceRegistration(String fcmDeviceRegistrationId,
                                       String pcfPushDeviceRegistrationId,
                                       Set<String> savedTags,
                                       PushParameters parameters,
                                       PCFPushRegistrationListener listener);

    PCFPushRegistrationApiRequest copy();

}
