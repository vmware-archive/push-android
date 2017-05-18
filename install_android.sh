#!/bin/sh

echo y | $ANDROID_HOME/tools/android update sdk --no-ui -a --filter android-24,build-tools-25.0.0,extra-android-m2repository,extra-google-m2repository
