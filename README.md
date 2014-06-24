Pivotal Mobile Services Suite Push and Analytics Client SDKs for Android
========================================================================

Features
--------

The Pivotal Mobile Services Suite Push and Analytics Client SDKs are light-weight tools that will help your application:

 1. Register for push notifications with Google Cloud Messaging (GCM) and an instance of the Pivotal Mobile Services Suite
    push messaging server.
 2. Receive push messages sent via the same frameworks.
 3. Capture basic analytics regarding push messages and your application life cycle.

Although the Push Client SDK does depend on the Analytics Client SDK, you are not forced to use our analytics engine.

Device Requirements
-------------------

The Push SDK requires API level 10 or greater.

The Google Play Services application must be installed on the device before you can register your device or receive
push messages.  Most devices should already have this application installed, but some odd ones may not.  By default,
Android Virtual Devices (i.e.: emulators) do not have Google Play Services installed.

Instructions for Integrating the Pivotal Mobile Services Suite Push and Analytics Client SDKs for Android
---------------------------------------------------------------------------------------------------------

In order to receive push messages from Pivotal Mobile Services Suite in your Android application you will need to follow
these tasks:

 1. Set up a project on Google Cloud Console.  Follow the instructions here:

      http://developer.android.com/google/gcm/gs.html

    You will need obtain the Project Number (AKA the "Sender ID") and register a "Web Application".  The Project
    Number is a parameter you most provide to the Push Client SDK when registering your device at run-time and to the
	Pivotal Mobile Services Suite console.  The Web Application on Google Cloud Console includes an "API Key" that you
	must supply to the Pivotal Mobile Services Suite administration console when creating your variant.

 2. Set up your application, environment, and a variant on the Pivotal Mobile Services Suite administration console.
    This task is beyond the scope of this document, but please note that you will need the two parameters from Google
    Cloud Console above.  After setting up your variant in Pivotal Mobile Services Suite, make sure to note the
    Variant UUID and Variant Secret parameters.  You will need them below.

 3. Link the library to your project.  This project has not yet been published to any Maven repositories, but once it has
    then you can add the following line to the `dependencies` section of your `build.gradle` file:

        compile 'com.pivotal.mss.pushsdk:1.0.0-RELEASE'

    Note that the version name may be different.

	Even if you don't have access to a Maven repository with this library, you could still link to the source of this module,
	or simply obtain the compiled AAR files.  Please contact the Pivotal Mobile Services Suite team for help.

 4. You will need define and use the following `permission` in the `manifest` element of your application's
    `AndroidManifest.xml` file.  Ensure that the base of the permission name is your application's package name:

        <permission
            android:name="YOUR.PACKAGE.NAME.permission.C2D_MESSAGE"
            android:protectionLevel="signature" />
        <uses-permission android:name="YOUR.PACKAGE.NAME.permission.C2D_MESSAGE" />

 5. You will need to add the following `receiver` to the `application` element of your application's
    `AndroidManifest.xml` file.  Ensure that you set the category name to your application's package name:

        <receiver
            android:name="com.pivotal.mss.pushsdk.broadcastreceiver.GcmBroadcastReceiver"
            android:permission="com.google.android.c2dm.permission.SEND">
            <intent-filter>
                <action android:name="com.google.android.c2dm.intent.RECEIVE"/>
                <category android:name="YOUR.PACKAGE.NAME"/>
            </intent-filter>
        </receiver>

 6. Add the following lines of code to the initialization section of your application.  You will need a `Context` object
    to pass to the `PushLib.init` method, so you should try to add this code to your `Application` class or to one of
    your `Activity` class.

		// Initialize the Push SDK.
		final PushSDK pushSDK = PushSDK.getInstance(this);

		// Setup the Analytics SDK.
		final URL analyticsServerUrl = new URL(ANALYTICS_BASE_SERVER_URL);
		final AnalyticsParameters analyticsParameters = new AnalyticsParameters(IS_ANALYTICS_ENABLED, analyticsServerUrl);
		pushSDK.setupAnalytics(analyticsParameters);

		// Register for push notifications.  The listener itself is optional (may be null).
		final URL pushServerUrl = new URL(PUSH_BASE_SERVER_URL);
		final RegistrationParameters parameters = new RegistrationParameters(GCM_SENDER_ID, VARIANT_UUID, VARIANT_SECRET, DEVICE_ALIAS, pushServerUrl);
		pushSDK.startRegistration(parameters, new RegistrationListener() {

			@Override
			public void onRegistrationComplete() {
				// Registration successful
			}

			@Override
			public void onRegistrationFailed(String reason) {
				// Registration failed. See reason.
			}
		});

    The `GCM_SENDER_ID`, `VARIANT_UUID`, and `VARIANT_SECRET` are described above.  The `DEVICE_ALIAS` is a custom field that
    you can use to differentiate this device from others in your own push messaging campaigns.  You can leave it empty
    if you'd like.

	Both the Analytics and Push Client SDKs require you to specify the base URL for your servers in the `ANALYTICS_BASE_SERVER_URL`
	and `PUSH_BASE_SERVER_URL` parameters.

	If you do not want to use the Pivotal Mobile Services Suite Analytics Client SDK then set `IS_ANALYTICS_ENABLED` to
	`false`.  If you are not using the Analytics Client SDK then you may provide a `null` value for the
	`ANALYTICS_BASE_SERVER_URL` parameter.

	You must call the `setupAnalytics` method in your `PushSDK` class instance before calling `startRegistration`.

    You should only have to call `startRegistration` once in the lifetime of your process -- but calling it more times
	is not harmful.

    The `startRegistration` method is asynchronous and will return before registration is complete.  If you need to know
    when registration is complete (or if it fails), then provide a `RegistrationListener` as the second argument.

    The Pivotal Mobile Services Suite Push SDK takes care of the following tasks for you:

        * Checking for Google Play Services.
        * Registering with Google Play Services.
        * Saving your registration ID.
        * Sending your registration ID to the back-end (i.e.: the Pivotal Mobile Services Suite).
        * Re-registering after the application version, or any other registration parameters are updated.

 7. To receive push notifications in your application, you will need to add a "broadcast receiver" to your application.
    The intent that GCM sends is provided in the "gcm_intent" parcelable extra of the intent passed to your receiver's
    `onReceive` method.  Here is a simple example:

        public class MyBroadcastReceiver extends WakefulBroadcastReceiver {

            public static final int NOTIFICATION_ID = 1;

            @Override
            public void onReceive(Context context, Intent intent) {

                final Intent gcmIntent = intent.getExtras().getParcelable("gcm_intent");
                final Bundle extras = gcmIntent.getExtras();
                final GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
                final String messageType = gcm.getMessageType(gcmIntent);

                if (!extras.isEmpty()) {

                    if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {

                        // Post notification of received message.
                        String message;
                        if (extras.containsKey("message")) {
                            message = "Received: \"" + extras.getString("message") + "\".";
                        } else {
                            message = "Received message with no extras.";
                        }
                        sendNotification(context, message);
                    }
                }

                MyBroadcastReceiver.completeWakefulIntent(intent);
            }

            // Put the message into a notification and post it.
            private void sendNotification(Context context, String msg) {
                final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                // Set up the notification to open MainActivity when the user touches it
                final PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, MyActivity.class), 0);

                final NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(context)
                                .setSmallIcon(R.drawable.your icon)
                                .setContentTitle("Your application name")
                                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                                .setContentText(msg);

                builder.setContentIntent(contentIntent);
                notificationManager.notify(NOTIFICATION_ID, builder.build());
            }
        }

 8. Finally, you will need to declare your broadcast receiver in your `AndroidManifest.xml` file.  The base of the
    action name in the intent filter should be the same as your application's package name:

         <receiver
             android:name=".broadcastreceiver.MyBroadcastReceiver">
             <intent-filter>
                 <action android:name="YOUR.PACKAGE.NAME.pmsspushsdk.RECEIVE_PUSH"/>
             </intent-filter>
         </receiver>


