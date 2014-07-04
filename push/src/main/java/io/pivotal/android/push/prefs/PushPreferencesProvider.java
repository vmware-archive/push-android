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

package io.pivotal.android.push.prefs;

public interface PushPreferencesProvider {

    public static int NO_SAVED_VERSION = -1;

    String getGcmDeviceRegistrationId();

    void setGcmDeviceRegistrationId(String gcmDeviceRegistrationId);

    String getBackEndDeviceRegistrationId();

    void setBackEndDeviceRegistrationId(String backendDeviceRegistrationId);

    int getAppVersion();

    void setAppVersion(int appVersion);

    String getGcmSenderId();

    void setGcmSenderId(String gcmSenderId);

    String getVariantUuid();

    void setVariantUuid(String variantUuid);

    String getVariantSecret();

    void setVariantSecret(String variantUuid);

    String getDeviceAlias();

    void setDeviceAlias(String deviceAlias);

    String getPackageName();

    void setPackageName(String packageName);

    String getBaseServerUrl();

    void setBaseServerUrl(String baseServerUrl);
}
