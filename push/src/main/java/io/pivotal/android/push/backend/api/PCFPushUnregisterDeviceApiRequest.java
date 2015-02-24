/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.backend.api;

import io.pivotal.android.push.PushParameters;

public interface PCFPushUnregisterDeviceApiRequest {
    void startUnregisterDevice(String pcfPushDeviceRegistrationId, PushParameters parameters, PCFPushUnregisterDeviceListener listener);
    PCFPushUnregisterDeviceApiRequest copy();
}
