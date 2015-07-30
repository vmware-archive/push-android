package io.pivotal.android.push.database;

import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseArray;

import java.util.HashMap;
import java.util.Map;

public class FakeCursor implements Cursor {
	
	public static class Field {

		private String name;
		private int column;
		private Object value;

		public Field(String name, int column, Object value) {
			this.name = name;
			this.column = column;

			// Android cursors don't have booleans or bytes, so represent them
			// as integers. Represents char as strings of length 1.
			if (value instanceof Boolean) {
				this.value = value.equals(Boolean.TRUE) ? 1 : 0;
			} else if (value.getClass() == boolean.class) {
				this.value = ((boolean) (Boolean) value == true ? 1 : 0);
			} else if (value instanceof Byte) {
				this.value = Integer.valueOf(((Byte) value).intValue());
			} else if (value.getClass() == byte.class) {
				this.value = (int) (Byte) value;
			} else if (value instanceof Character || value.getClass() == char.class) {
				this.value = String.valueOf(value);
			} else {
				this.value = value;
			}
		}

		public String getName() {
			return name;
		}

		public int getColumnIndex() {
			return column;
		}

		public Object getValue() {
			return value;
		}
	}

	private Map<String, Integer> columnIndexForName = new HashMap<String, Integer>();
	private SparseArray<String> columnNameForIndex = new SparseArray<String>();
	private SparseArray<Object> values = new SparseArray<Object>();
	private int columnCount = 0;

	public void addField(String name, Object value) {
		final Field field = new Field(name, columnCount, value);
		columnIndexForName.put(field.getName(), field.getColumnIndex());
		columnNameForIndex.put(field.getColumnIndex(), field.getName());
		values.put(field.getColumnIndex(), field.getValue());
		columnCount += 1;
	}

	@Override
	public void close() {
	}

	@Override
	public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
	}

	@Override
	@Deprecated
	public void deactivate() {
	}

	@Override
	public byte[] getBlob(int columnIndex) {
		return (byte[]) values.get(columnIndex);
	}

	@Override
	public int getColumnCount() {
		return columnCount;
	}

	@Override
	public int getColumnIndex(String columnName) {
		if (columnIndexForName.containsKey(columnName)) {
			return columnIndexForName.get(columnName);
		} else {
			return -1;
		}
	}

	@Override
	public int getColumnIndexOrThrow(String columnName) throws IllegalArgumentException {
		if (columnIndexForName.containsKey(columnName)) {
			return columnIndexForName.get(columnName);
		} else {
			throw new IllegalArgumentException("Unknown columnName: " + columnName);
		}
	}

	@Override
	public String getColumnName(int columnIndex) {
		return columnNameForIndex.get(columnIndex);
	}

	@Override
	public String[] getColumnNames() {
		final String[] columnNames = new String[columnCount];
		for (int i = 0; i < columnCount; i += 1) {
			columnNames[i] = columnNameForIndex.get(i);
		}
		return columnNames;
	}

	@Override
	public int getCount() {
		return 1;
	}

	@Override
	public double getDouble(int columnIndex) {
		return (Double) values.get(columnIndex);
	}

	@Override
	public Bundle getExtras() {
		return null;
	}

	@Override
	public float getFloat(int columnIndex) {
		return (Float) values.get(columnIndex);
	}

	@Override
	public int getInt(int columnIndex) {
		return (Integer) values.get(columnIndex);
	}

	@Override
	public long getLong(int columnIndex) {
		return (Long) values.get(columnIndex);
	}

	@Override
	public int getPosition() {
		return 0;
	}

	@Override
	public short getShort(int columnIndex) {
		return (Short) values.get(columnIndex);
	}

	@Override
	public String getString(int columnIndex) {
		return (String) values.get(columnIndex);
	}

	@Override
	public int getType(int columnIndex) {
		return 0;
	}

	@Override
	public boolean getWantsAllOnMoveCalls() {
		return false;
	}

	@Override
	public boolean isAfterLast() {
		return false;
	}

	@Override
	public boolean isBeforeFirst() {
		return false;
	}

	@Override
	public boolean isClosed() {
		return false;
	}

	@Override
	public boolean isFirst() {
		return false;
	}

	@Override
	public boolean isLast() {
		return false;
	}

	@Override
	public boolean isNull(int columnIndex) {
		return false;
	}

	@Override
	public boolean move(int offset) {
		return false;
	}

	@Override
	public boolean moveToFirst() {
		return false;
	}

	@Override
	public boolean moveToLast() {
		return false;
	}

	@Override
	public boolean moveToNext() {
		return false;
	}

	@Override
	public boolean moveToPosition(int position) {
		return false;
	}

	@Override
	public boolean moveToPrevious() {
		return false;
	}

	@Override
	public void registerContentObserver(ContentObserver observer) {
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
	}

	@Override
	@Deprecated
	public boolean requery() {
		return false;
	}

	@Override
	public Bundle respond(Bundle extras) {
		return null;
	}

	@Override
	public void setNotificationUri(ContentResolver cr, Uri uri) {
	}

    @Override
    public Uri getNotificationUri() {
        return null;
    }

    @Override
	public void unregisterContentObserver(ContentObserver observer) {
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
	}

}
