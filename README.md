Android Push Client SDK
=======================

The Push SDK requires API level 14 or greater.

The Push Android SDK v1.0.4 is compatible with the Push Notification Service
1.2.0

Push SDK Usage
--------------

For more information please visit the [docs site](http://docs.pivotal.io/mobile/push/android/)

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


