package io.pivotal.android.push;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.pivotal.android.push.prefs.Pivotal;

import static org.junit.Assert.*;

public class PushParametersTest {
    private String platformUuid = "platform-uuid";
    private String platformSecret = "platform-secret";
    private String serviceUrl = "service-url";
    private String deviceAlias = "device-alias";
    private String customUserId = "custom-user-id";
    private Set<String> tags;
    private boolean areGeofencesEnabled;
    private boolean areAnalyticsEnabled;
    private Pivotal.SslCertValidationMode certMode = Pivotal.SslCertValidationMode.TRUST_ALL;
    private List<String> pinnedCerts = new ArrayList<>();
    private Map<String, String> requestHeaders = new HashMap<>();

    private PushParameters parameters;
    @Before
    public void setUp() throws Exception {
        tags = new HashSet<>();
        tags.add("tag1");
        tags.add("tag2");

        platformUuid = "platform-uuid";
        platformSecret = "platform-secret";
        serviceUrl = "service-url";
        deviceAlias = "device-alias";
        customUserId = "custom-user-id";

        areGeofencesEnabled = false;
        areAnalyticsEnabled = true;
        certMode = Pivotal.SslCertValidationMode.TRUST_ALL;
        pinnedCerts = new ArrayList<>();
        requestHeaders = new HashMap<>();

    }

    private void initializeParameters() {
        parameters = new PushParameters(
                platformUuid,
                platformSecret,
                serviceUrl,
            "android-baidu", deviceAlias,
                customUserId,
                tags,
                areGeofencesEnabled,
                areAnalyticsEnabled,
                certMode,
                pinnedCerts,
                requestHeaders);
    }

    @Test
    public void initialization() throws Exception {
        initializeParameters();

        assertEquals(platformUuid, parameters.getPlatformUuid());
        assertEquals(platformSecret, parameters.getPlatformSecret());
        assertEquals(serviceUrl, parameters.getServiceUrl());
        assertEquals(deviceAlias, parameters.getDeviceAlias());
        assertEquals(customUserId, parameters.getCustomUserId());
        assertEquals(tags, parameters.getTags());
        assertEquals(areGeofencesEnabled, parameters.areGeofencesEnabled());
        assertEquals(areAnalyticsEnabled, parameters.areAnalyticsEnabled());
        assertEquals(certMode, parameters.getSslCertValidationMode());
        assertEquals(pinnedCerts, parameters.getPinnedSslCertificateNames());
        assertEquals(requestHeaders, parameters.getRequestHeaders());
    }

    @Test
    public void deviceAlias_nullWhenEmpty() throws Exception {
        deviceAlias = "   ";
        initializeParameters();
        assertNull(parameters.getDeviceAlias());
    }

    @Test
    public void customUserId_nullWhenEmpty() throws Exception {
        customUserId = "   ";
        initializeParameters();
        assertNull(parameters.getCustomUserId());
    }

    @Test
    public void tags_transformsToLowercase() throws Exception {
        tags = new HashSet<>();
        tags.add("TAGS1");
        tags.add("taGs2");
        initializeParameters();

        final Set<String> expectedTags = new HashSet<>();
        expectedTags.add("tags1");
        expectedTags.add("tags2");

        assertEquals(expectedTags, parameters.getTags());
    }
}