package com.gopivotal.pushlib.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.xtreme.commons.DebugUtil;

import java.io.File;
import java.io.PrintWriter;

public class Util {

    public static int getAppVersion(Context context) {
        try {
            final PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    public static void saveIdToFilesystem(Context context, String id, String idType) {
        // Saves the device_uuid or GCM registration ID to the file system.  Useful for debugging
        // only since the registration ID is not loaded anywhere.
        if (DebugUtil.getInstance(context).isDebuggable()) {
            int res = context.checkCallingOrSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE");
            if (res == PackageManager.PERMISSION_GRANTED) {
                final PrintWriter pw;
                try {
                    final File externalFilesDir = context.getExternalFilesDir(null);
                    if (externalFilesDir == null) {
                        PushLibLogger.d("Was not able to get the externalFilesDir");
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
                    PushLibLogger.d("Saved " + idType + " to file: " + deviceUuidFile.getAbsolutePath());
                } catch (Exception e) {
                    PushLibLogger.w("Was not able to save " + idType + " to filesystem. This error is not-fatal. " + e.getLocalizedMessage());
                }
            }
        }
    }
}
