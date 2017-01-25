/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.prefs;

import android.content.Context;
import android.text.TextUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Pivotal {

    public static final class Keys {
        public static final String SERVICE_URL = "pivotal.push.serviceUrl";
        public static final String PLATFORM_UUID = "pivotal.push.platformUuid";
        public static final String PLATFORM_SECRET = "pivotal.push.platformSecret";
        public static final String SSL_CERT_VALIDATION_MODE = "pivotal.push.sslCertValidationMode";
        public static final String PINNED_SSL_CERTIFICATE_NAMES = "pivotal.push.pinnedSslCertificateNames";
        public static final String ARE_ANALYTICS_ENABLED = "pivotal.push.areAnalyticsEnabled";
        static final String PERSIST_REQUEST_HEADERS = "pivotal.push.persistRequestHeaders";
    }

    public enum SslCertValidationMode {
        DEFAULT,
        TRUST_ALL,
        PINNED,
        CALLBACK
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

    /* package */ static List<String> getOptionalListProperty(Context context, String key, String defaultValue) {
        final String property = getProperties(context).getProperty(key, defaultValue);
        if (property == null || property.isEmpty()) {
            return null;
        }

        final String[] properties = property.split("\\s+");
        final List<String> result = new ArrayList<>(properties.length);
        for (String p : properties) {
            String s = p.trim();
            if (!s.isEmpty()) {
                result.add(s);
            }
        }

        return result;
    }

    public static String getPlatformUuid(Context context) {
        return getRequiredProperty(context, Keys.PLATFORM_UUID);
    }

    public static String getPlatformSecret(Context context) {
        return getRequiredProperty(context, Keys.PLATFORM_SECRET);
    }

    public static String getServiceUrl(Context context) {
        return getRequiredProperty(context, Keys.SERVICE_URL);
    }

    public static SslCertValidationMode getSslCertValidationMode(Context context) throws IllegalArgumentException {
        final String s = getOptionalProperty(context, Keys.SSL_CERT_VALIDATION_MODE, "default");

        if (s.equalsIgnoreCase("trustall") || s.equalsIgnoreCase("trust_all")) {
            return SslCertValidationMode.TRUST_ALL;

        } else if (s.equalsIgnoreCase("pinned")) {
            return SslCertValidationMode.PINNED;

        } else if (s.equalsIgnoreCase("callback")) {
            return SslCertValidationMode.CALLBACK;

        } else if (s.equalsIgnoreCase("default") || s.isEmpty()) {
            return SslCertValidationMode.DEFAULT;
        }

        throw new IllegalArgumentException("Invalid pivotal.push.sslCertValidationMode '" + s + "'");
    }

    public static List<String> getPinnedSslCertificateNames(Context context) {
        return getOptionalListProperty(context, Keys.PINNED_SSL_CERTIFICATE_NAMES, null);
    }

    public static boolean getAreAnalyticsEnabled(Context context) {
        return Boolean.parseBoolean(getOptionalProperty(context, Keys.ARE_ANALYTICS_ENABLED, "true"));
    }

    public static boolean getPersistRequestHeaders(Context context) {
        return Boolean.parseBoolean(getOptionalProperty(context, Keys.PERSIST_REQUEST_HEADERS, "true"));
    }
}
