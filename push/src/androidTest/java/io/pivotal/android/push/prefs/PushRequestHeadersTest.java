package io.pivotal.android.push.prefs;


import static junit.framework.Assert.assertTrue;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class PushRequestHeadersTest {

    @After
    public void tearDown() {
        // Reset settings back to default values
        PushRequestHeaders.setPersistRequestHeadersToDisk(true);
    }

    @Test
    public void testPushRequestHeadersUsingDefaultSettings() {
        PushRequestHeaders pushRequestHeaders = PushRequestHeaders
                .getInstance(InstrumentationRegistry.getContext());

        assertTrue(pushRequestHeaders instanceof PersistedPushRequestHeaders);
    }

    @Test
    public void testPushRequestHeadersWithPersistRequestHeadersEnabled() {
        PushRequestHeaders.setPersistRequestHeadersToDisk(true);

        PushRequestHeaders pushRequestHeaders = PushRequestHeaders.getInstance(InstrumentationRegistry.getContext());

        assertTrue(pushRequestHeaders instanceof PersistedPushRequestHeaders);
    }

    @Test
    public void testPushRequestHeadersWithPersistRequestHeadersDisabled() {
        PushRequestHeaders.setPersistRequestHeadersToDisk(false);

        PushRequestHeaders pushRequestHeaders = PushRequestHeaders.getInstance(InstrumentationRegistry.getContext());

        assertTrue(pushRequestHeaders instanceof InMemoryPushRequestHeaders);
    }
}