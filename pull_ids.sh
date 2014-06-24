#!/bin/bash
adb pull /mnt/sdcard/Android/data/com.pivotal.mss.pushsdk.sample/files/pushlib/gcm_registration_id.txt gcm_registration_id.txt || {
	echo "Error reading gcm_registration_id from device"
}
echo "Read GCM registration ID:"
cat gcm_registration_id.txt

adb pull /mnt/sdcard/Android/data/com.pivotal.mss.pushsdk.sample/files/pushlib/device_uuid.txt device_uuid.txt || {
	echo "Error reading device_uuid from device"
}
echo "Read device UUID:"
cat device_uuid.txt
