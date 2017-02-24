package io.pivotal.android.push.backend.geofence;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.mock.MockContext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import io.pivotal.android.push.geofence.GeofencePersistentStore;
import io.pivotal.android.push.model.geofence.PCFPushGeofenceData;
import io.pivotal.android.push.model.geofence.PCFPushGeofenceDataList;
import io.pivotal.android.push.util.FileHelper;
import io.pivotal.android.push.util.GsonUtil;
import io.pivotal.android.push.util.ModelUtil;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class GeofencePersistentStoreTest extends AndroidTestCase {

    private File file;
    private Context context;
    private FileHelper fileHelper;
    private GeofencePersistentStore store;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("dexmaker.dexcache", mContext.getCacheDir().getPath());
        file = mock(File.class);
        context = mock(MockContext.class);
        fileHelper = mock(FileHelper.class);
        store = new GeofencePersistentStore(context, fileHelper);
    }

    public void testEmptyGet() {
        when(context.getFilesDir()).thenReturn(file);
        when(file.list(any(FilenameFilter.class))).thenReturn(new String[]{});
        final PCFPushGeofenceDataList geofences = store.getCurrentlyRegisteredGeofences();
        assertNotNull(geofences);
        assertEquals(0, geofences.size());
    }

    public void testGetOneFile() throws IOException {
        when(context.getFilesDir()).thenReturn(file);
        when(file.list(any(FilenameFilter.class))).thenReturn(new String[]{"test_file_1"});
        when(fileHelper.getReader("test_file_1")).thenReturn(getReader(
            "geofence_one_item_persisted_1.json"));
        final PCFPushGeofenceDataList geofences = store.getCurrentlyRegisteredGeofences();
        assertNotNull(geofences);
        assertEquals(1, geofences.size());
        assertNotNull(geofences.get(1L));
    }

    public void testGetThreeFiles() throws IOException {
        when(context.getFilesDir()).thenReturn(file);
        when(file.list(any(FilenameFilter.class))).thenReturn(new String[]{"test_file_1", "test_file_2", "test_file_3"});
        when(fileHelper.getReader("test_file_1")).thenReturn(getReader(
            "geofence_one_item_persisted_1.json"));
        when(fileHelper.getReader("test_file_2")).thenReturn(getReader(
            "geofence_one_item_persisted_2.json"));
        when(fileHelper.getReader("test_file_3")).thenReturn(getReader(
            "geofence_one_item_persisted_3.json"));
        final PCFPushGeofenceDataList geofences = store.getCurrentlyRegisteredGeofences();
        assertNotNull(geofences);
        assertEquals(3, geofences.size());
        assertNotNull(geofences.get(1L));
        assertNotNull(geofences.get(2L));
        assertNotNull(geofences.get(3L));
    }

    public void testReadsBadFiles() throws IOException {
        when(context.getFilesDir()).thenReturn(file);
        when(file.list(any(FilenameFilter.class))).thenReturn(new String[]{"test_file_1", "bad_json", "doesnt_exist"});
        when(fileHelper.getReader("test_file_1")).thenReturn(getReader(
            "geofence_one_item_persisted_1.json"));
        when(fileHelper.getReader("bad_json")).thenReturn(getReader(
            "geofence_one_item_persisted_bad.json"));
        when(fileHelper.getReader("doesnt_exist")).thenThrow(new FileNotFoundException("Ran out of tacos"));
        final PCFPushGeofenceDataList geofences = store.getCurrentlyRegisteredGeofences();
        assertNotNull(geofences);
        assertEquals(1, geofences.size());
        assertNotNull(geofences.get(1L));
    }

    public void testNullWrite() {
        store.saveRegisteredGeofences(null);

        verifyZeroInteractions(context);
        verifyZeroInteractions(file);
        verifyZeroInteractions(fileHelper);
    }

    public void testEmptyWriteWithEmptyStore() {
        when(context.getFilesDir()).thenReturn(file);
        when(file.list(any(FilenameFilter.class))).thenReturn(new String[]{});

        store.saveRegisteredGeofences(new PCFPushGeofenceDataList());

        verify(context, times(1)).getFilesDir();
        verify(file, times(1)).list(any(FilenameFilter.class));
        verifyNoMoreInteractions(context);
        verifyNoMoreInteractions(file);
        verifyZeroInteractions(fileHelper);
    }

    public void testEmptyWriteWithPopulatedStore() {
        when(context.getFilesDir()).thenReturn(file);
        when(file.list(any(FilenameFilter.class))).thenReturn(new String[]{"old_file_1", "old_file_2"});
        when(context.deleteFile("old_file_1")).thenReturn(true);
        when(context.deleteFile("old_file_2")).thenReturn(true);

        store.saveRegisteredGeofences(new PCFPushGeofenceDataList());

        verify(context, times(1)).getFilesDir();
        verify(context, times(1)).deleteFile("old_file_1");
        verify(context, times(1)).deleteFile("old_file_2");
        verify(file, times(1)).list(any(FilenameFilter.class));
        verifyNoMoreInteractions(context);
        verifyNoMoreInteractions(file);
        verifyZeroInteractions(fileHelper);
    }

    public void testWritesOneFileWithEmptyStore() throws IOException {
        when(context.getFilesDir()).thenReturn(file);
        when(file.list(any(FilenameFilter.class))).thenReturn(new String[]{});

        final ByteArrayOutputStream outputStream = mockWriter(GeofencePersistentStore.GEOFENCE_PERSISTENT_STORE_FILE_PREFIX + "7.json");
        final PCFPushGeofenceDataList list = ModelUtil.getPCFPushGeofenceDataList(getContext(),
            "geofence_one_item.json");

        store.saveRegisteredGeofences(list);

        assertGeofenceIdEquals(7L, outputStream);

        verify(context, times(1)).getFilesDir();
        verify(context, never()).deleteFile(anyString());
        verify(file, times(1)).list(any(FilenameFilter.class));
        verifyNoMoreInteractions(context);
        verifyNoMoreInteractions(file);
    }

    public void testWritesOneFileWithPopulatedStore() throws IOException {
        when(context.getFilesDir()).thenReturn(file);
        when(file.list(any(FilenameFilter.class))).thenReturn(new String[]{GeofencePersistentStore.GEOFENCE_PERSISTENT_STORE_FILE_PREFIX + "7.json", "old_file_2"});
        when(context.deleteFile("old_file_2")).thenReturn(true);

        final ByteArrayOutputStream outputStream = mockWriter(GeofencePersistentStore.GEOFENCE_PERSISTENT_STORE_FILE_PREFIX + "7.json");
        final PCFPushGeofenceDataList list = ModelUtil.getPCFPushGeofenceDataList(getContext(),
            "geofence_one_item.json");

        store.saveRegisteredGeofences(list);

        assertGeofenceIdEquals(7L, outputStream);

        verify(context, times(1)).getFilesDir();
        verify(context, times(1)).deleteFile("old_file_2");
        verify(file, times(1)).list(any(FilenameFilter.class));
        verifyNoMoreInteractions(context);
        verifyNoMoreInteractions(file);
    }

    public void testWritesThreeFilesWithEmptyStore() throws IOException {
        when(context.getFilesDir()).thenReturn(file);
        when(file.list(any(FilenameFilter.class))).thenReturn(new String[]{});

        final ByteArrayOutputStream outputStream1 = mockWriter(GeofencePersistentStore.GEOFENCE_PERSISTENT_STORE_FILE_PREFIX + "7.json");
        final ByteArrayOutputStream outputStream2 = mockWriter(GeofencePersistentStore.GEOFENCE_PERSISTENT_STORE_FILE_PREFIX + "9.json");
        final ByteArrayOutputStream outputStream3 = mockWriter(GeofencePersistentStore.GEOFENCE_PERSISTENT_STORE_FILE_PREFIX + "44.json");
        final PCFPushGeofenceDataList list = ModelUtil.getPCFPushGeofenceDataList(getContext(),
            "geofence_three_items.json");

        store.saveRegisteredGeofences(list);

        assertGeofenceIdEquals(7L, outputStream1);
        assertGeofenceIdEquals(9L, outputStream2);
        assertGeofenceIdEquals(44L, outputStream3);

        verify(context, times(1)).getFilesDir();
        verify(context, never()).deleteFile(anyString());
        verify(file, times(1)).list(any(FilenameFilter.class));
        verify(fileHelper, times(3)).getWriter(anyString());
        verify(fileHelper, never()).getReader(anyString());
        verifyNoMoreInteractions(context);
        verifyNoMoreInteractions(file);
        verifyNoMoreInteractions(fileHelper);
    }

    public void testWritesThreeFilesWithPopulatedStore() throws IOException {
        when(context.getFilesDir()).thenReturn(file);
        when(file.list(any(FilenameFilter.class))).thenReturn(new String[]{GeofencePersistentStore.GEOFENCE_PERSISTENT_STORE_FILE_PREFIX + "7.json", "old_file_2"});
        when(context.deleteFile("old_file_2")).thenReturn(true);

        final ByteArrayOutputStream outputStream1 = mockWriter(GeofencePersistentStore.GEOFENCE_PERSISTENT_STORE_FILE_PREFIX + "7.json");
        final ByteArrayOutputStream outputStream2 = mockWriter(GeofencePersistentStore.GEOFENCE_PERSISTENT_STORE_FILE_PREFIX + "9.json");
        final ByteArrayOutputStream outputStream3 = mockWriter(GeofencePersistentStore.GEOFENCE_PERSISTENT_STORE_FILE_PREFIX + "44.json");
        final PCFPushGeofenceDataList list = ModelUtil.getPCFPushGeofenceDataList(getContext(),
            "geofence_three_items.json");

        store.saveRegisteredGeofences(list);

        assertGeofenceIdEquals(7L, outputStream1);
        assertGeofenceIdEquals(9L, outputStream2);
        assertGeofenceIdEquals(44L, outputStream3);

        verify(context, times(1)).getFilesDir();
        verify(context, times(1)).deleteFile("old_file_2");
        verify(file, times(1)).list(any(FilenameFilter.class));
        verify(fileHelper, times(3)).getWriter(anyString());
        verify(fileHelper, never()).getReader(anyString());
        verifyNoMoreInteractions(context);
        verifyNoMoreInteractions(file);
        verifyNoMoreInteractions(fileHelper);
    }

    public void testWritesThreeFilesAndOneFailsWithEmptyStore() throws IOException {
        when(context.getFilesDir()).thenReturn(file);
        when(file.list(any(FilenameFilter.class))).thenReturn(new String[]{});

        final ByteArrayOutputStream outputStream1 = mockWriter(GeofencePersistentStore.GEOFENCE_PERSISTENT_STORE_FILE_PREFIX + "7.json");
        final ByteArrayOutputStream outputStream2 = mockWriter(GeofencePersistentStore.GEOFENCE_PERSISTENT_STORE_FILE_PREFIX + "44.json");
        mockFailingWriter(GeofencePersistentStore.GEOFENCE_PERSISTENT_STORE_FILE_PREFIX + "9.json");
        final PCFPushGeofenceDataList list = ModelUtil.getPCFPushGeofenceDataList(getContext(),
            "geofence_three_items.json");

        store.saveRegisteredGeofences(list);

        assertGeofenceIdEquals(7L, outputStream1);
        assertGeofenceIdEquals(44L, outputStream2);

        verify(context, times(1)).getFilesDir();
        verify(context, never()).deleteFile(anyString());
        verify(file, times(1)).list(any(FilenameFilter.class));
        verify(fileHelper, times(3)).getWriter(anyString());
        verify(fileHelper, never()).getReader(anyString());
        verifyNoMoreInteractions(context);
        verifyNoMoreInteractions(file);
        verifyNoMoreInteractions(fileHelper);
    }

    public void testWritesThreeFilesAndOneFailsWithPopulatedStore() throws IOException {
        when(context.getFilesDir()).thenReturn(file);
        when(file.list(any(FilenameFilter.class))).thenReturn(new String[]{GeofencePersistentStore.GEOFENCE_PERSISTENT_STORE_FILE_PREFIX + "7.json", "old_file_2"});
        when(context.deleteFile("old_file_2")).thenReturn(true);

        final ByteArrayOutputStream outputStream1 = mockWriter(GeofencePersistentStore.GEOFENCE_PERSISTENT_STORE_FILE_PREFIX + "7.json");
        final ByteArrayOutputStream outputStream2 = mockWriter(GeofencePersistentStore.GEOFENCE_PERSISTENT_STORE_FILE_PREFIX + "44.json");
        mockFailingWriter(GeofencePersistentStore.GEOFENCE_PERSISTENT_STORE_FILE_PREFIX + "9.json");
        final PCFPushGeofenceDataList list = ModelUtil.getPCFPushGeofenceDataList(getContext(),
            "geofence_three_items.json");

        store.saveRegisteredGeofences(list);

        assertGeofenceIdEquals(7L, outputStream1);
        assertGeofenceIdEquals(44L, outputStream2);

        verify(context, times(1)).getFilesDir();
        verify(context, times(1)).deleteFile("old_file_2");
        verify(file, times(1)).list(any(FilenameFilter.class));
        verify(fileHelper, times(3)).getWriter(anyString());
        verify(fileHelper, never()).getReader(anyString());
        verifyNoMoreInteractions(context);
        verifyNoMoreInteractions(file);
        verifyNoMoreInteractions(fileHelper);
    }

    private void assertGeofenceIdEquals(long expectedId, ByteArrayOutputStream outputStream) {
        final PCFPushGeofenceData data = GsonUtil.getGson().fromJson(new InputStreamReader(new ByteArrayInputStream(outputStream.toByteArray())), PCFPushGeofenceData.class);
        assertEquals(expectedId, data.getId());
    }

    private Reader getReader(String filename) throws IOException {
        final InputStream inputStream = getContext().getAssets().open(filename);
        final Reader reader = new InputStreamReader(inputStream);
        return reader;
    }

    private ByteArrayOutputStream mockWriter(String filename) throws IOException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final Writer writer = new OutputStreamWriter(outputStream);
        when(fileHelper.getWriter(filename)).thenReturn(writer);
        return outputStream;
    }

    private void mockFailingWriter(String filename) throws IOException {
        when(fileHelper.getWriter(filename)).thenThrow(new IOException("I forgot my pants"));
    }
}
