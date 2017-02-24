package io.pivotal.android.push.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

public class PushPreferencesBaidu extends PushPreferences {

    private static final String PROPERTY_BAIDU_CHANNEL_ID = "baidu_channel_id";
    private OnBaiduChannelIdChangedListener onBaiduChannelIdChangedListener;

    public PushPreferencesBaidu(Context context) {
        super(context);
    }

    public String getBaiduChannelId() {
        return getSharedPreferences().getString(PROPERTY_BAIDU_CHANNEL_ID, null);
    }

    public void setBaiduChannelId(String baiduChannelId) {
        final SharedPreferences prefs = getSharedPreferences();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_BAIDU_CHANNEL_ID, baiduChannelId);
        editor.commit();
    }

    public interface OnBaiduChannelIdChangedListener {
        void onChannelIdChanged();
    }

    public void registerOnBaiduChannelIdChangedListener(final OnBaiduChannelIdChangedListener listener) {
        onBaiduChannelIdChangedListener = listener;
        getSharedPreferences().registerOnSharedPreferenceChangeListener(getOnSharedPreferenceChangeListener());
    }

    OnSharedPreferenceChangeListener getOnSharedPreferenceChangeListener() {
        return new OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals(PROPERTY_BAIDU_CHANNEL_ID)) {
                    if (onBaiduChannelIdChangedListener != null) {
                        onBaiduChannelIdChangedListener.onChannelIdChanged();
                    }
                }
            }
        };
    }
}
