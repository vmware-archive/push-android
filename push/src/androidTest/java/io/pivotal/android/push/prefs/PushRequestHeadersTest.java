package io.pivotal.android.push.prefs;


import android.test.AndroidTestCase;

import java.util.Properties;

public class PushRequestHeadersTest extends AndroidTestCase {

    public void testPushRequestHeadersWithPersistRequestHeadersEnabled() {
        final Properties properties = new Properties();
        properties.setProperty(Pivotal.Keys.PERSIST_REQUEST_HEADERS, "true");
        Pivotal.setProperties(properties);

        PushRequestHeaders pushRequestHeaders = PushRequestHeaders.getInstance(getContext());

        assertTrue(pushRequestHeaders instanceof PersistedPushRequestHeaders);
    }

    public void testPushRequestHeadersWithPersistRequestHeadersDisabled() {
        final Properties properties = new Properties();
        properties.setProperty(Pivotal.Keys.PERSIST_REQUEST_HEADERS, "false");
        Pivotal.setProperties(properties);

        PushRequestHeaders pushRequestHeaders = PushRequestHeaders.getInstance(getContext());

        assertTrue(pushRequestHeaders instanceof InMemoryPushRequestHeaders);
    }
}