/* Copyright (c) 2013 Pivotal Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.pivotal.android.push.gcm;

import android.content.Context;

import java.io.IOException;

public class FakeGcmProvider implements GcmProvider {

    private final String gcmDeviceRegistrationId;
    private final boolean willRegisterThrow;
    private boolean wasRegisterCalled = false;
    private boolean isGooglePlayServicesInstalled = true;
    private boolean wasUnregisterCalled = false;
    private boolean willUnregisterThrow = false;

    public FakeGcmProvider(String gcmDeviceRegistrationId) {
        this.gcmDeviceRegistrationId = gcmDeviceRegistrationId;
        this.willRegisterThrow = false;
    }

    public FakeGcmProvider(String gcmDeviceRegistrationId, boolean willRegisterThrow) {
        this.gcmDeviceRegistrationId = gcmDeviceRegistrationId;
        this.willRegisterThrow = willRegisterThrow;
    }

    public FakeGcmProvider(String gcmDeviceRegistrationId, boolean willRegisterThrow, boolean willUnregisterThrow) {
        this.gcmDeviceRegistrationId = gcmDeviceRegistrationId;
        this.willRegisterThrow = willRegisterThrow;
        this.willUnregisterThrow = willUnregisterThrow;
    }

    public void setIsGooglePlayServicesInstalled(boolean isGooglePlayServicesInstalled) {
        this.isGooglePlayServicesInstalled = isGooglePlayServicesInstalled;
    }

    @Override
    public String register(String... senderIds) throws IOException {
        this.wasRegisterCalled = true;
        if (willRegisterThrow) {
            throw new IOException("Fake GCM device registration failed fakely.");
        }
        return gcmDeviceRegistrationId;
    }

    @Override
    public void unregister() throws IOException {
        this.wasUnregisterCalled = true;
        if (willUnregisterThrow) {
            throw new IOException("Fake GCM device unregistration failed fakely.");
        }
    }

    @Override
    public boolean isGooglePlayServicesInstalled(Context context) {
        return isGooglePlayServicesInstalled;
    }

    public boolean wasRegisterCalled() {
        return wasRegisterCalled;
    }

    public boolean wasUnregisterCalled() {
        return wasUnregisterCalled;
    }
}
