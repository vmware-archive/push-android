package io.pivotal.android.push.model.geofence;

import android.test.AndroidTestCase;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;

import io.pivotal.android.push.util.ModelUtil;

public class PCFPushGeofenceDataTest extends AndroidTestCase {

    public void testNewCopyWithoutLocations() throws IOException {
        final TypeToken<PCFPushGeofenceData> typeToken = new TypeToken<PCFPushGeofenceData>(){};
        final PCFPushGeofenceData item = ModelUtil.getJson(getContext(),
            "geofence_one_item_persisted_1.json", typeToken);
        final PCFPushGeofenceData copy = item.newCopyWithoutLocations();
        assertEquals(1L, copy.getId());
        assertEquals(1142744274L, copy.getExpiryTime().getTime());
        assertEquals(1, copy.getPayload().getAndroidFcm().size());
        assertEquals("tacos", copy.getPayload().getAndroidFcm().get("message"));
        assertEquals(2, copy.getTags().size());
        assertEquals("MONDAY", copy.getTags().get(0));
        assertEquals("FRIDAY", copy.getTags().get(1));
        assertEquals("exit", copy.getTriggerType());
        assertEquals(0, copy.getLocations().size());
    }
}
