package org.omnia.pushsdk.util;

import java.util.Locale;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Debug;

/**
 * Debug utilities class
 */
public class DebugUtil {

	private static DebugUtil instance = null;

	public static DebugUtil getInstance(Context context) {
		if (instance == null) {
			instance = new DebugUtil(context);
		}
		return instance;
	}

	private boolean isDebuggable = false;

	private DebugUtil(Context context) {

		try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo applicationInfo;
			applicationInfo = pm.getApplicationInfo(context.getPackageName(), 0);
			isDebuggable = (applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
		} catch (NameNotFoundException e) {
			isDebuggable = false;
		} catch (NullPointerException e) {
			isDebuggable = false;
		}
	}

	/**
	 * Checks if the debuggable flag is set in the application's manifest file.
	 */
	public boolean isDebuggable() {
		return isDebuggable;
	}

	/**
	 * Dumps the application's current memory usage to the device log.  "i"nformation-level
	 * logging must be enabled for the log message to appear.
	 */
	public static void dumpMemoryInfo() {
		final Debug.MemoryInfo m = new Debug.MemoryInfo();
		Debug.getMemoryInfo(m);
		final String formattedMessage = String.format(Locale.getDefault(), "Memory info: dalvikPrivateDirty:%d dalvikPss:%d dalvikSharedDirty:%d nativePrivateDirty:%d nativePss:%d nativeSharedDirty:%d otherPrivateDirty:%d otherPss:%d otherSharedDirty:%d",
				m.dalvikPrivateDirty, m.dalvikPss, m.dalvikSharedDirty, m.nativePrivateDirty, m.nativePss, m.nativeSharedDirty, m.otherPrivateDirty, m.otherPss, m.otherSharedDirty);
		PushLibLogger.i(formattedMessage);
	}

}
