package io.pivotal.android.push.geofence;

import android.test.AndroidTestCase;

import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.pivotal.android.push.model.api.PCFPushGeofenceData;
import io.pivotal.android.push.model.api.PCFPushGeofenceDataList;
import io.pivotal.android.push.model.api.PCFPushGeofenceLocation;
import io.pivotal.android.push.model.api.PCFPushGeofenceResponseData;
import io.pivotal.android.push.util.ModelUtil;

import static java.util.Collections.EMPTY_MAP;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class GeofenceEngineTest extends AndroidTestCase {

    private static final PCFPushGeofenceDataList EMPTY_GEOFENCE_LIST = new PCFPushGeofenceDataList();
    private static final Map<String, PCFPushGeofenceLocation> EMPTY_GEOFENCE_MAP = EMPTY_MAP;

    private PCFPushGeofenceDataList ONE_ITEM_GEOFENCE_LIST;
    private PCFPushGeofenceDataList THREE_ITEM_GEOFENCE_LIST;
    private Map<String, PCFPushGeofenceLocation> ONE_ITEM_GEOFENCE_MAP;
    private GeofenceEngine engine;
    private GeofenceRegistrar registrar;
    private GeofencePersistentStore store;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("dexmaker.dexcache", mContext.getCacheDir().getPath());
        store = mock(GeofencePersistentStore.class);
        registrar = mock(GeofenceRegistrar.class);
        engine = new GeofenceEngine(registrar, store);
        ONE_ITEM_GEOFENCE_LIST = ModelUtil.getPCFPushGeofenceDataList(getContext(), "geofence_one_item.json");
        THREE_ITEM_GEOFENCE_LIST = ModelUtil.getPCFPushGeofenceDataList(getContext(), "geofence_three_items.json");
        ONE_ITEM_GEOFENCE_MAP = new HashMap<>();
        putLocation(ONE_ITEM_GEOFENCE_MAP, ONE_ITEM_GEOFENCE_LIST.first(), 0);
    }

    public void testNullResponseData() {
        engine.processResponseData(null);
        verifyZeroInteractions(registrar);
    }

    public void testEmptyResponseDataWithNoCurrentlyRegisteredGeofences() throws IOException {
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(EMPTY_GEOFENCE_LIST);
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_empty.json");
        engine.processResponseData(updateData);
        verifyZeroInteractions(registrar);
    }

    public void testEmptyResponseDataWithOneCurrentlyRegisteredGeofence() throws IOException {
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_LIST);
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_empty.json");
        engine.processResponseData(updateData);
        assertRegisterGeofences(ONE_ITEM_GEOFENCE_MAP);
    }

    public void testOneNewItemWithNoCurrentlyRegisteredGeofences() throws IOException {
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(EMPTY_GEOFENCE_LIST);
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_one_item.json");
        engine.processResponseData(updateData);
        final Map<String, PCFPushGeofenceLocation> map = new HashMap<>();
        putLocation(map, updateData.getGeofences().get(0), 0);
        assertRegisterGeofences(map);
    }

    public void testOneNewItemWithNoOneCurrentlyRegisteredGeofence() throws IOException {
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_LIST);
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_one_item.json");
        engine.processResponseData(updateData);
        final Map<String, PCFPushGeofenceLocation> map = new HashMap<>();
        putLocation(map, ONE_ITEM_GEOFENCE_LIST.first(), 0);
        putLocation(map, updateData.getGeofences().get(0), 0);
        assertRegisterGeofences(map);
    }

    public void testDeleteOneItemThatDoesExist() throws IOException {
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_LIST);
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_delete_one.json");
        engine.processResponseData(updateData);
        assertRegisterGeofences(EMPTY_GEOFENCE_MAP);
    }

    public void testDeleteOneItemThatDoesNotExistWithEmptyStore() throws IOException {
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(EMPTY_GEOFENCE_LIST);
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_delete_one.json");
        engine.processResponseData(updateData);
        verifyZeroInteractions(registrar);
    }

    public void testDeleteOneItemThatDoesNotExistWithSavedItems() throws IOException {
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_LIST);
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_delete_one_other.json");
        engine.processResponseData(updateData);
        assertRegisterGeofences(ONE_ITEM_GEOFENCE_MAP);
    }

    public void testUpdateOneItemWithNoItemsCurrentlyRegistered() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_one_item.json");
        engine.processResponseData(updateData);
        final Map<String, PCFPushGeofenceLocation> map = new HashMap<>();
        putLocation(map, updateData.getGeofences().get(0), 0);
        assertEquals(1, map.size());
        assertRegisterGeofences(map);
    }

    public void testUpdateOneItemThatIsCurrentlyRegistered() throws IOException {
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_LIST);
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_one_other_item.json");
        engine.processResponseData(updateData);
        final Map<String, PCFPushGeofenceLocation> map = new HashMap<>();
        putLocation(map, updateData.getGeofences().get(0), 0);
        assertEquals(1, map.size());
        assertRegisterGeofences(map);
    }

    public void testUpdateOneItemThatIsNotCurrentlyRegisteredWhenOneOtherItemIsAlreadySaved() throws IOException {
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_LIST);
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_one_item.json");
        engine.processResponseData(updateData);
        final Map<String, PCFPushGeofenceLocation> map = new HashMap<>();
        putLocation(map, updateData.getGeofences().get(0), 0);
        putLocation(map, ONE_ITEM_GEOFENCE_LIST.get(7L), 0);
        assertEquals(2, map.size());
        assertRegisterGeofences(map);
    }

    public void testUpdateSomeItemsAndDeleteSomeItemsWhenNoneCurrentlyStored() throws IOException {
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(EMPTY_GEOFENCE_LIST);
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_complex.json");
        engine.processResponseData(updateData);
        final Map<String, PCFPushGeofenceLocation> map = new HashMap<>();
        putLocation(map, updateData.getGeofences().get(0), 0);
        putLocation(map, updateData.getGeofences().get(1), 0);
        putLocation(map, updateData.getGeofences().get(2), 0);
        putLocation(map, updateData.getGeofences().get(2), 1);
        assertEquals(4, map.size());
        assertRegisterGeofences(map);
    }

    public void testUpdateSomeItemsAndDeleteSomeItemsWhenSomeItemsAreCurrentlyStored() throws IOException {
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_LIST);
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_complex.json");
        engine.processResponseData(updateData);
        final Map<String, PCFPushGeofenceLocation> map = new HashMap<>();
        putLocation(map, ONE_ITEM_GEOFENCE_LIST.get(7L), 0);   // ID 7  -- was kept. Note that ID 9 was deleted.
        putLocation(map, updateData.getGeofences().get(0), 0); // ID 5  -- got added
        putLocation(map, updateData.getGeofences().get(1), 0); // ID 10 -- got added
        putLocation(map, updateData.getGeofences().get(2), 0); // ID 44 -- got added
        putLocation(map, updateData.getGeofences().get(2), 1); // ID 44 -- got added
        assertEquals(5, map.size());
        assertRegisterGeofences(map);
    }

    public void testUpdateSomeItemsAndDeleteSomeItemsWhenSomeMoreItemsAreCurrentlyStored() throws IOException {
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(THREE_ITEM_GEOFENCE_LIST);
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_complex.json");
        engine.processResponseData(updateData);
        final Map<String, PCFPushGeofenceLocation> map = new HashMap<>();
        putLocation(map, THREE_ITEM_GEOFENCE_LIST.get(7L), 0); // ID 7  -- was kept. Note that ID 9 was deleted.
        putLocation(map, updateData.getGeofences().get(0), 0); // ID 5  -- got added
        putLocation(map, updateData.getGeofences().get(1), 0); // ID 10 -- got added
        putLocation(map, updateData.getGeofences().get(2), 0); // ID 44 -- got updated
        putLocation(map, updateData.getGeofences().get(2), 1); // ID 44 -- got updated
        assertEquals(5, map.size());
        assertRegisterGeofences(map);
    }

    // todo
    // - update in store
    // both cases for rest of tests (no existing store, and existing store)

    private void assertRegisterGeofences(Map<String, PCFPushGeofenceLocation> geofences) {
        final ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        verify(registrar).registerGeofences(captor.capture());
        assertEquals(geofences, captor.getValue());
    }

    private void putLocation(Map<String, PCFPushGeofenceLocation> map, PCFPushGeofenceData geofence, int i) {
        final PCFPushGeofenceLocation location = geofence.getLocations().get(i);
        final String androidRequestId = GeofenceEngine.getAndroidRequestId(geofence.getId(), location.getId());
        map.put(androidRequestId, location);
    }

}