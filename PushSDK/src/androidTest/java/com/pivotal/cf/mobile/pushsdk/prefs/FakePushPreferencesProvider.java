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

package com.pivotal.cf.mobile.pushsdk.prefs;

import java.net.URL;

public class FakePushPreferencesProvider implements PushPreferencesProvider {

    private String gcmDeviceRegistrationId;
    private String backEndDeviceRegistrationId;
    private String gcmSenderId;
    private String variantUuid;
    private String variantSecret;
    private String deviceAlias;
    private String packageName;
    private URL baseServerUrl;
    private int appVersion;
    private boolean wasGcmDeviceRegistrationIdSaved = false;
    private boolean wasBackEndDeviceRegistrationIdSaved = false;
    private boolean wasAppVersionSaved = false;
    private boolean wasGcmSenderIdSaved = false;
    private boolean wasVariantUuidSaved = false;
    private boolean wasVariantSecretSaved = false;
    private boolean wasDeviceAliasSaved = false;
    private boolean wasPackageNameSaved = false;
    private boolean wasBaseServerUrlSaved = false;

    public FakePushPreferencesProvider(String gcmDeviceRegistrationIdToLoad,
                                       String backEndDeviceRegistrationIdToLoad,
                                       int appVersionToLoad,
                                       String gcmSenderIdToLoad,
                                       String variantUuidToLoad,
                                       String variantSecretToLoad,
                                       String deviceAliasToLoad,
                                       String packageNameToLoad,
                                       URL baseServerUrlToLoad) {
        this.gcmDeviceRegistrationId = gcmDeviceRegistrationIdToLoad;
        this.backEndDeviceRegistrationId = backEndDeviceRegistrationIdToLoad;
        this.appVersion = appVersionToLoad;
        this.gcmSenderId = gcmSenderIdToLoad;
        this.variantUuid = variantUuidToLoad;
        this.variantSecret = variantSecretToLoad;
        this.deviceAlias = deviceAliasToLoad;
        this.packageName = packageNameToLoad;
        this.baseServerUrl = baseServerUrlToLoad;
    }

    @Override
    public String getGcmDeviceRegistrationId() {
        return gcmDeviceRegistrationId;
    }

    @Override
    public String getBackEndDeviceRegistrationId() {
        return backEndDeviceRegistrationId;
    }

    @Override
    public int getAppVersion() {
        return appVersion;
    }

    @Override
    public String getGcmSenderId() {
        return gcmSenderId;
    }

    @Override
    public String getVariantUuid() {
        return variantUuid;
    }

    @Override
    public String getVariantSecret() {
        return variantSecret;
    }

    @Override
    public String getDeviceAlias() {
        return deviceAlias;
    }

    @Override
    public String getPackageName() {
        return packageName;
    }

    @Override
    public URL getBaseServerUrl() {
        return baseServerUrl;
    }

    @Override
    public void setBackEndDeviceRegistrationId(String backendDeviceRegistrationId) {
        this.backEndDeviceRegistrationId = backendDeviceRegistrationId;
        wasBackEndDeviceRegistrationIdSaved = true;
    }

    @Override
    public void setAppVersion(int appVersion) {
        this.appVersion = appVersion;
        wasAppVersionSaved = true;
    }

    @Override
    public void setGcmSenderId(String gcmSenderId) {
        this.gcmSenderId = gcmSenderId;
        wasGcmSenderIdSaved = true;
    }

    @Override
    public void setGcmDeviceRegistrationId(String gcmDeviceRegistrationId) {
        this.gcmDeviceRegistrationId = gcmDeviceRegistrationId;
        wasGcmDeviceRegistrationIdSaved = true;
    }

    @Override
    public void setVariantUuid(String variantUuid) {
        this.variantUuid = variantUuid;
        wasVariantUuidSaved = true;
    }

    @Override
    public void setVariantSecret(String variantUuid) {
        this.variantSecret = variantUuid;
        wasVariantSecretSaved = true;
    }

    @Override
    public void setDeviceAlias(String deviceAlias) {
        this.deviceAlias = deviceAlias;
        wasDeviceAliasSaved = true;
    }

    @Override
    public void setPackageName(String packageName) {
        this.packageName = packageName;
        wasPackageNameSaved = true;
    }

    @Override
    public void setBaseServerUrl(URL baseServerUrl) {
        this.baseServerUrl = baseServerUrl;
        wasBaseServerUrlSaved = true;
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

    public boolean wasVariantUuidSaved() {
        return wasVariantUuidSaved;
    }

    public boolean wasVariantSecretSaved() {
        return wasVariantSecretSaved;
    }

    public boolean wasDeviceAliasSaved() {
        return wasDeviceAliasSaved;
    }

    public boolean isWasPackageNameSaved() {
        return wasPackageNameSaved;
    }

    public boolean wasBaseServerUrlSaved() {
        return wasBaseServerUrlSaved;
    }
}
