/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.backend;

import io.pivotal.android.push.RegistrationParameters;

public interface BackEndUnregisterDeviceApiRequest {
    void startUnregisterDevice(String backEndDeviceRegistrationId, RegistrationParameters parameters, BackEndUnregisterDeviceListener listener);
    BackEndUnregisterDeviceApiRequest copy();
}
