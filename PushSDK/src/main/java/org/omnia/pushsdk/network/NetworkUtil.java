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

package org.omnia.pushsdk.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/**
 * General purpose network helper methods.
 *
 */
public class NetworkUtil {

    private static NetworkUtil instance;
    private Context applicationContext;

    public static NetworkUtil getInstance(Context applicationContext) {
        if (instance == null) {
            instance = new NetworkUtil(applicationContext);
        }
        return instance;
    }

    private NetworkUtil(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Checks if the device thinks it is connected to an active network.
     */
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return (activeNetworkInfo != null) && activeNetworkInfo.isConnected();
    }

    /**
     * Check to see if the device is connected to wifi. Requires ACCESS_WIFI_STATE permission in manifest.
     */
    public boolean isWifiAvailable() {
        final WifiManager wifi = (WifiManager) applicationContext.getSystemService(Context.WIFI_SERVICE);
        final WifiInfo w = wifi.getConnectionInfo();
        if (w == null)
            return false;
        return (w.getIpAddress() != 0);
    }
}
