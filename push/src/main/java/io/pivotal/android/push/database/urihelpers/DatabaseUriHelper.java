package io.pivotal.android.push.database.urihelpers;

import android.net.Uri;

import java.util.Set;

import io.pivotal.android.push.model.analytics.urihelpers.EventsAllUriHelper;
import io.pivotal.android.push.model.analytics.urihelpers.EventsUriHelper;

public class DatabaseUriHelper {

    private static final class UriMatches {
        public static final int EVENTS_ALL = 0;
        public static final int EVENTS = 1;
    }

    private static UriHelperFactory uriHelperFactory;

    static {
        uriHelperFactory = new UriHelperFactory();
        uriHelperFactory.addUriHelper(new EventsAllUriHelper(), UriMatches.EVENTS_ALL);
        uriHelperFactory.addUriHelper(new EventsUriHelper(), UriMatches.EVENTS);
    }

    public static UriHelper getUriHelper(final Uri uri) {
        return uriHelperFactory.getUriHelper(uri);
    }

    public static Set<String> getAllTableNames() {
        return uriHelperFactory.getAllTableNames();
    }
}
