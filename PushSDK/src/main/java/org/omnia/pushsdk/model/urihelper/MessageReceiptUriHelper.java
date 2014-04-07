package org.omnia.pushsdk.model.urihelper;

import android.net.Uri;
import android.provider.BaseColumns;

import org.omnia.pushsdk.database.DatabaseConstants;
import org.omnia.pushsdk.database.urihelpers.DeleteParams;
import org.omnia.pushsdk.database.urihelpers.QueryParams;
import org.omnia.pushsdk.database.urihelpers.UpdateParams;
import org.omnia.pushsdk.database.urihelpers.UriHelper;
import org.omnia.pushsdk.database.urihelpers.UriMatcherParams;
import org.omnia.pushsdk.util.StringUtil;

public class MessageReceiptUriHelper implements UriHelper {

	@Override
	public String getType() {
		return "vnd.android.cursor.item/vnd.org.omnia.pushsdk.message_receipts";
	}

	@Override
	public String getDefaultTableName() {
		return DatabaseConstants.MESSAGE_RECEIPTS_TABLE_NAME;
	}
	
	@Override
	public UriMatcherParams getUriMatcherParams() {
		final UriMatcherParams uriMatcherParams = new UriMatcherParams();
		uriMatcherParams.authority = DatabaseConstants.AUTHORITY;
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
