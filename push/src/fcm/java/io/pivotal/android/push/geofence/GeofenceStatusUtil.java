package io.pivotal.android.push.geofence;

import android.content.Context;
import android.content.Intent;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import io.pivotal.android.push.Push;
import io.pivotal.android.push.util.FileHelper;
import io.pivotal.android.push.util.GsonUtil;
import io.pivotal.android.push.util.Logger;
import io.pivotal.android.push.version.GeofenceStatus;

public class GeofenceStatusUtil {

    private static final String GEOFENCE_STATUS_FILE_NAME = "pivotal.push.geofence_status.json";
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Context context;

    public GeofenceStatusUtil(Context context) {
        this.context = context;
    }

    public void saveGeofenceStatusAndSendBroadcast(GeofenceStatus resultantStatus) {
        saveGeofenceStatus(resultantStatus);
        sendGeofenceUpdateBroadcast();
    }

    public void saveGeofenceStatus(GeofenceStatus status) {
        lock.writeLock().lock();
        try {
            final FileHelper fileHelper = new FileHelper(context);
            final Writer writer = fileHelper.getWriter(GEOFENCE_STATUS_FILE_NAME);
            GsonUtil.getGson().toJson(status, writer);
            try {
                writer.close();
            } catch (IOException ee) {/* Swallow exception on close file*/}

        } catch (IOException e) {
            Logger.ex("Error saving geofence status", e);

        } finally {
            lock.writeLock().unlock();
        }
    }

    private void sendGeofenceUpdateBroadcast() {
        // TODO - consider adding a permission to this broadcast
        final Intent intent = new Intent(Push.GEOFENCE_UPDATE_BROADCAST);
        context.sendBroadcast(intent);
    }

    public GeofenceStatus loadGeofenceStatus() {
        lock.readLock().lock();
        try {
            final FileHelper fileHelper = new FileHelper(context);
            final Reader reader = fileHelper.getReader(GEOFENCE_STATUS_FILE_NAME);
            final GeofenceStatus geofenceStatus = GsonUtil.getGson().fromJson(reader, GeofenceStatus.class);
            try {
                reader.close();
            } catch (IOException ee) {/* Swallow exception on close file*/}

            return geofenceStatus;

        } catch (FileNotFoundException e) {
            return GeofenceStatus.emptyStatus();

        } catch (Exception e) {
            return new GeofenceStatus(true, "Could not load GeofenceStatus: " +e.getLocalizedMessage(), 0);

        } finally {
            lock.readLock().unlock();
        }
    }
}
