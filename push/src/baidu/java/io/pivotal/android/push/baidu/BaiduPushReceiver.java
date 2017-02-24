package io.pivotal.android.push.baidu;

import android.content.Context;
import android.util.Log;
import com.baidu.android.pushservice.PushMessageReceiver;
import io.pivotal.android.push.BaiduPush;
import io.pivotal.android.push.prefs.PushPreferencesBaidu;
import java.util.List;

public class BaiduPushReceiver extends PushMessageReceiver {

    @Override
    public void onBind(Context context, int errorCode, String appId, String userId, String channelId, String requestId) {
        Log.d("PCFPush", String.format("onBind: errorCode: %s, appId: %s, userId: %s, channelId:%s, requestId:%s", errorCode, appId, userId, channelId, requestId));
        BaiduPush.getInstance(context).onBaiduServiceBound(errorCode);

        final PushPreferencesBaidu pushPreferences = new PushPreferencesBaidu(context);
        pushPreferences.setBaiduChannelId(channelId);
    }

    @Override
    public void onUnbind(Context context, int errorCode, String requestId) {
        Log.d("PCFPush", String.format("onBind: errorCode: %s, requestId:%s", errorCode, requestId));
    }

    @Override
    public void onSetTags(Context context, int errorCode, List<String> successTags, List<String> failTags, String requestId) {
        Log.d("PCFPush", String.format("onSetTags - errorCode: %s, successTags: %s, failTags: %s, requestId: %s", errorCode, successTags, failTags, requestId));
    }

    @Override
    public void onDelTags(Context context, int errorCode, List<String> successTags, List<String> failTags, String requestId) {

    }

    @Override
    public void onListTags(Context context, int errorCode, List<String> tags, String requestId) {
        Log.d("PCFPush", String.format("onListTags - errorCode: %s, tags: %s, requestId: %s", errorCode, tags, requestId));
    }

    @Override
    public void onMessage(Context context, String message, String customContentString) {
        Log.d("PCFPush", "onMessage: message: " + message + ", custom content: " + customContentString);
    }

    @Override
    public void onNotificationClicked(Context context, String title, String description, String customContentString) {
        Log.d("PCFPush", String.format("onNotificationClicked - title: %s, description: %s, customContentString: %s", title, description, customContentString));
    }

    @Override
    public void onNotificationArrived(Context context, String title, String description, String customContentString) {
        Log.d("PCFPush", "onNotificationArrived: message: " + title);
    }
}
