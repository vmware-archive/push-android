package io.pivotal.android.push.prefs;

import android.test.AndroidTestCase;

import java.util.Properties;

import io.pivotal.android.push.version.Version;

public class PushPreferencesProviderImplTest extends AndroidTestCase {

    private PushPreferencesProviderImpl preferences;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        preferences = new PushPreferencesProviderImpl(getContext());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        preferences.clear();
    }

    public void testAreAnalyticsEnabled1() {
        preferences.setAreAnalyticsEnabled(false);
        assertFalse(preferences.areAnalyticsEnabled());
    }
}
