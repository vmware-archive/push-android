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

package com.pivotal.mss.pushsdk.gcm;

public interface GcmRegistrationListener {
    /**
     * Returns when registration is complete.  May return on a background thread.
     *
     * @param gcmDeviceRegistrationId The registration ID, as provided by Google Cloud Messaging.
     */
    void onGcmRegistrationComplete(String gcmDeviceRegistrationId);

    /**
     * Returns if registration has failed.
     *
     * @param reason Contains the reason that registration has failed. More information
     *               may be printed to the Android device log.
     */
    void onGcmRegistrationFailed(String reason);
}
