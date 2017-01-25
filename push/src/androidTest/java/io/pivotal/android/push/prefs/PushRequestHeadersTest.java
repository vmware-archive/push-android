package io.pivotal.android.push.prefs;


import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Properties;

import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class PushRequestHeadersTest {

    @Test
    public void testPushRequestHeadersWithPersistRequestHeadersEnabled() {
        final Properties properties = new Properties();
        properties.setProperty(Pivotal.Keys.PERSIST_REQUEST_HEADERS, "true");
        Pivotal.setProperties(properties);

        PushRequestHeaders pushRequestHeaders = PushRequestHeaders.getInstance(InstrumentationRegistry.getContext());

        assertTrue(pushRequestHeaders instanceof PersistedPushRequestHeaders);
    }

    @Test
    public void testPushRequestHeadersWithPersistRequestHeadersDisabled() {
        final Properties properties = new Properties();
        properties.setProperty(Pivotal.Keys.PERSIST_REQUEST_HEADERS, "false");
        Pivotal.setProperties(properties);

        PushRequestHeaders pushRequestHeaders = PushRequestHeaders.getInstance(InstrumentationRegistry.getContext());

        assertTrue(pushRequestHeaders instanceof InMemoryPushRequestHeaders);
    }
}