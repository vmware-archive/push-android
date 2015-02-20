package io.pivotal.android.push.model.api;

import android.support.v4.util.LongSparseArray;

import java.util.Iterator;

public class PCFPushGeofenceDataList extends LongSparseArray<PCFPushGeofenceData> implements Iterable<PCFPushGeofenceData> {

    public PCFPushGeofenceData first() {
        if (size() <= 0) {
            return null;
        }

        return get(keyAt(0));
    }

    public boolean addAll(Iterable<PCFPushGeofenceData> i) {
        boolean changed = false;
        for (final PCFPushGeofenceData item : i) {
            put(item.getId(), item);
            changed = true;
        }
        return changed;
    }

    @Override
    public Iterator<PCFPushGeofenceData> iterator() {
        return new Iterator<PCFPushGeofenceData>() {

            private final int size = size();
            private int i = 0;

            @Override
            public boolean hasNext() {
                return i < size;
            }

            @Override
            public PCFPushGeofenceData next() {
                return get(keyAt(i++));
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
