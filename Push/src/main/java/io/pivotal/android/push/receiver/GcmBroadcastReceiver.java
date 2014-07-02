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

package io.pivotal.android.push.receiver;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.support.v4.content.WakefulBroadcastReceiver;

import io.pivotal.android.common.util.Logger;
import io.pivotal.android.push.service.GcmService;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ComponentName comp = new ComponentName(context.getPackageName(), getService(context).getName());
        // Start the service, keeping the device awake while it is launching.
        WakefulBroadcastReceiver.startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }

    private Class<?> getService(final Context context) {
        try {
            final Class<?> klass = findServiceClassName(context);
            if (klass != null) return klass;
        } catch (Exception e) {
            Logger.ex(e);
        }

        return GcmService.class;
    }

    private Class<?> findServiceClassName(final Context context) throws PackageManager.NameNotFoundException, ClassNotFoundException {
        final PackageManager manager = context.getPackageManager();
        final PackageInfo info = manager.getPackageInfo(context.getPackageName(), PackageManager.GET_SERVICES);
        final ServiceInfo[] services = info.services;

        if (services != null) {
            for (int i =0; i < services.length; i++) {
                final Class<?> klass = Class.forName(services[i].name);
                if (GcmService.class.isAssignableFrom(klass)) {
                    return klass;
                }
            }
        }
        return null;
    }
}
