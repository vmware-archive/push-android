package io.pivotal.android.push.prefs;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import io.pivotal.android.push.prefs.PushPreferencesBaidu.OnBaiduChannelIdChangedListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class PushPreferencesBaiduTest {

    private PushPreferencesBaidu pushPreferences;
    private OnBaiduChannelIdChangedListener channelChangeListener;

    @Before
    public void setUp() throws Exception {
        pushPreferences = new PushPreferencesBaidu(InstrumentationRegistry.getContext());
        pushPreferences.clear();

        channelChangeListener = mock(OnBaiduChannelIdChangedListener.class);
    }

    @After
    public void tearDown() throws Exception {
        if (pushPreferences != null) {
            pushPreferences.clear();
        }
    }

    @Test
    public void updatePreference_baiduChannelId_notifiesListener() throws Exception {
        pushPreferences.registerOnBaiduChannelIdChangedListener(channelChangeListener);
        pushPreferences.setBaiduChannelId("channelId");

        verify(channelChangeListener, timeout(1000)).onChannelIdChanged();
    }

    @Test
    public void updatePreference_baiduChannelId_handlesNullListener() {
        pushPreferences.registerOnBaiduChannelIdChangedListener(null);

        pushPreferences.setBaiduChannelId("channelId");
    }

    @Test
    public void updatePreference_otherPreferencesChange_shouldNotCallListener() {
        pushPreferences.registerOnBaiduChannelIdChangedListener(channelChangeListener);

        pushPreferences.setServiceUrl("some url");
        verify(channelChangeListener, after(1000).never()).onChannelIdChanged();
    }

    @Test
    public void setsAndGetsBaiduChannelId() {
        pushPreferences.setBaiduChannelId("some id");
        assertEquals("some id", pushPreferences.getBaiduChannelId());
    }

    @Test public void persisteBaiduChannelId() {
        pushPreferences.setBaiduChannelId("some id");

        PushPreferencesBaidu otherPreferences = new PushPreferencesBaidu(InstrumentationRegistry.getContext());
        assertEquals("some id", otherPreferences.getBaiduChannelId());
    }
}