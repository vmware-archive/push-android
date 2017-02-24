package io.pivotal.android.push;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;
import io.pivotal.android.push.prefs.Pivotal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

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