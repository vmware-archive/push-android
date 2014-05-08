package com.pivotal.cf.mobile.analyticssdk.deviceid;

import android.content.Context;

public interface DeviceInspector {

	boolean isEmulator();
	String getDeviceId(Context context);
}
