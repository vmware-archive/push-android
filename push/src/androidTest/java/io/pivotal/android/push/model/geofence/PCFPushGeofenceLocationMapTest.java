package io.pivotal.android.push.model.geofence;

import android.test.AndroidTestCase;

import java.io.IOException;
import java.util.Set;

import io.pivotal.android.push.util.ModelUtil;

public class PCFPushGeofenceLocationMapTest extends AndroidTestCase {

    private final PCFPushGeofenceLocationMap model = new PCFPushGeofenceLocationMap();

    public void testAddAllNull() {
        model.addAll(null);
        assertEquals(0, model.size());
        assertEquals(0, model.locationEntrySet().size());
    }

    public void testAddAllEmpty() {
        final PCFPushGeofenceDataList emptyList = new PCFPushGeofenceDataList();
        model.addAll(emptyList);
        assertEquals(0, model.size());
        assertEquals(0, model.locationEntrySet().size());
    }

    public void testAddAllWithThreeItems() throws IOException {
        final PCFPushGeofenceDataList emptyList = ModelUtil.getPCFPushGeofenceDataList(getContext(), "geofence_three_items.json");
        model.addAll(emptyList);
        assertEquals(4, model.size());
        assertEquals(4, model.locationEntrySet().size());
    }

    public void testLocationEntrySet() throws IOException {
        final PCFPushGeofenceDataList emptyList = ModelUtil.getPCFPushGeofenceDataList(getContext(), "geofence_one_item.json");
        model.addAll(emptyList);
        assertEquals(1, model.size());
        final Set<PCFPushGeofenceLocationMap.LocationEntry> locationEntries = model.locationEntrySet();
        assertEquals(1, locationEntries.size());
        for (final PCFPushGeofenceLocationMap.LocationEntry locationEntry : locationEntries) {
            assertEquals(7L, locationEntry.getGeofenceId());
            assertEquals(66, locationEntry.getLocationId());
            assertEquals("robs_wizard_tacos", locationEntry.getLocation().getName());
        }
    }
}
