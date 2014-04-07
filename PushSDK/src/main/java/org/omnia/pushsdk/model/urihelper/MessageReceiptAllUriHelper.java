package org.omnia.pushsdk.model.urihelper;

import android.net.Uri;

import org.omnia.pushsdk.database.DatabaseConstants;
import org.omnia.pushsdk.database.urihelpers.DeleteParams;
import org.omnia.pushsdk.database.urihelpers.QueryParams;
import org.omnia.pushsdk.database.urihelpers.UpdateParams;
import org.omnia.pushsdk.database.urihelpers.UriHelper;
import org.omnia.pushsdk.database.urihelpers.UriMatcherParams;

public class MessageReceiptAllUriHelper implements UriHelper {

	@Override
	public String getType() {
		return "vnd.android.cursor.dir/vnd.org.omnia.pushsdk.message_receipts";
	}

	@Override
	public String getDefaultTableName() {
		return DatabaseConstants.MESSAGE_RECEIPTS_TABLE_NAME;
	}
	
	@Override
	public UriMatcherParams getUriMatcherParams() {
		final UriMatcherParams uriMatcherParams = new UriMatcherParams();
		uriMatcherParams.authority = DatabaseConstants.AUTHORITY;
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
