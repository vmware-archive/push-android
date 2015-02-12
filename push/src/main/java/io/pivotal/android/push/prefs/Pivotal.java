/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.prefs;

import android.text.TextUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Pivotal {

    private static final class Keys {
        private static final String SERVICE_URL = "pivotal.push.serviceUrl";
        private static final String GCM_SENDER_ID = "pivotal.push.gcmSenderId";
        private static final String PLATFORM_UUID = "pivotal.push.platformUuid";
        private static final String PLATFORM_SECRET = "pivotal.push.platformSecret";
    }

    private static final String[] LOCATIONS = {
        "assets/pivotal.properties", "res/raw/pivotal.properties"
    };

    private static Properties sProperties;

    /* package */ static Properties getProperties() {
        if (sProperties == null) {
            sProperties = loadProperties();
        }
        return sProperties;
    }

    /* package */ static void setProperties(final Properties properties) {
        sProperties = properties;
    }

    private static Properties loadProperties() {
        for (final String path : LOCATIONS) {
            try {
                return loadProperties(path);
            } catch (final Exception e) {
                // Swallow exception
            }
        }
        throw new IllegalStateException("Could not find pivotal.properties file.");
    }

    private static Properties loadProperties(final String path) throws IOException {
        final Properties properties = new Properties();
        properties.load(getInputStream(path));
        return properties;
    }

    private static InputStream getInputStream(final String path) {
        final Thread currentThread = Thread.currentThread();
        final ClassLoader loader = currentThread.getContextClassLoader();
        return loader.getResourceAsStream(path);
    }

    /* package */ static String get(final String key) {
        final String value = getProperties().getProperty(key);
        if (TextUtils.isEmpty(value)) {
            throw new IllegalStateException("'" + key + "' not found in pivotal.properties");
        }
        return value;
    }

    public static String getPlatformUuid() {
        return get(Keys.PLATFORM_UUID);
    }

    public static String getPlatformSecret() {
        return get(Keys.PLATFORM_SECRET);
    }

    public static String getGcmSenderId() {
        return get(Keys.GCM_SENDER_ID);
    }

    public static String getServiceUrl() {
        return get(Keys.SERVICE_URL);
    }
}
