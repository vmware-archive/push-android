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
import io.pivotal.android.push.util.TimeProvider;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class GeofenceEngineTest extends AndroidTestCase {

    private static final PCFPushGeofenceDataList EMPTY_GEOFENCE_LIST = new PCFPushGeofenceDataList();
    private static final PCFPushGeofenceLocationMap EMPTY_GEOFENCE_MAP = new PCFPushGeofenceLocationMap();

    private PCFPushGeofenceDataList ONE_ITEM_GEOFENCE_LIST;
    private PCFPushGeofenceDataList THREE_ITEM_GEOFENCE_LIST;
    private PCFPushGeofenceDataList FOUR_ITEM_GEOFENCE_LIST;
    private PCFPushGeofenceLocationMap ONE_ITEM_GEOFENCE_MAP;
    private GeofenceEngine engine;
    private GeofenceRegistrar registrar;
    private GeofencePersistentStore store;
    private TimeProvider timeProvider;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("dexmaker.dexcache", mContext.getCacheDir().getPath());
        store = mock(GeofencePersistentStore.class);
        registrar = mock(GeofenceRegistrar.class);
        timeProvider = mock(TimeProvider.class);
        when(timeProvider.currentTimeMillis()).thenReturn(0L); // Pretend the time is always zero so that nothing is expired.
        engine = new GeofenceEngine(registrar, store, timeProvider);
        ONE_ITEM_GEOFENCE_LIST = ModelUtil.getPCFPushGeofenceDataList(getContext(), "geofence_one_item.json");
        THREE_ITEM_GEOFENCE_LIST = ModelUtil.getPCFPushGeofenceDataList(getContext(), "geofence_three_items.json");
        FOUR_ITEM_GEOFENCE_LIST = ModelUtil.getPCFPushGeofenceDataList(getContext(), "geofence_four_items.json");
        ONE_ITEM_GEOFENCE_MAP = new PCFPushGeofenceLocationMap();
        putLocation(ONE_ITEM_GEOFENCE_MAP, ONE_ITEM_GEOFENCE_LIST.first(), 0);
    }

    public void testRequiresGeofenceRegistrar() {
        try {
            engine = new GeofenceEngine(null, store, timeProvider);
            fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {}
    }

    public void testRequiresGeofencePersistentStore() {
        try {
            engine = new GeofenceEngine(registrar, null, timeProvider);
            fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {}
    }


    public void testRequiresTimeProvider() {
        try {
            engine = new GeofenceEngine(registrar, store, null);
            fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {}
    }

    public void testNullResponseDataWithNoTimestamp() {
        engine.processResponseData(0L, null);
        verify(registrar, times(1)).reset();
        verify(store, times(1)).reset();
        verifyNoMoreInteractions(registrar);
        verifyNoMoreInteractions(store);
    }

    public void testNullResponseDataWithATimestamp() {
        engine.processResponseData(50L, null);
        verifyZeroInteractions(registrar);
        verifyZeroInteractions(store);
    }

    public void testEmptyResponseDataWithNoCurrentlyRegisteredGeofences() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_empty.json");
        engine.processResponseData(0L, updateData);
        verify(registrar, times(1)).reset();
        verify(store, times(1)).reset();
        verifyNoMoreInteractions(registrar);
        verifyNoMoreInteractions(store);
    }

    public void testEmptyResponseDataWithOneCurrentlyRegisteredGeofenceWithATimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_empty.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_LIST);
        engine.processResponseData(50L, updateData);
        assertRegisterGeofences(ONE_ITEM_GEOFENCE_MAP);
        assertSaveRegisteredGeofences(ONE_ITEM_GEOFENCE_LIST);

        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testOneNewItemWithNoCurrentlyRegisteredGeofencesWithNoTimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_one_item.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(EMPTY_GEOFENCE_LIST);
        engine.processResponseData(0L, updateData);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        putLocation(expectedMap, updateData.getGeofences().get(0), 0);
        assertEquals(1, expectedMap.size());
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(updateData.getGeofences());
        assertEquals(1, expectedList.size());
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, times(1)).reset();
        verify(store, times(1)).reset();
    }

    public void testOneNewItemWithNoCurrentlyRegisteredGeofencesWithATimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_one_item.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(EMPTY_GEOFENCE_LIST);
        engine.processResponseData(50L, updateData);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        putLocation(expectedMap, updateData.getGeofences().get(0), 0);
        assertEquals(1, expectedMap.size());
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(updateData.getGeofences());
        assertEquals(1, expectedList.size());
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testOneNewItemWithOneCurrentlyRegisteredGeofenceWithNoTimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_one_item.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_LIST);
        engine.processResponseData(0L, updateData);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        putLocation(expectedMap, updateData.getGeofences().get(0), 0);
        assertEquals(1, expectedMap.size());
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(updateData.getGeofences());
        assertEquals(1, expectedList.size());
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, times(1)).reset();
        verify(store, times(1)).reset();
    }

    public void testOneNewItemWithOneCurrentlyRegisteredGeofenceWithATimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_one_item.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_LIST);
        engine.processResponseData(50L, updateData);

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

        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testDeleteOneItemThatDoesExistWithNoTimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_delete_one.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_LIST);
        engine.processResponseData(0L, updateData);
        verify(registrar, times(1)).reset();
        verify(store, times(1)).reset();
        verifyNoMoreInteractions(registrar);
    }

    public void testDeleteOneItemThatDoesExistWithATimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_delete_one.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_LIST);
        engine.processResponseData(50L, updateData);
        assertRegisterGeofences(EMPTY_GEOFENCE_MAP);
        assertSaveRegisteredGeofences(EMPTY_GEOFENCE_LIST);
        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testDeleteOneItemThatDoesNotExistWithEmptyStoreWithNoTimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_delete_one.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(EMPTY_GEOFENCE_LIST);
        engine.processResponseData(0L, updateData);
        verify(registrar, times(1)).reset();
        verify(store, times(1)).reset();
        verifyNoMoreInteractions(registrar);
    }

    public void testDeleteOneItemThatDoesNotExistWithEmptyStoreWithATimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_delete_one.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(EMPTY_GEOFENCE_LIST);
        engine.processResponseData(50L, updateData);
        verifyZeroInteractions(registrar);
        verify(store, never()).saveRegisteredGeofences(any(PCFPushGeofenceDataList.class));
        verify(store, never()).reset();
    }

    public void testDeleteOneItemThatDoesNotExistWithSavedItemsWithNoTimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_delete_one_other.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_LIST);
        engine.processResponseData(0L, updateData);
        verify(registrar, times(1)).reset();
        verify(store, times(1)).reset();
        verifyNoMoreInteractions(registrar);
    }

    public void testDeleteOneItemThatDoesNotExistWithSavedItemsWithATimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_delete_one_other.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_LIST);
        engine.processResponseData(50L, updateData);
        assertRegisterGeofences(ONE_ITEM_GEOFENCE_MAP);
        assertSaveRegisteredGeofences(ONE_ITEM_GEOFENCE_LIST);
        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testUpdateOneItemWithNoItemsCurrentlyRegisteredWithNoTimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_one_item.json");
        engine.processResponseData(0L, updateData);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        putLocation(expectedMap, updateData.getGeofences().get(0), 0);
        assertEquals(1, expectedMap.size());
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(updateData.getGeofences());
        assertEquals(1, expectedList.size());
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, times(1)).reset();
        verify(store, times(1)).reset();
    }

    public void testUpdateOneItemWithNoItemsCurrentlyRegisteredWithATimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_one_item.json");
        engine.processResponseData(50L, updateData);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        putLocation(expectedMap, updateData.getGeofences().get(0), 0);
        assertEquals(1, expectedMap.size());
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(updateData.getGeofences());
        assertEquals(1, expectedList.size());
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testUpdateOneItemThatIsCurrentlyRegisteredWithNoTimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_one_other_item.json");
        engine.processResponseData(0L, updateData);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        putLocation(expectedMap, updateData.getGeofences().get(0), 0);
        assertEquals(1, expectedMap.size());
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(updateData.getGeofences());
        assertEquals(1, expectedList.size());
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, times(1)).reset();
        verify(store, times(1)).reset();
    }

    public void testUpdateOneItemThatIsCurrentlyRegisteredWithATimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_one_other_item.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_LIST);
        engine.processResponseData(50L, updateData);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        putLocation(expectedMap, updateData.getGeofences().get(0), 0);
        assertEquals(1, expectedMap.size());
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(updateData.getGeofences());
        assertEquals(1, expectedList.size());
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testUpdateOneItemThatIsNotCurrentlyRegisteredWhenOneOtherItemIsAlreadySavedWithNoTimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_one_item.json");
        engine.processResponseData(0L, updateData);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        putLocation(expectedMap, updateData.getGeofences().get(0), 0);
        assertEquals(1, expectedMap.size());
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(updateData.getGeofences());
        assertEquals(1, expectedList.size());
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, times(1)).reset();
        verify(store, times(1)).reset();
    }

    public void testUpdateOneItemThatIsNotCurrentlyRegisteredWhenOneOtherItemIsAlreadySavedWithATimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_one_item.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_LIST);
        engine.processResponseData(50L, updateData);

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

        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testUpdateSomeItemsAndDeleteSomeItemsWhenNoneCurrentlyStoredWithNoTimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_complex.json");
        engine.processResponseData(0L, updateData);

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

        verify(registrar, times(1)).reset();
        verify(store, times(1)).reset();
    }

    public void testUpdateSomeItemsAndDeleteSomeItemsWhenNoneCurrentlyStoredWithATimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_complex.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(EMPTY_GEOFENCE_LIST);
        engine.processResponseData(50L, updateData);

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

        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testUpdateSomeItemsAndDeleteSomeItemsWhenSomeItemsAreCurrentlyStoredWithNoTimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_complex.json");
        engine.processResponseData(0L, updateData);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        putLocation(expectedMap, updateData.getGeofences().get(0), 0); // ID 5  -- got added
        putLocation(expectedMap, updateData.getGeofences().get(1), 0); // ID 10 -- got added
        putLocation(expectedMap, updateData.getGeofences().get(2), 0); // ID 44 -- got added (1st location)
        putLocation(expectedMap, updateData.getGeofences().get(2), 1); // ID 44 -- got added (2nd location)
        putLocation(expectedMap, updateData.getGeofences().get(2), 2); // ID 44 -- got added (3rd location)
        assertEquals(5, expectedMap.size());
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(updateData.getGeofences());
        assertEquals(3, expectedList.size());
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, times(1)).reset();
        verify(store, times(1)).reset();
    }

    public void testUpdateSomeItemsAndDeleteSomeItemsWhenSomeItemsAreCurrentlyStoredWithATimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_complex.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_LIST);
        engine.processResponseData(50L, updateData);

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

        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testUpdateSomeItemsAndDeleteSomeItemsWhenSomeMoreItemsAreCurrentlyStoredWithNoTimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_complex.json");
        engine.processResponseData(0L, updateData);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        putLocation(expectedMap, updateData.getGeofences().get(0), 0); // ID 5  -- got added
        putLocation(expectedMap, updateData.getGeofences().get(1), 0); // ID 10 -- got added
        putLocation(expectedMap, updateData.getGeofences().get(2), 0); // ID 44 -- got added (1st location)
        putLocation(expectedMap, updateData.getGeofences().get(2), 1); // ID 44 -- got added (2nd location)
        putLocation(expectedMap, updateData.getGeofences().get(2), 2); // ID 44 -- got added (3rd location)
        assertEquals(5, expectedMap.size());
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(updateData.getGeofences());
        assertEquals(3, expectedList.size());
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, times(1)).reset();
        verify(store, times(1)).reset();
    }

    public void testUpdateSomeItemsAndDeleteSomeItemsWhenSomeMoreItemsAreCurrentlyStoredWithATimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_complex.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(THREE_ITEM_GEOFENCE_LIST);
        engine.processResponseData(50L, updateData);

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

        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testCullsItemsWithInsufficientData() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_all_items_culled.json");
        engine.processResponseData(50L, updateData);
        assertRegisterGeofences(EMPTY_GEOFENCE_MAP);
        assertSaveRegisteredGeofences(EMPTY_GEOFENCE_LIST);
    }

    public void testCullsExpiredItemsFromUpdates() throws IOException {
        timeProvider = mock(TimeProvider.class);
        when(timeProvider.currentTimeMillis()).thenReturn(991142744274L);
        engine = new GeofenceEngine(registrar, store, timeProvider);

        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_complex.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(EMPTY_GEOFENCE_LIST);
        engine.processResponseData(50L, updateData);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        putLocation(expectedMap, updateData.getGeofences().get(0), 0); // ID 5 was added since it is not expired. the others should be culled.
        assertEquals(1, expectedMap.size());
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.put(5L, updateData.getGeofences().get(0));
        assertEquals(1, expectedList.size());
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testCullsExpiredItemsFromStorage() throws IOException {
        timeProvider = mock(TimeProvider.class);
        when(timeProvider.currentTimeMillis()).thenReturn(991142744274L);
        engine = new GeofenceEngine(registrar, store, timeProvider);

        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_empty.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(THREE_ITEM_GEOFENCE_LIST);
        engine.processResponseData(50L, updateData);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        putLocation(expectedMap, THREE_ITEM_GEOFENCE_LIST.get(7L), 0); // ID 7 and ID 44 was kept since it is not expired. the other should be culled.
        putLocation(expectedMap, THREE_ITEM_GEOFENCE_LIST.get(44L), 0); // ID 7 and ID 44 was kept since it is not expired. the other should be culled.
        putLocation(expectedMap, THREE_ITEM_GEOFENCE_LIST.get(44L), 1); // ID 7 and ID 44 was kept since it is not expired. the other should be culled.
        assertEquals(3, expectedMap.size());
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.put(7L, THREE_ITEM_GEOFENCE_LIST.get(7L));
        expectedList.put(44L, THREE_ITEM_GEOFENCE_LIST.get(44L));
        assertEquals(2, expectedList.size());
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testCullsStoredAndUpdatedItemsThatHaveExpired() throws IOException {
        timeProvider = mock(TimeProvider.class);
        when(timeProvider.currentTimeMillis()).thenReturn(991142744274L);
        engine = new GeofenceEngine(registrar, store, timeProvider);

        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_complex.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(THREE_ITEM_GEOFENCE_LIST);
        engine.processResponseData(50L, updateData);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        putLocation(expectedMap, updateData.getGeofences().get(0), 0); // ID 5 was added since it is not expired. the others should be culled.
        putLocation(expectedMap, THREE_ITEM_GEOFENCE_LIST.get(7L), 0); // ID 7 was kept since it is not expired. the others should be culled.
        assertEquals(2, expectedMap.size());
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.put(5L, updateData.getGeofences().get(0));
        expectedList.put(7L, THREE_ITEM_GEOFENCE_LIST.get(7L));
        assertEquals(2, expectedList.size());
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testKeepsStoredItemsThatExpiredButReceivedNotExpiredUpdates() throws IOException {
        timeProvider = mock(TimeProvider.class);
        when(timeProvider.currentTimeMillis()).thenReturn(991142744274L);
        engine = new GeofenceEngine(registrar, store, timeProvider);

        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_complex.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(FOUR_ITEM_GEOFENCE_LIST);
        engine.processResponseData(50L, updateData);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        putLocation(expectedMap, updateData.getGeofences().get(0), 0); // ID 5 was added since it is not expired. the others should be culled.
        putLocation(expectedMap, FOUR_ITEM_GEOFENCE_LIST.get(11L), 0); // ID 11 was kept since it is not expired. the others should be culled.
        assertEquals(2, expectedMap.size());
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.put(5L, updateData.getGeofences().get(0));
        expectedList.put(11L, FOUR_ITEM_GEOFENCE_LIST.get(11L));
        assertEquals(2, expectedList.size());
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, never()).reset();
        verify(store, never()).reset();
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