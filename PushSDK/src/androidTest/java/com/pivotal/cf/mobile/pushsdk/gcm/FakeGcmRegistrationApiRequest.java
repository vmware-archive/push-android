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

package com.pivotal.cf.mobile.pushsdk.gcm;

public class FakeGcmRegistrationApiRequest implements GcmRegistrationApiRequest {

    private final FakeGcmProvider gcmProvider;

    public FakeGcmRegistrationApiRequest(FakeGcmProvider gcmProvider) {
        this.gcmProvider = gcmProvider;
    }

    @Override
    public void startRegistration(String senderId, GcmRegistrationListener listener) {
        try {
            final String registrationId = gcmProvider.register("Dummy Sender ID");
            listener.onGcmRegistrationComplete(registrationId);
        } catch (Exception e) {
            listener.onGcmRegistrationFailed(e.getLocalizedMessage());
        }
    }

    @Override
    public GcmRegistrationApiRequest copy() {
        return new FakeGcmRegistrationApiRequest(gcmProvider);
    }
}
