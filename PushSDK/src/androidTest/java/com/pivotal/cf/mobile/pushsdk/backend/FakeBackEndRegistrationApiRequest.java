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

public class FakeBackEndRegistrationApiRequest implements BackEndRegistrationApiRequest {

    private final FakeBackEndRegistrationApiRequest originatingRequest;
    private final String backEndDeviceRegistrationId;
    private final boolean willBeSuccessfulRequest;
    private boolean wasRegisterCalled = false;

    public FakeBackEndRegistrationApiRequest(String backEndDeviceRegistrationId) {
        this.originatingRequest = null;
        this.backEndDeviceRegistrationId = backEndDeviceRegistrationId;
        this.willBeSuccessfulRequest = true;
    }

    public FakeBackEndRegistrationApiRequest(String backEndDeviceRegistrationId, boolean willBeSuccessfulRequest) {
        this.originatingRequest = null;
        this.backEndDeviceRegistrationId = backEndDeviceRegistrationId;
        this.willBeSuccessfulRequest = willBeSuccessfulRequest;
    }

    public FakeBackEndRegistrationApiRequest(FakeBackEndRegistrationApiRequest originatingRequest, String backEndDeviceRegistrationId, boolean willBeSuccessfulRequest) {
        this.originatingRequest = originatingRequest;
        this.backEndDeviceRegistrationId = backEndDeviceRegistrationId;
        this.willBeSuccessfulRequest = willBeSuccessfulRequest;
    }

    @Override
    public void startDeviceRegistration(String gcmDeviceRegistrationId, RegistrationParameters parameters, BackEndRegistrationListener listener) {
        wasRegisterCalled = true;
        if (originatingRequest != null) {
            originatingRequest.wasRegisterCalled = true;
        }

        if (willBeSuccessfulRequest) {
            listener.onBackEndRegistrationSuccess(backEndDeviceRegistrationId);
        } else {
            listener.onBackEndRegistrationFailed("Fake back-end registration failed fakely");
        }
    }

    @Override
    public BackEndRegistrationApiRequest copy() {
        return new FakeBackEndRegistrationApiRequest(this, backEndDeviceRegistrationId, willBeSuccessfulRequest);
    }

    public boolean wasRegisterCalled() {
        return wasRegisterCalled;
    }
}