Building the SDKs themselves
----------------------------

You can build this project directly from the command line using Gradle or in Android Studio.

The library depends on the following libraries:

 * Google Android Application Compatibility (com.android.support:appcompat)
 * Google GSON - should be in the Maven Central repository
 * Google Play Services - should be provided in your Android SDK distribution. If you don't have iton your computer,
                          then run the Android SDK Manager (run `android` from your command line) and install it.  You
                          should be able to find it in the "Extras" section.

To load this project in Android Studio, you will need to select "Import Project" and select the `build.gradle` file in
the project's base directory.

To build the project from the command line, run the command `./gradlew clean assemble`.  If you have a device connected
to your computer then you can also run the unit test suite with the command `./gradlew connectedCheck`.

Modules in the Repository
-------------------------

 1. AnalyticsSDK - the source code for the Analytics Client SDK itself. Includes Android JUnit tests in the
    `androidTest` directory.
 2. AnalyticsSDK-Sample - an application that can be used to demonstrate the Analytics SDK (described below).
 3. Common - code common to both the AnalyticsSDK and PushSDK modules.
 4. CommonTest - code common to the Android JUnit tets for the AnalyticsSDK and PushSDK modules.
 5. PushSDK - the source code for the Push Client SDK itself. Includes Android JUnit tests in the `androidTest`
    directory.  The PushSDK depends on the AnalyticsSDK to handle 
 6. PushSDK-Sample - an application that can be used to demonstrate the Push SDK (described below).
 7. PushSDK-SimpleDemoApp - the simplest possible application that links to and demonstrates the Push Client
    SDK. (described below).
 8. SampleAppCore - code common to both the AnalyticsSDK-Sample and PushSDK-Sample modules.

