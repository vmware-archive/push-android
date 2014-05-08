package com.pivotal.cf.mobile.analyticssdk.deviceid;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.pivotal.cf.mobile.common.util.Logger;

public class DeviceInspectorImpl implements DeviceInspector {

	@Override
	public boolean isEmulator() {
		return Build.FINGERPRINT.contains("generic");
	}

	@Override
	public String getDeviceId(Context context) {

		try {
			// Requires android.permission.READ_PHONE_STATE
			final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			return tm.getDeviceId();
		} catch (Exception e) {
			Logger.w("Warning: could not read device ID");
		}

		return null;
	}
}
