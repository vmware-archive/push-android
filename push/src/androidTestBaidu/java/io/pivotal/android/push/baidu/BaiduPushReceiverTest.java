package io.pivotal.android.push.baidu;

import static org.junit.Assert.assertEquals;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import io.pivotal.android.push.prefs.PushPreferencesBaidu;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class BaiduPushReceiverTest {

  private BaiduPushReceiver pushReceiver;


  @Before
  public void setUp() throws Exception {
    pushReceiver = new BaiduPushReceiver();

  }

  @Test
  public void pushReceiver_onBind_savesTheChannelIdToSharedPreferences() throws Exception {
    pushReceiver.onBind(InstrumentationRegistry.getContext(), 0, "some app ID", "some user ID", "some channel ID", "some request ID");

    final PushPreferencesBaidu pushPreferences = new PushPreferencesBaidu(InstrumentationRegistry.getContext());

    assertEquals("some channel ID", pushPreferences.getBaiduChannelId());
  }
}