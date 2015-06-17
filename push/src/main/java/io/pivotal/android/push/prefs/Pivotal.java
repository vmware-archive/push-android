/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.prefs;

import android.content.Context;
import android.text.TextUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Pivotal {

    public static final class Keys {
        public static final String SERVICE_URL = "pivotal.push.serviceUrl";
        public static final String GCM_SENDER_ID = "pivotal.push.gcmSenderId";
        public static final String PLATFORM_UUID = "pivotal.push.platformUuid";
        public static final String PLATFORM_SECRET = "pivotal.push.platformSecret";
        public static final String GEOFENCES_ENABLED = "pivotal.push.geofencesEnabled";
        public static final String TRUST_ALL_SSL_CERTIFICATES = "pivotal.push.trustAllSslCertificates";
    }

    private static final String[] LOCATIONS = {
        "assets/pivotal.properties", "res/raw/pivotal.properties"
    };

    private static Properties sProperties;

    /* package */ static Properties getProperties(Context context) {
        if (sProperties == null) {
            sProperties = loadProperties(context);
        }
        return sProperties;
    }

    public static void setProperties(final Properties properties) {
        sProperties = properties;
    }

    private static Properties loadProperties(Context context) {
        Exception thrownException = null;
        for (final String path : LOCATIONS) {
            try {
                return loadProperties(context, path);
            } catch (final Exception e) {
                thrownException = e;
            }
        }
        if (thrownException == null) {
            throw new IllegalStateException("Could not find pivotal.properties file.");
        } else {
            throw new IllegalStateException("Could not find pivotal.properties file. " + thrownException.getLocalizedMessage(), thrownException);
        }
    }

    private static Properties loadProperties(Context context, final String path) throws IOException {
        final Properties properties = new Properties();
        final InputStream inputStream = getInputStream(context, path);
        properties.load(inputStream);
        return properties;
    }

    private static InputStream getInputStream(Context context, final String path) throws IOException {
        final ClassLoader loader = context.getClassLoader();
        return loader.getResourceAsStream(path);
    }

    /* package */ static String getRequiredProperty(Context context, final String key) {
        final String value = getProperties(context).getProperty(key);
        if (TextUtils.isEmpty(value)) {
            throw new IllegalStateException("'" + key + "' not found in pivotal.properties");
        }
        return value;
    }

    /* package */ static String getOptionalProperty(Context context, final String key, String defaultValue) {
        return getProperties(context).getProperty(key, defaultValue);
    }

    public static String getPlatformUuid(Context context) {
        return getRequiredProperty(context, Keys.PLATFORM_UUID);
    }

    public static String getPlatformSecret(Context context) {
        return getRequiredProperty(context, Keys.PLATFORM_SECRET);
    }

    public static String getGcmSenderId(Context context) {
        return getRequiredProperty(context, Keys.GCM_SENDER_ID);
    }

    public static String getServiceUrl(Context context) {
        return getRequiredProperty(context, Keys.SERVICE_URL);
    }

    public static boolean getGeofencesEnabled(Context context) {
        return Boolean.parseBoolean(getOptionalProperty(context, Keys.GEOFENCES_ENABLED, "false"));
    }

    public static boolean isTrustAllSslCertificates(Context context) {
        return Boolean.parseBoolean(getOptionalProperty(context, Keys.TRUST_ALL_SSL_CERTIFICATES, "false"));
    }
}
