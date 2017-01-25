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
        Pivotal.setProperties(null);
    }

    public void testAreAnalyticsEnabled1() {
        final Properties properties = new Properties();
        properties.put("areAnalyticsEnabled", "false");
        Pivotal.setProperties(properties);

        assertFalse(preferences.areAnalyticsEnabled());
    }

    public void testAreAnalyticsEnabled2() {
        final Properties properties = new Properties();
        properties.put("areAnalyticsEnabled", "true");
        Pivotal.setProperties(properties);

        preferences.setBackEndVersion(null);
        assertFalse(preferences.areAnalyticsEnabled());
    }

    public void testAreAnalyticsEnabled3() {
        final Properties properties = new Properties();
        properties.put("areAnalyticsEnabled", "true");
        Pivotal.setProperties(properties);

        preferences.setBackEndVersion(new Version("1.3.0"));
        assertFalse(preferences.areAnalyticsEnabled());
    }

    public void testAreAnalyticsEnabled4() {
        final Properties properties = new Properties();
        properties.put("areAnalyticsEnabled", "true");
        Pivotal.setProperties(properties);

        preferences.setBackEndVersion(new Version("1.3.2"));
        assertTrue(preferences.areAnalyticsEnabled());
    }

    public void testAreAnalyticsEnabled5() {
        final Properties properties = new Properties();
        properties.put("areAnalyticsEnabled", "true");
        Pivotal.setProperties(properties);

        preferences.setBackEndVersion(new Version("1.3.3"));
        assertTrue(preferences.areAnalyticsEnabled());
    }

}
