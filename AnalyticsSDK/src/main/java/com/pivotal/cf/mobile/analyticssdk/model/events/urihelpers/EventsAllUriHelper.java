package com.pivotal.cf.mobile.analyticssdk.model.events.urihelpers;

import android.net.Uri;

import com.pivotal.cf.mobile.analyticssdk.database.Database;
import com.pivotal.cf.mobile.analyticssdk.database.urihelpers.DeleteParams;
import com.pivotal.cf.mobile.analyticssdk.database.urihelpers.QueryParams;
import com.pivotal.cf.mobile.analyticssdk.database.urihelpers.UpdateParams;
import com.pivotal.cf.mobile.analyticssdk.database.urihelpers.UriHelper;
import com.pivotal.cf.mobile.analyticssdk.database.urihelpers.UriMatcherParams;

public class EventsAllUriHelper implements UriHelper {

	@Override
	public String getType() {
		return "vnd.android.cursor.dir/vnd.com.pivotal.cf.mobile.analyticssdk.events";
	}

	@Override
	public String getDefaultTableName() {
		return Database.EVENTS_TABLE_NAME;
	}
	
	@Override
	public UriMatcherParams getUriMatcherParams() {
		final UriMatcherParams uriMatcherParams = new UriMatcherParams();
		uriMatcherParams.authority = Database.AUTHORITY;
		uriMatcherParams.path = getDefaultTableName();
		return uriMatcherParams;
	}

	@Override
	public QueryParams getQueryParams(final Uri uri, final String[] projection, final String whereClause, final String[] whereArgs, final String sortOrder) {
		final QueryParams queryParams = new QueryParams();
		queryParams.uri = uri;
		queryParams.projection = projection;
		queryParams.whereClause = whereClause;
		queryParams.whereArgs = whereArgs;
		queryParams.sortOrder = sortOrder;
		return queryParams;
	}

	@Override
	public UpdateParams getUpdateParams(Uri uri, String whereClause, String[] whereArgs) {
		final UpdateParams updateParams = new UpdateParams();
		updateParams.uri = uri;
		updateParams.whereClause = whereClause;
		updateParams.whereArgs = whereArgs;
		return updateParams;
	}

	@Override
	public DeleteParams getDeleteParams(Uri uri, String whereClause, String[] whereArgs) {
		final DeleteParams deleteParams = new DeleteParams();
		deleteParams.uri = uri;
		deleteParams.whereClause = whereClause;
		deleteParams.whereArgs = whereArgs;
		return deleteParams;
	}
}
