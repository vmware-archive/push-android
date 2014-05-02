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

package com.pivotal.cf.mobile.pushsdk.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.pivotal.cf.mobile.common.util.DebugUtil;
import com.pivotal.cf.mobile.common.util.Logger;

import java.io.File;
import java.io.PrintWriter;

public class Util {

    /**
     * Reads the current application version code, as specified in the application's manifest file.
     *
     * @param context A context.
     * @return The current application version code, as specified in the application's manifest file.
     */
    public static int getAppVersion(Context context) {
        try {
            final PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Saves the ID string to a plain-text file on the SD card if the application is considered
     * debuggable and if it has WRITE_EXTERNAL_STORAGE permission.  Otherwise does nothing.
     *
     * Used for debugging, and by the sample application to send push message requests to GCM
     * and to the back-end server.
     *
     * @param context A context.
     * @param id      The ID string to save.
     * @param idType  The type of ID - used as the filename and in log messages.
     */
    public static void saveIdToFilesystem(Context context, String id, String idType) {
        if (DebugUtil.getInstance(context).isDebuggable()) {
            int res = context.checkCallingOrSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE");
            if (res == PackageManager.PERMISSION_GRANTED) {
                final PrintWriter pw;
                try {
                    final File externalFilesDir = context.getExternalFilesDir(null);
                    if (externalFilesDir == null) {
                        Logger.d("Was not able to get the externalFilesDir");
                        return;
                    }
                    final File dir = new File(externalFilesDir.getAbsolutePath() + File.separator + "pushlib");
                    if (!dir.exists()) {
                        dir.mkdir();
                    }
                    final File deviceUuidFile = new File(dir, idType + ".txt");
                    pw = new PrintWriter(deviceUuidFile);
                    pw.println(id);
                    pw.close();
                    Logger.d("Saved " + idType + " to file: " + deviceUuidFile.getAbsolutePath());
                } catch (Exception e) {
                    Logger.w("Was not able to save " + idType + " to filesystem. This error is not-fatal. " + e.getLocalizedMessage());
                }
            }
        }
    }
}
