package io.pivotal.android.push.model.geofence;

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
        if (i != null) {
            for (final PCFPushGeofenceData item : i) {
                put(item.getId(), item);
                changed = true;
            }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof PCFPushGeofenceDataList)) return false;

        final PCFPushGeofenceDataList other = (PCFPushGeofenceDataList)o;
        if (size() != other.size()) return false;

        final Iterator<PCFPushGeofenceData> otherIterator = other.iterator();

        for (final PCFPushGeofenceData thisItem : this) {
            final PCFPushGeofenceData otherItem = otherIterator.next();
            if (thisItem == null) {
                if (otherItem != null) return false;
            } else {
                if (!thisItem.equals(otherItem)) return false;
            }
        }

        return true;
    }
}
