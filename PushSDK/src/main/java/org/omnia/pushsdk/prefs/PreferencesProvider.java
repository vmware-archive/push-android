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

package org.omnia.pushsdk.prefs;

public interface PreferencesProvider {

    String loadGcmDeviceRegistrationId();

    void saveGcmDeviceRegistrationId(String gcmDeviceRegistrationId);

    String loadBackEndDeviceRegistrationId();

    void saveBackEndDeviceRegistrationId(String backendDeviceRegistrationId);

    int loadAppVersion();

    void saveAppVersion(int appVersion);

    String loadGcmSenderId();

    void saveGcmSenderId(String gcmSenderId);

    String loadReleaseUuid();

    void saveReleaseUuid(String releaseUuid);

    String loadReleaseSecret();

    void saveReleaseSecret(String releaseUuid);

    String loadDeviceAlias();

    void saveDeviceAlias(String deviceAlias);
}