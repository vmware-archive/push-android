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

public class FakePreferencesProvider implements PreferencesProvider {

    private String gcmDeviceRegistrationId;
    private String backEndDeviceRegistrationId;
    private String gcmSenderId;
    private String releaseUuid;
    private String releaseSecret;
    private String deviceAlias;
    private int appVersion;
    private boolean wasGcmDeviceRegistrationIdSaved = false;
    private boolean wasBackEndDeviceRegistrationIdSaved = false;
    private boolean wasAppVersionSaved = false;
    private boolean wasGcmSenderIdSaved = false;
    private boolean wasReleaseUuidSaved = false;
    private boolean wasReleaseSecretSaved = false;
    private boolean wasDeviceAliasSaved = false;

    public FakePreferencesProvider(String gcmDeviceRegistrationIdToLoad, String backEndDeviceRegistrationIdToLoad, int appVersionToLoad, String gcmSenderIdToLoad, String releaseUuidToLoad, String releaseSecretToLoad, String deviceAliasToLoad) {
        this.gcmDeviceRegistrationId = gcmDeviceRegistrationIdToLoad;
        this.backEndDeviceRegistrationId = backEndDeviceRegistrationIdToLoad;
        this.appVersion = appVersionToLoad;
        this.gcmSenderId = gcmSenderIdToLoad;
        this.releaseUuid = releaseUuidToLoad;
        this.releaseSecret = releaseSecretToLoad;
        this.deviceAlias = deviceAliasToLoad;
    }

    @Override
    public String loadGcmDeviceRegistrationId() {
        return gcmDeviceRegistrationId;
    }

    @Override
    public String loadBackEndDeviceRegistrationId() {
        return backEndDeviceRegistrationId;
    }

    @Override
    public int loadAppVersion() {
        return appVersion;
    }

    @Override
    public String loadGcmSenderId() {
        return gcmSenderId;
    }

    @Override
    public String loadReleaseUuid() {
        return releaseUuid;
    }

    @Override
    public String loadReleaseSecret() {
        return releaseSecret;
    }

    @Override
    public String loadDeviceAlias() {
        return deviceAlias;
    }

    @Override
    public void saveBackEndDeviceRegistrationId(String backendDeviceRegistrationId) {
        this.backEndDeviceRegistrationId = backendDeviceRegistrationId;
        wasBackEndDeviceRegistrationIdSaved = true;
    }

    @Override
    public void saveAppVersion(int appVersion) {
        this.appVersion = appVersion;
        wasAppVersionSaved = true;
    }

    @Override
    public void saveGcmSenderId(String gcmSenderId) {
        this.gcmSenderId = gcmSenderId;
        wasGcmSenderIdSaved = true;
    }

    @Override
    public void saveGcmDeviceRegistrationId(String gcmDeviceRegistrationId) {
        this.gcmDeviceRegistrationId = gcmDeviceRegistrationId;
        wasGcmDeviceRegistrationIdSaved = true;
    }

    @Override
    public void saveReleaseUuid(String releaseUuid) {
        this.releaseUuid = releaseUuid;
        wasReleaseUuidSaved = true;
    }

    @Override
    public void saveReleaseSecret(String releaseSecret) {
        this.releaseSecret = releaseSecret;
        wasReleaseSecretSaved = true;
    }

    @Override
    public void saveDeviceAlias(String deviceAlias) {
        this.deviceAlias = deviceAlias;
        wasDeviceAliasSaved = true;
    }

    public boolean wasGcmDeviceRegistrationIdSaved() {
        return wasGcmDeviceRegistrationIdSaved;
    }

    public boolean wasBackEndDeviceRegistrationIdSaved() {
        return wasBackEndDeviceRegistrationIdSaved;
    }

    public boolean wasAppVersionSaved() {
        return wasAppVersionSaved;
    }

    public boolean wasGcmSenderIdSaved() {
        return wasGcmSenderIdSaved;
    }

    public boolean wasReleaseUuidSaved() {
        return wasReleaseUuidSaved;
    }

    public boolean wasReleaseSecretSaved() {
        return wasReleaseSecretSaved;
    }

    public boolean wasDeviceAliasSaved() {
        return wasDeviceAliasSaved;
    }
}
