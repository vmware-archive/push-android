package com.pivotal.cf.mobile.pushsdk.simpledemoapp.broadcastreceiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.pivotal.cf.mobile.pushsdk.util.PushLibLogger;

import com.pivotal.cf.mobile.pushsdk.simpledemoapp.MainActivity;

/*
 * This BroadcastReceiver is called by the Pivotal CF Mobile Services Push SDK whenever it receives
 * a push message from GCM targeted at your application.  The original intent received by the PushLib
 * is in the "gcm_intent" parcelable extra in the intent passed to the `onReceive` method.  The fields
 * passed in the push message itself are in the extras of the Intent in the `gcm_intent` extra.
 */
public class MyPivotalCFMSRemotePushLibBroadcastReceiver extends WakefulBroadcastReceiver {

    public static final int NOTIFICATION_ID = 1;

    private static final int NOTIFICATION_LIGHTS_COLOUR = 0xff55aaaa;
    private static final int NOTIFICATION_LIGHTS_ON_MS = 500;
    private static final int NOTIFICATION_LIGHTS_OFF_MS = 1000;

    @Override
    public void onReceive(Context context, Intent intent) {

        final Intent gcmIntent = intent.getExtras().getParcelable("gcm_intent");
        final Bundle extras = gcmIntent.getExtras();
        final GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
        final String messageType = gcm.getMessageType(gcmIntent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                PushLibLogger.i("Received message with type 'MESSAGE_TYPE_SEND_ERROR'.");
                sendNotification(context, "Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                PushLibLogger.i("Received message with type 'MESSAGE_TYPE_DELETED'.");
                sendNotification(context, "Deleted messages on server: " + extras.toString());

            // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // Post notification of received message.
                String message;
                if (extras.containsKey("message")) {
                    message = "Received: \"" + extras.getString("message") + "\".";
                } else {
                    message = "Received message with no extras.";
                }
                PushLibLogger.i(message);
                sendNotification(context, message);
            }
        } else {
            PushLibLogger.i("Received message with no content.");
        }

        MyPivotalCFMSRemotePushLibBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    private void sendNotification(Context context, String msg) {
        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Set up the notification to open MainActivity when the user touches it
        final PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);

        final NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(com.pivotal.cf.mobile.pushsdk.simpledemoapp.R.drawable.ic_launcher)
                        .setContentTitle("Pivotal CF MS Push Simple Demo App")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                        .setContentText(msg);

        builder.setContentIntent(contentIntent);
        builder.setLights(NOTIFICATION_LIGHTS_COLOUR, NOTIFICATION_LIGHTS_ON_MS, NOTIFICATION_LIGHTS_OFF_MS);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
