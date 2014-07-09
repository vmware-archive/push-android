/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.backend;

public interface BackEndUnregisterDeviceListener {
    void onBackEndUnregisterDeviceSuccess();
    void onBackEndUnregisterDeviceFailed(String reason);
}
