/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
     * Returns a set of tags.  Each tag is converted to lowercase.
     *
     * @param tags A set of string tags.
     *
     * @return All of the given tags in lowercase.
     */
    public static Set<String> lowercaseTags(Set<String> tags) {
        if (tags == null) {
            return null;
        }

        final Set<String> lowercaseTags = new HashSet<>(tags.size());
        for (String tag : tags) {
            lowercaseTags.add(tag.toLowerCase());
        }
        return lowercaseTags;
    }

    /**
     * Saves the ID string to a plain-text file on the SD card if the application is considered
     * debuggable and if it has WRITE_EXTERNAL_STORAGE permission.  Otherwise does nothing.
     *
     * Used for debugging, and by the sample application to send push message requests to GCM
     * and to the PCF Push server.
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

    public static void saveJsonMapToFilesystem(Context context, List<Map<String, String>> map) {
        if (DebugUtil.getInstance(context).isDebuggable()) {
            int res = context.checkCallingOrSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE");
            if (res == PackageManager.PERMISSION_GRANTED) {
                FileWriter writer = null;
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
                    final File file = new File(dir, "geofences.json");
                    writer = new FileWriter(file);
                    new Gson().toJson(map, writer);
                    Logger.d("Saved geofences to file: " + file.getAbsolutePath());
                } catch (Exception e) {
                    Logger.w("Was not able to save geofences to filesystem. This error is not-fatal. " + e.getLocalizedMessage());
                } finally {
                    if (writer != null) {
                        try {
                            writer.close();
                        } catch (IOException e) {}
                    }
                }
            }

        }
    }
}
