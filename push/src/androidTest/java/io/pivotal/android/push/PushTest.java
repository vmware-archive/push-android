package io.pivotal.android.push;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.pivotal.android.push.prefs.Pivotal;
import io.pivotal.android.push.registration.RegistrationEngine;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class PushTest {
    private Context context;
    private Push push;
    private String deviceAlias;
    private Set<String> tags;
    private PushServiceInfo pushServiceInfo;

    @Before
    public void setup() {
        context = mock(Context.class);
        push = Push.getInstance(context);
        deviceAlias = "some-device-alias";
        tags = new HashSet<String>() {{
            add("tag1");
            add("tag2");
        }};

        List<String> certificateNames = new ArrayList<String>(){{
            add("cert1");
            add("cert2");
        }};
        pushServiceInfo = PushServiceInfo.Builder()
                .setServiceUrl("https://some-serviceurl.com")
                .setPlatformUuid("some-platform-uuid")
                .setPlatformSecret("some-platform-secret")
                .setSSLCertValidationMode(Pivotal.SslCertValidationMode.TRUST_ALL)
                .setPinnedCertificateNames(certificateNames)
                .setAnalyticsEnabled(false)
                .build();
    }

    @Test
    public void getInstance_returnsSameInstance() throws Exception {
        Push anotherPush = Push.getInstance(context);

        assertSame(push, anotherPush);
    }

    @Test(expected = IllegalArgumentException.class)
    public void startRegistration_throwWithNoServiceInfo() throws Exception {
        push.startRegistration(deviceAlias, tags, false);
    }
}