package io.pivotal.android.push.geofence;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import io.pivotal.android.push.model.geofence.PCFPushGeofenceData;
import io.pivotal.android.push.model.geofence.PCFPushGeofenceDataList;
import io.pivotal.android.push.util.FileHelper;
import io.pivotal.android.push.util.GsonUtil;
import io.pivotal.android.push.util.Logger;

public class GeofencePersistentStore {

    public static final String GEOFENCE_PERSISTENT_STORE_FILE_PREFIX = "pivotal.push.geofence.";

    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final Context context;
    private final FileHelper fileHelper;
    private final Gson gson = GsonUtil.getGson();


    public GeofencePersistentStore(Context context, FileHelper fileHelper) {
        this.context = context;
        this.fileHelper = fileHelper;
    }

    public PCFPushGeofenceDataList getCurrentlyRegisteredGeofences() {

        lock.readLock().lock();

        try {

            final String[] files = getFiles();
            final PCFPushGeofenceDataList result = new PCFPushGeofenceDataList();

            if (files != null) {
                for (final String filename : files) {
                    addFile(result, filename);
                }
            }

            return result;

        } finally {
            lock.readLock().unlock();
        }
    }

    public void saveRegisteredGeofences(PCFPushGeofenceDataList geofences) {
        if (geofences == null) {
            return;
        }

        lock.writeLock().lock();

        try {
            final Set<String> existingFiles = new HashSet<>(Arrays.asList(getFiles()));

            for (final PCFPushGeofenceData geofence : geofences) {
                final String filename = GEOFENCE_PERSISTENT_STORE_FILE_PREFIX + geofence.getId() + ".json";
                writeFile(geofence, filename);
                existingFiles.remove(filename);
            }

            deleteFiles(existingFiles);

        } finally {
            lock.writeLock().unlock();
        }
    }

    public PCFPushGeofenceData getGeofenceData(long id) {

        lock.readLock().lock();

        try {

            return getFromFile(GEOFENCE_PERSISTENT_STORE_FILE_PREFIX + id + ".json");

        } finally {
            lock.readLock().unlock();
        }
    }

    private String[] getFiles() {
        final File filesDir = context.getFilesDir();

        return filesDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.startsWith(GEOFENCE_PERSISTENT_STORE_FILE_PREFIX);
            }
        });
    }

    private void addFile(PCFPushGeofenceDataList result, String filename) {
        final PCFPushGeofenceData geofence = getFromFile(filename);

        if (geofence != null) {
            result.put(geofence.getId(), geofence);
        }
    }

    private void deleteFiles(Iterable<String> files) {
        for (final String filename : files) {
            context.deleteFile(filename);
        }
    }

    private PCFPushGeofenceData getFromFile(final String filename) {
        Reader reader = null;
        try {
            reader = fileHelper.getReader(filename);
            return gson.fromJson(reader, PCFPushGeofenceData.class);

        } catch (FileNotFoundException e) {
            Logger.w("File not found: '" + filename + "', error: " + e.getLocalizedMessage());
        } catch (JsonSyntaxException e) {
            Logger.w("Bad/corrupted Json found: '" + filename + "', error: " + e.getLocalizedMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {}
            }
        }

        return null;
    }

    private void writeFile(PCFPushGeofenceData geofence, String filename) {
        Writer writer = null;
        try {
            writer = fileHelper.getWriter(filename);
            gson.toJson(geofence, writer);

        } catch (IOException e) {
            Logger.w("Error writing json: '" + filename + "', error: " + e.getLocalizedMessage());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {}
            }
        }
    }
}
