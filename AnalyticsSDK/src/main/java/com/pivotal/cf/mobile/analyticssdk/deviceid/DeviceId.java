package com.pivotal.cf.mobile.analyticssdk.deviceid;

import android.content.Context;

import java.util.UUID;

public class DeviceId {

	private static final String DEVICE_ID_PREFERENCES = "com.pivotal.cf.mobile.analyticssdk.deviceid";

	private static final String DEVICE_ID = "deviceId";

	/* package */static final String EMULATOR_DEVICE_ID = "000000000000000";
	/* package */static final String PCFMS_DEVICE = "PCFMSD-";
	/* package */static final String PCFMS_EMULATOR = "PCFMSE-";

	private static String deviceId = null;

    /**
     * Returns an identifier for the device.
     *
     * If available, then this method will return the hardware device IMEI.
     * Return the IMEI requires android.permission.READ_PHONE_STATE permission.
     *
     * If not available, then this method will generate a device ID.  This device
     * ID will be saved and returned for future use.
     *
     * If a device ID is generated then it is prefixed with either `PCFMSD` or
     * `PCFMSE` depending if the device is physical or an emulator.
     *
     * @param context    Some context.
     * @param inspector  An object used to inspect the device.
     * @return           The assigned device ID.
     */
	public static String getDeviceId(Context context, DeviceInspector inspector) {

		if (deviceId == null) {
			deviceId = readDeviceId(context);

			if (deviceId != null) {
				return deviceId;
			}

			deviceId = inspector.getDeviceId(context);

			// The device id is not available
			if (deviceId == null) {
				if (inspector.isEmulator()) {
					deviceId = generateEmulatorDeviceId();
				} else {
					deviceId = generateFakeDeviceId();
				}
			} else if (EMULATOR_DEVICE_ID.equals(deviceId)) {
				deviceId = generateEmulatorDeviceId();
			}

			saveDeviceId(context);
		}

		return deviceId;
	}

	private static String generateFakeDeviceId() {
		return PCFMS_DEVICE + generateDeviceId();
	}

	private static String generateEmulatorDeviceId() {
		return PCFMS_EMULATOR + generateDeviceId();
	}

	private static String readDeviceId(Context context) {
		return context.getSharedPreferences(DEVICE_ID_PREFERENCES, Context.MODE_PRIVATE).getString(DEVICE_ID, null);
	}

	private static void saveDeviceId(Context context) {
		context.getSharedPreferences(DEVICE_ID_PREFERENCES, Context.MODE_PRIVATE).edit().putString(DEVICE_ID, deviceId).commit();
	}

	private static String generateDeviceId() {
		UUID deviceUuid = UUID.randomUUID();
		return deviceUuid.toString();
	}

	/* package */ static void reset(Context context) {
		clear();
		context.getSharedPreferences(DEVICE_ID_PREFERENCES, Context.MODE_PRIVATE).edit().clear().commit();
	}

	/* package */ static void clear() {
		deviceId = null;
	}

}
