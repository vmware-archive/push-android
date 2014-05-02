package com.pivotal.cf.mobile.analyticssdk.database.urihelpers;

import android.net.Uri;

public interface UriHelper {

	public String getType();
	public String getDefaultTableName();
	public QueryParams getQueryParams(Uri uri, String[] projection, String whereClause, String[] whereArgs, String sortOrder);
	public UpdateParams getUpdateParams(Uri uri, String whereClause, String[] whereArgs);
	public DeleteParams getDeleteParams(Uri uri, String whereClause, String[] whereArgs);
	public UriMatcherParams getUriMatcherParams();
}
