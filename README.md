Android Push Client SDK
=======================

Features
--------

The Pivotal Mobile Services Suite Push and Analytics Client SDKs are light-weight tools that will help your application:

 1. Register for push notifications with Google Cloud Messaging (GCM) and an instance of the Pivotal Mobile Services Suite
    push messaging server.

 2. Receive push messages sent via the same frameworks.


Device Requirements
-------------------

The Push SDK requires API level 10 or greater.

The Google Play Services application must be installed on the device before you can register your device or receive
push messages.  Most devices should already have this application installed, but some odd ones may not.  By default,
Android Virtual Devices (i.e.: emulators) do not have Google Play Services installed.

Getting Started
---------------

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

        compile 'io.pivotal.android:push:1.0.0'

    Note that the version name may be different.

	Even if you don't have access to a Maven repository with this library, you could still link to the source of this module,
	or simply obtain the compiled AAR files.  Please contact the Pivotal Mobile Services Suite team for help.

 4. You will need to define and use the following `permission` in the `manifest` element of your application's
    `AndroidManifest.xml` file.  Ensure that the base of the permission name is your application's package name:

        <permission
            android:name="[YOUR.PACKAGE.NAME].permission.C2D_MESSAGE"
            android:protectionLevel="signature" />

        <uses-permission android:name="[YOUR.PACKAGE.NAME].permission.C2D_MESSAGE" />

 5. You will need to add the following `receiver` to the `application` element of your application's
    `AndroidManifest.xml` file.  Ensure that you set the category name to your application's package name:

        <receiver
            android:name="io.pivotal.android.push.receiver.GcmBroadcastReceiver"
            android:permission="com.google.android.c2dm.permission.SEND">
            <intent-filter>
                <action android:name="com.google.android.c2dm.intent.RECEIVE"/>
                <category android:name="[YOUR.PACKAGE.NAME]"/>
            </intent-filter>
        </receiver>

 6. Add the following lines of code to the initialization section of your application.  You will need a `Context` object
    to pass to the `getInstance` method, so you should try to add this code to your `Application` class or to one of
    your `Activity` class.

        final RegistrationParameters parameters = new RegistrationParameters(
		    GCM_SENDER_ID, VARIANT_UUID, VARIANT_SECRET, DEVICE_ALIAS, PUSH_BASE_SERVER_URL
		);

		Push.getInstance(this).startRegistration(parameters);

    The `GCM_SENDER_ID`, `VARIANT_UUID`, and `VARIANT_SECRET` are described above.  The `DEVICE_ALIAS` is a custom field that
    you can use to differentiate this device from others in your own push messaging campaigns.  You can leave it empty
    if you'd like. The `PUSH_BASE_SERVER_URL` parameter is the base url of your push server.

    You should only have to call `startRegistration` once in the lifetime of your process -- but calling it more times
	is not harmful. The `startRegistration` method is asynchronous and will return before registration is complete.  If you 
	need to know when registration is complete (or if it fails), then provide a `RegistrationListener` as the second argument.

    The Pivotal Mobile Services Suite Push SDK takes care of the following tasks for you:

        * Checking for Google Play Services.
        * Registering with Google Play Services.
        * Saving your registration ID.
        * Sending your registration ID to the back-end (i.e.: the Pivotal Mobile Services Suite).
        * Re-registering after the application version, or any other registration parameters are updated.

 7. To receive push notifications in your application, you will need to add a custom `Service` to your application that
    extends the `GcmService` provided in the SDK. The intent that GCM sends is passed to your service's `onReceive` method.  
    Here is a simple example:

        public class MyPushService extends GcmService {

            @Override
            public void onReceiveMessage(Bundle payload) {
                if (payload.containsKey("message")) {
                    final String message = payload.getString("message");
                    handleMessage(message);
                }
            }

            private void handleMessage(String msg) {
                // Your code here
            }
        }

 8. Finally, you will need to declare your service in your `AndroidManifest.xml` file.

         <service android:name=".service.MyPushService" android:exported="false" />


Building the SDK
----------------

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
