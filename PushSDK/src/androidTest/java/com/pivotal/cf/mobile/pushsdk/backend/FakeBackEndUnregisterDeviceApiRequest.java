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

package com.pivotal.cf.mobile.pushsdk.backend;

import com.pivotal.cf.mobile.pushsdk.RegistrationParameters;

public class FakeBackEndUnregisterDeviceApiRequest implements BackEndUnregisterDeviceApiRequest {

    private final FakeBackEndUnregisterDeviceApiRequest originatingRequest;
    private final boolean willBeSuccessfulRequest;
    private boolean wasUnregisterCalled = false;

    public FakeBackEndUnregisterDeviceApiRequest() {
        this.originatingRequest = null;
        this.willBeSuccessfulRequest = true;
    }

    public FakeBackEndUnregisterDeviceApiRequest(boolean willBeSuccessfulRequest) {
        this.originatingRequest = null;
        this.willBeSuccessfulRequest = willBeSuccessfulRequest;
    }

    public FakeBackEndUnregisterDeviceApiRequest(FakeBackEndUnregisterDeviceApiRequest originatingRequest, boolean willBeSuccessfulRequest) {
        this.originatingRequest = originatingRequest;
        this.willBeSuccessfulRequest = willBeSuccessfulRequest;
    }

    @Override
    public void startUnregisterDevice(String backEndDeviceRegistrationId, RegistrationParameters parameters, BackEndUnregisterDeviceListener listener) {
        wasUnregisterCalled = true;
        if (originatingRequest != null) {
            originatingRequest.wasUnregisterCalled = true;
        }

        if (willBeSuccessfulRequest) {
            listener.onBackEndUnregisterDeviceSuccess();
        } else {
            listener.onBackEndUnregisterDeviceFailed("Fake back-end registration failed fakely");
        }
    }

    @Override
    public BackEndUnregisterDeviceApiRequest copy() {
        return new FakeBackEndUnregisterDeviceApiRequest(this, willBeSuccessfulRequest);
    }

    public boolean wasUnregisterCalled() {
        return wasUnregisterCalled;
    }
}
