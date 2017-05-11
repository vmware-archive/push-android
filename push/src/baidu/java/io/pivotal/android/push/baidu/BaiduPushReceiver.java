package io.pivotal.android.push.baidu;

import android.content.Context;
import com.baidu.android.pushservice.PushMessageReceiver;
import io.pivotal.android.push.Push;
import io.pivotal.android.push.prefs.PushPreferencesBaidu;
import io.pivotal.android.push.util.Logger;
import java.util.List;

public class BaiduPushReceiver extends PushMessageReceiver {

    @Override
    public void onBind(Context context, int errorCode, String appId, String userId, String channelId, String requestId) {
        Logger.d(String.format("onBind: errorCode: %s, appId: %s, userId: %s, channelId:%s, requestId:%s", errorCode, appId, userId, channelId, requestId));
        Push.getInstance(context).onBaiduServiceBound(errorCode, channelId); // also the channel id

        final PushPreferencesBaidu pushPreferences = new PushPreferencesBaidu(context); // not here
        pushPreferences.setBaiduChannelId(channelId);
    }

    @Override
    public void onUnbind(Context context, int errorCode, String requestId) {
        Logger.d(String.format("onUnbind: errorCode: %s, requestId:%s", errorCode, requestId));
    }

    @Override
    public void onSetTags(Context context, int errorCode, List<String> successTags, List<String> failTags, String requestId) {
        Logger.d(String.format("onSetTags - errorCode: %s, successTags: %s, failTags: %s, requestId: %s", errorCode, successTags, failTags, requestId));
    }

    @Override
    public void onDelTags(Context context, int errorCode, List<String> successTags, List<String> failTags, String requestId) {

    }

    @Override
    public void onListTags(Context context, int errorCode, List<String> tags, String requestId) {
        Logger.d(String.format("onListTags - errorCode: %s, tags: %s, requestId: %s", errorCode, tags, requestId));
    }

    @Override
    public void onMessage(Context context, String message, String customContentString) {
        Logger.d("onMessage: message: " + message + ", custom content: " + customContentString);
    }

    @Override
    public void onNotificationClicked(Context context, String title, String description, String customContentString) {
        Logger.d(String.format("onNotificationClicked - title: %s, description: %s, customContentString: %s", title, description, customContentString));
    }

    @Override
    public void onNotificationArrived(Context context, String title, String description, String customContentString) {
        Logger.d("onNotificationArrived - title: " + title + ", description: " + description + ", customContentString: " + customContentString);
    }
}
