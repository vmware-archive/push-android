package com.pivotal.cf.mobile.pushsdk.model.urihelpers;

import android.net.Uri;
import android.provider.BaseColumns;

import com.pivotal.cf.mobile.pushsdk.database.Database;
import com.pivotal.cf.mobile.pushsdk.database.urihelpers.DeleteParams;
import com.pivotal.cf.mobile.pushsdk.database.urihelpers.QueryParams;
import com.pivotal.cf.mobile.pushsdk.database.urihelpers.UpdateParams;
import com.pivotal.cf.mobile.pushsdk.database.urihelpers.UriHelper;
import com.pivotal.cf.mobile.pushsdk.database.urihelpers.UriMatcherParams;
import com.pivotal.cf.mobile.pushsdk.util.StringUtil;

public class EventsUriHelper implements UriHelper {

	@Override
	public String getType() {
		return "vnd.android.cursor.item/vnd.com.pivotal.cf.mobile.pushsdk.events";
	}

	@Override
	public String getDefaultTableName() {
		return Database.EVENTS_TABLE_NAME;
	}
	
	@Override
	public UriMatcherParams getUriMatcherParams() {
		final UriMatcherParams uriMatcherParams = new UriMatcherParams();
		uriMatcherParams.authority = Database.AUTHORITY;
		uriMatcherParams.path = getDefaultTableName() + "/*";
		return uriMatcherParams;
	}

	@Override
	public QueryParams getQueryParams(final Uri uri, final String[] projection, final String whereClause, final String[] whereArgs, final String sortOrder) {
		final long id = Long.parseLong(uri.getLastPathSegment());
		final QueryParams queryParams = new QueryParams();
		queryParams.uri = uri;
		queryParams.projection = projection;
		queryParams.whereClause = StringUtil.append(whereClause, BaseColumns._ID + " = " + id, " AND ");
		queryParams.whereArgs = whereArgs;
		queryParams.sortOrder = sortOrder;
		return queryParams;
	}

	@Override
	public UpdateParams getUpdateParams(Uri uri, String whereClause, String[] whereArgs) {
		final long id = Long.parseLong(uri.getLastPathSegment());
		final UpdateParams updateParams = new UpdateParams();
		updateParams.uri = uri;
		updateParams.whereClause = StringUtil.append(whereClause, BaseColumns._ID + " = " + id, " AND ");
		updateParams.whereArgs = whereArgs;
		return updateParams;
	}

	@Override
	public DeleteParams getDeleteParams(Uri uri, String whereClause, String[] whereArgs) {
		final long id = Long.parseLong(uri.getLastPathSegment());
		final DeleteParams deleteParams = new DeleteParams();
		deleteParams.uri = uri;
		deleteParams.whereClause = StringUtil.append(whereClause, BaseColumns._ID + " = " + id, " AND ");
		deleteParams.whereArgs = whereArgs;
		return deleteParams;
	}
}
