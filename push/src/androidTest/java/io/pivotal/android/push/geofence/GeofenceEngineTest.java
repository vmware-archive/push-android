package io.pivotal.android.push.geofence;

import android.test.AndroidTestCase;

import org.mockito.ArgumentCaptor;

import java.io.IOException;

import io.pivotal.android.push.model.geofence.PCFPushGeofenceData;
import io.pivotal.android.push.model.geofence.PCFPushGeofenceDataList;
import io.pivotal.android.push.model.geofence.PCFPushGeofenceLocation;
import io.pivotal.android.push.model.geofence.PCFPushGeofenceLocationMap;
import io.pivotal.android.push.model.geofence.PCFPushGeofenceResponseData;
import io.pivotal.android.push.util.ModelUtil;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class GeofenceEngineTest extends AndroidTestCase {

    private static final PCFPushGeofenceDataList EMPTY_GEOFENCE_LIST = new PCFPushGeofenceDataList();
    private static final PCFPushGeofenceLocationMap EMPTY_GEOFENCE_MAP = new PCFPushGeofenceLocationMap();

    private PCFPushGeofenceDataList ONE_ITEM_GEOFENCE_LIST;
    private PCFPushGeofenceDataList THREE_ITEM_GEOFENCE_LIST;
    private PCFPushGeofenceLocationMap ONE_ITEM_GEOFENCE_MAP;
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
        ONE_ITEM_GEOFENCE_MAP = new PCFPushGeofenceLocationMap();
        putLocation(ONE_ITEM_GEOFENCE_MAP, ONE_ITEM_GEOFENCE_LIST.first(), 0);
    }

    public void testNullResponseData() {
        engine.processResponseData(null);
        verifyZeroInteractions(registrar);
        verifyZeroInteractions(store);
    }

    public void testEmptyResponseDataWithNoCurrentlyRegisteredGeofences() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_empty.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(EMPTY_GEOFENCE_LIST);
        engine.processResponseData(updateData);
        verifyZeroInteractions(registrar);
        verify(store, never()).saveRegisteredGeofences(any(PCFPushGeofenceDataList.class));
    }

    public void testEmptyResponseDataWithOneCurrentlyRegisteredGeofence() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_empty.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_LIST);
        engine.processResponseData(updateData);
        assertRegisterGeofences(ONE_ITEM_GEOFENCE_MAP);
        assertSaveRegisteredGeofences(ONE_ITEM_GEOFENCE_LIST);
    }

    public void testOneNewItemWithNoCurrentlyRegisteredGeofences() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_one_item.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(EMPTY_GEOFENCE_LIST);
        engine.processResponseData(updateData);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        putLocation(expectedMap, updateData.getGeofences().get(0), 0);
        assertEquals(1, expectedMap.size());
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(updateData.getGeofences());
        assertEquals(1, expectedList.size());
        assertSaveRegisteredGeofences(expectedList);
    }

    public void testOneNewItemWithOneCurrentlyRegisteredGeofence() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_one_item.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_LIST);
        engine.processResponseData(updateData);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        putLocation(expectedMap, ONE_ITEM_GEOFENCE_LIST.first(), 0);
        putLocation(expectedMap, updateData.getGeofences().get(0), 0);
        assertEquals(2, expectedMap.size());
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(ONE_ITEM_GEOFENCE_LIST);
        expectedList.addAll(updateData.getGeofences());
        assertEquals(2, expectedList.size());
        assertSaveRegisteredGeofences(expectedList);
    }

    public void testDeleteOneItemThatDoesExist() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_delete_one.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_LIST);
        engine.processResponseData(updateData);
        assertRegisterGeofences(EMPTY_GEOFENCE_MAP);
        assertSaveRegisteredGeofences(EMPTY_GEOFENCE_LIST);
    }

    public void testDeleteOneItemThatDoesNotExistWithEmptyStore() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_delete_one.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(EMPTY_GEOFENCE_LIST);
        engine.processResponseData(updateData);
        verifyZeroInteractions(registrar);
        verify(store, never()).saveRegisteredGeofences(any(PCFPushGeofenceDataList.class));
    }

    public void testDeleteOneItemThatDoesNotExistWithSavedItems() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_delete_one_other.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_LIST);
        engine.processResponseData(updateData);
        assertRegisterGeofences(ONE_ITEM_GEOFENCE_MAP);
        assertSaveRegisteredGeofences(ONE_ITEM_GEOFENCE_LIST);
    }

    public void testUpdateOneItemWithNoItemsCurrentlyRegistered() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_one_item.json");
        engine.processResponseData(updateData);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        putLocation(expectedMap, updateData.getGeofences().get(0), 0);
        assertEquals(1, expectedMap.size());
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(updateData.getGeofences());
        assertEquals(1, expectedList.size());
        assertSaveRegisteredGeofences(expectedList);
    }

    public void testUpdateOneItemThatIsCurrentlyRegistered() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_one_other_item.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_LIST);
        engine.processResponseData(updateData);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        putLocation(expectedMap, updateData.getGeofences().get(0), 0);
        assertEquals(1, expectedMap.size());
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(updateData.getGeofences());
        assertEquals(1, expectedList.size());
        assertSaveRegisteredGeofences(expectedList);
    }

    public void testUpdateOneItemThatIsNotCurrentlyRegisteredWhenOneOtherItemIsAlreadySaved() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_one_item.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_LIST);
        engine.processResponseData(updateData);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        putLocation(expectedMap, updateData.getGeofences().get(0), 0);
        putLocation(expectedMap, ONE_ITEM_GEOFENCE_LIST.get(7L), 0);
        assertEquals(2, expectedMap.size());
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(updateData.getGeofences());
        expectedList.addAll(ONE_ITEM_GEOFENCE_LIST);
        assertEquals(2, expectedList.size());
        assertSaveRegisteredGeofences(expectedList);
    }

    public void testUpdateSomeItemsAndDeleteSomeItemsWhenNoneCurrentlyStored() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_complex.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(EMPTY_GEOFENCE_LIST);
        engine.processResponseData(updateData);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        putLocation(expectedMap, updateData.getGeofences().get(0), 0);
        putLocation(expectedMap, updateData.getGeofences().get(1), 0);
        putLocation(expectedMap, updateData.getGeofences().get(2), 0);
        putLocation(expectedMap, updateData.getGeofences().get(2), 1);
        putLocation(expectedMap, updateData.getGeofences().get(2), 2);
        assertEquals(5, expectedMap.size());
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(updateData.getGeofences());
        assertEquals(3, expectedList.size());
        assertSaveRegisteredGeofences(expectedList);
    }

    public void testUpdateSomeItemsAndDeleteSomeItemsWhenSomeItemsAreCurrentlyStored() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_complex.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_LIST);
        engine.processResponseData(updateData);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        putLocation(expectedMap, ONE_ITEM_GEOFENCE_LIST.get(7L), 0);   // ID 7  -- was kept. Note that ID 9 was deleted.
        putLocation(expectedMap, updateData.getGeofences().get(0), 0); // ID 5  -- got added
        putLocation(expectedMap, updateData.getGeofences().get(1), 0); // ID 10 -- got added
        putLocation(expectedMap, updateData.getGeofences().get(2), 0); // ID 44 -- got added (1st location)
        putLocation(expectedMap, updateData.getGeofences().get(2), 1); // ID 44 -- got added (2nd location)
        putLocation(expectedMap, updateData.getGeofences().get(2), 2); // ID 44 -- got added (3rd location)
        assertEquals(6, expectedMap.size());
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(ONE_ITEM_GEOFENCE_LIST);
        expectedList.addAll(updateData.getGeofences());
        assertEquals(4, expectedList.size());
        assertSaveRegisteredGeofences(expectedList);
    }

    public void testUpdateSomeItemsAndDeleteSomeItemsWhenSomeMoreItemsAreCurrentlyStored() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_complex.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(THREE_ITEM_GEOFENCE_LIST);
        engine.processResponseData(updateData);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        putLocation(expectedMap, THREE_ITEM_GEOFENCE_LIST.get(7L), 0); // ID 7  -- was kept. Note that ID 9 was deleted.
        putLocation(expectedMap, updateData.getGeofences().get(0), 0); // ID 5  -- got added
        putLocation(expectedMap, updateData.getGeofences().get(1), 0); // ID 10 -- got added
        putLocation(expectedMap, updateData.getGeofences().get(2), 0); // ID 44 -- got added (1st location)
        putLocation(expectedMap, updateData.getGeofences().get(2), 1); // ID 44 -- got added (2nd location)
        putLocation(expectedMap, updateData.getGeofences().get(2), 2); // ID 44 -- got added (3rd location)
        assertEquals(6, expectedMap.size());
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.put(7L, THREE_ITEM_GEOFENCE_LIST.get(7L));
        expectedList.addAll(updateData.getGeofences());
        assertEquals(4, expectedList.size());
        assertSaveRegisteredGeofences(expectedList);
    }

    public void testCullsItemsWithInsufficientData() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_all_items_culled.json");
        engine.processResponseData(updateData);
        assertRegisterGeofences(EMPTY_GEOFENCE_MAP);
        assertSaveRegisteredGeofences(EMPTY_GEOFENCE_LIST);
    }

    private void assertRegisterGeofences(PCFPushGeofenceLocationMap geofences) {
        final ArgumentCaptor<PCFPushGeofenceLocationMap> captor = ArgumentCaptor.forClass(PCFPushGeofenceLocationMap.class);
        verify(registrar).registerGeofences(captor.capture(), any(PCFPushGeofenceDataList.class));
        assertEquals(geofences, captor.getValue());
    }

    private void assertSaveRegisteredGeofences(PCFPushGeofenceDataList geofences) {
        final ArgumentCaptor<PCFPushGeofenceDataList> captor = ArgumentCaptor.forClass(PCFPushGeofenceDataList.class);
        verify(store).saveRegisteredGeofences(captor.capture());
        PCFPushGeofenceDataList value = captor.getValue();
        assertEquals(geofences, value);
    }

    private void putLocation(PCFPushGeofenceLocationMap map, PCFPushGeofenceData geofence, int i) {
        final PCFPushGeofenceLocation location = geofence.getLocations().get(i);
        final String androidRequestId = PCFPushGeofenceLocationMap.getAndroidRequestId(geofence.getId(), location.getId());
        map.put(androidRequestId, location);
    }

}