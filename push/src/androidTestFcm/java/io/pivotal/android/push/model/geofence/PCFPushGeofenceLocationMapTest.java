package io.pivotal.android.push.model.geofence;

import android.test.AndroidTestCase;

import java.io.IOException;
import java.util.Set;

import io.pivotal.android.push.util.ModelUtil;

public class PCFPushGeofenceLocationMapTest extends AndroidTestCase {

    private final PCFPushGeofenceLocationMap model = new PCFPushGeofenceLocationMap();

    public void testAddAllNull() {
        final int itemsAdded = model.addAll(null);
        assertEquals(0, itemsAdded);
        assertEquals(0, model.size());
        assertEquals(0, model.locationEntrySet().size());
    }

    public void testAddAllEmpty() {
        final PCFPushGeofenceDataList emptyList = new PCFPushGeofenceDataList();
        final int itemsAdded = model.addAll(emptyList);
        assertEquals(0, itemsAdded);
        assertEquals(0, model.size());
        assertEquals(0, model.locationEntrySet().size());
    }

    public void testAddAllWithThreeItems() throws IOException {
        final PCFPushGeofenceDataList list = ModelUtil.getPCFPushGeofenceDataList(getContext(),
            "geofence_three_items.json");
        final int itemsAdded = model.addAll(list);
        assertEquals(4, itemsAdded);
        assertEquals(4, model.size());
        assertEquals(4, model.locationEntrySet().size());
    }

    public void testFilteredAddNullList() {
        final int itemsAdded = model.addFiltered(null, new PCFPushGeofenceLocationMap.Filter() {
            @Override
            public boolean filterItem(PCFPushGeofenceData item, PCFPushGeofenceLocation location) {
                return true;
            }
        });
        assertEquals(0, itemsAdded);
        assertEquals(0, model.size());
        assertEquals(0, model.locationEntrySet().size());
    }

    public void testFilteredAddNullFilter() throws IOException {
        final PCFPushGeofenceDataList list = ModelUtil.getPCFPushGeofenceDataList(getContext(),
            "geofence_three_items.json");
        final int itemsAdded = model.addFiltered(list, null);
        assertEquals(0, itemsAdded);
        assertEquals(0, model.size());
        assertEquals(0, model.locationEntrySet().size());
    }

    public void testFilterAdd() throws IOException {
        final PCFPushGeofenceDataList list = ModelUtil.getPCFPushGeofenceDataList(getContext(),
            "geofence_three_items.json");
        final int itemsAdded = model.addFiltered(list, new PCFPushGeofenceLocationMap.Filter() {
            @Override
            public boolean filterItem(PCFPushGeofenceData item, PCFPushGeofenceLocation location) {
                return item.getId() == 9L || item.getId() == 44L && location.getId() == 82L;
            }
        });
        assertEquals(2, itemsAdded);
        assertEquals(2, model.size());
        assertEquals(2, model.locationEntrySet().size());
        assertNotNull(model.get(PCFPushGeofenceLocationMap.getAndroidRequestId(9L, 66L)));
        assertNotNull(model.get(PCFPushGeofenceLocationMap.getAndroidRequestId(44L, 82L)));
    }

    public void testLocationEntrySet() throws IOException {
        final PCFPushGeofenceDataList list = ModelUtil.getPCFPushGeofenceDataList(getContext(),
            "geofence_one_item.json");
        model.addAll(list);
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
