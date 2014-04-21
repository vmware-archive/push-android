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

package com.pivotal.cf.mobile.pushsdk;

/**
 * Parameters used to register with the Pivotal CF Mobile Services Push server.
 */
public class RegistrationParameters {

    private final String gcmSenderId;
    private final String variantUuid;
    private final String variantSecret;
    private final String deviceAlias;

    /**
     * Sets up parameters used by the Pivotal CF Push SDK
     *
     * @param gcmSenderId   The "sender ID" or "project ID", as defined by the Google Cloud Messaging.  May not be null or empty.
     *                      You can find it on the Google Cloud Console (https://cloud.google.com) for your project.
     * @param variantUuid   The "variant_uuid", as defined by Pivotal CF Push Services for your variant.  May not be null or empty.
     * @param variantSecret The "variant secret", as defined by Pivotal CF Push Services for your variant.  May not be null or empty.
     * @param deviceAlias   A developer-defined "device alias" which can be used to designate this device, or class.
     *                      of devices, in push or notification campaigns. May not be set to `null`. May be set to empty.
     *
     *                      If any of the above parameters ae `null` then the PushLib.startRegistration call will throw
     *                      an exception.
     */
    public RegistrationParameters(String gcmSenderId, String variantUuid, String variantSecret, String deviceAlias) {
        this.gcmSenderId = gcmSenderId;
        this.variantUuid = variantUuid;
        this.variantSecret = variantSecret;
        this.deviceAlias = deviceAlias;
    }

    public String getGcmSenderId() {
        return gcmSenderId;
    }

    public String getVariantUuid() {
        return variantUuid;
    }

    public String getVariantSecret() {
        return variantSecret;
    }

    public String getDeviceAlias() {
        return deviceAlias;
    }

    @Override
    public boolean equals(Object o) {

        if (o == null) {
            return false;
        }

        if (!(o instanceof RegistrationParameters)) {
            return false;
        }

        RegistrationParameters other = (RegistrationParameters)o;
        if (!other.gcmSenderId.equals(gcmSenderId)) {
            return false;
        }
        if (!other.variantUuid.equals(variantUuid)) {
            return false;
        }
        if (!other.variantSecret.equals(variantSecret)) {
            return false;
        }
        if (!other.deviceAlias.equals(deviceAlias)) {
            return false;
        }

        return true;
    }
}
