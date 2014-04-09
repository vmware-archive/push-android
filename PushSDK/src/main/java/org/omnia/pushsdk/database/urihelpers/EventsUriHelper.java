package org.omnia.pushsdk.database.urihelpers;

import android.net.Uri;

import org.omnia.pushsdk.model.urihelper.MessageReceiptAllUriHelper;
import org.omnia.pushsdk.model.urihelper.MessageReceiptUriHelper;

import java.util.Set;

public class EventsUriHelper {

    private static final class UriMatches {
        public static final int MESSAGE_RECEIPTS_ALL = 0;
        public static final int MESSAGE_RECEIPTS = 1;
    }

    private static UriHelperFactory uriHelperFactory;

    static {
        uriHelperFactory = new UriHelperFactory();
        uriHelperFactory.addUriHelper(new MessageReceiptAllUriHelper(), UriMatches.MESSAGE_RECEIPTS_ALL);
        uriHelperFactory.addUriHelper(new MessageReceiptUriHelper(), UriMatches.MESSAGE_RECEIPTS);
    }

    public static UriHelper getUriHelper(final Uri uri) {
        return uriHelperFactory.getUriHelper(uri);
    }

    public static Set<String> getAllTableNames() {
        return uriHelperFactory.getAllTableNames();
    }
}
