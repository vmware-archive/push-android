package com.pivotal.cf.mobile.analyticssdk.deviceid;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.MoreAsserts;

public class DeviceIdTest extends AndroidTestCase {

	private static final String HARDWARE_DEVICE_ID1 = "HARDWARE DEVICE ID1";
	private static final String HARDWARE_DEVICE_ID2 = "HARDWARE DEVICE ID2";
	private static final String SMELLY_DUMMY = "smelly";

	private static final String EMULATOR_DEVICE_ID = "000000000000000";

    private static final boolean IS_EMULATOR = true;
    private static final boolean IS_DEVICE = false;

	private DeviceInspector getDeviceInspector(final boolean isEmulator, final String deviceId) {
		return new DeviceInspector() {

			@Override
			public boolean isEmulator() {
				return isEmulator;
			}

			@Override
			public String getDeviceId(Context context) {
				return deviceId;
			}
		};
	}

	@Override
	public void setUp() {
		DeviceId.reset(getContext());
	}

	public void testGetFromDeviceWithPermissions() {
		final String deviceId = DeviceId.getDeviceId(getContext(), getDeviceInspector(IS_DEVICE, HARDWARE_DEVICE_ID1));
		assertEquals(deviceId, HARDWARE_DEVICE_ID1);

		DeviceId.clear();

        final String deviceId2 = DeviceId.getDeviceId(getContext(), getDeviceInspector(IS_DEVICE, SMELLY_DUMMY));
		assertEquals(deviceId2, deviceId);

		DeviceId.reset(getContext());

        final String deviceId3 = DeviceId.getDeviceId(getContext(), getDeviceInspector(IS_DEVICE, HARDWARE_DEVICE_ID2));
		assertEquals(deviceId3, HARDWARE_DEVICE_ID2);
	}

	public void testGetFromEmulatorWithPermissions() {
        final String deviceId = DeviceId.getDeviceId(getContext(), getDeviceInspector(IS_EMULATOR, EMULATOR_DEVICE_ID));
		assertTrue(deviceId.startsWith(DeviceId.PCFMS_EMULATOR));

		DeviceId.clear();

        final String deviceId2 = DeviceId.getDeviceId(getContext(), getDeviceInspector(IS_EMULATOR, SMELLY_DUMMY));
		assertEquals(deviceId2, deviceId);

		DeviceId.reset(getContext());

        final String deviceId3 = DeviceId.getDeviceId(getContext(), getDeviceInspector(IS_EMULATOR, EMULATOR_DEVICE_ID));
		assertTrue(deviceId3.startsWith(DeviceId.PCFMS_EMULATOR));
		MoreAsserts.assertNotEqual(deviceId3, deviceId);
	}

	public void testGetFromDeviceWithoutPermissions() {
        final String deviceId = DeviceId.getDeviceId(getContext(), getDeviceInspector(IS_DEVICE, null));
		assertTrue(deviceId.startsWith(DeviceId.PCFMS_DEVICE));

		DeviceId.clear();

        final String deviceId2 = DeviceId.getDeviceId(getContext(), getDeviceInspector(IS_DEVICE, SMELLY_DUMMY));
		assertEquals(deviceId2, deviceId);

		DeviceId.reset(getContext());

        final String deviceId3 = DeviceId.getDeviceId(getContext(), getDeviceInspector(IS_DEVICE, null));
		assertTrue(deviceId3.startsWith(DeviceId.PCFMS_DEVICE));
		MoreAsserts.assertNotEqual(deviceId3, deviceId);
	}

	public void testGetFromEmulatorWithoutPermissions() {
        final String deviceId = DeviceId.getDeviceId(getContext(), getDeviceInspector(IS_EMULATOR, null));
		assertTrue(deviceId.startsWith(DeviceId.PCFMS_EMULATOR));

		DeviceId.clear();

        final String deviceId2 = DeviceId.getDeviceId(getContext(), getDeviceInspector(IS_EMULATOR, SMELLY_DUMMY));
		assertEquals(deviceId2, deviceId);

		DeviceId.reset(getContext());

        final String deviceId3 = DeviceId.getDeviceId(getContext(), getDeviceInspector(IS_EMULATOR, null));
		assertTrue(deviceId3.startsWith(DeviceId.PCFMS_EMULATOR));
		MoreAsserts.assertNotEqual(deviceId3, deviceId);
	}
}