Staging Server
--------------

At this time, the library is hard coded to use the staging server on Amazon AWS.  You can confirm the current server
by looking at the `BACKEND_REGISTRATION_REQUEST_URL` string value in `Const.java`.  The intent is to change this value
to point to a production server when it is available.

Push Simple Demo Application
----------------------------

The Push Simple Demo Application is an example of the simplest application possible that uses the Pivotal Mobile Services Suite
Push Client SDK.  At this time, it only demonstrates how to register for push notifications.

This demo application registers for push notifications in the Activity object in order to make it easier to display the
output on the screen.  It is probably more appropriate for you to register for push notifications in your Application
object instead.

This application is set up to receive push messages via the `MyPivotalMSSRemotePushLibBroadcastReceiver` class.  These
messages are not displayed in the activity window, but they will display a status bar notification.

Push Sample Application
-----------------------

There is a small sample application included in this repository to demonstrate and exercise the features in the Push
Client SDK.

You can use this sample application to test registration against Google Cloud Messaging (GCM) and the Pivotal Mobile Services Suite
back-end server for push messages.  Any push messages that are received are printed to the log window.  Although not
currently supported by the library itself, you can also send push messages with the sample application itself.

At this time, the sample application uses a dummy project on Google Cloud Console.  It is recommend that you create your
own test Google API Project by following the directions at http://developer.android.com/google/gcm/gs.html.

You can save your own project details by editing the values in the sample project's `push_default_preferences.xml`
and `analytics_default_preferences.xml` resource files.

Watch the log output in the sample application's display to see what the Push SDK is doing in the background.  This
log output should also be visible in the Android device log (for debug builds), but the sample application registers a
"listener" with the Push Library's logger so it can show you what's going on.

Rotate the display to landscape mode to see the captions for the action bar buttons.

Press the `Register` button in the sample application action bar to ask the Push SDK to register the device.  If the
device is not already registered, then you should see a lot of output scroll by as the library registers with both
GCM and the Pivotal Mobile Services Suite.  If the device is already registered then the output should be shorter.

Press the `Unregister` button in the sample application action bar to ask the Push SDK to unregister the device.  This
unregister option will unregister with GCM and with the Pivotal Mobile Services Suite.

You can clear all or parts of the saved registration data with the `Clear Registration` action bar option.  Clearing
part or all of the registration data will cause a partial or complete re-registration the next time you press the
`Register` button.  Unlike the `Unregister` button, the `Clear Registration` button simply causes the Push Client SDK
to "forget" that it is registered.  Both GCM and Pivotal Mobile Services Suite will still think that the device is
registered.

You can use the `Clear Unsent Events` button to clear the database of any Analytics events that have yet to be sent
to the server.

You can change the registration preferences at run-time by selecting the `Edit Preferences` action bar item.  Selecting
this item will load the Preferences screen.  There's no space to describe the options on the Preferences screen itself,
but you can look in the `push_default_preferences.xml` and `analytics_default_preferences.xml` resource files for more
details.

You can reset the registration preferences to the default values by selecting the `Reset to Defaults` action bar item in
the Preferences screen.

The sample application is also set up to receive push messages once the device has been registered with GCM and
the Pivotal Mobile Services Suite.  Any messages that are received are printed to the log window.

Although the Push Client SDK has no support for sending push messages, the Push Sample App can do it for you as long
as it is set up with the correct `GCM Browser API Key` parameter (when sending messages via GCM or the correct
`Environment UUID` and `Environment Key` parameters (when sending messages via Pivotal Mobile Services Suite).  The
application can not distinguish between messages sent via the two services.

Analytics Sample Application
----------------------------

This application is similar to the Push Sample Application, but somewhat simpler in nature since it deals only with the
Analytics Client SDK.

You can use the "Log xxxxx" button to log events of different types.  Using the log window, you should see the analytics
events build up in the database.  After a short time (about one or two minutes in debug builds) you should see any unsent
events sent to the server (unless you have cleared them with the `Clear Unsent Events` button).

You can change the Analytics Preferences with the `Edit Preferences` option.
