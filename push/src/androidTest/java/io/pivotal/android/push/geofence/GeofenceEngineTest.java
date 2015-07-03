package io.pivotal.android.push.geofence;

import android.test.AndroidTestCase;

import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import io.pivotal.android.push.model.geofence.PCFPushGeofenceDataList;
import io.pivotal.android.push.model.geofence.PCFPushGeofenceLocationMap;
import io.pivotal.android.push.model.geofence.PCFPushGeofenceResponseData;
import io.pivotal.android.push.prefs.FakePushPreferencesProvider;
import io.pivotal.android.push.util.ModelUtil;
import io.pivotal.android.push.util.TimeProvider;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class GeofenceEngineTest extends AndroidTestCase {

    private static final PCFPushGeofenceDataList EMPTY_GEOFENCE_LIST = new PCFPushGeofenceDataList();
    private static final PCFPushGeofenceLocationMap EMPTY_GEOFENCE_MAP = new PCFPushGeofenceLocationMap();

    private PCFPushGeofenceDataList ONE_ITEM_GEOFENCE_LIST;
    private PCFPushGeofenceDataList ONE_ITEM_GEOFENCE_WITH_TAG_LIST;
    private PCFPushGeofenceDataList THREE_ITEM_GEOFENCE_LIST;
    private PCFPushGeofenceDataList THREE_ITEM_GEOFENCE_WITH_TAG_LIST;
    private PCFPushGeofenceDataList FIVE_ITEM_GEOFENCE_LIST;
    private PCFPushGeofenceLocationMap ONE_ITEM_GEOFENCE_MAP;
    private GeofenceEngine engine;
    private GeofenceRegistrar registrar;
    private GeofencePersistentStore store;
    private TimeProvider timeProvider;
    private FakePushPreferencesProvider pushPreferencesProvider;
    private Set<String> EMPTY_TAGS;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("dexmaker.dexcache", mContext.getCacheDir().getPath());
        store = mock(GeofencePersistentStore.class);
        registrar = mock(GeofenceRegistrar.class);
        timeProvider = mock(TimeProvider.class);
        pushPreferencesProvider = new FakePushPreferencesProvider();
        when(timeProvider.currentTimeMillis()).thenReturn(0L); // Pretend the time is always zero so that nothing is expired.
        engine = new GeofenceEngine(registrar, store, timeProvider, pushPreferencesProvider);
        ONE_ITEM_GEOFENCE_LIST = ModelUtil.getPCFPushGeofenceDataList(getContext(), "geofence_one_item.json");
        ONE_ITEM_GEOFENCE_WITH_TAG_LIST = ModelUtil.getPCFPushGeofenceDataList(getContext(), "geofence_one_item_with_tag.json");
        THREE_ITEM_GEOFENCE_LIST = ModelUtil.getPCFPushGeofenceDataList(getContext(), "geofence_three_items.json");
        THREE_ITEM_GEOFENCE_WITH_TAG_LIST = ModelUtil.getPCFPushGeofenceDataList(getContext(), "geofence_three_items_with_tag.json");
        FIVE_ITEM_GEOFENCE_LIST = ModelUtil.getPCFPushGeofenceDataList(getContext(), "geofence_five_items.json");
        ONE_ITEM_GEOFENCE_MAP = new PCFPushGeofenceLocationMap();
        ONE_ITEM_GEOFENCE_MAP.putLocation(ONE_ITEM_GEOFENCE_LIST.first(), 0);
        EMPTY_TAGS = new HashSet<>();
    }

    public void testRequiresGeofenceRegistrar() {
        try {
            engine = new GeofenceEngine(null, store, timeProvider, pushPreferencesProvider);
            fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {}
    }

    public void testRequiresGeofencePersistentStore() {
        try {
            engine = new GeofenceEngine(registrar, null, timeProvider, pushPreferencesProvider);
            fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {}
    }

    public void testRequiresTimeProvider() {
        try {
            engine = new GeofenceEngine(registrar, store, null, pushPreferencesProvider);
            fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {}
    }

    public void testRequiresPushPreferencesProvider() {
        try {
            engine = new GeofenceEngine(registrar, store, timeProvider, null);
            fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {}
    }

    public void testNullResponseDataWithNoTimestamp() {
        engine.processResponseData(0L, null, EMPTY_TAGS);
        verify(registrar, times(1)).reset();
        verify(store, times(1)).reset();
        verifyNoMoreInteractions(registrar);
        verifyNoMoreInteractions(store);
    }

    public void testNullResponseDataWithATimestamp() {
        engine.processResponseData(50L, null, EMPTY_TAGS);
        verifyZeroInteractions(registrar);
        verifyZeroInteractions(store);
    }

    public void testEmptyResponseDataWithNoCurrentlyRegisteredGeofences() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_empty.json");
        engine.processResponseData(0L, updateData, EMPTY_TAGS);
        verify(registrar, times(1)).reset();
        verify(store, times(1)).reset();
        verifyNoMoreInteractions(registrar);
        verifyNoMoreInteractions(store);
    }

    public void testEmptyResponseDataWithOneCurrentlyRegisteredGeofenceWithATimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_empty.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_LIST);
        engine.processResponseData(50L, updateData, EMPTY_TAGS);
        assertRegisterGeofences(ONE_ITEM_GEOFENCE_MAP);
        assertSaveRegisteredGeofences(ONE_ITEM_GEOFENCE_LIST);

        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testOneNewItemWithNoCurrentlyRegisteredGeofencesWithNoTimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_one_item.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(EMPTY_GEOFENCE_LIST);
        engine.processResponseData(0L, updateData, EMPTY_TAGS);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        expectedMap.putLocation(updateData.getGeofences().get(0), 0);
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(updateData.getGeofences());
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, times(1)).reset();
        verify(store, times(1)).reset();
    }

    public void testOneNewItemWithNoCurrentlyRegisteredGeofencesWithATimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_one_item.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(EMPTY_GEOFENCE_LIST);
        engine.processResponseData(50L, updateData, EMPTY_TAGS);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        expectedMap.putLocation(updateData.getGeofences().get(0), 0);
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(updateData.getGeofences());
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testOneNewItemWithOneCurrentlyRegisteredGeofenceWithNoTimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_one_item.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_LIST);
        engine.processResponseData(0L, updateData, EMPTY_TAGS);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        expectedMap.putLocation(updateData.getGeofences().get(0), 0);
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(updateData.getGeofences());
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, times(1)).reset();
        verify(store, times(1)).reset();
    }

    public void testOneNewItemWithOneCurrentlyRegisteredGeofenceWithATimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_one_item.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_LIST);
        engine.processResponseData(50L, updateData, EMPTY_TAGS);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        expectedMap.putLocation(ONE_ITEM_GEOFENCE_LIST.first(), 0);
        expectedMap.putLocation(updateData.getGeofences().get(0), 0);
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(ONE_ITEM_GEOFENCE_LIST);
        expectedList.addAll(updateData.getGeofences());
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testDeleteOneItemThatDoesExistWithNoTimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_delete_one.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_LIST);
        engine.processResponseData(0L, updateData, EMPTY_TAGS);
        verify(registrar, times(1)).reset();
        verify(store, times(1)).reset();
        verifyNoMoreInteractions(registrar);
    }

    public void testDeleteOneItemThatDoesExistWithATimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_delete_one.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_LIST);
        engine.processResponseData(50L, updateData, EMPTY_TAGS);
        assertRegisterGeofences(EMPTY_GEOFENCE_MAP);
        assertSaveRegisteredGeofences(EMPTY_GEOFENCE_LIST);
        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testDeleteOneItemThatDoesNotExistWithEmptyStoreWithNoTimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_delete_one.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(EMPTY_GEOFENCE_LIST);
        engine.processResponseData(0L, updateData, EMPTY_TAGS);
        verify(registrar, times(1)).reset();
        verify(store, times(1)).reset();
        verifyNoMoreInteractions(registrar);
    }

    public void testDeleteOneItemThatDoesNotExistWithEmptyStoreWithATimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_delete_one.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(EMPTY_GEOFENCE_LIST);
        engine.processResponseData(50L, updateData, EMPTY_TAGS);
        verifyZeroInteractions(registrar);
        verify(store, never()).saveRegisteredGeofences(any(PCFPushGeofenceDataList.class));
        verify(store, never()).reset();
    }

    public void testDeleteOneItemThatDoesNotExistWithSavedItemsWithNoTimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_delete_one_other.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_LIST);
        engine.processResponseData(0L, updateData, EMPTY_TAGS);
        verify(registrar, times(1)).reset();
        verify(store, times(1)).reset();
        verifyNoMoreInteractions(registrar);
    }

    public void testDeleteOneItemThatDoesNotExistWithSavedItemsWithATimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_delete_one_other.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_LIST);
        engine.processResponseData(50L, updateData, EMPTY_TAGS);
        assertRegisterGeofences(ONE_ITEM_GEOFENCE_MAP);
        assertSaveRegisteredGeofences(ONE_ITEM_GEOFENCE_LIST);
        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testProcessOneStoredItemWithASubscribedTagWithEmptyResponseDataAndSomeTimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_empty.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_WITH_TAG_LIST);
        engine.processResponseData(60L, updateData, getTags("pineapples"));

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        expectedMap.putLocation(ONE_ITEM_GEOFENCE_WITH_TAG_LIST.get(7L), 0);
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.put(7L, ONE_ITEM_GEOFENCE_WITH_TAG_LIST.get(7L));
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testProcessOneStoredItemWithAnUnsubscribedTagWithEmptyResponseDataAndSomeTimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_empty.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_WITH_TAG_LIST);
        engine.processResponseData(60L, updateData, EMPTY_TAGS);

        assertRegisterGeofences(EMPTY_GEOFENCE_MAP);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.put(7L, ONE_ITEM_GEOFENCE_WITH_TAG_LIST.get(7L));
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testUpdateOneItemWithNoItemsCurrentlyRegisteredWithNoTimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_one_item.json");
        engine.processResponseData(0L, updateData, EMPTY_TAGS);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        expectedMap.putLocation(updateData.getGeofences().get(0), 0);
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(updateData.getGeofences());
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, times(1)).reset();
        verify(store, times(1)).reset();
    }

    public void testUpdateOneItemWithNoItemsCurrentlyRegisteredWithATimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_one_item.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(EMPTY_GEOFENCE_LIST);
        engine.processResponseData(50L, updateData, EMPTY_TAGS);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        expectedMap.putLocation(updateData.getGeofences().get(0), 0);
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(updateData.getGeofences());
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testUpdateOneItemWithATagToAnItemWithNoTagWhileSubscribedToNoTags() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_one_other_item_with_no_tag.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_WITH_TAG_LIST);
        engine.processResponseData(50L, updateData, EMPTY_TAGS);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        expectedMap.putLocation(updateData.getGeofences().get(0), 0);
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(updateData.getGeofences());
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testUpdateOneItemWithATagToAnItemWithNoTagWhileSubscribedToOneTag() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_one_other_item_with_no_tag.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_WITH_TAG_LIST);
        engine.processResponseData(50L, updateData, getTags("pineapples"));

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        expectedMap.putLocation(updateData.getGeofences().get(0), 0);
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(updateData.getGeofences());
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testUpdateOneItemWithATagToAnItemWithADifferentTagWhileSubscribedToNoTags() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_one_item_with_tag.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_WITH_TAG_LIST);
        engine.processResponseData(50L, updateData, EMPTY_TAGS);

        assertRegisterGeofences(EMPTY_GEOFENCE_MAP);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(updateData.getGeofences());
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testUpdateOneItemWithATagToAnItemWithADifferentTagWhileSubscribedToTheOriginalTag() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_one_item_with_tag.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_WITH_TAG_LIST);
        engine.processResponseData(50L, updateData, getTags("pineapples"));

        assertRegisterGeofences(EMPTY_GEOFENCE_MAP);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(updateData.getGeofences());
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testUpdateOneItemWithATagToAnItemWithADifferentTagWhileSubscribedToTheNewTag() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_one_item_with_tag.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_WITH_TAG_LIST);
        engine.processResponseData(50L, updateData, getTags("ice cream"));

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        expectedMap.putLocation(updateData.getGeofences().get(0), 0);
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(updateData.getGeofences());
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }


    public void testUpdateOneItemWithNoItemsCurrentlyRegisteredWithASubscribedTagWithNoTimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_one_item_with_tag.json");
        engine.processResponseData(0L, updateData, getTags("ICE CREAM"));

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        expectedMap.putLocation(updateData.getGeofences().get(0), 0);
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(updateData.getGeofences());
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, times(1)).reset();
        verify(store, times(1)).reset();
    }

    public void testUpdateOneItemWithNoItemsCurrentlyRegisteredWithAnUnsubscribedTagWithNoTimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_one_item_with_tag.json");
        engine.processResponseData(0L, updateData, EMPTY_TAGS);

        assertRegisterGeofences(EMPTY_GEOFENCE_MAP);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(updateData.getGeofences());
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, times(1)).reset();
        verify(store, times(1)).reset();
    }

    public void testUpdateOneItemWithNoItemsCurrentlyRegisteredWithASubscribedTagWithSomeTimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_one_item_with_tag.json");
        engine.processResponseData(60L, updateData, getTags("ICE CREAM"));

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        expectedMap.putLocation(updateData.getGeofences().get(0), 0);
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(updateData.getGeofences());
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, never()).reset();
        verify(store, never()).reset();

    }

    public void testUpdateOneItemWithNoItemsCurrentlyRegisteredWithAnUnsubscribedTagWithSomeTimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_one_item_with_tag.json");

        engine.processResponseData(60L, updateData, EMPTY_TAGS);

        assertRegisterGeofences(EMPTY_GEOFENCE_MAP);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(updateData.getGeofences());
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testUpdateOneItemWithNoTagToAnItemWithATagWhileSubscribedToNoTags() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_one_other_item_with_tag.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_LIST);
        engine.processResponseData(50L, updateData, EMPTY_TAGS);

        assertRegisterGeofences(EMPTY_GEOFENCE_MAP);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(updateData.getGeofences());
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testUpdateOneItemWithNoTagToAnItemWithATagWhileSubscribedToThatTag() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_one_other_item_with_tag.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_LIST);
        engine.processResponseData(50L, updateData, getTags("pineapples"));

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        expectedMap.putLocation(updateData.getGeofences().get(0), 0);
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(updateData.getGeofences());
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testUpdateOneItemWithNoTagToAnItemWithATagWhileSubscribedToADifferentTag() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_one_other_item_with_tag.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_LIST);
        engine.processResponseData(50L, updateData, getTags("ice cream"));

        assertRegisterGeofences(EMPTY_GEOFENCE_MAP);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(updateData.getGeofences());
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testUpdateOneItemWithNoTagToAnItemWithNoTagWhileSubscribedToATag() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_one_other_item_with_no_tag.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_LIST);
        engine.processResponseData(50L, updateData, getTags("pineapples"));

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        expectedMap.putLocation(updateData.getGeofences().get(0), 0);
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(updateData.getGeofences());
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testUpdateOneItemThatIsCurrentlyRegisteredWithNoTimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_one_other_item.json");
        engine.processResponseData(0L, updateData, EMPTY_TAGS);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        expectedMap.putLocation(updateData.getGeofences().get(0), 0);
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(updateData.getGeofences());
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, times(1)).reset();
        verify(store, times(1)).reset();
    }

    public void testUpdateOneItemThatIsCurrentlyRegisteredWithATimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_one_other_item.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_LIST);
        engine.processResponseData(50L, updateData, EMPTY_TAGS);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        expectedMap.putLocation(updateData.getGeofences().get(0), 0);
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(updateData.getGeofences());
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testUpdateOneItemThatIsNotCurrentlyRegisteredWhenOneOtherItemIsAlreadySavedWithNoTimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_one_item.json");
        engine.processResponseData(0L, updateData, EMPTY_TAGS);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        expectedMap.putLocation(updateData.getGeofences().get(0), 0);
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(updateData.getGeofences());
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, times(1)).reset();
        verify(store, times(1)).reset();
    }

    public void testUpdateOneItemThatIsNotCurrentlyRegisteredWhenOneOtherItemIsAlreadySavedWithATimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_one_item.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_LIST);
        engine.processResponseData(50L, updateData, EMPTY_TAGS);
        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        expectedMap.putLocation(updateData.getGeofences().get(0), 0);
        expectedMap.putLocation(ONE_ITEM_GEOFENCE_LIST.get(7L), 0);
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(updateData.getGeofences());
        expectedList.addAll(ONE_ITEM_GEOFENCE_LIST);
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testUpdateSomeItemsAndDeleteSomeItemsWhenNoneCurrentlyStoredWithNoTimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_complex.json");
        engine.processResponseData(0L, updateData, EMPTY_TAGS);
        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        expectedMap.putLocation(updateData.getGeofences().get(0), 0);
        expectedMap.putLocation(updateData.getGeofences().get(1), 0);
        expectedMap.putLocation(updateData.getGeofences().get(2), 0);
        expectedMap.putLocation(updateData.getGeofences().get(2), 1);
        expectedMap.putLocation(updateData.getGeofences().get(2), 2);
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(updateData.getGeofences());
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, times(1)).reset();
        verify(store, times(1)).reset();
    }

    public void testUpdateSomeItemsAndDeleteSomeItemsWhenNoneCurrentlyStoredWithATimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_complex.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(EMPTY_GEOFENCE_LIST);
        engine.processResponseData(50L, updateData, EMPTY_TAGS);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        expectedMap.putLocation(updateData.getGeofences().get(0), 0);
        expectedMap.putLocation(updateData.getGeofences().get(1), 0);
        expectedMap.putLocation(updateData.getGeofences().get(2), 0);
        expectedMap.putLocation(updateData.getGeofences().get(2), 1);
        expectedMap.putLocation(updateData.getGeofences().get(2), 2);
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(updateData.getGeofences());
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testUpdateSomeItemsAndDeleteSomeItemsWhenSomeItemsAreCurrentlyStoredWithNoTimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_complex.json");
        engine.processResponseData(0L, updateData, EMPTY_TAGS);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        expectedMap.putLocation(updateData.getGeofences().get(0), 0); // ID 5  -- got added
        expectedMap.putLocation(updateData.getGeofences().get(1), 0); // ID 10 -- got added
        expectedMap.putLocation(updateData.getGeofences().get(2), 0); // ID 44 -- got added (1st location)
        expectedMap.putLocation(updateData.getGeofences().get(2), 1); // ID 44 -- got added (2nd location)
        expectedMap.putLocation(updateData.getGeofences().get(2), 2); // ID 44 -- got added (3rd location)
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(updateData.getGeofences());
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, times(1)).reset();
        verify(store, times(1)).reset();
    }

    public void testUpdateSomeItemsAndDeleteSomeItemsWhenSomeItemsAreCurrentlyStoredWithATimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_complex.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_LIST);
        engine.processResponseData(50L, updateData, EMPTY_TAGS);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        expectedMap.putLocation(ONE_ITEM_GEOFENCE_LIST.get(7L), 0);   // ID 7  -- was kept. Note that ID 9 was deleted.
        expectedMap.putLocation(updateData.getGeofences().get(0), 0); // ID 5  -- got added
        expectedMap.putLocation(updateData.getGeofences().get(1), 0); // ID 10 -- got added
        expectedMap.putLocation(updateData.getGeofences().get(2), 0); // ID 44 -- got added (1st location)
        expectedMap.putLocation(updateData.getGeofences().get(2), 1); // ID 44 -- got added (2nd location)
        expectedMap.putLocation(updateData.getGeofences().get(2), 2); // ID 44 -- got added (3rd location)
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(ONE_ITEM_GEOFENCE_LIST);
        expectedList.addAll(updateData.getGeofences());
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testUpdateSomeItemsAndDeleteSomeItemsWhenSomeMoreItemsAreCurrentlyStoredWithNoTimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_complex.json");
        engine.processResponseData(0L, updateData, EMPTY_TAGS);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        expectedMap.putLocation(updateData.getGeofences().get(0), 0); // ID 5  -- got added
        expectedMap.putLocation(updateData.getGeofences().get(1), 0); // ID 10 -- got added
        expectedMap.putLocation(updateData.getGeofences().get(2), 0); // ID 44 -- got added (1st location)
        expectedMap.putLocation(updateData.getGeofences().get(2), 1); // ID 44 -- got added (2nd location)
        expectedMap.putLocation(updateData.getGeofences().get(2), 2); // ID 44 -- got added (3rd location)
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.addAll(updateData.getGeofences());
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, times(1)).reset();
        verify(store, times(1)).reset();
    }

    public void testUpdateSomeItemsAndDeleteSomeItemsWhenSomeMoreItemsAreCurrentlyStoredWithATimestamp() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_complex.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(THREE_ITEM_GEOFENCE_LIST);
        engine.processResponseData(50L, updateData, EMPTY_TAGS);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        expectedMap.putLocation(THREE_ITEM_GEOFENCE_LIST.get(7L), 0); // ID 7  -- was kept. Note that ID 9 was deleted.
        expectedMap.putLocation(updateData.getGeofences().get(0), 0); // ID 5  -- got added
        expectedMap.putLocation(updateData.getGeofences().get(1), 0); // ID 10 -- got added
        expectedMap.putLocation(updateData.getGeofences().get(2), 0); // ID 44 -- got added (1st location)
        expectedMap.putLocation(updateData.getGeofences().get(2), 1); // ID 44 -- got added (2nd location)
        expectedMap.putLocation(updateData.getGeofences().get(2), 2); // ID 44 -- got added (3rd location)
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.put(7L, THREE_ITEM_GEOFENCE_LIST.get(7L));
        expectedList.addAll(updateData.getGeofences());
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testUpdateBadTriggerType() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_one_item_bad_trigger.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_LIST);
        engine.processResponseData(50L, updateData, EMPTY_TAGS);
        assertRegisterGeofences(new PCFPushGeofenceLocationMap());
        assertSaveRegisteredGeofences(new PCFPushGeofenceDataList());
        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testCullsItemsWithInsufficientData() throws IOException {
        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_all_items_culled.json");
        engine.processResponseData(50L, updateData, EMPTY_TAGS);
        assertRegisterGeofences(EMPTY_GEOFENCE_MAP);
        assertSaveRegisteredGeofences(EMPTY_GEOFENCE_LIST);
    }

    public void testCullsExpiredItemsFromUpdates() throws IOException {
        timeProvider = mock(TimeProvider.class);
        when(timeProvider.currentTimeMillis()).thenReturn(991142744274L);
        engine = new GeofenceEngine(registrar, store, timeProvider, pushPreferencesProvider);

        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_complex.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(EMPTY_GEOFENCE_LIST);
        engine.processResponseData(50L, updateData, EMPTY_TAGS);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        expectedMap.putLocation(updateData.getGeofences().get(0), 0); // ID 5 was added since it is not expired. the others should be culled.
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.put(5L, updateData.getGeofences().get(0));
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testCullsExpiredItemsFromStorage() throws IOException {
        timeProvider = mock(TimeProvider.class);
        when(timeProvider.currentTimeMillis()).thenReturn(991142744274L);
        engine = new GeofenceEngine(registrar, store, timeProvider, pushPreferencesProvider);

        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_empty.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(THREE_ITEM_GEOFENCE_LIST);
        engine.processResponseData(50L, updateData, EMPTY_TAGS);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        expectedMap.putLocation(THREE_ITEM_GEOFENCE_LIST.get(7L), 0); // ID 7 and ID 44 was kept since it is not expired. the other should be culled.
        expectedMap.putLocation(THREE_ITEM_GEOFENCE_LIST.get(44L), 0); // ID 7 and ID 44 was kept since it is not expired. the other should be culled.
        expectedMap.putLocation(THREE_ITEM_GEOFENCE_LIST.get(44L), 1); // ID 7 and ID 44 was kept since it is not expired. the other should be culled.
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.put(7L, THREE_ITEM_GEOFENCE_LIST.get(7L));
        expectedList.put(44L, THREE_ITEM_GEOFENCE_LIST.get(44L));
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testCullsStoredAndUpdatedItemsThatHaveExpired() throws IOException {
        timeProvider = mock(TimeProvider.class);
        when(timeProvider.currentTimeMillis()).thenReturn(991142744274L);
        engine = new GeofenceEngine(registrar, store, timeProvider, pushPreferencesProvider);

        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_complex.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(THREE_ITEM_GEOFENCE_LIST);
        engine.processResponseData(50L, updateData, EMPTY_TAGS);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        expectedMap.putLocation(updateData.getGeofences().get(0), 0); // ID 5 was added since it is not expired. the others should be culled.
        expectedMap.putLocation(THREE_ITEM_GEOFENCE_LIST.get(7L), 0); // ID 7 was kept since it is not expired. the others should be culled.
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.put(5L, updateData.getGeofences().get(0));
        expectedList.put(7L, THREE_ITEM_GEOFENCE_LIST.get(7L));
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testKeepsStoredItemsThatExpiredButReceivedNotExpiredUpdates() throws IOException {
        timeProvider = mock(TimeProvider.class);
        when(timeProvider.currentTimeMillis()).thenReturn(991142744274L);
        engine = new GeofenceEngine(registrar, store, timeProvider, pushPreferencesProvider);

        final PCFPushGeofenceResponseData updateData = ModelUtil.getPCFPushGeofenceResponseData(getContext(), "geofence_response_data_complex.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(FIVE_ITEM_GEOFENCE_LIST);
        engine.processResponseData(50L, updateData, EMPTY_TAGS);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        expectedMap.putLocation(updateData.getGeofences().get(0), 0); // ID 5 was added since it is not expired. the others should be culled.
        expectedMap.putLocation(FIVE_ITEM_GEOFENCE_LIST.get(11L), 0); // ID 11 was kept since it is not expired. the others should be culled.
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.put(5L, updateData.getGeofences().get(0));
        expectedList.put(11L, FIVE_ITEM_GEOFENCE_LIST.get(11L));
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testCullsStoredItemsWithBadLocations() throws IOException {
        final PCFPushGeofenceDataList storedItems = ModelUtil.getPCFPushGeofenceDataList(getContext(), "geofence_one_item_bad_radius.json");
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(storedItems);
        engine.processResponseData(50L, new PCFPushGeofenceResponseData(), EMPTY_TAGS);
        assertRegisterGeofences(new PCFPushGeofenceLocationMap());
        assertSaveRegisteredGeofences(new PCFPushGeofenceDataList());
        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testClearNullLocation() throws IOException {
        engine.clearLocations(null);
        verifyZeroInteractions(registrar);
        verifyZeroInteractions(store);
    }

    public void testClearEmptyLocations() throws IOException {
        final PCFPushGeofenceLocationMap emptyList = new PCFPushGeofenceLocationMap();
        engine.clearLocations(emptyList);
        verifyZeroInteractions(registrar);
        verifyZeroInteractions(store);
    }

    public void testClearOneItemWhileSubscribedToADifferentTag() throws IOException {
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(THREE_ITEM_GEOFENCE_WITH_TAG_LIST);
        setTags("ducks");
        final PCFPushGeofenceLocationMap locationsToClear = new PCFPushGeofenceLocationMap();
        locationsToClear.putLocation(THREE_ITEM_GEOFENCE_WITH_TAG_LIST.get(9L), 0); // remove the rats
        engine.clearLocations(locationsToClear);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        expectedMap.putLocation(THREE_ITEM_GEOFENCE_WITH_TAG_LIST.get(7L), 0);
        expectedMap.putLocation(THREE_ITEM_GEOFENCE_WITH_TAG_LIST.get(44L), 0);
        expectedMap.putLocation(THREE_ITEM_GEOFENCE_WITH_TAG_LIST.get(44L), 1);
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.put(7L, THREE_ITEM_GEOFENCE_WITH_TAG_LIST.get(7L));
        expectedList.put(44L, THREE_ITEM_GEOFENCE_WITH_TAG_LIST.get(44L));
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testClearOneItemWhileSubscribedToTheItemsTag() throws IOException {
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(THREE_ITEM_GEOFENCE_WITH_TAG_LIST);
        setTags("rats");
        final PCFPushGeofenceLocationMap locationsToClear = new PCFPushGeofenceLocationMap();
        locationsToClear.putLocation(THREE_ITEM_GEOFENCE_WITH_TAG_LIST.get(9L), 0); // remove the rats
        engine.clearLocations(locationsToClear);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        expectedMap.putLocation(THREE_ITEM_GEOFENCE_WITH_TAG_LIST.get(44L), 0);
        expectedMap.putLocation(THREE_ITEM_GEOFENCE_WITH_TAG_LIST.get(44L), 1);
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.put(7L, THREE_ITEM_GEOFENCE_WITH_TAG_LIST.get(7L));
        expectedList.put(44L, THREE_ITEM_GEOFENCE_WITH_TAG_LIST.get(44L));
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testClearOneLocation() throws IOException {
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(FIVE_ITEM_GEOFENCE_LIST);
        final PCFPushGeofenceLocationMap locations = new PCFPushGeofenceLocationMap();
        locations.putLocation(FIVE_ITEM_GEOFENCE_LIST.get(5L), 0);
        engine.clearLocations(locations);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        expectedMap.putLocation(FIVE_ITEM_GEOFENCE_LIST.get(11L), 0);
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.put(11L, FIVE_ITEM_GEOFENCE_LIST.get(11L));
        expectedList.put(44L, FIVE_ITEM_GEOFENCE_LIST.get(44L));
        expectedList.put(49L, FIVE_ITEM_GEOFENCE_LIST.get(49L));
        expectedList.put(51L, FIVE_ITEM_GEOFENCE_LIST.get(51L));
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testClearTwoLocations() throws IOException {
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(FIVE_ITEM_GEOFENCE_LIST);
        final PCFPushGeofenceLocationMap locations = new PCFPushGeofenceLocationMap();
        locations.putLocation(FIVE_ITEM_GEOFENCE_LIST.get(11L), 0);
        locations.putLocation(FIVE_ITEM_GEOFENCE_LIST.get(44L), 0);
        engine.clearLocations(locations);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        expectedMap.putLocation(FIVE_ITEM_GEOFENCE_LIST.get(5L), 0);
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        FIVE_ITEM_GEOFENCE_LIST.get(44L).getLocations().remove(0);
        expectedList.put(5L, FIVE_ITEM_GEOFENCE_LIST.get(5L));
        expectedList.put(44L, FIVE_ITEM_GEOFENCE_LIST.get(44L));
        expectedList.put(49L, FIVE_ITEM_GEOFENCE_LIST.get(49L));
        expectedList.put(51L, FIVE_ITEM_GEOFENCE_LIST.get(51L));
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testClearSixLocations() throws IOException {
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(FIVE_ITEM_GEOFENCE_LIST);
        final PCFPushGeofenceLocationMap locations = new PCFPushGeofenceLocationMap();
        locations.putLocation(FIVE_ITEM_GEOFENCE_LIST.get(5L), 0);
        locations.putLocation(FIVE_ITEM_GEOFENCE_LIST.get(11L), 0);
        locations.putLocation(FIVE_ITEM_GEOFENCE_LIST.get(44L), 0);
        locations.putLocation(FIVE_ITEM_GEOFENCE_LIST.get(44L), 1);
        locations.putLocation(FIVE_ITEM_GEOFENCE_LIST.get(49L), 1);
        locations.putLocation(FIVE_ITEM_GEOFENCE_LIST.get(51L), 0);
        engine.clearLocations(locations);

        assertRegisterGeofences(EMPTY_GEOFENCE_MAP);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        FIVE_ITEM_GEOFENCE_LIST.get(49L).getLocations().remove(1);
        expectedList.put(49L, FIVE_ITEM_GEOFENCE_LIST.get(49L));
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testClearLocationsThatDoNotExist() throws IOException {
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(FIVE_ITEM_GEOFENCE_LIST);
        final PCFPushGeofenceLocationMap locations = new PCFPushGeofenceLocationMap();
        locations.putLocation(THREE_ITEM_GEOFENCE_LIST.get(7L), 0);
        locations.putLocation(THREE_ITEM_GEOFENCE_LIST.get(9L), 0);
        engine.clearLocations(locations);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        expectedMap.putLocation(FIVE_ITEM_GEOFENCE_LIST.get(5L), 0);
        expectedMap.putLocation(FIVE_ITEM_GEOFENCE_LIST.get(11L), 0);
        assertRegisterGeofences(expectedMap);

        final PCFPushGeofenceDataList expectedList = new PCFPushGeofenceDataList();
        expectedList.put(5L, FIVE_ITEM_GEOFENCE_LIST.get(5L));
        expectedList.put(11L, FIVE_ITEM_GEOFENCE_LIST.get(11L));
        expectedList.put(44L, FIVE_ITEM_GEOFENCE_LIST.get(44L));
        expectedList.put(49L, FIVE_ITEM_GEOFENCE_LIST.get(49L));
        expectedList.put(51L, FIVE_ITEM_GEOFENCE_LIST.get(51L));
        assertSaveRegisteredGeofences(expectedList);

        verify(registrar, never()).reset();
        verify(store, never()).reset();
    }

    public void testReregisterNoLocations() {
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(EMPTY_GEOFENCE_LIST);
        engine.reregisterCurrentLocations(EMPTY_TAGS);
        assertRegisterGeofences(EMPTY_GEOFENCE_MAP);
        verify(store, never()).saveRegisteredGeofences(any(PCFPushGeofenceDataList.class));
        verify(store, never()).reset();
        verify(registrar, never()).reset();
    }

    public void testReregisterSomeLocations() {
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(THREE_ITEM_GEOFENCE_LIST);
        engine.reregisterCurrentLocations(EMPTY_TAGS);

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        expectedMap.putLocation(THREE_ITEM_GEOFENCE_LIST.get(7L), 0);
        expectedMap.putLocation(THREE_ITEM_GEOFENCE_LIST.get(9L), 0);
        expectedMap.putLocation(THREE_ITEM_GEOFENCE_LIST.get(44L), 0);
        expectedMap.putLocation(THREE_ITEM_GEOFENCE_LIST.get(44L), 1);

        assertRegisterGeofences(expectedMap);
        verify(store, never()).saveRegisteredGeofences(any(PCFPushGeofenceDataList.class));
        verify(store, never()).reset();
        verify(registrar, never()).reset();
    }

    public void testReregisterSomeLocationsWithTagsWithASubscribedTag() {
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_WITH_TAG_LIST);
        engine.reregisterCurrentLocations(getTags("pineapples"));

        final PCFPushGeofenceLocationMap expectedMap = new PCFPushGeofenceLocationMap();
        expectedMap.putLocation(ONE_ITEM_GEOFENCE_WITH_TAG_LIST.get(7L), 0);
        assertRegisterGeofences(expectedMap);

        verify(store, never()).saveRegisteredGeofences(any(PCFPushGeofenceDataList.class));
        verify(store, never()).reset();
        verify(registrar, never()).reset();
    }

    public void testReregisterSomeLocationsWithTagsWithNoSubscribedTags() {
        when(store.getCurrentlyRegisteredGeofences()).thenReturn(ONE_ITEM_GEOFENCE_WITH_TAG_LIST);
        engine.reregisterCurrentLocations(EMPTY_TAGS);

        assertRegisterGeofences(EMPTY_GEOFENCE_MAP);

        verify(store, never()).saveRegisteredGeofences(any(PCFPushGeofenceDataList.class));
        verify(store, never()).reset();
        verify(registrar, never()).reset();
    }

    public void testResetStore() {
        engine.resetStore();
        verify(store, times(1)).reset();
        verify(registrar, never()).reset();
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

    private Set<String> getTags(String tag) {
        final Set<String> tags = new HashSet<>();
        tags.add(tag);
        return tags;
    }

    private void setTags(String tag) {
        final Set<String> tags = new HashSet<>();
        tags.add(tag);
        pushPreferencesProvider.setTags(tags);
    }
}