package org.omnia.pushsdk.database.urihelpers;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import android.content.UriMatcher;
import android.net.Uri;
import android.util.SparseArray;

public class UriHelperFactory {

	private final UriMatcher uriMatcher;
	private final SparseArray<UriHelper> uriHelpers;
	private final Set<String> tableNames;

	public UriHelperFactory() {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriHelpers = new SparseArray<UriHelper>();
		tableNames = new HashSet<String>();
	}

	public void addUriHelper(UriHelper uriHelper, int matchId) {
		final UriMatcherParams uriMatcherParams = uriHelper.getUriMatcherParams();
		uriMatcher.addURI(uriMatcherParams.authority, uriMatcherParams.path, matchId);
		uriHelpers.put(matchId, uriHelper);
		tableNames.add(uriHelper.getDefaultTableName());
	}

	public UriHelper getUriHelper(final Uri uri) {
		final int matchId = uriMatcher.match(uri);
		final UriHelper uriHelper = uriHelpers.get(matchId);
		if (uriHelper != null) {
			return uriHelper;
		} else {
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	public Set<String> getAllTableNames() {
		return Collections.unmodifiableSet(tableNames);
	}

}
